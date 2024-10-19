package digit.repository.querybuilder;

import java.util.List;
import org.springframework.stereotype.Component;
import digit.web.models.security.SecurityCheckCriteria;

@Component
public class SecurityCheckQueryBuilder {

    private static final String BASE_QUERY2 = """
            select   arrivalid ,importpermission  ,stakeholdername ,dateofarrival,
              timeofarrival      ,vehiclenumber      ,mobilenumber      ,email      , stakeholdertypename,
              permissiondate,licencenumber      ,registrationnumber      , validtodate      ,  animaltype
             ,animaltypeid ,token,tradable,stable
              from eg_deonar_vmain

                                """;

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }

    private String createQuery(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ?");
            if (i != length - 1)
                builder.append(",");
        }
        return builder.toString();
    }

    private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
        ids.forEach(preparedStmtList::add);
    }

    public String getSearchQuery(SecurityCheckCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY2);

        if (criteria.getArrivalUuid() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(criteria.getArrivalUuid());
        }

        if (criteria.getImportPermissionNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" importpermission = ? ");
            preparedStmtList.add(criteria.getImportPermissionNumber());
        }

        if (criteria.getTraderName() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" stakeholdername = ? ");
            preparedStmtList.add(criteria.getTraderName());
        }

        if (criteria.getLicenseNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" licencenumber = ? ");
            preparedStmtList.add(criteria.getLicenseNumber());
        }

        if (criteria.getVehicleNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" vehiclenumber = ? ");
            preparedStmtList.add(criteria.getVehicleNumber());
        }
        if(criteria.getTradable() != null){
            addClauseIfRequired(query, preparedStmtList);
            query.append(" tradable = ? ");
            preparedStmtList.add(criteria.getTradable());
        }
        if(criteria.getStable() != null){
            addClauseIfRequired(query, preparedStmtList);
            query.append(" stable = ? ");
            preparedStmtList.add(criteria.getStable());
        }
        addClauseIfRequired(query, preparedStmtList);
        query.append(" token is not null ");
        query.append(" order by arrivalid ");

        return query.toString();
    }
}
