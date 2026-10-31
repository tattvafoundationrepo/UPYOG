package org.egov.demand.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.FiReport;
import org.egov.demand.model.FiReportType;
import org.junit.jupiter.api.Test;

/**
 * BMC 30-10-2026 transfer rule: the old licence's unpaid dues move to the new licence as a reclassification JV —
 * Dr 4-Series receivable (new licence reference) / Cr the same 4-Series receivable (old licence reference) — with no
 * revenue, no GST and no credit note. Example from BMC's sheet: Jan-Mar rent 3 x 354 = 1,062 and loft 3 x 35.40 =
 * 106.20 move from ABC (5000000111) to XYZ (5000000222).
 */
public class TransferJvFiTest {

    private static final String STALLAGE = "130100301";
    private static final String STALLAGE_4S = "431409938";
    private static final String LOFT = "140809912";
    private static final String LOFT_4S = "431409946";
    private static final String NEW_CODE = "5000000222rf";
    private static final String OLD_CODE = "5000000111rf";

    private static final DemandRepository.AdvanceReceiptRef NO_ADVANCE =
            new DemandRepository.AdvanceReceiptRef(null, null);

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static DemandRepository repo(boolean perHead) throws Exception {
        DemandRepository r = new DemandRepository();
        set(r, "perHeadReceivableEnabled", perHead);
        set(r, "advanceFourSeriesEnabled", true);
        return r;
    }

    private static void set(Object o, String field, Object value) throws Exception {
        Field f = DemandRepository.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(o, value);
    }

    private static DemandDetail detail(String head, String tax, String collected, String gl, String advGl) {
        Map<String, Object> add = new HashMap<>();
        if (gl != null)
            add.put("glcode", gl);
        if (advGl != null)
            add.put("advglcode", advGl);
        return DemandDetail.builder().id(head + "-id").taxHeadMasterCode(head).taxAmount(bd(tax))
                .collectionAmount(bd(collected)).additionalDetails(add).build();
    }

    private static Map<String, Object> source(String code, Object... headAmounts) {
        Map<String, Object> heads = new LinkedHashMap<>();
        for (int i = 0; i < headAmounts.length; i += 2)
            heads.put((String) headAmounts[i], headAmounts[i + 1]);
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("consumerCode", code);
        s.put("heads", heads);
        return s;
    }

    @SafeVarargs
    private static Demand carried(Object marker, boolean againstAdvance, String collected,
                                  Map<String, Object>... ignored) {
        Map<String, Object> add = new HashMap<>();
        add.put("fund", "11");
        add.put("fundCenter", "4120420103");
        add.put("businessArea", "4120");
        add.put("functionalArea", "55800000000");
        if (marker != null)
            add.put(TransferJv.KEY, marker);
        Demand d = Demand.builder().id("new-demand").consumerCode(NEW_CODE).businessService("TX.Emarket_Rental_Fees")
                .taxPeriodFrom(1767225600000L).additionalDetails(add).build();
        d.setDemandDetails(new ArrayList<>(Arrays.asList(
                detail("STALLAGE", "900", collected, STALLAGE, STALLAGE_4S),
                detail("LOFT", "90", collected, LOFT, LOFT_4S),
                detail("CGST", "89.10", collected, "350200421", null),
                detail("SGST", "89.10", collected, "350200422", null))));
        d.setApportionedAgainstAdvance(againstAdvance);
        return d;
    }

    private static Map<String, Object> marker(Map<String, Object>... sources) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("applicationNo", "TRF-0001");
        m.put("sources", Arrays.asList(sources));
        return m;
    }

    private static Map<String, Object> singleSource() {
        return marker(source(OLD_CODE, "STALLAGE", 900.0, "LOFT", 90.0, "CGST", 89.1, "SGST", 89.1));
    }

    private static List<FiReport> build(DemandRepository r, Demand d) {
        return r.buildDemandFiReports(d, Collections.<DemandDetail>emptyList(), NO_ADVANCE);
    }

    private static BigDecimal sum(List<FiReport> rows, String pk) {
        return rows.stream().filter(x -> pk.equals(x.getPostingKey())).map(FiReport::getCollectionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal on(List<FiReport> rows, String gl, String pk, String ref) {
        return rows.stream().filter(x -> gl.equals(x.getGlCode()) && pk.equals(x.getPostingKey())
                && ref.equals(x.getReferenceNo()))
                .map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** openReceivableByDemand's signed sum, with its transfer-JV source-leg exclusion. */
    private static Map<String, BigDecimal> openOfNewDemand(List<FiReport> rows) {
        Map<String, BigDecimal> open = new TreeMap<>();
        for (FiReport r : rows) {
            String t = r.getReportType() == null ? "" : r.getReportType();
            if (t.equals(FiReportType.UPMKT_TRF_JV) && "50".equals(r.getPostingKey()))
                continue;
            if (t.equals(FiReportType.UPMKT_TRF_JV_REV) && "40".equals(r.getPostingKey()))
                continue;
            if (!r.getGlCode().startsWith("4314099"))
                continue;
            BigDecimal v = "40".equals(r.getPostingKey()) ? r.getCollectionAmount() : r.getCollectionAmount().negate();
            open.merge(r.getGlCode(), v, BigDecimal::add);
        }
        open.values().removeIf(v -> v.signum() == 0);
        return open;
    }

    @Test
    void bmcExampleMovesEachReceivableFromTheOldLicenceToTheNewOneWithNoRevenueOrGst() throws Exception {
        List<FiReport> rows = build(repo(true), carried(singleSource(), false, "0"));

        assertEquals(4, rows.size(), rows.toString());
        assertEquals(0, bd("1062.00").compareTo(on(rows, STALLAGE_4S, "40", NEW_CODE)));
        assertEquals(0, bd("106.20").compareTo(on(rows, LOFT_4S, "40", NEW_CODE)));
        assertEquals(0, bd("1062.00").compareTo(on(rows, STALLAGE_4S, "50", OLD_CODE)));
        assertEquals(0, bd("106.20").compareTo(on(rows, LOFT_4S, "50", OLD_CODE)));
        assertEquals(0, sum(rows, "40").compareTo(sum(rows, "50")));
        rows.forEach(r -> {
            assertEquals(FiReportType.UPMKT_TRF_JV, r.getReportType());
            assertEquals("new-demand", r.getTransactionNumber());
            assertTrue(r.getGlCode().startsWith("4314099"), "no revenue / GST account: " + r);
        });
    }

    @Test
    void collectionsOnTheNewLicenceStillSeeTheWholeReceivable() throws Exception {
        Map<String, BigDecimal> open = openOfNewDemand(build(repo(true), carried(singleSource(), false, "0")));
        assertEquals(0, bd("1062.00").compareTo(open.get(STALLAGE_4S)));
        assertEquals(0, bd("106.20").compareTo(open.get(LOFT_4S)));
    }

    @Test
    void theMirrorOfTheJvLeavesNothingOpen() throws Exception {
        List<FiReport> rows = new ArrayList<>(build(repo(true), carried(singleSource(), false, "0")));
        List<FiReport> mirror = new ArrayList<>();
        for (FiReport r : rows) {
            mirror.add(FiReport.builder().glCode(r.getGlCode()).referenceNo(r.getReferenceNo())
                    .collectionAmount(r.getCollectionAmount()).reportType(FiReportType.UPMKT_TRF_JV_REV)
                    .postingKey("40".equals(r.getPostingKey()) ? "50" : "40").build());
        }
        rows.addAll(mirror);
        assertTrue(openOfNewDemand(rows).isEmpty());
        // ...and the old licence's reference is back to what its own demands debited.
        assertEquals(0, bd("1062.00").compareTo(on(rows, STALLAGE_4S, "40", OLD_CODE)));
    }

    @Test
    void anUnmarkedDemandPostsItsOrdinaryVoucher() throws Exception {
        List<FiReport> rows = build(repo(true), carried(null, false, "0"));
        assertTrue(rows.stream().anyMatch(r -> STALLAGE.equals(r.getGlCode()) && "50".equals(r.getPostingKey())));
        assertTrue(rows.stream().anyMatch(r -> "350200421".equals(r.getGlCode())));
        assertTrue(rows.stream().noneMatch(r -> FiReportType.UPMKT_TRF_JV.equals(r.getReportType())));
        assertTrue(rows.stream().noneMatch(r -> OLD_CODE.equals(r.getReferenceNo())));
    }

    @Test
    void aMalformedMarkerFallsBackToTheOrdinaryVoucher() throws Exception {
        Map<String, Object> bad = new HashMap<>();
        bad.put("sources", "not-a-list");
        Demand d = carried(bad, false, "0");
        assertTrue(TransferJv.isMarked(d));
        assertNull(TransferJv.sources(d));
        List<FiReport> rows = build(repo(true), d);
        assertTrue(rows.stream().anyMatch(r -> STALLAGE.equals(r.getGlCode())));
    }

    @Test
    void amalgamationCreditsEachSourceLicenceItsOwnShareOfEachHead() throws Exception {
        Map<String, Object> m = marker(
                source("5000000111rf", "STALLAGE", 600.0, "LOFT", 90.0, "CGST", 62.1, "SGST", 62.1),
                source("5000000333rf", "STALLAGE", 300.0, "CGST", 27.0, "SGST", 27.0));
        List<FiReport> rows = build(repo(true), carried(m, false, "0"));

        assertEquals(0, bd("708.00").compareTo(on(rows, STALLAGE_4S, "50", "5000000111rf")));
        assertEquals(0, bd("354.00").compareTo(on(rows, STALLAGE_4S, "50", "5000000333rf")));
        assertEquals(0, bd("106.20").compareTo(on(rows, LOFT_4S, "50", "5000000111rf")));
        assertEquals(0, BigDecimal.ZERO.compareTo(on(rows, LOFT_4S, "50", "5000000333rf")));
        assertEquals(0, sum(rows, "40").compareTo(sum(rows, "50")));
    }

    @Test
    void splitAlwaysAddsBackToTheAmount() {
        Demand d = Demand.builder().additionalDetails(Collections.singletonMap(TransferJv.KEY, marker(
                source("a", "STALLAGE", 1.0), source("b", "STALLAGE", 1.0), source("c", "STALLAGE", 1.0))))
                .build();
        Map<String, BigDecimal> parts = TransferJv.split(bd("100"), "STALLAGE", TransferJv.sources(d));
        assertEquals(Arrays.asList(bd("33.33"), bd("33.33"), bd("33.34")), new ArrayList<>(parts.values()));
        // A head no source names is split by the sources' totals.
        Map<String, BigDecimal> coin = TransferJv.split(bd("0.30"), "GST_CA", TransferJv.sources(d));
        assertEquals(0, bd("0.30").compareTo(coin.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    @Test
    void withoutThePerHeadReceivableTheControlAccountMoves() throws Exception {
        List<FiReport> rows = build(repo(false), carried(singleSource(), false, "0"));
        assertEquals(2, rows.size(), rows.toString());
        FiReport dr = rows.stream().filter(r -> "40".equals(r.getPostingKey())).findFirst().get();
        FiReport cr = rows.stream().filter(r -> "50".equals(r.getPostingKey())).findFirst().get();
        assertEquals(dr.getGlCode(), cr.getGlCode());
        assertEquals(NEW_CODE, dr.getReferenceNo());
        assertEquals(OLD_CODE, cr.getReferenceNo());
        assertEquals(0, bd("1168.20").compareTo(dr.getCollectionAmount()));
        assertEquals(0, bd("1168.20").compareTo(cr.getCollectionAmount()));
    }

    @Test
    void settledFromAnAdvanceTheDocumentStillBalancesAndKeepsTheAdvanceLegs() throws Exception {
        List<FiReport> rows = build(repo(true), carried(singleSource(), true, "1168.20"));
        assertEquals(0, sum(rows, "40").compareTo(sum(rows, "50")), rows.toString());
        assertTrue(rows.stream().anyMatch(r -> "350410215".equals(r.getGlCode()) && r.getReportType() == null));
        assertFalse(rows.stream().anyMatch(r -> STALLAGE.equals(r.getGlCode()) || "350200421".equals(r.getGlCode())));
        assertEquals(0, bd("1062.00").compareTo(on(rows, STALLAGE_4S, "50", OLD_CODE)));
    }
}
