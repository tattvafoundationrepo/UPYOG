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
                String advanceDocNo = getAdvanceReceipt(demand.getId()).documentNo;

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
			    List<FiReport> demandFiReports = buildDemandFiReports(demand, nettingDetails);
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
			    demandFiReports.forEach(r -> r.setReportType(fiReportType));
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

	public List<PaymentMarketInfo> getMarketEssentialInfo(String demandId) {
		String sql =
        "SELECT ep2.paymentmode, " +
         "       eem.fund_center, " +
         "       eem.fund, " +
         "       eem.business_area, " +
		 "       eem.functional_area, " +
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

        List<PaymentMarketInfo> result = jdbcTemplate.query(
        sql,
        new Object[]{ demandId },
        new RowMapper<PaymentMarketInfo>() {
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
        }
       ); 
	   return result;

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

    String sql =
        "SELECT p.transactionnumber, p.transactiondate " +
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
 * Doc date for a demand's FI rows.
 *
 * <p>Normally the tax period start. For a demand raised against an advance the invoice date
 * is clamped forward to the advance receipt date when the period has already elapsed: GST
 * time of supply is the earlier of invoice or payment, so where payment came first the
 * invoice cannot be stamped into a period that closed before the money arrived. The client's
 * own Tax Paid sheet honours AT DT &lt;= DEMAND DATE on 14 of its 15 rows.
 *
 * <p>Posting date is separately forced to the open period by the CSV exporter, so only the
 * tax date moves here.
 */
private Long resolveDemandDocDate(Demand demand, AdvanceReceiptRef advance) {

    Long taxPeriodFrom = demand.getTaxPeriodFrom();
    if (!demand.isApportionedAgainstAdvance() || taxPeriodFrom == null)
        return taxPeriodFrom;

    Long advanceReceiptDate = advance == null ? null : advance.receiptDate;
    if (advanceReceiptDate == null || advanceReceiptDate <= taxPeriodFrom)
        return taxPeriodFrom;

    log.info("Demand {} tax period {} predates its advance receipt {}; clamping FI doc date",
            demand.getId(), taxPeriodFrom, advanceReceiptDate);
    return advanceReceiptDate;
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

    List<FiReport> reports = new ArrayList<>();

    String consumerCode = demand.getConsumerCode();
    // Doc date drives the GST tax point. For a demand raised against an advance the invoice
    // cannot predate the money: an 11B adjustment stamped with an elapsed tax period lands in
    // an already-filed return and drives the GST advance negative until the receipt catches up.
    // Clamped only on that path; plain, reversal and dishonour demands keep taxPeriodFrom.
    // One lookup per demand, reused for the doc-date clamp and the advance leg's clearing key.
    AdvanceReceiptRef advance = demand.isApportionedAgainstAdvance()
            ? getAdvanceReceipt(demand.getId())
            : AdvanceReceiptRef.NONE;
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
	           if (settled.compareTo(BigDecimal.ZERO) > 0)
	               settledFromAdvance = settledFromAdvance.add(settled);
	       }
	    }
	}

    // Balancing debit(s). The advance-settled portion releases 350410215; only the
    // unsettled remainder is booked as a receivable. A demand with no advance behind it
    // (settledFromAdvance == 0) produces exactly the single receivable row it always has.
    settledFromAdvance = settledFromAdvance.min(totalReceivable);
    BigDecimal openReceivable = totalReceivable.subtract(settledFromAdvance);

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
    return reports;
}





public List<FiReport> buildCollectionFiReports(Demand demand,
                                               FiFlow flow,
                                               BigDecimal total,
                                               BigDecimal cgst,
                                               BigDecimal sgst,
                                               boolean reversal) {

    List<FiReport> reports = new ArrayList<>();

    total = total == null ? BigDecimal.ZERO : total;
    cgst = cgst == null ? BigDecimal.ZERO : cgst;
    sgst = sgst == null ? BigDecimal.ZERO : sgst;

    // Net rent collected at the bank (gross paid minus the GST components).
    // Defined as the residual so the GST_REGULAR voucher always balances.
    BigDecimal net = total.subtract(cgst).subtract(sgst);

    switch (flow) {

        case NON_GST_REGULAR:
            reports.add(fiRow(demand, "431409936", pk("50", reversal), total, "Receivable from Mun Mkt"));
            reports.add(fiRow(demand, "450100100", pk("40", reversal), total, "Bank/Interim Receipt"));
            break;

        case GST_REGULAR:
            reports.add(fiRow(demand, "450100100", pk("40", reversal), net, "Bank/Interim Receipt"));
            reports.add(fiRow(demand, "431409936", pk("50", reversal), total, "Receivable from Mun Mkt"));
            reports.add(fiRow(demand, "350200421", pk("40", reversal), cgst, "CGST Payable"));
            reports.add(fiRow(demand, "350200422", pk("40", reversal), sgst, "SGST Payable"));
            break;

        case NON_GST_ADVANCE:
            reports.add(fiRow(demand, "350410215", pk("50", reversal), total, "Advance"));
            reports.add(fiRow(demand, "450100100", pk("40", reversal), total, "Bank/Interim Receipt"));
            break;

        case GST_ADVANCE:
            reports.add(fiRow(demand, "450100100", pk("40", reversal), total, "Bank/Interim Receipt"));
            reports.add(fiRow(demand, "350410215", pk("50", reversal), total, "Advance"));
            reports.add(fiRow(demand, "350200421", pk("50", reversal), cgst, "CGST Payable"));
            reports.add(fiRow(demand, "350200422", pk("50", reversal), sgst, "SGST Payable"));
            reports.add(fiRow(demand, "439300200", pk("40", reversal), cgst, "Advance CGST"));
            reports.add(fiRow(demand, "439300201", pk("40", reversal), sgst, "Advance SGST"));
            break;

        case DEPOSIT:
            reports.add(fiRow(demand, "340100300", pk("50", reversal), total, "Security Deposit"));
            reports.add(fiRow(demand, "450100100", pk("40", reversal), total, "Bank/Interim Receipt"));
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
private FiReport fiRow(Demand demand, String glCode, String postingKey,
                       BigDecimal amount, String remarks) {
    long now = System.currentTimeMillis();
    return FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(demand.getTaxPeriodFrom())
            .postingDate(demand.getTaxPeriodFrom())
            .referenceNo(demand.getConsumerCode())
            .documentHeaderText(demand.getFiReceiptNo())
            .postingKey(postingKey)
            .glCode(glCode)
            .collectionAmount(amount)
            .fund(demand.getFund())
            .fundCentre(demand.getFundCenter())
            .businessArea(demand.getBusinessArea())
            .functionalArea(demand.getFunctionalArea())
            .remarks(remarks)
            .paymentModeDetails(demand.getPaymentMode())
            // SAP Assignment (ZUONR). Only the GST-advance legs carry it, set to this
            // receipt's transaction number — the same value the later demand's netting leg
            // carries — so F.13/FB05 can match the two and clear the advance GST. Every
            // other GL keeps the blank Assignment it has today.
            .assignment(isGstAdvanceGl(glCode) ? demand.getFiReceiptNo() : null)
            .docType("YY")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build();
}

/** True for the CGST/SGST Advance asset GLs that must clear against a demand's netting leg. */
private boolean isGstAdvanceGl(String glCode) {
    return GL_CGST_ADVANCE.equals(glCode) || GL_SGST_ADVANCE.equals(glCode);
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
	
