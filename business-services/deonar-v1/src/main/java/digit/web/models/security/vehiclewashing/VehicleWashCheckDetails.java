package digit.web.models.security.vehiclewashing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleWashCheckDetails {

    @JsonProperty("vehicleType")
    private String vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

    @JsonProperty("vehicleId")
    private long vehicleId;

    @JsonProperty("washingTime")
    private LocalTime washingTime;

    @JsonProperty("washingDate")
    private LocalDate washingDate;

    @JsonProperty("departureTime")
    private LocalTime departureTime;

    @JsonProperty("departureDate")
    private LocalDate departureDate;

}
