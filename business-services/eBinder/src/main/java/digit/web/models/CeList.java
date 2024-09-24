package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CeList {
    private int id;
    private int peEnquiryId;
    private String ceCode;
    private String ceName;
    private String ceDept;
    private String ceDesig;
    private boolean ceSuspended;
    private String ceSuspensionOrder;
    private boolean ceStatus;//0 or 1
    private String enqOrderType;
    private String caseType;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
