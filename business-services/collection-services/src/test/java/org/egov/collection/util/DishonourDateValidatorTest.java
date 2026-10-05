package org.egov.collection.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DishonourDateValidatorTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 29);
    /** Receipt taken 20-09-2026 10:00 IST. */
    private static final long RECEIPT = ist("2026-09-20T10:00");

    private static long ist(String dateTime) {
        return LocalDateTime.parse(dateTime).atZone(IST).toInstant().toEpochMilli();
    }

    private static ObjectNode details(String date) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("dishonourReason", "Insufficient funds");
        if (date != null) {
            node.put("dishonourDate", date);
        }
        return node;
    }

    private static String reject(ObjectNode details, Long receipt) {
        CustomException e = assertThrows(CustomException.class,
                () -> DishonourDateValidator.validate(details, receipt, TODAY));
        assertEquals("INVALID_DISHONOUR_DATE", e.getCode());
        return e.getMessage();
    }

    @Test
    public void absentNullOrMissingDetailsAreLeftAlone() {
        DishonourDateValidator.validate(null, RECEIPT, TODAY);
        ObjectNode none = details(null);
        DishonourDateValidator.validate(none, RECEIPT, TODAY);
        assertFalse(none.has("dishonourDate"));
        ObjectNode explicitNull = details(null);
        explicitNull.putNull("dishonourDate");
        DishonourDateValidator.validate(explicitNull, RECEIPT, TODAY);
    }

    @Test
    public void validDatesPassIncludingBothBoundaries() {
        for (String ok : new String[] { "2026-09-20", "2026-09-25", "2026-09-29" }) {
            ObjectNode d = details(ok);
            DishonourDateValidator.validate(d, RECEIPT, TODAY);
            assertEquals(ok, d.get("dishonourDate").asText());
        }
    }

    @Test
    public void valueIsTrimmedToCanonicalForm() {
        ObjectNode d = details(" 2026-09-25 ");
        DishonourDateValidator.validate(d, RECEIPT, TODAY);
        assertEquals("2026-09-25", d.get("dishonourDate").asText());
    }

    @Test
    public void futureDateIsRejected() {
        reject(details("2026-09-30"), RECEIPT);
    }

    /** Past dates are selectable without the old receipt-day floor (BMC, 05-10-2026). */
    @Test
    public void dateBeforeTheReceiptIsAccepted() {
        for (String ok : new String[] { "2026-09-19", "2026-01-15", "2000-01-01" }) {
            ObjectNode d = details(ok);
            DishonourDateValidator.validate(d, RECEIPT, TODAY);
            assertEquals(ok, d.get("dishonourDate").asText());
        }
    }

    @Test
    public void dayBeforeTheEarliestDayIsRejected() {
        String message = reject(details("1999-12-31"), RECEIPT);
        org.junit.jupiter.api.Assertions.assertTrue(message.contains("2000-01-01"), message);
    }

    @Test
    public void malformedValuesAreRejected() {
        for (String bad : new String[] { "", "29-09-2026", "2026/09/25", "2026-02-30", "2026-13-01", "yesterday", "2026-9-5" }) {
            reject(details(bad), RECEIPT);
        }
        ObjectNode numeric = details(null);
        numeric.put("dishonourDate", 1759104000000L);
        reject(numeric, RECEIPT);
    }

    @Test
    public void noReceiptDateStillChecksTheFuture() {
        DishonourDateValidator.validate(details("2020-01-01"), null, TODAY);
        reject(details("2026-10-01"), null);
    }

    @Test
    public void financialYearBoundaryIsAPlainDay() {
        LocalDate aprilSecond = LocalDate.of(2026, 4, 2);
        DishonourDateValidator.validate(details("2026-03-31"), ist("2026-03-30T12:00"), aprilSecond);
    }

    @Test
    public void parsesFromTheWireShape() throws Exception {
        ObjectNode wire = (ObjectNode) new ObjectMapper().readTree(
                "{\"dishonourReason\":\"Refer to drawer\",\"dishonourDate\":\"2026-09-22\"}");
        DishonourDateValidator.validate(wire, RECEIPT, TODAY);
        assertEquals("2026-09-22", wire.get("dishonourDate").asText());
    }

    /** Other modules send whatever additionalDetails they like; a non-object never blocks their DISHONOUR. */
    @Test
    public void nonObjectAdditionalDetailsAreLeftAlone() {
        JsonNodeFactory f = JsonNodeFactory.instance;
        DishonourDateValidator.validate(f.arrayNode().add("2026-09-25"), RECEIPT, TODAY);
        DishonourDateValidator.validate(f.textNode("dishonourDate"), RECEIPT, TODAY);
        DishonourDateValidator.validate(f.nullNode(), RECEIPT, TODAY);
        DishonourDateValidator.validate(com.fasterxml.jackson.databind.node.MissingNode.getInstance(), RECEIPT, TODAY);
    }

    @Test
    public void nonTextValuesAreRejected() {
        ObjectNode bool = details(null);
        bool.put("dishonourDate", true);
        reject(bool, RECEIPT);
        ObjectNode obj = details(null);
        obj.putObject("dishonourDate").put("day", "2026-09-25");
        reject(obj, RECEIPT);
        ObjectNode arr = details(null);
        arr.putArray("dishonourDate").add("2026-09-25");
        reject(arr, RECEIPT);
    }

    @Test
    public void blankAndDateTimeStringsAreRejected() {
        for (String bad : new String[] { "   ", "\t", "2026-09-25T10:00", "2026-09-25Z", "2026-09-25+05:30",
                "20260925", "2026-09-25 2026-09-26", "२०२६-०९-२५" }) {
            reject(details(bad), RECEIPT);
        }
    }

    @Test
    public void extremeYearsAreRejected() {
        reject(details("+10000-01-01"), RECEIPT);
        reject(details("0001-01-01"), RECEIPT);
        reject(details("1900-01-01"), RECEIPT);
    }

    /** A receipt taken at 23:50 IST may be dishonoured the same day; the bound is the day, not the instant. */
    @Test
    public void sameDayAsALateEveningReceiptIsAllowed() {
        long lateReceipt = ist("2026-09-25T23:50");
        DishonourDateValidator.validate(details("2026-09-25"), lateReceipt, LocalDate.of(2026, 9, 25));
        DishonourDateValidator.validate(details("2026-09-24"), lateReceipt, LocalDate.of(2026, 9, 25));
    }
}
