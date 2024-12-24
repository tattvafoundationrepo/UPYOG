package digit.web;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import digit.web.models.SlaughterBookingDetails;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaughterBookingResponse {

      @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   @JsonProperty("SlaughterBookingList")
   private List<SlaughterBookingDetails> detailsList;

}
