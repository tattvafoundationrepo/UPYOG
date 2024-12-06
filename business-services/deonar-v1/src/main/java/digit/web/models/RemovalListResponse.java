package digit.web.models;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemovalListResponse {

     @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

     @JsonProperty("SecurityCheckDetails")
    private List<RemovalList> securityCheckDetails;

    @JsonProperty("Message")
    private String message;

}
