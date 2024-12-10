package digit.repository.querybuilder;

import org.springframework.stereotype.Component;

import java.util.List;
import org.springframework.util.ObjectUtils;

@Component
public class CountQueryBuilder {

    private static final String BASE_QUERY = """
            SELECT DISTINCT action,wardcnt, totalcnt
            FROM eg_bmc_vcountreport b
            """;

    private static final String EXISTS_CLAUSE = """
            WHERE EXISTS (
                SELECT * 
                FROM eg_bmc_employeewardmapper c 
                WHERE uuid = ? AND c.ward = b.block
            )
            """;

    private static final String ACTION_FILTER = " AND action IN (";

    private static final String WHERE_ACTION_FILTER = " WHERE action IN (";

   
    public String getActionCountQuery(Boolean forVerify, String uuid, List<String> actions, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);

        if (forVerify) {
            query.append(EXISTS_CLAUSE);
            preparedStmtList.add(uuid);
        }
        if (!ObjectUtils.isEmpty(actions)) {
            if (forVerify) {
                query.append(ACTION_FILTER);
            } else {
                query.append(WHERE_ACTION_FILTER);
            }
            query.append(createPlaceholders(actions.size())).append(")");
            addToPreparedStatement(preparedStmtList, actions);
        }

        return query.toString();
    }


    private String createPlaceholders(int count) {
        return String.join(",", "?".repeat(count).split(""));
    }


    private void addToPreparedStatement(List<Object> preparedStmtList, List<String> values) {
        values.forEach(preparedStmtList::add);
    }
}



