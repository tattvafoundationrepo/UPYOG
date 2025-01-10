package digit.web.models.citizen;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitizenGatePassSaveResponse {

     @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("CitizenGatePassSaveDetails")
    private CitizenGatePassDetails details;

}
