package org.egov.demand.util;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

/**
 * The bank return (dishonour) date the clerk entered on the dishonour screen, as YYYY-MM-DD.
 * collection-services validates it and keeps it on the bill; the collection reversal and the
 * dishonour charge both use it for their doc and posting dates.
 *
 * <p>Written as 00:00 UTC of that day, the MDMS tax-period convention, which reads as the same
 * calendar day in IST and in UTC.
 */
@Slf4j
public final class DishonourDate {

    public static final String KEY = "dishonourDate";

    private DishonourDate() {
    }

    /** The raw value under {@link #KEY} in a Map or JsonNode, or null. */
    public static String rawFrom(Object additionalDetails) {
        Object value = null;
        if (additionalDetails instanceof Map) {
            value = ((Map<?, ?>) additionalDetails).get(KEY);
        } else if (additionalDetails instanceof JsonNode) {
            JsonNode node = ((JsonNode) additionalDetails).get(KEY);
            value = node == null || node.isNull() ? null : node.asText();
        }
        return value == null ? null : value.toString();
    }

    /** Epoch millis of 00:00 UTC on that day; null when absent or not a valid YYYY-MM-DD date. */
    public static Long toEpochMillis(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            log.warn("Ignoring an invalid dishonour date '{}'", raw);
            return null;
        }
    }

    public static Long fromAdditionalDetails(Object additionalDetails) {
        return toEpochMillis(rawFrom(additionalDetails));
    }
}
