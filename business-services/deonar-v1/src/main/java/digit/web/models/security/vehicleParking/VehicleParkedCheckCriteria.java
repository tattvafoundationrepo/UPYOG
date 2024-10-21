package digit.web.models.security.vehicleParking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleParkedCheckCriteria {

    @JsonProperty("vehicleType")
    private String vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;
}
