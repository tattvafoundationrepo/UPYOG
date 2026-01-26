package org.egov.demand.model;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvSettlement {

        @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;


    private String advanceDemandId;
    private String settledDemandId;
    private Long taxPeriodFrom;
    private Long taxPeriodTo;
    private String consumerCode;


}
