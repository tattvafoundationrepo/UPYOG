package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.web.models.shopkeeper.ShopkeeperRequest;

@Component
public class ShopkeeperQueryBuilder {


    private static final String BASE_QUERY = """
        SELECT  *
        FROM eg_deonar_vmainshopkeeper
        """;
    public String getShopKeeperQuery(ShopkeeperRequest criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);

        if (criteria.getMobileNo() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" mobilenumber = ? ");
            preparedStmtList.add(criteria.getMobileNo());
        }

        if (criteria.getLicenseNo() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" licencenumber = ? ");
            preparedStmtList.add(criteria.getLicenseNo());
        }
        if (criteria.getRegistrationNo() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" registrationnumber = ? ");
            preparedStmtList.add(criteria.getRegistrationNo());
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


}
