package digit.web.models.citizen;

import java.util.List;

import org.egov.common.contract.models.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CitizenGatePassDetails {

    @JsonProperty("ArrivalId")
    private long arrivalid;

    @JsonProperty("DDReference")
    private String ddReference;

    @JsonProperty("CitizenName")
    private String stakeholderName;

    @JsonProperty("StakeholderType")
    private String stakeholdertype;

    @JsonProperty("Date")
    private String assigndate;

    @JsonProperty("Time")
    private String assigntime;

    @JsonProperty("AnimalDetails")
    private List<CitizenAnimalDetails> citizenAnimalDetails;

    private AuditDetails auditDetails; 
    private String jsonAnimalDetails;
    private String gatePassReference;

}
