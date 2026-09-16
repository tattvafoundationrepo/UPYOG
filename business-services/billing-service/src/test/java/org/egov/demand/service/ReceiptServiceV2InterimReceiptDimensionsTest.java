package org.egov.demand.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.egov.demand.model.FiDimensions;
import org.egov.demand.model.PaymentMarketInfo;
import org.egov.demand.repository.DemandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Which business area an interim receipt is posted at, and what it falls back to.
 *
 * <p>The repository is a SPY over a real instance, so the rule itself (fund centre = business area +
 * 130000, functional area 00301000000) runs for real and only the ward-master lookup is stubbed.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReceiptServiceV2InterimReceiptDimensionsTest {

	private static final String WARD_C = "mh.mumbai.zone1.wardc";
	private static final String WARD_KW = "mh.mumbai.zone4.wardk_west";

	@Spy
	private DemandRepository demandRepository = new DemandRepository();

	@InjectMocks
	private ReceiptServiceV2 receiptServiceV2;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(receiptServiceV2, "collectionCfcDimensionsEnabled", true);
		doReturn(demandRepository.interimReceiptDimensions("11", "4030")).when(demandRepository).getCfcWardDimensions(WARD_C);
		doReturn(demandRepository.interimReceiptDimensions("11", "4130")).when(demandRepository).getCfcWardDimensions(WARD_KW);
		doReturn(null).when(demandRepository).getCfcWardDimensions("mh.mumbai.zone7.wardr_north");
	}

	private FiDimensions resolve(PaymentMarketInfo info) {
		return ReflectionTestUtils.invokeMethod(receiptServiceV2, "resolveCollectingDimensions", info);
	}

	/**
	 * A receipt for a licensee whose market is in K/East (4120). {@code collectingWardTenant} is what
	 * emarket-v1 stamps for a collector holding a ward-level CFC grant; null leaves the key out, as it
	 * does for a SUPERUSER.
	 */
	private static PaymentMarketInfo payment(String collectingWardTenant) {
		return paymentWithDetails(collectingWardTenant == null ? "{}"
				: "{\"collectingWardTenant\":\"" + collectingWardTenant + "\"}");
	}

	private static PaymentMarketInfo paymentWithDetails(String additionalDetails) {
		PaymentMarketInfo info = new PaymentMarketInfo();
		info.setFund("11");
		info.setFundCenter("4120420103");
		info.setBusinessArea("4120");
		info.setFunctionalArea("55800000000");
		info.setAdditionalDetails(additionalDetails);
		return info;
	}

	private static void assertInterim(FiDimensions d, String businessArea) {
		assertEquals("11", d.getFund());
		assertEquals(businessArea, d.getBusinessArea());
		assertEquals(businessArea + "130000", d.getFundCentre());
		assertEquals("00301000000", d.getFunctionalArea());
	}

	@Test
	@DisplayName("Rule off: nothing is resolved and the ward master is never read")
	void ruleOffResolvesNothing() {
		ReflectionTestUtils.setField(receiptServiceV2, "collectionCfcDimensionsEnabled", false);

		assertNull(resolve(payment(WARD_C)));
		verify(demandRepository, never()).getCfcWardDimensions(any());
	}

	@Test
	@DisplayName("A CFC counter receipt posts at the CFC's ward, not the licensee's market")
	void theCollectingWardWins() {
		assertInterim(resolve(payment(WARD_C)), "4030");
		assertInterim(resolve(payment(WARD_KW)), "4130");
	}

	/**
	 * UAT receipts MARKET/26-27/000029, 031 and 032: taken by SUPERUSER user 330, who holds no CFC
	 * grant, so emarket-v1 wrote no collectingWardTenant. egcl_payment.tenantid said ward C, and the
	 * old fallback posted them at 4030. BMC: a superuser's collection posts at the market's ward.
	 */
	@Test
	@DisplayName("No collectingWardTenant (a superuser's receipt) posts at the licensee's market business area")
	void aReceiptWithNoCollectingWardPostsAtTheMarket() {
		assertInterim(resolve(payment(null)), "4120");
		assertInterim(resolve(paymentWithDetails(null)), "4120");
		verify(demandRepository, never()).getCfcWardDimensions(any());
	}

	@Test
	@DisplayName("A city-level collectingWardTenant names no counter: the market business area")
	void aCityLevelWardPostsAtTheMarket() {
		assertInterim(resolve(payment("mh.mumbai")), "4120");
		verify(demandRepository, never()).getCfcWardDimensions(any());
	}

	@Test
	@DisplayName("Unreadable additionalDetails posts at the market business area, it does not fail")
	void malformedAdditionalDetailsPostsAtTheMarket() {
		assertInterim(resolve(paymentWithDetails("{not json")), "4120");
	}

	@Test
	@DisplayName("A ward with no business area in the master posts at the market's")
	void unmappedWardPostsAtTheMarket() {
		assertInterim(resolve(payment("mh.mumbai.zone7.wardr_north")), "4120");
	}

	@Test
	@DisplayName("No ward and no market business area: nothing to build a fund centre from")
	void nothingToBuildOnResolvesNothing() {
		PaymentMarketInfo info = payment(null);
		info.setBusinessArea(null);
		assertNull(resolve(info));
	}
}
