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
    String animaltype;

    @JsonProperty("Token")
    long token;

    @JsonProperty("CitizenName")
    String stakeholderName;

}
