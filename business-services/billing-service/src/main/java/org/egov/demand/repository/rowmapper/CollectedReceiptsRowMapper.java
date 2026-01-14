package org.egov.demand.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.demand.model.AuditDetail;
import org.egov.demand.model.CollectedReceipt;
import org.egov.demand.util.Util;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
@Component
public class CollectedReceiptsRowMapper implements RowMapper<CollectedReceipt> {

	@Autowired
	private Util util;

	@Override
	public CollectedReceipt mapRow(ResultSet rs, int rowNum) throws SQLException {

		CollectedReceipt receipt=new CollectedReceipt();
		
		receipt.setBusinessService(rs.getString("businessservice"));
		receipt.setConsumerCode(rs.getString("consumercode"));
		receipt.setReceiptAmount(rs.getDouble("receiptamount"));
		receipt.setReceiptDate(rs.getLong("receiptdate"));
		receipt.setReceiptNumber(rs.getString("receiptnumber"));
		receipt.setStatus(rs.getString("status"));
		receipt.setTenantId(rs.getString("tenantid"));
		receipt.setTransactionNumber(rs.getString("transactionnumber"));
		receipt.setTotalAmountPaid(rs.getDouble("totalamountpaid"));

		PGobject additionalDetailsObj = (PGobject) rs.getObject("additionaldetails");
		JsonNode json = util.getJsonValue(additionalDetailsObj);
		receipt.setAdditionalDetails(json);
		
		receipt.setFromPeriod(rs.getLong("fromperiod"));
		receipt.setToPeriod(rs.getLong("toperiod"));
		receipt.setAdvancePayment(rs.getDouble("advancepayment"));
		receipt.setRegularPayment(rs.getDouble("regularpayment"));
		
		AuditDetail auditDetail=new AuditDetail();
		
		auditDetail.setCreatedBy(rs.getString("createdby"));
		auditDetail.setCreatedTime(rs.getLong("createdtime"));
		auditDetail.setLastModifiedBy(rs.getString("lastmodifiedby"));
		auditDetail.setLastModifiedTime(rs.getLong("lastmodifiedtime"));
		
		receipt.setAuditDetail(auditDetail);
		
		return receipt;
	}

}
