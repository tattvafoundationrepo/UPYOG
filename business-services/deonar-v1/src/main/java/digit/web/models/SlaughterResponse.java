package digit.web.models;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaughterResponse {

    @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   @JsonProperty("SlaughterList")
   private List<SlaughterList> detailsList;

   @JsonProperty("Details")
   private Slaughter details;
}


