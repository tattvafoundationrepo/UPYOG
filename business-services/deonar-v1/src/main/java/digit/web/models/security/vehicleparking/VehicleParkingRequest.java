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
public class VehicleParkingRequest {

    @JsonProperty("RequestInfo")
    @Valid
    @Builder.Default
    private RequestInfo requestInfo = null;

    @JsonProperty("vehicleParking")
    private VehicleParkingDetails vehicleParkingDetails;

    private Long createdAt;

    private int createdBy;

    private int updatedBy;

    private Long updatedAt;

}
