package digit.web.models;

import java.util.List;
import org.egov.common.contract.response.ResponseInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import digit.bmc.model.UserSchemeApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemeApplicationResponse {

   @JsonProperty("ResponseInfo")
   private ResponseInfo responseInfo;

   @JsonProperty("SchemeApplications")
   private List<SchemeApplication> schemeApplications;

   private UserSchemeApplication userSchemeApplication;

   private List<UserSchemeApplication> randomizedCitizens;

   private String message;

}
