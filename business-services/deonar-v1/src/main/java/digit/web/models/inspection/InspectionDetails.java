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

    @JsonProperty("inspectionDate")
    private String inspectionDate;

    @JsonProperty("animalDetails")
    private AnimalDetail animalDetails;

    @JsonProperty("animalTokenNumber")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String animalTokenNumber;

    private String resultmark;

    private Long id;

    private  List<InspectionIndicators> report;

    private String inspectiontime;

    private Long inspectionid;



}

