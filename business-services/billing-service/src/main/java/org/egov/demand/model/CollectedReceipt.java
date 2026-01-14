package org.egov.demand.model;

import org.egov.demand.model.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectedReceipt {

	private String businessService;

	private String consumerCode;

	private String receiptNumber;

	private Double receiptAmount;

	private Long receiptDate;

	private String status;

	private String additionalDetails;

	private AuditDetail auditDetail;

	private String tenantId;

	private String transactionNumber;

	private Double totalAmountPaid;

	private Long fromPeriod;

	private Long toPeriod;

	private Double advancePayment;

	private Double regularPayment;
}