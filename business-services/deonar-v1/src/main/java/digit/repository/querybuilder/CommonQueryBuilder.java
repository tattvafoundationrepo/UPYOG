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
               SK.id, \
               SK.stakeholdername AS name,\
               l.licencenumber as licencenumber,\
               at.id as animalId,\
               at.name as animalType \
                FROM \
               eg_deonar_stakeholder SK \
            LEFT JOIN \
            eg_deonar_stakeholder_type_mapping SKTM ON SKTM.stakeholderid = SK.id \
            LEFT JOIN \
            eg_deonar_stakeholders_type SKT ON SKT.id = stakeholdertypeid \
            LEFT JOIN \
            eg_deonar_stakeholder_licence_mapping sl ON sl.stakeholderid = SK.id \
            LEFT JOIN \
            eg_deonar_licence l ON l.id = sl.licenceid \
            LEFT JOIN \
            eg_deonar_stakeholder_animal_type_mapping sam ON sam.stakeholdertypeid = SKT.id \
            LEFT JOIN \
            eg_deonar_animal_type at ON at.id = sam.animalTypeId \
            where  lower( SKT.name) = ? \
            ORDER BY \
            SK.stakeholdername
              """;

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
            case "broker" :
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
