package digit.web.models.user;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UdidResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;
    
    @JsonProperty("UdidDetails")
    private  DivyangDetails divyangDetails;

}
