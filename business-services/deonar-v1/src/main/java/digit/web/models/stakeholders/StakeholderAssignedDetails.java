package digit.web.models.stakeholders;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import digit.web.models.citizen.CitizenAnimalDetails;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StakeholderAssignedDetails {

     @JsonProperty("ArrivalId")
    private long arrivalid;

    @JsonProperty("DDReference")
    private String ddReference;

    @JsonProperty("StakeholderName")
    private String stakeholderName;

    @JsonProperty("StakeholderType")
    private String stakeholdertype;

    @JsonProperty("Date")
    private String assigndate;

    @JsonProperty("Time")
    private String assigntime;

    @JsonProperty("LicenceNumber")
    private String licenceNumber;

    @JsonProperty("AnimalDetails")
    private List<CitizenAnimalDetails> citizenAnimalDetails;

}
