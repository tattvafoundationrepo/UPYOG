package org.egov.demand.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.egov.demand.model.FiReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * One line per GL on a collection voucher. Modelled on receipt MARKET/26-27/000243 (licence 5000014904,
 * UAT 28-10-2026): seven months of rent posted 21 legs, seven of them 431409938 637.20.
 */
public class CollectionFiRowMergerTest {

    private static final String RECEIPT = "MARKET/26-27/000243";
    private static final Predicate<FiReport> MONEY = r -> "450100100".equals(r.getGlCode()) || "450210010".equals(r.getGlCode());

    private static FiReport leg(String demand, String key, String gl, String amount, String remarks) {
        return FiReport.builder().transactionNumber(demand).docDate(1793205416569L).postingDate(1793205416569L)
                .referenceNo("5000014904rf").documentHeaderText(RECEIPT).postingKey(key).glCode(gl)
                .collectionAmount(new BigDecimal(amount)).fund("11").fundCentre("4080420101")
                .functionalArea("55800000000").businessArea("4080").remarks(remarks).paymentModeDetails("CASH")
                .isNew(Boolean.TRUE).docType("YY").reportType("upmktcoll").build();
    }

    private static FiReport credit(String demand, String gl, String amount) {
        return leg(demand, "50", gl, amount, "Receivable from Mun Mkt");
    }

    /** The 21 legs receipt 000243 posted, in posting order. */
    private static List<FiReport> receipt000243() {
        List<FiReport> rows = new ArrayList<>();
        rows.add(leg("d0", "40", "450100100", "9650.00", "Bank/Interim Receipt"));
        rows.add(credit("lf", "431409940", "900.00"));
        for (int m = 1; m <= 7; m++)
            rows.add(credit("m" + m, "431409938", "637.20"));
        String[] penalties = { "135.00", "270.00", "405.00", "540.00", "540.00", "540.00", "540.00" };
        for (int m = 1; m <= 7; m++)
            rows.add(credit("p" + m, "431409979", penalties[m - 1]));
        for (int m = 1; m <= 3; m++)
            rows.add(credit("g" + m, "431409978", "225.00"));
        rows.add(credit("d0", "431409936", "644.00"));
        rows.add(leg("d0", "50", "350119997", "0.60", "Coin Note"));
        return rows;
    }

    private static BigDecimal sum(List<FiReport> rows, String key) {
        return rows.stream().filter(r -> key.equals(r.getPostingKey())).map(FiReport::getCollectionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** What the open-receivable read sees: each leg expanded back into its parts. */
    private static Map<String, BigDecimal> perDemandAndGl(List<FiReport> rows) {
        Map<String, BigDecimal> out = new HashMap<>();
        for (FiReport r : rows) {
            Map<String, BigDecimal> parts = r.getDemandSplit() != null ? r.getDemandSplit()
                    : java.util.Collections.singletonMap(r.getTransactionNumber(), r.getCollectionAmount());
            parts.forEach((d, a) -> out.merge(d + "|" + r.getGlCode() + "|" + r.getPostingKey() + "|" + r.getRemarks(),
                    a, BigDecimal::add));
        }
        return out;
    }

    @Test
    @DisplayName("Receipt 000243: 21 legs become 7, one per GL, and the voucher still balances")
    void multiMonthReceiptPostsOneLinePerGl() {
        List<FiReport> before = receipt000243();

        List<FiReport> after = CollectionFiRowMerger.merge(before, MONEY);

        assertEquals(7, after.size());
        assertEquals(Arrays.asList("450100100", "431409940", "431409938", "431409979", "431409978", "431409936", "350119997"),
                Arrays.asList(after.stream().map(FiReport::getGlCode).toArray()));
        assertEquals(0, new BigDecimal("4460.40").compareTo(after.get(2).getCollectionAmount()));
        assertEquals(0, new BigDecimal("2970.00").compareTo(after.get(3).getCollectionAmount()));
        assertEquals(0, new BigDecimal("675.00").compareTo(after.get(4).getCollectionAmount()));
        assertEquals(0, sum(after, "40").compareTo(sum(after, "50")));
        assertEquals(0, sum(before, "50").compareTo(sum(after, "50")));
    }

    @Test
    @DisplayName("Every demand's credit per GL is exactly what it was before merging")
    void partsReadBackAsBefore() {
        List<FiReport> before = receipt000243();
        List<FiReport> after = CollectionFiRowMerger.merge(before, MONEY);

        Map<String, BigDecimal> expected = perDemandAndGl(before);
        Map<String, BigDecimal> actual = perDemandAndGl(after);
        assertEquals(expected.keySet(), actual.keySet());
        expected.forEach((k, v) -> assertEquals(0, v.compareTo(actual.get(k)), k));

        // The merged leg keeps the first demand's id; the parts name every month.
        FiReport rent = after.get(2);
        assertEquals("m1", rent.getTransactionNumber());
        assertEquals(7, rent.getDemandSplit().size());
    }

    @Test
    @DisplayName("A leg merged with nothing is the very same row, with no split")
    void singleLegsUntouched() {
        List<FiReport> before = receipt000243();
        List<FiReport> after = CollectionFiRowMerger.merge(before, MONEY);

        assertSame(before.get(0), after.get(0));   // money leg
        assertSame(before.get(1), after.get(1));   // licence fee, one demand
        assertSame(before.get(before.size() - 1), after.get(after.size() - 1)); // coin note
        assertNull(after.get(1).getDemandSplit());
    }

    @Test
    @DisplayName("A voucher with no repeated GL comes back unchanged")
    void nothingToMerge() {
        List<FiReport> before = Arrays.asList(
                leg("a", "40", "450100100", "1000.00", "Bank/Interim Receipt"),
                leg("a", "50", "350410215", "847.46", "Advance"),
                leg("a", "50", "350200421", "76.27", "CGST Payable"),
                leg("a", "50", "350200422", "76.27", "SGST Payable"),
                leg("a", "40", "439300200", "76.27", "Advance CGST"),
                leg("a", "40", "439300201", "76.27", "Advance SGST"));

        List<FiReport> after = CollectionFiRowMerger.merge(before, MONEY);

        assertEquals(before.size(), after.size());
        for (int i = 0; i < before.size(); i++)
            assertSame(before.get(i), after.get(i));
    }

    @Test
    @DisplayName("Anything SAP would see differently keeps legs apart")
    void differingColumnsStayApart() {
        FiReport a = credit("m1", "431409938", "637.20");
        FiReport otherKey = credit("m2", "431409938", "637.20");
        otherKey.setPostingKey("40");
        FiReport otherDate = credit("m3", "431409938", "637.20");
        otherDate.setDocDate(1L);                      // a dishonour charge on its bank-return date
        FiReport otherCentre = credit("m4", "431409938", "637.20");
        otherCentre.setFundCentre("1000130000");
        FiReport otherRemark = credit("m5", "431409938", "637.20");
        otherRemark.setRemarks("TX.STALLAGE");
        FiReport otherLicence = credit("m6", "431409938", "637.20");
        otherLicence.setReferenceNo("5000014905rf");

        List<FiReport> after = CollectionFiRowMerger.merge(
                Arrays.asList(a, otherKey, otherDate, otherCentre, otherRemark, otherLicence), MONEY);

        assertEquals(6, after.size());
    }

    @Test
    @DisplayName("Money legs are never merged: a mixed receipt keeps its two vouchers' bank legs")
    void moneyLegsNeverMerged() {
        List<FiReport> after = CollectionFiRowMerger.merge(Arrays.asList(
                leg("adv", "40", "450100100", "2000.00", "Bank/Interim Receipt"),
                leg("adv", "50", "350410215", "2000.00", "Advance"),
                leg("m1", "40", "450100100", "1274.40", "Bank/Interim Receipt"),
                credit("m1", "431409938", "637.20"),
                credit("m2", "431409938", "637.20")), MONEY);

        assertEquals(4, after.size());
        assertEquals("450100100", after.get(0).getGlCode());
        assertEquals("450100100", after.get(2).getGlCode());
    }

    @Test
    @DisplayName("Legs with no receipt, licence or demand are not merged")
    void unkeyedLegsStayApart() {
        FiReport noReceipt1 = credit("m1", "431409938", "637.20");
        noReceipt1.setDocumentHeaderText("");
        FiReport noReceipt2 = credit("m2", "431409938", "637.20");
        noReceipt2.setDocumentHeaderText("");
        FiReport noDemand1 = credit(null, "431409979", "540.00");
        FiReport noDemand2 = credit(null, "431409979", "540.00");

        assertEquals(4, CollectionFiRowMerger.merge(Arrays.asList(noReceipt1, noReceipt2, noDemand1, noDemand2), MONEY).size());
    }

    @Test
    @DisplayName("Already-merged legs contribute their parts, not their first demand")
    void mergedInputsKeepTheirParts() {
        FiReport merged = credit("m1", "431409938", "1274.40");
        Map<String, BigDecimal> parts = new LinkedHashMap<>();
        parts.put("m1", new BigDecimal("637.20"));
        parts.put("m2", new BigDecimal("637.20"));
        merged.setDemandSplit(parts);

        List<FiReport> after = CollectionFiRowMerger.merge(Arrays.asList(merged, credit("m3", "431409938", "637.20")), MONEY);

        assertEquals(1, after.size());
        assertEquals(0, new BigDecimal("1911.60").compareTo(after.get(0).getCollectionAmount()));
        assertEquals(Arrays.asList("m1", "m2", "m3"), new ArrayList<>(after.get(0).getDemandSplit().keySet()));
        // The input rows are not modified.
        assertEquals(2, merged.getDemandSplit().size());
        assertEquals(0, new BigDecimal("1274.40").compareTo(merged.getCollectionAmount()));
    }

    @Test
    @DisplayName("Two parts of one demand on one GL add up under that demand")
    void sameDemandTwiceAddsUp() {
        List<FiReport> after = CollectionFiRowMerger.merge(Arrays.asList(
                credit("m1", "431409938", "100.00"), credit("m1", "431409938", "37.20")), MONEY);

        assertEquals(1, after.size());
        assertEquals(0, new BigDecimal("137.20").compareTo(after.get(0).getDemandSplit().get("m1")));
    }

    @Test
    @DisplayName("Null and single-row inputs pass straight through")
    void trivialInputs() {
        assertNull(CollectionFiRowMerger.merge(null, MONEY));
        List<FiReport> one = Arrays.asList(credit("m1", "431409938", "637.20"));
        assertSame(one, CollectionFiRowMerger.merge(one, MONEY));
    }
}
