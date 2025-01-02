package digit.web.models.security.vehiclewashing;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VehicleWashingRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("vehicleWashing")
    private List<VehicleWashingDetails> vehicleWashingDetails;

    private Long createdAt;

    private int createdBy;

    private int updatedBy;

    private Long updatedAt;

}
