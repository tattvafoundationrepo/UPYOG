package org.egov.demand.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.model.AdvSettlement;
import org.egov.demand.model.BillAccountDetailV2;
import org.egov.demand.model.BillDetailV2;
import org.egov.demand.model.BillV2;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.PaymentBackUpdateAudit;
import org.egov.demand.producer.Producer;
import org.egov.demand.repository.DemandRepository;
import org.egov.demand.util.Util;
import org.egov.demand.web.contract.BillRequestV2;
import org.egov.demand.web.contract.DemandRequest;
import org.egov.mdms.model.MdmsCriteriaReq;
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

import com.jayway.jsonpath.JsonPath;

/**
 * Cancelling a receipt that took MORE THAN ONE advance.
 *
 * <p>Reproduces licence 5000006753: one payment, a licence advance on one bill and a rental advance
 * on another. The rental vessel rides on an existing rent month so it sorts ahead of the licence
 * vessel, which is stamped with the fiscal year being prepaid. The old {@code findFirst()} unwound
 * the rent side and left the licence demand marked paid, with no licence penalty raised.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReceiptServiceV2AdvanceReversalTest {

	private static final String TENANT = "mh.mumbai";
	private static final String RENT_SERVICE = "TX.Emarket_Rental_Fees";
	private static final String LICENCE_SERVICE = "TX.Emarket_License_Fees";
	private static final String PENALTY_TOPIC = "create-penalty-demand-onpayment-reversal";

	private static final String RENT_VESSEL = "rent-vessel";
	private static final String LICENCE_VESSEL = "licence-vessel";
	private static final String RENT_SETTLED_APRIL = "rent-settled-april";
	private static final String RENT_SETTLED_MAY = "rent-settled-may";
	private static final String LICENCE_SETTLED = "licence-settled";

	@Mock
	private DemandService demandService;

	@Mock
	private DemandRepository demandRepository;

	@Mock
	private Util util;

	@Mock
	private Producer producer;

	@InjectMocks
	private ReceiptServiceV2 receiptServiceV2;

	@BeforeEach
	void setUp() {

		ReflectionTestUtils.setField(receiptServiceV2, "reverseAllAdvanceVessels", true);
		ReflectionTestUtils.setField(receiptServiceV2, "penaltyReversalBusinessService", RENT_SERVICE);

		when(util.getValueFromAdditionalDetailsForKey(any(), any())).thenReturn("market1124121012");
		when(util.prepareMdMsRequest(any(), any(), any(), any(), any())).thenReturn(MdmsCriteriaReq.builder().build());
		when(util.getAttributeValues(any())).thenReturn(JsonPath.parse(TAXHEAD_MASTER_JSON));

		// Everything the FI half of the cancellation touches. None of it is under test here; the
		// stubs exist so the method runs to the penalty publish at the end.
		when(demandRepository.getCollectionDate(any(), any())).thenReturn(null);
		when(demandRepository.getMarketEssentialInfo(any(), any())).thenReturn(Collections.emptyList());
		when(demandRepository.getPostedAdvanceGst(any(), any())).thenReturn(Collections.emptyMap());
		when(demandRepository.wasPostedAsAdvance(any())).thenReturn(null);
		when(demandRepository.buildCollectionFiReports(any(), any(), any(), any(), any(), anyBoolean(), any()))
				.thenReturn(new ArrayList<>());
		when(demandRepository.buildGstNettingReversalFiReports(any(), any())).thenReturn(new ArrayList<>());
		when(demandRepository.buildAdvanceSettlementReversalFiReports(any())).thenReturn(new ArrayList<>());
	}

	// ---------------------------------------------------------------- the bug

	@Test
	@DisplayName("Cancelling a receipt with a licence AND a rental advance reopens both sides")
	void bothAdvanceVesselsAreUnwound() {

		givenTwoVesselPayment();

		receiptServiceV2.updateDemandFromReceipt(twoVesselBillRequest(), true);

		Map<String, Demand> updated = capturedDemandsById();

		assertTrue(updated.containsKey(LICENCE_SETTLED),
				"the licence settled demand must be in the update request");
		assertEquals(BigDecimal.ZERO, collectedOn(updated.get(LICENCE_SETTLED), "TX.EMARKET_LICENSE_FEES"),
				"the licence demand must be reopened, not left paid");
		assertEquals(BigDecimal.ZERO, collectedOn(updated.get(RENT_SETTLED_APRIL), "STALLAGE"));
		assertEquals(BigDecimal.ZERO, collectedOn(updated.get(RENT_SETTLED_MAY), "STALLAGE"));
	}

	@Test
	@DisplayName("Only rent settlements reach the penalty topic; the licence one is skipped")
	void licenceSettlementIsNotPublished() {

		givenTwoVesselPayment();

		receiptServiceV2.updateDemandFromReceipt(twoVesselBillRequest(), true);

		List<String> published = capturedPublishedSettledDemandIds();

		assertEquals(Arrays.asList(RENT_SETTLED_APRIL, RENT_SETTLED_MAY), published,
				"a licence settlement carries a fiscal-year period and must not drive rent calculation");
	}

	@Test
	@DisplayName("The FI unwind runs for the licence settlement too")
	void licenceSettlementIsReversedInFi() {

		givenTwoVesselPayment();

		receiptServiceV2.updateDemandFromReceipt(twoVesselBillRequest(), true);

		verify(demandRepository).buildAdvanceSettlementReversalFiReports(LICENCE_SETTLED);
		verify(demandRepository).buildAdvanceSettlementReversalFiReports(RENT_SETTLED_APRIL);
		verify(demandRepository).buildAdvanceSettlementReversalFiReports(RENT_SETTLED_MAY);
	}

	// ------------------------------------------------------------- edge cases

	@Test
	@DisplayName("Kill switch off restores the old first-vessel-only behaviour")
	void killSwitchRestoresPreviousBehaviour() {

		ReflectionTestUtils.setField(receiptServiceV2, "reverseAllAdvanceVessels", false);
		givenTwoVesselPayment();

		receiptServiceV2.updateDemandFromReceipt(twoVesselBillRequest(), true);

		Map<String, Demand> updated = capturedDemandsById();

		assertTrue(updated.containsKey(RENT_SETTLED_APRIL), "the first vessel is still unwound");
		assertFalse(updated.containsKey(LICENCE_SETTLED), "the second vessel is skipped, as before");
	}

	@Test
	@DisplayName("A demand settled by two advances is reopened once and published once")
	void oneDemandSettledByTwoAdvancesIsDeduplicated() {

		Demand rentVessel = advanceVessel(RENT_VESSEL, "5000006753rf", RENT_SERVICE,
				"TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD");
		Demand secondRentVessel = advanceVessel("rent-vessel-2", "5000006753rf", RENT_SERVICE,
				"TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD");

		when(demandService.getDemands(any(), any()))
				.thenReturn(new ArrayList<>(Arrays.asList(rentVessel, secondRentVessel)));

		// Both advances contributed to the SAME rent demand: two rows, one demand.
		when(demandRepository.getSettledDemandIdsByAdvanceDemandId(RENT_VESSEL))
				.thenReturn(Collections.singletonList(settlement(RENT_VESSEL, RENT_SETTLED_APRIL, "5000006753rf")));
		when(demandRepository.getSettledDemandIdsByAdvanceDemandId("rent-vessel-2"))
				.thenReturn(Collections.singletonList(settlement("rent-vessel-2", RENT_SETTLED_APRIL, "5000006753rf")));

		when(demandRepository.getDemands(any(DemandCriteria.class)))
				.thenReturn(new ArrayList<>(Collections.singletonList(
						settledDemand(RENT_SETTLED_APRIL, "5000006753rf", RENT_SERVICE, "STALLAGE", "795.00"))));

		receiptServiceV2.updateDemandFromReceipt(
				billRequest(billFor(RENT_SERVICE, "bill-rent", RENT_VESSEL, "rent-vessel-2")), true);

		long occurrences = capturedDemandList().stream()
				.filter(d -> RENT_SETTLED_APRIL.equals(d.getId()))
				.count();
		assertEquals(1L, occurrences, "the demand must not be added to the update request twice");
		assertEquals(Collections.singletonList(RENT_SETTLED_APRIL), capturedPublishedSettledDemandIds(),
				"and must not raise two penalties");
	}

	@Test
	@DisplayName("A receipt with no advance vessel looks up nothing and publishes nothing")
	void noAdvanceVesselIsANoOp() {

		Demand plain = settledDemand("plain", "5000006753rf", RENT_SERVICE, "STALLAGE", "795.00");
		when(demandService.getDemands(any(), any()))
				.thenReturn(new ArrayList<>(Collections.singletonList(plain)));

		receiptServiceV2.updateDemandFromReceipt(
				billRequest(billFor(RENT_SERVICE, "bill-rent", "plain")), true);

		verify(demandRepository, never()).getSettledDemandIdsByAdvanceDemandId(any());
		verify(producer, never()).push(eq(PENALTY_TOPIC), any());
	}

	@Test
	@DisplayName("A settlement whose demand no longer exists is reopened-safe and not published")
	void settlementWithMissingDemandIsNotPublished() {

		Demand rentVessel = advanceVessel(RENT_VESSEL, "5000006753rf", RENT_SERVICE,
				"TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD");
		when(demandService.getDemands(any(), any()))
				.thenReturn(new ArrayList<>(Collections.singletonList(rentVessel)));
		when(demandRepository.getSettledDemandIdsByAdvanceDemandId(RENT_VESSEL))
				.thenReturn(Collections.singletonList(settlement(RENT_VESSEL, "vanished", "5000006753rf")));
		when(demandRepository.getDemands(any(DemandCriteria.class))).thenReturn(new ArrayList<>());

		receiptServiceV2.updateDemandFromReceipt(
				billRequest(billFor(RENT_SERVICE, "bill-rent", RENT_VESSEL)), true);

		verify(producer, never()).push(eq(PENALTY_TOPIC), any());
	}

	@Test
	@DisplayName("A blank business-service override falls back to the default instead of muting penalties")
	void blankPropertyFallsBackToDefault() {

		ReflectionTestUtils.setField(receiptServiceV2, "penaltyReversalBusinessService", "   ");

		assertTrue(receiptServiceV2.isPublishableToPenaltyTopic(RENT_SERVICE));
		assertFalse(receiptServiceV2.isPublishableToPenaltyTopic(LICENCE_SERVICE));
	}

	@Test
	@DisplayName("The business-service filter tolerates nulls, case and padding")
	void businessServiceFilterIsDefensive() {

		assertFalse(receiptServiceV2.isPublishableToPenaltyTopic(null),
				"an unknown service must not be published");
		assertTrue(receiptServiceV2.isPublishableToPenaltyTopic("tx.emarket_rental_fees"));
		assertTrue(receiptServiceV2.isPublishableToPenaltyTopic(" TX.Emarket_Rental_Fees "));
		assertFalse(receiptServiceV2.isPublishableToPenaltyTopic("TX.Emarket_Penalty_Fees"),
				"the rent-penalty service ends in the rent suffix and must still be excluded");
	}

	@Test
	@DisplayName("A normal payment is untouched: no settlement lookup, no publish")
	void paymentPathIsUnchanged() {

		Demand plain = settledDemand("plain", "5000006753rf", RENT_SERVICE, "STALLAGE", "795.00");
		when(demandService.getDemands(any(), any()))
				.thenReturn(new ArrayList<>(Collections.singletonList(plain)));

		receiptServiceV2.updateDemandFromReceipt(
				billRequest(billFor(RENT_SERVICE, "bill-rent", "plain")), false);

		verify(demandRepository, never()).getSettledDemandIdsByAdvanceDemandId(any());
		verify(producer, never()).push(eq(PENALTY_TOPIC), any());
	}

	// ------------------------------------------------------------------ setup

	/**
	 * The 5000006753 shape. The rental vessel is returned FIRST, exactly as the demand search does:
	 * it orders by taxperiodfrom and the rental advance rides on an earlier rent month.
	 */
	private void givenTwoVesselPayment() {

		Demand rentVessel = advanceVessel(RENT_VESSEL, "5000006753rf", RENT_SERVICE,
				"TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD");
		Demand licenceVessel = advanceVessel(LICENCE_VESSEL, "5000006753lf", LICENCE_SERVICE,
				"TX.EMARKET_LICENSE_ADVANCE_CARRYFORWARD");

		when(demandService.getDemands(any(), any()))
				.thenReturn(new ArrayList<>(Arrays.asList(rentVessel, licenceVessel)));

		when(demandRepository.getSettledDemandIdsByAdvanceDemandId(RENT_VESSEL))
				.thenReturn(Arrays.asList(
						settlement(RENT_VESSEL, RENT_SETTLED_APRIL, "5000006753rf"),
						settlement(RENT_VESSEL, RENT_SETTLED_MAY, "5000006753rf")));
		when(demandRepository.getSettledDemandIdsByAdvanceDemandId(LICENCE_VESSEL))
				.thenReturn(Collections.singletonList(
						settlement(LICENCE_VESSEL, LICENCE_SETTLED, "5000006753lf")));

		when(demandRepository.getDemands(any(DemandCriteria.class))).thenAnswer(inv -> {
			DemandCriteria criteria = inv.getArgument(0);
			List<Demand> all = Arrays.asList(
					settledDemand(RENT_SETTLED_APRIL, "5000006753rf", RENT_SERVICE, "STALLAGE", "795.00"),
					settledDemand(RENT_SETTLED_MAY, "5000006753rf", RENT_SERVICE, "STALLAGE", "795.00"),
					settledDemand(LICENCE_SETTLED, "5000006753lf", LICENCE_SERVICE, "TX.EMARKET_LICENSE_FEES", "900.00"));
			return all.stream()
					.filter(d -> criteria.getDemandId().contains(d.getId()))
					.collect(Collectors.toCollection(ArrayList::new));
		});
	}

	private BillRequestV2 twoVesselBillRequest() {
		return billRequest(
				billFor(RENT_SERVICE, "bill-rent", RENT_VESSEL),
				billFor(LICENCE_SERVICE, "bill-licence", LICENCE_VESSEL));
	}

	// ------------------------------------------------------------- assertions

	private List<Demand> capturedDemandList() {
		ArgumentCaptor<DemandRequest> captor = ArgumentCaptor.forClass(DemandRequest.class);
		verify(demandService).updateAsync(captor.capture(), any(PaymentBackUpdateAudit.class));
		return captor.getValue().getDemands();
	}

	private Map<String, Demand> capturedDemandsById() {
		return capturedDemandList().stream()
				.collect(Collectors.toMap(Demand::getId, d -> d, (a, b) -> a));
	}

	private List<String> capturedPublishedSettledDemandIds() {
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(producer, atLeast(0)).push(eq(PENALTY_TOPIC), captor.capture());
		return captor.getAllValues().stream()
				.map(o -> ((AdvSettlement) o).getSettledDemandId())
				.collect(Collectors.toList());
	}

	private static BigDecimal collectedOn(Demand demand, String taxHead) {
		return demand.getDemandDetails().stream()
				.filter(dd -> taxHead.equals(dd.getTaxHeadMasterCode()))
				.map(DemandDetail::getCollectionAmount)
				.findFirst()
				.orElseThrow(() -> new AssertionError("no " + taxHead + " detail on " + demand.getId()));
	}

	// ---------------------------------------------------------------- fixtures

	/** An advance vessel after the advance was drawn on: the head is negative, the collection too. */
	private static Demand advanceVessel(String id, String consumerCode, String businessService, String advanceHead) {
		return Demand.builder()
				.id(id)
				.tenantId(TENANT)
				.consumerCode(consumerCode)
				.businessService(businessService)
				.taxPeriodFrom(1L)
				.taxPeriodTo(2L)
				.demandDetails(new ArrayList<>(Collections.singletonList(
						DemandDetail.builder()
								.id(id + "-dd")
								.taxHeadMasterCode(advanceHead)
								.taxAmount(new BigDecimal("-900.00"))
								.collectionAmount(new BigDecimal("-900.00"))
								.build())))
				.build();
	}

	/** A demand the advance settled: fully collected, which is what leaves it looking paid. */
	private static Demand settledDemand(String id, String consumerCode, String businessService,
			String taxHead, String amount) {
		return Demand.builder()
				.id(id)
				.tenantId(TENANT)
				.consumerCode(consumerCode)
				.businessService(businessService)
				.taxPeriodFrom(1L)
				.taxPeriodTo(2L)
				.demandDetails(new ArrayList<>(Collections.singletonList(
						DemandDetail.builder()
								.id(id + "-dd")
								.taxHeadMasterCode(taxHead)
								.taxAmount(new BigDecimal(amount))
								.collectionAmount(new BigDecimal(amount))
								.build())))
				.build();
	}

	private static AdvSettlement settlement(String advanceDemandId, String settledDemandId, String consumerCode) {
		return AdvSettlement.builder()
				.advanceDemandId(advanceDemandId)
				.settledDemandId(settledDemandId)
				.consumerCode(consumerCode)
				.taxPeriodFrom(1L)
				.taxPeriodTo(2L)
				.build();
	}

	private static BillRequestV2 billRequest(BillV2... bills) {
		return BillRequestV2.builder()
				.requestInfo(RequestInfo.builder().build())
				.bills(new ArrayList<>(Arrays.asList(bills)))
				.build();
	}

	/** A bill whose details carry no money, so the bill-detail pass is a no-op for these tests. */
	private static BillV2 billFor(String businessService, String billId, String... demandIds) {
		List<BillDetailV2> details = new ArrayList<>();
		for (String demandId : demandIds) {
			details.add(BillDetailV2.builder()
					.id(billId + "-" + demandId)
					.demandId(demandId)
					.fromPeriod(1L)
					.toPeriod(2L)
					.amount(BigDecimal.ONE)
					.amountPaid(BigDecimal.ZERO)
					.billAccountDetails(new ArrayList<>())
					.build());
		}
		return BillV2.builder()
				.id(billId)
				.tenantId(TENANT)
				.businessService(businessService)
				.status(BillV2.BillStatus.CANCELLED)
				.consumerCode("5000006753")
				.billDetails(details)
				.build();
	}

	private static final String TAXHEAD_MASTER_JSON =
			"{\"MdmsRes\":{\"BillingService\":{\"TaxHeadMaster\":["
					+ "{\"category\":\"ADVANCE_COLLECTION\",\"service\":\"" + RENT_SERVICE
					+ "\",\"code\":\"TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD\"},"
					+ "{\"category\":\"ADVANCE_COLLECTION\",\"service\":\"" + LICENCE_SERVICE
					+ "\",\"code\":\"TX.EMARKET_LICENSE_ADVANCE_CARRYFORWARD\"}"
					+ "]}}}";
}
