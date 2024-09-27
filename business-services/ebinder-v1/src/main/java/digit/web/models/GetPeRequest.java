package digit.web.models;



import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data

public class GetPeRequest {
    
    @JsonProperty("RequestInfo")
    RequestInfo requestInfo;

    private String enqId;
    private Integer type; 

}
