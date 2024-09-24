package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PeEnquiry {
    private int id;
    private String newCode;
    private String oldCode;
    private Long orderDate;
    private String orderNo;
    private String ceDesig;
    private String ceDept;
    private String ceEmpCode;
    private String enqSubject;
    private String tenantId;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
