package org.egov.collection.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import org.egov.tracer.model.CustomException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The bank return (dishonour) date an eMarket clerk enters when dishonouring a cheque/DD. It rides
 * on the DISHONOUR workflow's additionalDetails, is merged into each bill's additionaldetails, and
 * billing-service and emarket-v1 date the collection reversal and the dishonour charge by it.
 *
 * <p>Checked here because this is the last point before the receipt is reversed. Optional: a
 * DISHONOUR sent without it (any other module) is not affected. Bounds: not before the receipt day,
 * not after today (IST). The cheque date is deliberately not a bound: on SAP-migrated receipts it is
 * typically months after the receipt, which would forbid the true date or block the dishonour.
 */
public final class DishonourDateValidator {

    public static final String DISHONOUR_DATE_KEY = "dishonourDate";

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private DishonourDateValidator() {
    }

    /** Rewrites a valid value as canonical YYYY-MM-DD; throws INVALID_DISHONOUR_DATE otherwise. */
    public static void validate(JsonNode additionalDetails, Long transactionDate, LocalDate today) {
        if (additionalDetails == null || !additionalDetails.has(DISHONOUR_DATE_KEY)
                || additionalDetails.get(DISHONOUR_DATE_KEY).isNull()) {
            return;
        }
        JsonNode value = additionalDetails.get(DISHONOUR_DATE_KEY);
        if (!value.isTextual()) {
            throw invalid("The dishonour date must be a date in YYYY-MM-DD format.");
        }
        LocalDate day;
        try {
            day = LocalDate.parse(value.asText().trim());
        } catch (DateTimeParseException e) {
            throw invalid("The dishonour date must be a date in YYYY-MM-DD format.");
        }
        if (day.isAfter(today)) {
            throw invalid("The dishonour date cannot be in the future.");
        }
        if (transactionDate != null && day.isBefore(istDay(transactionDate))) {
            throw invalid("The dishonour date cannot be before " + istDay(transactionDate) + ", the receipt date.");
        }
        if (additionalDetails instanceof ObjectNode) {
            ((ObjectNode) additionalDetails).put(DISHONOUR_DATE_KEY, day.toString());
        }
    }

    public static LocalDate todayInIst() {
        return LocalDate.now(IST);
    }

    private static LocalDate istDay(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(IST).toLocalDate();
    }

    private static CustomException invalid(String message) {
        return new CustomException("INVALID_DISHONOUR_DATE", message);
    }
}
