package digit.web.models.inspection;

import lombok.AllArgsConstructor;

import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor


public enum IndicatorName {
    PULSE_RATE("Pulse Rate"),
    EYES("Eyes"),
    SPECIES("Species"),
    BREED("Breed"),
    SEX("Sex"),
    BODY_COLOR("Body Color"),
    PREGNANCY("Pregnancy"),
    APPROXIMATE_AGE("Approximate Age"),
    GAIT("Gait"),
    POSTURE("Posture"),
    BODY_TEMPERATURE("Body Temperature"),
    APPETITE("Appetite"),
    NOSTRILS("Nostrils"),
    MUZZLE("Muzzle"),
    OPINION("Opinion"),
    OTHER("Other"),
    REMARK("Remark");

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
