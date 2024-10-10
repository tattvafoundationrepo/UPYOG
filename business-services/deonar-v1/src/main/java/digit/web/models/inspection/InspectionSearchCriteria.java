package digit.web.models.inspection;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionSearchCriteria {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    private String entryUnitId;
    private Long inspectionType;

}
