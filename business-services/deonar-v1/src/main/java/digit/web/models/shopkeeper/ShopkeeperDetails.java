package digit.web.models.shopkeeper;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import digit.web.models.AnimalAssignmentDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopkeeperDetails {

    private String arrivalId;
    private String importPermission;
    private String traderName;
    private Date dateOfArrival;
    private Time timeOfArrival;
    private String vehicleNumber;
    private String mobileNumber;
    private String email;
    private String stakeholderTypeName;
    private Date permissionDate;
    private String licenceNumber;
    private String registrationNumber;
    private Date validToDate;
    private String assigneeId;
    private String shopkeeperName;
    private List<AnimalAssignmentDetails> animalAssignmentDetailsList;

}