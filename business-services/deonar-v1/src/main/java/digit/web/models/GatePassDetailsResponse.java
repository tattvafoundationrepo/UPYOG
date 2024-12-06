package digit.web.models;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GatePassDetailsResponse {
   
    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;
    
    @JsonProperty("gatePassDetails")
    private  List<GatePassMapper> details;

    @JsonProperty("SavedGatePassDetails")
    private GatePassDetails saveDetails;

}
