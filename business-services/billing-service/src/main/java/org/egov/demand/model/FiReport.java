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
}
