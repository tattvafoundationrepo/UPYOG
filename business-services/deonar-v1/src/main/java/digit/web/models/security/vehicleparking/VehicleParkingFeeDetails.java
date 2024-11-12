package digit.web.models.security.vehicleparking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VehicleParkingFeeDetails {

    @JsonProperty("vehicleType")
    private int vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

    @JsonProperty("monthlyFee")
    private double monthlyFee;

    @JsonProperty("parkingType")
    private String parkingType;

}
