package digit.web.models.inspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionSearchRequest{

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("InspectionRequest")
    private InspectionRequest inspectionRequest;

    @JsonProperty("Animal")
    private Animal animal;

    @JsonProperty("InspectionDetail")
    private List<InspectionDetail> inspectionDetail;


    @JsonProperty("Inspection")
    private Inspection inspection;

}
