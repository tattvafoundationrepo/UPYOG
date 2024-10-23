package digit.web.models.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityCheckDetails {

    @JsonProperty("entryUnitId")
    private String entryUnitId;

    @JsonProperty("importPermission")
    private String importPermission;

    @JsonProperty("traderName")
    private String traderName;

    @JsonProperty("dateOfArrival")
    private String dateOfArrival;

    @JsonProperty("timeOfArrival")
    private String timeOfArrival;

    @JsonProperty("vehicleNumber")
    private String vehicleNumber;

    @JsonProperty("mobileNumber")
    private Long mobileNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("stakeholderTypeName")
    private String stakeholderTypeName;

    @JsonProperty("licenceNumber")
    private String licenceNumber;

    @JsonProperty("registrationNumber")
    private String registrationNumber;

    @JsonProperty("validToDate")
    private String validToDate;

    @JsonProperty("animalDetails")
    private List<AnimalDetail> animalDetails;

    private String permissionDate; 

    private Long stakeholderId;
}

