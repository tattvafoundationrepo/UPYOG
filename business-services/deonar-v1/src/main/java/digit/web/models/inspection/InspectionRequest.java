package digit.web.models.inspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("Animal")
    private Animal animal;

    @JsonProperty("InspectionUnit")
    private InspectionUnit inspectionUnit;

    @JsonProperty("Inspection")
    private Inspection inspection;


    @JsonProperty("InspectionIndicators")
    private InspectionIndicators  inspectionIndicators;

    @JsonProperty("InspectionDetail")
    private InspectionDetail inspectionDetail;

    @JsonProperty("InspectionType")
    private String inspectionType;


}
