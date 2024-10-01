package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.repository.CommonSearchCriteria;

@Component
public class CommonQueryBuilder {
    // for Caste
    private static final String BASE_QUERY = """
            SELECT  \
            id , UPPER(name) as name \
            FROM \
            """;

    private static final String ORDERBY_NAME = " ORDER BY name DESC ";

    private static final String STAKEHOLDER_BASE_QUERY = """
            SELECT \
            SK.id , SK.stakeholdername as name \
            from eg_deonar_stakeholder SK \
            left join eg_deonar_stakeholder_type_mapping SKTM on SKTM.stakeholderid = SK.id \
            left join eg_deonar_stakeholders_type SKT on SKT.id = stakeholdertypeid \
            where lower(SKT.name) = ? \
            order by SK.stakeholdername
            """;

    public String getStakeHolderQuery(CommonSearchCriteria criteria, List<Object> preparedStmtList){
        StringBuilder query = new StringBuilder(STAKEHOLDER_BASE_QUERY);
        preparedStmtList.add(criteria.getOption().toLowerCase());
        return query.toString();
    }
    
    public String getSchemeSearchQuery(CommonSearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        
        switch (criteria.getOption().toLowerCase()) {
            case "animal":
                query.append("eg_deonar_animal_type as tbl ");
                break;
            case "stakeholder":
                query.append("eg_deonar_stakeholders_type as tbl");
                break;
            case "collection":
                query.append("eg_deonar_collection_type as tbl");
                break;
            case "stable":
                query.append("eg_deonar_stable_unit as tbl");
                break;
            case "document":
                query.append("eg_bmc_document as tbl");
                break;
            default:
                query.append("(Select 0 as id, 'No Record found'  as name) as tbl ");
                break;
        }
        query.append(ORDERBY_NAME);
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
