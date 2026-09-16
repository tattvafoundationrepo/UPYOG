package org.egov.demand.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.FiDimensions;
import org.egov.demand.model.FiFlow;
import org.egov.demand.model.FiReport;
import org.egov.demand.model.FiReportType;
import org.junit.jupiter.api.Test;

/**
 * Verification of the two changes BMC asked for:
 *
 * <ul>
 *   <li>the per-tax-head 4-Series pair on a demand raised against an advance — Dr the head's
 *       receivable account (431409937..431409977), Cr the head's revenue account;</li>
 *   <li>the interim-receipt dimensions on a collection voucher's money leg (Business Area = the
 *       collecting CFC's ward, Fund Centre = business area + 130000, Functional Area = 00301000000),
 *       on the collection and on its reversal.</li>
 * </ul>
 *
 * The single most important assertion in here is {@link #flagOffIsByteIdentical()} together with
 * {@link #aMappedHeadPostsItsFourSeriesGl()}: without the second, the whole feature can ship as a
 * no-op that still satisfies every balance, control-account and GST-return check, because a pair
 * that is never emitted trivially balances and changes nothing.
 *
 * Worked case throughout: licence 5000000284, monthly rent split STALLAGE 500 / LOFT 400 /
 * PEDHI 50, CGST and SGST 85.50 each on the 950 net.
 */
public class FourSeriesAndCfcDimensionVerifyTest {

    private static final String STALLAGE_REVENUE = "130100301";
    private static final String STALLAGE_4S = "431409938";
    private static final String LOFT_REVENUE = "140809912";
    private static final String LOFT_4S = "431409946";
    private static final String PEDHI_REVENUE = "140809917";
    private static final String PEDHI_4S = "431409949";

    private static final String RECEIVABLE = "431409936";
    private static final String ADVANCE = "350410215";
    private static final String BANK = "450100100";
    private static final String CGST_ADVANCE = "439300200";
    private static final String SGST_ADVANCE = "439300201";

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private DemandRepository repoWithFlag(boolean fourSeriesEnabled) throws Exception {
        DemandRepository repo = new DemandRepository();
        Field f = DemandRepository.class.getDeclaredField("advanceFourSeriesEnabled");
        f.setAccessible(true);
        f.setBoolean(repo, fourSeriesEnabled);
        return repo;
    }

    /**
     * The advance receipt is supplied rather than looked up, so these stay pure-logic tests with no
     * JdbcTemplate. It is the same value {@code getAdvanceReceipt} would return for licence
     * 5000000284's twelve-month advance.
     */
    private static final DemandRepository.AdvanceReceiptRef ADVANCE_RECEIPT =
            new DemandRepository.AdvanceReceiptRef("market9152139233", 1743465600000L);

    private static List<FiReport> build(DemandRepository repo, Demand demand) {
        return repo.buildDemandFiReports(demand, java.util.Collections.<DemandDetail>emptyList(),
                ADVANCE_RECEIPT);
    }

    /** A demand detail carrying both GLs, as GlSacMapperService stamps them. */
    private static DemandDetail detail(String head, String tax, String collected,
                                       String revenueGl, String advanceGl) {
        Map<String, Object> additional = new HashMap<>();
        if (revenueGl != null) {
            additional.put("glcode", revenueGl);
        }
        if (advanceGl != null) {
            additional.put("advglcode", advanceGl);
        }
        return DemandDetail.builder()
                .taxHeadMasterCode(head)
                .taxAmount(bd(tax))
                .collectionAmount(bd(collected))
                .additionalDetails(additional)
                .build();
    }

    private static Demand demandSettledFromAdvance(DemandDetail... details) {
        Map<String, String> dims = new HashMap<>();
        dims.put("fund", "11");
        dims.put("fundCenter", "4060420103");
        dims.put("businessArea", "4060");
        dims.put("functionalArea", "55800000000");

        Demand d = Demand.builder()
                .id("demand-1")
                .consumerCode("5000000284rf")
                .businessService("TX.Emarket_Rental_Fees")
                .taxPeriodFrom(1743465600000L)
                .additionalDetails(dims)
                .build();
        d.setDemandDetails(new ArrayList<>(java.util.Arrays.asList(details)));
        d.setApportionedAgainstAdvance(true);
        return d;
    }

    /** Fully settled from advance: every head's collectionAmount equals its taxAmount. */
    private static Demand fullySettledRentDemand() {
        return demandSettledFromAdvance(
                detail("STALLAGE", "500", "500", STALLAGE_REVENUE, STALLAGE_4S),
                detail("LOFT", "400", "400", LOFT_REVENUE, LOFT_4S),
                detail("PEDHI", "50", "50", PEDHI_REVENUE, PEDHI_4S),
                detail("CGST", "85.50", "85.50", "350200421", null),
                detail("SGST", "85.50", "85.50", "350200422", null));
    }

    private static BigDecimal debitTotal(List<FiReport> rows) {
        return rows.stream().filter(r -> "40".equals(r.getPostingKey()))
                .map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal creditTotal(List<FiReport> rows) {
        return rows.stream().filter(r -> "50".equals(r.getPostingKey()))
                .map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<FiReport> rowsOn(List<FiReport> rows, String glCode) {
        return rows.stream().filter(r -> glCode.equals(r.getGlCode())).collect(Collectors.toList());
    }

    private static String signature(List<FiReport> rows) {
        return rows.stream()
                .map(r -> r.getPostingKey() + "|" + r.getGlCode() + "|" + r.getCollectionAmount()
                        + "|" + r.getRemarks() + "|" + r.getReportType() + "|" + r.getBusinessArea()
                        + "|" + r.getFundCentre())
                .collect(Collectors.joining("\n"));
    }

    // ---------------------------------------------------------------- Part 1: the 4-Series pair

    /**
     * The flag genuinely gates output. With it off the voucher must be the one that shipped
     * before this change, row for row.
     */
    @Test
    public void flagOffIsByteIdentical() throws Exception {
        List<FiReport> off = build(repoWithFlag(false), fullySettledRentDemand());

        // Five revenue/GST credits plus the single advance debit. No 4-Series row anywhere.
        assertEquals(6, off.size());
        assertTrue(off.stream().noneMatch(r -> r.getGlCode() != null
                && r.getGlCode().compareTo("431409937") >= 0
                && r.getGlCode().compareTo("431409977") <= 0),
                "no 4-Series GL may appear with the flag off");
        assertEquals(1, rowsOn(off, ADVANCE).size());
        assertEquals(0, bd("1121.00").compareTo(rowsOn(off, ADVANCE).get(0).getCollectionAmount()));
    }

    /**
     * The assertion the whole feature hangs on. A head that HAS a 4-Series row must post it — if
     * the mapping lookup silently returns nothing, every other test here still passes.
     */
    @Test
    public void aMappedHeadPostsItsFourSeriesGl() throws Exception {
        List<FiReport> on = build(repoWithFlag(true), fullySettledRentDemand());

        List<FiReport> stallageDebit = on.stream()
                .filter(r -> STALLAGE_4S.equals(r.getGlCode()) && "40".equals(r.getPostingKey()))
                .collect(Collectors.toList());

        assertEquals(1, stallageDebit.size(), "STALLAGE must debit its 4-Series receivable");
        assertEquals(0, bd("500").compareTo(stallageDebit.get(0).getCollectionAmount()));
        assertEquals(STALLAGE_REVENUE, matchingCredit(on, "STALLAGE").getGlCode(),
                "the credit leg must be the head's ordinary revenue GL");
    }

    private static FiReport matchingCredit(List<FiReport> rows, String head) {
        return rows.stream()
                .filter(r -> FiReportType.UPMKT_DEMDADV_4S.equals(r.getReportType()))
                .filter(r -> "50".equals(r.getPostingKey()) && head.equals(r.getRemarks()))
                .findFirst().orElseThrow(() -> new AssertionError("no 4-Series credit leg for " + head));
    }

    /** Dr == Cr, with the flag on, for the full multi-head GST case. */
    @Test
    public void theDocumentBalancesWithThePairPosted() throws Exception {
        List<FiReport> on = build(repoWithFlag(true), fullySettledRentDemand());
        assertEquals(0, debitTotal(on).compareTo(creditTotal(on)),
                "Dr " + debitTotal(on) + " != Cr " + creditTotal(on));
    }

    /**
     * The pair is additive. 350410215 must still take the FULL settled amount, or the advance a
     * licensee paid stops unwinding to nil over its cycle.
     */
    @Test
    public void theAdvanceLegIsUntouchedByThePair() throws Exception {
        List<FiReport> off = build(repoWithFlag(false), fullySettledRentDemand());
        List<FiReport> on = build(repoWithFlag(true), fullySettledRentDemand());

        assertEquals(signature(rowsOn(off, ADVANCE)), signature(rowsOn(on, ADVANCE)));
        assertEquals(signature(rowsOn(off, RECEIVABLE)), signature(rowsOn(on, RECEIVABLE)));
    }

    /** CGST/SGST have no 4-Series account, by design: the pair covers the NET rent only. */
    @Test
    public void gstHeadsGetNoPair() throws Exception {
        List<FiReport> on = build(repoWithFlag(true), fullySettledRentDemand());

        List<FiReport> pair = on.stream()
                .filter(r -> FiReportType.UPMKT_DEMDADV_4S.equals(r.getReportType()))
                .collect(Collectors.toList());

        assertEquals(6, pair.size(), "three mapped heads => three pairs");
        assertTrue(pair.stream().noneMatch(r -> "CGST".equals(r.getRemarks()) || "SGST".equals(r.getRemarks())));

        BigDecimal paired = pair.stream().filter(r -> "40".equals(r.getPostingKey()))
                .map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, bd("950").compareTo(paired), "the pair totals the NET rent, not the gross");
    }

    /**
     * A head BMC supplied no 4-Series account for — PAYRI, PHALI, ONING and the rest — posts no
     * pair at all, and the document still balances.
     */
    @Test
    public void anUnmappedHeadPostsNoPair() throws Exception {
        Demand d = demandSettledFromAdvance(
                detail("STALLAGE", "500", "500", STALLAGE_REVENUE, STALLAGE_4S),
                detail("PAYRI", "120", "120", "140809916", null));

        List<FiReport> on = build(repoWithFlag(true), d);

        assertEquals(0, debitTotal(on).compareTo(creditTotal(on)));
        assertTrue(on.stream().noneMatch(r -> "PAYRI".equals(r.getRemarks())
                && FiReportType.UPMKT_DEMDADV_4S.equals(r.getReportType())));
        assertEquals(2, on.stream()
                .filter(r -> FiReportType.UPMKT_DEMDADV_4S.equals(r.getReportType())).count());
    }

    /**
     * A head with no mapping row at all stamps no glcode. It must post NOTHING rather than two
     * rows naming no account — the fallback the first draft of the plan called for.
     */
    @Test
    public void aHeadWithNoRevenueGlPostsNoPair() throws Exception {
        Demand d = demandSettledFromAdvance(
                detail("ONING", "300", "300", null, null),
                detail("STALLAGE", "500", "500", STALLAGE_REVENUE, STALLAGE_4S));

        List<FiReport> on = build(repoWithFlag(true), d);

        assertTrue(on.stream().filter(r -> FiReportType.UPMKT_DEMDADV_4S.equals(r.getReportType()))
                .noneMatch(r -> r.getGlCode() == null), "no pair row may name a null account");
        assertEquals(0, debitTotal(on).compareTo(creditTotal(on)));
    }

    /** A partially settled head pairs only what the advance actually covered. */
    @Test
    public void aPartiallySettledHeadPairsOnlyTheSettledPortion() throws Exception {
        Demand d = demandSettledFromAdvance(
                detail("STALLAGE", "500", "200", STALLAGE_REVENUE, STALLAGE_4S));

        List<FiReport> on = build(repoWithFlag(true), d);

        FiReport debit = on.stream()
                .filter(r -> STALLAGE_4S.equals(r.getGlCode())).findFirst().orElseThrow(AssertionError::new);
        assertEquals(0, bd("200").compareTo(debit.getCollectionAmount()));
        assertEquals(0, debitTotal(on).compareTo(creditTotal(on)));
        // and the unsettled 300 is still a real receivable
        assertEquals(0, bd("300").compareTo(rowsOn(on, RECEIVABLE).get(0).getCollectionAmount()));
    }

    /**
     * The pair carries its own report type, and the caller in save() must not overwrite it — that
     * type is what keeps these rows out of the GST return's report_type filter.
     */
    @Test
    public void thePairCarriesItsOwnReportTypeAndTheRestCarryNone() throws Exception {
        List<FiReport> on = build(repoWithFlag(true), fullySettledRentDemand());

        for (FiReport r : on) {
            boolean isPair = STALLAGE_4S.equals(r.getGlCode()) || LOFT_4S.equals(r.getGlCode())
                    || PEDHI_4S.equals(r.getGlCode())
                    || FiReportType.UPMKT_DEMDADV_4S.equals(r.getReportType());
            if (isPair) {
                assertEquals(FiReportType.UPMKT_DEMDADV_4S, r.getReportType());
            } else {
                assertEquals(null, r.getReportType(),
                        "every other row must arrive untyped so save() can label it");
            }
        }
    }

    /** Both legs share a remark, so the GST return's SELECT DISTINCT behaves predictably. */
    @Test
    public void bothLegsOfAPairShareTheirRemark() throws Exception {
        List<FiReport> on = build(repoWithFlag(true), fullySettledRentDemand());

        Map<String, List<FiReport>> byRemark = on.stream()
                .filter(r -> FiReportType.UPMKT_DEMDADV_4S.equals(r.getReportType()))
                .collect(Collectors.groupingBy(FiReport::getRemarks));

        assertEquals(3, byRemark.size());
        byRemark.forEach((remark, legs) -> {
            assertEquals(2, legs.size(), remark + " must have exactly two legs");
            assertEquals(0, legs.get(0).getCollectionAmount().compareTo(legs.get(1).getCollectionAmount()));
            assertFalse(legs.get(0).getPostingKey().equals(legs.get(1).getPostingKey()));
        });
    }

    /** An ordinary demand — no advance behind it — is completely unaffected by the flag. */
    @Test
    public void anOrdinaryDemandIsUnaffected() throws Exception {
        Demand d = demandSettledFromAdvance(
                detail("STALLAGE", "500", "0", STALLAGE_REVENUE, STALLAGE_4S));
        d.setApportionedAgainstAdvance(false);

        assertEquals(signature(build(repoWithFlag(false), d)),
                signature(build(repoWithFlag(true), d)));
    }

    /** Twelve months of a fully consumed advance must leave 350410215 at nil, flag or no flag. */
    @Test
    public void theAdvanceControlAccountStillClosesToNil() throws Exception {
        BigDecimal advanceBalance = bd("13452.00"); // 1121.00 x 12
        DemandRepository repo = repoWithFlag(true);

        for (int month = 0; month < 12; month++) {
            List<FiReport> rows = build(repo, fullySettledRentDemand());
            for (FiReport r : rowsOn(rows, ADVANCE)) {
                assertEquals("40", r.getPostingKey());
                advanceBalance = advanceBalance.subtract(r.getCollectionAmount());
            }
            assertEquals(0, debitTotal(rows).compareTo(creditTotal(rows)));
        }
        assertEquals(0, BigDecimal.ZERO.compareTo(advanceBalance),
                "advance left standing: " + advanceBalance);
    }

    // ------------------------------------------- Part 2: interim-receipt dimensions (CFC ward)

    /*
     * BMC's rule for the interim receipt, the money leg of a collection voucher: Business Area = the
     * ward of the CFC where the money was collected, Fund Centre = business area + 130000,
     * Functional Area = 00301000000. Every other leg stays with the licensee's market.
     *
     * The market below is 4060 / 4060420103 / 55800000000. The CFC is ward A, 4010.
     */

    private static final String CHEQUE_IN_HAND = "450210010";
    private static final String MARKET_BA = "4060";
    private static final String MARKET_FC = "4060420103";
    private static final String MARKET_FA = "55800000000";
    private static final String INTERIM_FA = "00301000000";

    private DemandRepository repo() throws Exception {
        return repoWithFlag(false);
    }

    /** What ReceiptServiceV2 hands over for a receipt taken at the ward A counter. */
    private static FiDimensions wardA(DemandRepository repo) {
        return repo.interimReceiptDimensions("11", "4010");
    }

    private Demand collectionShim(FiDimensions collecting) {
        Demand d = new Demand();
        d.setId("demand-1");
        d.setConsumerCode("5000000284rf");
        d.setBusinessService("TX.Emarket_Rental_Fees");
        d.setFund("11");
        d.setFundCenter(MARKET_FC);
        d.setBusinessArea(MARKET_BA);
        d.setFunctionalArea(MARKET_FA);
        d.setFiReceiptNo("market9152139240");
        d.setPaymentMode("CASH");
        d.setCollectingDimensions(collecting);
        return d;
    }

    private static void assertDims(FiReport r, String fund, String fundCentre, String businessArea,
                                   String functionalArea) {
        String leg = r.getGlCode() + "@" + r.getPostingKey();
        assertEquals(fund, r.getFund(), leg + " fund");
        assertEquals(fundCentre, r.getFundCentre(), leg + " fund centre");
        assertEquals(businessArea, r.getBusinessArea(), leg + " business area");
        assertEquals(functionalArea, r.getFunctionalArea(), leg + " functional area");
    }

    private static void assertMarket(FiReport r) {
        assertDims(r, "11", MARKET_FC, MARKET_BA, MARKET_FA);
    }

    private static boolean isMoneyLeg(FiReport r) {
        return BANK.equals(r.getGlCode()) || CHEQUE_IN_HAND.equals(r.getGlCode());
    }

    @Test
    public void theInterimReceiptRuleIsBusinessAreaPlus130000AndAFixedFunctionalArea() throws Exception {
        DemandRepository repo = repo();

        FiDimensions a = repo.interimReceiptDimensions("11", "4010");
        assertEquals("11", a.getFund());
        assertEquals("4010", a.getBusinessArea());
        assertEquals("4010130000", a.getFundCentre());
        assertEquals("00301000000", a.getFunctionalArea(), "the leading zeros are part of the value");
        assertTrue(a.isComplete());

        assertEquals("4090130000", repo.interimReceiptDimensions(" 11 ", " 4090 ").getFundCentre());

        assertEquals(null, repo.interimReceiptDimensions("11", null), "no business area, no fund centre");
        assertEquals(null, repo.interimReceiptDimensions("11", " "));
        assertEquals(null, repo.interimReceiptDimensions(null, "4010"));
    }

    /** Rule off: every leg keeps the licensee's market, exactly as before the feature. */
    @Test
    public void ruleOffKeepsTheMarketOnEveryLeg() throws Exception {
        List<FiReport> rows = repo().buildCollectionFiReports(
                collectionShim(null), FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO,
                BigDecimal.ZERO, false, 1743465600000L);

        assertEquals(2, rows.size());
        rows.forEach(FourSeriesAndCfcDimensionVerifyTest::assertMarket);
    }

    /** The cash interim receipt posts on the CFC's rule; the receivable stays with the market. */
    @Test
    public void theCashInterimReceiptPostsOnTheCfcWardRule() throws Exception {
        DemandRepository repo = repo();
        List<FiReport> rows = repo.buildCollectionFiReports(
                collectionShim(wardA(repo)), FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO,
                BigDecimal.ZERO, false, 1743465600000L);

        FiReport bank = rowsOn(rows, BANK).get(0);
        assertEquals("40", bank.getPostingKey());
        assertDims(bank, "11", "4010130000", "4010", INTERIM_FA);

        FiReport receivable = rowsOn(rows, RECEIVABLE).get(0);
        assertEquals("50", receivable.getPostingKey());
        assertMarket(receivable);
    }

    /** A cheque receipt's money leg is cheques-in-hand, and it takes the same interim-receipt rule. */
    @Test
    public void theChequeInterimReceiptPostsOnTheSameRule() throws Exception {
        DemandRepository repo = repo();
        Demand d = collectionShim(wardA(repo));
        d.setPaymentMode("CHEQUE");

        List<FiReport> rows = repo.buildCollectionFiReports(d, FiFlow.NON_GST_REGULAR, bd("932"),
                BigDecimal.ZERO, BigDecimal.ZERO, false, 1743465600000L);

        assertTrue(rowsOn(rows, BANK).isEmpty(), "a cheque does not post to the cash interim account");
        assertDims(rowsOn(rows, CHEQUE_IN_HAND).get(0), "11", "4010130000", "4010", INTERIM_FA);
        assertMarket(rowsOn(rows, RECEIVABLE).get(0));
    }

    /**
     * Only the money leg moves. The old rule moved EVERY forward-40 leg but the advance-GST pair, so
     * the net-of-GST shape's CGST/SGST payable debits went to the CFC as well.
     */
    @Test
    public void noOtherForwardDebitLegTakesTheCfcDimensions() throws Exception {
        DemandRepository repo = repo();
        Field gross = DemandRepository.class.getDeclaredField("grossBankOnRegularCollection");
        gross.setAccessible(true);
        gross.setBoolean(repo, false);

        List<FiReport> netOfGst = repo.buildCollectionFiReports(collectionShim(wardA(repo)),
                FiFlow.GST_REGULAR, bd("1452"), bd("110.70"), bd("110.70"), false, 1743465600000L);
        assertEquals(4, netOfGst.size());
        for (String gl : new String[] { "350200421", "350200422" }) {
            FiReport payable = rowsOn(netOfGst, gl).get(0);
            assertEquals("40", payable.getPostingKey());
            assertMarket(payable);
        }

        List<FiReport> advance = repo.buildCollectionFiReports(collectionShim(wardA(repo)),
                FiFlow.GST_ADVANCE, bd("11589.60"), bd("766.80"), bd("766.80"), false, 1743465600000L);
        for (String gl : new String[] { CGST_ADVANCE, SGST_ADVANCE }) {
            FiReport clearing = rowsOn(advance, gl).get(0);
            assertEquals("40", clearing.getPostingKey());
            assertMarket(clearing);
        }
        assertDims(rowsOn(advance, BANK).get(0), "11", "4010130000", "4010", INTERIM_FA);
    }

    /**
     * The posted-leg map a real cancellation reads back. A money leg gets the corrected rule's values
     * (what this code posts). Every other leg gets the market's, or, for a business area other than the
     * market's, what the OLD rule wrote.
     */
    private Map<String, FiDimensions> postedLegs(String... glAtKeyThenBa) throws Exception {
        DemandRepository repo = repo();
        Map<String, FiDimensions> m = new HashMap<>();
        for (int i = 0; i < glAtKeyThenBa.length; i += 2) {
            String[] leg = glAtKeyThenBa[i].split("@");
            String ba = glAtKeyThenBa[i + 1];
            boolean moneyLeg = BANK.equals(leg[0]) || CHEQUE_IN_HAND.equals(leg[0]);
            FiDimensions dims = moneyLeg
                    ? repo.interimReceiptDimensions("11", ba)
                    : new FiDimensions("11", MARKET_BA.equals(ba) ? MARKET_FC : ba + "420101", ba, MARKET_FA);
            m.put(DemandRepository.postedLegKey(leg[0], leg[1]), dims);
        }
        return m;
    }

    private Demand reversalShim(Map<String, FiDimensions> posted, FiDimensions collecting) {
        Demand d = collectionShim(collecting);
        d.setPostedLegDimensions(posted);
        return d;
    }

    /** What getPostedCollectionDimensions returns for a set of stored rows, keyed as it keys them. */
    private static Map<String, FiDimensions> asPosted(List<FiReport> forwardRows) {
        Map<String, FiDimensions> m = new HashMap<>();
        for (FiReport r : forwardRows) {
            m.put(DemandRepository.postedLegKey(r.getGlCode(), r.getPostingKey()),
                    new FiDimensions(r.getFund(), r.getFundCentre(), r.getBusinessArea(), r.getFunctionalArea()));
        }
        return m;
    }

    /**
     * The round trip SAP depends on. Post a receipt at the ward A counter, read its rows back as a
     * cancellation does, reverse it. Every leg of the reversal must carry exactly the original's
     * values with the opposite key, so each account nets to zero per business area AND fund centre.
     */
    @Test
    public void aReversalNetsOffItsCollectionLegForLeg() throws Exception {
        DemandRepository repo = repo();
        for (String mode : new String[] { "CASH", "CHEQUE" }) {
            for (FiFlow flow : FiFlow.values()) {
                Demand forward = collectionShim(wardA(repo));
                forward.setPaymentMode(mode);
                List<FiReport> collection = repo.buildCollectionFiReports(forward, flow, bd("1000"),
                        bd("76.27"), bd("76.27"), false, 1743465600000L);
                if (collection.isEmpty()) {
                    continue;
                }

                Demand back = reversalShim(asPosted(collection), wardA(repo));
                back.setPaymentMode(mode);
                // a stall re-pointed at another market in between must change nothing
                back.setBusinessArea("4130");
                back.setFundCenter("4130420101");
                List<FiReport> reversal = repo.buildCollectionFiReports(back, flow, bd("1000"),
                        bd("76.27"), bd("76.27"), true, 1743465600000L);

                // Dr positive, Cr negative, across BOTH documents: an account only nets to zero if
                // the reversal flips the key AND carries the same four values.
                Map<String, BigDecimal> net = new HashMap<>();
                for (FiReport r : collection) {
                    net.merge(netKey(r), signed(r), BigDecimal::add);
                }
                for (FiReport r : reversal) {
                    net.merge(netKey(r), signed(r), BigDecimal::add);
                }
                String where = mode + " " + flow;
                assertEquals(collection.size(), reversal.size(), where);
                net.forEach((k, v) -> assertEquals(0, BigDecimal.ZERO.compareTo(v),
                        where + " leaves " + v + " standing on " + k));
                reversal.forEach(r -> assertFalse("4130".equals(r.getBusinessArea()), where));
            }
        }
    }

    private static BigDecimal signed(FiReport r) {
        return "40".equals(r.getPostingKey()) ? r.getCollectionAmount() : r.getCollectionAmount().negate();
    }

    /** The account as SAP nets it: GL plus all four dimensions. */
    private static String netKey(FiReport r) {
        return r.getGlCode() + "|" + r.getFund() + "|" + r.getFundCentre() + "|" + r.getBusinessArea()
                + "|" + r.getFunctionalArea();
    }

    /**
     * Review finding: a receipt posted BEFORE the rule was corrected carries 4030420101 / 55800000000
     * on its money leg, and may already be in SAP. Its reversal must carry the same values, or the
     * original stands at one fund centre and the reversal at another and neither clears. The rule
     * being on today must not rebuild it. fix_interim_receipt_dimensions.sql is what corrects it,
     * both rows together.
     */
    @Test
    public void aReversalGivesBackOldValuesSoItNetsAgainstAnUploadedOriginal() throws Exception {
        DemandRepository repo = repo();
        Map<String, FiDimensions> postedBeforeTheFix = new HashMap<>();
        postedBeforeTheFix.put(DemandRepository.postedLegKey(BANK, "40"),
                new FiDimensions("11", "4030420101", "4030", MARKET_FA));
        postedBeforeTheFix.put(DemandRepository.postedLegKey(RECEIVABLE, "50"),
                new FiDimensions("11", MARKET_FC, MARKET_BA, MARKET_FA));

        for (FiDimensions collecting : java.util.Arrays.asList(wardA(repo), null)) {
            List<FiReport> rows = repo.buildCollectionFiReports(reversalShim(postedBeforeTheFix, collecting),
                    FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO, BigDecimal.ZERO,
                    true, 1743465600000L);

            FiReport bank = rowsOn(rows, BANK).get(0);
            assertEquals("50", bank.getPostingKey(), "the money leg flips");
            assertDims(bank, "11", "4030420101", "4030", MARKET_FA);

            FiReport receivable = rowsOn(rows, RECEIVABLE).get(0);
            assertEquals("40", receivable.getPostingKey(), "the receivable flips the other way");
            assertMarket(receivable);
        }
    }

    /**
     * Review finding: a posted money leg with a business area but a blank value elsewhere (a market
     * with no functional_area) must still land its reversal in THAT business area. It is rebuilt by
     * the rule there, and does not jump to the collecting ward.
     */
    @Test
    public void anIncompletePostedMoneyLegIsRebuiltAtItsBookedBusinessArea() throws Exception {
        DemandRepository repo = repo();
        Map<String, FiDimensions> posted = new HashMap<>();
        posted.put(DemandRepository.postedLegKey(BANK, "40"), new FiDimensions("11", MARKET_FC, MARKET_BA, null));
        posted.put(DemandRepository.postedLegKey(RECEIVABLE, "50"), new FiDimensions("11", MARKET_FC, MARKET_BA, null));

        List<FiReport> ruleOn = repo.buildCollectionFiReports(reversalShim(posted, wardA(repo)),
                FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO, BigDecimal.ZERO, true, 1743465600000L);
        assertDims(rowsOn(ruleOn, BANK).get(0), "11", "4060130000", MARKET_BA, INTERIM_FA);
        assertMarket(rowsOn(ruleOn, RECEIVABLE).get(0));

        // no fund on the posted row either: the collecting set supplies it
        posted.put(DemandRepository.postedLegKey(BANK, "40"), new FiDimensions(null, null, MARKET_BA, null));
        List<FiReport> noFund = repo.buildCollectionFiReports(reversalShim(posted, wardA(repo)),
                FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO, BigDecimal.ZERO, true, 1743465600000L);
        assertDims(rowsOn(noFund, BANK).get(0), "11", "4060130000", MARKET_BA, INTERIM_FA);

        // rule off: nothing complete to mirror, so the market, as before
        List<FiReport> ruleOff = repo.buildCollectionFiReports(reversalShim(posted, null),
                FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO, BigDecimal.ZERO, true, 1743465600000L);
        assertMarket(rowsOn(ruleOff, BANK).get(0));
    }

    /**
     * The read-back itself. A row with a business area is kept even when another value is blank; a row
     * with none is dropped; and where two rows share a key, the complete one wins whatever the order.
     */
    @Test
    public void thePostedReadBackKeepsRowsThatCarryABusinessArea() throws Exception {
        String[][] rows = {
            // gl, key, fund, fund_centre, business_area, functional_area
            { BANK, "40", "11", MARKET_FC, MARKET_BA, null },                 // incomplete, kept
            { BANK, "40", "11", "4060130000", MARKET_BA, INTERIM_FA },         // same key, complete: wins
            { RECEIVABLE, "50", "11", MARKET_FC, " ", MARKET_FA },            // blank business area: dropped
            { ADVANCE, "50", "11", "4060130000", MARKET_BA, INTERIM_FA },     // complete, kept
            { CHEQUE_IN_HAND, "40", "11", "4060130000", MARKET_BA, INTERIM_FA },
            { CHEQUE_IN_HAND, "40", "11", null, MARKET_BA, null },            // incomplete AFTER complete: loses
            { "340100300", "50", "11", MARKET_FC, MARKET_BA, null },          // incomplete and alone: kept
        };

        org.springframework.jdbc.core.JdbcTemplate jdbc =
                org.mockito.Mockito.mock(org.springframework.jdbc.core.JdbcTemplate.class);
        org.mockito.Mockito.doAnswer(inv -> {
            org.springframework.jdbc.core.RowCallbackHandler handler = inv.getArgument(2);
            for (String[] row : rows) {
                java.sql.ResultSet rs = org.mockito.Mockito.mock(java.sql.ResultSet.class);
                org.mockito.Mockito.when(rs.getString("gl_code")).thenReturn(row[0]);
                org.mockito.Mockito.when(rs.getString("posting_key")).thenReturn(row[1]);
                org.mockito.Mockito.when(rs.getString("fund")).thenReturn(row[2]);
                org.mockito.Mockito.when(rs.getString("fund_centre")).thenReturn(row[3]);
                org.mockito.Mockito.when(rs.getString("business_area")).thenReturn(row[4]);
                org.mockito.Mockito.when(rs.getString("functional_area")).thenReturn(row[5]);
                handler.processRow(rs);
            }
            return null;
        }).when(jdbc).query(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(Object[].class),
                org.mockito.ArgumentMatchers.any(org.springframework.jdbc.core.RowCallbackHandler.class));

        DemandRepository repo = repo();
        Field f = DemandRepository.class.getDeclaredField("jdbcTemplate");
        f.setAccessible(true);
        f.set(repo, jdbc);

        Map<String, FiDimensions> posted = repo.getPostedCollectionDimensions("MARKET/26-27/000032", "5000007527rf");

        assertEquals(4, posted.size(), "only the blank-business-area row may be dropped: " + posted);
        FiDimensions alone = posted.get(DemandRepository.postedLegKey("340100300", "50"));
        assertNotNull(alone, "a row with a business area must be kept even when incomplete");
        assertFalse(alone.isComplete());
        assertEquals(MARKET_BA, alone.getBusinessArea());
        assertEquals("4060130000", posted.get(DemandRepository.postedLegKey(BANK, "40")).getFundCentre());
        assertTrue(posted.get(DemandRepository.postedLegKey(BANK, "40")).isComplete());
        assertTrue(posted.get(DemandRepository.postedLegKey(CHEQUE_IN_HAND, "40")).isComplete(),
                "a later incomplete row must not overwrite a complete one");
        assertFalse(posted.containsKey(DemandRepository.postedLegKey(RECEIVABLE, "50")));
    }

    /** A receipt with no FI rows to read back takes the payment's collecting ward. */
    @Test
    public void aReversalWithNothingPostedUsesTheCollectingWard() throws Exception {
        DemandRepository repo = repo();
        for (Map<String, FiDimensions> posted : java.util.Arrays.asList(
                (Map<String, FiDimensions>) null, new HashMap<String, FiDimensions>())) {
            List<FiReport> rows = repo.buildCollectionFiReports(reversalShim(posted, wardA(repo)),
                    FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO, BigDecimal.ZERO,
                    true, 1743465600000L);
            assertDims(rowsOn(rows, BANK).get(0), "11", "4010130000", "4010", INTERIM_FA);
            assertMarket(rowsOn(rows, RECEIVABLE).get(0));
        }
    }

    /**
     * Re-pointing a stall at another market between collection and cancellation must not move any
     * leg of the reversal to the new market.
     */
    @Test
    public void aReversalMirrorsEveryOtherLegEvenWhenTheMarketHasMovedSince() throws Exception {
        DemandRepository repo = repo();
        Demand d = reversalShim(postedLegs(BANK + "@40", "4010", RECEIVABLE + "@50", MARKET_BA), wardA(repo));
        // the licence has since been re-pointed at a market in another ward
        d.setBusinessArea("4130");
        d.setFundCenter("4130420101");

        List<FiReport> rows = repo.buildCollectionFiReports(
                d, FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO, BigDecimal.ZERO,
                true, 1743465600000L);

        assertDims(rowsOn(rows, BANK).get(0), "11", "4010130000", "4010", INTERIM_FA);
        assertMarket(rowsOn(rows, RECEIVABLE).get(0));
        assertTrue(rows.stream().noneMatch(r -> "4130".equals(r.getBusinessArea())),
                "no leg may pick up the CURRENT market on a reversal");
    }

    /**
     * A GST_ADVANCE receipt posts THREE forward-40 legs. Each leg must find its own counterpart, not
     * whichever key-40 row the planner returns first.
     */
    @Test
    public void aReversalPicksEachLegsOwnDimensionsNotTheFirstKey40Row() throws Exception {
        DemandRepository repo = repo();
        Demand d = reversalShim(postedLegs(
                BANK + "@40", "4120",            // taken at the K/East counter
                CGST_ADVANCE + "@40", MARKET_BA, // clearing legs stayed with the market
                SGST_ADVANCE + "@40", MARKET_BA,
                ADVANCE + "@50", MARKET_BA,
                "350200421@50", MARKET_BA,
                "350200422@50", MARKET_BA), wardA(repo));

        List<FiReport> rows = repo.buildCollectionFiReports(
                d, FiFlow.GST_ADVANCE, bd("11589.60"), bd("766.80"), bd("766.80"),
                true, 1743465600000L);

        assertDims(rowsOn(rows, BANK).get(0), "11", "4120130000", "4120", INTERIM_FA);
        assertMarket(rowsOn(rows, CGST_ADVANCE).get(0));
        assertMarket(rowsOn(rows, SGST_ADVANCE).get(0));
        assertEquals(0, debitTotal(rows).compareTo(creditTotal(rows)));
    }

    /** A partial set is treated as the rule being off, so nothing moves. */
    @Test
    public void anIncompleteDimensionSetIsIgnored() throws Exception {
        FiDimensions partial = new FiDimensions("11", null, "4010", INTERIM_FA);
        assertFalse(partial.isComplete());

        List<FiReport> rows = repo().buildCollectionFiReports(
                collectionShim(partial), FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO,
                BigDecimal.ZERO, false, 1743465600000L);

        rows.forEach(FourSeriesAndCfcDimensionVerifyTest::assertMarket);
    }

    /**
     * Every flow, both directions, cash and cheque: the voucher balances, every money leg carries the
     * interim-receipt set, and no other leg does. Without the "every money leg" assertion a rule that
     * silently never fired would pass the balance check.
     */
    @Test
    public void everyFlowPutsTheRuleOnTheMoneyLegOnlyAndStillBalances() throws Exception {
        DemandRepository repo = repo();
        Map<String, FiDimensions> everyLegPosted = postedLegs(
                BANK + "@40", "4010", CHEQUE_IN_HAND + "@40", "4010",
                RECEIVABLE + "@50", MARKET_BA, ADVANCE + "@50", MARKET_BA,
                "340100300@50", MARKET_BA, "350200421@40", MARKET_BA, "350200422@40", MARKET_BA,
                "350200421@50", MARKET_BA, "350200422@50", MARKET_BA,
                CGST_ADVANCE + "@40", MARKET_BA, SGST_ADVANCE + "@40", MARKET_BA);

        for (String mode : new String[] { "CASH", "CHEQUE" }) {
            for (FiFlow flow : FiFlow.values()) {
                for (boolean reversal : new boolean[] { false, true }) {
                    Demand shim = reversal ? reversalShim(everyLegPosted, wardA(repo)) : collectionShim(wardA(repo));
                    shim.setPaymentMode(mode);
                    List<FiReport> rows = repo.buildCollectionFiReports(shim,
                            flow, bd("1000"), bd("76.27"), bd("76.27"), reversal, 1743465600000L);
                    assertNotNull(rows);
                    if (rows.isEmpty()) {
                        continue;
                    }
                    String where = mode + " " + flow + " reversal=" + reversal;
                    assertTrue(rows.stream().anyMatch(FourSeriesAndCfcDimensionVerifyTest::isMoneyLeg),
                            where + " posted no money leg; the assertions below would be vacuous");
                    for (FiReport r : rows) {
                        if (isMoneyLeg(r)) {
                            assertDims(r, "11", "4010130000", "4010", INTERIM_FA);
                        } else {
                            assertMarket(r);
                        }
                    }
                    assertEquals(0, debitTotal(rows).compareTo(creditTotal(rows)),
                            where + " is unbalanced: Dr " + debitTotal(rows) + " Cr " + creditTotal(rows));
                }
            }
        }
    }
}
