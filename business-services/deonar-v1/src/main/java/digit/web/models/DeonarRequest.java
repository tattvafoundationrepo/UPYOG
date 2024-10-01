package digit.web.models;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Contract class to receive request. Array of items are used in case of create,
 * whereas single item is used for update.
 */
@Validated
@Getter
@Setter
@AllArgsConstructor
@Builder
public class DeonarRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("DeonarRequest")
    @Builder.Default
    @Valid
    private Object object=null;
}
