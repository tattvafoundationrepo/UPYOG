package digit.web.models.penalty;


import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyRequest {
        
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo;

        private String stakeholderId;
        private Long penaltyId;
        private Integer units;
        private Double amount;
        private String penaltyReference;
        private Boolean isPaid;
        private AuditDetails auditDetails;
    }
    



