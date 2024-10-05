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
    private int arrivalId;
    private Integer inspectionUnitId;
    @JsonProperty("inspectionType")
    private Integer inspectionType;
    private String employeeId;
    private Integer animalId;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
