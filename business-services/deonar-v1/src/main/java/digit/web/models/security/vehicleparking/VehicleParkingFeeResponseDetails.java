package digit.web.models.security.vehicleparking;

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
public class VehicleParkingFeeResponseDetails {
    @JsonProperty("vehicleType")
    private String vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

    @JsonProperty("parkingFee")
    private double parkingFee;

    @JsonProperty("startDate")
    private LocalDate startDate;

    @JsonProperty("endDate")
    private LocalDate endDate;

    @JsonProperty("parkInTime")
    private LocalTime parkInTime;

    @JsonProperty("parkOutTime")
    private LocalTime parkOutTime;
}
