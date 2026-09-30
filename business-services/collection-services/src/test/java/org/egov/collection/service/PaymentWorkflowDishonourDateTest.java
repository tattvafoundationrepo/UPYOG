package org.egov.collection.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.egov.collection.config.ApplicationProperties;
import org.egov.collection.model.AuditDetails;
import org.egov.collection.model.Payment;
import org.egov.collection.model.PaymentDetail;
import org.egov.collection.model.PaymentSearchCriteria;
import org.egov.collection.model.enums.InstrumentStatusEnum;
import org.egov.collection.model.enums.PaymentModeEnum;
import org.egov.collection.model.enums.PaymentStatusEnum;
import org.egov.collection.producer.CollectionProducer;
import org.egov.collection.repository.PaymentRepository;
import org.egov.collection.util.PaymentWorkflowValidator;
import org.egov.collection.web.contract.Bill;
import org.egov.collection.web.contract.PaymentWorkflow;
import org.egov.collection.web.contract.PaymentWorkflowRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** The DISHONOUR workflow with and without the bank return date the eMarket screen sends. */
class PaymentWorkflowDishonourDateTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final String TODAY = LocalDate.now(IST).toString();
    private static final String TOMORROW = LocalDate.now(IST).plusDays(1).toString();
    private static final long TEN_DAYS_AGO = LocalDate.now(IST).minusDays(10).atTime(11, 0).atZone(IST).toInstant().toEpochMilli();

    private PaymentRepository repository;
    private CollectionProducer producer;

    private Payment payment(ObjectNode billDetails) {
        Bill bill = Bill.builder().consumerCode("5000007519rf").additionalDetails(billDetails).auditDetails(new AuditDetails()).build();
        return Payment.builder()
                .id("pay-1").tenantId("mh.mumbai")
                .transactionDate(TEN_DAYS_AGO)
                .paymentMode(PaymentModeEnum.CHEQUE)
                .instrumentStatus(InstrumentStatusEnum.APPROVED)
                .paymentStatus(PaymentStatusEnum.NEW)
                .totalAmountPaid(new BigDecimal("1000"))
                .auditDetails(new AuditDetails())
                .paymentDetails(new ArrayList<>(Collections.singletonList(
                        PaymentDetail.builder().id("pd-1").tenantId("mh.mumbai").bill(bill).auditDetails(new AuditDetails()).build())))
                .build();
    }

    private PaymentWorkflowService service(Payment payment) {
        repository = mock(PaymentRepository.class);
        producer = mock(CollectionProducer.class);
        when(repository.fetchPayments(any(PaymentSearchCriteria.class)))
                .thenAnswer(inv -> new ArrayList<>(Collections.singletonList(payment)));
        return new PaymentWorkflowService(repository, new PaymentWorkflowValidator(), producer, new ApplicationProperties());
    }

    private static PaymentWorkflowRequest request(PaymentWorkflow.PaymentAction action, ObjectNode additional) {
        PaymentWorkflow wf = new PaymentWorkflow();
        wf.setPaymentId("pay-1");
        wf.setTenantId("mh.mumbai");
        wf.setAction(action);
        wf.setReason("Insufficient funds");
        wf.setAdditionalDetails(additional);
        RequestInfo info = new RequestInfo();
        info.setUserInfo(User.builder().id(7L).uuid("clerk-1").build());
        PaymentWorkflowRequest request = new PaymentWorkflowRequest();
        request.setRequestInfo(info);
        request.setPaymentWorkflows(new ArrayList<>(Collections.singletonList(wf)));
        return request;
    }

    private static ObjectNode dishonourDetails(String date) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("dishonourReason", "Insufficient funds");
        if (date != null) {
            node.put("dishonourDate", date);
        }
        return node;
    }

    @Test
    void aValidDateIsStoredOnTheBillAndThePaymentIsDishonoured() {
        ObjectNode existing = JsonNodeFactory.instance.objectNode();
        existing.put("paymentId", "pay-1");
        Payment p = payment(existing);
        List<Payment> result = service(p).performWorkflow(
                request(PaymentWorkflow.PaymentAction.DISHONOUR, dishonourDetails(TODAY)));

        assertEquals(PaymentStatusEnum.DISHONOURED, result.get(0).getPaymentStatus());
        Bill bill = result.get(0).getPaymentDetails().get(0).getBill();
        assertEquals(TODAY, bill.getAdditionalDetails().get("dishonourDate").asText());
        assertEquals("pay-1", bill.getAdditionalDetails().get("paymentId").asText());
        verify(repository).updateStatus(anyList());
        verify(producer, times(1)).producer(any(), any());
    }

    @Test
    void anInvalidDateRejectsBeforeAnythingIsChanged() {
        Payment p = payment(null);
        PaymentWorkflowService svc = service(p);
        CustomException e = assertThrows(CustomException.class, () -> svc.performWorkflow(
                request(PaymentWorkflow.PaymentAction.DISHONOUR, dishonourDetails(TOMORROW))));
        assertEquals("INVALID_DISHONOUR_DATE", e.getCode());
        assertEquals(PaymentStatusEnum.NEW, p.getPaymentStatus());
        verify(repository, never()).updateStatus(anyList());
        verify(repository, never()).updateFileStoreId(anyList());
        verify(producer, never()).producer(any(), any());
    }

    @Test
    void aDishonourWithoutADateBehavesAsBefore() {
        Payment p = payment(null);
        List<Payment> result = service(p).performWorkflow(
                request(PaymentWorkflow.PaymentAction.DISHONOUR, dishonourDetails(null)));
        assertEquals(PaymentStatusEnum.DISHONOURED, result.get(0).getPaymentStatus());
        assertFalse(result.get(0).getPaymentDetails().get(0).getBill().getAdditionalDetails().has("dishonourDate"));
        verify(repository).updateStatus(anyList());
    }

    /**
     * SAP-migrated receipts carry a "cheque date" typically 86 days (up to 2,095) after the receipt.
     * It must not be a bound, or the true bank return date is refused and the dishonour blocked.
     */
    @Test
    void aCorruptOrFutureChequeDateNeverBlocksTheDishonour() {
        Payment p = payment(null);
        p.setInstrumentDate(LocalDate.now(IST).plusDays(76).atStartOfDay(IST).toInstant().toEpochMilli());
        List<Payment> result = service(p).performWorkflow(
                request(PaymentWorkflow.PaymentAction.DISHONOUR, dishonourDetails(LocalDate.now(IST).minusDays(3).toString())));
        assertEquals(PaymentStatusEnum.DISHONOURED, result.get(0).getPaymentStatus());
    }

    private static Payment paymentFor(String id, String consumerCode, int bills) {
        List<PaymentDetail> details = new ArrayList<>();
        for (int i = 0; i < bills; i++) {
            ObjectNode own = JsonNodeFactory.instance.objectNode();
            own.put("billOwnKey", "b" + i);
            Bill bill = Bill.builder().consumerCode(consumerCode + i).additionalDetails(own)
                    .auditDetails(new AuditDetails()).build();
            details.add(PaymentDetail.builder().id(id + "-pd" + i).tenantId("mh.mumbai").bill(bill)
                    .auditDetails(new AuditDetails()).build());
        }
        return Payment.builder().id(id).tenantId("mh.mumbai").transactionDate(TEN_DAYS_AGO)
                .paymentMode(PaymentModeEnum.CHEQUE).instrumentStatus(InstrumentStatusEnum.APPROVED)
                .paymentStatus(PaymentStatusEnum.NEW).totalAmountPaid(new BigDecimal("1000"))
                .auditDetails(new AuditDetails()).paymentDetails(details).build();
    }

    private PaymentWorkflowService serviceFor(List<Payment> payments) {
        repository = mock(PaymentRepository.class);
        producer = mock(CollectionProducer.class);
        when(repository.fetchPayments(any(PaymentSearchCriteria.class))).thenAnswer(inv -> new ArrayList<>(payments));
        return new PaymentWorkflowService(repository, new PaymentWorkflowValidator(), producer, new ApplicationProperties());
    }

    private static PaymentWorkflowRequest requestFor(List<String> ids, List<ObjectNode> details) {
        PaymentWorkflowRequest request = request(PaymentWorkflow.PaymentAction.DISHONOUR, details.get(0));
        List<PaymentWorkflow> wfs = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            PaymentWorkflow wf = new PaymentWorkflow();
            wf.setPaymentId(ids.get(i));
            wf.setTenantId("mh.mumbai");
            wf.setAction(PaymentWorkflow.PaymentAction.DISHONOUR);
            wf.setReason("Insufficient funds");
            wf.setAdditionalDetails(details.get(i));
            wfs.add(wf);
        }
        request.setPaymentWorkflows(wfs);
        return request;
    }

    /** One bad date in a multi-payment request rejects the whole request before ANY payment is touched. */
    @Test
    void oneBadDateInABatchChangesNothing() {
        Payment good = paymentFor("pay-a", "5000000001rf", 1);
        Payment bad = paymentFor("pay-b", "5000000002rf", 1);
        PaymentWorkflowService svc = serviceFor(java.util.Arrays.asList(good, bad));

        CustomException e = assertThrows(CustomException.class, () -> svc.performWorkflow(requestFor(
                java.util.Arrays.asList("pay-a", "pay-b"),
                java.util.Arrays.asList(dishonourDetails(TODAY), dishonourDetails(TOMORROW)))));

        assertEquals("INVALID_DISHONOUR_DATE", e.getCode());
        assertEquals(PaymentStatusEnum.NEW, good.getPaymentStatus(), "the valid payment is not changed either");
        assertEquals(PaymentStatusEnum.NEW, bad.getPaymentStatus());
        assertFalse(good.getPaymentDetails().get(0).getBill().getAdditionalDetails().has("dishonourDate"));
        verify(repository, never()).updateStatus(anyList());
        verify(producer, never()).producer(any(), any());
    }

    /** Every bill of the payment gets the date, and keeps its own keys. */
    @Test
    void everyBillOfThePaymentCarriesTheDate() {
        Payment p = paymentFor("pay-c", "5000000003rf", 2);
        List<Payment> result = serviceFor(Collections.singletonList(p)).performWorkflow(
                requestFor(Collections.singletonList("pay-c"), Collections.singletonList(dishonourDetails(TODAY))));

        for (PaymentDetail pd : result.get(0).getPaymentDetails()) {
            assertEquals(TODAY, pd.getBill().getAdditionalDetails().get("dishonourDate").asText());
            assertTrue(pd.getBill().getAdditionalDetails().has("billOwnKey"), "a bill's own details survive");
        }
    }

    /** An explicit null is treated as "no date": the dishonour proceeds exactly as before. */
    @Test
    void anExplicitNullDateBehavesAsNoDate() {
        ObjectNode details = dishonourDetails(null);
        details.putNull("dishonourDate");
        Payment p = payment(null);
        List<Payment> result = service(p).performWorkflow(request(PaymentWorkflow.PaymentAction.DISHONOUR, details));
        assertEquals(PaymentStatusEnum.DISHONOURED, result.get(0).getPaymentStatus());
        verify(repository).updateStatus(anyList());
    }

    /** CANCEL never looks at the key, so an odd caller cannot be blocked by it. */
    @Test
    void cancelIgnoresTheDishonourDate() {
        Payment p = payment(null);
        p.setPaymentMode(PaymentModeEnum.CASH);
        List<Payment> result = service(p).performWorkflow(
                request(PaymentWorkflow.PaymentAction.CANCEL, dishonourDetails("not-a-date")));
        assertEquals(PaymentStatusEnum.CANCELLED, result.get(0).getPaymentStatus());
        verify(repository).updateStatus(anyList());
    }
}
