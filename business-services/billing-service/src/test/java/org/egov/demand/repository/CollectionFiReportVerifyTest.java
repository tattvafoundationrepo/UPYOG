package org.egov.demand.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.demand.model.Demand;
import org.egov.demand.model.FiFlow;
import org.egov.demand.model.FiReport;
import org.junit.jupiter.api.Test;

/**
 * Temporary verification of the per-flow collection FI rows. Pure logic — no
 * Spring context / DB needed (buildCollectionFiReports only reads demand getters).
 */
public class CollectionFiReportVerifyTest {

    private final DemandRepository repo = new DemandRepository();

    private Demand demand() {
        return Demand.builder()
                .id("demand-1")
                .consumerCode("5000016412")
                .taxPeriodFrom(1L)
                .fund("11")
                .fundCenter("4090420103")
                .businessArea("4090")
                .functionalArea("55800000000")
                .fiReceiptNo("2025ACR05877043")
                .paymentMode("CASH")
                .build();
    }

    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    private String key(FiReport r) {
        return r.getPostingKey() + "|" + r.getGlCode() + "|" + r.getCollectionAmount().stripTrailingZeros().toPlainString();
    }

    private List<String> keys(List<FiReport> rows) {
        return rows.stream().map(this::key).collect(Collectors.toList());
    }

    private void assertBalanced(List<FiReport> rows) {
        BigDecimal dr = rows.stream().filter(r -> "40".equals(r.getPostingKey()))
                .map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cr = rows.stream().filter(r -> "50".equals(r.getPostingKey()))
                .map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, dr.compareTo(cr), "Dr " + dr + " != Cr " + cr);
    }

    @Test
    void nonGstRegular() {
        List<FiReport> r = repo.buildCollectionFiReports(demand(), FiFlow.NON_GST_REGULAR, bd("200"), bd("0"), bd("0"), false);
        assertEquals(2, r.size());
        assertTrue(keys(r).containsAll(List.of("50|431409936|200", "40|450100100|200")), keys(r).toString());
        assertBalanced(r);
    }

    @Test
    void gstRegular() {
        List<FiReport> r = repo.buildCollectionFiReports(demand(), FiFlow.GST_REGULAR, bd("1180"), bd("90"), bd("90"), false);
        assertEquals(4, r.size());
        assertTrue(keys(r).containsAll(List.of(
                "40|450100100|1000", "50|431409936|1180", "40|350200421|90", "40|350200422|90")), keys(r).toString());
        assertBalanced(r);
    }

    @Test
    void nonGstAdvance() {
        List<FiReport> r = repo.buildCollectionFiReports(demand(), FiFlow.NON_GST_ADVANCE, bd("1100"), bd("0"), bd("0"), false);
        assertEquals(2, r.size());
        assertTrue(keys(r).containsAll(List.of("50|350410215|1100", "40|450100100|1100")), keys(r).toString());
        assertBalanced(r);
    }

    @Test
    void gstAdvance() {
        List<FiReport> r = repo.buildCollectionFiReports(demand(), FiFlow.GST_ADVANCE, bd("1180"), bd("90"), bd("90"), false);
        assertEquals(6, r.size());
        assertTrue(keys(r).containsAll(List.of(
                "40|450100100|1180", "50|350410215|1180",
                "50|350200421|90", "50|350200422|90",
                "40|439300200|90", "40|439300201|90")), keys(r).toString());
        assertBalanced(r);
    }

    @Test
    void deposit() {
        List<FiReport> r = repo.buildCollectionFiReports(demand(), FiFlow.DEPOSIT, bd("1100"), bd("0"), bd("0"), false);
        assertEquals(2, r.size());
        assertTrue(keys(r).containsAll(List.of("50|340100300|1100", "40|450100100|1100")), keys(r).toString());
        assertBalanced(r);
    }

    @Test
    void gstAdvanceReversalSwapsPostingKeys() {
        List<FiReport> r = repo.buildCollectionFiReports(demand(), FiFlow.GST_ADVANCE, bd("1180"), bd("90"), bd("90"), true);
        assertEquals(6, r.size());
        // every 40<->50 swapped vs the forward case
        assertTrue(keys(r).containsAll(List.of(
                "50|450100100|1180", "40|350410215|1180",
                "40|350200421|90", "40|350200422|90",
                "50|439300200|90", "50|439300201|90")), keys(r).toString());
        assertBalanced(r);
    }
}
