package org.egov.demand.web.contract;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.demand.model.CollectedReceipt;
import org.egov.demand.model.MergedDemand;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergedDemandResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("MergedDemands")
	private List<MergedDemand> mergedDemands;

	@JsonProperty("CollectedReceipt")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<CollectedReceipt> collectedReceipts;

	private Integer limit;

	private Integer offset;

	private Long totalCount;
}
