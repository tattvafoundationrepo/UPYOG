package org.egov.collection.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.junit.jupiter.api.Test;

/**
 * 07-Oct-2026 at the CFC: a cheque dated 07-Oct was refused as "future" (sent as 23:59:59 of the day) and one dated
 * 07-Jul as "not within 90 days" (07-Jul to 07-Oct is 92 days), though emarket-v1 and the form accept both.
 */
class ChequeDateWindowTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final int MAX_DAYS = 90;

    private static long ist(String dateTime) {
        return LocalDateTime.parse(dateTime).atZone(IST).toInstant().toEpochMilli();
    }

    /** What the CFC form sends for a picked day: 23:59:59 IST of it. */
    private static long picked(String day) {
        return ist(day + "T23:59:59");
    }

    private static boolean accepted(long instrumentDate, long now) {
        return !ChequeDateWindow.isFutureDated(instrumentDate, now)
                && !ChequeDateWindow.isTooOld(instrumentDate, now, MAX_DAYS);
    }

    /** The checks this replaces, exactly as they were. */
    private static boolean acceptedBefore(long instrumentDate, long now) {
        DateTime cheque = new DateTime(instrumentDate);
        boolean tooOld = Days.daysBetween(cheque, new DateTime(now)).getDays() > MAX_DAYS;
        boolean future = cheque.isAfter(now);
        return !tooOld && !future;
    }

    @Test
    void todaysChequeIsAcceptedAllDay() {
        assertTrue(accepted(picked("2026-10-07"), ist("2026-10-07T00:00:01")));
        assertTrue(accepted(picked("2026-10-07"), ist("2026-10-07T18:22:00")));
        assertFalse(acceptedBefore(picked("2026-10-07"), ist("2026-10-07T18:22:00")), "the reported failure");
    }

    @Test
    void tomorrowsChequeIsStillFuture() {
        assertFalse(accepted(ist("2026-10-08T00:00:00"), ist("2026-10-07T23:59:59")));
        assertFalse(accepted(picked("2026-10-08"), ist("2026-10-07T18:22:00")));
    }

    @Test
    void threeMonthsBackIsAcceptedAndTheDayBeforeIsNot() {
        long now = ist("2026-10-07T18:22:00");
        assertTrue(accepted(picked("2026-07-07"), now));
        assertFalse(acceptedBefore(picked("2026-07-07"), now), "the reported failure");
        assertFalse(accepted(picked("2026-07-06"), now));
    }

    @Test
    void monthEndIsClamped() {
        // 31-May minus 3 months = 28-Feb (2027 is not a leap year).
        long now = ist("2027-05-31T12:00:00");
        assertTrue(accepted(picked("2027-02-28"), now));
        assertFalse(accepted(picked("2027-02-27"), now));
    }

    @Test
    void whereThreeMonthsIsShorterThanNinetyDaysTheOldRuleStillAccepts() {
        // 01-May: 3 months back is 01-Feb (89 days), the 90-day rule reached 31-Jan. Never stricter than before.
        long now = ist("2027-05-01T12:00:00");
        assertTrue(accepted(picked("2027-01-31"), now));
        assertTrue(accepted(picked("2027-01-30"), now), "90 whole days (and 12 hours) back: the old rule accepts it");
        assertFalse(accepted(picked("2027-01-29"), now));
    }

    /** Over two years of days and times: everything the old checks accepted is still accepted. */
    @Test
    void neverStricterThanBefore() {
        String[] times = { "00:00:00", "05:29:59", "05:30:00", "09:15:00", "12:00:00", "18:29:59", "18:30:00", "23:59:59" };
        LocalDate start = LocalDate.of(2026, 1, 1);
        int checked = 0;
        for (int d = 0; d < 730; d++) {
            LocalDate today = start.plusDays(d);
            long now = ist(today + "T14:07:00");
            for (int back = -2; back <= 100; back++) {
                for (String t : times) {
                    long cheque = ist(today.minusDays(back) + "T" + t);
                    if (acceptedBefore(cheque, now) && !accepted(cheque, now)) {
                        fail("now stricter for cheque " + today.minusDays(back) + "T" + t + " on " + today);
                    }
                    checked++;
                }
            }
        }
        assertTrue(checked > 500_000);
    }

    /** And it never accepts a cheque dated after today or before the wider of the two windows. */
    @Test
    void neverAcceptsOutsideTheWiderWindow() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        for (int d = 0; d < 730; d++) {
            LocalDate today = start.plusDays(d);
            long now = ist(today + "T14:07:00");
            assertFalse(accepted(picked(today.plusDays(1).toString()), now), "tomorrow on " + today);
            LocalDate earliest = today.minusMonths(3);
            LocalDate floor = earliest.isBefore(today.minusDays(91)) ? earliest : today.minusDays(91);
            assertFalse(accepted(ist(floor.minusDays(1) + "T00:00:00"), now), "too old on " + today);
        }
    }
}
