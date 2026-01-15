package org.egov.demand.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvSettlement {

    private String advanceDemandId;
    private String settledDemandId;
    private Long taxPeriodFrom;
    private Long taxPeriodTo;
    private String consumerCode;


}
