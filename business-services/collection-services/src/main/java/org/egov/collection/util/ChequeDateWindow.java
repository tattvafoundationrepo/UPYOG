package org.egov.collection.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.joda.time.DateTime;
import org.joda.time.Days;

/**
 * The cheque/DD date window for a payment received today (no manual receipt date).
 *
 * <p>eMarket accepts a cheque dated within 3 calendar months up to and including the day it is presented, by the IST
 * calendar day (BMC, MARKET-130; emarket-v1 ChequeValidity and the CFC collect form apply the same rule). The
 * original checks here compared exact instants instead: "after now" refused TODAY's cheque, because the form sends a
 * picked day as 23:59:59 of that day, and "more than 90 days" refused the oldest day or two of a 3-month window
 * (7-Jul to 7-Oct is 92 days).
 *
 * <p>Both checks only ever accept MORE than before, never less, so no module that posts cheques through this service
 * can start failing: a date is refused as future only when its IST day is after today (an instant at or before now
 * always falls on today or earlier), and as too old only when BOTH the original 90-day rule and the 3-month rule
 * refuse it (3 calendar months is shorter than 90 days in some weeks, e.g. 1-Feb to 1-May is 89 days).
 */
public final class ChequeDateWindow {

    static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    /** Calendar months a cheque/DD stays valid, matching emarket-v1's emarket.cheque.validity-months default. */
    static final int VALIDITY_MONTHS = 3;

    private ChequeDateWindow() {
    }

    /** True when the cheque/DD is dated on a later IST day than {@code now}. */
    public static boolean isFutureDated(long instrumentDate, long now) {
        return istDay(instrumentDate).isAfter(istDay(now));
    }

    /**
     * True when the cheque/DD is too old: before the same IST day {@link #VALIDITY_MONTHS} months back AND more than
     * {@code maxDays} whole days before {@code now} (the original rule).
     */
    public static boolean isTooOld(long instrumentDate, long now, int maxDays) {
        if (!istDay(instrumentDate).isBefore(istDay(now).minusMonths(VALIDITY_MONTHS))) {
            return false;
        }
        try {
            return Days.daysBetween(new DateTime(instrumentDate), new DateTime(now)).getDays() > maxDays;
        } catch (ArithmeticException e) {
            // More days than an int holds: a nonsense date centuries back, certainly too old (a 400, not a 500).
            return true;
        }
    }

    static LocalDate istDay(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(IST).toLocalDate();
    }
}
