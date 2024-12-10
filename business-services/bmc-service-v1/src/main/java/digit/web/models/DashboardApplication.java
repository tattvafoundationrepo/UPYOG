package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardApplication {

    @JsonProperty ("id")
    private String id;

    @JsonProperty ("MachineName")
    private String machineName;

    @JsonProperty ("CourseNAme")
    private String courseName;

    @JsonProperty("City")
    private String city;

    @JsonProperty("Zone")
    private String zone;

    @JsonProperty ("Name")
    private String name;

    @JsonProperty ("ApplicationNumber")
    private String applicationNumber;

    @JsonProperty ("state")
    private String status;

    @JsonProperty ("ModuleName")
    private String module; 

    @JsonProperty("SchemeName")
    private String schemeName;

    @JsonProperty("CreatedDate")
    private String createdDate;

    @JsonProperty("endtime")
    private Long endTime;

    @JsonProperty ("Ward")
    private String wards;

}
