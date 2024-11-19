package digit.web.models;

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
    ResponseInfo responseInfo;

    GatePassDetails details;

}
