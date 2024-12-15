package digit.repository.querybuilder;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
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

    private static final String BOUNDARY_QUERY = """
            SELECT
            id, pincode, district, latitude, longitude, subwardno, officename, wardname, wardno, statename, divisionname
            FROM eg_bmc_boundary 
            """;

    public String getSchemeSearchQuery(CommonSearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        
        switch (criteria.getOption().toLowerCase()) {
            case "caste":
                query.append("eg_bmc_Caste as tbl ");
                break;
            case "religion":
                query.append("eg_bmc_Religion as tbl");
                break;
            case "qualification":
                query.append("eg_bmc_qualificationmaster as tbl");
                break;
            case "divyang":
                query.append("eg_bmc_divyang as tbl");
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

    public String getBoundaryQuery(RequestInfo requestInfo, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BOUNDARY_QUERY);
        String searchfor = requestInfo.getUserInfo().getTenantId();
        addClauseIfRequired(query, preparedStmtList);
        query.append(" tenantid = ? ");
        preparedStmtList.add(searchfor);
        query.append(" ORDER BY pincode,wardname,officename");
        return query.toString();
    }

}
