package org.egov.collection.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.egov.collection.config.ApplicationProperties;
import org.egov.collection.model.Payment;
import org.egov.collection.model.enums.PaymentModeEnum;
import org.egov.collection.repository.PaymentRepository;
import org.egov.collection.repository.ServiceRequestRepository;
import org.egov.collection.service.PaymentWorkflowService;
import org.junit.jupiter.api.Test;

/** The cheque/DD date branch of PaymentValidator as wired, error keys included. */
class PaymentValidatorChequeDateTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final String FUTURE = "CHEQUE_DD_DATE_WITH_FUTURE_DATE";
    private static final String TOO_OLD = "CHEQUE_DD_DATE_WITH_RECEIPT_DATE";

    private final PaymentValidator validator = new PaymentValidator(mock(PaymentRepository.class),
            mock(PaymentWorkflowService.class), new ApplicationProperties(), mock(ServiceRequestRepository.class));

    private Set<String> errors(Long instrumentDate, Long transactionDate) throws Exception {
        Payment payment = new Payment();
        payment.setPaymentMode(PaymentModeEnum.CHEQUE);
        payment.setInstrumentDate(instrumentDate);
        payment.setTransactionDate(transactionDate);
        Map<String, String> errorMap = new HashMap<>();
        Method m = PaymentValidator.class.getDeclaredMethod("validateChequeDD", Payment.class, Map.class);
        m.setAccessible(true);
        try {
            m.invoke(validator, payment, errorMap);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
        return errorMap.keySet();
    }

    private static long endOfIstDay(LocalDate day) {
        return day.plusDays(1).atStartOfDay(IST).toInstant().toEpochMilli() - 1000;
    }

    private static LocalDate today() {
        return LocalDate.now(IST);
    }

    @Test
    void todaysChequeAsTheFormSendsItPasses() throws Exception {
        assertEquals(Collections.emptySet(), errors(endOfIstDay(today()), null));
    }

    @Test
    void threeMonthsBackPasses() throws Exception {
        assertEquals(Collections.emptySet(), errors(endOfIstDay(today().minusMonths(3)), null));
    }

    @Test
    void tomorrowIsFuture() throws Exception {
        assertEquals(Collections.singleton(FUTURE), errors(today().plusDays(1).atStartOfDay(IST).toInstant().toEpochMilli(), null));
    }

    @Test
    void fourMonthsBackIsTooOld() throws Exception {
        assertEquals(Collections.singleton(TOO_OLD), errors(endOfIstDay(today().minusMonths(4)), null));
    }

    @Test
    void missingDateAddsNothingHere() throws Exception {
        // As before (read as "now"); create reports the missing date separately as INVALID_INST_DATE.
        assertEquals(Collections.emptySet(), errors(null, null));
    }

    @Test
    void manualReceiptDateBranchIsUnchanged() throws Exception {
        long receiptDay = endOfIstDay(today().minusDays(10));
        // Cheque after the manual receipt date: refused as before.
        assertEquals(Collections.singleton("INVALID_CHEQUE_DD_DATE"),
                errors(receiptDay + 86_400_000L, receiptDay));
        // More than 90 days before it: refused as before (this branch keeps the 90-day rule).
        assertTrue(errors(receiptDay - 95L * 86_400_000L, receiptDay).contains("CHEQUE_DD_DATE_WITH_MANUAL_RECEIPT_DATE"));
    }

    @Test
    void absurdDatesAreRefusedNotCrashed() throws Exception {
        assertEquals(Collections.singleton(TOO_OLD), errors(0L, null));
        assertEquals(Collections.singleton(TOO_OLD), errors(-1L, null));
        assertEquals(Collections.singleton(TOO_OLD), errors(LocalDate.of(1, 1, 1).atStartOfDay(IST).toInstant().toEpochMilli(), null));
        assertEquals(Collections.singleton(FUTURE), errors(LocalDate.of(9999, 12, 31).atStartOfDay(IST).toInstant().toEpochMilli(), null));
    }
}
