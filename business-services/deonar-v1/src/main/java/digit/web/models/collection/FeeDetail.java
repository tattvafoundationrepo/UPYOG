package digit.web.models.collection;

import org.egov.common.contract.models.AuditDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class FeeDetail {
    private String paidby;
    private Long feetype;
    private float feevalue;
    private String method;
    private String referenceno;
    private String recieptno;
    private AuditDetails audit;
}
