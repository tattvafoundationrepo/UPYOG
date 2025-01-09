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
    RequestInfo requestInfo;

    @JsonProperty("CitizenGatePassCriteria")
    @Builder.Default
    CitizenGatePassCriteria citizenGatePassCriteria = null;

}
