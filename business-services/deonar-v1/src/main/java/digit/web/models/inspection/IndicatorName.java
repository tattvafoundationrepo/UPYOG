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
    APPROXIMATE_AGE("approximateAge"),
    GAIT("gait"),
    POSTURE("posture"),
    BODY_TEMPERATURE("bodyTemperature"),
    APPETITE("appetite"),
    NOSTRILS("nostrils"),
    MUZZLE("muzzle"),
    OPINION("opinion"),
    OTHER("other"),
    REMARK("remark");

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
