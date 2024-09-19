package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employees {

    private String employeeStatus;
    @JsonProperty("user")
    private EmployeeUser user;
    private String code;
    private String employeeType;
    private List<Jurisdictions> jurisdictions;
    private List<Assignments> assignments;
    private List<ServiceHistory> serviceHistory;
    private List<Education> education;
    private List<Tests> tests;
    private String tenantId;
}
