package digit.web.models;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoundaryResponse {
    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;
    @JsonProperty("BoundaryDetails")
    private List<Boundary> details;

}
