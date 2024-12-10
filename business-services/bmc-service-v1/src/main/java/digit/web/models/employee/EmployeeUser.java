package digit.web.models.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeUser {
    private String name;
    private String mobileNumber;
    private String fatherOrHusbandName;
    private String gender;
    private Long dob;
    private String correspondenceAddress;
    private List<EmployeeRole> roles;
    private String tenantId;
}
