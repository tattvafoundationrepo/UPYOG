package org.egov.demand.web.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;

/**
 * An eMarket transfer raises the source's unpaid rent as one demand on advanceIndex -1 dated in the transfer month,
 * each line carrying its real span. It refused the transfer month's own rent as a duplicate even when it carried only
 * earlier months (5888888995: Mar-Jul on an October demand). Real UAT stamps; lines read as JsonNode, as the row
 * mapper gives them.
 */
class DemandValidatorV1CarriedDuesTest {

    private static final String RENT = "TX.Emarket_Rental_Fees";
    private static final String CC = "5888888995rf";
    private static final String DUPLICATE = "EG_BS_DUPLICATE_CONSUMERCODE";
    private static final long MAR_FROM = 1772323200000L;
    private static final long JUL_TO = 1785522599000L;
    private static final long OCT_FROM = 1790812800000L;
    private static final long OCT_TO = 1793471399000L;
    private static final long AUG_FROM = 1785542400000L;
    private static final long AUG_TO = 1788200999000L;

    private final ObjectMapper mapper = new ObjectMapper();
    private DemandValidatorV1 validator;
    private DemandRepository repository;

    @BeforeEach
    void setUp() {
        validator = new DemandValidatorV1();
        repository = mock(DemandRepository.class);
        ReflectionTestUtils.setField(validator, "demandRepository", repository);
    }

    private DemandDetail line(String head, String additionalJson) throws Exception {
        DemandDetail dd = new DemandDetail();
        dd.setTaxHeadMasterCode(head);
        dd.setTaxAmount(BigDecimal.TEN);
        dd.setAdditionalDetails(additionalJson == null ? null : mapper.readTree(additionalJson));
        return dd;
    }

    private static String span(long from, long to) {
        return "{\"periodFrom\":\"" + from + "\",\"periodTo\":\"" + to + "\",\"glcode\":\"130100300\"}";
    }

    private static Demand demand(long from, long to, Integer advanceIndex, DemandDetail... lines) {
        Demand d = new Demand();
        d.setTenantId("mh.mumbai");
        d.setConsumerCode(CC);
        d.setBusinessService(RENT);
        d.setTaxPeriodFrom(from);
        d.setTaxPeriodTo(to);
        d.setAdvanceIndex(advanceIndex);
        d.setIsAdvance(false);
        d.setDemandDetails(new ArrayList<>(Arrays.asList(lines)));
        return d;
    }

    private Set<String> errorsFor(Demand incoming, Demand... inDb) throws Exception {
        when(repository.getDemandsForConsumerCodes(any(), anyString())).thenReturn(Arrays.asList(inDb));
        Map<String, Set<String>> byService = new HashMap<>();
        byService.put(RENT, new HashSet<>(Collections.singleton(CC)));
        Map<String, String> errorMap = new HashMap<>();
        Method m = DemandValidatorV1.class.getDeclaredMethod("validateConsumerCodes", List.class, Map.class, Map.class,
                DocumentContext.class);
        m.setAccessible(true);
        m.invoke(validator, Collections.singletonList(incoming), byService, errorMap, mock(DocumentContext.class));
        return errorMap.keySet();
    }

    private Demand octRent() throws Exception {
        return demand(OCT_FROM, OCT_TO, null, line("STALLAGE", null));
    }

    @Test
    void octobersRentIsNotADuplicateOfDuesCarriedOnlyToJuly() throws Exception {
        Demand lump = demand(OCT_FROM, OCT_TO, -1, line("STALLAGE", span(MAR_FROM, JUL_TO)),
                line("CGST", span(MAR_FROM, JUL_TO)));
        assertFalse(errorsFor(octRent(), lump).contains(DUPLICATE));
    }

    @Test
    void octoberIsStillADuplicateWhenTheCarriedDuesIncludeIt() throws Exception {
        Demand lump = demand(OCT_FROM, OCT_TO, -1, line("STALLAGE", span(MAR_FROM, OCT_TO)));
        assertTrue(errorsFor(octRent(), lump).contains(DUPLICATE));
    }

    @Test
    void aCarriedDemandWithoutLinePeriodsIsADuplicateAsBefore() throws Exception {
        assertTrue(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, -1, line("STALLAGE", null))).contains(DUPLICATE));
        assertTrue(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, -1, line("STALLAGE", "{\"periodTo\":\"x\"}")))
                .contains(DUPLICATE));
    }

    @Test
    void anAdvanceCreditLineDoesNotStretchTheCarriedSpan() throws Exception {
        Demand mixed = demand(OCT_FROM, OCT_TO, -1, line("STALLAGE", span(MAR_FROM, JUL_TO)),
                line("TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD", span(1775001600000L, 1806517799000L)));
        assertFalse(errorsFor(octRent(), mixed).contains(DUPLICATE));
    }

    @Test
    void anOrdinaryDemandIsStillADuplicate() throws Exception {
        assertTrue(errorsFor(octRent(), demand(OCT_FROM, OCT_TO, 0, line("STALLAGE", span(MAR_FROM, JUL_TO))))
                .contains(DUPLICATE));
    }

    @Test
    void anotherPeriodWasNeverADuplicate() throws Exception {
        Demand lump = demand(OCT_FROM, OCT_TO, -1, line("STALLAGE", span(MAR_FROM, OCT_TO)));
        assertFalse(errorsFor(demand(AUG_FROM, AUG_TO, null, line("STALLAGE", null)), lump).contains(DUPLICATE));
    }

    @Test
    void monthBoundariesAreReadInIst() throws Exception {
        Demand lump = demand(OCT_FROM, OCT_TO, -1, line("STALLAGE", span(MAR_FROM, JUL_TO)));
        Demand aug = demand(AUG_FROM, AUG_TO, null, line("STALLAGE", null));
        assertTrue(DemandValidatorV1.isCarriedDuesOutsideMonth(lump, aug), "31-Jul 23:59:59 IST is July");
        Demand jul = demand(1782864000000L, JUL_TO, null, line("STALLAGE", null));
        assertFalse(DemandValidatorV1.isCarriedDuesOutsideMonth(lump, jul));
    }

    @Test
    void readsJsonNodesMapsAndNumbers() throws Exception {
        assertEquals(Long.valueOf(5), DemandValidatorV1.epochField(mapper.readTree("{\"periodTo\":5}"), "periodTo"));
        assertEquals(Long.valueOf(5), DemandValidatorV1.epochField(mapper.readTree("{\"periodTo\":\"5\"}"), "periodTo"));
        Map<String, Object> map = new HashMap<>();
        map.put("periodTo", "7");
        assertEquals(Long.valueOf(7), DemandValidatorV1.epochField(map, "periodTo"));
        assertNull(DemandValidatorV1.epochField(mapper.readTree("{}"), "periodTo"));
        assertNull(DemandValidatorV1.epochField(null, "periodTo"));
    }
}
