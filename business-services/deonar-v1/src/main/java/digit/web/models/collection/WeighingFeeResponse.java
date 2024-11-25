package digit.web.models.collection;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class WeighingFeeResponse {

    @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   @JsonProperty("Details")
   private List<WeighingFee> details;

}
