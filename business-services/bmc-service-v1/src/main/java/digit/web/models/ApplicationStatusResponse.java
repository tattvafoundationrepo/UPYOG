package digit.web.models;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Builder
@Getter
@Setter
public class ApplicationStatusResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;
     
    @JsonProperty("Applications")
     private List<SchemeApplicationStatus> list; 
}
