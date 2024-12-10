package digit.web.models.employee;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationCountRequest {

     @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;
    
    @JsonProperty("action")
    private String action;

    private Boolean forVerify;



}
