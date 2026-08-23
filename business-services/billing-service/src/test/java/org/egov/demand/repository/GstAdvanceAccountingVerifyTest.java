package org.egov.demand.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Pure-logic verification of the GST demand-against-advance accounting:
 *
 * <ul>
 *   <li>the netting amount is the GST actually settled from advance, per component,
 *       summed over every head — full, partial and multi-head demands;</li>
 *   <li>advance GST at receipt scales by the month count, falling back to x1 when the
 *       payment JSON does not carry one (licence advances, legacy payments).</li>
 * </ul>
 *
 * Worked case: licence 5000000284 — monthly rent 710, CGST/SGST 63.90 each, 12-month
 * advance 766.80 per component (63.90 x 12).
 */
public class GstAdvanceAccountingVerifyTest {

    private final DemandRepository repo = new DemandRepository();

    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    private static DemandDetail detail(String head, String tax, String collected) {
        return DemandDetail.builder()
                .taxHeadMasterCode(head)
                .taxAmount(bd(tax))
                .collectionAmount(bd(collected))
                .build();
    }

    private static Demand rentalDemand(DemandDetail... details) {
        Demand d = Demand.builder()
                .id("demand-1")
                .consumerCode("5000000284")
                .businessService("TX.Emarket_Rental_Fees")
                .build();
        d.setDemandDetails(new java.util.ArrayList<>(java.util.List.of(details)));
        return d;
    }

    /** gstSettledFromAdvance is private; the rule it implements is the contract under test. */
    private BigDecimal settled(Demand demand, String component) throws Exception {
        Method m = DemandRepository.class.getDeclaredMethod("gstSettledFromAdvance", Demand.class, String.class);
        m.setAccessible(true);
        return (BigDecimal) m.invoke(repo, demand, component);
    }

    // ---------------------------------------------------------------- netting rule

    @Test
    void fullySettledDemandNetsTheWholeGst() throws Exception {
        Demand d = rentalDemand(
                detail("STALLAGE", "710", "710"),
                detail("CGST", "63.90", "63.90"),
                detail("SGST", "63.90", "63.90"),
                detail("GST_CA", "0.20", "0.20"));
        assertEquals(0, settled(d, "CGST").compareTo(bd("63.90")));
        assertEquals(0, settled(d, "SGST").compareTo(bd("63.90")));
    }

    @Test
    void partiallySettledDemandNetsOnlyTheCollectedPortion() throws Exception {
        // The old proxy (taxAmount == collectionAmount) netted NOTHING here.
        Demand d = rentalDemand(
                detail("STALLAGE", "710", "300"),
                detail("CGST", "63.90", "27.00"),
                detail("SGST", "63.90", "27.00"));
        assertEquals(0, settled(d, "CGST").compareTo(bd("27.00")));
        assertEquals(0, settled(d, "SGST").compareTo(bd("27.00")));
    }

    @Test
    void multiHeadDemandSumsEveryGstHead() throws Exception {
        // The old proxy took get(0) for all four legs — the godown head was dropped.
        Demand d = rentalDemand(
                detail("STALLAGE", "710", "710"),
                detail("CGST", "63.90", "63.90"),
                detail("SGST", "63.90", "63.90"),
                detail("GODOWN_CGST", "20.00", "20.00"),
                detail("GODOWN_SGST", "20.00", "20.00"));
        assertEquals(0, settled(d, "CGST").compareTo(bd("83.90")));
        assertEquals(0, settled(d, "SGST").compareTo(bd("83.90")));
    }

    @Test
    void gstCaRoundingHeadIsNeverNetted() throws Exception {
        Demand d = rentalDemand(detail("GST_CA", "0.20", "0.20"));
        assertEquals(0, settled(d, "CGST").compareTo(BigDecimal.ZERO));
        assertEquals(0, settled(d, "SGST").compareTo(BigDecimal.ZERO));
    }

    @Test
    void uncollectedGstNetsNothing() throws Exception {
        // Demand raised, nothing settled from advance yet.
        Demand d = rentalDemand(
                detail("STALLAGE", "710", "0"),
                detail("CGST", "63.90", "0"),
                detail("SGST", "63.90", "0"));
        assertEquals(0, settled(d, "CGST").compareTo(BigDecimal.ZERO));
    }

    @Test
    void nullAmountsAreTreatedAsZero() throws Exception {
        Demand d = rentalDemand(DemandDetail.builder().taxHeadMasterCode("CGST").build());
        assertEquals(0, settled(d, "CGST").compareTo(BigDecimal.ZERO));
    }

    @Test
    void twelveMonthlyReleasesExactlyConsumeTheAdvance() throws Exception {
        // The reconciliation the Tax Paid sheet depends on: 12 x 63.90 == 766.80,
        // so the GST advance closes to zero with no residue.
        Demand monthly = rentalDemand(
                detail("STALLAGE", "710", "710"),
                detail("CGST", "63.90", "63.90"),
                detail("SGST", "63.90", "63.90"));
        BigDecimal released = BigDecimal.ZERO;
        for (int m = 0; m < 12; m++)
            released = released.add(settled(monthly, "CGST"));
        assertEquals(0, released.compareTo(bd("766.80")));
    }

    // -------------------------------------------------- advance GST x month count

    private BigDecimal monthCount(ObjectNode root) throws Exception {
        Method m = Class.forName("org.egov.demand.service.ReceiptServiceV2")
                .getDeclaredMethod("advanceMonthCount", com.fasterxml.jackson.databind.JsonNode.class);
        m.setAccessible(true);
        return (BigDecimal) m.invoke(null, root);
    }

    @Test
    void advanceGstScalesByMonthCount() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("advanceMonthCount", 12);
        assertEquals(0, monthCount(root).compareTo(bd("12")));
        // 63.90 per month x 12 = 766.80 — the AT sheet figure.
        assertEquals(0, bd("63.90").multiply(monthCount(root)).compareTo(bd("766.80")));

        root.put("advanceMonthCount", 6);
        assertEquals(0, bd("63.90").multiply(monthCount(root)).compareTo(bd("383.40")));
    }

    @Test
    void missingOrInvalidMonthCountFallsBackToOne() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Absent (licence advance / legacy payment JSON) -> x1, today's behaviour.
        assertEquals(0, monthCount(mapper.createObjectNode()).compareTo(BigDecimal.ONE));

        ObjectNode zero = mapper.createObjectNode();
        zero.put("advanceMonthCount", 0);
        assertEquals(0, monthCount(zero).compareTo(BigDecimal.ONE));

        ObjectNode nullNode = mapper.createObjectNode();
        nullNode.putNull("advanceMonthCount");
        assertEquals(0, monthCount(nullNode).compareTo(BigDecimal.ONE));
    }

    // ------------------------------------------------------------- assignment key

    @Test
    void nettingDetailCarriesGlAndAssignment() throws Exception {
        Demand d = rentalDemand(detail("STALLAGE", "710", "710"));
        int ownDetails = d.getDemandDetails().size();
        java.util.List<DemandDetail> netting = new java.util.ArrayList<>();
        Method m = DemandRepository.class.getDeclaredMethod("addNettingDetail",
                java.util.List.class, Demand.class, String.class, String.class, BigDecimal.class, String.class);
        m.setAccessible(true);

        m.invoke(repo, netting, d, "CSP40", "350200421", bd("63.90"), "market9152139233");
        assertEquals(ownDetails, d.getDemandDetails().size(),
                "the synthetic heads must never be appended to the caller's Demand: those same objects "
                + "are returned from /demand/_create and pushed to the demand-index topic");
        DemandDetail added = netting.get(netting.size() - 1);
        assertEquals("CSP40", added.getTaxHeadMasterCode());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> extra = (java.util.Map<String, Object>) added.getAdditionalDetails();
        assertEquals("350200421", extra.get("glcode"));
        assertEquals("market9152139233", extra.get("assignment"));

        // Zero and null amounts add nothing — a demand with nothing to net emits no legs.
        int before = netting.size();
        m.invoke(repo, netting, d, "SSP40", "350200422", BigDecimal.ZERO, "x");
        m.invoke(repo, netting, d, "SSP40", "350200422", null, "x");
        assertEquals(before, netting.size());

        // Without an advance document the leg still posts, just with no clearing key.
        m.invoke(repo, netting, d, "CSA50", "439300200", bd("63.90"), null);
        DemandDetail noKey = netting.get(netting.size() - 1);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> extra2 = (java.util.Map<String, Object>) noKey.getAdditionalDetails();
        assertNull(extra2.get("assignment"));
        assertTrue(extra2.containsKey("glcode"));
    }
}
