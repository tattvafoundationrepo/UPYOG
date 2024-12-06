package digit.web.models;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaughterResponse {

    @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   @JsonProperty("Details")
   private Slaughter details;
}


