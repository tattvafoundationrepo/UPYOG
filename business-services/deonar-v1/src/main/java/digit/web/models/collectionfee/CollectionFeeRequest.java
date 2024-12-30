package digit.web.models.collectionfee;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CollectionFeeRequest {

    @JsonProperty("RequestInfo")
    @Valid
    @Builder.Default
    RequestInfo requestInfo = null;

    @JsonProperty("CollectionFee")
    CollectionFee collectionFee;

}
