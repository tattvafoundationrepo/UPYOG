package digit.repository.querybuilder;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InspectionQueryBuilder {


		   private static final String BASE_QUERY2 = """
				select * from eg_deonar_vinspection
				""";    



    public String getSearchQuery(String arrivalId,Long inspectionType, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY2);
        if (arrivalId!= null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(arrivalId);
        }
		if (inspectionType!= null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" inspectiontype = ? ");
            preparedStmtList.add(inspectionType);
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
