package digit.web.models.collection;

import org.egov.common.contract.models.AuditDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class FeeDetail {
    private String uuid;
    private String paidby;
    private Long feetype;
    private float feevalue;
    private String method;
    private String referenceno;
    private String recieptno;
    private AuditDetails audit;
    private String arrivalid;
    private Long stakeholderId;
    private String licenceNumber;
}
