package digit.web.models.security.vehicleParking;

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
public class VehicleParkedCheckDetails {

        @JsonProperty("vehicleType")
        private Long vehicleType;

        @JsonProperty("vehicleNumber")
        private String vehicleNumber;

        @JsonProperty("parkingTime")
        private LocalTime parkingTime;

        @JsonProperty("parkingDate")
        private LocalDate parkingDate;

        @JsonProperty("departureTime")
        private LocalTime departureTime;

        @JsonProperty("departureDate")
        private LocalDate departureDate;

}
