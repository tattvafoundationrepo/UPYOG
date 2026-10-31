package org.egov.demand.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.egov.demand.model.FiReport;

/**
 * One line per GL on a collection voucher (BMC, 30-10-2026): a receipt that pays several months used to
 * post one credit per month on the same receivable, so a seven-month receipt carried seven identical
 * {@code 431409938 637.20} lines. Legs that agree on EVERY column SAP receives are merged into one, with
 * the amounts added; anything that tells two legs apart (posting key, GL, date, dimensions, remarks,
 * assignment, ...) keeps them apart.
 *
 * <p>Nothing is lost: the merged leg records in {@link FiReport#getDemandSplit()} which demand each part
 * cleared, which is what the open-receivable read and the reversal mirror need. Pure: no I/O, the input
 * rows are not modified, and a voucher with no two mergeable legs comes back as the same rows.
 */
public final class CollectionFiRowMerger {

    private CollectionFiRowMerger() {
    }

    /**
     * @param rows       one receipt's collection legs, in posting order
     * @param neverMerge legs kept exactly as built, such as the money leg: a mixed receipt's advance and
     *                   regular vouchers each carry one, and the report labels them apart by amount
     * @return the legs with every mergeable group replaced, at its first leg's position, by one leg
     */
    public static List<FiReport> merge(List<FiReport> rows, Predicate<FiReport> neverMerge) {
        if (rows == null || rows.size() < 2)
            return rows;

        Map<List<Object>, List<FiReport>> groups = new LinkedHashMap<>();
        List<Object> order = new ArrayList<>();
        for (FiReport r : rows) {
            if (!mergeable(r, neverMerge)) {
                order.add(r);
                continue;
            }
            List<Object> key = key(r);
            List<FiReport> group = groups.get(key);
            if (group == null) {
                group = new ArrayList<>();
                groups.put(key, group);
                order.add(key);
            }
            group.add(r);
        }

        List<FiReport> out = new ArrayList<>(rows.size());
        for (Object o : order) {
            if (o instanceof FiReport) {
                out.add((FiReport) o);
                continue;
            }
            List<FiReport> group = groups.get(o);
            out.add(group.size() == 1 ? group.get(0) : merged(group));
        }
        return out;
    }

    private static boolean mergeable(FiReport r, Predicate<FiReport> neverMerge) {
        return r != null
                && r.getCollectionAmount() != null
                // The part a leg cleared is keyed by its demand; without one it could not be split back.
                && notBlank(r.getTransactionNumber())
                // A leg with no receipt or licence belongs to no voucher, so it can only speak for itself.
                && notBlank(r.getDocumentHeaderText())
                && notBlank(r.getReferenceNo())
                && (neverMerge == null || !neverMerge.test(r));
    }

    /** Every stored column except the amount, the demand it cleared, and the audit timestamps. */
    private static List<Object> key(FiReport r) {
        return Arrays.asList(r.getDocDate(), r.getPostingDate(), r.getReferenceNo(), r.getDocumentHeaderText(),
                r.getPostingKey(), r.getGlCode(), r.getFund(), r.getFundCentre(), r.getFunctionalArea(),
                r.getBusinessArea(), r.getRemarks(), r.getPaymentModeDetails(), r.getIsNew(), r.getDocType(),
                r.getCostCenter(), r.getCommitmentItem(), r.getReportType(), r.getAssignment());
    }

    private static FiReport merged(List<FiReport> group) {
        FiReport first = group.get(0);
        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> split = new LinkedHashMap<>();
        for (FiReport r : group) {
            total = total.add(r.getCollectionAmount());
            // A leg that is itself a merge (a mirrored reversal) contributes its parts, not its first demand.
            if (r.getDemandSplit() != null && !r.getDemandSplit().isEmpty())
                r.getDemandSplit().forEach((demand, amount) -> split.merge(demand, amount, BigDecimal::add));
            else
                split.merge(r.getTransactionNumber(), r.getCollectionAmount(), BigDecimal::add);
        }
        return first.toBuilder()
                .collectionAmount(total)
                .demandSplit(split)
                .build();
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
