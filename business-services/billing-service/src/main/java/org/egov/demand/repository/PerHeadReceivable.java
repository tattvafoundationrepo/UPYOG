package org.egov.demand.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Arithmetic for the per-tax-head receivable of BMC's 24-Sep-2026 accounting entries
 * ("dUpyog Accounting Entry", AO FAR).
 *
 * <p>A demand debits one 4-Series receivable per revenue head instead of one control account, and
 * each head's debit is GROSS: the head plus its share of the demand's CGST and SGST. The PDF's
 * illustration is Stallage 100 + Loft 10 with GST 19.80 and a 0.20 round-off: Dr Stallage 118,
 * Dr Loft 11.80, Dr Coin note 0.20.
 *
 * <p>Pure functions only, so every rule here is testable without a database. The one invariant
 * every caller relies on: the lines always sum to exactly the amount they break down. Callers only
 * ever change WHICH account carries an amount, never the amount, so a voucher balances exactly as
 * it did before this existed.
 */
public final class PerHeadReceivable {

    private static final int SCALE = 2;

    private PerHeadReceivable() {
    }

    /** One posted detail of a demand, as the FI builder sees it. */
    public static final class Head {
        final String taxHeadCode;
        final BigDecimal amount;
        /** The 4-Series GL this head's receivable posts to, already resolved by the caller. */
        final String receivableGl;

        public Head(String taxHeadCode, BigDecimal amount, String receivableGl) {
            this.taxHeadCode = taxHeadCode;
            this.amount = amount;
            this.receivableGl = receivableGl;
        }
    }

    /** One receivable debit: the head it belongs to (the row's remark), its GL and its amount. */
    public static final class Line {
        public final String taxHeadCode;
        public final String glCode;
        public final BigDecimal amount;

        Line(String taxHeadCode, String glCode, BigDecimal amount) {
            this.taxHeadCode = taxHeadCode;
            this.glCode = glCode;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return taxHeadCode + "@" + glCode + "=" + amount;
        }
    }

    /**
     * CGST / SGST. Their amount is folded into the revenue heads' receivables, never posted on a
     * receivable of its own. Round-off heads (GST_CA) contain "GST" too and are excluded.
     */
    static boolean isGstHead(String th) {
        return th != null && th.toUpperCase(Locale.ROOT).contains("GST") && !isRoundOffHead(th);
    }

    /**
     * The paise round-off ("Coin note" in BMC's entries): GST_CA on rent, RENT_PENALTY_CA /
     * LICENSE_PENALTY_CA and COIN_ADJUSTMENT on penalties. Posts on its own receivable line.
     */
    static boolean isRoundOffHead(String th) {
        if (th == null)
            return false;
        String u = th.toUpperCase(Locale.ROOT);
        return u.equals("GST_CA") || u.endsWith("_CA") || u.equals("COIN_ADJUSTMENT");
    }

    /** Advance vessels and carry-forwards are not revenue; their lines are netted, not split out. */
    static boolean isAdvanceHead(String th) {
        return th != null && th.toUpperCase(Locale.ROOT).contains("ADVANCE");
    }

    /**
     * Breaks a demand's receivable into per-head lines.
     *
     * <p>The rules are:
     * <ul>
     *   <li>Each revenue head with a positive amount gets one line on its own receivable GL.</li>
     *   <li>That line also carries the head's proportional share of CGST + SGST.</li>
     *   <li>Each positive round-off head gets its own line on {@code coinGl}, or on its own GL when
     *       {@code coinGl} is null (no round-off receivable configured; user decision 21-10-2026: never NA).</li>
     *   <li>Anything else, such as a negative advance carry-forward or a negative round-off, is
     *       netted into the revenue lines in proportion.</li>
     * </ul>
     *
     * @param heads           every non-netting detail with a non-zero amount, in posting order
     * @param totalReceivable what the single receivable line would have carried
     * @param coinGl          the round-off receivable GL, or null to post each round-off on its own GL
     * @return the lines, which sum to exactly {@code totalReceivable}. An empty list means the
     *         demand has no revenue head to split by (an advance vessel, for instance); the caller
     *         keeps the single receivable line it has always posted.
     */
    public static List<Line> lines(List<Head> heads, BigDecimal totalReceivable, String coinGl) {

        Map<Integer, BigDecimal> base = new LinkedHashMap<>();
        BigDecimal gst = BigDecimal.ZERO;
        BigDecimal accounted = BigDecimal.ZERO;
        List<Line> coinLines = new ArrayList<>();

        for (int i = 0; i < heads.size(); i++) {
            Head h = heads.get(i);
            if (h.amount == null || h.amount.signum() == 0)
                continue;
            if (isRoundOffHead(h.taxHeadCode) && h.amount.signum() > 0) {
                // A negative round-off is left to the netting below: a negative receivable line
                // could never be cleared by a collection.
                coinLines.add(new Line(h.taxHeadCode, coinGl != null ? coinGl : h.receivableGl, h.amount));
                accounted = accounted.add(h.amount);
            } else if (isGstHead(h.taxHeadCode)) {
                gst = gst.add(h.amount);
                accounted = accounted.add(h.amount);
            } else if (!isAdvanceHead(h.taxHeadCode) && h.amount.signum() > 0) {
                base.put(i, h.amount);
                accounted = accounted.add(h.amount);
            }
        }

        if (base.isEmpty())
            return Collections.emptyList();

        Map<Integer, BigDecimal> gstShare = splitByWeights(gst, base);
        Map<Integer, BigDecimal> gross = new LinkedHashMap<>();
        for (Map.Entry<Integer, BigDecimal> e : base.entrySet())
            gross.put(e.getKey(), e.getValue().add(gstShare.getOrDefault(e.getKey(), BigDecimal.ZERO)));

        BigDecimal other = totalReceivable.subtract(accounted);
        Map<Integer, BigDecimal> otherShare = splitByWeights(other, gross);

        List<Line> out = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> e : gross.entrySet()) {
            Head h = heads.get(e.getKey());
            out.add(new Line(h.taxHeadCode, h.receivableGl,
                    e.getValue().add(otherShare.getOrDefault(e.getKey(), BigDecimal.ZERO))));
        }
        out.addAll(coinLines);

        BigDecimal sum = BigDecimal.ZERO;
        for (Line l : out)
            sum = sum.add(l.amount);
        if (sum.compareTo(totalReceivable) != 0)
            throw new IllegalStateException("per-head receivable " + sum + " != total " + totalReceivable);
        return out;
    }

    /**
     * Splits {@code amount} across the keys in proportion to their (positive) weights, rounded to
     * paise. The rounding difference goes to the largest weight, so the parts always sum to exactly
     * {@code amount}. Non-positive weights take nothing. Returns an empty map when there is nothing
     * to split by or nothing to split.
     */
    public static <K> Map<K, BigDecimal> splitByWeights(BigDecimal amount, Map<K, BigDecimal> weights) {
        Map<K, BigDecimal> out = new LinkedHashMap<>();
        if (amount == null || amount.signum() == 0)
            return out;
        BigDecimal total = BigDecimal.ZERO;
        K largest = null;
        for (Map.Entry<K, BigDecimal> e : weights.entrySet()) {
            if (e.getValue() == null || e.getValue().signum() <= 0)
                continue;
            total = total.add(e.getValue());
            if (largest == null || e.getValue().compareTo(weights.get(largest)) > 0)
                largest = e.getKey();
        }
        if (largest == null)
            return out;

        BigDecimal assigned = BigDecimal.ZERO;
        for (Map.Entry<K, BigDecimal> e : weights.entrySet()) {
            if (e.getValue() == null || e.getValue().signum() <= 0)
                continue;
            BigDecimal part = amount.multiply(e.getValue()).divide(total, SCALE, RoundingMode.HALF_UP);
            out.put(e.getKey(), part);
            assigned = assigned.add(part);
        }
        out.put(largest, out.get(largest).add(amount.subtract(assigned)));
        return out;
    }

    /**
     * Allocates a non-negative {@code amount} against open balances, never putting more on a key
     * than its balance.
     *
     * <p>If the amount covers every balance, each balance is cleared exactly. Otherwise the amount is
     * split in proportion, rounded DOWN to paise, and the leftover paise go to the largest balances
     * first, each up to its own cap. Whatever cannot be placed (amount above the balances' total) is
     * left out; the caller posts it elsewhere. Non-positive balances take nothing.
     */
    public static <K> Map<K, BigDecimal> allocateCapped(BigDecimal amount, Map<K, BigDecimal> balances) {
        Map<K, BigDecimal> out = new LinkedHashMap<>();
        if (amount == null || amount.signum() <= 0)
            return out;
        Map<K, BigDecimal> caps = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<K, BigDecimal> e : balances.entrySet()) {
            if (e.getValue() != null && e.getValue().signum() > 0) {
                caps.put(e.getKey(), e.getValue());
                total = total.add(e.getValue());
            }
        }
        if (caps.isEmpty())
            return out;
        if (amount.compareTo(total) >= 0) {
            out.putAll(caps);
            return out;
        }

        BigDecimal assigned = BigDecimal.ZERO;
        for (Map.Entry<K, BigDecimal> e : caps.entrySet()) {
            BigDecimal part = amount.multiply(e.getValue()).divide(total, SCALE, RoundingMode.DOWN);
            out.put(e.getKey(), part);
            assigned = assigned.add(part);
        }
        BigDecimal left = amount.subtract(assigned);
        List<K> byCap = new ArrayList<>(caps.keySet());
        byCap.sort((a, b) -> caps.get(b).compareTo(caps.get(a)));
        for (K k : byCap) {
            if (left.signum() <= 0)
                break;
            BigDecimal room = caps.get(k).subtract(out.get(k));
            BigDecimal add = room.min(left);
            if (add.signum() > 0) {
                out.put(k, out.get(k).add(add));
                left = left.subtract(add);
            }
        }
        out.entrySet().removeIf(e -> e.getValue().signum() == 0);
        return out;
    }
}
