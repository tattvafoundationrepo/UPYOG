package org.egov.demand.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.model.AdvSettlement;
import org.egov.demand.model.ApportionDemandResponse;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.repository.DemandRepository;
import org.egov.demand.repository.ServiceRequestRepository;
import org.egov.demand.util.Util;
import org.egov.demand.web.contract.DemandRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * Recording how much of an advance each settlement drew. The switch is off by default and, while off, the
 * settlement row is written exactly as before; on, the same row also carries the amount drawn and the time.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DemandServiceSettlementAmountTest {

	private static final String RENT = "TX.Emarket_Rental_Fees";
	private static final String ADVANCE_HEAD = "TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD";
	private static final String ADVANCE = "advance-demand";
	private static final String NEW_MONTH = "november-demand";

	@Mock
	private DemandRepository demandRepository;

	@Mock
	private ServiceRequestRepository serviceRequestRepository;

	@Mock
	private Util util;

	@InjectMocks
	private DemandService demandService;

	private final DocumentContext mdms = JsonPath.parse("{\"MdmsRes\":{\"BillingService\":{\"TaxHeadMaster\":["
			+ "{\"category\":\"ADVANCE_COLLECTION\",\"service\":\"" + RENT + "\",\"code\":\"" + ADVANCE_HEAD + "\"}]}}}");

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(demandService, "mapper", new ObjectMapper());
		when(util.getApportionURL()).thenReturn("http://apportion");

		// An unused 2,192.44 advance; the new November demand of 1,096.22 is about to be created.
		when(demandRepository.getDemands(any(DemandCriteria.class)))
				.thenReturn(new ArrayList<>(Collections.singletonList(advance("0.00"))));
		// The apportion service draws 1,096.22 of the advance to settle November in full.
		ApportionDemandResponse response = new ApportionDemandResponse();
		response.setDemands(Arrays.asList(advance("-1096.22"), month("1096.22")));
		// fetchResult hands back the raw JSON map; the service converts it, as it does in production.
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> raw = new ObjectMapper().convertValue(response, java.util.Map.class);
		when(serviceRequestRepository.fetchResult(any(), any())).thenReturn(raw);
	}

	private void apportion() {
		DemandRequest request = DemandRequest.builder()
				.requestInfo(new RequestInfo())
				.demands(new ArrayList<>(Collections.singletonList(month("0.00"))))
				.build();
		ReflectionTestUtils.invokeMethod(demandService, "apportionAdvanceIfExist", request, mdms,
				new ArrayList<Demand>(), new ArrayList<Demand>(), new HashMap<String, BigDecimal>());
	}

	@Test
	@DisplayName("Switch off: the settlement row is written exactly as before")
	void switchOffWritesTheRowAsBefore() {

		apportion();

		ArgumentCaptor<AdvSettlement> row = ArgumentCaptor.forClass(AdvSettlement.class);
		verify(demandRepository).saveAdvSettlementDemandIds(row.capture());
		verify(demandRepository, never()).saveAdvSettlementDemandIds(any(), any(), any());
		assertEquals(ADVANCE, row.getValue().getAdvanceDemandId());
		assertEquals(NEW_MONTH, row.getValue().getSettledDemandId());
	}

	@Test
	@DisplayName("Switch on: the same row also carries the amount drawn and the time")
	void switchOnRecordsAmountAndTime() {

		ReflectionTestUtils.setField(demandService, "recordSettlementAmount", true);

		apportion();

		ArgumentCaptor<AdvSettlement> row = ArgumentCaptor.forClass(AdvSettlement.class);
		ArgumentCaptor<Long> at = ArgumentCaptor.forClass(Long.class);
		verify(demandRepository).saveAdvSettlementDemandIds(row.capture(), eq(new BigDecimal("1096.22")), at.capture());
		verify(demandRepository, never()).saveAdvSettlementDemandIds(any());
		assertEquals(ADVANCE, row.getValue().getAdvanceDemandId());
		assertEquals(NEW_MONTH, row.getValue().getSettledDemandId());
		assertNotNull(at.getValue());
	}

	@Test
	@DisplayName("The amount drawn is never guessed when either side is unknown")
	void amountDrawnIsNullWhenUnknown() {

		assertEquals(new BigDecimal("1096.22"), DemandService.advanceDrawn(BigDecimal.ZERO, advance("-1096.22")));
		assertEquals(new BigDecimal("900.00"), DemandService.advanceDrawn(new BigDecimal("-200.00"), advance("-1100.00")));
		assertNull(DemandService.advanceDrawn(null, advance("-1096.22")));
		assertNull(DemandService.advanceDrawn(BigDecimal.ZERO, null));
	}

	private static Demand advance(String collected) {
		return demand(ADVANCE, ADVANCE_HEAD, "-2192.44", collected);
	}

	private static Demand month(String collected) {
		return demand(NEW_MONTH, "STALLAGE", "1096.22", collected);
	}

	private static Demand demand(String id, String head, String tax, String collected) {
		List<DemandDetail> details = new ArrayList<>();
		details.add(DemandDetail.builder().taxHeadMasterCode(head)
				.taxAmount(new BigDecimal(tax)).collectionAmount(new BigDecimal(collected)).build());
		return Demand.builder().id(id).tenantId("mh.mumbai").consumerCode("5000007528rf").businessService(RENT)
				.taxPeriodFrom(1L).taxPeriodTo(2L).demandDetails(details).build();
	}
}
