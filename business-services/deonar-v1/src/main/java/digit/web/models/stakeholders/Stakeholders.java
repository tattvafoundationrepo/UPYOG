package digit.web.models.stakeholders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Stakeholders {

    private int id;
    private String stakeholderName;
    private String address1;
    private String address2;
    private int pincode;
    private long mobileNumber;
    private long contactNumber;
    private String email;
    private long createdAt;
    private long updatedAt;
    private String createdBy;
    private String updatedBy;
    private long stakeholderTypeId;
    private long animalTypeId;
    private String licenceNumber;
    private String registrationNumber;
    private long validfromdate;
    private long validtodate;

}
