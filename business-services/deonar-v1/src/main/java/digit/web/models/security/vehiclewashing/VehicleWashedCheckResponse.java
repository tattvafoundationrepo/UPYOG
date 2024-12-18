package digit.web.models.security.vehiclewashing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleWashedCheckResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("VehicleWashedCheckDetails")
    private List<VehicleWashCheckDetails> vehicleWashedCheckDetails;

    @JsonProperty
    private String message;

}
