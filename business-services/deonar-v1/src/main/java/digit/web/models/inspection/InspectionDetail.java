package digit.web.models.inspection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InspectionDetail {
    private Long id;
    private Long inspectionId;
    private Long inspectionIndicatorId;
    private String inspectionIndicatorValue;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;

    public InspectionDetail(Long inspectionIndicatorId, String inspectionIndicatorValue, Long createdAt, Long updatedAt, String createdBy, String updatedBy) {
        this.inspectionIndicatorId = inspectionIndicatorId;
        this.inspectionIndicatorValue = inspectionIndicatorValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
