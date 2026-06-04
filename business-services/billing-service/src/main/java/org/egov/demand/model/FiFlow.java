package org.egov.demand.model;

/**
 * Distinct emarket collection flows. Each maps to a fixed set of financial
 * (FI) report rows that must be inserted into eg_emarket_fi_report_collection
 * at the time of collection (and mirrored, with posting keys swapped, on
 * receipt cancellation).
 */
public enum FiFlow {
    NON_GST_REGULAR,
    GST_REGULAR,
    NON_GST_ADVANCE,
    GST_ADVANCE,
    DEPOSIT
}
