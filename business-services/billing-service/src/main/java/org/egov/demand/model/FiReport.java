package org.egov.demand.model;


import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiReport {

    private Integer id;

    private String  transactionNumber;

    private Long docDate;
    private Long postingDate;

    private String referenceNo;              
    private String documentHeaderText;       

    private String postingKey;

    private String glCode;
    private BigDecimal collectionAmount;

    private String fund;
    private String fundCentre;

    private String functionalArea;
    private String businessArea;

    private String remarks;
    private String paymentModeDetails;

    private Boolean isNew;

    private Long createdAt;
    private Long updatedAt;

    private String docType;

    private String costCenter;

    private String commitmentItem;

    /** One of the FiReportType codes (upmktcoll, upmktcolrev, upmktdemd, ...). */
    private String reportType;

    /**
     * SAP Assignment (ZUONR) — the key F.13/FB05 match open items on. Set only on the
     * advance-GST legs, to the advance document number, so the advance receipt's
     * CGST/SGST Advance debit and the later demand's netting credit carry the same
     * value and SAP can clear them. Null on every other row.
     */
    private String assignment;
}
