package digit.web.models;

import java.util.List;
import digit.web.models.security.AnimalDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlaughterList {


    private String arrivalId;
    private String ddReference;
    private String mobileNumber;
    private String licenceNumber;
    private Integer stakeholderId;
    private String shopkeeperName;
    private String opinion;
    private List<AnimalDetail> animalAssignmentDetailsList;
    private String purchaseDate;
    private String purchaseTime;
    
}
