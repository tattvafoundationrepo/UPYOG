package org.egov.demand.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMarketInfo {
    private String paymentMode;
    private String fundCenter;
    private String fund;
    private String businessArea;
    private String additionalDetails;
    private BigDecimal totalDue;
    private BigDecimal totalAmountPaid;
    private String functionalArea;
    private String receiptNumber;
    private String transactionNumber;
}

