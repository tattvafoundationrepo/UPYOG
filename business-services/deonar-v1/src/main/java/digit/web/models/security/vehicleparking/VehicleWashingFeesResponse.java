package digit.web.models.security.vehicleparking;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleWashingFeesResponse {


    @JsonProperty("vehicleType")
    private String vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

    @JsonProperty("washingFee")
    private long washingFee;

    @JsonProperty
    private String message;
}
