package digit.web.models;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GatePassMapper {


    private String ddReference;                  
    private String licenceNumber;      
    private String shopkeeperName;     
    private String animalType;
    private Long carcassKenaecount;




}
