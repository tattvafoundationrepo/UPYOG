package digit.web.models.inspection;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InspectionDetail {
    private Long id;
    private Long inspectionId;
    private Long inspectionIndicatorId;
    private String inspectionIndicatorValue;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
