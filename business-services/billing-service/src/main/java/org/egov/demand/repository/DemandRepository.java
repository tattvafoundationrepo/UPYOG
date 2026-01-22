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
import org.egov.demand.model.FiReport;
import org.egov.demand.model.FiReportRequest;
import org.egov.demand.model.GstAdvanceMap;
import org.egov.demand.model.PaymentBackUpdateAudit;
import org.egov.demand.model.PaymentMarketInfo;
import org.egov.demand.producer.Producer;
import org.egov.demand.repository.querybuilder.DemandQueryBuilder;
import org.egov.demand.repository.rowmapper.CollectedReceiptsRowMapper;
import org.egov.demand.repository.rowmapper.DemandRowMapper;
import org.egov.demand.util.Util;
import org.egov.demand.web.contract.DemandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.web.mappings.servlet.FilterRegistrationMappingDescription;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
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
	private Util util;
    
	@Autowired
	private Producer producer;
	
	public List<Demand> getDemands(DemandCriteria demandCriteria) {

		List<Object> preparedStatementValues = new ArrayList<>();
		String searchDemandQuery = demandQueryBuilder.getDemandQuery(demandCriteria, preparedStatementValues);
		return jdbcTemplate.query(searchDemandQuery, preparedStatementValues.toArray(), demandRowMapper);
	}

	public List<CollectedReceipt> getCollectedReceipts(DemandCriteria demandCriteria) {
    
		List<Object> preparedStatementValues = new ArrayList<>();
		String query = demandQueryBuilder.getCollectedReceiptsQuery(demandCriteria, preparedStatementValues);
		
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


        for (Demand demand : demands) {
		  
            if(demand.getBusinessService().equalsIgnoreCase("TX.Emarket_Rental_Fees")){
               
              List<DemandDetail> gstDemandDetail = demand.getDemandDetails().stream()
                .filter(d -> 
                    (d.getTaxHeadMasterCode().contains("GST") && !d.getTaxHeadMasterCode().equalsIgnoreCase("GST_CA")
                      && d.getTaxAmount().compareTo(d.getCollectionAmount()) == 0)
                ).collect(Collectors.toList());

                if(gstDemandDetail != null && !gstDemandDetail.isEmpty()) {

                Map<String, Object> cgstMap = new HashMap<>();
                   cgstMap.put("glcode",  "350200421");

                Map<String, Object> sgstMap = new HashMap<>();
                   sgstMap.put("glcode",  "350200422");

                Map<String, Object> advCgstMap = new HashMap<>();
                   advCgstMap.put("glcode", "439300200");

                Map<String, Object> advSgstMap = new HashMap<>();
                   advSgstMap.put("glcode",  "439300201");
                    
                    demand.getDemandDetails().add(DemandDetail.builder()
		                .demandId(demand.getId())
                        .taxAmount(gstDemandDetail.get(0).getTaxAmount())
                        .taxHeadMasterCode("CSP40")
                        .additionalDetails(cgstMap)
                        .build());

                    demand.getDemandDetails().add(DemandDetail.builder()
		                .demandId(demand.getId())
                        .taxAmount(gstDemandDetail.get(0).getTaxAmount())
                        .taxHeadMasterCode("SSP40")
                        .additionalDetails(sgstMap)
                        .build());   
                        
                    demand.getDemandDetails().add(DemandDetail.builder()
		                .demandId(demand.getId())
                        .taxAmount(gstDemandDetail.get(0).getTaxAmount())
                        .taxHeadMasterCode("CSA50")
                        .additionalDetails(advCgstMap)
                        .build());

                    demand.getDemandDetails().add(DemandDetail.builder()
		                .demandId(demand.getId())
                        .taxAmount(gstDemandDetail.get(0).getTaxAmount())
                        .taxHeadMasterCode("SSA50")
                        .additionalDetails(advSgstMap)
                        .build());        
                }


            }
               

		// 	demand.getDemandDetails().add(DemandDetail.builder()
		//         .demandId(demand.getId())
        //         .taxAmount(totalTaxAmount)
        //         .taxHeadMasterCode("Customer "+demand.getConsumerCode())
        //         .additionalDetails(demand.getAdditionalDetails())
        //         .build());

			//reportList.addAll(buildFiReportsFromDemand(demand , "50", false , null));
			reportList.addAll(buildDemandFiReports(demand));
		}

		if(!reportList.isEmpty()){
            batchInsertFiReports(reportList);
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
                        .createdAt(now)
                        .updatedAt(now)
                        .remarks(isCollection ? remark != null ? remark :" Collection " : " Demand")
                        .build();
            })
            .collect(Collectors.toList());
}


	public void batchInsertFiReports(List<FiReport> reports) {

    if (reports == null || reports.isEmpty()) return;

        String sql =
        "INSERT INTO public.eg_emarket_fi_report ("
        + " transaction_number, doc_date, posting_date,"
        + " reference_no, document_header_text,"
        + " posting_key, gl_code, collection_amount,"
        + " fund, fund_centre,"
        + " functional_area, business_area,"
        + " remarks, payment_mode_details, is_new,"
        + " created_at, updated_at"
        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


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
    });

    log.info("Batch inserted {} FI Report records", reports.size());
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
		 "       ep2.additionaldetails , ep2.totaldue , ep2.totalamountpaid " +
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




public List<FiReport> buildDemandFiReports(Demand demand) {

    List<FiReport> reports = new ArrayList<>();
    long now = System.currentTimeMillis();

    String consumerCode = demand.getConsumerCode();
    Long postingDate = demand.getTaxPeriodFrom();

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

    for(DemandDetail dd : demand.getDemandDetails()){
        if(dd.getTaxAmount() == null || dd.getTaxAmount().compareTo(BigDecimal.ZERO) == 0)
           continue;
        String th = dd.getTaxHeadMasterCode();

          reports.add(FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(consumerCode)
            .documentHeaderText(th.contains("GST") && !th.equalsIgnoreCase("GST_CA") ? 
			        (th.contains("CGST") ? "CGST Payable" : "SGST Payable") 
					 :  th.contains("40") ? th.equalsIgnoreCase("CSP40") ? "CGST Payable" : "SGST Payable"
                     : th.contains("50") ? th.equalsIgnoreCase("CSA50") ? "CGST Advance" : "SGST Advance" 
                     : th)
            .postingKey(th.contains("40") ? "40" : 
                        th.contains("50") ? "50" : "50")
            .glCode(extractGlCode(dd))
            .collectionAmount(dd.getTaxAmount())
            .fund(fund)
            .fundCentre(fundCenter)
            .businessArea(businessArea)
            .functionalArea(functionalArea)
            .remarks("YX")  
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build());
        if(!th.contains("40") && !th.contains("50"))    
	       totalReceivable = totalReceivable.add(dd.getTaxAmount());		
	}


    // 1️⃣ Customer / Receivable (Dr)
     if(totalReceivable.compareTo(BigDecimal.ZERO) != 0){     
    reports.add(FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(consumerCode)
            .documentHeaderText("Customer " + consumerCode)
            .postingKey("40")
            .glCode("431190300")
            .collectionAmount(totalReceivable)
            .fund(fund)
            .fundCentre(fundCenter)
            .businessArea(businessArea)
            .functionalArea(functionalArea)
            .remarks("YX")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build());
     }
    return reports;
}



public List<FiReport> buildCollectionFiReports(Demand demand,
                                               GstAdvanceMap advanceMap) {

    List<FiReport> reports = new ArrayList<>();
    long now = System.currentTimeMillis();

    BigDecimal total = advanceMap.getTotalAmountPaid();
    Long postingDate = demand.getTaxPeriodFrom();
    
	List<String> advanceTaxHeadLists = new ArrayList<>();	
    
	Boolean hasAdvanceTaxhead = false;
    Boolean hasRentAdvance = false;
    Boolean hasLicenseAdvance = false;
	for(DemandDetail d: demand.getDemandDetails()){
        Map<String, Object> advanceGlMap = new HashMap<>();
        advanceGlMap.put("glcode",  "450100100");

       if(d.getTaxHeadMasterCode().contains("ADVANCE")){
          if(d.getTaxHeadMasterCode().equalsIgnoreCase("TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD")){
              hasRentAdvance = true;
              d.setTaxAmount(advanceMap.getRentalAdvancePaid());
              d.setPostingKey("40");
              d.setAdditionalDetails(advanceGlMap);
          }
            
          if(d.getTaxHeadMasterCode().equalsIgnoreCase("TX.EMARKET_LICENSE_ADVANCE_CARRYFORWARD")){
              hasLicenseAdvance = true;
              d.setTaxAmount(advanceMap.getLicenseAdvancePaid());
              d.setPostingKey("40");
              d.setAdditionalDetails(advanceGlMap);
          }
            
          hasAdvanceTaxhead = true;
	   }
		 
	}


    // If advanceMap provided, append demandDetails for CGST/SGST and ADV_CGST/ADV_SGST
    if (advanceMap != null  && hasAdvanceTaxhead ) {
            
        Map<String, Object> advanceGlMap = new HashMap<>();
        advanceGlMap.put("glcode",  "NA");

        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(hasRentAdvance && hasLicenseAdvance ? 
                            advanceMap.getRentalAdvancePaid().add(advanceMap.getLicenseAdvancePaid()) :
                    hasRentAdvance ? advanceMap.getRentalAdvancePaid() : advanceMap.getLicenseAdvancePaid())
                .taxHeadMasterCode("Rent From Municipal market")
                .additionalDetails(advanceGlMap)
                .postingKey("50")
                .build());    
        
        if(advanceMap.getCgstAmount() != null && advanceMap.getSgstAmount() != null){

        Map<String, Object> cgstMap = new HashMap<>();
        cgstMap.put("glcode",  "350200421");

        Map<String, Object> sgstMap = new HashMap<>();
        sgstMap.put("glcode", "350200422");

        Map<String, Object> advCgstMap = new HashMap<>();
        advCgstMap.put("glcode",  "439300200");

        Map<String, Object> advSgstMap = new HashMap<>();
        advSgstMap.put("glcode", "439300201");
    

         // Add ADV_CGST (payment side)
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getCgstAmount())
                .taxHeadMasterCode("ADV_CGST")
                .additionalDetails(advCgstMap)
                .postingKey("40")
                .build());

        // Add ADV_SGST (payment side)
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getSgstAmount())
                .taxHeadMasterCode("ADV_SGST")
                .additionalDetails(advSgstMap)
                .postingKey("40")
                .build());        

     


        // Add CGST Payable
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getCgstAmount())
                .taxHeadMasterCode("CGST Payable")
                .additionalDetails(cgstMap)
                .postingKey("50")
                .build());

        // Add SGST Payable
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getSgstAmount())
                .taxHeadMasterCode("SGST Payable")
                .additionalDetails(sgstMap)
                .postingKey("50")
                .build());

       
            advanceTaxHeadLists.add("ADV_SGST");
            advanceTaxHeadLists.add("ADV_CGST");
            advanceTaxHeadLists.add("CGST Payable");
            advanceTaxHeadLists.add("SGST Payable");				
    }
}




   for(DemandDetail d : demand.getDemandDetails()){
    // Bank / Interim Receipt (Dr)
    if(d.getTaxAmount() == null || d.getTaxAmount().compareTo(BigDecimal.ZERO) == 0)
           continue;
    reports.add(FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(demand.getConsumerCode())
            .documentHeaderText(d.getTaxHeadMasterCode())
            .postingKey(d.getPostingKey() != null ? d.getPostingKey() : "40")
            .glCode(extractGlCode(d))
            .collectionAmount(d.getTaxAmount())
            .fund(demand.getFund())
            .fundCentre(demand.getFundCenter())
            .businessArea(demand.getBusinessArea())
            .functionalArea(demand.getFunctionalArea())
            .remarks("YY")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build());

   }			
   if(total.compareTo(BigDecimal.ZERO) != 0){
       
    // Customer / Receivable (Cr)
    reports.add(FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(demand.getConsumerCode())
            .documentHeaderText("Customer " + demand.getConsumerCode())
            .postingKey("50")
			.glCode("NA")
            .collectionAmount(total)
            .fund(demand.getFund())
            .fundCentre(demand.getFundCenter())
            .businessArea(demand.getBusinessArea())
            .functionalArea(demand.getFunctionalArea())
            .remarks("YY")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build());

     }
    return reports;
}






public List<FiReport> buildCollectionReversalFiReports(Demand demand,
                                               GstAdvanceMap advanceMap) {

    List<FiReport> reports = new ArrayList<>();
    long now = System.currentTimeMillis();

    BigDecimal total = advanceMap.getTotalAmountPaid();
    Long postingDate = demand.getTaxPeriodFrom();
    
	List<String> advanceTaxHeadLists = new ArrayList<>();	
    
	Boolean hasAdvanceTaxhead = false;
    Boolean hasRentAdvance = false;
    Boolean hasLicenseAdvance = false;
	for(DemandDetail d: demand.getDemandDetails()){
       if(d.getTaxHeadMasterCode().contains("ADVANCE")){
          if(d.getTaxHeadMasterCode().equalsIgnoreCase("TX.EMARKET_RENTAL_ADVANCE_CARRYFORWARD")){
              hasRentAdvance = true;
              d.setTaxAmount(advanceMap.getRentalAdvancePaid());
              d.setPostingKey("50");
          }
            
          if(d.getTaxHeadMasterCode().equalsIgnoreCase("TX.EMARKET_LICENSE_ADVANCE_CARRYFORWARD")){
              hasLicenseAdvance = true;
              d.setTaxAmount(advanceMap.getLicenseAdvancePaid());
              d.setPostingKey("50");
          }
            
          hasAdvanceTaxhead = true;
	   }
		 
	}


    // If advanceMap provided, append demandDetails for CGST/SGST and ADV_CGST/ADV_SGST
    if (advanceMap != null  && hasAdvanceTaxhead ) {
            
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(hasRentAdvance && hasLicenseAdvance ? 
                            advanceMap.getRentalAdvancePaid().add(advanceMap.getLicenseAdvancePaid()) :
                    hasRentAdvance ? advanceMap.getRentalAdvancePaid() : advanceMap.getLicenseAdvancePaid())
                .taxHeadMasterCode("Rent From Municipal market")
                .additionalDetails(demand.getDemandDetails().get(demand.getDemandDetails().size() - 1).getAdditionalDetails())
                .postingKey("40")
                .build());    
        
        if(advanceMap.getCgstAmount() != null && advanceMap.getSgstAmount() != null){

        Map<String, Object> cgstMap = new HashMap<>();
        cgstMap.put("glcode", "350200421");

        Map<String, Object> sgstMap = new HashMap<>();
        sgstMap.put("glcode", "350200422");

        Map<String, Object> advCgstMap = new HashMap<>();
        advCgstMap.put("glcode", "439300200");

        Map<String, Object> advSgstMap = new HashMap<>();
        advSgstMap.put("glcode", "439300201");
    

         // Add ADV_CGST (payment side)
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getCgstAmount())
                .taxHeadMasterCode("ADV_CGST")
                .additionalDetails(advCgstMap)
                .postingKey("50")
                .build());

        // Add ADV_SGST (payment side)
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getSgstAmount())
                .taxHeadMasterCode("ADV_SGST")
                .additionalDetails(advSgstMap)
                .postingKey("50")
                .build());        

     


        // Add CGST Payable
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getCgstAmount())
                .taxHeadMasterCode("CGST Payable")
                .additionalDetails(cgstMap)
                .postingKey("40")
                .build());

        // Add SGST Payable
        demand.getDemandDetails().add(DemandDetail.builder()
		        .demandId(demand.getId())
                .taxAmount(advanceMap.getSgstAmount())
                .taxHeadMasterCode("SGST Payable")
                .additionalDetails(sgstMap)
                .postingKey("40")
                .build());

       
            advanceTaxHeadLists.add("ADV_SGST");
            advanceTaxHeadLists.add("ADV_CGST");
            advanceTaxHeadLists.add("CGST Payable");
            advanceTaxHeadLists.add("SGST Payable");				
    }
}




   for(DemandDetail d : demand.getDemandDetails()){
    // Bank / Interim Receipt (Dr)
     if(d.getTaxAmount() == null || d.getTaxAmount().compareTo(BigDecimal.ZERO) == 0)
           continue;
    reports.add(FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(demand.getConsumerCode())
            .documentHeaderText(d.getTaxHeadMasterCode())
            .postingKey(d.getPostingKey() != null ? d.getPostingKey() : "50")
            .glCode(extractGlCode(d))
            .collectionAmount(d.getTaxAmount())
            .fund(demand.getFund())
            .fundCentre(demand.getFundCenter())
            .businessArea(demand.getBusinessArea())
            .functionalArea(demand.getFunctionalArea())
            .remarks("YY")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build());

   }			

    // Customer / Receivable (Cr)

    if(total.compareTo(BigDecimal.ZERO) != 0){
    reports.add(FiReport.builder()
            .transactionNumber(demand.getId())
            .docDate(postingDate)
            .postingDate(postingDate)
            .referenceNo(demand.getConsumerCode())
            .documentHeaderText("Customer " + demand.getConsumerCode())
            .postingKey("40")
			.glCode("NA")
            .collectionAmount(total)
            .fund(demand.getFund())
            .fundCentre(demand.getFundCenter())
            .businessArea(demand.getBusinessArea())
            .functionalArea(demand.getFunctionalArea())
            .remarks("YY")
            .isNew(Boolean.TRUE)
            .createdAt(now)
            .updatedAt(now)
            .build());
    }
    return reports;
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


}
	