package digit.web.models.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ParkingFee {
    private String vehiclenumber;
    private String vehicletype;
    private String parkingdate;
    private String parkingtime;
    private String departuredate;
    private String departuretime;
    private Integer totalhours;
    private Float total;
}