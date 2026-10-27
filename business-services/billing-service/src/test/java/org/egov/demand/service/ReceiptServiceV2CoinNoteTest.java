package org.egov.demand.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.egov.demand.model.GstAdvanceMap;
import org.egov.demand.model.PaymentMarketInfo;
import org.junit.jupiter.api.Test;

/**
 * Reading the Coin Note emarket-v1 writes on a cash receipt rounded up to the rupee. The payment total stays the
 * cash received; the coin is carried separately so the FI flows can book what was applied.
 */
public class ReceiptServiceV2CoinNoteTest {

    private static GstAdvanceMap read(String additionalDetails) {
        PaymentMarketInfo info = new PaymentMarketInfo();
        info.setAdditionalDetails(additionalDetails);
        info.setTotalAmountPaid(new BigDecimal("1681.00"));
        info.setTotalDue(new BigDecimal("1680.70"));
        return new ReceiptServiceV2().extractGstAdvanceFromAdditionalDetails(info);
    }

    @Test
    void readsTheCoinAndKeepsTheCashReceivedAsTheTotal() {
        GstAdvanceMap m = read("{\"collectingWardTenant\":\"mh.mumbai.zone1.warda\",\"coinNoteAdjustment\":0.30}");
        assertEquals(0, new BigDecimal("0.30").compareTo(m.getCoinNoteAdjustment()));
        assertEquals(0, new BigDecimal("1681.00").compareTo(m.getTotalAmountPaid()));
    }

    @Test
    void aPaymentWithoutTheKeyIsReadAsBefore() {
        GstAdvanceMap m = read("{\"collectingWardTenant\":\"mh.mumbai.zone1.warda\"}");
        assertNull(m.getCoinNoteAdjustment());
        assertEquals(0, new BigDecimal("1681.00").compareTo(m.getTotalAmountPaid()));
    }

    /** verifiedCoinNote with bill 0 carrying paymentAppliedTotal as the consumer writes it (text). */
    private static java.math.BigDecimal verified(String coinJson, String received, String applied) {
        PaymentMarketInfo info = new PaymentMarketInfo();
        info.setAdditionalDetails(coinJson);
        info.setTotalAmountPaid(new BigDecimal(received));
        ReceiptServiceV2 s = new ReceiptServiceV2();
        org.egov.demand.util.Util util = new org.egov.demand.util.Util();
        org.springframework.test.util.ReflectionTestUtils.setField(util, "mapper", new com.fasterxml.jackson.databind.ObjectMapper());
        org.springframework.test.util.ReflectionTestUtils.setField(s, "util", util);
        org.egov.demand.model.BillV2 bill = new org.egov.demand.model.BillV2();
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        if (applied != null)
            details.put(org.egov.demand.util.Constants.PAYMENT_APPLIED_TOTAL_KEY, applied);
        bill.setAdditionalDetails(new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(details));
        return s.verifiedCoinNote(s.extractGstAdvanceFromAdditionalDetails(info),
                java.util.Collections.singletonList(bill));
    }

    @Test
    void aCoinIsPostedOnlyWhenItIsCashReceivedLessApplied() {
        assertEquals(0, new BigDecimal("0.30").compareTo(verified("{\"coinNoteAdjustment\":0.30}", "1681.00", "1680.70")));
        // forged / inconsistent: ignored, so the receipt is booked as before
        assertEquals(0, BigDecimal.ZERO.compareTo(verified("{\"coinNoteAdjustment\":0.90}", "1681.00", "1680.70")));
        assertEquals(0, BigDecimal.ZERO.compareTo(verified("{\"coinNoteAdjustment\":0.30}", "1681.00", "1681.00")));
        // nothing to check against, or garbage: ignored
        assertEquals(0, BigDecimal.ZERO.compareTo(verified("{\"coinNoteAdjustment\":0.30}", "1681.00", null)));
        assertEquals(0, BigDecimal.ZERO.compareTo(verified("{\"coinNoteAdjustment\":0.30}", "1681.00", "abc")));
        // no coin at all: zero, without looking
        assertEquals(0, BigDecimal.ZERO.compareTo(verified("{}", "1681.00", "1681.00")));
        assertEquals(0, BigDecimal.ZERO.compareTo(new ReceiptServiceV2().verifiedCoinNote(null, null)));
    }

    @Test
    void anythingOutsideZeroToOneIsIgnored() {
        for (String bad : new String[] { "0", "-0.20", "1", "1.50", "\"0.30\"", "null" }) {
            assertNull(read("{\"coinNoteAdjustment\":" + bad + "}").getCoinNoteAdjustment(), "value " + bad);
        }
    }
}
