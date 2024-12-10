package digit.web.models;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
public class DashboardCriteria {

    @JsonProperty ("state")
    private String state;

    @JsonProperty ("courseid")
    @Builder.Default
    private Long courseId = null;

    @JsonProperty ("machineid")
    @Builder.Default
    private Long machineId = null;

    @JsonProperty ("ward")
    private String ward;

    @JsonProperty ("zone")
    private String zone;

    @JsonProperty ("city")
    private String city;
    
    @JsonProperty ("schemeId")
    @Builder.Default
    private Long schemeId = null;

    @JsonProperty ("createddate")
    private String createdDate;

    @JsonProperty ("enddate")
    private String endDate;

}
