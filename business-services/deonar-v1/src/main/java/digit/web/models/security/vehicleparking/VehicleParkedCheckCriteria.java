package digit.web.models.security.vehicleparking;

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
    private Long vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

}
