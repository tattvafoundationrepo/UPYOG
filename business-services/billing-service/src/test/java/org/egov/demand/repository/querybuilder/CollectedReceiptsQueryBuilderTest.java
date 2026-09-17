package org.egov.demand.repository.querybuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.egov.demand.model.DemandCriteria;
import org.junit.jupiter.api.Test;

/**
 * Locks {@link DemandQueryBuilder#getCollectedReceiptsQuery}.
 *
 * <p>A consumer-code search (demand/_search) gets the bill-scoped query; every other search must keep the
 * legacy text exactly. The two must also agree on the SELECT list, since CollectedReceiptsRowMapper reads the
 * columns by name.
 */
class CollectedReceiptsQueryBuilderTest {

	private final DemandQueryBuilder builder = new DemandQueryBuilder();

	private static int placeholders(String sql) {
		return (int) sql.chars().filter(c -> c == '?').count();
	}

	private static DemandCriteria criteria() {
		DemandCriteria criteria = new DemandCriteria();
		criteria.setTenantId("mh.mumbai");
		return criteria;
	}

	/** Output column names of the outermost SELECT, in order. */
	private static List<String> outerColumns(String sql) {
		String select = sql.substring(sql.lastIndexOf("SELECT ") + "SELECT ".length(), sql.lastIndexOf(" FROM "));
		// Split on top-level commas only: COALESCE(x, 0) carries one inside its parentheses.
		List<String> columns = new ArrayList<>();
		int depth = 0;
		int start = 0;
		for (int i = 0; i < select.length(); i++) {
			char c = select.charAt(i);
			if (c == '(') depth++;
			else if (c == ')') depth--;
			else if (c == ',' && depth == 0) {
				columns.add(select.substring(start, i));
				start = i + 1;
			}
		}
		columns.add(select.substring(start));
		List<String> names = new ArrayList<>();
		for (String column : columns) {
			Matcher alias = Pattern.compile("(?:as\\s+|\\.)(\\w+)\\s*$").matcher(column.trim());
			names.add(alias.find() ? alias.group(1) : column.trim());
		}
		return names;
	}

	@Test
	void consumerCodeSearch_totalsOnlyItsOwnBills_bindsInTheSameOrder() {
		DemandCriteria criteria = criteria();
		criteria.setConsumerCode(new LinkedHashSet<>(Arrays.asList("5000004942rf", "5000004942lf")));
		criteria.setBusinessServices(new LinkedHashSet<>(Arrays.asList("TX.Emarket_Rental_Fees")));
		criteria.setPeriodFrom(1_700_000_000_000L);
		List<Object> params = new ArrayList<>();

		String sql = builder.getCollectedReceiptsQuery(criteria, params, false);

		assertTrue(sql.startsWith(DemandQueryBuilder.COLLECTED_RECEIPT_BY_CONSUMER_QUERY_HEAD));
		assertTrue(sql.endsWith(DemandQueryBuilder.COLLECTED_RECEIPT_BY_CONSUMER_QUERY_TAIL));
		int receiptsEnd = sql.indexOf(" ), bill_periods AS (");
		assertTrue(sql.indexOf("AND bd.consumercode IN (") < receiptsEnd, "consumer filter must sit inside the receipts CTE");
		assertTrue(sql.indexOf("AND pd.businessservice IN (") < receiptsEnd, "service filter must sit inside the receipts CTE");
		assertTrue(sql.indexOf("AND pd.receiptdate >= ?") < receiptsEnd, "period filter must sit inside the receipts CTE");
		assertEquals(2, sql.split(Pattern.quote("WHERE bdt.billid IN (SELECT bill_id FROM receipts)"), -1).length - 1,
				"both per-bill totals must be scoped to the receipts' bills");
		assertEquals(Arrays.asList("mh.mumbai", "TX.Emarket_Rental_Fees", "5000004942rf", "5000004942lf", 1_700_000_000_000L), params);
		assertEquals(params.size(), placeholders(sql));
	}

	@Test
	void consumerCodeSearch_returnsTheSameColumnsAsTheLegacyQuery() {
		DemandCriteria scoped = criteria();
		scoped.setConsumerCode(new LinkedHashSet<>(Arrays.asList("5000004942rf")));
		String scopedSql = builder.getCollectedReceiptsQuery(scoped, new ArrayList<>(), false);
		String legacySql = builder.getCollectedReceiptsQuery(criteria(), new ArrayList<>(), false);

		assertEquals(outerColumns(legacySql), outerColumns(scopedSql));
		assertEquals(18, outerColumns(scopedSql).size());
	}

	@Test
	void searchWithoutConsumerCodes_keepsTheLegacyQueryText() {
		DemandCriteria criteria = criteria();
		criteria.setBusinessServices(new LinkedHashSet<>(Arrays.asList("TX.Emarket_Rental_Fees")));
		List<Object> params = new ArrayList<>();

		String sql = builder.getCollectedReceiptsQuery(criteria, params, false);

		assertEquals(DemandQueryBuilder.COLLECTED_RECEIPT_QUERY + " AND pd.businessservice IN ( ? ) ORDER BY pd.receiptdate DESC", sql);
		assertEquals(Arrays.asList("mh.mumbai", "TX.Emarket_Rental_Fees"), params);
	}

	@Test
	void mergedSearch_keepsTheLegacyQueryText_evenWithConsumerCodes() {
		DemandCriteria criteria = criteria();
		criteria.setConsumerCode(new LinkedHashSet<>(Arrays.asList("5000004942")));
		List<Object> params = new ArrayList<>();

		String sql = builder.getCollectedReceiptsQuery(criteria, params, true);

		assertEquals(DemandQueryBuilder.COLLECTED_RECEIPT_QUERY + " AND LEFT(bd.consumercode, 10) IN ( ? ) ORDER BY pd.receiptdate DESC", sql);
		assertFalse(sql.contains("receipts AS"));
		assertEquals(Arrays.asList("mh.mumbai", "5000004942"), params);
	}
}
