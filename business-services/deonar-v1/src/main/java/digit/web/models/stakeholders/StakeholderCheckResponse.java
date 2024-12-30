package digit.web.models.stakeholders;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StakeholderCheckResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("StakeholderCheckDetails")
    private List<StakeholderCheckDetails> stakeholderCheckDetails;

    @JsonProperty
    private String message;
    
}
