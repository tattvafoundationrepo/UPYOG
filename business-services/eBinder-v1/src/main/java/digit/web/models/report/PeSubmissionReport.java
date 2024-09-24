package digit.web.models.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PeSubmissionReport {
    private int id;
    private Long reportSubmissionDate;
    private String competentAuthDes;
    private String orderPassedDes;
    private String peNumberId;
    private int typeId;
    private int orderId;
    private int reportNumber;
    private Long createdAt;
    private Long upDatedAt;
    private Integer createdBy;
    private Integer updatedBy;
}
