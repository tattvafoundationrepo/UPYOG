package digit.web.models;

import java.util.List;

import org.egov.common.contract.models.AuditDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatePassDetails {
    
    private String vehicleNumber;
    private String receiverName;
    private String receiverContact;
    private Long shopkeeper;
    private Double total; 
    private List<AnimalTypeDetails> animalDetails; 
    private AuditDetails auditDetails; 
    private String jsonAnimalDetails;
    private String gatePassReference;

    @Data
    public static class AnimalTypeDetails { 
        private String ddReference; 
        private String animalType; 
        private Integer carcassCount; 
        private Integer kenaeCount; 
    }

}
