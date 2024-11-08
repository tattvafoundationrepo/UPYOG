package digit.web.models.inspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

import org.egov.common.contract.request.RequestInfo;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;
    
    @JsonProperty("Inspection")
    private Inspection inspection;

    @JsonProperty("InspectionDetails")
    private InspectionDetails inspectionDetails;


}
