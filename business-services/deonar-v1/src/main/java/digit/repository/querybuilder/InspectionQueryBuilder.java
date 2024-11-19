package digit.repository.querybuilder;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InspectionQueryBuilder {


		   private static final String BASE_QUERY2 = """
				select * from eg_deonar_vinspection
				""";    

	private static final String SEARCH_BASE_QUERY= """
			SELECT  \

			 f.id as animalTypeId, a.id  as aId  \
			FROM \

			    public.eg_deonar_arrival a \

			LEFT JOIN \
			    public.eg_deonar_animal_at_arrival f ON a.id = f.arrivalid

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
