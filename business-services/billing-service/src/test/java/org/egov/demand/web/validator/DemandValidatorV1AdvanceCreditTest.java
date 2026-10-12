package org.egov.demand.web.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.repository.DemandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.jayway.jsonpath.DocumentContext;

/**
 * 5888888990 (07-10-2026): an eMarket transfer carries the source's advance as a credit-only demand on the transfer
 * month (isAdvance false, advanceIndex -1). The duplicate check took it for that month's rent and refused October.
 * Only that case changes; every other duplicate outcome is pinned here.
 */
class DemandValidatorV1AdvanceCreditTest {

    private static final String RENT = "TX.Emarket_Rental_Fees";
    private static final String CC = "5888888990rf";
    private static final String CF = "TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD";
    private static final String DUPLICATE = "EG_BS_DUPLICATE_CONSUMERCODE";
    private static final long OCT_FROM = 1790812800000L;
    private static final long OCT_TO = 1793471399000L;

    private DemandValidatorV1 validator;
    private DemandRepository repository;

    @BeforeEach
    void setUp() {
        validator = new DemandValidatorV1();
        repository = mock(DemandRepository.class);
        ReflectionTestUtils.setField(validator, "demandRepository", repository);
    }

    private static Demand demand(long from, long to, Integer advanceIndex, Boolean isAdvance, String... heads) {
        Demand d = new Demand();
        d.setTenantId("mh.mumbai");
        d.setConsumerCode(CC);
        d.setBusinessService(RENT);
        d.setTaxPeriodFrom(from);
        d.setTaxPeriodTo(to);
        d.setAdvanceIndex(advanceIndex);
        d.setIsAdvance(isAdvance);
        List<DemandDetail> details = new ArrayList<>();
        for (String h : heads) {
            DemandDetail dd = new DemandDetail();
            dd.setTaxHeadMasterCode(h);
            dd.setTaxAmount(BigDecimal.TEN);
            details.add(dd);
        }
        d.setDemandDetails(details);
        return d;
    }

    /** Runs the private duplicate check; returns the error keys. */
    private Set<String> errorsFor(Demand incoming, Demand... inDb) throws Exception {
        when(repository.getDemandsForConsumerCodes(any(), anyString())).thenReturn(Arrays.asList(inDb));
        Map<String, Set<String>> byService = new HashMap<>();
        byService.put(RENT, new HashSet<>(Collections.singleton(CC)));
        Map<String, String> errorMap = new HashMap<>();
        Method m = DemandValidatorV1.class.getDeclaredMethod("validateConsumerCodes", List.class, Map.class, Map.class,
                DocumentContext.class);
        m.setAccessible(true);
        // MDMS lookup is not needed for regular demands; a failing read defaults isAdvanceAllowed to false.
        m.invoke(validator, Collections.singletonList(incoming), byService, errorMap, mock(DocumentContext.class));
        return errorMap.keySet();
    }

    private static Demand octRent() {
        return demand(OCT_FROM, OCT_TO, null, null, "STALLAGE", "CGST", "SGST");
    }

    @Test
    void transferCreditOnTheMonthIsNotADuplicate() throws Exception {
        assertFalse(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, -1, false, CF)).contains(DUPLICATE));
    }

    @Test
    void aRealRentDemandForTheMonthIsStillADuplicate() throws Exception {
        assertTrue(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, 0, false, "STALLAGE")).contains(DUPLICATE));
    }

    @Test
    void carriedRentOnTheTransferIndexIsStillADuplicate() throws Exception {
        assertTrue(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, -1, false, "STALLAGE", "CGST")).contains(DUPLICATE));
    }

    @Test
    void aCreditOnTheSameIndexIsStillADuplicate() throws Exception {
        assertTrue(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, 0, false, CF)).contains(DUPLICATE));
        assertTrue(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, null, false, CF)).contains(DUPLICATE));
        assertTrue(errorsFor(demand(OCT_FROM, OCT_TO, -1, null, "STALLAGE"), demand(OCT_FROM, OCT_TO, -1, false, CF))
                .contains(DUPLICATE));
    }

    @Test
    void mixedRentAndCreditIsStillADuplicate() throws Exception {
        assertTrue(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, -1, false, "STALLAGE", CF)).contains(DUPLICATE));
    }

    @Test
    void aDemandWithoutDetailsIsStillADuplicate() throws Exception {
        assertTrue(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, -1, false)).contains(DUPLICATE));
        Demand nullDetails = demand(OCT_FROM, OCT_TO, -1, false);
        nullDetails.setDemandDetails(null);
        assertTrue(errorsFor(octRent(), nullDetails).contains(DUPLICATE));
    }

    @Test
    void aDetailWithoutATaxHeadIsNotACredit() throws Exception {
        Demand d = demand(OCT_FROM, OCT_TO, -1, false, CF);
        d.getDemandDetails().add(new DemandDetail());
        assertTrue(errorsFor(octRent(), d).contains(DUPLICATE));
    }

    @Test
    void existingExemptionsAreUnchanged() throws Exception {
        // isAdvance rows and positive advance indexes were never compared, credit or not.
        assertFalse(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, 0, true, CF)).contains(DUPLICATE));
        assertFalse(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, 3, false, "STALLAGE")).contains(DUPLICATE));
    }

    @Test
    void anotherPeriodWasNeverADuplicate() throws Exception {
        assertFalse(errorsFor(octRent(), demand(OCT_FROM + 1, OCT_TO, 0, false, "STALLAGE")).contains(DUPLICATE));
    }
}
