package digit.web.models.security.vehiclewashing;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VehicleWashingResponse {

    private long vehicleType;
    private String vehicleNumber;
    private Long washingTime;
    private Long departureTime;

}
