package org.egov.demand.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.egov.demand.repository.DemandRepository;
import org.egov.demand.service.ReceiptServiceV2;
import org.egov.demand.util.Util;
import org.egov.demand.web.contract.BillRequestV2;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * collection-services merges the bank return date into every bill of a dishonoured payment; the
 * consumer replaces bill 0's additionalDetails and must carry the date across, for a dishonour only.
 */
public class BillingServiceConsumerDishonourDateTest {

    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private ReceiptServiceV2 receiptService;

    private BillingServiceConsumer consumer() {
        BillingServiceConsumer consumer = new BillingServiceConsumer();
        Util util = new Util();
        ReflectionTestUtils.setField(util, "mapper", mapper);
        DemandRepository repository = mock(DemandRepository.class);
        when(repository.searchPaymentBackUpdateAudit(any())).thenReturn(null);
        receiptService = mock(ReceiptServiceV2.class);
        ReflectionTestUtils.setField(consumer, "objectMapper", mapper);
        ReflectionTestUtils.setField(consumer, "util", util);
        ReflectionTestUtils.setField(consumer, "demandRepository", repository);
        ReflectionTestUtils.setField(consumer, "receiptServiceV2", receiptService);
        return consumer;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> message(String status, String billZeroDate, String billOneDate) throws Exception {
        String bill0 = billZeroDate == null ? "{\"dishonourReason\":\"x\"}"
                : "{\"dishonourReason\":\"x\",\"dishonourDate\":\"" + billZeroDate + "\"}";
        String bill1 = billOneDate == null ? "null" : "{\"dishonourDate\":\"" + billOneDate + "\"}";
        String statusField = status == null ? "" : "\"paymentStatus\":\"" + status + "\",";
        String json = "{\"RequestInfo\":{},\"Payment\":{\"id\":\"pay-1\",\"transactionNumber\":\"MARKET/26-27/000009\","
                + statusField
                + "\"paymentDetails\":["
                + "{\"totalAmountPaid\":100,\"bill\":{\"id\":\"b0\",\"totalAmount\":100,\"additionalDetails\":" + bill0 + ",\"billDetails\":[]}},"
                + "{\"totalAmountPaid\":50,\"bill\":{\"id\":\"b1\",\"totalAmount\":50,\"additionalDetails\":" + bill1 + ",\"billDetails\":[]}}"
                + "]}}";
        return mapper.readValue(json, Map.class);
    }

    private JsonNode billZeroSent(Map<String, Object> record, boolean cancellation) {
        ReflectionTestUtils.invokeMethod(consumer(), "updateDemandsFromPayment", record, cancellation);
        ArgumentCaptor<BillRequestV2> captor = ArgumentCaptor.forClass(BillRequestV2.class);
        verify(receiptService).updateDemandFromReceipt(captor.capture(), eq(cancellation));
        return captor.getValue().getBills().get(0).getAdditionalDetails();
    }

    @Test
    public void aDishonourCarriesTheDateAlongsideThePaymentKeys() throws Exception {
        JsonNode sent = billZeroSent(message("DISHONOURED", "2026-09-25", "2026-09-25"), true);
        assertEquals("2026-09-25", sent.get("dishonourDate").asText());
        assertEquals("pay-1", sent.get("paymentId").asText());
        assertEquals("MARKET/26-27/000009", sent.get("transactionNumber").asText());
        assertFalse(sent.has("dishonourReason"), "bill 0's other details are still replaced as before");
    }

    @Test
    public void theDateIsFoundOnALaterBillToo() throws Exception {
        JsonNode sent = billZeroSent(message("DISHONOURED", null, "2026-09-24"), true);
        assertEquals("2026-09-24", sent.get("dishonourDate").asText());
    }

    /** Same choice as ChequeDishonourRepository.loadEnteredDishonourDate, so both documents agree. */
    @Test
    public void disagreeingBillsResolveToTheEarliestDate() throws Exception {
        JsonNode sent = billZeroSent(message("DISHONOURED", "2026-09-24", "2026-09-22"), true);
        assertEquals("2026-09-22", sent.get("dishonourDate").asText());
    }

    @Test
    public void aCancellationNeverCarriesADate() throws Exception {
        JsonNode sent = billZeroSent(message("CANCELLED", "2026-09-25", null), true);
        assertFalse(sent.has("dishonourDate"));
    }

    @Test
    public void aForwardCollectionNeverCarriesADate() throws Exception {
        JsonNode sent = billZeroSent(message("DISHONOURED", "2026-09-25", null), false);
        assertFalse(sent.has("dishonourDate"));
    }

    @Test
    public void aMessageWithoutAStatusIsProcessedAsBefore() throws Exception {
        JsonNode sent = billZeroSent(message(null, "2026-09-25", null), true);
        assertFalse(sent.has("dishonourDate"));
        assertEquals("pay-1", sent.get("paymentId").asText());
    }

    @Test
    public void aDishonourWithoutADateIsProcessedAsBefore() throws Exception {
        JsonNode sent = billZeroSent(message("DISHONOURED", null, null), true);
        assertFalse(sent.has("dishonourDate"));
        assertEquals("pay-1", sent.get("paymentId").asText());
    }
}
