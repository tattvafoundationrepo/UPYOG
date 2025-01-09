package digit.web.models.citizen;

import java.util.List;

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
    long arrivalid;

    @JsonProperty("CitizenName")
    String stakeholderName;

    @JsonProperty("StakeholderType")
    String stakeholdertype;

    @JsonProperty("Date")
    String assigndate;

    @JsonProperty("Time")
    String assigntime;

    @JsonProperty("AnimalDetails")
    List<CitizenAnimalDetails> citizenAnimalDetails;

}
