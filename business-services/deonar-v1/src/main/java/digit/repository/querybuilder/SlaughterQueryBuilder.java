package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.web.SlaughterSearchCriteria;

@Component
public class SlaughterQueryBuilder {

    private static final String BASE_QUERY = """
            SELECT  DISTINCT *
            FROM eg_deonar_list_slaughter edls
            """;
    private static final String BOOKED_QUERY = """
               left join eg_deonar_booked_for_slaughter edbfs on edls.ddreference = edbfs.ddreference 
               and edls.animaltypeid = edbfs.animaltypeid and edls."token" = edbfs."token"
            """;
    public String getSlaughterListQuery(SlaughterSearchCriteria criteria, List<Object> preparedStmtList) {

        StringBuilder query = new StringBuilder(BASE_QUERY);

        if (criteria.getSlaughterUnitType().equalsIgnoreCase("emergency")) {
            query.append(BOOKED_QUERY);
            query.append(" where opinionid in (16,17,18)  and  edbfs.unitshiftid in (4,5,6) ");
        }  else if (criteria.getSlaughterUnitType().equalsIgnoreCase("normal")){
            query.append(BOOKED_QUERY);
            query.append(" where opinionid = 3 and  unitshiftid in (1,2,3) ");
        } else if (criteria.getSlaughterUnitType().equalsIgnoreCase("export")) {
            query.append(BOOKED_QUERY);
            query.append(" where opinionid = 3 and  unitshiftid in (7,8,9) ");
        }else if (criteria.getSlaughterUnitType().equalsIgnoreCase("booking")) {
        }

        return query.toString();

    }
}
