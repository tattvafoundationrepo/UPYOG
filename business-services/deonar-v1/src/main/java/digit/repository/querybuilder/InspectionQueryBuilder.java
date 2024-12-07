package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.web.models.inspection.InspectionSearchCriteria;

@Component
public class InspectionQueryBuilder {


		   private static final String BASE_QUERY2 = """
				select * from eg_deonar_vinspection
				""";    



    public String getSearchQuery(InspectionSearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY2);
        if (criteria.getEntryUnitId()!= null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(criteria.getEntryUnitId());
        }
		if (criteria.getInspectionType()!= null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" inspectiontype = ? ");
            preparedStmtList.add(criteria.getInspectionType());
        }
        if (criteria.getAnimalTypeId()!= null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" animaltypeid = ? ");
            preparedStmtList.add(criteria.getAnimalTypeId());
        }
        if (criteria.getToken()!= null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" tokenno = ? ");
            preparedStmtList.add(criteria.getToken());
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

}
