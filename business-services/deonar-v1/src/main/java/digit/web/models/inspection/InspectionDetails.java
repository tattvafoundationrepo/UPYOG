package digit.web.models.inspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.web.models.security.AnimalDetail;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionDetails {

    @JsonProperty("arrivalId")
    private String arrivalId;

    @JsonProperty("inspectionType")
    private String inspectionType;

    @JsonProperty("importPermission")
    private String importPermission;

    @JsonProperty("licenceNumber")
    private String licenceNumber;

    @JsonProperty("traderName")
    private String traderName;

    @JsonProperty("veterinaryOfficerName")
    private String veterinaryOfficerName;

    @JsonProperty("inspectionDate")
    private String inspectionDate;

    @JsonProperty("inspectionDay")
    private String inspectionDay;

    @JsonProperty("animalDetails")
    private List<AnimalDetail> animalDetails;

    @JsonProperty("animalTokenNumber")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String animalTokenNumber;

    @JsonProperty("indicatorValue")
    private String indicatorValue;

//    @JsonProperty("inspectionIndicator")
//    private InspectionIndicator inspectionIndicator;

    @JsonProperty("animalStabling")
    private String animalStabling;
}
