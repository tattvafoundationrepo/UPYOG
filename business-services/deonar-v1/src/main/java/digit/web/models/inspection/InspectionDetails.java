package digit.web.models.inspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.web.models.security.AnimalDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InspectionDetails {

    private Long inspectionId;

    private Long inspectionDetailId;

    private AnimalDetail animalDetail;

    @JsonProperty("animalTokenNumber")
    private Long animalTokenNumber;

    @JsonProperty("entryUnitId")
    private String arrivalId;

    @JsonProperty("eyes")
    private String eyes;

    @JsonProperty("pulseRate")
    private String pulseRate;

    @JsonProperty("species")
    private String species;

    @JsonProperty("breed")
    private String breed;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("bodyColor")
    private String bodyColor;

    @JsonProperty("pregnancy")
    private String pregnancy;

    @JsonProperty("approxAge")
    private String approximateAge;

    @JsonProperty("gait")
    private String gait;
    @JsonProperty("posture")
    private String posture;

    @JsonProperty("bodyTemp")
    private String bodyTemperature;

    @JsonProperty("appetite")
    private String appetite;

    @JsonProperty("nostrils")
    private String nostrils;

    @JsonProperty("muzzle")
    private String muzzle;

    private String slaughterReceiptNumber;
    private String visibleMucusMembrane;
    private String thoracicCavity;
    private String abdominalCavity;
    private String pelvicCavity;
    private String specimenCollection;
    private String specialObservation;

    @JsonProperty("opinion")
    private String opinion;

    @JsonProperty("other")
    private String other;

    @JsonProperty("remark")
    private String remark;


    private Long animalTypeId;
    private String report;


}

