package digit.web.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import digit.web.models.security.AnimalDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemovalList {


     @JsonProperty("entryUnitId")
    private String entryUnitId;

    @JsonProperty("dateOfRemoval")
    private String removaldate;

    @JsonProperty("timeOfRemoval")
    private String removaltime;


    @JsonProperty("licenceNumber")
    private String licenceNumber;

    @JsonProperty("animalDetails")
    private List<AnimalDetail> animalDetails;

    private Long stakeholderId;

    private String ddreference;

    private String removaltype;

    private String shopkeepername;

    private String mobilenumber;

    private String registrationNumber;




  

}
