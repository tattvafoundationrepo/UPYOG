package digit.web.models;

import java.util.List;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaughterBookingRequest {

  @JsonProperty("RequestInfo")
  RequestInfo requestInfo;

  private List<SlaughterBookingDetails> bookingDetails;

  private AuditDetails auditDetails;

}
