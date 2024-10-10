package digit.web.models.inspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("tokenNumber")
    private Long animalTokenNumber;

    @JsonProperty("arrivalId")
    private String arrivalId;

    @JsonProperty("eyes")
    private Properties eyes;

    @JsonProperty("pulseRate")
    private Properties pulseRate;

    @JsonProperty("species")
    private Properties species;

    @JsonProperty("breed")
    private Properties breed;

    @JsonProperty("sex")
    private Properties sex;

    @JsonProperty("bodyColor")
    private Properties bodyColor;

    @JsonProperty("pregnancy")
    private Properties pregnancy;

    @JsonProperty("approximateAge")
    private Properties approximateAge;

    @JsonProperty("gait")
    private Properties gait;
    @JsonProperty("posture")
    private Properties posture;

    @JsonProperty("bodyTemperature")
    private Properties bodyTemperature;

    @JsonProperty("appetite")
    private Properties appetite;

    @JsonProperty("nostrils")
    private Properties nostrils;

    @JsonProperty("muzzle")
    private Properties muzzle;

    @JsonProperty("opinion")
    private Properties opinion;

    @JsonProperty("other")
    private String other;

    @JsonProperty("remark")
    private String remark;

    @JsonProperty("InspectionType")
    private String inspectionType;

    private Long animalTypeId;
}
