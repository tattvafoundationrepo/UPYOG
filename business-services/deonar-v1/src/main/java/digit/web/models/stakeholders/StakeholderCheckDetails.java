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

    @JsonProperty("address1")
    private String address1;

    @JsonProperty("address2")
    private String address2;

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

    @JsonProperty("pincode")
    private String pincode;

}
