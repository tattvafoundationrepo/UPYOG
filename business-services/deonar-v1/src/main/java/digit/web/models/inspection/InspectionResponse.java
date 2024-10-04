package digit.web.models.inspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("InspectionDetails")
    private List<InspectionDetails> inspectionDetails;

    @JsonProperty("Message")
    private String message;
}
