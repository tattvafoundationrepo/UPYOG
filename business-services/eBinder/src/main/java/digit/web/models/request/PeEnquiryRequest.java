package digit.web.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.web.models.PeEnquiry;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Validated
public class PeEnquiryRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("PeEnquiry")
    @Valid
    private PeEnquiry peEnquiry;
}
