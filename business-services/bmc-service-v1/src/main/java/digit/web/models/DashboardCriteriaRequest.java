package digit.web.models;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DashboardCriteriaRequest {

    @JsonProperty ("RequestInfo")
    @Valid
    @Builder.Default
    private RequestInfo requestInfo = null;

    @JsonProperty ("DashboardCriteria")
    @Valid
    @Builder.Default
    private DashboardCriteria dashboardCriteria = null;

}
