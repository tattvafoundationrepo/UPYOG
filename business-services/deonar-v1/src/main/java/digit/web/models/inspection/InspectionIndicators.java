package digit.web.models.inspection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionIndicators {
    private int id;
    private String name;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
