package digit.web.models.security.vehicleParking;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.web.models.security.SecurityCheckDetails;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleParkedCheckResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("VehicleParkedCheckDetails")
    private List<VehicleParkedCheckDetails> vehicleParkedCheckDetails;

    @JsonProperty
    private String message;

}
