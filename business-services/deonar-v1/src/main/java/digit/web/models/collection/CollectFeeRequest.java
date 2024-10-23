package digit.web.models.collection;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class CollectFeeRequest {
    @JsonProperty("RequestInfo")
    @Valid
    @Builder.Default
    private RequestInfo requestInfo = null;

    @JsonProperty("FeeDetail")
    @Valid
    @Builder.Default
    private FeeDetail feedetail = null;

}
