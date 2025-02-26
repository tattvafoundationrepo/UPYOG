package digit.web.models;

import org.apache.kafka.common.protocol.types.Field.Bool;
import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetListRequest {
    
    @JsonProperty("RequestInfo")
    RequestInfo requestInfo;

    private Boolean forCollection;

    private Boolean forEntryCollection;

    private Boolean forStablingCollection;

    private Boolean forRemovalCollection;

    private Boolean forSlaughterRecoveryFee;


}
