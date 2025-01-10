package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.web.models.GatePassSearchCriteria;
import digit.web.models.citizen.CitizenGatePassCriteria;


@Component
public class GatePassQueryBuilder {

    private static final String BASE_QUERY = """
                select * from eg_deonar_vgatepassdetails
                
                            """;

    public String getGatePassQuery(GatePassSearchCriteria criteria, List<Object> preparedStmtList) {

        StringBuilder query  = new StringBuilder(BASE_QUERY);
        addClauseIfRequired(query, preparedStmtList);
        query.append(  " licencenumber  = ? ");
        preparedStmtList.add(criteria.getLicenceNumber()); 
        return query.toString();
    }

    public String getCitizenGatePassQuery(CitizenGatePassCriteria criteria, List<Object> preparedStmtList){
        final String CITIZEN_GATE_PASS_QUERY = """
                SELECT eds.stakeholdername,
                edlac.stakeholdertype,
                edlac.arrivalid,
                edlac.ddreference, 
                edat."name" ,
                token,
                assigndate,
                assigntime
                from eg_deonar_list_assigned_citizen edlac 
                left join eg_deonar_stakeholder eds on edlac.stakeholderid = eds.id
                left join eg_deonar_animal_type edat on edlac.animaltypeid = edat.id
                left join eg_deonar_fee_paid_details edf on edlac.stakeholderid = edf.paidbystakeholderid 
                where edf.paidbystakeholderid = edlac.stakeholderid and edf.arrivalid = edlac.arrivalid  
                """;
        StringBuilder query = new StringBuilder(CITIZEN_GATE_PASS_QUERY);
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
