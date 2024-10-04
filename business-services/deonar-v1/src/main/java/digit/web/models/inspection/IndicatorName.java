package digit.web.models.inspection;

import lombok.AllArgsConstructor;

import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor


public enum IndicatorName {
    PULSE_RATE(1L),
    EYES(2L),
    SPECIES(3L),
    BREED(4L),
    SEX(5L),
    BODY_COLOR(6L),
    PREGNANCY(7L),
    APPROXIMATE_AGE(8L),
    GAIT(9L),
    POSTURE(10L),
    BODY_TEMPERATURE(11L),
    APPETITE(12L),
    NOSTRILS(13L),
    MUZZLE(14L),
    OPINION(15L),
    OTHER(16L),
    REMARK(17L);

    private final Long id;
    public static IndicatorName fromId(Long value) {
        for (IndicatorName name : IndicatorName.values()) {
            if(Objects.equals(name.getId(), value)) {
                return name;
            }
        }
        return null;
    }

}
