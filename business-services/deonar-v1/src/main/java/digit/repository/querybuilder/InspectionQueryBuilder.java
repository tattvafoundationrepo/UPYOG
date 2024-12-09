package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.web.models.inspection.InspectionSearchCriteria;

@Component
public class InspectionQueryBuilder {

    private static final String BASE_QUERY2 = """
            select * from eg_deonar_vinspection
            """;

    private static final String ANTEMORTEM_DEFAULT_DETAILS = """
            select * from eg_deonar_vantemortemdefaultsave
            """;

    private static final String RE_ANTEMORTEM_DEFAULT_DETAILS = """
            select * from eg_deonar_vreantemortemdefaultsave
            """;

    private static final String PREMORTEM_DEFAULT_DETAILS = """
            select * from eg_deonar_vpremortemdefaultsave
            """;

    private static final String POSTMORTEM_DEFAULT_DETAILS = """
            select * from eg_deonar_vpostmortemdefaultsave
            """;

    public String getSearchQuery(InspectionSearchCriteria criteria, List<Object> preparedStmtList) {
        
        StringBuilder query  = new StringBuilder();
        if(criteria.getIsInitialCheck() == true){
             query = new StringBuilder(BASE_QUERY2);
        }
        else{
         int inpectionType = criteria.getInspectionType().intValue();
         
         switch(inpectionType) {

            case 1 :
               query.append(ANTEMORTEM_DEFAULT_DETAILS);
               break;

            case  2 :
               query.append(RE_ANTEMORTEM_DEFAULT_DETAILS);
               break;   

            case  3 : 
               query.append(PREMORTEM_DEFAULT_DETAILS);
               break;  
               
            case  4 : 
               query.append(POSTMORTEM_DEFAULT_DETAILS);
               break;

         }

        }
        

        if (criteria.getEntryUnitId() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(criteria.getEntryUnitId());
        }
        if (criteria.getInspectionType() != null && criteria.getIsInitialCheck() == true) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" inspectiontype = ? ");
            preparedStmtList.add(criteria.getInspectionType());
        }
        if (criteria.getAnimalTypeId() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" animaltypeid = ? ");
            preparedStmtList.add(criteria.getAnimalTypeId());
        }
        if (criteria.getToken() != null) {
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
