package digit.web.models.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;

@Data
@Builder

@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDataRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;
    @JsonProperty("EMP_ID")
    private List<String> empId;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("boundaryType")
    private String boundaryType;
    @JsonProperty("empDesignationCode")
    private String empDesignationCode;
    @JsonProperty("empDepartment")
    private String empDepartment;
    @JsonProperty("boundary")
    private String boundary;
    @JsonProperty("hierarchy")
    private String hierarchy;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
