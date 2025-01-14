package digit.web.models.security.vehiclewashing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VehicleWashingDetails {

    @JsonProperty("vehicleType")
    private long vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

    @JsonProperty("washingTime")
    private Long washingTime;

    @JsonProperty("IN")
    private boolean vehicleIn;

    @JsonProperty("OUT")
    private boolean vehicleOut;

    private Long departureTime;

}
