package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Assignments {
    private Long fromDate;
    @JsonProperty("isCurrentAssignment")
    private boolean isCurrentAssignment;
    private String department;
    private String designation;

}
