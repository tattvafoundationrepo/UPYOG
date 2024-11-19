package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import digit.web.models.GatePassSearchCriteria;


@Component
public class GatePassQueryBuilder {

    private static final String BASE_QUERY = """
                  select
            w.carcassweight,
            w.kenaweight,
            edat."name" as animalType
            a.ddreference

                            """;

    private static final String FROM_TABLES = """

            from eg_deonar_vcurrentassignmentlist a
            left join eg_deonar_animal_slaughter_weight w on a.ddreference = w.referencenumber and a.animaltypeid = w.animaltypeid and a."token" = w.tokenno
            left join eg_deonar_animal_type edat on a.animaltypeid = edat.id

                   """;

    public String getGatePassQuery(GatePassSearchCriteria criteria, List<Object> preparedStmtList) {

        StringBuilder query  = new StringBuilder(BASE_QUERY);
        query.append(FROM_TABLES);

        if(!ObjectUtils.isEmpty(criteria.getHelkari())){
          addClauseIfRequired(query, preparedStmtList);
          query.append(  " a.stakeholderid  = ? ");
          preparedStmtList.add(criteria.getHelkari());
          
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

}
