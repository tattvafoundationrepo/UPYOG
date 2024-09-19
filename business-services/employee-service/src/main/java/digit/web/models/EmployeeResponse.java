package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {

    @JsonProperty("E_CODE")
    private Long eCode;

    @JsonProperty("E_MSG")
    private String eMsg;

    @JsonProperty("EMP_DATA")
    private EmpData empData;

}

