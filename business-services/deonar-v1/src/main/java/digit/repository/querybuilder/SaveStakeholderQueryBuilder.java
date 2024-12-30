package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

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

    public String getStakeholderQuery(StakeholderCheckCriteria criteria, List<Object> preparedStmtList){
        StringBuilder query = new StringBuilder(GET_ALL);
        query.append(" FROM (");
        query.append(GET_STAKEHOLDER);
        if(criteria == null){
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
        }
        query.append(") dl ");
        query.append(" LEFT JOIN eg_deonar_stakeholder sa ON dl.id = sa.id");
        query.append(" ORDER BY stakeholdername ASC ");
        return query.toString();
    }

}
