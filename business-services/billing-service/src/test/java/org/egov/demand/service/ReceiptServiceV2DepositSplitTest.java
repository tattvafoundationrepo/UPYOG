package org.egov.demand.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.egov.demand.model.BillAccountDetailV2;
import org.egov.demand.model.BillDetailV2;
import org.egov.demand.model.BillV2;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.FiReport;
import org.egov.demand.model.GstAdvanceMap;
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
 * A receipt paying a deposit together with other demands (UAT MARKET/26-27/000126: transfer licence fee
 * 900 + deposit 4,098 + transfer fee 1,000) must credit Security Deposit with the deposit only, and clear
 * the other demands' receivable with the rest. Before the split the whole 5,998 went to 340100300.
 *
 * <p>The repository is a SPY over a real instance with no database, so the real voucher builder runs and
 * the per-head plan finds no open balances (today's single receivable line).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReceiptServiceV2DepositSplitTest {

	private static final String SECURITY_DEPOSIT = "340100300";
	private static final String RECEIVABLE = "431409936";
	private static final String CASH = "450100100";

	@Spy
	private DemandRepository demandRepository = new DemandRepository();

	@InjectMocks
	private ReceiptServiceV2 receiptServiceV2;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(demandRepository, "cashBankGlCode", CASH);
		ReflectionTestUtils.setField(demandRepository, "chequeBankGlCode", "450210010");
	}

	private static Demand demand(String id, String consumerCode, String businessService, String... headAmounts) {
		Demand d = new Demand();
		d.setId(id);
		d.setConsumerCode(consumerCode);
		d.setBusinessService(businessService);
		d.setTaxPeriodFrom(1775001600000L);
		d.setTaxPeriodTo(1806517799000L);
		List<DemandDetail> details = new ArrayList<>();
		for (int i = 0; i < headAmounts.length; i += 2) {
			DemandDetail dd = new DemandDetail();
			dd.setTaxHeadMasterCode(headAmounts[i]);
			dd.setTaxAmount(new BigDecimal(headAmounts[i + 1]));
			details.add(dd);
		}
		d.setDemandDetails(details);
		return d;
	}

	private static BillV2 bill(String demandId, String paid) {
		BillDetailV2 bd = new BillDetailV2();
		bd.setDemandId(demandId);
		bd.setAmountPaid(new BigDecimal(paid));
		BillV2 b = new BillV2();
		b.setBillDetails(Collections.singletonList(bd));
		return b;
	}

	/** The receipt header the service builds from the payment: market dimensions, receipt no., mode. */
	private static Demand receipt(Demand first) {
		Demand d = new Demand();
		d.setId(first.getId());
		d.setConsumerCode(first.getConsumerCode());
		d.setBusinessService(first.getBusinessService());
		d.setFund("11");
		d.setFundCenter("4130420103");
		d.setBusinessArea("4130");
		d.setFunctionalArea("55800000000");
		d.setPaymentMode("CASH");
		d.setFiReceiptNo("MARKET/26-27/000126");
		return d;
	}

	private static GstAdvanceMap paid(String total) {
		return GstAdvanceMap.builder().totalAmountPaid(new BigDecimal(total)).build();
	}

	private List<FiReport> split(List<Demand> demands, List<BillV2> bills, String total) {
		Map<String, Demand> byId = new HashMap<>();
		demands.forEach(d -> byId.put(d.getId(), d));
		return receiptServiceV2.buildDepositSplitRows(receipt(demands.get(0)), demands, bills, byId,
				new BigDecimal(total), paid(total), 1792175400000L);
	}

	private static BigDecimal sum(List<FiReport> rows, Predicate<FiReport> p) {
		return rows.stream().filter(p).map(FiReport::getCollectionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private static void assertBalanced(List<FiReport> rows) {
		assertEquals(0, sum(rows, r -> "40".equals(r.getPostingKey()))
				.compareTo(sum(rows, r -> "50".equals(r.getPostingKey()))), "Dr 40 must equal Cr 50");
	}

	@Test
	@DisplayName("Transfer receipt (lf 900 + df 4,098 + tf 1,000): only 4,098 to Security Deposit, 1,900 clears the receivable")
	void transferReceiptIsSplit() {
		Demand lf = demand("lf-1", "5888889000lf", "TX.Emarket_License_Fees", "TX.EMARKET_LICENSE_FEES", "900");
		Demand df = demand("df-1", "5888889000df", "TX.Emarket_Deposit_Fees", "DEPOSIT", "4098");
		Demand tf = demand("tf-1", "5888889000tf", "TX.Emarket_Transfer_Fees", "ADMINISTRATIVE", "1000");

		List<FiReport> rows = split(Arrays.asList(lf, df, tf),
				Arrays.asList(bill("lf-1", "900"), bill("df-1", "4098"), bill("tf-1", "1000")), "5998");

		assertBalanced(rows);
		assertEquals(0, new BigDecimal("4098").compareTo(sum(rows, r -> "50".equals(r.getPostingKey())
				&& SECURITY_DEPOSIT.equals(r.getGlCode()))));
		assertEquals(0, new BigDecimal("1900").compareTo(sum(rows, r -> "50".equals(r.getPostingKey())
				&& RECEIVABLE.equals(r.getGlCode()))));
		assertEquals(0, new BigDecimal("5998").compareTo(sum(rows, r -> "40".equals(r.getPostingKey())
				&& CASH.equals(r.getGlCode()))), "the bank still receives the whole receipt");
		// The deposit voucher names the deposit demand; the regular one a demand it clears.
		rows.stream().filter(r -> SECURITY_DEPOSIT.equals(r.getGlCode()))
				.forEach(r -> assertEquals("5888889000df", r.getReferenceNo()));
		rows.stream().filter(r -> RECEIVABLE.equals(r.getGlCode()))
				.forEach(r -> assertEquals("5888889000lf", r.getReferenceNo()));
		rows.forEach(r -> assertEquals("MARKET/26-27/000126", r.getDocumentHeaderText()));
	}

	@Test
	@DisplayName("Deposit listed first still splits: the regular voucher is referenced to a non-deposit demand")
	void depositFirstStillReferencesTheRegularDemand() {
		Demand df = demand("df-2", "5000007471df", "TX.Emarket_Deposit_Fees", "DEPOSIT", "1470");
		Demand tf = demand("tf-2", "5000007471tf", "TX.Emarket_Transfer_Fees", "PREMIUM", "14700", "DEBRIS", "500");

		List<FiReport> rows = split(Arrays.asList(df, tf), Arrays.asList(bill("df-2", "1470"), bill("tf-2", "15200")),
				"16670");

		assertBalanced(rows);
		assertEquals(0, new BigDecimal("1470").compareTo(sum(rows, r -> "50".equals(r.getPostingKey())
				&& SECURITY_DEPOSIT.equals(r.getGlCode()))));
		assertEquals(0, new BigDecimal("15200").compareTo(sum(rows, r -> "50".equals(r.getPostingKey())
				&& RECEIVABLE.equals(r.getGlCode()))));
		rows.stream().filter(r -> RECEIVABLE.equals(r.getGlCode()))
				.forEach(r -> assertEquals("5000007471tf", r.getReferenceNo()));
	}

	@Test
	@DisplayName("A pure deposit receipt is not split (null: posted whole as before)")
	void pureDepositIsLeftAlone() {
		Demand df = demand("df-3", "5000000001df", "TX.Emarket_Deposit_Fees", "DEPOSIT", "2340");
		assertNull(split(Collections.singletonList(df), Collections.singletonList(bill("df-3", "2340")), "2340"));
	}

	@Test
	@DisplayName("Bills that show nothing paid on the deposit are not guessed at: posted whole as before")
	void unknownDepositShareFallsBack() {
		Demand lf = demand("lf-4", "5000000002lf", "TX.Emarket_License_Fees", "TX.EMARKET_LICENSE_FEES", "900");
		Demand df = demand("df-4", "5000000002df", "TX.Emarket_Deposit_Fees", "DEPOSIT", "450");
		assertNull(split(Arrays.asList(lf, df), Collections.singletonList(bill("lf-4", "900")), "1350"));
	}

	@Test
	@DisplayName("Deposit share equal to the whole receipt leaves nothing to split")
	void nothingLeftAfterDeposit() {
		Demand lf = demand("lf-5", "5000000003lf", "TX.Emarket_License_Fees", "TX.EMARKET_LICENSE_FEES", "900");
		Demand df = demand("df-5", "5000000003df", "TX.Emarket_Deposit_Fees", "DEPOSIT", "450");
		assertNull(split(Arrays.asList(lf, df), Arrays.asList(bill("lf-5", "0"), bill("df-5", "450")), "450"));
	}

	@Test
	@DisplayName("Mirror of a split deposit receipt needs the posted document; with none it gives nothing back")
	void mirrorWithoutDatabaseIsEmpty() {
		assertTrue(demandRepository.mirrorSplitDepositCollection("MARKET/26-27/000126", "5888889000lf", null)
				.isEmpty());
	}

	private static FiReport posted(String key, String gl, String amount) {
		return FiReport.builder().postingKey(key).glCode(gl).collectionAmount(new BigDecimal(amount)).build();
	}

	@Test
	@DisplayName("Cancelling a split receipt mirrors its posted legs, whatever the flow says (the review blocker)")
	void cancellationMirrorsASplitReceipt() {
		List<FiReport> mirror = Arrays.asList(posted("40", SECURITY_DEPOSIT, "4098"), posted("50", CASH, "4098"),
				posted("40", RECEIVABLE, "1900"), posted("50", CASH, "1900"));
		doReturn(new ArrayList<FiReport>()).when(demandRepository).mirrorPerHeadCollection(anyString(), anyString(), any());
		doReturn(mirror).when(demandRepository).mirrorSplitDepositCollection(anyString(), anyString(), any());

		List<FiReport> rows = receiptServiceV2.mirroredReversal(receipt(
				demand("lf-1", "5888889000lf", "TX.Emarket_License_Fees")), null);

		assertEquals(mirror, rows);
		assertEquals(0, new BigDecimal("4098").compareTo(sum(rows, r -> "40".equals(r.getPostingKey())
				&& SECURITY_DEPOSIT.equals(r.getGlCode()))), "the deposit leaves Security Deposit");
		assertBalanced(rows);
	}

	@Test
	@DisplayName("Per-head legs are mirrored first; the deposit check is then not needed")
	void perHeadMirrorWins() {
		List<FiReport> perHead = Arrays.asList(posted("40", "431409940", "900"), posted("50", CASH, "900"));
		doReturn(perHead).when(demandRepository).mirrorPerHeadCollection(anyString(), anyString(), any());

		assertEquals(perHead, receiptServiceV2.mirroredReversal(receipt(
				demand("lf-1", "5888889000lf", "TX.Emarket_License_Fees")), null));
		verify(demandRepository, never()).mirrorSplitDepositCollection(anyString(), anyString(), any());
	}

	@Test
	@DisplayName("Nothing to mirror (unsplit or ordinary receipt): empty, and the caller rebuilds as before")
	void nothingToMirror() {
		doReturn(new ArrayList<FiReport>()).when(demandRepository).mirrorPerHeadCollection(anyString(), anyString(), any());
		doReturn(new ArrayList<FiReport>()).when(demandRepository).mirrorSplitDepositCollection(anyString(), anyString(), any());
		assertTrue(receiptServiceV2.mirroredReversal(receipt(
				demand("df-9", "5000000009df", "TX.Emarket_Deposit_Fees")), null).isEmpty());
	}

	@Test
	@DisplayName("A bill detail without amountPaid gives the deposit share from its lines' adjusted amounts")
	void depositShareFromLineAmounts() {
		Demand lf = demand("lf-6", "5000000006lf", "TX.Emarket_License_Fees", "TX.EMARKET_LICENSE_FEES", "900");
		Demand df = demand("df-6", "5000000006df", "TX.Emarket_Deposit_Fees", "DEPOSIT", "450");
		BillAccountDetailV2 line = new BillAccountDetailV2();
		line.setAdjustedAmount(new BigDecimal("450"));
		BillDetailV2 bd = new BillDetailV2();
		bd.setDemandId("df-6");
		bd.setBillAccountDetails(Collections.singletonList(line));
		BillV2 depositBill = new BillV2();
		depositBill.setBillDetails(Collections.singletonList(bd));

		List<FiReport> rows = split(Arrays.asList(lf, df), Arrays.asList(bill("lf-6", "900"), depositBill), "1350");

		assertBalanced(rows);
		assertEquals(0, new BigDecimal("450").compareTo(sum(rows, r -> "50".equals(r.getPostingKey())
				&& SECURITY_DEPOSIT.equals(r.getGlCode()))));
	}
}
