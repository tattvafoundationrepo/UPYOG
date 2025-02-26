package digit.web.models.citizen;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CitizenGatePassRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("CitizenGatePassCriteria")
    @Builder.Default
    private CitizenGatePassCriteria citizenGatePassCriteria = null;

}
