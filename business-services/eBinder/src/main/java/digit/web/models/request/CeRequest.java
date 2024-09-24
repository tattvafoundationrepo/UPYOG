package digit.web.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.web.models.CeList;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
@Builder
public class CeRequest {
    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("CeList")
    @Valid
    private CeList ceList;

    @JsonProperty("isRemove")
    private boolean isRemove;
}
