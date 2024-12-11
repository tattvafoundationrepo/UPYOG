package digit.web.models;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaughterUnitRequest {


 @JsonProperty("RequestInfo")
  RequestInfo requestInfo;

  private Integer  slaughterUnitId;
 

}
