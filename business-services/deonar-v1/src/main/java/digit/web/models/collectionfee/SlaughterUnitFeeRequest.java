package digit.web.models.collectionfee;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SlaughterUnitFeeRequest {

    @JsonProperty("RequestInfo")
    @Valid
    @Builder.Default
    private RequestInfo requestInfo = null;

    @JsonProperty("SlaughterUnitFee")
    private SlaughterUnitFee slaughterUnitFee;
}
