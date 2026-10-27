package org.egov.collection.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.egov.collection.config.ApplicationProperties;
import org.egov.collection.model.PaymentDetail;
import org.egov.collection.repository.PaymentRepository;
import org.egov.collection.repository.ServiceRequestRepository;
import org.egov.collection.service.PaymentWorkflowService;
import org.egov.collection.web.contract.Bill;
import org.egov.collection.web.contract.BillDetail;
import org.junit.jupiter.api.Test;

/**
 * Paise on eMarket bills (BMC 26-10-2026): a demand carries its exact amount, so collection-services must accept
 * 1680.70 for the configured business services, and keep the whole-rupee rule for every other one.
 */
class PaymentValidatorFractionTest {

    private static final String RENT = "TX.Emarket_Rental_Fees";
    private static final String PENALTY = "TX.Emarket_Penalty_Fees";
    private static final String OTHER = "PT";

    private static PaymentValidator validator(String configured) {
        ApplicationProperties props = new ApplicationProperties();
        props.setFractionAllowedBusinessServices(configured);
        return new PaymentValidator(mock(PaymentRepository.class), mock(PaymentWorkflowService.class), props,
                mock(ServiceRequestRepository.class));
    }

    private static Map<String, String> errors(PaymentValidator v, String service, boolean advanceAllowed,
                                              String due, String paid) throws Exception {
        BillDetail detail = new BillDetail();
        detail.setExpiryDate(System.currentTimeMillis() + 86_400_000L);
        Bill bill = new Bill();
        bill.setId("bill-1");
        bill.setBusinessService(service);
        bill.setTotalAmount(new BigDecimal(due));
        bill.setIsAdvanceAllowed(advanceAllowed);
        bill.setPartPaymentAllowed(true);
        bill.setBillDetails(Collections.singletonList(detail));
        PaymentDetail pd = new PaymentDetail();
        pd.setBillId("bill-1");
        pd.setBill(bill);
        pd.setTotalDue(new BigDecimal(due));
        pd.setTotalAmountPaid(new BigDecimal(paid));

        Map<String, String> errorMap = new HashMap<>();
        Method m = PaymentValidator.class.getDeclaredMethod("validatePaymentDetailAgainstBill", String.class,
                PaymentDetail.class, Map.class);
        m.setAccessible(true);
        try {
            m.invoke(v, "CASH", pd, errorMap);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
        return errorMap;
    }

    private static final String EMARKET = RENT + ", " + PENALTY;

    @Test
    void paiseAreAcceptedForAConfiguredService() throws Exception {
        assertEquals(Collections.emptyMap(), errors(validator(EMARKET), RENT, true, "1680.70", "1680.70"));
        // A penalty bill takes no advance: the bill-amount check runs too and must accept paise.
        assertEquals(Collections.emptyMap(), errors(validator(EMARKET), PENALTY, false, "123.45", "123.45"));
    }

    @Test
    void moreThanTwoDecimalsIsStillRejected() throws Exception {
        Map<String, String> e = errors(validator(EMARKET), RENT, true, "1680.705", "1680.705");
        assertTrue(e.containsKey("INVALID_BILL"));
        assertTrue(e.containsKey("INVALID_PAYMENTDETAIL"));
    }

    @Test
    void aNegativeAmountIsStillRejected() throws Exception {
        assertTrue(errors(validator(EMARKET), PENALTY, false, "-10.50", "10.50").containsKey("INVALID_BILL_AMOUNT"));
    }

    @Test
    void otherServicesKeepTheWholeRupeeRule() throws Exception {
        Map<String, String> e = errors(validator(EMARKET), OTHER, true, "1680.70", "1680.70");
        assertEquals("The due amount cannot be fractional", e.get("INVALID_BILL"));
        assertEquals("The amount paid cannot be fractional", e.get("INVALID_PAYMENTDETAIL"));
    }

    @Test
    void unconfiguredIsExactlyTheOldRule() throws Exception {
        for (String configured : new String[] { "", null, "   " }) {
            Map<String, String> e = errors(validator(configured), RENT, true, "1680.70", "1680.70");
            assertEquals("The due amount cannot be fractional", e.get("INVALID_BILL"), "configured=" + configured);
            assertEquals(Collections.emptyMap(), errors(validator(configured), RENT, true, "1681", "1681"));
        }
    }
}
