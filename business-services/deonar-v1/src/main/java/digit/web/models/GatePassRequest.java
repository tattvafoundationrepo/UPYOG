package digit.web.models;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatePassRequest {

    @JsonProperty("RequestInfo")
    RequestInfo requestInfo;

    GatePassSearchCriteria criteria;
    GatePassDetails gatePassDetails;

}

