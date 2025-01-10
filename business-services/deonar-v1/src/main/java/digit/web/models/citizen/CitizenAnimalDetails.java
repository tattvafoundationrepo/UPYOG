package digit.web.models.citizen;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CitizenAnimalDetails {

    @JsonProperty("AnimalType")
    private String animaltype;

    @JsonProperty("Token")
    private long token;

    @JsonProperty("CitizenName")
    private String stakeholderName;

}
