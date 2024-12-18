package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

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

    private static final String STAKEHOLDER_VIEW_QUERY = """
            select stakeholdername as name , stakeholderid as id, licencenumber, animalid , animaltype
            , mobilenumber , tradertype , registrationnumber
            """;

    public String getStakeHolderQuery(CommonSearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(STAKEHOLDER_VIEW_QUERY);
        String stakeHolder = criteria.getOption().toLowerCase();
        switch (stakeHolder) {
            case "trader":
                query.append(" from eg_deonar_vtrader ");

                break;
            case "shopkeeper":
                query.append(" from eg_deonar_vshopkeeper ");
                break;
            case "gawal":
                query.append(" from eg_deonar_vgawal ");

                break;
            case "dawanwala":
                query.append(" from  eg_deonar_vdawanwala ");

                break;
            case "helkari":
                query.append(" from eg_deonar_vhelkari ");

                break;
            case "broker":
                query.append(" from eg_deonar_vbroker ");

                break;

            default:
                break;
        }
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
            case "vehicle":
                query.append("eg_deonar_vehicle_type as tbl");
                break;
            case "removal":
                query.append("eg_deonar_removal_type as tbl");
                break;
            case "slaughterunit":
                query.append("eg_deonar_slaughter_unit as tbl");
                break;
            case "opinion":
                query.append("eg_deonar_inspection_opinion ");
                break;    
            default:
                query.append("(Select 0 as id, 'No Record found'  as name) as tbl ");
                break;
        }
        if(!ObjectUtils.isEmpty(criteria.getInspectionTypeId())){
            addClauseIfRequired(query, preparedStmtList);
            query.append(" \"InspectionType\" = ? ");
            preparedStmtList.add(criteria.getInspectionTypeId());
        }

        query.append(ORDERBY_NAME);
        return query.toString();
    }

    public String getSlaughterUnitShift(CommonSearchCriteria commonSearchCriteria, List<Object> preparedStmtList) {
        String sql = """    
            select opentime , closetime from eg_deonar_slaughter_unit_shift edsus 
            where edsus.slaughterunitid =?
 
                                """;
        preparedStmtList.add(commonSearchCriteria.getId());
         return sql;                       
    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {

        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }

}
