package digit.web.models.penalty;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PenaltyResponse {
   
    @JsonProperty("PenaltyTypeDetails")
   private List<PenaltyTypeDetails> details;
   
   @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   @JsonProperty("PenaltyLists")
   private List<RaisedPenalties> list;

   @JsonProperty("message")
   private String message;
   
   




}
