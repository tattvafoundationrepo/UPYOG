package org.egov.demand.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.demand.model.Demand;
import org.egov.demand.model.FiFlow;
import org.egov.demand.model.FiReport;
import org.junit.jupiter.api.Test;

/**
 * Coin Note (BMC 26-10-2026): a demand carries its exact amount, a cash receipt is rounded up to the rupee and the
 * paise go to the Coin Note account on the collection voucher. Every flow builds its rows on the amount APPLIED to
 * dues; {@link DemandRepository#addCoinNote} then grows the money leg and adds the credit, once per receipt.
 *
 * Worked case: 1,680.70 due, 1,681 cash received, 0.30 coin.
 */
public class CoinNoteFiTest {

    private static final String BANK = "450100100";
    private static final String COIN = "350119997";
    private static final BigDecimal APPLIED = new BigDecimal("1680.70");
    private static final BigDecimal COIN_AMT = new BigDecimal("0.30");
    private static final Long DATE = 1761436800000L;

    private static Demand receipt() {
        Demand d = new Demand();
        d.setId("demand-1");
        d.setConsumerCode("5000007528rf");
        d.setFiReceiptNo("market123");
        d.setPaymentMode("CASH");
        d.setFund("10");
        d.setFundCenter("1000130000");
        d.setBusinessArea("1000");
        d.setFunctionalArea("00301000000");
        return d;
    }

    private static BigDecimal side(List<FiReport> rows, String key) {
        return rows.stream().filter(r -> key.equals(r.getPostingKey()))
                .map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Every voucher (keyed by its transaction number) balances on its own. */
    private static void assertEachVoucherBalances(List<FiReport> rows) {
        Map<String, List<FiReport>> byVoucher = rows.stream()
                .collect(Collectors.groupingBy(FiReport::getTransactionNumber));
        byVoucher.forEach((v, legs) -> assertEquals(0, side(legs, "40").compareTo(side(legs, "50")),
                "voucher " + v + " must balance: " + legs));
    }

    private static List<FiReport> coinRows(List<FiReport> rows) {
        return rows.stream().filter(r -> DemandRepository.COIN_NOTE_REMARK.equals(r.getRemarks()))
                .collect(Collectors.toList());
    }

    private static FiReport bankLeg(List<FiReport> rows) {
        return rows.stream().filter(r -> BANK.equals(r.getGlCode())).reduce((a, b) -> b).orElse(null);
    }

    @Test
    void cashRegularReceiptPostsTheCoinAndStillBalances() {
        DemandRepository repo = new DemandRepository();
        List<FiReport> rows = repo.buildCollectionFiReports(receipt(), FiFlow.NON_GST_REGULAR, APPLIED,
                BigDecimal.ZERO, BigDecimal.ZERO, false, DATE, null);

        repo.addCoinNote(rows, receipt(), COIN_AMT, false, DATE);

        assertEachVoucherBalances(rows);
        assertEquals(0, new BigDecimal("1681.00").compareTo(bankLeg(rows).getCollectionAmount()),
                "the money leg carries the cash received");
        List<FiReport> coin = coinRows(rows);
        assertEquals(1, coin.size());
        assertEquals(COIN, coin.get(0).getGlCode());
        assertEquals("50", coin.get(0).getPostingKey());
        assertEquals(0, COIN_AMT.compareTo(coin.get(0).getCollectionAmount()));
        // the receivable is credited with what was applied, not with the cash
        assertEquals(0, APPLIED.compareTo(rows.stream().filter(r -> "431409936".equals(r.getGlCode()))
                .map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    @Test
    void everyFlowBalancesWithACoin() {
        for (FiFlow flow : new FiFlow[] { FiFlow.NON_GST_REGULAR, FiFlow.GST_REGULAR, FiFlow.NON_GST_ADVANCE,
                FiFlow.GST_ADVANCE, FiFlow.DEPOSIT }) {
            DemandRepository repo = new DemandRepository();
            List<FiReport> rows = repo.buildCollectionFiReports(receipt(), flow, APPLIED,
                    new BigDecimal("128.21"), new BigDecimal("128.21"), false, DATE, null);
            repo.addCoinNote(rows, receipt(), COIN_AMT, false, DATE);
            assertEachVoucherBalances(rows);
            assertEquals(1, coinRows(rows).size(), "one coin leg for " + flow);
        }
    }

    @Test
    void noCoinLeavesTheRowsExactlyAsToday() {
        for (BigDecimal none : new BigDecimal[] { null, BigDecimal.ZERO }) {
            DemandRepository repo = new DemandRepository();
            List<FiReport> expected = repo.buildCollectionFiReports(receipt(), FiFlow.GST_REGULAR, APPLIED,
                    new BigDecimal("128.21"), new BigDecimal("128.21"), false, DATE, null);
            List<FiReport> actual = repo.buildCollectionFiReports(receipt(), FiFlow.GST_REGULAR, APPLIED,
                    new BigDecimal("128.21"), new BigDecimal("128.21"), false, DATE, null);
            repo.addCoinNote(actual, receipt(), none, false, DATE);
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i).getGlCode(), actual.get(i).getGlCode());
                assertEquals(expected.get(i).getPostingKey(), actual.get(i).getPostingKey());
                assertEquals(0, expected.get(i).getCollectionAmount().compareTo(actual.get(i).getCollectionAmount()));
            }
        }
    }

    @Test
    void mixedReceiptGetsOneCoinOnTheRegularVoucher() {
        DemandRepository repo = new DemandRepository();
        Demand advanceVoucher = receipt();
        advanceVoucher.setId("advance-vessel");
        // The split builds the advance voucher, then the regular one (ReceiptServiceV2.buildCollectionRows).
        List<FiReport> rows = new ArrayList<>(repo.buildCollectionFiReports(advanceVoucher, FiFlow.NON_GST_ADVANCE,
                new BigDecimal("3000.00"), BigDecimal.ZERO, BigDecimal.ZERO, false, DATE, null));
        rows.addAll(repo.buildCollectionFiReports(receipt(), FiFlow.NON_GST_REGULAR, APPLIED,
                BigDecimal.ZERO, BigDecimal.ZERO, false, DATE, null));

        repo.addCoinNote(rows, receipt(), COIN_AMT, false, DATE);

        assertEachVoucherBalances(rows);
        List<FiReport> coin = coinRows(rows);
        assertEquals(1, coin.size(), "the coin is posted once per receipt");
        assertEquals("demand-1", coin.get(0).getTransactionNumber(), "on the regular voucher");
        assertEquals(0, new BigDecimal("3000.00").compareTo(rows.stream()
                .filter(r -> BANK.equals(r.getGlCode()) && "advance-vessel".equals(r.getTransactionNumber()))
                .findFirst().get().getCollectionAmount()), "the advance voucher's money leg is untouched");
    }

    @Test
    void rebuiltReversalGivesTheCoinBack() {
        DemandRepository repo = new DemandRepository();
        List<FiReport> rows = repo.buildCollectionFiReports(receipt(), FiFlow.NON_GST_REGULAR, APPLIED,
                BigDecimal.ZERO, BigDecimal.ZERO, true, DATE, null);

        repo.addCoinNote(rows, receipt(), COIN_AMT, true, DATE);

        assertEachVoucherBalances(rows);
        FiReport coin = coinRows(rows).get(0);
        assertEquals("40", coin.getPostingKey(), "the Coin Note credit is debited back");
        assertEquals("50", bankLeg(rows).getPostingKey());
        assertEquals(0, new BigDecimal("1681.00").compareTo(bankLeg(rows).getCollectionAmount()),
                "the full cash received goes back");
    }

    @Test
    void withoutAMoneyLegNothingIsPosted() {
        DemandRepository repo = new DemandRepository();
        List<FiReport> rows = new ArrayList<>();
        repo.addCoinNote(rows, receipt(), COIN_AMT, false, DATE);
        assertTrue(rows.isEmpty(), "a lone credit would unbalance the voucher");
        repo.addCoinNote(null, receipt(), COIN_AMT, false, DATE); // must not throw
    }
}
