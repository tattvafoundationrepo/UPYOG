package digit.web.models;


import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import  org.egov.common.contract.models.AuditDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlaughterRequest {

  @JsonProperty("RequestInfo")
  RequestInfo requestInfo;

  private Slaughter slaughterDetails;
 
  private AuditDetails audit;

}
