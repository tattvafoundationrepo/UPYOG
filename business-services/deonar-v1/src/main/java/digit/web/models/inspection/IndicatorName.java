package digit.web.models.inspection;

import lombok.AllArgsConstructor;

import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor


public enum IndicatorName {
    PULSE_RATE("pulseRate"),
    EYES("eyes"),
    SPECIES("species"),
    BREED("breed"),
    SEX("sex"),
    BODY_COLOR("bodyColor"),
    PREGNANCY("pregnancy"),
    APPROXIMATE_AGE("approxAge"),
    GAIT("gait"),
    POSTURE("posture"),
    BODY_TEMPERATURE("bodyTemp"),
    APPETITE("appetite"),
    NOSTRILS("nostrils"),
    MUZZLE("muzzle"),
    OPINION("opinion"),
    OTHER("other"),
    REMARK("remark"),
    SLAUGHTER_RECEIPT_NUMBER("slaughterReceiptNumber"),
    VISIBLE_MUCUS_MEMBRANE("visibleMucusMembrane"),
    THORACIC_CAVITY("thoracicCavity"),
    ABDOMINAL_CAVITY("abdominalCavity"),
    PELVIC_CAVITY("pelvicCavity"),
    SPECIMEN_COLLECTION("specimenCollection"),
    SPECIAL_OBSERVATION("specialObservation");

    private final String value;

    public static IndicatorName fromValue(String value) {
        for (IndicatorName name : IndicatorName.values()) {
            if (Objects.equals(name.getValue(), value)) {
                return name;
            }
        }
        return null;
    }

}
