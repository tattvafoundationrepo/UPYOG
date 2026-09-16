/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.demand.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.el.ArrayELResolver;

import org.apache.kafka.common.protocol.types.Field.Str;
import org.egov.demand.model.AdvSettlement;
import org.egov.demand.model.AuditDetails;
import org.egov.demand.model.CollectedReceipt;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.FiDimensions;
import org.egov.demand.model.FiFlow;
import org.egov.demand.model.MergedDemand;
import org.egov.demand.model.FiReport;
import org.egov.demand.model.FiReportType;
import org.egov.demand.model.FiReportRequest;
import org.egov.demand.model.GstAdvanceMap;
import org.egov.demand.model.PaymentBackUpdateAudit;
import org.egov.demand.model.PaymentMarketInfo;
import org.egov.demand.producer.Producer;
import org.egov.demand.repository.querybuilder.DemandQueryBuilder;
import org.egov.demand.repository.rowmapper.CollectedReceiptsRowMapper;
import org.egov.demand.repository.rowmapper.DemandRowMapper;
import org.egov.demand.repository.rowmapper.MergedDemandRowMapper;
import org.egov.demand.util.Util;
import org.egov.demand.web.contract.DemandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.web.mappings.servlet.FilterRegistrationMappingDescription;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class DemandRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DemandQueryBuilder demandQueryBuilder;
	
	@Autowired
	private DemandRowMapper demandRowMapper;

	@Autowired
	private CollectedReceiptsRowMapper collectedReceiptRowMapper;

	@Autowired
	private MergedDemandRowMapper mergedDemandRowMapper;
	
	@Autowired
	private Util util;
    
	@Autowired
	private Producer producer;

	/**
	 * "Receivable from Mun Mkt". Collection and demand-reversal have always used
	 * 431409936; demand creation was left on the superseded 431190300, so the two
	 * sides could never reconcile. Property-driven so the change can be reverted
	 * without a deploy — see the plan's rollback note.
	 */
	@org.springframework.beans.factory.annotation.Value("${emarket.fi.receivable.gl:431409936}")
	private String receivableGlCode = "431409936"; // field default keeps non-Spring construction (tests) on the same GL

	/**
	 * The money leg of a collection voucher, split by how the money arrived.
	 *
	 * <p>BMC: "where collection is made by cheque, the generated CSV reflects it as an interim
	 * receipt done by cash. This is to be corrected", and a cheque is to carry its own GL wherever
	 * the GL is shown. Cash keeps the interim account it has always used; a cheque or DD is not
	 * money in the bank until it clears, so it is held in cheques-in-hand instead.
	 *
	 * <p>Resolved at POSTING time, not at report time: the stored FI row, the CSV built from it and
	 * the document SAP receives all carry the same account. Property-driven so either code can be
	 * corrected without a code change, with field defaults so non-Spring construction (tests) keeps
	 * the same GLs.
	 */
	@org.springframework.beans.factory.annotation.Value("${emarket.fi.bank.cash.gl:450100100}")
	private String cashBankGlCode = "450100100";

	@org.springframework.beans.factory.annotation.Value("${emarket.fi.bank.cheque.gl:450210010}")
	private String chequeBankGlCode = "450210010";

	/**
	 * The SAP dimensions of the interim receipt, the money leg of a collection voucher (cash
	 * interim or cheques-in-hand). BMC: "in the interim receipt entry functional area is fixed
	 * 00301000000 and the fund centre is business area + 130000; business area is the ward of the
	 * CFC where the money is collected".
	 *
	 * <p>This matches BMC's own SAP collection upload (NeedClarification.xlsx, the CV-system daily
	 * collection file). There, 450210010 / 450100100 post at Fund Centre 4090130000, Functional Area
	 * 00301000000 and Business Area 4090, which is the collecting CFC. The receivable and revenue
	 * legs of the same document stay on the property's own ward, e.g. 4100240000 / 4100. So a
	 * document that carries two business areas is normal in their SAP client.
	 *
	 * <p>Kept as strings, because the functional area's leading zeros are part of the value
	 * ("11 digits").
	 */
	@org.springframework.beans.factory.annotation.Value("${emarket.fi.collection.interim.functional.area:00301000000}")
	private String interimReceiptFunctionalArea = "00301000000";

	@org.springframework.beans.factory.annotation.Value("${emarket.fi.collection.interim.fund.centre.suffix:130000}")
	private String interimReceiptFundCentreSuffix = "130000";

	/**
	 * A regular (non-advance) collection on a GST-bearing demand debits Bank for the FULL cash
	 * received and credits the receivable for the same — no CGST/SGST legs (BMC direction,
	 * 2026-08-25). The GST liability was recognised when the demand was raised and is settled to
	 * Government by a separate remittance, so the collection voucher must not touch the payable.
	 * The previous shape banked the net and debited the payable, which left 350200421/422 with a
	 * debit balance and the bank short by the tax on every receipt. False restores that shape.
	 */
	@org.springframework.beans.factory.annotation.Value("${emarket.fi.regular.collection.gross.bank.enabled:true}")
	private boolean grossBankOnRegularCollection = true;

	/**
	 * Post the per-tax-head 4-Series pair on a demand raised against an advance: Dr the head's
	 * receivable account (431409937..431409977), Cr the head's revenue account, for the amount that
	 * head settled out of the advance. Everything else about the voucher is unchanged, so 350410215
	 * still takes the full settled amount and still closes to nil over an advance cycle.
	 *
	 * <p>Default OFF. The pair credits the head's revenue GL a SECOND time, on top of the credit the
	 * voucher already posts, so with it on the ledger recognises the month's rent twice. That is what
	 * BMC's reference entry shows and it may be how they write a summary line rather than what they
	 * want posted — every automated balance assertion passes either way, so no test can settle it.
	 * Turn this on only once BMC Finance has confirmed the credit leg.
	 *
	 * <p>OFF restores byte-identical output on a database where the flag has never been on. It is
	 * a kill switch for the WRITE only — the unwind in buildFourSeriesReversalFiReports is
	 * deliberately ungated, so a pair posted while this was on is still given back if a receipt
	 * is later cancelled with it off. Gating both would strand the pair for ever.
	 */
	@org.springframework.beans.factory.annotation.Value("${emarket.fi.advance.four.series.enabled:false}")
	private boolean advanceFourSeriesEnabled = false;

	/** GL codes for the GST-advance netting voucher (Accounting Entries 2025, entry 4). */
	/**
	 * The four synthetic, FI-only heads appended by the GST net-off block. Matched exactly:
	 * deriving the posting key from a substring ("40"/"50") would silently reclassify any real
	 * tax head whose code happened to contain those digits, giving it the wrong posting key and
	 * dropping it out of the balancing receivable. No current head does, so this is identical
	 * in behaviour today and stays correct if one is ever added.
	 */
	private static final Set<String> NETTING_DEBIT_HEADS =
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList("CSP40", "SSP40")));
	private static final Set<String> NETTING_CREDIT_HEADS =
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList("CSA50", "SSA50")));

	private static final String GL_CGST_PAYABLE = "350200421";
	private static final String GL_SGST_PAYABLE = "350200422";
	private static final String GL_CGST_ADVANCE = "439300200";
	private static final String GL_SGST_ADVANCE = "439300201";
	/** Rental/licence advance held against future demands. */
	private static final String GL_ADVANCE = "350410215";

	/** Only rental demands carry GST that can be netted against an advance. */
	private static final String BS_RENTAL = "TX.Emarket_Rental_Fees";

	public List<Demand> getDemands(DemandCriteria demandCriteria) {

		List<Object> preparedStatementValues = new ArrayList<>();
		String searchDemandQuery = demandQueryBuilder.getDemandQuery(demandCriteria, preparedStatementValues);
		return jdbcTemplate.query(searchDemandQuery, preparedStatementValues.toArray(), demandRowMapper);
	}

	public List<MergedDemand> getMergedDemands(DemandCriteria demandCriteria) {

		List<Object> preparedStatementValues = new ArrayList<>();
		String query = demandQueryBuilder.getMergedDemandQuery(demandCriteria, preparedStatementValues);

		log.debug("Merged demand query: {}", query);
		log.debug("Prepared statement values: {}", preparedStatementValues);

		return jdbcTemplate.query(query, preparedStatementValues.toArray(), mergedDemandRowMapper);
	}

	public List<CollectedReceipt> getCollectedReceipts(DemandCriteria demandCriteria, Boolean isMerged) {
    
		List<Object> preparedStatementValues = new ArrayList<>();
		String query = demandQueryBuilder.getCollectedReceiptsQuery(demandCriteria, preparedStatementValues, isMerged);
		
		log.debug("Collected receipts query: " + query);
		log.debug("Prepared statement values: " + preparedStatementValues);
		
		return jdbcTemplate.query(query, preparedStatementValues.toArray(), collectedReceiptRowMapper);
	}
	
	/**
	 * Fetches demand from DB based on a map of business code and set of consumer codes
	 * 
	 * @param businessConsumercodeMap
	 * @param tenantId
	 * @return
	 */
	public List<Demand> getDemandsForConsumerCodes(Map<String, Set<String>> businessConsumercodeMap, String tenantId) {

		List<Object> presparedStmtList = new ArrayList<>();
		String sql = demandQueryBuilder.getDemandQueryForConsumerCodes(businessConsumercodeMap, presparedStmtList,
				tenantId);
		return jdbcTemplate.query(sql, presparedStmtList.toArray(), demandRowMapper);
	}

	@Transactional
	public void save(DemandRequest demandRequest) {

		log.debug("DemandRepository save, the request object : " + demandRequest);
		List<Demand> demands = demandRequest.getDemands();
		List<DemandDetail> demandDetails = new ArrayList<>();
		List<FiReport> reportList  = new ArrayList<>();
		
		for (Demand demand : demands) {
			demandDetails.addAll(demand.getDemandDetails());
		}
		
		insertBatch(demands, demandDetails);
		log.debug("Demands saved >>>> ");
		insertBatchForAudit(demands, demandDetails);
        

        fetchDemandSeqNoByDemandIds(
            demands.stream()
                .map(Demand::getId)
                .collect(Collectors.toSet())
        ).forEach( (demandId, seqNo) -> {
            demands.stream()
                .filter(d -> d.getId().equals(demandId))
                .forEach( d -> d.setDemandSeqNo(seqNo) );
        });

        // GST advance released by demands EARLIER IN THIS BATCH, keyed by consumer+GL. The cap
        // below reads committed FI rows, but nothing in this request is written until after the
        // loop — so without this running tally every demand in a multi-month batch would see the
        // same full balance and they would jointly over-release. Reachable in production:
        // RestorationService.unblock posts one rental demand per intervening month for a single
        // licensee in one DemandRequest.
        Map<String, BigDecimal> releasedInThisBatch = new HashMap<>();

        for (Demand demand : demands) {

            // GST netting-off voucher (Accounting Entries 2025, entry 4): a demand raised
            // against a previously-taxed advance must square its GST liability against the
            // GST already remitted to Government, otherwise the tax is paid twice.
            //
            // The amount netted is the GST on THIS demand that was actually settled out of
            // the advance, capped at the GST advance still unreleased for this licensee.
            // That cap matters: legacy-migrated advances carry cash but no 439300200 asset
            // (LegacyFinancialRepository writes no FI row), so an uncapped netting would
            // credit an asset that never existed and drive the GL negative.
            // Held locally, never appended to the caller's Demand. These four heads are
            // FI-only: DemandService.create returns these same objects in the /demand/_create
            // response and pushes them to the demand-index topic, so mutating them would ship
            // four phantom tax heads to every caller and inflate the indexed demand total.
            List<DemandDetail> nettingDetails = new ArrayList<>();
            // Resolved at most once per demand and reused for both the clearing key and the doc-date
            // clamp. It was previously looked up twice, and the lookup is a scan of the collection
            // table, so a twelve-month restoration batch paid for it twenty-four times.
            AdvanceReceiptRef advanceRef = null;

            if (BS_RENTAL.equalsIgnoreCase(demand.getBusinessService()) && demand.isApportionedAgainstAdvance()) {

                // The batch tally below covers demands within THIS request; this lock covers
                // concurrent requests for the same licensee, which would otherwise both read
                // the full balance and jointly over-release it.
                lockAdvanceForConsumer(demand.getConsumerCode());

                BigDecimal cgstNet = cappedGstNetOff(demand, "CGST", GL_CGST_ADVANCE, releasedInThisBatch);
                BigDecimal sgstNet = cappedGstNetOff(demand, "SGST", GL_SGST_ADVANCE, releasedInThisBatch);

                // An intra-state supply is taxed half CGST, half SGST, so the two legs must
                // release the same amount. Their caps are computed against two independent GL
                // balances (439300200 / 439300201) which can diverge — a one-sided legacy row, or
                // a correction posted to one GL only. Releasing different amounts would file a
                // GSTR-1 whose CGST and SGST disagree, which the portal rejects.
                //
                // Gated on whether the demand CARRIES both heads — not on the caps, and not on how
                // much of each was settled. Both of those are already zero in the very cases this
                // exists to catch: apportionment drains buckets in ascending-amount order, so a
                // nearly-spent advance routinely settles CGST in part and leaves SGST at zero, and
                // an exhausted advance GL caps its side at zero. Testing either would let exactly
                // that asymmetry through. A demand carrying only one component has no symmetry to
                // enforce and keeps its existing treatment.
                if (carriesGstComponent(demand, "CGST") && carriesGstComponent(demand, "SGST")
                        && cgstNet.compareTo(sgstNet) != 0) {
                    BigDecimal symmetric = cgstNet.min(sgstNet);
                    log.warn("Asymmetric GST advance for licence {} (consumer {}): cgst cap {} vs sgst cap {}; "
                            + "both legs released at {} to keep the return internally consistent",
                            licenceKey(demand.getConsumerCode()), demand.getConsumerCode(),
                            cgstNet, sgstNet, symmetric);
                    cgstNet = symmetric;
                    sgstNet = symmetric;
                }

                recordBatchRelease(releasedInThisBatch, demand, GL_CGST_ADVANCE, cgstNet);
                recordBatchRelease(releasedInThisBatch, demand, GL_SGST_ADVANCE, sgstNet);

                // The advance receipt this demand drew on — its number is SAP's clearing key (ZUONR).
                advanceRef = getAdvanceReceipt(demand.getId());
                String advanceDocNo = advanceRef.documentNo;

                addNettingDetail(nettingDetails, demand, "CSP40", GL_CGST_PAYABLE, cgstNet, advanceDocNo);
                addNettingDetail(nettingDetails, demand, "SSP40", GL_SGST_PAYABLE, sgstNet, advanceDocNo);
                addNettingDetail(nettingDetails, demand, "CSA50", GL_CGST_ADVANCE, cgstNet, advanceDocNo);
                addNettingDetail(nettingDetails, demand, "SSA50", GL_SGST_ADVANCE, sgstNet, advanceDocNo);

                log.info("GST net-off for demand {} consumer {}: cgst={} sgst={} advanceDoc={}",
                        demand.getId(), demand.getConsumerCode(), cgstNet, sgstNet, advanceDocNo);
            }


		// 	demand.getDemandDetails().add(DemandDetail.builder()
		//         .demandId(demand.getId())
        //         .taxAmount(totalTaxAmount)
        //         .taxHeadMasterCode("Customer "+demand.getConsumerCode())
        //         .additionalDetails(demand.getAdditionalDetails())
        //         .build());

			//reportList.addAll(buildFiReportsFromDemand(demand , "50", false , null));
			if (!"TX.Emarket_Deposit_Fees".equalsIgnoreCase(demand.getBusinessService())) {
			    List<FiReport> demandFiReports = buildDemandFiReports(demand, nettingDetails, advanceRef);
			    // Label only (accounting rows unchanged). Demand FI rows, inserted via
			    // batchInsertDemandFiReports. Report type:
			    //   - new demand apportioned against a previously-collected advance -> demand against advance
			    //     (flagged in DemandService.apportionAdvanceIfExist);
			    //   - dishonour demand -> discheque;
			    //   - otherwise -> demand.
			    String fiReportType;
			    if (demand.isApportionedAgainstAdvance()) {
			        fiReportType = FiReportType.UPMKT_DEMDADV;
			    } else if ("TX.Emarket_Dishonor_Fees".equalsIgnoreCase(demand.getBusinessService())) {
			        fiReportType = FiReportType.UPMKT_DISCHQ;
			    } else {
			        fiReportType = FiReportType.UPMKT_DEMD;
			    }
			    // Guarded on null rather than applied blanketly. Every row buildDemandFiReports
			    // builds arrives untyped and still takes fiReportType, so nothing about the
			    // existing output changes. The 4-Series pair is the one exception: it tags itself
			    // UPMKT_DEMDADV_4S, and overwriting that with UPMKT_DEMDADV would put both of its
			    // legs inside the GST return's report_type filter — filing a taxable supply against
			    // a receivable account and double-counting the revenue leg.
			    demandFiReports.forEach(r -> {
			        if (r.getReportType() == null) {
			            r.setReportType(fiReportType);
			        }
			    });
			    reportList.addAll(demandFiReports);
			}
		}

		if(!reportList.isEmpty()){
            batchInsertDemandFiReports(reportList);
		}

	}



	public List<FiReport> buildFiReportsFromDemand(Demand demand,
                                              String key,
                                              Boolean isCollection,
                                              GstAdvanceMap advanceMap) {

    final Long periodFrom = demand.getTaxPeriodFrom();
	List<String> advanceTaxHeadLists = new ArrayList<>();											
    final String consumerCode = demand.getConsumerCode();
    final long now = System.currentTimeMillis();
	String fund;
	String fundCenter;
	String businessArea;
	String functionalArea;
    if(!isCollection) {
       Map<String,String> additionalMarketDetails = new HashMap<>();
	   Object additiaonalsObj = demand.getAdditionalDetails();
       if (additiaonalsObj instanceof Map) {
          additionalMarketDetails = (Map) additiaonalsObj;
       } 
	    fund = additionalMarketDetails.get("fund");
	    fundCenter = additionalMarketDetails.get("fundCenter");
        businessArea = additionalMarketDetails.get("businessArea");
		functionalArea = additionalMarketDetails.get("functionalArea");
	}else{

		fund = demand.getFund();
        fundCenter = demand.getFundCenter();
        businessArea = demand.getBusinessArea();
		functionalArea = demand.getFunctionalArea();

	}
    log.info("FUNDS >>> fund=" + fund + " | fc=" + fundCenter + " | ba=" + businessArea);

	Boolean hasAdvanceTaxhead = false;

	for(DemandDetail d: demand.getDemandDetails()){
       if(d.getTaxHeadMasterCode().contains("ADVANCE")){
          hasAdvanceTaxhead = true;
	   }
		 
	}


    // If advanceMap provided, append demandDetails for CGST/SGST and ADV_CGST/ADV_SGST
    if (advanceMap != null  && hasAdvanceTaxhead ) {
        if(advanceMap.getCgstAmount() != null && advanceMap.getSgstAmount() != null){

		
        Map<String, Object> cgstMap = new HashMap<>();
        cgstMap.put("glcode", advanceMap.getCgstGlCode() != null ? advanceMap.getCgstGlCode() : "350200421");

        Map<String, Object> sgstMap = new HashMap<>();
        sgstMap.put("glcode", advanceMap.getSgstGlCode() != null ? advanceMap.getSgstGlCode() : "350200422");

        Map<String, Object> advCgstMap = new HashMap<>();
        advCgstMap.put("glcode", advanceMap.getCgstGlCode() != null ? advanceMap.getCgstGlCode() : "439300200");

        Map<String, Object> advSgstMap = new HashMap<>();
        advSgstMap.put("glcode", advanceMap.getSgstGlCode() != null ? advanceMap.getSgstGlCode() : "439300201");

        // Add CGST Payable
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getCgstAmount())
                .taxHeadMasterCode("CGST Payable")
                .additionalDetails(cgstMap)
                .build());

        // Add SGST Payable
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getSgstAmount())
                .taxHeadMasterCode("SGST Payable")
                .additionalDetails(sgstMap)
                .build());

        // Add ADV_CGST (payment side)
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getCgstAmount())
                .taxHeadMasterCode("ADV_CGST")
                .additionalDetails(advCgstMap)
                .build());

        // Add ADV_SGST (payment side)
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getSgstAmount())
                .taxHeadMasterCode("ADV_SGST")
                .additionalDetails(advSgstMap)
                .build());
            advanceTaxHeadLists.add("ADV_SGST");
            advanceTaxHeadLists.add("ADV_CGST");
            advanceTaxHeadLists.add("CGST Payable");
            advanceTaxHeadLists.add("SGST Payable");				
    }
}

    // Stream, filter and map to FiReport
    return demand.getDemandDetails()
            .stream()
            // Skip GST lines where taxAmount == collectionAmount (null-safe)
            .filter(detail -> {
                BigDecimal taxAmt = detail.getTaxAmount() == null ? BigDecimal.ZERO : detail.getTaxAmount();
                BigDecimal collAmt = detail.getCollectionAmount() == null ? BigDecimal.ZERO : detail.getCollectionAmount();

                if (detail.getTaxHeadMasterCode() != null &&
                        detail.getTaxHeadMasterCode().contains("GST") &&
                        taxAmt.compareTo(collAmt) == 0 && !isCollection) {
                    return false;
                }
                return true;
            })
            .map(detail -> {
                // Extract GL code if present in additionalDetails
                String glCode = null;
                Object addDetailsObj = detail.getAdditionalDetails();
                if (addDetailsObj instanceof Map) {
                    Object gl = ((Map<?, ?>) addDetailsObj).get("glcode");
                    if (gl != null) glCode = gl.toString();
                }

                // Handle ADVANCE detail adjustments (null-safe)
                if (detail.getTaxHeadMasterCode() != null &&
                        detail.getTaxHeadMasterCode().contains("ADVANCE")) {

                    BigDecimal coll = detail.getCollectionAmount() == null ? BigDecimal.ZERO : detail.getCollectionAmount();
                    BigDecimal tax = detail.getTaxAmount() == null ? BigDecimal.ZERO : detail.getTaxAmount();
                    
					detail.setCollectionAmount(coll.abs());
                    detail.setTaxAmount(tax.abs());
                    glCode = "450100100";  
                }
                String remark = null;
                // Choose postingKey
                String postingKey;
                if (detail.getTaxHeadMasterCode() != null && detail.getTaxHeadMasterCode().contains("ADVANCE")) {
                    postingKey = "39";
                } else if (advanceTaxHeadLists.contains(detail.getTaxHeadMasterCode())) {
					if(detail.getTaxHeadMasterCode().contains("Payable") ){
                       postingKey = "50";
					   remark = "Demand";
					} 
					else
					   postingKey = "40";
                }else if(detail.getTaxHeadMasterCode().contains("Customer") ) {
                       postingKey = isCollection ? "19" : "01" ;  
				} else {
                       postingKey = key;
                }

                // Determine GL code for collections: if it's an advance/GST head use detail's gl else default collection gl
                // String resolvedGlCode;
                // if (isCollection) {
                //     if (advanceTaxHeadLists.contains(detail.getTaxHeadMasterCode())) {
                //         resolvedGlCode = glCode; // use provided gl from demandDetail.additionalDetails
                //     } else {
                //         resolvedGlCode = "450100100"; // default collection GL
                //     }
                // } else {
                //     resolvedGlCode = glCode;
                // }

                // Determine amount to set
                // BigDecimal resolvedAmount;
                // if (isCollection) {
                //     if (detail.getTaxHeadMasterCode() != null && detail.getTaxHeadMasterCode().contains("ADVANCE")) {
                //         resolvedAmount = detail.getTaxAmount() == null ? BigDecimal.ZERO : detail.getTaxAmount();
                //     } else {
                //         resolvedAmount = detail.getCollectionAmount() == null ? BigDecimal.ZERO : detail.getCollectionAmount();
                //     }
                // } else {
                //     resolvedAmount = detail.getTaxAmount() == null ? BigDecimal.ZERO : detail.getTaxAmount();
                // }

                // Build FiReport using demand getters for fund/fundCenter/businessArea
                return FiReport.builder()
                        .transactionNumber(detail.getDemandId())
                        .docDate(periodFrom)
                        .postingDate(periodFrom)
                        .referenceNo(consumerCode)
                        .documentHeaderText(detail.getTaxHeadMasterCode())
                        .postingKey(postingKey)
                        .glCode(glCode)
                        .collectionAmount(detail.getTaxAmount())
                        .fund(fund)
                        .fundCentre(fundCenter)
                        .businessArea(businessArea)
                        .functionalArea(functionalArea)
                        .isNew(Boolean.TRUE)
                        .paymentModeDetails(demand.getPaymentMode())
                        .createdAt(System.currentTimeMillis())
                        .updatedAt(System.currentTimeMillis())
                        .remarks(isCollection ? remark != null ? remark :" Collection " : " Demand")
                        .build();
            })
            .collect(Collectors.toList());
}


	public void batchInsertDemandFiReports(List<FiReport> reports) {

    if (reports == null || reports.isEmpty()) return;

        String sql =
        "INSERT INTO public.eg_emarket_fi_report ("
        + " transaction_number, doc_date, posting_date,"
        + " reference_no, document_header_text,"
        + " posting_key, gl_code, collection_amount, "
        + " fund, fund_centre,"
        + " functional_area, business_area,"
        + " remarks, payment_mode_details, is_new,"
        + " created_at, updated_at, doc_type, cost_center, commitmentitem, report_type, assignment "
        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ? , ? , ? , ?)";


    jdbcTemplate.batchUpdate(sql, reports, 100, (ps, r) -> {

        ps.setString(1, r.getTransactionNumber() == null ? null : String.valueOf(r.getTransactionNumber()));
        ps.setObject(2, r.getDocDate());
        ps.setObject(3, r.getPostingDate());

        ps.setString(4, r.getReferenceNo());
        ps.setString(5, r.getDocumentHeaderText());

        ps.setString(6, r.getPostingKey());
        ps.setString(7, r.getGlCode());
        ps.setBigDecimal(8, r.getCollectionAmount());

        ps.setString(9, r.getFund());
        ps.setString(10, r.getFundCentre());
        ps.setString(11, r.getFunctionalArea());
        ps.setString(12, r.getBusinessArea());

        ps.setString(13, r.getRemarks());
        ps.setString(14, r.getPaymentModeDetails());
        ps.setObject(15, r.getIsNew());

        ps.setTimestamp(16, r.getCreatedAt() == null ? null : new Timestamp(r.getCreatedAt()));
        ps.setTimestamp(17, r.getUpdatedAt() == null ? null : new Timestamp(r.getUpdatedAt()));
        ps.setString(18,    r.getDocType());
        ps.setString(19,    r.getCostCenter());
        ps.setString(20,    r.getCommitmentItem());
        ps.setString(21,    r.getReportType());
        ps.setString(22,    r.getAssignment());
    });

    log.info("Batch inserted Demand {} FI Report records", reports.size());
}






	public void batchInsertCollectionFiReports(List<FiReport> reports) {

    if (reports == null || reports.isEmpty()) return;

        String sql =
        "INSERT INTO public.eg_emarket_fi_report_collection ("
        + " transaction_number, doc_date, posting_date,"
        + " reference_no, document_header_text,"
        + " posting_key, gl_code, collection_amount,"
        + " fund, fund_centre,"
        + " functional_area, business_area,"
        + " remarks, payment_mode_details, is_new,"
        + " created_at, updated_at, doc_type, cost_center, commitmentitem, report_type, assignment "
        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ? , ? , ? , ?)";


    jdbcTemplate.batchUpdate(sql, reports, 100, (ps, r) -> {

        ps.setString(1, r.getTransactionNumber() == null ? null : String.valueOf(r.getTransactionNumber()));
        ps.setObject(2, r.getDocDate());
        ps.setObject(3, r.getPostingDate());

        ps.setString(4, r.getReferenceNo());
        ps.setString(5, r.getDocumentHeaderText());

        ps.setString(6, r.getPostingKey());
        ps.setString(7, r.getGlCode());
        ps.setBigDecimal(8, r.getCollectionAmount());

        ps.setString(9, r.getFund());
        ps.setString(10, r.getFundCentre());
        ps.setString(11, r.getFunctionalArea());
        ps.setString(12, r.getBusinessArea());

        ps.setString(13, r.getRemarks());
        ps.setString(14, r.getPaymentModeDetails());
        ps.setObject(15, r.getIsNew());

        ps.setTimestamp(16, r.getCreatedAt() == null ? null : new Timestamp(r.getCreatedAt()));
        ps.setTimestamp(17, r.getUpdatedAt() == null ? null : new Timestamp(r.getUpdatedAt()));
        ps.setString(18,    r.getDocType());
        ps.setString(19,    r.getCostCenter());
        ps.setString(20,    r.getCommitmentItem());
        ps.setString(21,    r.getReportType());
        ps.setString(22,    r.getAssignment());
    });

    log.info("Batch inserted {} Collection FI Report records", reports.size());
}


	
	@Transactional
	public void update(DemandRequest demandRequest, PaymentBackUpdateAudit paymentBackUpdateAudit) {

		List<Demand> demands = demandRequest.getDemands();
		List<Demand> oldDemands = new ArrayList<>();
		List<DemandDetail> oldDemandDetails = new ArrayList<>();
		List<Demand> newDemands = new ArrayList<>();
		List<DemandDetail> newDemandDetails = new ArrayList<>();

		DemandCriteria demandCriteria = DemandCriteria.builder()
				.demandId(demands.stream().map(Demand::getId).collect(Collectors.toSet()))
				.tenantId(demands.get(0).getTenantId()).build();
		List<Demand> existingDemands = getDemands(demandCriteria);
		
		log.debug("repository demands "+existingDemands);
		Map<String, String> existingDemandMap = existingDemands.stream().collect(
						Collectors.toMap(Demand::getId, Demand::getId));
		Map<String, String> existingDemandDetailMap = new HashMap<>();
		for (Demand demand : existingDemands) {
			for (DemandDetail demandDetail : demand.getDemandDetails())
				existingDemandDetailMap.put(demandDetail.getId(), demandDetail.getId());
		}

		for (Demand demand : demands) {
			if (existingDemandMap.get(demand.getId()) == null)
				newDemands.add(demand);
			else
				oldDemands.add(demand);
			for (DemandDetail demandDetail : demand.getDemandDetails()) {
				if (existingDemandDetailMap.get(demandDetail.getId()) == null)
					newDemandDetails.add(demandDetail);
				else
					oldDemandDetails.add(demandDetail);
			}
		}
		
		updateBatch(oldDemands, oldDemandDetails);
		insertBatchForAudit(oldDemands, oldDemandDetails);
		
		if (!newDemands.isEmpty() || !newDemandDetails.isEmpty()) {
			
			insertBatch(newDemands, newDemandDetails);
			insertBatchForAudit(newDemands, newDemandDetails);
		}
		
		if (null != paymentBackUpdateAudit)
			insertBackUpdateForPayment(paymentBackUpdateAudit);
	}

	public void insertBatch(List<Demand> newDemands, List<DemandDetail> newDemandDetails) {

		jdbcTemplate.batchUpdate(DemandQueryBuilder.DEMAND_INSERT_QUERY, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int rowNum) throws SQLException {
				
				Demand demand = newDemands.get(rowNum);
				String status = demand.getStatus() != null ? demand.getStatus().toString() : null;
				AuditDetails auditDetail = demand.getAuditDetails();
				String payerUuid = null != demand.getPayer() ? demand.getPayer().getUuid() : null;
				ps.setString(1, demand.getId());
				ps.setString(2, demand.getConsumerCode());
				ps.setString(3, demand.getConsumerType());
				ps.setString(4, demand.getBusinessService());
				ps.setString(5, payerUuid);
				ps.setLong(6, demand.getTaxPeriodFrom());
				ps.setLong(7, demand.getTaxPeriodTo());
				ps.setBigDecimal(8, demand.getMinimumAmountPayable());
				ps.setString(9, auditDetail.getCreatedBy());
				ps.setString(10, auditDetail.getLastModifiedBy());
				ps.setLong(11, auditDetail.getCreatedTime());
				ps.setLong(12, auditDetail.getLastModifiedTime());
				ps.setString(13, demand.getTenantId());
				ps.setString(14, status);
				ps.setObject(15, util.getPGObject(demand.getAdditionalDetails()));
				ps.setObject(16, demand.getBillExpiryTime());
				ps.setObject(17, null);
				ps.setBoolean(18, demand.getIsAdvance() != null ? demand.getIsAdvance() : false);
				ps.setInt(19, demand.getAdvanceIndex() != null ? demand.getAdvanceIndex() : 0);
			}

			@Override
			public int getBatchSize() {
				return newDemands.size();
			}
		});

		jdbcTemplate.batchUpdate(DemandQueryBuilder.DEMAND_DETAIL_INSERT_QUERY, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int rowNum) throws SQLException {
				
				DemandDetail demandDetail = newDemandDetails.get(rowNum);
				AuditDetails auditDetail = demandDetail.getAuditDetails();
				ps.setString(1, demandDetail.getId());
				ps.setString(2, demandDetail.getDemandId());
				ps.setString(3, demandDetail.getTaxHeadMasterCode());
				ps.setBigDecimal(4, demandDetail.getTaxAmount());
				ps.setBigDecimal(5, demandDetail.getCollectionAmount());
				ps.setString(6, auditDetail.getCreatedBy());
				ps.setString(7, auditDetail.getLastModifiedBy());
				ps.setLong(8, auditDetail.getCreatedTime());
				ps.setLong(9, auditDetail.getLastModifiedTime());
				ps.setString(10, demandDetail.getTenantId());
				ps.setObject(11, util.getPGObject(demandDetail.getAdditionalDetails()));
			}

			@Override
			public int getBatchSize() {
				return newDemandDetails.size();
			}
		});
	}
	
	public void updateBatch(List<Demand> oldDemands, List<DemandDetail> oldDemandDetails) {

		jdbcTemplate.batchUpdate(DemandQueryBuilder.DEMAND_UPDATE_QUERY, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int rowNum) throws SQLException {
				Demand demand = oldDemands.get(rowNum);

				String status = demand.getStatus() != null ? demand.getStatus().toString() : null;
				String payerUuid = null != demand.getPayer() ? demand.getPayer().getUuid() : null;
				AuditDetails auditDetail = demand.getAuditDetails();

				ps.setString(1, payerUuid);
				ps.setLong(2, demand.getTaxPeriodFrom());
				ps.setLong(3, demand.getTaxPeriodTo());
				ps.setBigDecimal(4, demand.getMinimumAmountPayable());
				ps.setString(5, auditDetail.getLastModifiedBy());
				ps.setLong(6, auditDetail.getLastModifiedTime());
				ps.setString(7, demand.getTenantId());
				ps.setString(8, status);
				ps.setObject(9, util.getPGObject(demand.getAdditionalDetails()));
				ps.setObject(10, demand.getBillExpiryTime());
				ps.setBoolean(11, demand.getIsPaymentCompleted());
				ps.setObject(12, null);
				ps.setBoolean(13, demand.getIsAdvance() != null ? demand.getIsAdvance() : false);
				ps.setInt(14, demand.getAdvanceIndex() != null ? demand.getAdvanceIndex() : 0);
				ps.setString(15, demand.getId());
				ps.setString(16, demand.getTenantId());

			}

			@Override
			public int getBatchSize() {
				return oldDemands.size();
			}
		});

		jdbcTemplate.batchUpdate(DemandQueryBuilder.DEMAND_DETAIL_UPDATE_QUERY, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int rowNum) throws SQLException {
				DemandDetail demandDetail = oldDemandDetails.get(rowNum);
				AuditDetails auditDetail = demandDetail.getAuditDetails();

				ps.setBigDecimal(1, demandDetail.getTaxAmount());
				ps.setBigDecimal(2, demandDetail.getCollectionAmount());
				ps.setString(3, auditDetail.getLastModifiedBy());
				ps.setLong(4, auditDetail.getLastModifiedTime());
				ps.setObject(5, util.getPGObject(demandDetail.getAdditionalDetails()));
				ps.setString(6, demandDetail.getId());
				ps.setString(7, demandDetail.getDemandId());
				ps.setString(8, demandDetail.getTenantId());
			}

			@Override
			public int getBatchSize() {
				return oldDemandDetails.size();
			}
		});
	}
	
	
	/*
	 * Audit 
	 */
	
	@Transactional
	public void insertBatchForAudit(List<Demand> demands, List<DemandDetail> demandDetails) {

		jdbcTemplate.batchUpdate(DemandQueryBuilder.DEMAND_AUDIT_INSERT_QUERY, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int rowNum) throws SQLException {

				Demand demand = demands.get(rowNum);
				String status = demand.getStatus() != null ? demand.getStatus().toString() : null;
				AuditDetails auditDetail = demand.getAuditDetails();
				String payerUuid = null != demand.getPayer() ? demand.getPayer().getUuid() : null;
				ps.setString(1, demand.getId());
				ps.setString(2, demand.getConsumerCode());
				ps.setString(3, demand.getConsumerType());
				ps.setString(4, demand.getBusinessService());
				ps.setString(5, payerUuid);
				ps.setLong(6, demand.getTaxPeriodFrom());
				ps.setLong(7, demand.getTaxPeriodTo());
				ps.setBigDecimal(8, demand.getMinimumAmountPayable());
				ps.setString(9, auditDetail.getLastModifiedBy());
				ps.setLong(10, auditDetail.getLastModifiedTime());
				ps.setString(11, demand.getTenantId());
				ps.setString(12, status);
				ps.setObject(13, util.getPGObject(demand.getAdditionalDetails()));
				ps.setString(14, UUID.randomUUID().toString());
				ps.setObject(15, demand.getBillExpiryTime());
				ps.setBoolean(16, demand.getIsPaymentCompleted());
				ps.setBoolean(17, demand.getIsAdvance() != null ? demand.getIsAdvance() : false);
				ps.setInt(18, demand.getAdvanceIndex() != null ? demand.getAdvanceIndex() : 0);
			}

			@Override
			public int getBatchSize() {
				return demands.size();
			}
		});

		jdbcTemplate.batchUpdate(DemandQueryBuilder.DEMAND_DETAIL_AUDIT_INSERT_QUERY,
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int rowNum) throws SQLException {

						DemandDetail demandDetail = demandDetails.get(rowNum);
						AuditDetails auditDetail = demandDetail.getAuditDetails();
						ps.setString(1, demandDetail.getId());
						ps.setString(2, demandDetail.getDemandId());
						ps.setString(3, demandDetail.getTaxHeadMasterCode());
						ps.setBigDecimal(4, demandDetail.getTaxAmount());
						ps.setBigDecimal(5, demandDetail.getCollectionAmount());
						ps.setString(6, auditDetail.getLastModifiedBy());
						ps.setLong(7, auditDetail.getLastModifiedTime());
						ps.setString(8, demandDetail.getTenantId());
						ps.setObject(9, util.getPGObject(demandDetail.getAdditionalDetails()));
						ps.setString(10, UUID.randomUUID().toString());
					}

					@Override
					public int getBatchSize() {
						return demandDetails.size();
					}
				});
	}

	/**
	 *  Persists back-update log from collection
	 *  
	 *  in case of failure or success
	 *  
	 * @param paymentBackUpdateAudit
	 */
	public void insertBackUpdateForPayment(PaymentBackUpdateAudit paymentBackUpdateAudit) {

		jdbcTemplate.update(DemandQueryBuilder.PAYMENT_BACKUPDATE_AUDIT_INSERT_QUERY, new PreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {

				ps.setString(1, paymentBackUpdateAudit.getPaymentId());
				ps.setBoolean(2, paymentBackUpdateAudit.getIsBackUpdateSucces());
				ps.setBoolean(3, paymentBackUpdateAudit.getIsReceiptCancellation());
				ps.setString(4, paymentBackUpdateAudit.getErrorMessage());
			}
		});
	}

	public String searchPaymentBackUpdateAudit(PaymentBackUpdateAudit backUpdateAudit) {

		String paymentId = null;
		Object[] preparedStatementValues = new Object[] {

				backUpdateAudit.getPaymentId(),
				backUpdateAudit.getIsBackUpdateSucces(),
				backUpdateAudit.getIsReceiptCancellation() };

		try {
			paymentId = jdbcTemplate.queryForObject(
					DemandQueryBuilder.PAYMENT_BACKUPDATE_AUDIT_SEARCH_QUERY, preparedStatementValues, 	String.class);
		} catch (DataAccessException e) {
			log.info("No data found for incoming receipt in backupdate log");
		}

		return paymentId;
	}

	private static final String MARKET_ESSENTIAL_INFO_SQL =
        "SELECT ep2.paymentmode, " +
         "       eem.fund_center, " +
         "       eem.fund, " +
         "       eem.business_area, " +
		 "       eem.functional_area, " +
		 // No ep2.tenantid. The collecting ward comes only from additionaldetails.collectingWardTenant,
		 // which emarket-v1 writes from the collector's own role grants. egcl_payment.tenantid is
		 // whatever the browser sent, and a SUPERUSER's receipt carried a ward there
		 // (MARKET/26-27/000029, 031, 032).
		 "       ep2.additionaldetails , ep2.totaldue , ep2.totalamountpaid , ep.receiptnumber , ep2.transactionnumber " +
        "FROM egcl_billdetial eb " +
        "JOIN egcl_bill eb2 ON eb.billid = eb2.id " +
        "JOIN egcl_paymentdetail ep ON ep.billid = eb2.id " +
        "JOIN egcl_payment ep2 ON ep2.id = ep.paymentid " +
         "JOIN eg_emarket_allotment eea " +
         "     ON regexp_replace(eb2.consumercode, '[^0-9]', '', 'g') = eea.license_number " +
         "JOIN eg_emarket_assets eea2 ON eea.asset_id = eea2.id " +
         "JOIN eg_emarket_markets eem ON eea2.market_id = eem.market_id " +
        "WHERE eb.demandid = ?";

	private static final RowMapper<PaymentMarketInfo> MARKET_INFO_ROW_MAPPER = new RowMapper<PaymentMarketInfo>() {
            @Override
            public PaymentMarketInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                PaymentMarketInfo info = new PaymentMarketInfo();
                info.setPaymentMode(rs.getString("paymentmode"));
				info.setFundCenter(rs.getString("fund_center"));
                info.setFund(rs.getString("fund"));
                info.setBusinessArea(rs.getString("business_area"));
				info.setAdditionalDetails(rs.getString("additionaldetails"));
				info.setTotalAmountPaid(rs.getBigDecimal("totalamountpaid"));
				info.setTotalDue(rs.getBigDecimal("totaldue"));
				info.setFunctionalArea(rs.getString("functional_area"));
                info.setReceiptNumber(rs.getString("receiptnumber"));
                info.setTransactionNumber(rs.getString("transactionnumber"));
                return info;
            }
        };

	public List<PaymentMarketInfo> getMarketEssentialInfo(String demandId) {
		return jdbcTemplate.query(MARKET_ESSENTIAL_INFO_SQL, new Object[] { demandId }, MARKET_INFO_ROW_MAPPER);
	}

	/**
	 * The interim-receipt dimensions of the CFC ward a collection was taken at, or null when the
	 * ward is not mapped.
	 *
	 * <p>Only the ward's fund and business area are read from the master. The fund centre and
	 * functional area of an interim receipt follow a fixed rule, not a per-ward value, so they are
	 * derived by {@link #interimReceiptDimensions}. The table's own fund_centre and functional_area
	 * columns are NOT read.
	 *
	 * <p>Returns null — meaning "no collecting ward established" — for every case that is not a
	 * mapped, active CFC ward: a tenant that is not ward-level, a ward BMC has not supplied a
	 * business area for (seeded '0'), or a database where docs/sql/add_cfc_ward_dimension.sql has
	 * not been run yet. The last of those matters: the table is created by a hand-run script, not
	 * Flyway, so the jar can legitimately reach production first and must degrade rather than fail
	 * every collection.
	 */
	public FiDimensions getCfcWardDimensions(String wardTenant) {

		if (wardTenant == null || wardTenant.trim().isEmpty())
			return null;

		String sql =
			"SELECT fund, business_area " +
			"FROM eg_emarket_cfc_ward_dimension " +
			"WHERE ward_tenant = ? AND is_active " +
			"  AND fund <> '0' AND business_area <> '0' " +
			"LIMIT 1";

		try {
			List<FiDimensions> found = jdbcTemplate.query(sql, new Object[] { wardTenant.trim() },
					(rs, rowNum) -> interimReceiptDimensions(rs.getString("fund"), rs.getString("business_area")));
			if (found.isEmpty() || found.get(0) == null) {
				log.info("Collecting ward {} has no usable CFC dimension row; the interim receipt "
						+ "falls back to the licensee's market business area", wardTenant);
				return null;
			}
			return found.get(0);
		} catch (DataAccessException e) {
			log.warn("Could not read CFC dimensions for ward {} ({}); the interim receipt falls back "
					+ "to the licensee's market business area", wardTenant, e.getMessage());
			return null;
		}
	}

	/**
	 * The complete dimension set an interim receipt posts with at one business area. The fund
	 * centre is the business area followed by the fixed suffix, e.g. 4010 -> 4010130000, and the
	 * functional area is the fixed 00301000000.
	 *
	 * <p>Returns null when there is no business area to build on, because a fund centre of
	 * "130000" alone names no account.
	 */
	public FiDimensions interimReceiptDimensions(String fund, String businessArea) {
		if (fund == null || fund.trim().isEmpty() || businessArea == null || businessArea.trim().isEmpty())
			return null;
		String ba = businessArea.trim();
		return new FiDimensions(fund.trim(), ba + interimReceiptFundCentreSuffix, ba, interimReceiptFunctionalArea);
	}

	/**
	 * The dimensions each leg of a receipt was ACTUALLY posted with, for its reversal.
	 *
	 * <p>A reversal must give back what was posted, which is the principle this file already
	 * applies four times over — {@code wasCollectionSplit}, {@code wasPostedAsAdvance},
	 * {@code wasPostedWithGstPayableDebit} and {@code postedBankGl} all read the forward document
	 * rather than recomputing. Dimensions need it too: a receipt posted before the interim-receipt rule
	 * was corrected carries other values on its money leg, the flag can be turned off again, and a
	 * licensee's market can be re-pointed between collection and cancellation. Rows that carry a
	 * business area are returned even when another value is blank. See
	 * {@code interimReceiptLegDimensions} for how the money leg uses such a row.
	 *
	 * <p>Scoped three ways, all load-bearing. {@code report_type} stops a second cancellation
	 * reading back its own reversal rows. {@code reference_no} is required because
	 * {@code transactionnumber} is ten random idgen digits with no unique constraint, so a receipt
	 * number alone can land on another licence's payment. Keyed by GL and forward posting key, so each
	 * leg finds its own counterpart.
	 */
	public Map<String, FiDimensions> getPostedCollectionDimensions(String documentHeaderText, String referenceNo) {

		if (documentHeaderText == null || documentHeaderText.trim().isEmpty()
				|| referenceNo == null || referenceNo.trim().isEmpty())
			return Collections.emptyMap();

		// PER LEG, keyed by GL. One row for the whole voucher cannot work: a GST_ADVANCE receipt
		// posts THREE forward-40 legs (bank, 439300200, 439300201) and only the bank leg carries the
		// CFC's dimensions, so "the first key-40 row" is two different answers. It is not even a random pick — every leg of a voucher is written in one batch
		// with the same created_at, so the tiebreak is decided by whatever index the planner walks.
		//
		// Reading every leg also fixes the larger problem: the dimensions this returns were applied
		// to the key-40 legs while the rest of the reversal took demand.getFund()/getBusinessArea(),
		// which MARKET_ESSENTIAL_INFO_SQL resolves LIVE through the current allotment. Re-point a
		// stall at another market between collection and cancellation — an ordinary asset edit —
		// and the compensating document straddles two business areas.
		String sql =
			"SELECT gl_code, posting_key, fund, fund_centre, business_area, functional_area " +
			"FROM public.eg_emarket_fi_report_collection " +
			"WHERE document_header_text = ? AND reference_no = ? AND report_type = ?";

		try {
			Map<String, FiDimensions> byLeg = new HashMap<>();
			jdbcTemplate.query(sql,
					new Object[] { documentHeaderText.trim(), referenceNo.trim(), FiReportType.UPMKT_COLL },
					rs -> {
						FiDimensions dims = new FiDimensions(rs.getString("fund"), rs.getString("fund_centre"),
								rs.getString("business_area"), rs.getString("functional_area"));
						// Kept when it has a business area, even if another value is blank: the
						// interim receipt's reversal needs only that to land where the money was
						// booked. Every leg that is mirrored whole checks isComplete() itself.
						// Two legs on one key (the money legs of a split receipt) prefer a complete set.
						if (notBlank(dims.getBusinessArea()))
							byLeg.merge(postedLegKey(rs.getString("gl_code"), rs.getString("posting_key")), dims,
									(kept, next) -> kept.isComplete() ? kept : next);
					});
			return byLeg;
		} catch (DataAccessException e) {
			log.warn("Could not read the posted dimensions of receipt {} for licence {} ({}); the "
					+ "reversal falls back to the market's dimensions",
					documentHeaderText, referenceNo, e.getMessage());
			return Collections.emptyMap();
		}
	}

	/**
	 * Key a posted collection leg by the GL it hit and the key it hit it with.
	 *
	 * <p>GL alone is not unique within a voucher — the non-gross GST_REGULAR shape debits
	 * 350200421/422 while the same GLs can be credited elsewhere — and the forward posting key is
	 * stable across a reversal, which the final key is not.
	 */
	public static String postedLegKey(String glCode, String forwardPostingKey) {
		return glCode + "@" + forwardPostingKey;
	}


	/**
	 * The same lookup, narrowed to the payment currently being processed.
	 *
	 * <p>The unfiltered query returns one row per (bill, payment) the demand has ever appeared on,
	 * with no ORDER BY, and every caller then takes {@code get(0)}. For a demand paid more than once
	 * — a dishonoured cheque followed by cash, say — that is an arbitrary pick, and it supplies the
	 * total paid, the advance JSON and the payment mode. Licence 5000007527 posted a ₹2,569 cash
	 * receipt as ₹2,106 because the row chosen was the bounced cheque's.
	 *
	 * <p>The market dimensions (fund, fund centre, business area, functional area) are invariant
	 * across the rows — they come from the allotment/market join, and no demand in the database
	 * resolves to more than one fund centre — so narrowing can only ever change the five
	 * payment-derived fields, which is precisely the intent.
	 *
	 * <p>Falls back to the unfiltered result when the transaction number is absent or matches
	 * nothing, so this can only ever refine the choice of row, never remove a posting.
	 */
	public List<PaymentMarketInfo> getMarketEssentialInfo(String demandId, String transactionNumber) {

		if (transactionNumber == null || transactionNumber.trim().isEmpty())
			return getMarketEssentialInfo(demandId);

		List<PaymentMarketInfo> filtered = jdbcTemplate.query(
				MARKET_ESSENTIAL_INFO_SQL
						+ " AND ep2.transactionnumber = ? "
						+ "ORDER BY ep2.transactiondate ASC, ep2.transactionnumber ASC",
				new Object[] { demandId, transactionNumber },
				MARKET_INFO_ROW_MAPPER);

		if (filtered.isEmpty()) {
			log.warn("No payment row for demand {} and transaction {}; falling back to the unfiltered lookup",
					demandId, transactionNumber);
			return getMarketEssentialInfo(demandId);
		}
		return filtered;
	}


	public void saveAdvSettlementDemandIds(AdvSettlement settlement){
		   String SQL =
            "INSERT INTO eg_emarket_demand_settlement_info " +
            "(advance_demandid, settled_demandid , consumercode ,periodfrom, periodto) " +
            "VALUES (?, ?, ?, ?, ?)";			
        jdbcTemplate.update(
                SQL,
                settlement.getAdvanceDemandId(),
                settlement.getSettledDemandId(),
                settlement.getConsumerCode(),
                settlement.getTaxPeriodFrom(),
                settlement.getTaxPeriodTo()
        );
    

	}

public List<AdvSettlement> getSettledDemandIdsByAdvanceDemandId(String advanceDemandId) {

    String FETCH_SETTLED_DEMAND_ID =
        "SELECT advance_demandid, settled_demandid, consumercode, periodfrom, periodto " +
        "FROM eg_emarket_demand_settlement_info " +
        "WHERE advance_demandid = ?";

    return jdbcTemplate.query(
        FETCH_SETTLED_DEMAND_ID,
        new Object[]{advanceDemandId},
        (rs, rowNum) -> AdvSettlement.builder()
                .advanceDemandId(rs.getString("advance_demandid"))
                .settledDemandId(rs.getString("settled_demandid"))
                .consumerCode(rs.getString("consumercode"))
                .taxPeriodFrom(rs.getLong("periodfrom"))
                .taxPeriodTo(rs.getLong("periodto"))
                .build()
    );
}






private String extractGlCode(DemandDetail detail) {
    Object addObj = detail.getAdditionalDetails();
    if (addObj instanceof Map) {
        Object gl = ((Map<?, ?>) addObj).get("glcode");
        if (gl != null) {
            return gl.toString();
        }
    }
    return null;
}

/**
 * The head's 4-Series RECEIVABLE GL (431409937..431409977), stamped alongside — never instead
 * of — {@code glcode} by emarket-v1's GlSacMapperService. Null when the head has no 4-Series
 * row in the master, and null for every demand created before that master was seeded.
 *
 * <p>Total by contract: never throws. TransferReversalExecutor.buildFiLegsFor swallows every
 * RuntimeException and drops ALL of a demand's reversal legs when one is raised, so an
 * exception here would silently cost a licence its whole reversal document.
 */
private String extractAdvanceGlCode(DemandDetail detail) {
    try {
        Object addObj = detail.getAdditionalDetails();
        if (addObj instanceof Map) {
            Object gl = ((Map<?, ?>) addObj).get("advglcode");
            if (gl != null && !gl.toString().trim().isEmpty()) {
                return gl.toString().trim();
            }
        }
    } catch (RuntimeException ignored) {
        // additionalDetails is typed Object across the whole demand contract; a shape we cannot
        // read is a reason to post today's voucher, never a reason to fail the demand.
    }
    return null;
}

/** SAP Assignment (ZUONR) carried on the synthetic netting heads; null on every other detail. */
private String extractAssignment(DemandDetail detail) {
    Object addObj = detail.getAdditionalDetails();
    if (addObj instanceof Map) {
        Object a = ((Map<?, ?>) addObj).get("assignment");
        if (a != null) {
            return a.toString();
        }
    }
    return null;
}

/**
 * GST on this demand that was actually settled out of the advance, capped at the GST
 * advance still unreleased for the licensee.
 *
 * <p>{@code min(taxAmount, collectionAmount)} summed over every head of the component
 * handles full settlement, partial settlement and multi-head demands in one expression;
 * the cap handles an exhausted (or never-created) GST advance. Returns ZERO when there is
 * nothing to net, in which case no netting rows are emitted at all.
 */
/**
 * The licence a consumer code belongs to, with its business-service suffix removed.
 *
 * <p>Consumer codes are the licence number plus a per-service suffix — 5000000284rf for rent,
 * 5000000284lf for the licence fee, prf/plf for penalties. The GST advance, however, is a
 * single pool held for the LICENSEE: one receipt can carry rent and licence-fee advances
 * together, and the collection voucher stamps that pool with whichever demand happened to be
 * oldest. Keying the balance lookup on the raw consumer code would therefore look for the
 * asset under the wrong code, find nothing, and silently skip the net-off — paying the GST a
 * second time, which is the very thing this work exists to prevent.
 */
private static String licenceKey(String consumerCode) {
    return consumerCode == null ? null : consumerCode.replaceAll("[^0-9]", "");
}

/**
 * Every consumer code under which a licensee's GST advance may have been stamped.
 *
 * <p>Deliberately an explicit list of exact values rather than a pattern on the column: the
 * balance lookup sits on the demand-creation path and runs twice per demand, and the partial
 * index on (reference_no, gl_code) only serves equality. Matching with regexp_replace on the
 * column instead forces a sequential scan of the whole FI history.
 *
 * <p>The bare licence number is included for rows migrated from the legacy system, which
 * carry no business-service suffix.
 */
private static final String[] CONSUMER_CODE_SUFFIXES = { "rf", "lf", "prf", "plf", "cbf", "tf", "df" };

private static List<String> licenceConsumerCodes(String consumerCode) {
    String licence = licenceKey(consumerCode);
    if (licence == null || licence.isEmpty())
        return Collections.emptyList();
    List<String> codes = new ArrayList<>(CONSUMER_CODE_SUFFIXES.length + 1);
    codes.add(licence);
    for (String suffix : CONSUMER_CODE_SUFFIXES)
        codes.add(licence + suffix);
    return codes;
}

/**
 * Serialise GST-advance releases for one licensee across concurrent requests.
 *
 * <p>The cap is read-then-write: two {@code /demand/_create} calls for the same consumer
 * (the monthly rent sweeper racing a restoration, say) would each read the full unreleased
 * balance and both net against it, releasing more advance than exists. A transaction-scoped
 * Postgres advisory lock closes that window without a schema change; it is released
 * automatically at commit or rollback.
 *
 * <p>Taken only on the rental demand-against-advance path, so the five sibling services and
 * every ordinary demand never contend for it. A failure to acquire is logged and ignored
 * rather than failing the demand — the cap still bounds the release, it merely loses the
 * cross-request guarantee.
 */
private void lockAdvanceForConsumer(String consumerCode) {

    if (consumerCode == null || consumerCode.isEmpty())
        return;

    try {
        jdbcTemplate.query("SELECT pg_advisory_xact_lock(hashtext(?))",
                new Object[] { licenceKey(consumerCode) }, rs -> null);
    } catch (DataAccessException e) {
        log.warn("Could not take the advance lock for consumer {}; continuing without it", consumerCode, e);
    }
}

private BigDecimal cappedGstNetOff(Demand demand, String component, String advanceGlCode,
                                   Map<String, BigDecimal> releasedInThisBatch) {

    BigDecimal settled = gstSettledFromAdvance(demand, component);
    if (settled.compareTo(BigDecimal.ZERO) <= 0)
        return BigDecimal.ZERO;

    // Committed balance, less whatever earlier demands in this same batch have already
    // claimed but not yet written.
    String batchKey = licenceKey(demand.getConsumerCode()) + "|" + advanceGlCode;
    BigDecimal alreadyClaimed = releasedInThisBatch.getOrDefault(batchKey, BigDecimal.ZERO);
    BigDecimal remaining = remainingGstAdvance(demand.getConsumerCode(), advanceGlCode)
            .subtract(alreadyClaimed);

    if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
        // WARN, not INFO: the demand DID settle GST out of an advance, but no unreleased
        // advance can be found for it. That is either a migrated advance (expected) or a
        // mis-stamped collection voucher (a defect) — either way it must be visible.
        log.warn("No unreleased {} advance for licence {} (consumer {}, claimed {} earlier in this batch); "
                + "net-off of {} suppressed — GST will be borne again on this demand",
                component, licenceKey(demand.getConsumerCode()), demand.getConsumerCode(), alreadyClaimed, settled);
        return BigDecimal.ZERO;
    }

    // Not recorded against the batch tally here: the caller reconciles CGST against SGST
    // first and commits the agreed amount via recordBatchRelease.
    return settled.min(remaining);
}

/** Commit an agreed net-off against the running per-batch tally. */
private void recordBatchRelease(Map<String, BigDecimal> releasedInThisBatch, Demand demand,
                                String advanceGlCode, BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        return;
    String batchKey = licenceKey(demand.getConsumerCode()) + "|" + advanceGlCode;
    releasedInThisBatch.merge(batchKey, amount, BigDecimal::add);
}

/**
 * Whether the demand carries a taxable head for this GST component at all — independent of how
 * much of it any advance settled. GST_CA is a rounding-adjustment head, not tax, and is excluded.
 */
private boolean carriesGstComponent(Demand demand, String component) {

    if (demand.getDemandDetails() == null)
        return false;

    return demand.getDemandDetails().stream()
            .filter(dd -> dd.getTaxHeadMasterCode() != null)
            .filter(dd -> dd.getTaxHeadMasterCode().toUpperCase().contains(component)
                    && !"GST_CA".equalsIgnoreCase(dd.getTaxHeadMasterCode()))
            .anyMatch(dd -> dd.getTaxAmount() != null && dd.getTaxAmount().signum() > 0);
}

/**
 * Sum of {@code min(taxAmount, collectionAmount)} across every demand detail of the given
 * GST component. GST_CA is a rounding-adjustment head, not tax, and is excluded.
 */
private BigDecimal gstSettledFromAdvance(Demand demand, String component) {

    if (demand.getDemandDetails() == null)
        return BigDecimal.ZERO;

    return demand.getDemandDetails().stream()
            .filter(dd -> dd.getTaxHeadMasterCode() != null)
            .filter(dd -> dd.getTaxHeadMasterCode().toUpperCase().contains(component)
                    && !"GST_CA".equalsIgnoreCase(dd.getTaxHeadMasterCode()))
            .map(dd -> {
                BigDecimal tax = dd.getTaxAmount() == null ? BigDecimal.ZERO : dd.getTaxAmount();
                BigDecimal coll = dd.getCollectionAmount() == null ? BigDecimal.ZERO : dd.getCollectionAmount();
                BigDecimal settled = tax.min(coll);
                return settled.compareTo(BigDecimal.ZERO) > 0 ? settled : BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}

/**
 * GST advance created for this licensee and not yet released.
 *
 * <p>Created at advance receipt (posting key 40 on the collection side), released by each
 * demand's netting leg (posting key 50 on the demand side); reversals swap the keys, so a
 * signed sum over both tables is self-correcting. A licensee whose advance was migrated
 * rather than collected in-system has no collection row and therefore a zero balance —
 * which is exactly what stops the netting from inventing an asset.
 */
private BigDecimal remainingGstAdvance(String consumerCode, String advanceGlCode) {

    if (consumerCode == null || advanceGlCode == null)
        return BigDecimal.ZERO;

    List<String> codes = licenceConsumerCodes(consumerCode);
    if (codes.isEmpty())
        return BigDecimal.ZERO;
    String in = String.join(",", Collections.nCopies(codes.size(), "?"));
    String sql =
        "SELECT COALESCE(SUM(CASE WHEN posting_key = '40' THEN collection_amount " +
        "                         WHEN posting_key = '50' THEN -collection_amount " +
        "                         ELSE 0 END), 0) " +
        "FROM public.eg_emarket_fi_report_collection " +
        "WHERE reference_no IN (" + in + ") AND gl_code = ? " +
        "UNION ALL " +
        "SELECT COALESCE(SUM(CASE WHEN posting_key = '40' THEN collection_amount " +
        "                         WHEN posting_key = '50' THEN -collection_amount " +
        "                         ELSE 0 END), 0) " +
        "FROM public.eg_emarket_fi_report " +
        "WHERE reference_no IN (" + in + ") AND gl_code = ?";

    try {
        List<Object> params = new ArrayList<>(codes);
        params.add(advanceGlCode);
        params.addAll(codes);
        params.add(advanceGlCode);
        List<BigDecimal> legs = jdbcTemplate.queryForList(sql, params.toArray(), BigDecimal.class);
        return legs.stream()
                .map(v -> v == null ? BigDecimal.ZERO : v)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    } catch (DataAccessException e) {
        // Never block demand creation on the balance lookup. Returning ZERO suppresses the
        // netting rows, which is the safe direction: GST stays payable rather than being
        // written off against an advance we could not confirm.
        log.error("Could not read GST advance balance for consumer {} gl {}; suppressing net-off",
                consumerCode, advanceGlCode, e);
        return BigDecimal.ZERO;
    }
}

/** The advance receipt a demand drew on: its transaction number and its date. */
static final class AdvanceReceiptRef {
    final String documentNo;
    final Long receiptDate;
    AdvanceReceiptRef(String documentNo, Long receiptDate) {
        this.documentNo = documentNo;
        this.receiptDate = receiptDate;
    }
    static final AdvanceReceiptRef NONE = new AdvanceReceiptRef(null, null);
}

/**
 * Resolve the advance receipt behind a demand settled from advance, in one query.
 *
 * <p>The transaction number becomes the SAP Assignment (ZUONR) on the netting legs — the
 * key F.13/FB05 matches against the advance receipt's own legs — and the receipt date
 * bounds the FI doc date. Both come from the same row, so they can never disagree.
 *
 * <p>Deliberately joined straight through the payment tables rather than via
 * {@link #getMarketEssentialInfo}: that helper also joins allotment → assets → markets on a
 * regexp-stripped consumer code, so a licensee with an incomplete master chain would yield
 * no row and silently lose its clearing key — which is precisely the "GST not getting
 * cleared in SAP" symptom this work exists to fix. Nothing here needs the market masters.
 *
 * <p>Returns {@link AdvanceReceiptRef#NONE} when unresolvable; the netting rows still post,
 * they simply carry no clearing key, and the doc date falls back to the tax period.
 */
private AdvanceReceiptRef getAdvanceReceipt(String settledDemandId) {

    if (settledDemandId == null)
        return AdvanceReceiptRef.NONE;

    // The DATE is taken from the advance's own accounting row, not from when the payment was
    // keyed in. A backdated advance -- migrated, or a catch-up entry for an earlier period --
    // carries an FI doc_date in its true period while egcl_payment.transactiondate is the day it
    // was recorded. Clamping to the latter dragged every demand that drew on that advance forward
    // to the entry date, so twelve monthly demands all landed in one period and the revenue and
    // GST fell outside the month they belong to. This is the same source the AT (11A) sheet reads,
    // so the tax point the demand carries and the date the return declares can no longer disagree.
    // Falls back to the payment date when the advance has no FI row (a legacy advance).
    String sql =
        "SELECT p.transactionnumber, " +
        "       COALESCE((SELECT MIN(f.doc_date) FROM public.eg_emarket_fi_report_collection f " +
        "                  WHERE f.document_header_text = p.transactionnumber " +
        "                    AND f.gl_code IN ('439300200','439300201','350410215') " +
        "                    AND f.report_type IS NOT NULL), p.transactiondate) AS transactiondate " +
        "FROM eg_emarket_demand_settlement_info s " +
        "JOIN egcl_billdetial bd ON bd.demandid = s.advance_demandid " +
        "JOIN egcl_paymentdetail pd ON pd.billid = bd.billid " +
        "JOIN egcl_payment p ON p.id = pd.paymentid " +
        // The advance demand rides along as a carry-forward line on every later bill for the
        // same licensee, so this join yields one row per bill it ever appeared on. The receipt
        // that FUNDED the advance is the earliest of them — DESC would deterministically pick
        // an unrelated later payment, giving SAP a clearing key that matches nothing and
        // clamping the tax point to the wrong date. Cancelled payments are excluded outright.
        "WHERE s.settled_demandid = ? " +
        "  AND (p.paymentstatus IS NULL OR p.paymentstatus NOT IN ('CANCELLED','DISHONOURED')) " +
        "ORDER BY p.transactiondate ASC LIMIT 1";

    try {
        List<AdvanceReceiptRef> refs = jdbcTemplate.query(sql, new Object[] { settledDemandId },
                (rs, rowNum) -> new AdvanceReceiptRef(
                        rs.getString("transactionnumber"), (Long) rs.getObject("transactiondate")));
        return refs.isEmpty() ? AdvanceReceiptRef.NONE : refs.get(0);
    } catch (DataAccessException e) {
        log.error("Could not resolve the advance receipt for settled demand {}", settledDemandId, e);
        return AdvanceReceiptRef.NONE;
    }
}

/**
 * Doc date for a demand's FI rows: always the tax period start, for demands against an
 * advance too (BMC direction, 2026-08-24 — a demand document belongs in the period of the
 * rent it invoices, whatever date the funding advance was keyed in).
 *
 * <p>An earlier revision clamped a demand-against-advance forward to its advance receipt
 * date on GST time-of-supply grounds. In time-aligned operation an advance always arrives
 * before the period it settles, so that clamp could only ever fire on backdated data —
 * migration loads and UAT — which is exactly where the true-period date is wanted. The
 * advance receipt is still resolved for the SAP clearing key (ZUONR); only its date is no
 * longer used here. SAP-side posting-period control, where needed, belongs to the export
 * step, not to this document date.
 */
private Long resolveDemandDocDate(Demand demand, AdvanceReceiptRef advance) {
    return demand.getTaxPeriodFrom();
}

/**
 * Collect one synthetic, FI-only demand detail carrying the netting leg. Written to a
 * caller-supplied list rather than onto the Demand: they must never reach
 * egbs_demanddetail_v1, the /demand/_create response or the demand-index topic — they exist
 * purely to drive {@link #buildDemandFiReports}. Zero amounts are skipped so a demand with
 * nothing to net produces no netting rows.
 */
private void addNettingDetail(List<DemandDetail> target, Demand demand, String headCode, String glCode,
                              BigDecimal amount, String advanceDocNo) {

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        return;

    Map<String, Object> additional = new HashMap<>();
    additional.put("glcode", glCode);
    if (advanceDocNo != null)
        additional.put("assignment", advanceDocNo);

    target.add(DemandDetail.builder()
            .demandId(demand.getId())
            .taxAmount(amount)
            .taxHeadMasterCode(headCode)
            .additionalDetails(additional)
            .build());
}




public List<FiReport> buildDemandFiReports(Demand demand) {
    return buildDemandFiReports(demand, Collections.<DemandDetail>emptyList());
}

/**
 * @param extraDetails synthetic FI-only heads to account for alongside the demand's own
 *                     persisted details. Empty for every path except the GST net-off.
 */
public List<FiReport> buildDemandFiReports(Demand demand, List<DemandDetail> extraDetails) {
    return buildDemandFiReports(demand, extraDetails, null);
}

/**
 * @param preResolvedAdvance the advance receipt already looked up by the caller, or null to
 *                           resolve it here. Avoids a second scan of the collection table.
 */
public List<FiReport> buildDemandFiReports(Demand demand, List<DemandDetail> extraDetails,
                                           AdvanceReceiptRef preResolvedAdvance) {

    List<FiReport> reports = new ArrayList<>();

    String consumerCode = demand.getConsumerCode();
    // Every row of this document is dated the demand's tax period (see resolveDemandDocDate).
    // The advance receipt is resolved only for the SAP clearing key (ZUONR) on the advance leg;
    // its date is deliberately not used for dating.
    AdvanceReceiptRef advance = !demand.isApportionedAgainstAdvance() ? AdvanceReceiptRef.NONE
            : preResolvedAdvance != null ? preResolvedAdvance
            : getAdvanceReceipt(demand.getId());
    Long postingDate = resolveDemandDocDate(demand, advance);

	String fund;
	String fundCenter;
	String businessArea;
	String functionalArea;

       Map<String,String> additionalMarketDetails = new HashMap<>();
	   Object additiaonalsObj = demand.getAdditionalDetails();
       if (additiaonalsObj instanceof Map) {
          additionalMarketDetails = (Map) additiaonalsObj;
       } 
	    fund = additionalMarketDetails.get("fund");
	    fundCenter = additionalMarketDetails.get("fundCenter");
        businessArea = additionalMarketDetails.get("businessArea");
		functionalArea = additionalMarketDetails.get("functionalArea");


    BigDecimal totalReceivable = BigDecimal.ZERO;
    // Portion of this demand already settled out of an advance. The balancing debit for it
    // belongs on 350410215 (releasing the advance the licensee has already paid), NOT on the
    // receivable — nothing is owed for it, and a receivable booked here would never clear
    // because no collection follows a demand settled from advance. Only the unsettled
    // remainder is a true receivable.
    BigDecimal settledFromAdvance = BigDecimal.ZERO;

    // Per-head breakdown of settledFromAdvance, for the 4-Series pair. Collected in the same pass
    // so the two can never disagree about what a head settled.
    List<FourSeriesLine> fourSeriesLines = new ArrayList<>();

    List<DemandDetail> detailsForFi = demand.getDemandDetails();
    if (extraDetails != null && !extraDetails.isEmpty()) {
        detailsForFi = new ArrayList<>(detailsForFi);
        detailsForFi.addAll(extraDetails);
    }

    for(DemandDetail dd : detailsForFi){
        if(dd.getTaxAmount() == null || dd.getTaxAmount().compareTo(BigDecimal.ZERO) == 0)
           continue;
        String th = dd.getTaxHeadMasterCode();

          reports.add(FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(consumerCode)
            .remarks(th.contains("GST") && !th.equalsIgnoreCase("GST_CA") ? 
			        (th.contains("CGST") ? "CGST Payable" : "SGST Payable") 
					 :  NETTING_DEBIT_HEADS.contains(th) ? "CSP40".equals(th) ? "CGST Payable" : "SGST Payable"
                     : NETTING_CREDIT_HEADS.contains(th) ? "CSA50".equals(th) ? "CGST Advance" : "SGST Advance" 
                     : th)
            .postingKey(NETTING_DEBIT_HEADS.contains(th) ? "40" : "50")
            .glCode(extractGlCode(dd))
            .assignment(extractAssignment(dd))
            .collectionAmount(dd.getTaxAmount())
            .fund(fund)
            .fundCentre(fundCenter)
            .businessArea(businessArea)
            .functionalArea(functionalArea)
            .documentHeaderText(demand.getDemandSeqNo() != null ?  demand.getDemandSeqNo().toString() : null)
            .docType("YX")
            .isNew(Boolean.TRUE)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build());
        if(!NETTING_DEBIT_HEADS.contains(th) && !NETTING_CREDIT_HEADS.contains(th)) {
	       totalReceivable = totalReceivable.add(dd.getTaxAmount());
	       if (demand.isApportionedAgainstAdvance() && dd.getCollectionAmount() != null) {
	           BigDecimal settled = dd.getTaxAmount().min(dd.getCollectionAmount());
	           if (settled.compareTo(BigDecimal.ZERO) > 0) {
	               settledFromAdvance = settledFromAdvance.add(settled);
	               fourSeriesLines.add(new FourSeriesLine(th, settled,
	                       extractGlCode(dd), extractAdvanceGlCode(dd)));
	           }
	       }
	    }
	}

    // Balancing debit(s). The advance-settled portion releases 350410215; only the
    // unsettled remainder is booked as a receivable. A demand with no advance behind it
    // (settledFromAdvance == 0) produces exactly the single receivable row it always has.
    BigDecimal uncappedSettled = settledFromAdvance;
    settledFromAdvance = settledFromAdvance.min(totalReceivable);
    BigDecimal openReceivable = totalReceivable.subtract(settledFromAdvance);

    // The per-head lines sum to the UNCAPPED figure. Each head's settled amount is bounded by its
    // own taxAmount and totalReceivable is the sum of those same taxAmounts, so the cap cannot
    // bind — but if it ever did, the per-head rows would no longer add up to the lumped debit, so
    // drop the pair rather than post a breakdown that disagrees with the total it breaks down.
    if (!fourSeriesLines.isEmpty() && uncappedSettled.compareTo(settledFromAdvance) != 0) {
        log.warn("Demand {}: advance-settled total capped from {} to {}; 4-Series pair skipped "
                + "because the per-head breakdown would no longer reconcile",
                demand.getId(), uncappedSettled, settledFromAdvance);
        fourSeriesLines.clear();
    }

    if (settledFromAdvance.compareTo(BigDecimal.ZERO) != 0) {
        reports.add(FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(consumerCode)
            .remarks("Advance")
            .postingKey("40")
            .glCode("350410215")
            .collectionAmount(settledFromAdvance)
            .assignment(advance.documentNo)
            .fund(fund)
            .fundCentre(fundCenter)
            .businessArea(businessArea)
            .functionalArea(functionalArea)
            .documentHeaderText(demand.getDemandSeqNo() != null ?  demand.getDemandSeqNo().toString() : null)
            .docType("YX")
            .isNew(Boolean.TRUE)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build());
    }

    // 1️⃣ Customer / Receivable (Dr)
     if(openReceivable.compareTo(BigDecimal.ZERO) != 0){
    reports.add(FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(consumerCode)
            .remarks("Receivable from Mun Mkt")
            .postingKey("40")
            .glCode(receivableGlCode)
            .collectionAmount(openReceivable)
            .fund(fund)
            .fundCentre(fundCenter)
            .businessArea(businessArea)
            .functionalArea(functionalArea)
            .documentHeaderText(demand.getDemandSeqNo() != null ?  demand.getDemandSeqNo().toString() : null)
            .docType("YX")
            .isNew(Boolean.TRUE)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis())
            .build());
     }

    reports.addAll(buildFourSeriesPair(demand, fourSeriesLines, postingDate, consumerCode,
            fund, fundCenter, businessArea, functionalArea, advance.documentNo));

    return reports;
}

/**
 * The per-tax-head 4-Series pair for a demand raised against an advance: Dr the head's
 * RECEIVABLE account (431409937..431409977), Cr the head's revenue account, for the amount that
 * head settled out of the advance.
 *
 * <p>Purely additive. It touches no row the voucher already contains, so 350410215 still takes
 * the full settled amount and still unwinds to nil across an advance cycle, and switching the
 * feature off gives byte-identical output.
 *
 * <p>Both legs are the same amount at opposite posting keys, so the pair is self-balancing and
 * the document balances whether or not any given head has a 4-Series account.
 *
 * <p>A head with no 4-Series GL gets NO pair rather than a pair on its revenue GL twice. The
 * master BMC supplied covers 20 rent heads; PAYRI, PHALI, NAME_BOARD_ILLUMINATED, ONING, ADMIN,
 * REBATE and DIFFERENCE_FEE have none, and CGST/SGST/GST_CA are excluded by design because the
 * reference entry's 4-Series lines total the NET rent, not the gross. A head with no revenue GL
 * either — an unmapped head, which stamps no glcode at all — likewise gets nothing: two rows
 * naming no account is worse than no rows.
 *
 * <p>Tagged {@link FiReportType#UPMKT_DEMDADV_4S} here rather than by the caller. The caller
 * stamps its own type over every row it is handed, and that type sits inside the GST return's
 * report_type filter, where these rows must never appear.
 */
private List<FiReport> buildFourSeriesPair(Demand demand, List<FourSeriesLine> lines,
                                           Long postingDate, String consumerCode,
                                           String fund, String fundCenter,
                                           String businessArea, String functionalArea,
                                           String advanceDocNo) {

    List<FiReport> pair = new ArrayList<>();
    if (!advanceFourSeriesEnabled || lines.isEmpty())
        return pair;

    String headerText = demand.getDemandSeqNo() != null ? demand.getDemandSeqNo().toString() : null;
    long now = System.currentTimeMillis();

    for (FourSeriesLine line : lines) {

        if (line.advanceGlCode == null || line.revenueGlCode == null)
            continue;

        // Identical on both legs. The GST return's B2CS inner query is a SELECT DISTINCT over
        // (gl_code, posting_key, collection_amount, remarks, report_type), so remarks decides
        // whether two otherwise-identical rows collapse. Keeping them equal and keeping the head
        // name means the CSV reads the same way as every other line on the voucher.
        String remarks = line.taxHeadCode;

        pair.add(fourSeriesLeg(demand, "40", line.advanceGlCode, line.settledAmount, remarks,
                postingDate, consumerCode, fund, fundCenter, businessArea, functionalArea,
                headerText, advanceDocNo, now));
        pair.add(fourSeriesLeg(demand, "50", line.revenueGlCode, line.settledAmount, remarks,
                postingDate, consumerCode, fund, fundCenter, businessArea, functionalArea,
                headerText, advanceDocNo, now));
    }

    return pair;
}

private FiReport fourSeriesLeg(Demand demand, String postingKey, String glCode, BigDecimal amount,
                               String remarks, Long postingDate, String consumerCode,
                               String fund, String fundCenter, String businessArea,
                               String functionalArea, String headerText, String advanceDocNo,
                               long now) {
    return FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(consumerCode)
            .remarks(remarks)
            .postingKey(postingKey)
            .glCode(glCode)
            .collectionAmount(amount)
            .assignment(advanceDocNo)
            .fund(fund)
            .fundCentre(fundCenter)
            .businessArea(businessArea)
            .functionalArea(functionalArea)
            .documentHeaderText(headerText)
            .docType("YX")
            .reportType(FiReportType.UPMKT_DEMDADV_4S)
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build();
}

/** One tax head's contribution to the advance-settled total, with both GLs it can post to. */
private static final class FourSeriesLine {
    private final String taxHeadCode;
    private final BigDecimal settledAmount;
    /** The head's ordinary revenue GL — the credit leg. Null when the head has no mapping row. */
    private final String revenueGlCode;
    /** The head's 4-Series receivable GL — the debit leg. Null when BMC supplied none. */
    private final String advanceGlCode;

    private FourSeriesLine(String taxHeadCode, BigDecimal settledAmount,
                           String revenueGlCode, String advanceGlCode) {
        this.taxHeadCode = taxHeadCode;
        this.settledAmount = settledAmount;
        this.revenueGlCode = revenueGlCode;
        this.advanceGlCode = advanceGlCode;
    }
}





public List<FiReport> buildCollectionFiReports(Demand demand,
                                               FiFlow flow,
                                               BigDecimal total,
                                               BigDecimal cgst,
                                               BigDecimal sgst,
                                               boolean reversal) {
    return buildCollectionFiReports(demand, flow, total, cgst, sgst, reversal, null);
}

/**
 * @param collectionDate the date the money changed hands; both doc date and posting date on a
 *                       collection voucher take it. Null falls back to the demand's tax period.
 */
public List<FiReport> buildCollectionFiReports(Demand demand,
                                               FiFlow flow,
                                               BigDecimal total,
                                               BigDecimal cgst,
                                               BigDecimal sgst,
                                               boolean reversal,
                                               Long collectionDate) {

    List<FiReport> reports = new ArrayList<>();

    total = total == null ? BigDecimal.ZERO : total;
    cgst = cgst == null ? BigDecimal.ZERO : cgst;
    sgst = sgst == null ? BigDecimal.ZERO : sgst;

    // Net rent collected at the bank (gross paid minus the GST components).
    // Defined as the residual so the GST_REGULAR voucher always balances.
    BigDecimal net = total.subtract(cgst).subtract(sgst);

    // The money leg's account, resolved ONCE per voucher so every leg of one receipt agrees and a
    // reversal costs at most one lookup. Cash keeps the interim account; cheque and DD are held in
    // cheques-in-hand until they clear.
    final String bankGl = resolveBankGl(demand, reversal);

    // Each leg names its FORWARD posting key; fiRow flips it for a reversal. The forward key is
    // what identifies the interim receipt (the money leg) and keys the legs read back off the
    // posted document, so it has to survive the flip: the money leg is a debit going out and a
    // credit coming back, and a rule written against the final key would move the receivable
    // instead on a cancellation.
    switch (flow) {

        case NON_GST_REGULAR:
            reports.add(fiRow(demand, "431409936", "50", reversal, total, "Receivable from Mun Mkt", collectionDate));
            reports.add(fiRow(demand, bankGl,  "40", reversal, total, "Bank/Interim Receipt", collectionDate));
            break;

        case GST_REGULAR:
            // A cancellation must give back exactly what was posted: a receipt booked under the
            // old net-of-GST shape keeps its four legs on reversal, whatever the flag says now.
            if (grossBankOnRegularCollection && !(reversal && wasPostedWithGstPayableDebit(demand.getFiReceiptNo()))) {
                reports.add(fiRow(demand, bankGl,  "40", reversal, total, "Bank/Interim Receipt", collectionDate));
                reports.add(fiRow(demand, "431409936", "50", reversal, total, "Receivable from Mun Mkt", collectionDate));
            } else {
                reports.add(fiRow(demand, bankGl,  "40", reversal, net, "Bank/Interim Receipt", collectionDate));
                reports.add(fiRow(demand, "431409936", "50", reversal, total, "Receivable from Mun Mkt", collectionDate));
                reports.add(fiRow(demand, "350200421", "40", reversal, cgst, "CGST Payable", collectionDate));
                reports.add(fiRow(demand, "350200422", "40", reversal, sgst, "SGST Payable", collectionDate));
            }
            break;

        case NON_GST_ADVANCE:
            reports.add(fiRow(demand, "350410215", "50", reversal, total, "Advance", collectionDate));
            reports.add(fiRow(demand, bankGl,  "40", reversal, total, "Bank/Interim Receipt", collectionDate));
            break;

        case GST_ADVANCE:
            reports.add(fiRow(demand, bankGl,  "40", reversal, total, "Bank/Interim Receipt", collectionDate));
            reports.add(fiRow(demand, "350410215", "50", reversal, total, "Advance", collectionDate));
            reports.add(fiRow(demand, "350200421", "50", reversal, cgst, "CGST Payable", collectionDate));
            reports.add(fiRow(demand, "350200422", "50", reversal, sgst, "SGST Payable", collectionDate));
            reports.add(fiRow(demand, "439300200", "40", reversal, cgst, "Advance CGST", collectionDate));
            reports.add(fiRow(demand, "439300201", "40", reversal, sgst, "Advance SGST", collectionDate));
            break;

        case DEPOSIT:
            reports.add(fiRow(demand, "340100300", "50", reversal, total, "Security Deposit", collectionDate));
            reports.add(fiRow(demand, bankGl,  "40", reversal, total, "Bank/Interim Receipt", collectionDate));
            break;

        default:
            break;
    }

    // Drop any zero/null-amount rows defensively.
    reports.removeIf(r -> r.getCollectionAmount() == null
            || r.getCollectionAmount().compareTo(BigDecimal.ZERO) == 0);

    return reports;
}

/**
 * Build a single collection FI report row with the fields shared across all
 * emarket collection flows.
 */
/**
 * The date money actually changed hands, for the collection voucher's doc and posting date.
 * Resolved from the payment being processed — by id where the bill carries one, else by its
 * transaction number. Deliberately NOT taken from getMarketEssentialInfo, whose unordered join
 * returns one row per bill the demand ever appeared on.
 *
 * <p>Returns null when the payment cannot be found, and the caller then keeps the demand's tax
 * period, which is the behaviour that shipped before.
 */
public Long getCollectionDate(String paymentId, String transactionNumber) {

    try {
        if (paymentId != null && !paymentId.isEmpty()) {
            List<Long> byId = jdbcTemplate.queryForList(
                    "SELECT transactiondate FROM egcl_payment WHERE id = ?", Long.class, paymentId);
            if (!byId.isEmpty() && byId.get(0) != null)
                return byId.get(0);
        }
        if (transactionNumber != null && !transactionNumber.isEmpty()) {
            List<Long> byTxn = jdbcTemplate.queryForList(
                    "SELECT transactiondate FROM egcl_payment WHERE transactionnumber = ? "
                  + "ORDER BY transactiondate ASC LIMIT 1", Long.class, transactionNumber);
            if (!byTxn.isEmpty() && byTxn.get(0) != null)
                return byTxn.get(0);
        }
    } catch (DataAccessException e) {
        log.error("Could not resolve the collection date for payment {} / txn {}",
                paymentId, transactionNumber, e);
    }
    log.warn("No payment date for payment {} / txn {}; collection voucher keeps the demand's tax period",
            paymentId, transactionNumber);
    return null;
}

/**
 * The dimensions one leg of a collection voucher posts with, or null to keep the licensee's market.
 *
 * <p><b>The interim receipt</b> (the money leg) is the only leg that follows the collecting CFC,
 * and it posts on BMC's fixed rule: see {@link #interimReceiptDimensions}. Every other leg stays
 * with the market, including the forward-40 ones: the advance-GST legs clear against a
 * demand-side netting release built from the market's dimensions, and the GST payable debits of
 * the old net-of-GST shape are not money. That matches BMC's own upload, where 431910000 is a
 * forward-40 leg and still carries the property's ward.
 *
 * <p><b>Every other leg on a reversal</b> is mirrored from its own forward counterpart, never
 * re-derived. The market a licensee's stall belongs to can be re-pointed by an ordinary asset edit
 * between collection and cancellation, and a re-derived compensating document would then carry two
 * business areas where the original carried one. A leg with nothing posted to mirror falls back to
 * the market.
 */
private FiDimensions resolveLegDimensions(Demand demand, String glCode, String forwardPostingKey,
                                          boolean reversal) {

    if (isInterimReceiptLeg(glCode, forwardPostingKey))
        return interimReceiptLegDimensions(demand, glCode, forwardPostingKey, reversal);

    if (!reversal)
        return null;

    FiDimensions mirrored = postedLeg(demand, glCode, forwardPostingKey);
    return mirrored != null && mirrored.isComplete() ? mirrored : null;
}

/** The money leg: cash interim or cheques-in-hand, always debited on the forward voucher. */
private boolean isInterimReceiptLeg(String glCode, String forwardPostingKey) {
    return "40".equals(forwardPostingKey)
            && (cashBankGlCode.equals(glCode) || chequeBankGlCode.equals(glCode));
}

/**
 * The interim receipt's dimensions, in both directions.
 *
 * <p>{@code collectingDimensions} carries the rule's set for this receipt: the collecting CFC ward,
 * or the market's business area when no ward can be established. It is null when the rule is off,
 * and also when the rule is on but there is no business area to build on at all. Either way the
 * forward leg then keeps the market.
 *
 * <p><b>Reversal: give back exactly what the money leg was booked with.</b> SAP nets the interim
 * account per business area AND fund centre, so the cancellation must carry the original's values,
 * whatever they are. A receipt posted before the rule was corrected carries the market's fund centre
 * and 55800000000. Rebuilding its reversal by the rule would leave the original standing at the old
 * fund centre and the reversal at the new one, and neither would ever clear. That is exactly the case
 * where the original was already uploaded to SAP and cannot be rewritten. Correcting such a receipt is
 * the job of docs/sql/fix_interim_receipt_dimensions.sql, which re-states the collection and its
 * reversal together.
 *
 * <p>Two fallbacks, rule on only. A posted money leg that carries a business area but not all four
 * values is rebuilt by the rule at THAT business area, so the reversal still lands where the money was
 * booked. With nothing posted, the collecting set is used.
 */
private FiDimensions interimReceiptLegDimensions(Demand demand, String glCode, String forwardPostingKey,
                                                 boolean reversal) {

    FiDimensions collecting = demand.getCollectingDimensions();
    boolean ruleOn = collecting != null && collecting.isComplete();

    if (!reversal)
        return ruleOn ? collecting : null;

    FiDimensions posted = postedLeg(demand, glCode, forwardPostingKey);
    if (posted != null && posted.isComplete())
        return posted;
    if (!ruleOn)
        return null;

    if (posted != null) {
        FiDimensions atBookedArea = interimReceiptDimensions(
                notBlank(posted.getFund()) ? posted.getFund() : collecting.getFund(),
                posted.getBusinessArea());
        if (atBookedArea != null)
            return atBookedArea;
    }
    return collecting;
}

/** The dimensions this leg was posted with on the original receipt, or null. */
private static FiDimensions postedLeg(Demand demand, String glCode, String forwardPostingKey) {
    Map<String, FiDimensions> posted = demand.getPostedLegDimensions();
    return posted == null ? null : posted.get(postedLegKey(glCode, forwardPostingKey));
}

private static boolean notBlank(String value) {
    return value != null && !value.trim().isEmpty();
}

/**
 * @param forwardPostingKey the key this leg carries on the ORIGINAL voucher, before any reversal
 *                          flip. Both the flip and the dimension rule are derived from it.
 */
private FiReport fiRow(Demand demand, String glCode, String forwardPostingKey, boolean reversal,
                       BigDecimal amount, String remarks, Long collectionDate) {
    long now = System.currentTimeMillis();
    // A collection is recognised when the money arrives, so both dates are the collection date.
    // The demand's tax period is the fallback only when the payment cannot be resolved.
    Long voucherDate = collectionDate != null ? collectionDate : demand.getTaxPeriodFrom();
    String postingKey = pk(forwardPostingKey, reversal);

    FiDimensions dims = resolveLegDimensions(demand, glCode, forwardPostingKey, reversal);

    String fund = dims != null ? dims.getFund() : demand.getFund();
    String fundCentre = dims != null ? dims.getFundCentre() : demand.getFundCenter();
    String businessArea = dims != null ? dims.getBusinessArea() : demand.getBusinessArea();
    String functionalArea = dims != null ? dims.getFunctionalArea() : demand.getFunctionalArea();

    return FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(voucherDate)
            .postingDate(voucherDate)
            .referenceNo(demand.getConsumerCode())
            .documentHeaderText(demand.getFiReceiptNo())
            .postingKey(postingKey)
            .glCode(glCode)
            .collectionAmount(amount)
            .fund(fund)
            .fundCentre(fundCentre)
            .businessArea(businessArea)
            .functionalArea(functionalArea)
            .remarks(remarks)
            .paymentModeDetails(demand.getPaymentMode())
            // SAP Assignment (ZUONR). Only the GST-advance legs carry it, set to this
            // receipt's transaction number — the same value the later demand's netting leg
            // carries — so F.13/FB05 can match the two and clear the advance GST. Every
            // other GL keeps the blank Assignment it has today.
            .assignment(resolveAssignment(demand, glCode))
            .docType("YY")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build();
}

/**
 * Was this receipt originally posted as a SPLIT mixed voucher (an advance leg and a regular
 * leg), rather than as one voucher booking everything to the advance account?
 *
 * <p>A cancellation must give back exactly what was posted. The split is behind a property, so
 * a receipt taken before it was switched on — or after it was switched back off — would
 * otherwise be reversed under today's rules rather than the ones it was booked under, leaving
 * the advance account and the receivable each wrong by the arrears portion. Reading the posted
 * document instead makes the reversal symmetric no matter how the flag has moved since.
 *
 * <p>Identified by the pair of pk-50 legs only a split voucher carries: the advance AND the
 * receivable. A single advance voucher credits only 350410215; a pure regular voucher credits
 * only the receivable.
 */
public boolean wasCollectionSplit(String transactionNumber) {

    if (transactionNumber == null || transactionNumber.trim().isEmpty())
        return false;

    String sql =
        "SELECT count(DISTINCT gl_code) FROM public.eg_emarket_fi_report_collection " +
        "WHERE document_header_text = ? AND report_type = ? " +
        "  AND posting_key = '50' AND gl_code IN ('431409936', '350410215')";

    try {
        Integer distinctLegs = jdbcTemplate.queryForObject(sql,
                new Object[] { transactionNumber, FiReportType.UPMKT_COLL }, Integer.class);
        return distinctLegs != null && distinctLegs == 2;
    } catch (DataAccessException e) {
        log.error("Could not tell whether receipt {} was posted as a split voucher; "
                + "reversing it as a single voucher", transactionNumber, e);
        return false;
    }
}

/**
 * Was this receipt posted to the ADVANCE account, or against the receivable?
 *
 * <p>Returns {@code TRUE} when the posted document credits 350410215 (a pure advance receipt, or
 * the advance leg of a split mixed receipt), {@code FALSE} when it credits only the receivable,
 * and {@code null} when neither is present — a deposit, or no document at all — in which case the
 * caller keeps whatever flow it computed.
 *
 * <p>Exists so a cancellation gives back exactly what was posted. Whether a receipt counts as an
 * advance is decided by rules that sit behind a property, so a receipt taken under one setting
 * could otherwise be reversed under another: the advance account would be debited for a receipt
 * that credited the receivable, or the other way round, leaving both GLs wrong by the full receipt
 * value while the voucher still balanced.
 */
public Boolean wasPostedAsAdvance(String transactionNumber) {

    if (transactionNumber == null || transactionNumber.trim().isEmpty())
        return null;

    String sql =
        "SELECT COUNT(*) FILTER (WHERE gl_code = '350410215') AS advance_legs, " +
        "       COUNT(*) FILTER (WHERE gl_code = '431409936') AS receivable_legs " +
        "FROM public.eg_emarket_fi_report_collection " +
        "WHERE document_header_text = ? AND report_type = ? AND posting_key = '50'";

    try {
        Map<String, Object> counts = jdbcTemplate.queryForMap(sql,
                new Object[] { transactionNumber, FiReportType.UPMKT_COLL });
        long advance = ((Number) counts.get("advance_legs")).longValue();
        long receivable = ((Number) counts.get("receivable_legs")).longValue();
        if (advance > 0)
            return Boolean.TRUE;
        return receivable > 0 ? Boolean.FALSE : null;
    } catch (DataAccessException e) {
        log.error("Could not read how receipt {} was posted; reversing on the computed flow",
                transactionNumber, e);
        return null;
    }
}

/**
 * Did this receipt's forward voucher debit the GST payable (the pre-2026-08-25 net-of-GST shape
 * for a regular GST collection)? Read from the posted rows so a reversal mirrors the document
 * that exists rather than the shape the flag would produce today. False when nothing is on file
 * or the lookup cannot run (unit tests construct this repository without a JdbcTemplate).
 */
private boolean wasPostedWithGstPayableDebit(String transactionNumber) {
    if (jdbcTemplate == null || transactionNumber == null || transactionNumber.trim().isEmpty())
        return false;
    String sql =
        "SELECT count(*) FROM public.eg_emarket_fi_report_collection " +
        "WHERE document_header_text = ? AND report_type = ? AND posting_key = '40' " +
        "  AND gl_code IN ('350200421','350200422')";
    try {
        Integer n = jdbcTemplate.queryForObject(sql, new Object[] { transactionNumber, FiReportType.UPMKT_COLL }, Integer.class);
        return n != null && n > 0;
    } catch (DataAccessException e) {
        log.error("Could not read the posted shape of receipt {}; reversing on the current rule", transactionNumber, e);
        return false;
    }
}

/** True for the CGST/SGST Advance asset GLs that must clear against a demand's netting leg. */
private boolean isGstAdvanceGl(String glCode) {
    return GL_CGST_ADVANCE.equals(glCode) || GL_SGST_ADVANCE.equals(glCode);
}

/**
 * The GL a collection voucher's money leg posts to, chosen by how the money arrived.
 *
 * <p>Cash (and anything not instrument-backed) keeps the bank/interim account. A cheque or DD is
 * not money in the bank until it clears, so it posts to cheques-in-hand instead.
 *
 * <p><b>A reversal gives back exactly what was posted.</b> A receipt taken before this split
 * existed sits on the cash GL whatever its payment mode was; reversing it onto the cheque GL would
 * leave both accounts carrying a balance that never nets off. So a reversal reuses the account the
 * original collection actually used, and only falls back to the payment-mode rule when the original
 * cannot be read. This mirrors {@link #wasPostedWithGstPayableDebit}, which guards the GST legs for
 * exactly the same reason.
 */
private String resolveBankGl(Demand demand, boolean reversal) {
    if (reversal) {
        String posted = postedBankGl(demand.getFiReceiptNo(), demand.getConsumerCode());
        if (posted != null)
            return posted;
    }
    return isInstrumentBacked(demand) ? chequeBankGlCode : cashBankGlCode;
}

/** True when the receipt was taken against a physical instrument that has yet to clear. */
private boolean isInstrumentBacked(Demand demand) {
    String mode = demand.getPaymentMode() == null
            ? "" : demand.getPaymentMode().trim().toUpperCase(Locale.ROOT);
    return "CHEQUE".equals(mode) || "DD".equals(mode);
}

/**
 * The money-leg GL an earlier collection on this receipt was actually posted to, or null when
 * there is none to read. Restricted to the two bank accounts so a receivable or GST leg can never
 * be mistaken for the money leg.
 *
 * <p>Scoped to the LICENCE as well as the receipt, and that is load-bearing rather than defensive:
 * {@code transactionnumber} carries no unique constraint and is minted from idgen as ten RANDOM
 * digits, so {@code document_header_text} alone can land on an unrelated receipt and reverse this
 * one onto the wrong bank account. {@code reference_no} is written from the demand's consumer code
 * by {@link #fiRow}, so an equality match on it is exact.
 */
private String postedBankGl(String transactionNumber, String consumerCode) {
    if (jdbcTemplate == null || transactionNumber == null || transactionNumber.trim().isEmpty()
            || consumerCode == null || consumerCode.trim().isEmpty())
        return null;
    String sql =
        "SELECT gl_code FROM public.eg_emarket_fi_report_collection " +
        "WHERE document_header_text = ? AND reference_no = ? AND report_type = ? " +
        "  AND posting_key = '40' AND gl_code IN (?, ?) LIMIT 1";
    try {
        List<String> gl = jdbcTemplate.queryForList(sql, String.class,
                transactionNumber, consumerCode, FiReportType.UPMKT_COLL,
                cashBankGlCode, chequeBankGlCode);
        return gl.isEmpty() ? null : gl.get(0);
    } catch (DataAccessException e) {
        log.error("Could not read the posted bank GL of receipt {} / licence {}; posting on the payment-mode rule",
                transactionNumber, consumerCode, e);
        return null;
    }
}

/**
 * SAP Assignment (ZUONR) for one leg.
 *
 * <p>Two legs carry one, for unrelated reasons. The GST-advance legs carry the receipt's
 * transaction number, which is the key F.13/FB05 uses to clear the advance against the later
 * demand's netting leg. The cheques-in-hand leg carries the cheque number, so finance can identify
 * the instrument sitting in that account — BMC: "the cheque number is to be mentioned in the
 * Assignment column for GL 450210010". Every other leg keeps the blank Assignment it has today.
 */
private String resolveAssignment(Demand demand, String glCode) {
    if (isGstAdvanceGl(glCode))
        return demand.getFiReceiptNo();
    if (chequeBankGlCode.equals(glCode))
        return instrumentNumberFor(demand.getFiReceiptNo(), demand.getConsumerCode());
    return null;
}

/**
 * The cheque/DD number on the payment this receipt belongs to.
 *
 * <p>Never blocks the posting: the Assignment is a reconciliation aid, so a lookup failure leaves
 * it blank rather than failing the voucher and, with it, the collection.
 */
private String instrumentNumberFor(String transactionNumber, String consumerCode) {
    if (jdbcTemplate == null || transactionNumber == null || transactionNumber.trim().isEmpty()
            || consumerCode == null || consumerCode.trim().isEmpty())
        return null;
    // The payment must belong to THIS licence. Load-bearing: transactionnumber has no unique
    // constraint and is ten random idgen digits, so matching on it alone can land on an unrelated
    // payment and stamp a stranger's cheque number onto this voucher. Matched on the digits of the
    // bill's consumer code, the same normalisation used to get from a service-suffixed reference
    // ("5000000284rf") back to a licence number; the blank guard stops a reference with no digits
    // matching every blank consumer code.
    String sql =
        "SELECT NULLIF(p.instrumentnumber, '') FROM egcl_payment p "
      + " WHERE p.transactionnumber = ? "
      + "   AND regexp_replace(?, '[^0-9]', '', 'g') <> '' "
      + "   AND EXISTS (SELECT 1 FROM egcl_paymentdetail pd "
      + "                 JOIN egcl_bill b ON b.id = pd.billid "
      + "                WHERE pd.paymentid = p.id "
      + "                  AND regexp_replace(b.consumercode, '[^0-9]', '', 'g') "
      + "                      = regexp_replace(?, '[^0-9]', '', 'g')) "
      + " LIMIT 1";
    try {
        List<String> found = jdbcTemplate.queryForList(sql, String.class,
                transactionNumber, consumerCode, consumerCode);
        return found.isEmpty() ? null : found.get(0);
    } catch (DataAccessException e) {
        log.error("Could not resolve the instrument number for receipt {} / licence {}; Assignment left blank",
                transactionNumber, consumerCode, e);
        return null;
    }
}

/**
 * GST actually posted to the advance-asset GLs by a receipt, keyed by GL code.
 *
 * <p>A cancellation must give back exactly what was taken, not what today's rules would
 * compute. Advance GST used to be booked for a single month regardless of how many months
 * the advance covered; recomputing at cancellation time would now reverse {@code months x}
 * that amount and drive the advance GL deeply negative for every receipt taken before the
 * fix. Reading the posted rows back makes pre-fix and post-fix receipts reverse correctly
 * without needing to know which is which.
 *
 * <p>Signed, so an already-reversed receipt yields zero rather than reversing twice.
 * Returns an empty map when nothing was posted, in which case the caller keeps its
 * computed amounts (the path a brand-new receipt takes).
 */
public Map<String, BigDecimal> getPostedAdvanceGst(String fiReceiptNo, String consumerCode) {

    if (fiReceiptNo == null || fiReceiptNo.isEmpty() || consumerCode == null || consumerCode.isEmpty())
        return Collections.emptyMap();

    String sql =
        "SELECT gl_code, " +
        "       SUM(CASE WHEN posting_key = '40' THEN collection_amount ELSE -collection_amount END) AS posted " +
        "FROM public.eg_emarket_fi_report_collection " +
        // Scoped to the licensee as well as the receipt: document_header_text is a
        // transaction number, not a guaranteed-unique key, and summing another licensee's
        // advance GST into this cancellation would reverse the wrong amount.
        "WHERE document_header_text = ? AND reference_no = ? AND gl_code IN (?, ?) " +
        "GROUP BY gl_code";

    try {
        Map<String, BigDecimal> posted = new HashMap<>();
        jdbcTemplate.query(sql, new Object[] { fiReceiptNo, consumerCode, GL_CGST_ADVANCE, GL_SGST_ADVANCE },
                rs -> { posted.put(rs.getString("gl_code"), rs.getBigDecimal("posted")); });
        return posted;
    } catch (DataAccessException e) {
        log.error("Could not read posted advance GST for receipt {}", fiReceiptNo, e);
        return Collections.emptyMap();
    }
}

/**
 * Compensating rows that undo a demand's GST net-off when the advance it drew on is
 * cancelled or dishonoured.
 *
 * <p>Without these the collection reversal credits the whole GST advance back while the
 * demand-side releases stay posted, so the advance GL goes negative and the liability that
 * was netted away is never restored — GST payable ends up understated.
 *
 * <p>The rows are read back from what was actually posted and mirrored, rather than
 * recomputed, so a reversal can never disagree with its original even if the netting rule
 * changes later. Returns empty for a demand that carries no netting legs, which keeps a
 * cancellation with nothing to undo byte-identical to today.
 */
public List<FiReport> buildGstNettingReversalFiReports(String settledDemandId, Long postingDate) {

    if (settledDemandId == null)
        return Collections.emptyList();

    // Reverse the RESIDUAL net-off, not the raw history.
    //
    // A demand's net-off can be undone by either of two independent paths — cancelling the
    // advance receipt (here) or cancelling the demand itself (emarket-v1 DemandReversalService).
    // Mirroring every upmktdemdadv leg would let the second path reverse what the first already
    // reversed: GST payable over-credited, the advance asset over-debited, and — worst —
    // remainingGstAdvance() would then read a phantom positive balance and let FUTURE demands
    // net against an advance that no longer exists.
    //
    // The residual is measured on the ADVANCE GLs alone (439300200/201), which appear on the
    // demand side only ever as netting legs: released at posting key 50, restored at 40. The
    // payable GLs cannot be used for this because the ordinary demand GST line shares their
    // GL and key. A residual of zero means the net-off is already undone — emit nothing.
    String residualSql =
        "SELECT gl_code, " +
        "       SUM(CASE WHEN posting_key = '50' THEN collection_amount ELSE -collection_amount END) AS residual, " +
        "       MIN(reference_no) AS reference_no, MIN(document_header_text) AS document_header_text, " +
        "       MIN(fund) AS fund, MIN(fund_centre) AS fund_centre, MIN(functional_area) AS functional_area, " +
        "       MIN(business_area) AS business_area, " +
        "       MIN(assignment) AS assignment, MIN(doc_date) AS doc_date " +
        "FROM public.eg_emarket_fi_report " +
        "WHERE transaction_number = ? AND report_type IN (?, ?) AND gl_code IN (?, ?) " +
        "GROUP BY gl_code HAVING SUM(CASE WHEN posting_key = '50' THEN collection_amount ELSE -collection_amount END) > 0";

    try {
        long now = System.currentTimeMillis();
        List<FiReport> reversals = new ArrayList<>();

        jdbcTemplate.query(residualSql,
                new Object[] { settledDemandId, FiReportType.UPMKT_DEMDADV, FiReportType.UPMKT_DEMDREV,
                               GL_CGST_ADVANCE, GL_SGST_ADVANCE },
                rs -> {
                    String advanceGl = rs.getString("gl_code");
                    BigDecimal residual = rs.getBigDecimal("residual");
                    boolean cgstSide = GL_CGST_ADVANCE.equals(advanceGl);
                    String payableGl = cgstSide ? GL_CGST_PAYABLE : GL_SGST_PAYABLE;
                    // Null postingDate keeps each reversal leg on the doc date of the legs it
                    // mirrors, so original and reversal always fall in the same report window.
                    Long docDate = postingDate != null ? postingDate : (Long) rs.getObject("doc_date");

                    // Restore the liability that was netted away, and give the advance asset back.
                    reversals.add(nettingReversalRow(settledDemandId, rs, payableGl, "50",
                            cgstSide ? "CGST Payable" : "SGST Payable", residual, docDate, now));
                    reversals.add(nettingReversalRow(settledDemandId, rs, advanceGl, "40",
                            cgstSide ? "CGST Advance" : "SGST Advance", residual, docDate, now));
                });

        if (!reversals.isEmpty())
            log.info("Reversing residual GST net-off on demand {}: {} legs", settledDemandId, reversals.size());
        return reversals;

    } catch (DataAccessException e) {
        log.error("Could not read netting rows to reverse for settled demand {}", settledDemandId, e);
        return Collections.emptyList();
    }
}

/**
 * Compensating rows that put a re-opened demand's dues back on the receivable when the
 * advance it was settled from is cancelled.
 *
 * <p>The forward document split its balancing debit: the part covered by the advance went to
 * 350410215, the remainder to the receivable. Cancelling the advance receipt credits the
 * whole advance back on the collection side and re-opens the demand, but nothing undoes that
 * demand-side debit — so 350410215 is left permanently overstated and dues the licensee now
 * genuinely owes never reappear on the receivable. This emits the missing pair.
 *
 * <p>Residual-based like the GST netting reversal, so running it twice is a no-op.
 */
public List<FiReport> buildAdvanceSettlementReversalFiReports(String settledDemandId) {

    if (settledDemandId == null)
        return Collections.emptyList();

    String sql =
        "SELECT SUM(CASE WHEN posting_key = '40' THEN collection_amount ELSE -collection_amount END) AS residual, " +
        "       MIN(reference_no) AS reference_no, MIN(document_header_text) AS document_header_text, " +
        "       MIN(fund) AS fund, MIN(fund_centre) AS fund_centre, MIN(functional_area) AS functional_area, " +
        "       MIN(business_area) AS business_area, MIN(assignment) AS assignment, MIN(doc_date) AS doc_date " +
        "FROM public.eg_emarket_fi_report " +
        "WHERE transaction_number = ? AND report_type IN (?, ?) AND gl_code = '350410215' " +
        "HAVING SUM(CASE WHEN posting_key = '40' THEN collection_amount ELSE -collection_amount END) > 0";

    try {
        long now = System.currentTimeMillis();
        List<FiReport> rows = new ArrayList<>();
        jdbcTemplate.query(sql,
                new Object[] { settledDemandId, FiReportType.UPMKT_DEMDADV, FiReportType.UPMKT_DEMDREV },
                rs -> {
                    BigDecimal residual = rs.getBigDecimal("residual");
                    Long docDate = (Long) rs.getObject("doc_date");
                    // Give the advance liability back, and put the dues back on the receivable.
                    rows.add(nettingReversalRow(settledDemandId, rs, "350410215", "50",
                            "Advance", residual, docDate, now));
                    rows.add(nettingReversalRow(settledDemandId, rs, receivableGlCode, "40",
                            "Receivable from Mun Mkt", residual, docDate, now));
                });
        return rows;
    } catch (DataAccessException e) {
        log.error("Could not read the advance-settlement leg to reverse for demand {}", settledDemandId, e);
        return Collections.emptyList();
    }
}

/**
 * Mirror the per-tax-head 4-Series pair when the ADVANCE RECEIPT behind a demand is cancelled or
 * its cheque is dishonoured.
 *
 * <p>The sibling of the emarket-v1 reversal path. Two entirely separate routes undo a
 * demand-against-advance — cancelling the demand (emarket-v1 DemandReversalService) and
 * cancelling the receipt that funded it (here) — and neither can rely on the other having run.
 * Without this, a bounced advance cheque correctly returns the dues to the receivable while the
 * 4-Series account stays debited and the revenue stays credited twice, permanently.
 *
 * <p>Reads back what was POSTED, exactly like {@link #buildGstNettingReversalFiReports} and
 * {@link #buildAdvanceSettlementReversalFiReports}: the amounts, the doc date and all four
 * dimensions come off the forward rows, so the mirror can never disagree with its original nor
 * fall into a different GST return period.
 *
 * <p>The residual is SIGNED and the posting key follows its sign. Filtering {@code > 0} would
 * keep the receivable debit and drop the revenue credit, leaving the document unbalanced by the
 * whole amount. Running it twice nets to zero and emits nothing.
 */
public List<FiReport> buildFourSeriesReversalFiReports(String settledDemandId) {

    // Deliberately NOT gated on advanceFourSeriesEnabled. The flag is a kill switch for the
    // WRITE; gating the unwind too makes the switch a trap. A pair posted while it was on is a
    // persisted row, and turning the flag off would strand it: the receivable would stay debited
    // and the revenue credited for money that later bounced, with nothing in either service ever
    // touching 431409937..977 again. With no pair ever posted this query matches nothing, so
    // leaving it ungated is byte-identical for a database that never had the flag on. Matches
    // buildAdvanceSettlementReversalFiReports, buildGstNettingReversalFiReports and emarket-v1's
    // own twin, none of which are flag-gated either.
    if (settledDemandId == null)
        return Collections.emptyList();

    String sql =
        "SELECT gl_code, " +
        "       SUM(CASE WHEN posting_key = '40' THEN collection_amount ELSE -collection_amount END) AS residual, " +
        "       MIN(reference_no) AS reference_no, MIN(document_header_text) AS document_header_text, " +
        "       MIN(fund) AS fund, MIN(fund_centre) AS fund_centre, MIN(functional_area) AS functional_area, " +
        "       MIN(business_area) AS business_area, MIN(assignment) AS assignment, " +
        "       MIN(doc_date) AS doc_date, remarks " +
        "FROM public.eg_emarket_fi_report " +
        "WHERE transaction_number = ? AND report_type IN (?, ?) " +
        "GROUP BY gl_code, remarks " +
        "HAVING SUM(CASE WHEN posting_key = '40' THEN collection_amount ELSE -collection_amount END) <> 0";

    try {
        long now = System.currentTimeMillis();
        List<FiReport> rows = new ArrayList<>();
        jdbcTemplate.query(sql,
                new Object[] { settledDemandId, FiReportType.UPMKT_DEMDADV_4S,
                               FiReportType.UPMKT_DEMDADV_4S_REV },
                rs -> {
                    BigDecimal residual = rs.getBigDecimal("residual");
                    if (residual == null || residual.signum() == 0)
                        return;
                    // A net debit is given back as a credit, and the other way round.
                    String mirroredKey = residual.signum() > 0 ? "50" : "40";
                    FiReport leg = nettingReversalRow(settledDemandId, rs, rs.getString("gl_code"),
                            mirroredKey, rs.getString("remarks"), residual.abs(),
                            (Long) rs.getObject("doc_date"), now);
                    // Tagged here: ReceiptServiceV2 stamps UPMKT_COLREV/UPMKT_DEMDREV over the rows
                    // it is handed, and both sit inside the GST return's report_type filter.
                    leg.setReportType(FiReportType.UPMKT_DEMDADV_4S_REV);
                    rows.add(leg);
                });
        return rows;
    } catch (DataAccessException e) {
        log.error("Could not read the 4-Series pair to reverse for demand {}", settledDemandId, e);
        return Collections.emptyList();
    }
}

/**
 * GLs that can never be a demand's receivable — the advance account itself and the four GST
 * legs. Everything else a demand document debits is, by construction, the receivable.
 */
private static final String NON_RECEIVABLE_GLS =
        "'350410215','350200421','350200422','439300200','439300201'";

/**
 * The per-tax-head 4-Series RECEIVABLE accounts BMC allocated for a demand raised against an
 * advance. A contiguous block, so range comparison is exact and needs no list to be maintained.
 * Compared as text because gl_code is a varchar; every code in the range is 9 digits, so the
 * lexicographic and numeric orderings agree.
 */
private static final String FOUR_SERIES_GL_FIRST = "431409937";
private static final String FOUR_SERIES_GL_LAST  = "431409977";

/**
 * Relieve the receivable of a PRE-EXISTING demand that an apportion has just settled from the
 * licensee's advance.
 *
 * <p>{@code DemandService.apportionAdvanceIfExist} spreads an advance across every open demand
 * of a licensee, but only the demand being CREATED is routed through {@link #save}, which
 * writes FI. The others go to {@link #update}, which writes none — so their dues are marked
 * collected in the ledger while the receivable they were raised against stays open for ever.
 *
 * <p>The missing document is <em>not</em> the one {@code save()} emits. The revenue credits were
 * already posted when the demand itself was created; re-emitting them would double-count income.
 * All that is missing is the two-line relief:
 *
 * <pre>
 *   Dr 350410215                        (the advance is drawn down)
 *   Cr &lt;the demand's own receivable GL&gt;  (the dues are cleared)
 * </pre>
 *
 * <p>The GL and all four FI dimensions are read back from the demand's OWN forward document
 * rather than taken from the Demand object. A demand raised before the receivable-GL cutover
 * sits on 431190300, and crediting today's GL would leave the old account overstated for ever;
 * and the Demand objects reaching this path come from the apportion service without fund,
 * fundCentre, businessArea or functionalArea at all.
 *
 * <p>Idempotent by construction: the amount is capped at the receivable still open on the
 * demand, so a replay finds nothing left to relieve and posts nothing. A demand with no forward
 * document, or none still open, emits nothing at all — relief is never invented for a receivable
 * that was never booked.
 *
 * <p>Deliberately writes no {@code eg_emarket_demand_settlement_info} row. That table drives the
 * cancellation unwind in {@code ReceiptServiceV2}, which zeroes <em>every</em> positive
 * collection amount on the demands it finds; listing a pre-existing demand there would let the
 * cancellation of an advance also wipe cash collected against the same demand. The relief posted
 * here is still residual-based, so {@link #buildAdvanceSettlementReversalFiReports} unwinds it
 * correctly whenever it is invoked for the demand.
 */
@Transactional
public void postAdvanceSettlementFiReports(Map<String, BigDecimal> settlements) {

    if (settlements == null || settlements.isEmpty())
        return;

    // The receivable is the only GL a demand document debits once the advance, the GST legs and
    // the 4-Series receivable accounts are excluded; revenue is credited, so its signed balance is
    // negative and the HAVING drops it.
    //
    // The 4-Series exclusion matters and the ORDER BY is why. Picking by largest residual was safe
    // only while the receivable was the single debit. With the pair posted, a demand whose advance
    // covered most of it carries a large per-head 4-Series debit and a small remaining receivable —
    // 431409938 = +720 against 431409936 = +280 — and the 4-Series would win. The relief would then
    // credit a receivable sub-account instead of the control account, leaving 431409936 overstated
    // for ever, and cap the amount against 720 rather than 280, which breaks the replay-idempotency
    // this method documents above.
    //
    // Excluded by GL RANGE rather than by report_type: report_type is set by the builders and a
    // future write site could lose it, and it cannot help rows that are already written.
    // The receivable GL is also preferred explicitly in the ORDER BY, so the residual-size tiebreak
    // only ever applies to the pre-cutover 431190300 case this method already supports.
    String sql =
        "SELECT gl_code, " +
        "       SUM(CASE WHEN posting_key = '40' THEN collection_amount ELSE -collection_amount END) AS residual, " +
        "       MIN(doc_date) AS doc_date, MIN(reference_no) AS reference_no, " +
        "       MIN(document_header_text) AS document_header_text, MIN(assignment) AS assignment, " +
        "       MIN(fund) AS fund, MIN(fund_centre) AS fund_centre, " +
        "       MIN(functional_area) AS functional_area, MIN(business_area) AS business_area " +
        "FROM public.eg_emarket_fi_report " +
        "WHERE transaction_number = ? AND gl_code NOT IN (" + NON_RECEIVABLE_GLS + ") " +
        "  AND gl_code NOT BETWEEN '" + FOUR_SERIES_GL_FIRST + "' AND '" + FOUR_SERIES_GL_LAST + "' " +
        "GROUP BY gl_code " +
        "HAVING SUM(CASE WHEN posting_key = '40' THEN collection_amount ELSE -collection_amount END) > 0 " +
        "ORDER BY (gl_code = ?) DESC, 2 DESC LIMIT 1";

    long now = System.currentTimeMillis();
    List<FiReport> rows = new ArrayList<>();

    for (Map.Entry<String, BigDecimal> entry : settlements.entrySet()) {

        String demandId = entry.getKey();
        BigDecimal settled = entry.getValue();
        if (demandId == null || settled == null || settled.signum() <= 0)
            continue;

        try {
            // Taken BEFORE the residual is read. The cap is read-then-write, so two concurrent
            // /demand/_create calls apportioning the same pre-existing demand would otherwise
            // both see the full receivable open and both relieve it. Locking after the read
            // would serialise the writes but not the decision, which is the part that matters.
            lockAdvanceSettlement(demandId);

            List<Map<String, Object>> forward =
                    jdbcTemplate.queryForList(sql, new Object[] { demandId, receivableGlCode });
            if (forward.isEmpty()) {
                log.warn("Demand {} has no open receivable to relieve; advance settlement of {} posts no FI",
                        demandId, settled);
                continue;
            }

            Map<String, Object> fwd = forward.get(0);
            BigDecimal residual = (BigDecimal) fwd.get("residual");
            BigDecimal amount = settled.min(residual);
            if (amount.signum() <= 0)
                continue;

            if (amount.compareTo(settled) != 0)
                log.warn("Advance settlement of {} on demand {} capped to the {} still open on {}",
                        settled, demandId, amount, fwd.get("gl_code"));

            Long docDate = (Long) fwd.get("doc_date");

            rows.add(settlementReliefRow(demandId, fwd, GL_ADVANCE, "40", "Advance", amount, docDate, now));
            rows.add(settlementReliefRow(demandId, fwd, (String) fwd.get("gl_code"), "50",
                    "Receivable from Mun Mkt", amount, docDate, now));

            log.info("Advance-settlement relief for demand {}: Dr {} / Cr {} {}",
                    demandId, GL_ADVANCE, fwd.get("gl_code"), amount);

        } catch (DataAccessException e) {
            log.error("Could not post the advance-settlement relief for demand {}", demandId, e);
        }
    }

    if (!rows.isEmpty())
        batchInsertDemandFiReports(rows);
}

/**
 * Serialise advance-settlement relief for one demand across concurrent requests. Keyed on the
 * demand rather than the licensee: that is exactly the row whose residual is being read and
 * written, and it keeps this off the lock {@link #save} takes for the GST net-off, so the two
 * paths cannot deadlock against each other. Transaction-scoped, released at commit or rollback.
 * A failure to acquire is logged and ignored — the residual cap still bounds the relief, it
 * merely loses the cross-request guarantee.
 */
private void lockAdvanceSettlement(String demandId) {
    try {
        jdbcTemplate.query("SELECT pg_advisory_xact_lock(hashtext(?))",
                new Object[] { "adv-settle:" + demandId }, rs -> null);
    } catch (DataAccessException e) {
        log.warn("Could not take the advance-settlement lock for demand {}; continuing without it",
                demandId, e);
    }
}

/** One relief leg, taking its dimensions and dates from the demand's own forward document. */
private FiReport settlementReliefRow(String demandId, Map<String, Object> fwd, String glCode,
                                     String postingKey, String remarks, BigDecimal amount,
                                     Long docDate, long now) {
    return FiReport.builder()
            .transactionNumber(demandId)
            .docDate(docDate)
            .postingDate(docDate)
            .referenceNo((String) fwd.get("reference_no"))
            .documentHeaderText((String) fwd.get("document_header_text"))
            .postingKey(postingKey)
            .glCode(glCode)
            .collectionAmount(amount)
            .fund((String) fwd.get("fund"))
            .fundCentre((String) fwd.get("fund_centre"))
            .functionalArea((String) fwd.get("functional_area"))
            .businessArea((String) fwd.get("business_area"))
            .assignment((String) fwd.get("assignment"))
            .remarks(remarks)
            .reportType(FiReportType.UPMKT_DEMDADV)
            .docType("YX")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build();
}

/** One netting-reversal leg, taking its dimensions from the aggregated original rows. */
private FiReport nettingReversalRow(String demandId, java.sql.ResultSet rs, String glCode,
                                    String postingKey, String remarks, BigDecimal amount,
                                    Long docDate, long now) throws java.sql.SQLException {
    return FiReport.builder()
            .transactionNumber(demandId)
            .docDate(docDate)
            .postingDate(docDate)
            .referenceNo(rs.getString("reference_no"))
            .documentHeaderText(rs.getString("document_header_text"))
            .postingKey(postingKey)
            .glCode(glCode)
            .collectionAmount(amount)
            .fund(rs.getString("fund"))
            .fundCentre(rs.getString("fund_centre"))
            .functionalArea(rs.getString("functional_area"))
            .businessArea(rs.getString("business_area"))
            .assignment(rs.getString("assignment"))
            .remarks(remarks)
            .reportType(FiReportType.UPMKT_DEMDREV)
            .docType("YX")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build();
}

/**
 * Posting-key helper. On reversal the debit/credit posting keys are swapped
 * (40 to 50 and vice versa) so the reversal mirrors the original collection.
 */
private String pk(String key, boolean reversal) {
    if (!reversal)
        return key;
    if ("40".equals(key))
        return "50";
    if ("50".equals(key))
        return "40";
    return key;
}


public List<FiReport> buildGstNettingFiReports(Demand demand,
                                               BigDecimal cgst,
                                               BigDecimal sgst,
                                               GstAdvanceMap gstMap) {

    List<FiReport> list = new ArrayList<>();
    long now = System.currentTimeMillis();
    Long postingDate = demand.getTaxPeriodFrom();

    // CGST Netting
    list.add(gstFi(demand, "40", "CGST Payable", cgst, gstMap.getCgstGlCode(), now));
    list.add(gstFi(demand, "50", "CGST Advance", cgst, gstMap.getCgstGlCode(), now));

    // SGST Netting
    list.add(gstFi(demand, "40", "SGST Payable", sgst, gstMap.getSgstGlCode(), now));
    list.add(gstFi(demand, "50", "SGST Advance", sgst, gstMap.getSgstGlCode(), now));

    return list;
}




private FiReport gstFi(Demand demand,
                       String postingKey,
                       String header,
                       BigDecimal amount,
                       String glCode,
                       long now) {

    return FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(demand.getTaxPeriodFrom())
            .postingDate(demand.getTaxPeriodFrom())
            .referenceNo(demand.getConsumerCode())
            .documentHeaderText(header)
            .postingKey(postingKey)
            .glCode(glCode)
            .collectionAmount(amount)
            .fund(demand.getFund())
            .fundCentre(demand.getFundCenter())
            .businessArea(demand.getBusinessArea())
            .functionalArea(demand.getFunctionalArea())
            .remarks("Adjustment")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build();
}






 public Map<String, Long> fetchDemandSeqNoByDemandIds(Set<String> demandIds) {

        String FETCH_DEMAND_SEQNO_BY_IDS = 
        "SELECT id, demandseqno " +
        "FROM egbs_demand_v1 " +
        "WHERE id IN (:ids)";

        if (demandIds == null || demandIds.isEmpty()) {
            return Collections.emptyMap();
        }

        NamedParameterJdbcTemplate namedJdbcTemplate =
                new NamedParameterJdbcTemplate(jdbcTemplate);

        Map<String, Object> params = new HashMap<>();
        params.put("ids", demandIds);

        return namedJdbcTemplate.query(
                FETCH_DEMAND_SEQNO_BY_IDS,
                params,
                rs -> {
                    Map<String, Long> result = new HashMap<>();
                    while (rs.next()) {
                        result.put(
                                rs.getString("id"),
                                rs.getLong("demandseqno")
                        );
                    }
                    return result;
                }
        );
    }


}
	
