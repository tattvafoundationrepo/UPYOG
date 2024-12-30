package digit.web.models.stakeholders;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StakeholderCheckCriteria {

    @JsonProperty("stakeholdertype")
    private String stakeholderType;

    @JsonProperty("animaltype")
    private String animalType;

    @JsonProperty("createdat")
    private String createdAt;

}
