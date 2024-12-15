package digit.web.models.common;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;


@Getter

public class BaundrySearchRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;
    
    @JsonProperty("tenantid")
    private String tenantId;

}
