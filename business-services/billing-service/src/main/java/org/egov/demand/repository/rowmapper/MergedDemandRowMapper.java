package org.egov.demand.repository.rowmapper;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.egov.demand.model.MergedDemand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MergedDemandRowMapper implements RowMapper<MergedDemand> {

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public MergedDemand mapRow(ResultSet rs, int rowNum) throws SQLException {

		List<String> businessServices = Collections.emptyList();
		Array businessServicesArray = rs.getArray("business_services");
		if (businessServicesArray != null) {
			String[] services = (String[]) businessServicesArray.getArray();
			if (services != null) {
				businessServices = Arrays.asList(services);
			}
		}

		JsonNode demandsByService = null;
		String demandsByServiceJson = rs.getString("demands_by_service");
		if (demandsByServiceJson != null) {
			try {
				demandsByService = objectMapper.readTree(demandsByServiceJson);
			} catch (JsonProcessingException e) {
				throw new SQLException("Failed to parse demands_by_service JSON", e);
			}
		}

		return MergedDemand.builder()
				.consumerCode(rs.getString("consumer_code"))
				.consumerType(rs.getString("consumer_type"))
				.tenantId(rs.getString("tenant_id"))
				.payer(rs.getString("payer"))
				.licenceeName(rs.getString("licencee_name"))
				.licenceeMobile(rs.getString("licencee_mobile"))
				.reason(rs.getString("reason"))
				.overallStatus(rs.getString("overall_status"))
				.isFullyPaid(rs.getBoolean("is_fully_paid"))
				.hasPartialPayment(rs.getBoolean("has_partial_payment"))
				.businessServices(businessServices)
				.demandsByService(demandsByService)
				.totalTaxAmount(rs.getBigDecimal("total_tax_amount"))
				.totalCollectionAmount(rs.getBigDecimal("total_collection_amount"))
				.totalMinimumPayable(rs.getBigDecimal("total_minimum_payable"))
				.totalRemainingAmount(rs.getBigDecimal("total_remaining_amount"))
				.earliestTaxPeriod(getNullableLong(rs, "earliest_tax_period"))
				.latestTaxPeriod(getNullableLong(rs, "latest_tax_period"))
				.totalDemandCount(rs.getInt("total_demand_count"))
				.paidDemandCount(rs.getInt("paid_demand_count"))
				.unpaidDemandCount(rs.getInt("unpaid_demand_count"))
				.firstCreatedTime(getNullableLong(rs, "first_created_time"))
				.lastModifiedTime(getNullableLong(rs, "last_modified_time"))
				.totalCount(rs.getLong("total_count"))
				.build();
	}

	private Long getNullableLong(ResultSet rs, String column) throws SQLException {
		long value = rs.getLong(column);
		return rs.wasNull() ? null : value;
	}
}
