package digit.web.models.security.vehiclewashing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleWashCheckCriteria {

    @JsonProperty("vehicleType")
    private Long vehicleType;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

}
