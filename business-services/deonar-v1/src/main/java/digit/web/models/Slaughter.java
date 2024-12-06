package digit.web.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Slaughter {


    private String arrivalId;         
    private String ddReference;       
    private String shopkeeperName;     
    private String licenceNumber;      
    private Integer animalTypeId;      
    private Integer token;             
    private Boolean slaughtering;      
    private Integer slaughterUnit;     
    private String slaughterType; 

}
