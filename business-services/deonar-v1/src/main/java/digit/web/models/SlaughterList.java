package digit.web.models;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.hibernate.validator.constraints.pl.NIP;

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
    private String assigneeId;
    private String shopkeeperName;
    private List<AnimalDetail> animalAssignmentDetailsList;
    private String ddReference;


}
