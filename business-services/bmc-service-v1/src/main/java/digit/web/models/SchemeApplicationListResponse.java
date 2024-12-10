package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemeApplicationListResponse {

    private List<DashboardApplication> applications;

    private String message;
     @JsonProperty ("ResponseInfo")
    private ResponseInfo responseInfo;

}