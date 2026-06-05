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
}
