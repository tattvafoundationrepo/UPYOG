package digit.web.models.security.vehicleparking;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VehicleParkingResponse {

    private long vehicleType;
    private String vehicleNumber;
    private Long parkingTime;
    private Long departureTime;

}
