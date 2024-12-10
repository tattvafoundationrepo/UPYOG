package digit.web.models.employee;

import java.util.Map;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationCountResponse {

   @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   private Map<String, Long> count;

}
