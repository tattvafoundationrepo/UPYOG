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
    private String mobileNumber;
    private String licenceNumber;
    private String stakeholderId;
    private String shopkeeperName;
    private List<AnimalDetail> animalAssignmentDetailsList;
    private String ddReference;


}
