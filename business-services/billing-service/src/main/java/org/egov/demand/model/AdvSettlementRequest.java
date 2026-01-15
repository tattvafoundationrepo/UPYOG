package org.egov.demand.model;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvSettlementRequest {

   private RequestInfo requestInfo;
   private List<AdvSettlement> settlements;
}
