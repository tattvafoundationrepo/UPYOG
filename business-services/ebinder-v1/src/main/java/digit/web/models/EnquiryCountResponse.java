package digit.web.models;

import org.egov.common.contract.response.ResponseInfo;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EnquiryCountResponse {


      @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   private Map<String, Long> count;

}
