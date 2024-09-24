package digit.web.models;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeData {

    private String employeename;
    private Department department; 
    private String employeeCode;
    private Designation designation; 
    private String mobileNumber;
    private Boolean cesuspended;
    private String cesuspensionorder;
    private Boolean cestatus;
    private String enqordertype;
    private String casetype;
    private String email;
    private Long createdAt ;
	private Long updatedAt ;
	private String createdBy ;
	private String updatedBy ;


}
