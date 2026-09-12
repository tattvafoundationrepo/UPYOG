package org.egov.demand.model;

/**
 * Report-type codes stored on each emarket FI row (column report_type) so the
 * /fi/report/_get API can bifurcate beyond the demand-vs-collection split.
 * The label is set in the calling/orchestration layer only — the accounting
 * builders (amounts / GL codes / posting keys) are unchanged.
 */
public final class FiReportType {

    private FiReportType() {}

    /** Market Collection — normal receipt/payment collection (regular/deposit). */
    public static final String UPMKT_COLL = "upmktcoll";
    /** Market Collection Reversal — receipt cancellation. */
    public static final String UPMKT_COLREV = "upmktcolrev";
    /** Market Demand — demand creation. */
    public static final String UPMKT_DEMD = "upmktdemd";
    /** Market Demand Reversal — demand cancellation/reversal. */
    public static final String UPMKT_DEMDREV = "upmktdemdrev";
    /** Market Demand Discheque — dishonoured cheque demand. */
    public static final String UPMKT_DISCHQ = "upmktdischq";
    /** Market Demand against Advance — demand apportioned against an advance. */
    public static final String UPMKT_DEMDADV = "upmktdemdadv";

    /**
     * The per-tax-head 4-Series pair on a demand raised against an advance: Dr the head's
     * receivable account (431409937..431409977), Cr the head's revenue account, for the amount
     * settled out of the advance.
     *
     * <p>Carries its own type rather than riding on {@link #UPMKT_DEMDADV} because the GST return
     * selects on report_type. emarket-v1's GstReturnQueryBuilder B2CS query filters
     * {@code report_type IN ('upmktdemd','upmktdemdadv','upmktdemdrev')} and signs posting key 50
     * positive / 40 negative, so a pair tagged as an ordinary demand would file a taxable supply
     * against a receivable account and double-count the revenue leg. Neither this constant nor
     * {@link #UPMKT_DEMDADV_4S_REV} may ever be added to that IN list.
     *
     * <p>Must stay in step with the emarket-v1 copy of this class.
     */
    public static final String UPMKT_DEMDADV_4S = "upmktdemdadv4s";

    /** The mirror of {@link #UPMKT_DEMDADV_4S}, written when the demand or its receipt is reversed. */
    public static final String UPMKT_DEMDADV_4S_REV = "upmktdemdadv4srev";
}
