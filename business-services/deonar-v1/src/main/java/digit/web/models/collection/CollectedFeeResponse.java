package digit.web.models.collection;


import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class CollectedFeeResponse {
    @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   @JsonProperty("Details")
   private FeeDetail details;
}
