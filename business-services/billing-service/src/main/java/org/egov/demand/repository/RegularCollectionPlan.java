package org.egov.demand.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egov.demand.model.FiDimensions;

/**
 * How the regular (non-advance) part of one receipt is credited, under BMC's 24-Sep-2026 entries:
 * "Interim Receipt A/C Dr 130, To 431409938 Revenue Stallage A/C Cr 118, 431409957 Revenue Loft A/C
 * Cr 11.8, 4 Series Coin note A/C Cr 0.20", and, after a dishonour, the charges straight to income.
 *
 * <p>Settles as posted: each paid demand is credited on the receivable accounts its own demand
 * document debited, read from the FI tables as an open balance per GL, never re-derived from today's
 * configuration. A demand posted on the single control account therefore keeps clearing that
 * account, and a demand with no FI document at all (SAP-migrated) keeps today's credit.
 *
 * <p>Changes only WHICH account carries the money. {@link #creditsFor} always returns credits that
 * sum to exactly the amount it was given; anything it cannot place goes on {@code fallbackGl}, which is
 * where the whole amount went before this existed.
 *
 * <p>{@link #isActive()} is false unless some paid demand actually carries a balance on a
 * per-head account, or is a dishonour charge recognised at collection. The caller then posts its
 * original single line, so a receipt that touches nothing new is byte-identical to before.
 */
public final class RegularCollectionPlan {

    /** One income credit for a dishonour charge recognised at collection. */
    public static final class Charge {
        final String taxHeadCode;
        final String glCode;
        final BigDecimal paid;
        final FiDimensions dimensions;

        public Charge(String taxHeadCode, String glCode, BigDecimal paid, FiDimensions dimensions) {
            this.taxHeadCode = taxHeadCode;
            this.glCode = glCode;
            this.paid = paid;
            this.dimensions = dimensions;
        }
    }

    /** One demand this receipt paid, in bill order. */
    public static final class PaidDemand {
        final String demandId;
        final BigDecimal paid;
        /** Open receivable per GL from the FI tables; empty when the demand posted none. */
        final Map<String, BigDecimal> openByGl;
        /** Non-empty only for a dishonour charge recognised at collection. */
        final List<Charge> charges;

        public PaidDemand(String demandId, BigDecimal paid, Map<String, BigDecimal> openByGl, List<Charge> charges) {
            this.demandId = demandId;
            this.paid = paid == null ? BigDecimal.ZERO : paid;
            this.openByGl = openByGl == null ? Collections.<String, BigDecimal>emptyMap() : openByGl;
            this.charges = charges == null ? Collections.<Charge>emptyList() : charges;
        }
    }

    /** One credit leg of the collection voucher. */
    public static final class Credit {
        public final String glCode;
        public final BigDecimal amount;
        /** The demand this credit clears; becomes the row's transaction_number. */
        public final String demandId;
        public final String remarks;
        /** Null to keep the licensee's market dimensions, as every receivable leg always has. */
        public final FiDimensions dimensions;

        Credit(String glCode, BigDecimal amount, String demandId, String remarks, FiDimensions dimensions) {
            this.glCode = glCode;
            this.amount = amount;
            this.demandId = demandId;
            this.remarks = remarks;
            this.dimensions = dimensions;
        }

        @Override
        public String toString() {
            return demandId + ":" + glCode + "=" + amount;
        }
    }

    static final String RECEIVABLE_REMARK = "Receivable from Mun Mkt";

    private final List<PaidDemand> demands;
    private final String fallbackGl;
    private final Set<String> legacyReceivableGls;

    /**
     * @param demands             the paid demands, in bill order
     * @param fallbackGl          today's single receivable (431409936)
     * @param legacyReceivableGls control accounts that predate the per-head receivable; a balance on
     *                            one of them alone does not activate the plan
     */
    public RegularCollectionPlan(List<PaidDemand> demands, String fallbackGl, Set<String> legacyReceivableGls) {
        this.demands = demands == null ? Collections.<PaidDemand>emptyList() : demands;
        this.fallbackGl = fallbackGl;
        this.legacyReceivableGls = legacyReceivableGls;
    }

    public boolean isActive() {
        for (PaidDemand d : demands) {
            if (!d.charges.isEmpty())
                return true;
            for (Map.Entry<String, BigDecimal> e : d.openByGl.entrySet())
                if (e.getValue() != null && e.getValue().signum() > 0 && !legacyReceivableGls.contains(e.getKey()))
                    return true;
        }
        return false;
    }

    /**
     * The credit legs for {@code amount}, which is the regular part of the receipt (the whole receipt
     * for a pure regular collection, the arrears half of a split mixed receipt). Sums to exactly
     * {@code amount}.
     *
     * @param firstDemandId the voucher's own transaction number, carried by any fallback credit
     *                      exactly as the single line always was
     */
    public List<Credit> creditsFor(BigDecimal amount, String firstDemandId) {
        List<Credit> out = new ArrayList<>();
        if (amount == null || amount.signum() <= 0)
            return out;
        BigDecimal available = amount;

        for (PaidDemand d : demands) {
            if (available.signum() <= 0)
                break;
            if (!d.charges.isEmpty()) {
                for (Charge c : d.charges) {
                    BigDecimal take = c.paid.min(available);
                    if (take.signum() <= 0 || c.glCode == null)
                        continue;
                    out.add(new Credit(c.glCode, take, d.demandId, c.taxHeadCode, c.dimensions));
                    available = available.subtract(take);
                }
                continue;
            }
            BigDecimal want = d.paid.min(available);
            Map<String, BigDecimal> placed = PerHeadReceivable.allocateCapped(want, d.openByGl);
            for (Map.Entry<String, BigDecimal> e : placed.entrySet()) {
                out.add(new Credit(e.getKey(), e.getValue(), d.demandId, RECEIVABLE_REMARK, null));
                available = available.subtract(e.getValue());
            }
        }

        if (available.signum() > 0)
            out.add(new Credit(fallbackGl, available, firstDemandId, RECEIVABLE_REMARK, null));
        return merge(out);
    }

    /** Receivable credits on the same demand and GL are one leg; a charge credit is always its own. */
    private static List<Credit> merge(List<Credit> credits) {
        Map<String, Credit> byKey = new LinkedHashMap<>();
        int i = 0;
        for (Credit c : credits) {
            String key = c.dimensions != null ? "charge#" + (i++)
                    : c.demandId + "|" + c.glCode + "|" + c.remarks;
            Credit prev = byKey.get(key);
            byKey.put(key, prev == null ? c
                    : new Credit(c.glCode, prev.amount.add(c.amount), c.demandId, c.remarks, c.dimensions));
        }
        return new ArrayList<>(byKey.values());
    }
}
