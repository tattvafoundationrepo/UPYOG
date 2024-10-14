package digit.web.models;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnquiryCountRequest {

      @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;
    
    @JsonProperty("action")
    private String action;

}
