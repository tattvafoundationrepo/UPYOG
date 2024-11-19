package digit.web.models.penalty;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class PenaltyResponse {
   
    @JsonProperty("PenaltyTypeDetails")
   private List<PenaltyTypeDetails> details;
   
   @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   private List<RaisedPenalties> list;
   private String message;



}
