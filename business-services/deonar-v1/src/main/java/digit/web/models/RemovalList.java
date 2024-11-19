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

    @JsonProperty("traderName")
    private String traderName;

    @JsonProperty("dateOfArrival")
    private String dateOfArrival;

    @JsonProperty("timeOfArrival")
    private String timeOfArrival;


    @JsonProperty("licenceNumber")
    private String licenceNumber;

    @JsonProperty("animalDetails")
    private List<AnimalDetail> animalDetails;

    private Long stakeholderId;

    private String arrivalId;


}
