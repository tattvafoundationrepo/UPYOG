package digit.repository.querybuilder;

import digit.web.models.security.SecurityCheckCriteria;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InspectionQueryBuilder {

	private static final String BASE_QUERY = """
			      select \
			a.arrivalid \
			,a.importpermission \
			,b.stakeholdername \
			,TO_TIMESTAMP(i.inspectiondate / 1000)::date as inspectiondate \
			, TO_CHAR(TO_TIMESTAMP(i.inspectiondate / 1000), 'Day') AS day \
			, e.licencenumber \
			, j.count \
			,an.animaltokenumber \
			,h.name \
			,g.inspectionindicatorvalue \
			,u.animalStabling \
			from public.eg_deonar_inspection i \
			left join public.eg_deonar_arrival a on a.id=i.arrivalId \
			left join public.eg_deonar_stakeholder b on b.id=a.stakeholderid \
			left join public.eg_stakeholder_licence_mapping d on b.id = d.stakeholderid \
			left join public.eg_deonar_licence e on e.id = d.licenceid \
			left join public.eg_deonar_animal_at_arrival f on a.id = f.arrivalid \
			left join public.eg_deonar_inspection_details g on g.inspectionid=i.id \
			left join public.eg_deonar_inspection_indicators h on h.id= g.inspectionindicatorid \
			left join public.eg_deonar_stable_stakeholder s on s.stakeholderid=b.id \
			left join public.eg_deonar_stable_unit u on u.id=s.stableunitid \
			left join public.eg_deonar_animal an on an.id=i.animalid \

			""";

		   private static final String BASE_QUERY2 = """
				select * from eg_deonar_vinspection
				""";    

	private static final String SEARCH_BASE_QUERY= """
			SELECT  \

			 f.id as animalTypeId, a.id  as aId  \
			FROM \

			    public.eg_deonar_arrival a \

			LEFT JOIN \
			    public.eg_deonar_animal_at_arrival f ON a.id = f.arrivalid

			""";

	public String getSearchBaseQuery(String arrivalId, List<Object> preparedStmtList) {

		StringBuilder query = new StringBuilder(SEARCH_BASE_QUERY);
		if (arrivalId != null) {
			addClauseIfRequired(query, preparedStmtList);
			query.append(" a.arrivalid = ? ");
			preparedStmtList.add(arrivalId);
		}
		return query.toString();
	}

    public String getSearchQuery(String arrivalId,Long inspectionType, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY2);
        if (arrivalId!= null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(arrivalId);
        }
		if (inspectionType!= null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" inspectiontype = ? ");
            preparedStmtList.add(inspectionType);
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
