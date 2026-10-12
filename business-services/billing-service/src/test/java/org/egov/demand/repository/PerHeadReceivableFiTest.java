package org.egov.demand.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.stream.Collectors;

import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.FiDimensions;
import org.egov.demand.model.FiFlow;
import org.egov.demand.model.FiReport;
import org.junit.jupiter.api.Test;

/**
 * BMC's 24-Sep-2026 accounting entries ("dUpyog Accounting Entry", AO FAR), checked entry by entry
 * with the PDF's own illustration: Stallage 100 + Loft 10, GST 9% + 9% = 19.80, round-off 0.20,
 * total 130.
 *
 * <p>The GLs are the GL master's pairing for a GST (taxable) licensee: STALLAGE 130100301 /
 * 431409938, LOFT 140809912 / 431409946. The PDF prints 130100300 and 431409957 beside them, which
 * the master shows are the NON-taxable accounts; BMC has been asked to confirm.
 *
 * <p>Pure logic: no database. Open balances are computed here from the rows themselves, the same
 * signed sum DemandRepository.openReceivableByDemand runs in SQL.
 */
public class PerHeadReceivableFiTest {

    private static final String STALLAGE_T = "130100301";
    private static final String STALLAGE_T_4S = "431409938";
    private static final String LOFT_T = "140809912";
    private static final String LOFT_T_4S = "431409946";
    private static final String STALLAGE_NT = "130100300";
    private static final String STALLAGE_NT_4S = "431409937";
    private static final String LOFT_NT = "140810034";
    private static final String LOFT_NT_4S = "431409957";
    private static final String COIN = "350119997";
    private static final String NA = "NA";
    private static final String RECEIVABLE = "431409936";
    private static final String ADVANCE = "350410215";
    private static final String BANK = "450100100";

    private static final DemandRepository.AdvanceReceiptRef ADVANCE_RECEIPT =
            new DemandRepository.AdvanceReceiptRef("MARKET/26-27/000047", 1743465600000L);

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static DemandRepository repo(boolean perHead, boolean fourSeriesPair) throws Exception {
        DemandRepository r = new DemandRepository();
        set(r, "perHeadReceivableEnabled", perHead);
        set(r, "advanceFourSeriesEnabled", fourSeriesPair);
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

    private static Demand demand(String id, boolean againstAdvance, DemandDetail... details) {
        Map<String, String> dims = new HashMap<>();
        dims.put("fund", "11");
        dims.put("fundCenter", "4120420103");
        dims.put("businessArea", "4120");
        dims.put("functionalArea", "55800000000");
        Demand d = Demand.builder().id(id).consumerCode("5000007528rf").businessService("TX.Emarket_Rental_Fees")
                .taxPeriodFrom(1743465600000L).additionalDetails(dims).build();
        d.setDemandDetails(new ArrayList<>(Arrays.asList(details)));
        d.setApportionedAgainstAdvance(againstAdvance);
        return d;
    }

    /** PDF illustration, GST licensee. collected = tax on every head when settled from an advance. */
    private static Demand pdfDemand(String id, boolean againstAdvance) {
        String c = againstAdvance ? null : "0";
        return demand(id, againstAdvance,
                detail("STALLAGE", "100", c == null ? "100" : c, STALLAGE_T, STALLAGE_T_4S),
                detail("LOFT", "10", c == null ? "10" : c, LOFT_T, LOFT_T_4S),
                detail("CGST", "9.90", c == null ? "9.90" : c, "350200421", null),
                detail("SGST", "9.90", c == null ? "9.90" : c, "350200422", null),
                detail("GST_CA", "0.20", c == null ? "0.20" : c, COIN, null));
    }

    private static List<FiReport> build(DemandRepository r, Demand d) {
        return r.buildDemandFiReports(d, Collections.<DemandDetail>emptyList(), ADVANCE_RECEIPT);
    }

    private static BigDecimal sum(List<FiReport> rows, String pk) {
        return rows.stream().filter(x -> pk.equals(x.getPostingKey())).map(FiReport::getCollectionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal on(List<FiReport> rows, String gl, String pk) {
        return rows.stream().filter(x -> gl.equals(x.getGlCode()) && pk.equals(x.getPostingKey()))
                .map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void assertBalanced(List<FiReport> rows) {
        assertEquals(0, sum(rows, "40").compareTo(sum(rows, "50")),
                "Dr " + sum(rows, "40") + " != Cr " + sum(rows, "50") + " in " + rows);
    }

    private static void assertAmount(String expected, BigDecimal actual, String what) {
        assertEquals(0, bd(expected).compareTo(actual), what + ": expected " + expected + " got " + actual);
    }

    // ------------------------------------------------------------------ the arithmetic

    @Test
    void pdfIllustrationSplitsGstIntoEachHead() {
        List<PerHeadReceivable.Line> lines = PerHeadReceivable.lines(Arrays.asList(
                new PerHeadReceivable.Head("STALLAGE", bd("100"), STALLAGE_T_4S),
                new PerHeadReceivable.Head("LOFT", bd("10"), LOFT_T_4S),
                new PerHeadReceivable.Head("CGST", bd("9.90"), NA),
                new PerHeadReceivable.Head("SGST", bd("9.90"), NA),
                new PerHeadReceivable.Head("GST_CA", bd("0.20"), NA)), bd("130"), NA);
        assertEquals(3, lines.size(), lines.toString());
        assertEquals(STALLAGE_T_4S, lines.get(0).glCode);
        assertAmount("118", lines.get(0).amount, "Revenue Stallage");
        assertEquals(LOFT_T_4S, lines.get(1).glCode);
        assertAmount("11.80", lines.get(1).amount, "Revenue Loft");
        assertEquals(NA, lines.get(2).glCode);
        assertAmount("0.20", lines.get(2).amount, "4 Series Coin note");
    }

    @Test
    void negativeCarryForwardIsNettedAndTheTotalIsKept() {
        List<PerHeadReceivable.Line> lines = PerHeadReceivable.lines(Arrays.asList(
                new PerHeadReceivable.Head("STALLAGE", bd("570"), STALLAGE_T_4S),
                new PerHeadReceivable.Head("LOFT", bd("144"), LOFT_T_4S),
                new PerHeadReceivable.Head("CGST", bd("64.26"), NA),
                new PerHeadReceivable.Head("SGST", bd("64.26"), NA),
                new PerHeadReceivable.Head("GST_CA", bd("0.48"), NA),
                new PerHeadReceivable.Head("TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD", bd("-300"), ADVANCE)),
                bd("543"), NA);
        BigDecimal total = lines.stream().map(l -> l.amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertAmount("543", total, "lines must sum to the receivable");
        assertTrue(lines.stream().noneMatch(l -> ADVANCE.equals(l.glCode)), "carry-forward gets no line of its own");
    }

    @Test
    void aNegativeRoundOffIsNettedNotLeftOnALineThatCannotClear() {
        List<PerHeadReceivable.Line> lines = PerHeadReceivable.lines(Arrays.asList(
                new PerHeadReceivable.Head("STALLAGE", bd("100.30"), STALLAGE_T_4S),
                new PerHeadReceivable.Head("GST_CA", bd("-0.30"), NA)), bd("100"), NA);
        assertEquals(1, lines.size(), lines.toString());
        assertAmount("100", lines.get(0).amount, "head carries the net");
    }

    @Test
    void aMarkedChargeThatPostedItsVoucherIsSettledAsPosted() throws Exception {
        // billing without marker support posted Dr 431409936 240 for the charge; its later payment must
        // clear that receivable, not credit income a second time.
        DemandRepository r = new DemandRepository() {
            @Override
            public Map<String, Map<String, BigDecimal>> openReceivableByDemand(java.util.Collection<String> ids) {
                Map<String, Map<String, BigDecimal>> m = new HashMap<>();
                m.put("cbf1", new LinkedHashMap<>(Collections.singletonMap(RECEIVABLE, bd("240"))));
                return m;
            }
        };
        Map<String, Object> marker = new HashMap<>();
        marker.put("fiRecognition", "ON_COLLECTION");
        Demand cbf = Demand.builder().id("cbf1").businessService("TX.Emarket_Dishonor_Fees")
                .additionalDetails(marker).build();
        cbf.setDemandDetails(new ArrayList<>(Arrays.asList(detail("CHQ_BOUNCE_CHARGE", "200", "0", "180809906", null))));
        Map<String, BigDecimal> paid = new LinkedHashMap<>();
        paid.put("cbf1", bd("240"));
        Map<String, BigDecimal> byDetail = new HashMap<>();
        byDetail.put("CHQ_BOUNCE_CHARGE-id", bd("200"));
        RegularCollectionPlan p = r.regularCollectionPlan(paid, byDetail, Collections.singletonMap("cbf1", cbf));
        assertFalse(p.isActive(), "nothing new: the old single line, on 431409936");

        // The same charge with no voucher behind it IS credited to income.
        DemandRepository none = new DemandRepository() {
            @Override
            public Map<String, Map<String, BigDecimal>> openReceivableByDemand(java.util.Collection<String> ids) {
                return new HashMap<>();
            }
        };
        RegularCollectionPlan q = none.regularCollectionPlan(paid, byDetail, Collections.singletonMap("cbf1", cbf));
        assertTrue(q.isActive());
        assertEquals("180809906", q.creditsFor(bd("200"), "cbf1").get(0).glCode);
    }

    @Test
    void aDemandWithNoRevenueHeadKeepsTheSingleLine() {
        assertTrue(PerHeadReceivable.lines(Collections.singletonList(
                new PerHeadReceivable.Head("TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD", bd("-720"), ADVANCE)),
                bd("-720"), NA).isEmpty());
    }

    @Test
    void splitAndAllocateAlwaysSumExactly() {
        Map<String, BigDecimal> w = new LinkedHashMap<>();
        w.put("a", bd("33.33"));
        w.put("b", bd("33.33"));
        w.put("c", bd("33.34"));
        for (String amt : new String[] { "0.01", "1", "99.99", "100", "100.01", "-7.77" }) {
            BigDecimal s = PerHeadReceivable.splitByWeights(bd(amt), w).values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertAmount(amt, s, "split " + amt);
        }
        for (String amt : new String[] { "0.01", "1", "50", "99.99", "100" }) {
            Map<String, BigDecimal> a = PerHeadReceivable.allocateCapped(bd(amt), w);
            assertAmount(amt, a.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add), "allocate " + amt);
            a.forEach((k, v) -> assertTrue(v.compareTo(w.get(k)) <= 0, k + " over its cap"));
        }
        assertAmount("100", PerHeadReceivable.allocateCapped(bd("250"), w).values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add), "allocation never exceeds the balances");
    }

    // ------------------------------------------------------------------ entry 1: regular demand

    @Test
    void flagOffIsTheSingleReceivableExactlyAsBefore() throws Exception {
        List<FiReport> off = build(repo(false, false), pdfDemand("d1", false));
        assertEquals(6, off.size(), off.toString());
        assertAmount("130", on(off, RECEIVABLE, "40"), "single receivable");
        assertTrue(off.stream().noneMatch(r -> r.getGlCode() != null && r.getGlCode().startsWith("4314099")
                && !RECEIVABLE.equals(r.getGlCode())));
        assertBalanced(off);
    }

    @Test
    void entry1RegularDemand() throws Exception {
        List<FiReport> rows = build(repo(true, false), pdfDemand("d1", false));
        assertAmount("118", on(rows, STALLAGE_T_4S, "40"), "Dr Revenue Stallage");
        assertAmount("11.80", on(rows, LOFT_T_4S, "40"), "Dr Revenue Loft");
        assertAmount("0.20", on(rows, NA, "40"), "Dr 4 Series Coin note");
        assertAmount("100", on(rows, STALLAGE_T, "50"), "Cr Stallage");
        assertAmount("10", on(rows, LOFT_T, "50"), "Cr Loft");
        assertAmount("9.90", on(rows, "350200421", "50"), "Cr CGST");
        assertAmount("9.90", on(rows, "350200422", "50"), "Cr SGST");
        assertAmount("0.20", on(rows, COIN, "50"), "Cr Coin note");
        assertAmount("0", on(rows, RECEIVABLE, "40"), "no control-account debit");
        assertBalanced(rows);
    }

    @Test
    void entry1NonGstLicensee() throws Exception {
        Demand d = demand("d2", false,
                detail("STALLAGE", "100", "0", STALLAGE_NT, STALLAGE_NT_4S),
                detail("LOFT", "10", "0", LOFT_NT, LOFT_NT_4S));
        List<FiReport> rows = build(repo(true, false), d);
        assertAmount("100", on(rows, STALLAGE_NT_4S, "40"), "Dr 431409937");
        assertAmount("10", on(rows, LOFT_NT_4S, "40"), "Dr 431409957");
        assertBalanced(rows);
    }

    @Test
    void aHeadWithNoFourSeriesGlPostsNa() throws Exception {
        Demand penalty = demand("p1", false, detail("STALLAGE_PENALTY", "60", "0", "140409942", null),
                detail("RENT_PENALTY_CA", "0.25", "0", COIN, null));
        penalty.setBusinessService("TX.Emarket_Penalty_Fees");
        List<FiReport> rows = build(repo(true, false), penalty);
        assertAmount("60.25", on(rows, NA, "40"), "penalty and its round-off on NA");
        assertBalanced(rows);
    }

    // ------------------------------------------------------------------ entries 4, 5, 6

    @Test
    void entries4And6DemandAgainstAdvance() throws Exception {
        List<FiReport> rows = build(repo(true, true), pdfDemand("d3", true));
        // Entry 4: the demand itself.
        assertAmount("118", on(rows, STALLAGE_T_4S, "40"), "Dr Revenue Stallage");
        assertAmount("11.80", on(rows, LOFT_T_4S, "40"), "Dr Revenue Loft");
        assertAmount("100", on(rows, STALLAGE_T, "50"), "Cr Stallage once, never twice");
        // Entry 6: Revenue Adjustment.
        assertAmount("130", on(rows, ADVANCE, "40"), "Dr 3Series Liability");
        assertAmount("118", on(rows, STALLAGE_T_4S, "50"), "Cr Revenue Stallage");
        assertAmount("11.80", on(rows, LOFT_T_4S, "50"), "Cr Revenue Loft");
        assertAmount("0.20", on(rows, NA, "50"), "Cr 4 Series Coin note");
        assertTrue(rows.stream().noneMatch(r -> "upmktdemdadv4s".equals(r.getReportType())),
                "the 4-Series pair is retired while per-head is on, whatever its own flag says");
        rows.stream().filter(r -> "50".equals(r.getPostingKey()) && STALLAGE_T_4S.equals(r.getGlCode()))
                .forEach(r -> assertEquals("MARKET/26-27/000047", r.getAssignment()));
        assertBalanced(rows);
    }

    @Test
    void partlySettledDemandAdjustsOnlyTheSettledPart() throws Exception {
        Demand d = demand("d4", true,
                detail("STALLAGE", "100", "50", STALLAGE_T, STALLAGE_T_4S),
                detail("LOFT", "10", "0", LOFT_T, LOFT_T_4S),
                detail("CGST", "9.90", "9.90", "350200421", null),
                detail("SGST", "9.90", "0", "350200422", null),
                detail("GST_CA", "0.20", "0", COIN, null));
        List<FiReport> rows = build(repo(true, false), d);
        assertAmount("59.90", on(rows, ADVANCE, "40"), "advance released = settled");
        assertAmount("59.90", sum(rows.stream().filter(r -> DemandRepository.REVENUE_ADJUSTMENT_REMARK
                .equals(r.getRemarks())).collect(Collectors.toList()), "50"), "Revenue Adjustment = settled");
        assertBalanced(rows);
    }

    // ------------------------------------------------------------------ entries 2, 7, 9: collection

    private static Demand voucher(String firstDemandId) {
        Demand d = Demand.builder().id(firstDemandId).consumerCode("5000007528rf")
                .businessService("TX.Emarket_Rental_Fees").taxPeriodFrom(1743465600000L).build();
        d.setFund("11");
        d.setFundCenter("4120420103");
        d.setBusinessArea("4120");
        d.setFunctionalArea("55800000000");
        d.setFiReceiptNo("MARKET/26-27/000099");
        d.setPaymentMode("CASH");
        return d;
    }

    /** The signed open balance per GL of one demand's rows, as openReceivableByDemand computes it. */
    private static Map<String, BigDecimal> open(List<FiReport> rows, String demandId) {
        Map<String, BigDecimal> m = new TreeMap<>();
        for (FiReport r : rows) {
            if (!demandId.equals(r.getTransactionNumber()) || r.getGlCode() == null)
                continue;
            if ("upmktdemdadv4s".equals(r.getReportType()) || "upmktdemdadv4srev".equals(r.getReportType()))
                continue; // the superseded pair has its own unwind and is never collected
            String gl = r.getGlCode();
            boolean receivable = gl.equals(NA) || gl.equals(RECEIVABLE)
                    || (gl.compareTo("431409937") >= 0 && gl.compareTo("431409977") <= 0);
            if (!receivable)
                continue;
            BigDecimal v = "40".equals(r.getPostingKey()) ? r.getCollectionAmount() : r.getCollectionAmount().negate();
            m.merge(gl, v, BigDecimal::add);
        }
        m.entrySet().removeIf(e -> e.getValue().signum() <= 0);
        return m;
    }

    private static RegularCollectionPlan plan(String demandId, String paid, Map<String, BigDecimal> openByGl) {
        return new RegularCollectionPlan(Collections.singletonList(
                new RegularCollectionPlan.PaidDemand(demandId, bd(paid), openByGl, null)),
                RECEIVABLE, DemandRepository.LEGACY_RECEIVABLE_GLS);
    }

    @Test
    void entry2RegularCollectionClearsWhatTheDemandDebited() throws Exception {
        DemandRepository r = repo(true, false);
        List<FiReport> demandRows = build(r, pdfDemand("d5", false));
        List<FiReport> coll = r.buildCollectionFiReports(voucher("d5"), FiFlow.GST_REGULAR, bd("130"), bd("9.90"),
                bd("9.90"), false, 1L, plan("d5", "130", open(demandRows, "d5")));
        assertAmount("130", on(coll, BANK, "40"), "Dr Interim Receipt");
        assertAmount("118", on(coll, STALLAGE_T_4S, "50"), "Cr Revenue Stallage");
        assertAmount("11.80", on(coll, LOFT_T_4S, "50"), "Cr Revenue Loft");
        assertAmount("0.20", on(coll, NA, "50"), "Cr 4 Series Coin note");
        assertAmount("0", on(coll, RECEIVABLE, "50"), "nothing on the control account");
        coll.stream().filter(x -> "50".equals(x.getPostingKey()))
                .forEach(x -> assertEquals("d5", x.getTransactionNumber(), "credit names its demand"));
        assertBalanced(coll);
        List<FiReport> all = new ArrayList<>(demandRows);
        all.addAll(coll);
        assertTrue(open(all, "d5").isEmpty(), "every receivable closes: " + open(all, "d5"));
    }

    @Test
    void entry7ChequeCollectionUsesTheChequeGl() throws Exception {
        DemandRepository r = repo(true, false);
        List<FiReport> demandRows = build(r, pdfDemand("d6", false));
        Demand v = voucher("d6");
        v.setPaymentMode("CHEQUE");
        List<FiReport> coll = r.buildCollectionFiReports(v, FiFlow.GST_REGULAR, bd("130"), bd("9.90"), bd("9.90"),
                false, 1L, plan("d6", "130", open(demandRows, "d6")));
        assertAmount("130", on(coll, "450210010", "40"), "Dr Interim Receipt Cheque");
        assertAmount("118", on(coll, STALLAGE_T_4S, "50"), "Cr Revenue Stallage");
        assertBalanced(coll);
    }

    @Test
    void partialPaymentStaysWithinEachBalance() throws Exception {
        DemandRepository r = repo(true, false);
        List<FiReport> demandRows = build(r, pdfDemand("d7", false));
        List<FiReport> first = r.buildCollectionFiReports(voucher("d7"), FiFlow.GST_REGULAR, bd("50"), bd("9.90"),
                bd("9.90"), false, 1L, plan("d7", "50", open(demandRows, "d7")));
        assertBalanced(first);
        List<FiReport> all = new ArrayList<>(demandRows);
        all.addAll(first);
        List<FiReport> second = r.buildCollectionFiReports(voucher("d7"), FiFlow.GST_REGULAR, bd("80"), bd("9.90"),
                bd("9.90"), false, 1L, plan("d7", "80", open(all, "d7")));
        assertBalanced(second);
        all.addAll(second);
        assertTrue(open(all, "d7").isEmpty(), "two partial payments clear every account: " + open(all, "d7"));
        assertAmount("0", on(first, RECEIVABLE, "50").add(on(second, RECEIVABLE, "50")), "no spill to the control account");
    }

    @Test
    void anOldShapeDemandKeepsTheSingleLine() throws Exception {
        DemandRepository off = repo(false, false);
        List<FiReport> demandRows = build(off, pdfDemand("d8", false));
        RegularCollectionPlan p = plan("d8", "130", open(demandRows, "d8"));
        assertFalse(p.isActive(), "a demand on 431409936 alone does not activate the plan");
        List<FiReport> withPlan = repo(true, false).buildCollectionFiReports(voucher("d8"), FiFlow.GST_REGULAR,
                bd("130"), bd("9.90"), bd("9.90"), false, 1L, p);
        List<FiReport> without = repo(true, false).buildCollectionFiReports(voucher("d8"), FiFlow.GST_REGULAR,
                bd("130"), bd("9.90"), bd("9.90"), false, 1L);
        assertEquals(describe(without), describe(withPlan), "byte-identical to the old voucher");
    }

    @Test
    void anOldDemandAgainstAdvanceWithThePairStillCollectsOnTheControlAccount() throws Exception {
        Demand d = demand("old-adv", true,
                detail("STALLAGE", "100", "50", STALLAGE_T, STALLAGE_T_4S),
                detail("LOFT", "10", "0", LOFT_T, LOFT_T_4S),
                detail("CGST", "9.90", "0", "350200421", null),
                detail("SGST", "9.90", "0", "350200422", null),
                detail("GST_CA", "0.20", "0", COIN, null));
        List<FiReport> rows = build(repo(false, true), d);
        assertTrue(rows.stream().anyMatch(x -> "upmktdemdadv4s".equals(x.getReportType())), "pair posted");
        RegularCollectionPlan p = plan("old-adv", "80", open(rows, "old-adv"));
        assertFalse(p.isActive(), "the pair's 4-Series debit must not pull cash onto 431409938");
    }

    @Test
    void aMigratedDemandWithNoDocumentFallsBackToTheControlAccount() throws Exception {
        DemandRepository r = repo(true, false);
        List<FiReport> d1 = build(r, pdfDemand("n1", false));
        RegularCollectionPlan p = new RegularCollectionPlan(Arrays.asList(
                new RegularCollectionPlan.PaidDemand("n1", bd("130"), open(d1, "n1"), null),
                new RegularCollectionPlan.PaidDemand("migrated", bd("240"), null, null)),
                RECEIVABLE, DemandRepository.LEGACY_RECEIVABLE_GLS);
        List<FiReport> coll = r.buildCollectionFiReports(voucher("n1"), FiFlow.NON_GST_REGULAR, bd("370"),
                BigDecimal.ZERO, BigDecimal.ZERO, false, 1L, p);
        assertAmount("240", on(coll, RECEIVABLE, "50"), "migrated dues stay on 431409936");
        assertAmount("118", on(coll, STALLAGE_T_4S, "50"), "new demand clears its own accounts");
        assertBalanced(coll);
    }

    @Test
    void entry9ChargesAreIncomeAtCollection() throws Exception {
        DemandRepository r = repo(true, false);
        List<FiReport> rent = build(r, pdfDemand("d9", false));
        FiDimensions bounceDims = new FiDimensions("11", "1000130000", "1000", "00301000000");
        FiDimensions adminDims = new FiDimensions("11", "4030130000", "4030", "00301000000");
        RegularCollectionPlan p = new RegularCollectionPlan(Arrays.asList(
                new RegularCollectionPlan.PaidDemand("d9", bd("130"), open(rent, "d9"), null),
                new RegularCollectionPlan.PaidDemand("cbf1", bd("240"), null, Arrays.asList(
                        new RegularCollectionPlan.Charge("CHQ_BOUNCE_CHARGE", "180809906", bd("200"), bounceDims),
                        new RegularCollectionPlan.Charge("Administrative_Charge", "140709903", bd("40"), adminDims)))),
                RECEIVABLE, DemandRepository.LEGACY_RECEIVABLE_GLS);
        List<FiReport> coll = r.buildCollectionFiReports(voucher("d9"), FiFlow.GST_REGULAR, bd("370"), bd("9.90"),
                bd("9.90"), false, 1L, p);
        assertAmount("370", on(coll, BANK, "40"), "Dr Interim Receipt Cash = rent + charges");
        assertAmount("200", on(coll, "180809906", "50"), "Cr Cheque Bounce Charges");
        assertAmount("40", on(coll, "140709903", "50"), "Cr Administrative Charges");
        assertAmount("118", on(coll, STALLAGE_T_4S, "50"), "Cr Revenue Stallage");
        FiReport bounce = coll.stream().filter(x -> "180809906".equals(x.getGlCode())).findFirst().get();
        assertEquals("1000130000", bounce.getFundCentre());
        assertEquals("1000", bounce.getBusinessArea());
        FiReport admin = coll.stream().filter(x -> "140709903".equals(x.getGlCode())).findFirst().get();
        assertEquals("4030", admin.getBusinessArea());
        assertBalanced(coll);
    }

    @Test
    void theRegularHalfOfASplitReceiptUsesThePlanAndTheAdvanceHalfIsUntouched() throws Exception {
        DemandRepository r = repo(true, false);
        List<FiReport> rent = build(r, pdfDemand("d10", false));
        RegularCollectionPlan p = plan("d10", "130", open(rent, "d10"));
        List<FiReport> adv = r.buildCollectionFiReports(voucher("d10"), FiFlow.GST_ADVANCE, bd("260"), bd("19.80"),
                bd("19.80"), false, 1L, p);
        List<FiReport> advOld = repo(false, false).buildCollectionFiReports(voucher("d10"), FiFlow.GST_ADVANCE,
                bd("260"), bd("19.80"), bd("19.80"), false, 1L);
        assertEquals(describe(advOld), describe(adv), "entry 3 (advance receipt) is unchanged");
        assertAmount("260", on(adv, ADVANCE, "50"), "Cr 3Series Liability, gross");
        assertAmount("19.80", on(adv, "439300200", "40"), "Dr Advance CGST");
        assertAmount("19.80", on(adv, "350200421", "50"), "Cr CGST");
    }

    @Test
    void aReversalNeverUsesThePlan() throws Exception {
        DemandRepository r = repo(true, false);
        List<FiReport> rent = build(r, pdfDemand("d11", false));
        List<FiReport> rev = r.buildCollectionFiReports(voucher("d11"), FiFlow.NON_GST_REGULAR, bd("130"),
                BigDecimal.ZERO, BigDecimal.ZERO, true, 1L, plan("d11", "130", open(rent, "d11")));
        assertAmount("130", on(rev, RECEIVABLE, "40"), "the rebuilt path is only for old receipts");
    }

    // ------------------------------------------------------------------ ledger closure sweep

    /**
     * Every combination of flag at demand time x settlement route, followed through to collection:
     * each voucher balances, and every receivable account of the demand closes to nil.
     */
    @Test
    void everyFlagCombinationClosesTheReceivable() throws Exception {
        for (boolean perHeadAtDemand : new boolean[] { false, true }) {
            for (boolean pairFlag : new boolean[] { false, true }) {
                for (String paidPattern : new String[] { "130", "60+70", "0.01+129.99" }) {
                    String id = "s-" + perHeadAtDemand + pairFlag + paidPattern;
                    List<FiReport> all = new ArrayList<>(build(repo(perHeadAtDemand, pairFlag), pdfDemand(id, false)));
                    assertBalanced(all);
                    for (String part : paidPattern.split("\\+")) {
                        RegularCollectionPlan p = plan(id, part, open(all, id));
                        List<FiReport> coll = repo(true, pairFlag).buildCollectionFiReports(voucher(id),
                                FiFlow.GST_REGULAR, bd(part), bd("9.90"), bd("9.90"), false, 1L, p);
                        assertBalanced(coll);
                        if (!p.isActive()) {
                            // Old shape: the single line, tagged with the voucher's own demand.
                            coll.forEach(x -> x.setTransactionNumber(id));
                        }
                        all.addAll(coll);
                    }
                    assertTrue(open(all, id).isEmpty(), id + " leaves " + open(all, id));
                }
            }
        }
    }

    @Test
    void theMarkerIsReadFromBothJsonShapes() {
        Demand d = Demand.builder().businessService("TX.Emarket_Dishonor_Fees").build();
        Map<String, Object> m = new HashMap<>();
        m.put("fiRecognition", "ON_COLLECTION");
        d.setAdditionalDetails(m);
        assertTrue(DemandRepository.isRecognisedOnCollection(d));
        com.fasterxml.jackson.databind.node.ObjectNode n =
                com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        n.put("fiRecognition", "ON_COLLECTION");
        d.setAdditionalDetails(n);
        assertTrue(DemandRepository.isRecognisedOnCollection(d));
        d.setBusinessService("TX.Emarket_Rental_Fees");
        assertFalse(DemandRepository.isRecognisedOnCollection(d), "only a dishonour charge can skip its FI");
        d.setBusinessService("TX.Emarket_Dishonor_Fees");
        d.setAdditionalDetails(new HashMap<>());
        assertFalse(DemandRepository.isRecognisedOnCollection(d), "unmarked charges post as before");
    }

    @Test
    void anUnstampedLineFallsBackToNaWithoutADatabase() throws Exception {
        DemandRepository r = repo(true, false);
        assertEquals(NA, r.receivableGlFor(detail("PREMIUM", "500", "0", "140110300", null)));
        assertEquals(STALLAGE_T_4S, r.receivableGlFor(detail("STALLAGE", "1", "0", STALLAGE_T, STALLAGE_T_4S)));
        DemandDetail fromDb = DemandDetail.builder().taxHeadMasterCode("STALLAGE").taxAmount(bd("1"))
                .additionalDetails(new com.fasterxml.jackson.databind.ObjectMapper().readTree(
                        "{\"glcode\":\"130100301\",\"advglcode\":\"431409938\"}")).build();
        assertEquals(STALLAGE_T_4S, r.receivableGlFor(fromDb), "a JsonNode detail is read too");
    }

    private static List<String> describe(List<FiReport> rows) {
        return rows.stream().map(x -> x.getPostingKey() + "|" + x.getGlCode() + "|" + x.getCollectionAmount() + "|"
                + x.getRemarks() + "|" + x.getTransactionNumber() + "|" + x.getBusinessArea() + "|" + x.getFundCentre())
                .collect(Collectors.toList());
    }
}
