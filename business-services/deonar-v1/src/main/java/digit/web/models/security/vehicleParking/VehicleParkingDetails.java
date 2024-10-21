package digit.web.models.security.vehicleParking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VehicleParkingDetails {

    @JsonProperty("vehicleType")
    private String vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

    @JsonProperty("IN")
    private boolean vehicleIn;

    @JsonProperty("OUT")
    private boolean vehicleOut;

}
