package digit.web.models;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class UpdateCeListRequest {


    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;
    
    @JsonProperty("action")
    private String action;

    private String empCode;

    private String enqId;
                       
    @JsonProperty("EmployeeAdd")
    private EmployeeData empData;


}
