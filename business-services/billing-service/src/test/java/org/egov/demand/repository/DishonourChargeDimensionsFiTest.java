package org.egov.demand.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.demand.model.AuditDetails;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.FiReport;
import org.junit.jupiter.api.Test;

/**
 * BMC, 22-09-2026: on the dishonoured-cheque voucher (upmktdischq) the cheque bounce charge posts
 * at fund centre 1000130000 / business area 1000, and the administrative charge posts at the CFC
 * ward where the cheque was dishonoured. Both use functional area 00301000000. emarket-v1 stamps
 * those on each line; this pins that billing-service posts a line at its own dimensions, leaves the
 * receivable on the market's, and changes nothing for a line that carries none.
 *
 * Worked case: licence 5000007519 (market K/East, BA 4120) whose cheque was taken and bounced at
 * the ward C counter (BA 4030), as on UAT 22-09-2026.
 */
public class DishonourChargeDimensionsFiTest {

    private static Map<String, String> marketDimensions() {
        Map<String, String> dims = new HashMap<>();
        dims.put("fund", "11");
        dims.put("fundCenter", "4120420103");
        dims.put("businessArea", "4120");
        dims.put("functionalArea", "55800000000");
        return dims;
    }

    private static DemandDetail line(String head, String amount, String gl, Map<String, Object> extra) {
        Map<String, Object> additional = new HashMap<>();
        additional.put("glcode", gl);
        additional.put("saccode", "999111");
        if (extra != null) {
            additional.putAll(extra);
        }
        return DemandDetail.builder()
                .taxHeadMasterCode(head)
                .taxAmount(new BigDecimal(amount))
                .collectionAmount(BigDecimal.ZERO)
                .additionalDetails(additional)
                .build();
    }

    private static Demand demand(String consumerCode, String businessService, DemandDetail... lines) {
        Demand d = Demand.builder()
                .id("demand-1")
                .consumerCode(consumerCode)
                .businessService(businessService)
                .taxPeriodFrom(1743465600000L)
                .additionalDetails(marketDimensions())
                .build();
        d.setDemandDetails(new ArrayList<>(Arrays.asList(lines)));
        return d;
    }

    private static FiReport row(List<FiReport> rows, String gl) {
        return rows.stream().filter(r -> gl.equals(r.getGlCode())).findFirst()
                .orElseThrow(() -> new AssertionError("no row for GL " + gl));
    }

    private static void assertDims(FiReport r, String fund, String fc, String ba, String fa) {
        assertEquals(fund, r.getFund(), r.getGlCode() + " fund");
        assertEquals(fc, r.getFundCentre(), r.getGlCode() + " fund centre");
        assertEquals(ba, r.getBusinessArea(), r.getGlCode() + " business area");
        assertEquals(fa, r.getFunctionalArea(), r.getGlCode() + " functional area");
    }

    @Test
    public void dishonourLinesPostAtTheirOwnDimensionsAndTheReceivableStaysOnTheMarket() {
        Map<String, Object> bounce = new HashMap<>();
        bounce.put("fundCenter", "1000130000");
        bounce.put("businessArea", "1000");
        bounce.put("functionalArea", "00301000000");
        Map<String, Object> admin = new HashMap<>();
        admin.put("fund", "11");
        admin.put("fundCenter", "4030130000");
        admin.put("businessArea", "4030");
        admin.put("functionalArea", "00301000000");

        List<FiReport> rows = new DemandRepository().buildDemandFiReports(demand("5000007519cbf",
                "TX.Emarket_Dishonor_Fees",
                line("CHQ_BOUNCE_CHARGE", "200", "180809906", bounce),
                line("Administrative_Charge", "40", "140709903", admin)));

        assertEquals(3, rows.size());
        // Fund is not part of the bounce rule, so it keeps the demand's.
        assertDims(row(rows, "180809906"), "11", "1000130000", "1000", "00301000000");
        assertDims(row(rows, "140709903"), "11", "4030130000", "4030", "00301000000");
        assertDims(row(rows, "431409936"), "11", "4120420103", "4120", "55800000000");
        assertEquals("50", row(rows, "180809906").getPostingKey());
        assertEquals("50", row(rows, "140709903").getPostingKey());
        assertEquals("40", row(rows, "431409936").getPostingKey());
        assertEquals(new BigDecimal("240"), row(rows, "431409936").getCollectionAmount());
    }

    /** A dishonour charge raised before emarket-v1 stamped dimensions posts exactly as it used to. */
    @Test
    public void aLineWithNoDimensionsKeepsTheDemandsOnes() {
        List<FiReport> rows = new DemandRepository().buildDemandFiReports(demand("5000007519cbf",
                "TX.Emarket_Dishonor_Fees",
                line("CHQ_BOUNCE_CHARGE", "200", "180809906", null),
                line("Administrative_Charge", "40", "140709903", null)));

        for (FiReport r : rows) {
            assertDims(r, "11", "4120420103", "4120", "55800000000");
        }
    }

    /** Blank stamped values must not blank the posting; the demand's value fills the gap. */
    @Test
    public void aBlankLineValueFallsBackToTheDemand() {
        Map<String, Object> partial = new HashMap<>();
        partial.put("fundCenter", "  ");
        partial.put("businessArea", "1000");
        List<FiReport> rows = new DemandRepository().buildDemandFiReports(demand("5000007519cbf",
                "TX.Emarket_Dishonor_Fees",
                line("CHQ_BOUNCE_CHARGE", "200", "180809906", partial)));

        assertDims(row(rows, "180809906"), "11", "4120420103", "1000", "55800000000");
    }

    private static final long DISHONOURED_ON = 1759123800000L;

    /** BMC, 29-09-2026: every leg of the discheque voucher is dated the day of the dishonour. */
    @Test
    public void dishonourVoucherIsDatedOnTheDishonourDay() {
        Demand d = demand("5000007519cbf", "TX.Emarket_Dishonor_Fees",
                line("CHQ_BOUNCE_CHARGE", "200", "180809906", null),
                line("Administrative_Charge", "40", "140709903", null));
        d.setAuditDetails(AuditDetails.builder().createdTime(DISHONOURED_ON).build());

        List<FiReport> rows = new DemandRepository().buildDemandFiReports(d);

        assertEquals(3, rows.size());
        for (FiReport r : rows) {
            assertEquals(Long.valueOf(DISHONOURED_ON), r.getDocDate(), r.getGlCode() + " doc date");
            assertEquals(Long.valueOf(DISHONOURED_ON), r.getPostingDate(), r.getGlCode() + " posting date");
        }
    }

    private static void assertDated(List<FiReport> rows, long expected) {
        for (FiReport r : rows) {
            assertEquals(Long.valueOf(expected), r.getDocDate(), r.getGlCode() + " doc date");
            assertEquals(Long.valueOf(expected), r.getPostingDate(), r.getGlCode() + " posting date");
        }
    }

    private static Demand dishonour(String businessService) {
        return demand("5000007519cbf", businessService,
                line("CHQ_BOUNCE_CHARGE", "200", "180809906", null),
                line("Administrative_Charge", "40", "140709903", null));
    }

    @Test
    public void dishonourWithoutAuditDetailsKeepsTheTaxPeriod() {
        assertDated(new DemandRepository().buildDemandFiReports(dishonour("TX.Emarket_Dishonor_Fees")), 1743465600000L);
    }

    @Test
    public void dishonourWithoutACreatedTimeKeepsTheTaxPeriod() {
        Demand d = dishonour("TX.Emarket_Dishonor_Fees");
        d.setAuditDetails(AuditDetails.builder().lastModifiedTime(DISHONOURED_ON).build());
        assertDated(new DemandRepository().buildDemandFiReports(d), 1743465600000L);
    }

    @Test
    public void businessServiceMatchIsCaseInsensitiveLikeTheReportTypeCheck() {
        Demand d = dishonour("tx.emarket_dishonor_fees");
        d.setAuditDetails(AuditDetails.builder().createdTime(DISHONOURED_ON).build());
        assertDated(new DemandRepository().buildDemandFiReports(d), DISHONOURED_ON);
    }

    /** 31-03-2026 23:30 IST, a rent demand of Apr-2026: the voucher stays in the old financial year. */
    @Test
    public void financialYearBoundaryFollowsTheDishonourInstant() {
        long lastEveningOfFy = 1774980000000L;
        Demand d = dishonour("TX.Emarket_Dishonor_Fees");
        d.setAuditDetails(AuditDetails.builder().createdTime(lastEveningOfFy).build());
        assertDated(new DemandRepository().buildDemandFiReports(d), lastEveningOfFy);
    }

    @Test
    public void everyOtherDemandTypeKeepsItsTaxPeriod() {
        String[][] cases = {
                {"5000007519rf", "TX.Emarket_Rental_Fees", "STALLAGE", "130100300"},
                {"5000007519prf", "TX.Emarket_Penalty_Rental_Fees", "PENALTY", "140709901"},
                {"5000007519lf", "TX.Emarket_License_Fees", "LICENSE_FEE", "140709902"},
                {"5000007519tf", "TX.Emarket_Transfer_Fees", "TRANSFER_FEE", "140709904"}};
        for (String[] c : cases) {
            Demand d = demand(c[0], c[1], line(c[2], "500", c[3], null));
            d.setAuditDetails(AuditDetails.builder().createdTime(DISHONOURED_ON).build());
            assertDated(new DemandRepository().buildDemandFiReports(d), 1743465600000L);
        }
    }

    /** The create path reaches save() through the Kafka consumer's ObjectMapper; createdTime must survive it. */
    @Test
    public void createdTimeSurvivesTheKafkaRoundTrip() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Demand d = dishonour("TX.Emarket_Dishonor_Fees");
        d.setAuditDetails(AuditDetails.builder().createdTime(DISHONOURED_ON).build());
        Map<?, ?> wire = mapper.convertValue(d, Map.class);
        Demand back = mapper.convertValue(wire, Demand.class);
        assertDated(new DemandRepository().buildDemandFiReports(back), DISHONOURED_ON);
    }

    private static Demand withEnteredDate(Demand d, Object enteredDate) {
        Map<String, Object> additional = new HashMap<>(marketDimensions());
        additional.put("dishonourDate", enteredDate);
        d.setAdditionalDetails(additional);
        d.setAuditDetails(AuditDetails.builder().createdTime(DISHONOURED_ON).build());
        return d;
    }

    /** 25-09-2025 00:00 UTC, what the clerk's "2025-09-25" becomes. */
    private static final long ENTERED_DAY = 1758758400000L;

    @Test
    public void theEnteredBankReturnDateWinsOverTheDayItWasRaised() {
        Demand d = withEnteredDate(dishonour("TX.Emarket_Dishonor_Fees"), "2025-09-25");
        assertDated(new DemandRepository().buildDemandFiReports(d), ENTERED_DAY);
        // The dimensions are untouched by the date.
        assertDims(row(new DemandRepository().buildDemandFiReports(d), "431409936"), "11", "4120420103", "4120", "55800000000");
    }

    @Test
    public void anUnreadableEnteredDateFallsBackToTheDayItWasRaised() {
        for (Object bad : new Object[] { "25-09-2025", "2025-02-30", "", "  ", null }) {
            Demand d = withEnteredDate(dishonour("TX.Emarket_Dishonor_Fees"), bad);
            assertDated(new DemandRepository().buildDemandFiReports(d), DISHONOURED_ON);
        }
    }

    @Test
    public void anEnteredDateOnAnyOtherDemandIsIgnored() {
        Demand d = withEnteredDate(demand("5000007519rf", "TX.Emarket_Rental_Fees",
                line("STALLAGE", "500", "130100300", null)), "2025-09-25");
        assertDated(new DemandRepository().buildDemandFiReports(d), 1743465600000L);
    }

    /** The shape billing-service really sees: additionalDetails deserialised from the create request. */
    @Test
    public void theEnteredDateSurvivesTheKafkaRoundTrip() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Demand d = withEnteredDate(dishonour("TX.Emarket_Dishonor_Fees"), "2025-09-25");
        Demand back = mapper.convertValue(mapper.convertValue(d, Map.class), Demand.class);
        assertDated(new DemandRepository().buildDemandFiReports(back), ENTERED_DAY);
    }

    @Test
    public void aRentDemandKeepsItsTaxPeriod() {
        Demand d = demand("5000007519rf", "TX.Emarket_Rental_Fees", line("STALLAGE", "500", "130100300", null));
        d.setAuditDetails(AuditDetails.builder().createdTime(DISHONOURED_ON).build());

        for (FiReport r : new DemandRepository().buildDemandFiReports(d)) {
            assertEquals(Long.valueOf(1743465600000L), r.getDocDate());
            assertEquals(Long.valueOf(1743465600000L), r.getPostingDate());
        }
    }

    @Test
    public void anOrdinaryRentDemandIsUnchanged() {
        List<FiReport> rows = new DemandRepository().buildDemandFiReports(demand("5000007519rf",
                "TX.Emarket_Rental_Fees",
                line("STALLAGE", "500", "130100300", null)));

        assertEquals(2, rows.size());
        for (FiReport r : rows) {
            assertDims(r, "11", "4120420103", "4120", "55800000000");
        }
    }
}
