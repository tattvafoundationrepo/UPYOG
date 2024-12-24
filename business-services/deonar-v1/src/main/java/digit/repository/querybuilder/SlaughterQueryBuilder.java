package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.web.SlaughterSearchCriteria;

@Component
public class SlaughterQueryBuilder {

    private static final String BASE_QUERY = """
            SELECT  *
            FROM eg_deonar_list_slaughter
            """;

    public String getSlaughterListQuery(SlaughterSearchCriteria criteria, List<Object> preparedStmtList) {

        StringBuilder query = new StringBuilder(BASE_QUERY);

        if (criteria.getSlaughterUnitType().equalsIgnoreCase("emergency")) {
            query.append(" where opinionid in (16,17,18) ");
        } else if (criteria.getSlaughterUnitType().equalsIgnoreCase("booking")) {
        } else {
            query.append(" where opinionid = 3 ");
        }

        return query.toString();

    }
}
