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
 *   <li>the collecting-CFC dimensions on a collection voucher's debit legs, and their mirror
 *       when those legs flip to posting key 50 in a reversal.</li>
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

    // -------------------------------------------------- Part 2: collecting-CFC dimensions

    private static final FiDimensions CFC_WARD_A =
            new FiDimensions("11", "4010420101", "4010", "55800000000");

    private Demand collectionShim(FiDimensions collecting) {
        Demand d = new Demand();
        d.setId("demand-1");
        d.setConsumerCode("5000000284rf");
        d.setBusinessService("TX.Emarket_Rental_Fees");
        d.setFund("11");
        d.setFundCenter("4060420103");
        d.setBusinessArea("4060");
        d.setFunctionalArea("55800000000");
        d.setFiReceiptNo("market9152139240");
        d.setPaymentMode("CASH");
        d.setCollectingDimensions(collecting);
        return d;
    }

    /** With no collecting ward established, every leg keeps the licensee's market. */
    @Test
    public void noCollectingWardKeepsTodaysDimensions() throws Exception {
        List<FiReport> rows = repoWithFlag(false).buildCollectionFiReports(
                collectionShim(null), FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO,
                BigDecimal.ZERO, false, 1743465600000L);

        assertEquals(2, rows.size());
        rows.forEach(r -> {
            assertEquals("4060", r.getBusinessArea());
            assertEquals("4060420103", r.getFundCentre());
        });
    }

    /** The bank leg follows the CFC; the receivable stays with the market so it still clears. */
    @Test
    public void onlyTheDebitLegMovesToTheCollectingWard() throws Exception {
        List<FiReport> rows = repoWithFlag(false).buildCollectionFiReports(
                collectionShim(CFC_WARD_A), FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO,
                BigDecimal.ZERO, false, 1743465600000L);

        FiReport bank = rowsOn(rows, BANK).get(0);
        FiReport receivable = rowsOn(rows, RECEIVABLE).get(0);

        assertEquals("40", bank.getPostingKey());
        assertEquals("4010", bank.getBusinessArea());
        assertEquals("4010420101", bank.getFundCentre());

        assertEquals("50", receivable.getPostingKey());
        assertEquals("4060", receivable.getBusinessArea(),
                "the receivable must stay with the market or it can never clear against the demand");
    }

    /** The posted-leg map a real cancellation reads back, keyed glCode@forwardPostingKey. */
    private static Map<String, FiDimensions> postedLegs(String... glAtKeyThenBa) {
        Map<String, FiDimensions> m = new HashMap<>();
        for (int i = 0; i < glAtKeyThenBa.length; i += 2) {
            String[] leg = glAtKeyThenBa[i].split("@");
            String ba = glAtKeyThenBa[i + 1];
            m.put(DemandRepository.postedLegKey(leg[0], leg[1]),
                    new FiDimensions("11", ba + "420101", ba, "55800000000"));
        }
        return m;
    }

    private Demand reversalShim(Map<String, FiDimensions> posted) {
        Demand d = collectionShim(null);
        d.setPostedLegDimensions(posted);
        return d;
    }

    /**
     * The rule that "40 entries become 50 entries". In a reversal the bank leg flips to posting
     * key 50 and must KEEP the ward it was taken at, while the receivable — now a debit — must
     * keep the market's.
     */
    @Test
    public void theSameLegsKeepTheWardWhenTheyFlipInAReversal() throws Exception {
        List<FiReport> rows = repoWithFlag(false).buildCollectionFiReports(
                reversalShim(postedLegs(BANK + "@40", "4010", RECEIVABLE + "@50", "4060")),
                FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO, BigDecimal.ZERO,
                true, 1743465600000L);

        FiReport bank = rowsOn(rows, BANK).get(0);
        FiReport receivable = rowsOn(rows, RECEIVABLE).get(0);

        assertEquals("50", bank.getPostingKey(), "the bank leg flips");
        assertEquals("4010", bank.getBusinessArea(), "and keeps the ward it was taken at");

        assertEquals("40", receivable.getPostingKey(), "the receivable flips the other way");
        assertEquals("4060", receivable.getBusinessArea(),
                "a posting-key-40 rule would wrongly move the receivable here");
    }

    /**
     * The defect this replaced: applying one dimension set to the key-40 legs only, while every
     * other leg took the licensee's CURRENT market. Re-pointing a stall at another market between
     * collection and cancellation then split the compensating document across two business areas.
     */
    @Test
    public void aReversalMirrorsEveryLegEvenWhenTheMarketHasMovedSince() throws Exception {
        Demand d = reversalShim(postedLegs(BANK + "@40", "4010", RECEIVABLE + "@50", "4060"));
        // the licence has since been re-pointed at a market in another ward
        d.setBusinessArea("4130");
        d.setFundCenter("4130420101");

        List<FiReport> rows = repoWithFlag(false).buildCollectionFiReports(
                d, FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO, BigDecimal.ZERO,
                true, 1743465600000L);

        assertEquals("4010", rowsOn(rows, BANK).get(0).getBusinessArea());
        assertEquals("4060", rowsOn(rows, RECEIVABLE).get(0).getBusinessArea(),
                "the receivable must mirror what was posted, not today's market");
        assertTrue(rows.stream().noneMatch(r -> "4130".equals(r.getBusinessArea())),
                "no leg may pick up the CURRENT market on a reversal");
        assertEquals(new java.util.TreeSet<>(java.util.Arrays.asList("4010", "4060")),
                rows.stream().map(FiReport::getBusinessArea)
                        .collect(Collectors.toCollection(java.util.TreeSet::new)),
                "the reversal spans exactly the two business areas the original posted to");
    }

    /**
     * A GST_ADVANCE receipt posts THREE forward-40 legs and two of them are dimension-excluded.
     * A single-row read-back picked between them by an index-decided tiebreak on an identical
     * created_at; a per-leg map cannot.
     */
    @Test
    public void aReversalPicksEachLegsOwnDimensionsNotTheFirstKey40Row() throws Exception {
        Demand d = reversalShim(postedLegs(
                BANK + "@40", "4120",            // taken at the K/East counter
                CGST_ADVANCE + "@40", "4060",    // clearing legs stayed with the market
                SGST_ADVANCE + "@40", "4060",
                ADVANCE + "@50", "4060",
                "350200421@50", "4060",
                "350200422@50", "4060"));

        List<FiReport> rows = repoWithFlag(false).buildCollectionFiReports(
                d, FiFlow.GST_ADVANCE, bd("11589.60"), bd("766.80"), bd("766.80"),
                true, 1743465600000L);

        assertEquals("4120", rowsOn(rows, BANK).get(0).getBusinessArea(),
                "the money leg must come back at the counter that took it");
        assertEquals("4060", rowsOn(rows, CGST_ADVANCE).get(0).getBusinessArea());
        assertEquals("4060", rowsOn(rows, SGST_ADVANCE).get(0).getBusinessArea());
        assertEquals(0, debitTotal(rows).compareTo(creditTotal(rows)));
    }

    /** A receipt posted before any of this existed has no posted map: every leg keeps the market. */
    @Test
    public void aReversalWithNothingPostedKeepsTodaysDimensions() throws Exception {
        for (Map<String, FiDimensions> posted : java.util.Arrays.asList(
                (Map<String, FiDimensions>) null, new HashMap<String, FiDimensions>())) {
            List<FiReport> rows = repoWithFlag(false).buildCollectionFiReports(
                    reversalShim(posted), FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO,
                    BigDecimal.ZERO, true, 1743465600000L);
            rows.forEach(r -> assertEquals("4060", r.getBusinessArea()));
        }
    }

    /**
     * The advance-GST legs are forward-40 but clear against a demand-side row that keeps the
     * market's dimensions. Moving them would split the clearing pair and make GSTR-1 table 11A
     * disagree with 11B for the same advance.
     */
    @Test
    public void theAdvanceGstClearingLegsAreExcluded() throws Exception {
        List<FiReport> rows = repoWithFlag(false).buildCollectionFiReports(
                collectionShim(CFC_WARD_A), FiFlow.GST_ADVANCE, bd("11589.60"), bd("766.80"),
                bd("766.80"), false, 1743465600000L);

        for (String gl : new String[] { CGST_ADVANCE, SGST_ADVANCE }) {
            List<FiReport> legs = rowsOn(rows, gl);
            assertEquals(1, legs.size());
            assertEquals("40", legs.get(0).getPostingKey());
            assertEquals("4060", legs.get(0).getBusinessArea(),
                    gl + " must keep the market's dimensions to stay clearable");
        }
        // the bank leg of the same voucher still moves
        assertEquals("4010", rowsOn(rows, BANK).get(0).getBusinessArea());
        assertEquals(0, debitTotal(rows).compareTo(creditTotal(rows)));
    }

    /** A ward seeded '0' in the master resolves to no dimensions, so nothing moves. */
    @Test
    public void anIncompleteDimensionSetIsIgnored() throws Exception {
        FiDimensions partial = new FiDimensions("11", null, "4010", "55800000000");
        assertFalse(partial.isComplete());

        List<FiReport> rows = repoWithFlag(false).buildCollectionFiReports(
                collectionShim(partial), FiFlow.NON_GST_REGULAR, bd("838"), BigDecimal.ZERO,
                BigDecimal.ZERO, false, 1743465600000L);

        rows.forEach(r -> assertEquals("4060", r.getBusinessArea(),
                "a partial dimension set must be treated as none at all"));
    }

    /**
     * Every flow still balances with a ward applied, in BOTH directions.
     *
     * <p>The reversal pass must be given a POSTED-LEG map, not {@code collectingDimensions}:
     * resolveLegDimensions ignores the latter when reversing, so a shim carrying only that field
     * would silently exercise no ward at all and the assertion would go vacuous for the reversal
     * half of every flow. The map below covers every GL any flow can post at either forward key.
     */
    @Test
    public void everyFlowStillBalancesWithTheWardApplied() throws Exception {
        DemandRepository repo = repoWithFlag(false);
        Map<String, FiDimensions> everyLegAtWardA = postedLegs(
                BANK + "@40", "4010", "450210010@40", "4010",
                RECEIVABLE + "@50", "4010", ADVANCE + "@50", "4010",
                "340100300@50", "4010", "350200421@40", "4010", "350200422@40", "4010",
                "350200421@50", "4010", "350200422@50", "4010",
                CGST_ADVANCE + "@40", "4010", SGST_ADVANCE + "@40", "4010");

        for (FiFlow flow : FiFlow.values()) {
            for (boolean reversal : new boolean[] { false, true }) {
                Demand shim = reversal ? reversalShim(everyLegAtWardA) : collectionShim(CFC_WARD_A);
                List<FiReport> rows = repo.buildCollectionFiReports(shim,
                        flow, bd("1000"), bd("76.27"), bd("76.27"), reversal, 1743465600000L);
                if (rows != null && !rows.isEmpty()) {
                    assertTrue(rows.stream().anyMatch(r -> "4010".equals(r.getBusinessArea())),
                            flow + " reversal=" + reversal + " applied no ward to any leg — the "
                                    + "balance assertion below would be vacuous");
                }
                assertNotNull(rows);
                if (rows.isEmpty()) {
                    continue;
                }
                assertEquals(0, debitTotal(rows).compareTo(creditTotal(rows)),
                        flow + " reversal=" + reversal + " is unbalanced: Dr " + debitTotal(rows)
                                + " Cr " + creditTotal(rows));
            }
        }
    }
}
