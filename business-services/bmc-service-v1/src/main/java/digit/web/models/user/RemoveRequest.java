package digit.web.models.user;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RemoveRequest {

   @JsonProperty("RequestInfo")
    RequestInfo requestInfo;
    
    private RemovalCriteria removalcriteria;
}
