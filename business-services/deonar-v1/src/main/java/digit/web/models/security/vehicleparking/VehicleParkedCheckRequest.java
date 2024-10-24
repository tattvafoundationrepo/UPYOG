package digit.web.models.security.vehicleparking;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VehicleParkedCheckRequest {

    @JsonProperty("RequestInfo")
    @Valid
    @Builder.Default
    private RequestInfo requestInfo = null;

    @JsonProperty("vehicleParking")
    private VehicleParkedCheckCriteria vehicleParkedCheckCriteria;

}
