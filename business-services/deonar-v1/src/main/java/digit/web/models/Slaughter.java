package digit.web.models;

import java.util.List;

import digit.web.models.security.AnimalDetail;
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
    private List<AnimalDetail> details;

}
