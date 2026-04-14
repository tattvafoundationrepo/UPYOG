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
package org.egov.demand.repository.querybuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.egov.demand.model.DemandCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DemandQueryBuilder {
	

	public static final String PAYMENT_BACKUPDATE_AUDIT_INSERT_QUERY = "INSERT INTO egbs_payment_backupdate_audit (paymentid, isbackupdatesuccess, isreceiptcancellation, errorMessage)"
			+ " VALUES (?,?,?,?);";
	
	public static final String PAYMENT_BACKUPDATE_AUDIT_SEARCH_QUERY = "SELECT paymentid FROM egbs_payment_backupdate_audit where paymentid=? AND isbackupdatesuccess=? AND isreceiptcancellation=?;";

	public static final String BASE_DEMAND_QUERY = "SELECT dmd.id AS did,dmd.consumercode AS dconsumercode,"
			+ "dmd.consumertype AS dconsumertype,dmd.businessservice AS dbusinessservice,dmd.payer,"
			+ "dmd.billexpirytime AS dbillexpirytime, dmd.fixedBillExpiryDate as dfixedBillExpiryDate, "
			+ "dmd.taxperiodfrom AS dtaxperiodfrom,dmd.taxperiodto AS dtaxperiodto,"
			+ "dmd.minimumamountpayable AS dminimumamountpayable,dmd.createdby AS dcreatedby,"
			+ "dmd.lastmodifiedby AS dlastmodifiedby,dmd.createdtime AS dcreatedtime,"
			+ "dmd.lastmodifiedtime AS dlastmodifiedtime,dmd.tenantid AS dtenantid,dmd.status,"
			+ "dmd.additionaldetails as demandadditionaldetails,dmd.ispaymentcompleted as ispaymentcompleted,dmd.demandseqno,"
			+ "dmd.isadvance as isadvance,dmd.advanceindex as advanceindex,"
            + "concat( eelm.primary_title,' ',eelm.first_name,' ',eelm.last_name ) as licencee_name, eelm.mobile as licencee_mobile,eea.reason, "
			+ "dmdl.id AS dlid,dmdl.demandid AS dldemandid,dmdl.taxheadcode AS dltaxheadcode,"
			+ "dmdl.taxamount AS dltaxamount,dmdl.collectionamount AS dlcollectionamount,"
			+ "dmdl.createdby AS dlcreatedby,dmdl.lastModifiedby AS dllastModifiedby,"
			+ "dmdl.createdtime AS dlcreatedtime,dmdl.lastModifiedtime AS dllastModifiedtime,"
			+ "dmdl.tenantid AS dltenantid,dmdl.additionaldetails as detailadditionaldetails " + "FROM egbs_demand_v1 dmd "
			+ "INNER JOIN egbs_demanddetail_v1 dmdl ON dmd.id=dmdl.demandid " + "AND dmd.tenantid=dmdl.tenantid "
            + "LEFT JOIN eg_emarket_allotment eea  ON regexp_replace(dmd.consumercode, '[^0-9]', '', 'g')\n" + "=eea.license_number " 
			+ "LEFT JOIN eg_emarket_licensee_master eelm   ON eea.license_id = eelm.licensee_id " + " WHERE ";

	public static final String BASE_DEMAND_DETAIL_QUERY = "SELECT "
			+ "demanddetail.id AS dlid,demanddetail.demandid AS dldemandid,demanddetail.taxheadcode AS dltaxheadcode,"
			+ "demanddetail.taxamount AS dltaxamount,demanddetail.collectionamount AS dlcollectionamount,"
			+ "demanddetail.createdby AS dlcreatedby,demanddetail.lastModifiedby AS dllastModifiedby,"
			+ "demanddetail.createdtime AS dlcreatedtime,demanddetail.lastModifiedtime AS dllastModifiedtime,"
			+ "demanddetail.tenantid AS dltenantid " + " FROM egbs_demanddetail_v1 demanddetail "
					+ "INNER JOIN egbs_demand demand ON demanddetail.demandid=demand.id AND "
					+ "demanddetail.tenantid=demand.tenantid WHERE ";

	public static final String DEMAND_QUERY_ORDER_BY_CLAUSE = "dmd.taxperiodfrom";

	public static final String BASE_DEMAND_DETAIL_QUERY_ORDER_BY_CLAUSE = "dmdl.id";

	public static final String DEMAND_INSERT_QUERY = "INSERT INTO egbs_demand_v1 "
			+ "(id,consumerCode,consumerType,businessService,payer,taxPeriodFrom,taxPeriodTo,"
			+ "minimumAmountPayable,createdby,lastModifiedby,createdtime,lastModifiedtime,tenantid, status, additionaldetails, billexpirytime, fixedBillExpiryDate, isadvance, advanceindex) "
			+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

	public static final String DEMAND_DETAIL_INSERT_QUERY = "INSERT INTO egbs_demanddetail_v1 "
			+ "(id,demandid,taxHeadCode,taxamount,collectionamount,"
			+ "createdby,lastModifiedby,createdtime,lastModifiedtime,tenantid,additionaldetails)" 
			+ " VALUES (?,?,?,?,?,?,?,?,?,?,?);";

	public static final String DEMAND_UPDATE_QUERY = "UPDATE egbs_demand_v1 SET " + "payer=?,taxPeriodFrom=?,"
			+ "taxPeriodTo=?,minimumAmountPayable=?,lastModifiedby=?," + "lastModifiedtime=?,tenantid=?,"
			+ " status=?,additionaldetails=?,billexpirytime=?,ispaymentcompleted=?, fixedBillExpiryDate=?, isadvance=?, advanceindex=? WHERE id=? AND tenantid=?;";
	
	public static final String DEMAND_DETAIL_UPDATE_QUERY = "UPDATE egbs_demanddetail_v1 SET taxamount=?,collectionamount=?,"
			+ "lastModifiedby=?,lastModifiedtime=?, additionaldetails=? WHERE id=? AND demandid=? AND tenantid=?;";

	public static final String DEMAND_AUDIT_INSERT_QUERY = "INSERT INTO egbs_demand_v1_audit "
			+ "(demandid,consumerCode,consumerType,businessService,payer,taxPeriodFrom,taxPeriodTo,"
			+ "minimumAmountPayable,createdby,createdtime,tenantid, status, additionaldetails,id,billexpirytime, ispaymentcompleted, isadvance, advanceindex) "
			+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

	public static final String DEMAND_DETAIL_AUDIT_INSERT_QUERY = "INSERT INTO egbs_demanddetail_v1_audit "
			+ "(demanddetailid,demandid,taxHeadCode,taxamount,collectionamount,"
			+ "createdby,createdtime,tenantid,additionaldetails,id)" 
			+ " VALUES (?,?,?,?,?,?,?,?,?,?);";
	
	public static final String DEMAND_UPDATE_CONSUMERCODE_QUERY="UPDATE egbs_demand_v1 SET consumercode=?, lastmodifiedby=?, lastmodifiedtime=? "
			+ " WHERE tenantid=? AND id IN (";
	

	public static final String COLLECTED_RECEIPT_QUERY = "WITH bill_periods AS ( " +
			"SELECT " +
			"bdt.billid, " +
			"bdt.tenantid, " +
			"MIN(bdt.fromperiod) as fromperiod, " +
			"MAX(bdt.toperiod) as toperiod " +
			"FROM egcl_billdetial bdt " +
			"GROUP BY bdt.billid, bdt.tenantid " +
			"), " +
			"bill_amounts AS ( " +
			"SELECT " +
			"bdt.billid, " +
			"bdt.tenantid, " +
			"SUM(CASE WHEN bad.taxheadcode LIKE '%CARRY%' THEN bad.amount ELSE 0 END) as advancepayment, " +
			"SUM(CASE WHEN bad.taxheadcode NOT LIKE '%CARRY%' THEN bad.amount ELSE 0 END) as regularpayment " +
			"FROM egcl_billdetial bdt " +
			"INNER JOIN egcl_billaccountdetail bad ON bdt.id = bad.billdetailid AND bdt.tenantid = bad.tenantid " +
			"GROUP BY bdt.billid, bdt.tenantid " +
			") " +
			"SELECT " +
			"pd.businessservice, " +
			"bd.consumercode, " +
			"pd.receiptnumber, " +
			"pd.amountpaid as receiptamount, " +
			"pd.receiptdate, " +
			"p.paymentstatus as status, " +
			"p.tenantid, " +
			"p.createdby, " +
			"p.createdtime, " +
			"p.lastmodifiedby, " +
			"p.lastmodifiedtime, " +
			"p.transactionnumber, " +
			"p.additionaldetails, " +
			"p.totalamountpaid, " +
			"bp.fromperiod, " +
			"bp.toperiod, " +
			"COALESCE(ba.advancepayment, 0) as advancepayment, " +
			"COALESCE(ba.regularpayment, 0) as regularpayment " +
			"FROM egcl_payment p " +
			"INNER JOIN egcl_paymentdetail pd ON p.id = pd.paymentid AND p.tenantid = pd.tenantid " +
			"INNER JOIN egcl_bill bd ON pd.billid = bd.id AND pd.tenantid = bd.tenantid " +
			"LEFT JOIN bill_periods bp ON bd.id = bp.billid AND bd.tenantid = bp.tenantid " +
			"LEFT JOIN bill_amounts ba ON bd.id = ba.billid AND bd.tenantid = ba.tenantid " +
			"WHERE p.tenantid = ? ";

	public static final String MERGED_DEMAND_BASE_QUERY = "WITH filtered_demands AS ( "
			+ "    SELECT "
			+ "        LEFT(dmd.consumercode, 10) AS base_code, "
			+ "        dmd.id AS did, dmd.consumercode, dmd.consumertype, dmd.businessservice, "
			+ "        dmd.tenantid, dmd.payer, dmd.taxperiodfrom, dmd.taxperiodto, "
			+ "        dmd.minimumamountpayable, dmd.status, dmd.ispaymentcompleted, "
			+ "        dmd.isadvance, dmd.advanceindex, dmd.demandseqno, "
			+ "        dmd.billexpirytime, dmd.fixedBillExpiryDate, "
			+ "        dmd.additionaldetails AS demand_additionaldetails, "
			+ "        dmd.createdby, dmd.lastmodifiedby, dmd.createdtime, dmd.lastmodifiedtime, "
			+ "        concat(eelm.primary_title,' ',eelm.first_name,' ',eelm.last_name) AS licencee_name, "
			+ "        eelm.mobile AS licencee_mobile, eea.reason, "
			+ "        dmdl.id AS dlid, dmdl.taxheadcode, dmdl.taxamount, dmdl.collectionamount, "
			+ "        dmdl.additionaldetails AS detail_additionaldetails "
			+ "    FROM egbs_demand_v1 dmd "
			+ "    INNER JOIN egbs_demanddetail_v1 dmdl ON dmd.id = dmdl.demandid AND dmd.tenantid = dmdl.tenantid "
			+ "    LEFT JOIN eg_emarket_allotment eea ON regexp_replace(dmd.consumercode, '[^0-9]', '', 'g') = eea.license_number "
			+ "    LEFT JOIN eg_emarket_licensee_master eelm ON eea.license_id = eelm.licensee_id "
			+ "    WHERE %s "
			+ "), "
			+ "detail_agg AS ( "
			+ "    SELECT did, "
			+ "           json_agg(json_build_object( "
			+ "               'id', dlid, 'demandId', did, 'taxHeadMasterCode', taxheadcode, "
			+ "               'taxAmount', taxamount, 'collectionAmount', collectionamount, "
			+ "               'additionalDetails', detail_additionaldetails, 'tenantId', tenantid "
			+ "           ) ORDER BY dlid) AS demand_details, "
			+ "           SUM(CASE WHEN taxheadcode NOT LIKE '%%CARRYFORWARD%%' THEN taxamount ELSE 0 END) AS tax_total, "
			+ "           SUM(CASE WHEN taxheadcode NOT LIKE '%%CARRYFORWARD%%' THEN collectionamount ELSE 0 END) AS coll_total "
			+ "    FROM filtered_demands "
			+ "    GROUP BY did "
			+ "), "
			+ "demand_info AS ( "
			+ "    SELECT DISTINCT ON (did) "
			+ "        base_code, did, consumercode, consumertype, businessservice, tenantid, "
			+ "        payer, taxperiodfrom, taxperiodto, minimumamountpayable, "
			+ "        status, ispaymentcompleted, isadvance, advanceindex, demandseqno, "
			+ "        billexpirytime, fixedBillExpiryDate, demand_additionaldetails, "
			+ "        createdby, lastmodifiedby, createdtime, lastmodifiedtime, "
			+ "        licencee_name, licencee_mobile, reason "
			+ "    FROM filtered_demands "
			+ "    ORDER BY did "
			+ "), "
			+ "service_agg AS ( "
			+ "    SELECT di.base_code, di.businessservice, "
			+ "           json_agg(json_build_object( "
			+ "               'id', di.did, 'consumerCode', di.consumercode, 'businessService', di.businessservice, "
			+ "               'demandSeqNo', di.demandseqno, 'tenantId', di.tenantid, 'payer', di.payer, "
			+ "               'taxPeriodFrom', di.taxperiodfrom, 'taxPeriodTo', di.taxperiodto, "
			+ "               'status', di.status, 'isPaymentCompleted', di.ispaymentcompleted, "
			+ "               'isAdvance', di.isadvance, 'advanceIndex', di.advanceindex, "
			+ "               'minimumAmountPayable', di.minimumamountpayable, "
			+ "               'billExpiryTime', di.billexpirytime, 'fixedBillExpiryDate', di.fixedBillExpiryDate, "
			+ "               'additionalDetails', di.demand_additionaldetails, "
			+ "               'demandDetails', dt.demand_details "
			+ "           ) ORDER BY di.taxperiodfrom) AS service_demands, "
			+ "           SUM(CASE WHEN di.status='ACTIVE' THEN dt.tax_total ELSE 0 END) AS svc_tax_total, "
			+ "           SUM(CASE WHEN di.status='ACTIVE' THEN dt.coll_total ELSE 0 END) AS svc_coll_total, "
			+ "           SUM(CASE WHEN di.status='ACTIVE' THEN di.minimumamountpayable ELSE 0 END) AS svc_min_payable, "
			+ "           COUNT(CASE WHEN di.status='ACTIVE' THEN 1 END) AS svc_demand_count, "
			+ "           COUNT(CASE WHEN di.status='ACTIVE' AND di.ispaymentcompleted THEN 1 END) AS svc_paid_count, "
			+ "           MIN(di.taxperiodfrom) AS svc_min_period, "
			+ "           MAX(di.taxperiodto) AS svc_max_period "
			+ "    FROM demand_info di "
			+ "    JOIN detail_agg dt ON di.did = dt.did "
			+ "    GROUP BY di.base_code, di.businessservice "
			+ "), "
			+ "consumer_info AS ( "
			+ "    SELECT DISTINCT ON (base_code) "
			+ "        base_code, consumertype, tenantid, payer, licencee_name, licencee_mobile, reason "
			+ "    FROM demand_info "
			+ "    ORDER BY base_code, createdtime ASC "
			+ "), "
			+ "consumer_times AS ( "
			+ "    SELECT base_code, "
			+ "           MIN(createdtime) AS first_created_time, "
			+ "           MAX(lastmodifiedtime) AS last_modified_time, "
			+ "           COUNT(CASE WHEN status='ACTIVE' AND NOT ispaymentcompleted THEN 1 END) AS unpaid_active_count, "
			+ "           COUNT(CASE WHEN status='ACTIVE' AND ispaymentcompleted THEN 1 END) AS paid_active_count "
			+ "    FROM demand_info "
			+ "    GROUP BY base_code "
			+ "), "
			+ "all_base_codes AS ( "
			+ "    SELECT base_code FROM consumer_info ORDER BY base_code "
			+ "), "
			+ "total_count_cte AS ( "
			+ "    SELECT COUNT(*) AS total_count FROM all_base_codes "
			+ "), "
			+ "paginated_codes AS ( "
			+ "    SELECT base_code FROM all_base_codes %s "
			+ "), "
			+ "merged AS ( "
			+ "    SELECT pc.base_code AS consumer_code, "
			+ "           ci.consumertype AS consumer_type, ci.tenantid AS tenant_id, ci.payer, "
			+ "           ci.licencee_name, ci.licencee_mobile, ci.reason, "
			+ "           ct.first_created_time, ct.last_modified_time, "
			+ "           CASE WHEN ct.paid_active_count + ct.unpaid_active_count > 0 "
			+ "                THEN 'ACTIVE' ELSE 'INACTIVE' END AS overall_status, "
			+ "           (ct.unpaid_active_count = 0) AS is_fully_paid, "
			+ "           (ct.paid_active_count > 0 AND ct.unpaid_active_count > 0) AS has_partial_payment, "
			+ "           array_agg(DISTINCT sa.businessservice ORDER BY sa.businessservice) AS business_services, "
			+ "           json_object_agg(sa.businessservice, json_build_object( "
			+ "               'demands', sa.service_demands, "
			+ "               'totalTaxAmount', sa.svc_tax_total, "
			+ "               'totalCollectionAmount', sa.svc_coll_total, "
			+ "               'totalMinimumPayable', sa.svc_min_payable, "
			+ "               'remainingAmount', sa.svc_tax_total - sa.svc_coll_total "
			+ "           )) AS demands_by_service, "
			+ "           COALESCE(SUM(sa.svc_tax_total), 0) AS total_tax_amount, "
			+ "           COALESCE(SUM(sa.svc_coll_total), 0) AS total_collection_amount, "
			+ "           COALESCE(SUM(sa.svc_min_payable), 0) AS total_minimum_payable, "
			+ "           COALESCE(SUM(sa.svc_tax_total) - SUM(sa.svc_coll_total), 0) AS total_remaining_amount, "
			+ "           MIN(sa.svc_min_period) AS earliest_tax_period, "
			+ "           MAX(sa.svc_max_period) AS latest_tax_period, "
			+ "           COALESCE(SUM(sa.svc_demand_count), 0)::int AS total_demand_count, "
			+ "           COALESCE(SUM(sa.svc_paid_count), 0)::int AS paid_demand_count, "
			+ "           COALESCE(SUM(sa.svc_demand_count) - SUM(sa.svc_paid_count), 0)::int AS unpaid_demand_count "
			+ "    FROM paginated_codes pc "
			+ "    JOIN consumer_info ci ON pc.base_code = ci.base_code "
			+ "    JOIN consumer_times ct ON pc.base_code = ct.base_code "
			+ "    JOIN service_agg sa ON pc.base_code = sa.base_code "
			+ "    GROUP BY pc.base_code, ci.consumertype, ci.tenantid, ci.payer, "
			+ "             ci.licencee_name, ci.licencee_mobile, ci.reason, "
			+ "             ct.first_created_time, ct.last_modified_time, "
			+ "             ct.unpaid_active_count, ct.paid_active_count "
			+ ") "
			+ "SELECT m.*, tc.total_count FROM merged m CROSS JOIN total_count_cte tc ORDER BY m.consumer_code ";

	public String getMergedDemandQuery(DemandCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder where = new StringBuilder();

		String tenantId = criteria.getTenantId();
		String[] tenantIdChunks = tenantId.split("\\.");
		if (tenantIdChunks.length == 1) {
			where.append("dmd.tenantid LIKE ? ");
			preparedStmtList.add(tenantId + "%");
		} else {
			where.append("dmd.tenantid = ? ");
			preparedStmtList.add(tenantId);
		}

		if (criteria.getStatus() != null) {
			where.append(" AND dmd.status = ?");
			preparedStmtList.add(criteria.getStatus());
		}

		if (criteria.getDemandId() != null && !criteria.getDemandId().isEmpty()) {
			where.append(" AND dmd.id IN (").append(getIdQueryForStrings(criteria.getDemandId())).append(")");
			addToPreparedStatement(preparedStmtList, criteria.getDemandId());
		}

		if (!CollectionUtils.isEmpty(criteria.getPayer())) {
			where.append(" AND dmd.payer IN (").append(getIdQueryForStrings(criteria.getPayer())).append(")");
			addToPreparedStatement(preparedStmtList, criteria.getPayer());
		}

		if (!CollectionUtils.isEmpty(criteria.getBusinessServices())) {
			where.append(" AND dmd.businessservice IN (")
				 .append(getIdQueryForStrings(criteria.getBusinessServices()))
				 .append(")");
			addToPreparedStatement(preparedStmtList, criteria.getBusinessServices());
		} else if (criteria.getBusinessService() != null) {
			where.append(" AND dmd.businessservice = ?");
			preparedStmtList.add(criteria.getBusinessService());
		}

		if (criteria.getIsPaymentCompleted() != null) {
			where.append(" AND dmd.ispaymentcompleted = ?");
			preparedStmtList.add(criteria.getIsPaymentCompleted());
		}

		if (criteria.getPeriodFrom() != null) {
			where.append(" AND dmd.taxPeriodFrom >= ?");
			preparedStmtList.add(criteria.getPeriodFrom());
		}

		if (criteria.getPeriodTo() != null) {
			where.append(" AND dmd.taxPeriodTo <= ?");
			preparedStmtList.add(criteria.getPeriodTo());
		}

		if (criteria.getIsAdvance() != null) {
			where.append(" AND dmd.isadvance = ?");
			preparedStmtList.add(criteria.getIsAdvance());
		}

		if (criteria.getConsumerCode() != null && !criteria.getConsumerCode().isEmpty()) {
			where.append(" AND LEFT(dmd.consumercode, 10) IN (")
				 .append(getIdQueryForStrings(criteria.getConsumerCode()))
				 .append(")");
			addToPreparedStatement(preparedStmtList, criteria.getConsumerCode());
		}

		StringBuilder pagingClause = new StringBuilder();
		if (criteria.getLimit() != null) {
			pagingClause.append("LIMIT ? ");
			preparedStmtList.add(Math.min(criteria.getLimit(), 200));
		}
		if (criteria.getOffset() != null) {
			pagingClause.append("OFFSET ? ");
			preparedStmtList.add(criteria.getOffset());
		}

		String query = String.format(MERGED_DEMAND_BASE_QUERY, where.toString(), pagingClause.toString());
		log.info("merged demand query: {}", query);
		return query;
	}

	public String getDemandQueryForConsumerCodes(Map<String,Set<String>> businessConsumercodeMap,List<Object> preparedStmtList, String tenantId){
		
		StringBuilder query = new StringBuilder(BASE_DEMAND_QUERY);
		
		query.append("dmd.tenantid=? ");
		preparedStmtList.add(tenantId);
		
		query.append("AND dmd.status='ACTIVE' ");
		
		boolean orFlag = false;
		for (Entry<String, Set<String>> consumerCode : businessConsumercodeMap.entrySet()) {
			
			String businessService = consumerCode.getKey();
			Set<String> consumerCodes = consumerCode.getValue();
			
			if(consumerCodes!=null && !consumerCodes.isEmpty()){
				
				if(orFlag)
					query.append("OR");
				else
					query.append("AND");
				
				query.append(" dmd.businessservice='"+businessService+"' AND dmd.consumercode IN ("
						+getIdQueryForStrings(consumerCodes)+")");
				addToPreparedStatement(preparedStmtList, consumerCodes);
				orFlag=true;
			}
		}
		
		return query.toString();
	}

	public String getCollectedReceiptsQuery(DemandCriteria demandCriteria, List<Object> preparedStatementValues) {
    
		StringBuilder query = new StringBuilder(COLLECTED_RECEIPT_QUERY);
		
		preparedStatementValues.add(demandCriteria.getTenantId());
		
		// Filter by business service
		if (!CollectionUtils.isEmpty(demandCriteria.getBusinessServices())) {
			query.append(" AND pd.businessservice IN (")
				.append(getIdQueryForStrings(demandCriteria.getBusinessServices()))
				.append(")");
			addToPreparedStatement(preparedStatementValues, demandCriteria.getBusinessServices());
		} else if (demandCriteria.getBusinessService() != null) {
			query.append(" AND pd.businessservice = ?");
			preparedStatementValues.add(demandCriteria.getBusinessService());
		}
		
		// Filter by consumer codes
		if (!CollectionUtils.isEmpty(demandCriteria.getConsumerCode())) {
			query.append(" AND bd.consumercode IN (")
				.append(getIdQueryForStrings(demandCriteria.getConsumerCode()))
				.append(")");
			addToPreparedStatement(preparedStatementValues, demandCriteria.getConsumerCode());
		}
		
		// Filter by payment status (only successful payments)
		// query.append(" AND p.paymentstatus IN ('NEW', 'DEPOSITED')");
		
		// Filter by receipt date range if needed
		if (demandCriteria.getPeriodFrom() != null) {
			query.append(" AND pd.receiptdate >= ?");
			preparedStatementValues.add(demandCriteria.getPeriodFrom());
		}
		
		if (demandCriteria.getPeriodTo() != null) {
			query.append(" AND pd.receiptdate <= ?");
			preparedStatementValues.add(demandCriteria.getPeriodTo());
		}
		
		query.append(" ORDER BY pd.receiptdate DESC");
		
		return query.toString();
	}

	public String getDemandQuery(DemandCriteria demandCriteria, List<Object> preparedStatementValues) {

		StringBuilder demandQuery = new StringBuilder(BASE_DEMAND_QUERY);

		String tenantId = demandCriteria.getTenantId();
		String[] tenantIdChunks = tenantId.split("\\.");
		
		if(tenantIdChunks.length == 1){
			demandQuery.append(" dmd.tenantid LIKE ? ");
			preparedStatementValues.add(demandCriteria.getTenantId() + '%');
		}else{
			demandQuery.append(" dmd.tenantid = ? ");
			preparedStatementValues.add(demandCriteria.getTenantId());
		}
		

		if (demandCriteria.getStatus() != null) {

			addAndClause(demandQuery);
			demandQuery.append("dmd.status=?");
			preparedStatementValues.add(demandCriteria.getStatus());
		}
		
		if (demandCriteria.getDemandId() != null && !demandCriteria.getDemandId().isEmpty()) {
			addAndClause(demandQuery);
			demandQuery.append("dmd.id IN (" + getIdQueryForStrings(demandCriteria.getDemandId()) + ")");
			addToPreparedStatement(preparedStatementValues, demandCriteria.getDemandId());
		}
		if (!CollectionUtils.isEmpty(demandCriteria.getPayer())) {
			addAndClause(demandQuery);
			demandQuery.append("dmd.payer IN (" + getIdQueryForStrings(demandCriteria.getPayer()) + ")");
			addToPreparedStatement(preparedStatementValues, demandCriteria.getPayer());
		}
		// Support multiple business services with backward compatibility
		if (!CollectionUtils.isEmpty(demandCriteria.getBusinessServices())) {
			addAndClause(demandQuery);
			demandQuery.append("dmd.businessservice IN (" + getIdQueryForStrings(demandCriteria.getBusinessServices()) + ")");
			addToPreparedStatement(preparedStatementValues, demandCriteria.getBusinessServices());
		} else if (demandCriteria.getBusinessService() != null) {
			// Backward compatibility: single businessService
			addAndClause(demandQuery);
			demandQuery.append("dmd.businessservice=?");
			preparedStatementValues.add(demandCriteria.getBusinessService());
		}
		
		if(demandCriteria.getIsPaymentCompleted() != null){
			addAndClause(demandQuery);
			demandQuery.append("dmd.ispaymentcompleted = ?");
			preparedStatementValues.add(demandCriteria.getIsPaymentCompleted());
		}
		
		if (demandCriteria.getPeriodFrom() != null) {
			addAndClause(demandQuery);
			demandQuery.append("dmd.taxPeriodFrom >= ?");
			preparedStatementValues.add(demandCriteria.getPeriodFrom());
		}
		
		if(demandCriteria.getPeriodTo() != null){
			addAndClause(demandQuery);
			demandQuery.append("dmd.taxPeriodTo <= ?");
			preparedStatementValues.add(demandCriteria.getPeriodTo());
		}

		if (demandCriteria.getIsAdvance() != null) {
			addAndClause(demandQuery);
			demandQuery.append("dmd.isadvance = ?");
			preparedStatementValues.add(demandCriteria.getIsAdvance());
		}
		
		if (demandCriteria.getConsumerCode() != null && !demandCriteria.getConsumerCode().isEmpty()) {
			addAndClause(demandQuery);
			demandQuery.append("dmd.consumercode IN ("
			+ getIdQueryForStrings(demandCriteria.getConsumerCode()) + ")");
			addToPreparedStatement(preparedStatementValues, demandCriteria.getConsumerCode());
		}

		addOrderByClause(demandQuery, DEMAND_QUERY_ORDER_BY_CLAUSE);
		addPagingClause(demandQuery, preparedStatementValues);

		log.info("the query String for demand : " + demandQuery.toString());
		return demandQuery.toString();
	}
	
	private static void addOrderByClause(StringBuilder demandQueryBuilder,String columnName) {
		demandQueryBuilder.append(" ORDER BY " + columnName);
	}

	private static void addPagingClause(StringBuilder demandQueryBuilder, List<Object> preparedStatementValues) {
//		demandQueryBuilder.append(" LIMIT ?");
//		preparedStatementValues.add(500);
//		demandQueryBuilder.append(" OFFSET ?");
//		preparedStatementValues.add(0);
	}

	private static boolean addAndClause(StringBuilder queryString) {
		queryString.append(" AND ");
		return true;
	}
	
	private static String getIdQueryForStrings(Set<String> idList) {

		StringBuilder builder = new StringBuilder();
		int length = idList.size();
		for( int i = 0; i< length; i++){
			builder.append(" ? ");
			if(i != length -1) builder.append(",");
		}
		return builder.toString();
	}

	private void addToPreparedStatement(List<Object> preparedStmtList, Collection<String> ids)
	{
		ids.forEach(id ->{ preparedStmtList.add(id);});
	}
}
