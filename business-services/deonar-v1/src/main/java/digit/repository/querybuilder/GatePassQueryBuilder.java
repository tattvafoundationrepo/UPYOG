package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import digit.web.models.GatePassSearchCriteria;


@Component
public class GatePassQueryBuilder {

    private static final String BASE_QUERY = """
                select * from eg_deonar_vgatepassdetails
                
                            """;

    public String getGatePassQuery(GatePassSearchCriteria criteria, List<Object> preparedStmtList) {

        StringBuilder query  = new StringBuilder(BASE_QUERY);
        addClauseIfRequired(query, preparedStmtList);
        query.append(  " licencenumber  = ? ");
        preparedStmtList.add(criteria.getLicenceNumber()); 
        return query.toString();
    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {

        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }

}
