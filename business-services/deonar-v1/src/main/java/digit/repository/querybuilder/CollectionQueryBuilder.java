package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.repository.CollectionSearchCriteria;


@Component
public class CollectionQueryBuilder {
    public String getEntryFee(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {
        final String ENTRYFEE_QUERY = """
            SELECT arrivalid,animal,animalcount,feevalue,totalentryfee 
            FROM eg_deonar_ventryfee 
            """;
        StringBuilder query = new StringBuilder(ENTRYFEE_QUERY);
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        return query.toString();
    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }

    public String getStableFee(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {
        final String STABLEFEE_QUERY = """
            SELECT arrivalid,animal,animalcount,feevalue,totalstablefee 
            FROM eg_deonar_vstablefee 
            """;
        StringBuilder query = new StringBuilder(STABLEFEE_QUERY);
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        return query.toString();
    }
}
