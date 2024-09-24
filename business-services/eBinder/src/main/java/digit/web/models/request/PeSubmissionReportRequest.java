package digit.web.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.web.models.report.DisciplinaryOrderType;
import digit.web.models.report.PeReport;
import digit.web.models.report.PeSubmissionReport;
import digit.web.models.report.TypeCase;
import lombok.Builder;
import lombok.Data;
import org.egov.common.contract.request.RequestInfo;

@Data
@Builder
public class PeSubmissionReportRequest {
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("Report")
    private PeReport report;

    @JsonProperty("TypeCase")
    private TypeCase typeCase;

    @JsonProperty("OrderType")
    private DisciplinaryOrderType orderType;

    @JsonProperty("PeSubmissionReport")
    private PeSubmissionReport peSubmissionReport;
}