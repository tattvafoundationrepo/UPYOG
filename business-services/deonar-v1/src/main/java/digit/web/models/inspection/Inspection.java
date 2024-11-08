package digit.web.models.inspection;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inspection {
    private Long id;
    private Long inspectionDate;
    private String inspectionTime;
    private String arrivalId;
    private Integer inspectionUnitId;
    @JsonProperty("inspectionType")
    private Integer inspectionType;
    private String employeeId;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
