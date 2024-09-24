package digit.web.models.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TypeCase {
    private int typeId;
    private String name;
    private Long createdAt;
    private Long upDatedAt;
    private Integer createdBy;
    private Integer updatedBy;
}
