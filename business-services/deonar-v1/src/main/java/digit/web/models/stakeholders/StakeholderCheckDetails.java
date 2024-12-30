package digit.web.models.stakeholders;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StakeholderCheckDetails {

    @JsonProperty("stakeholdername")
    private String stakeholderName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("mobilenumber")
    private long mobilenumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("createdat")
    private long createdAt;

    @JsonProperty("licencenumber")
    private String licencenumber;

    @JsonProperty("registrationnumber")
    private String registrationnumber;

    @JsonProperty("stakeholdertype")
    private String stakeholdertype;

    @JsonProperty("animaltype")
    private String animaltype;

}
