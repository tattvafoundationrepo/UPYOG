package digit.web.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SlaughterBookingDetails {

    private Long id;
    private String arrivalId; 
    private String ddReference; 
    private Integer animalTypeId; 
    private Integer token; 
    private Integer slaughterUnitId; 
    private Integer unitShiftId; 
    private Long slaughterDate; 
    

}
