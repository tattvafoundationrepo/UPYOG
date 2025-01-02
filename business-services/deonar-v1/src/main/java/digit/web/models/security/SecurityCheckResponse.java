package digit.web.models.security;

import java.util.List;
import org.egov.common.contract.response.ResponseInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import digit.web.models.collection.CollectionStablingListDetails;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityCheckResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("SecurityCheckDetails")
    private List<SecurityCheckDetails> securityCheckDetails;

    @JsonProperty("CollectionStablingList")
    private List<CollectionStablingListDetails> stablingDetails; 
    
    @JsonProperty("Message")
    private String message;
}