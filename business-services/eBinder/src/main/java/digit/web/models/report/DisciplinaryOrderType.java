package digit.web.models.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisciplinaryOrderType {
    private int orderId;
    private String name;
    private Long createdAt;
    private Long upDatedAt;
    private Integer createdBy;
    private Integer updatedBy;
}
