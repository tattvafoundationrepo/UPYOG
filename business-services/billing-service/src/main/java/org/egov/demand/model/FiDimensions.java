package org.egov.demand.model;

/**
 * The four SAP dimensions an FI row carries: fund, fund centre, functional area and business area.
 *
 * <p>Held together on purpose. They describe one accounting location, and a voucher leg carrying
 * one ward's business area beside another ward's fund centre is worse than one carrying neither —
 * it looks reconciled and is not. Every resolver either returns a complete set or returns null.
 *
 * <p>Used for the collecting-CFC dimensions on a collection voucher's debit legs. The licensee's
 * market dimensions still travel on the {@link Demand} itself, so the two are never confused.
 */
public final class FiDimensions {

    private final String fund;
    private final String fundCentre;
    private final String businessArea;
    private final String functionalArea;

    public FiDimensions(String fund, String fundCentre, String businessArea, String functionalArea) {
        this.fund = fund;
        this.fundCentre = fundCentre;
        this.businessArea = businessArea;
        this.functionalArea = functionalArea;
    }

    public String getFund() {
        return fund;
    }

    public String getFundCentre() {
        return fundCentre;
    }

    public String getBusinessArea() {
        return businessArea;
    }

    public String getFunctionalArea() {
        return functionalArea;
    }

    /** True only when all four are present; a partial set is treated as no set at all. */
    public boolean isComplete() {
        return notBlank(fund) && notBlank(fundCentre) && notBlank(businessArea) && notBlank(functionalArea);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "FiDimensions[fund=" + fund + ", fundCentre=" + fundCentre
                + ", businessArea=" + businessArea + ", functionalArea=" + functionalArea + "]";
    }
}
