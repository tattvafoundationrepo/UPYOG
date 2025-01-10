package digit.web.models.stakeholders;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StakeholderAssignedCriteria {

    @JsonProperty("StakeholderName")
    private String stakeholderName;

    @JsonProperty("MobileNumber")
    private long mobileNumber;

    @JsonProperty("LicenseNumber")
    private String licenseNumber;

    @JsonProperty("StakeholderTypeId")
    private int stakeholderTypeId;

}
