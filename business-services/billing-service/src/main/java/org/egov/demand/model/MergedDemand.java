package org.egov.demand.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergedDemand {

	private String consumerCode;
	private String consumerType;
	private String tenantId;
	private String payer;
	private String licenceeName;
	private String licenceeMobile;
	private String reason;

	private String overallStatus;
	private Boolean isFullyPaid;
	private Boolean hasPartialPayment;

	private List<String> businessServices;

	private JsonNode demandsByService;

	private BigDecimal totalTaxAmount;
	private BigDecimal totalCollectionAmount;
	private BigDecimal totalMinimumPayable;
	private BigDecimal totalRemainingAmount;

	private Long earliestTaxPeriod;
	private Long latestTaxPeriod;

	private Integer totalDemandCount;
	private Integer paidDemandCount;
	private Integer unpaidDemandCount;

	private Long firstCreatedTime;
	private Long lastModifiedTime;

	private Long totalCount;
}
