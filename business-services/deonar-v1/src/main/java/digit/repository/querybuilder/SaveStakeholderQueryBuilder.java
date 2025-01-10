package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.web.models.stakeholders.StakeholderAssignedCriteria;
import digit.web.models.stakeholders.StakeholderCheckCriteria;
import digit.web.models.stakeholders.Stakeholders;

@Component
public class SaveStakeholderQueryBuilder {

    private static final String BASE_QUERY = """
            INSERT INTO eg_deonar_stakeholder
                (
                    id,
                    stakeholdername,
                    address1,
                    address2,
                    pincode,
                    mobilenumber,
                    contactnumber,
                    email,
                    createdat,
                    updatedat,
                    createdby,
                    updatedby
                )
                VALUES
                (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
            """;

    private static final String UPDATE_STAKEHOLDER_ANIMAL_TYPE_TABLE = """

                INSERT INTO eg_deonar_stakeholder_animal_type_mapping (
                    stakeholdertypeid,
                    animaltypeid,
                    createdat,
                    updatedat,
                    createdby,
                    updatedby
                ) VALUES (
                    ?, ?, ?, ?, ?, ?
                );
            """;

    private static final String UPDATE_STAKEHOLDER_LICENSE_TABLE = """

                INSERT INTO eg_deonar_stakeholder_licence_mapping (
                    licenseid,
                    animaltypeid,
                    createdat,
                    updatedat,
                    createdby,
                    updatedby
                ) VALUES (
                    ?, ?, ?, ?, ?, ?
                );
            """;

    private static final String UPDATE_STAKEHOLDER_TYPE_TABLE = """

                INSERT INTO eg_deonar_stakeholder_type_mapping (
                    stakeholdertypeid,
                    stakeholderid,
                    createdat,
                    updatedat,
                    createdby,
                    updatedby
                ) VALUES (
                    ?, ?, ?, ?, ?, ?
                );
            """;

    private static final String GET_STAKEHOLDER = """
            SELECT id, stakeholdername, mobilenumber, email,
            licencenumber, registrationnumber, stakeholdertypename, animaltype
            """;

    private static final String GET_CITIZEN = """
            SELECT id, stakeholdername, mobilenumber, email,
            NULL AS licencenumber, NULL AS registrationnumber, stakeholdertypename, NULL AS animaltype
            """;
    private static final String GET_ALL = """
            SELECT dl.stakeholdername AS stakeholdername,
            dl.mobilenumber AS mobilenumber,
            dl.email AS email,
            dl.licencenumber AS licencenumber,
            dl.registrationnumber AS registrationnumber,
            dl.stakeholdertypename AS stakeholdertype,
            dl.animaltype AS animaltype,
            sa.address1 AS address1,
            sa.address2 AS address2,
            sa.pincode AS pincode
            """;

    private static final String GET_ASSIGNED_STAKEHOLDER = """
            SELECT eds.stakeholdername,
                edl.licencenumber,
                edlac.stakeholdertype,
                edlac.arrivalid,
                edlac.ddreference,
                edat.name,
                token,
            """;

    private static final String STAKEHOLDER_COLOUMNS = """
            assigndate,
            assigntime
            """;

    private static final String SHOPKEEPER_COLOUMNS = """
            purchasedate as assigndate,
            purchasetime as assigntime
            """;

    private static final String TABLE_JOINS = """
            left join eg_deonar_stakeholder eds on edlac.stakeholderid = eds.id
            left join eg_deonar_animal_type edat on edlac.animaltypeid = edat.id
            left join eg_deonar_stakeholder_licence_mapping edslm on edlac.stakeholderid = edslm.stakeholderid 
            left join eg_deonar_licence edl on edslm.id = edl.id 
            """;
    private static final String UNION_ALL = """
            UNION ALL \
            """;

    public String getSaveStakeholderQuery(Stakeholders stakeholderDetails) {
        StringBuilder query = new StringBuilder(BASE_QUERY);

        return query.toString();
    }

    public String getStakeholderTypeQuery(Stakeholders stakeholderDetails) {
        StringBuilder query = new StringBuilder(UPDATE_STAKEHOLDER_TYPE_TABLE);

        return query.toString();
    }

    public String getAnimalTypeQuery(Stakeholders stakeholderDetails) {
        StringBuilder query = new StringBuilder(UPDATE_STAKEHOLDER_ANIMAL_TYPE_TABLE);

        return query.toString();
    }

    public String getSaveLicenseIdQuery(Stakeholders stakeholderDetails) {
        StringBuilder query = new StringBuilder(UPDATE_STAKEHOLDER_LICENSE_TABLE);

        return query.toString();
    }

    public String getStakeholderQuery(StakeholderCheckCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(GET_ALL);
        query.append(" FROM (");
        query.append(GET_STAKEHOLDER);
        if (criteria == null) {
            query.append(" FROM eg_deonar_deal_list_animal_broker ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_dawanwala ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_gawal ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_helkari ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_shopkeeper ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_trader ");
            query.append(UNION_ALL);
            query.append(GET_CITIZEN);
            query.append(" FROM eg_deonar_list_citizen ");
        } else {
            query.append(" FROM eg_deonar_deal_list_animal_broker ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_dawanwala ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_gawal ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_helkari ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_shopkeeper ");
            query.append(UNION_ALL);
            query.append(GET_STAKEHOLDER);
            query.append(" FROM eg_deonar_deal_list_animal_trader ");
            query.append(UNION_ALL);
            query.append(GET_CITIZEN);
            query.append(" FROM eg_deonar_list_citizen ");
        }
        query.append(") dl ");
        query.append(" LEFT JOIN eg_deonar_stakeholder sa ON dl.id = sa.id");
        query.append(" ORDER BY stakeholdername ASC ");
        return query.toString();
    }

    public String getAssignedStakeholderQuery(StakeholderAssignedCriteria criteria){
        StringBuilder query = new StringBuilder(GET_ASSIGNED_STAKEHOLDER);
        switch(criteria.getStakeholderTypeId()) {

            case 2:
                query.append(SHOPKEEPER_COLOUMNS);
                query.append(" from eg_deonar_list_assigned_shopkeeper ");
                break;

            case 3:
                query.append(STAKEHOLDER_COLOUMNS);
                query.append(" from eg_deonar_list_assigned_gawal ");
                break;

            case 4:
                query.append(STAKEHOLDER_COLOUMNS);
                query.append(" from eg_deonar_list_assigned_dawanwala ");
                break;

            case 5:
                query.append(STAKEHOLDER_COLOUMNS);
                query.append(" from eg_deonar_list_assigned_helkari ");
                break;

            case 6:
                query.append(STAKEHOLDER_COLOUMNS);
                query.append(" from eg_deonar_list_assigned_broker ");
                break;

            case 7:
                query.append(STAKEHOLDER_COLOUMNS);
                query.append(" from eg_deonar_list_assigned_citizen ");
                break;

            default:
                break;
        }
        query.append(" edlac ");
        query.append(TABLE_JOINS);
        query.append(" where eds.stakeholdername = '").append(String.valueOf(criteria.getStakeholderName())).append("' ").append(" and eds.mobilenumber = ").append(String.valueOf(criteria.getMobileNumber())).append(" and edl.licencenumber = '").append(String.valueOf(criteria.getLicenseNumber())).append("' ");
        return query.toString();
    }

}
