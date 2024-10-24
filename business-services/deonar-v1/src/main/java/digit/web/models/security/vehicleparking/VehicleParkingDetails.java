package digit.web.models.security.vehicleparking;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VehicleParkingDetails {

    @JsonProperty("vehicleType")
    private long vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

    @JsonProperty("parkingTime")
    private Long parkingTime;

    @JsonProperty("IN")
    private boolean vehicleIn;

    @JsonProperty("OUT")
    private boolean vehicleOut;

    private Long departureTime;


}
