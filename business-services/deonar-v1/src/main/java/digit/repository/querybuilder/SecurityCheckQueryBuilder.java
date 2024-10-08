package digit.repository.querybuilder;

import java.util.List;
import org.springframework.stereotype.Component;
import digit.web.models.security.SecurityCheckCriteria;

@Component
public class SecurityCheckQueryBuilder {

    private static final String BASE_QUERY = """
                select \
                i.arrivalid  \
                ,i.importpermission  \
                ,a.stakeholdername  \
                ,TO_TIMESTAMP(i.dateofarrival / 1000)::date as dateofarrival \
                ,i.timeofarrival::time as timeofarrival  \
                ,i.vehiclenumber  \
                ,a.mobilenumber  \
                ,a.email  \
                ,c.name as stakeholdertypename  \
                ,e.licencenumber  \
                ,e.registrationnumber  \
                ,TO_TIMESTAMP(dateofarrival / 1000)::date as validtodate  \
                ,h.name as animaltype  \
                ,j.count  \
                from public.eg_deonar_arrival i   \
                left join public.eg_deonar_stakeholder a on  a.id = i.stakeholderid  \
                left join public.eg_deonar_stakeholder_type_mapping b on a.id = b.stakeholderid  \
                left join public.eg_deonar_stakeholders_type c on c.id = b.stakeholdertypeid  \
                left join public.eg_deonar_stakeholder_licence_mapping d on a.id = d.stakeholderid  \
                left join public.eg_deonar_licence e on e.id = d.licenceid  \
                left join public.eg_deonar_stakeholder_animal_type_mapping f on c.id = f.stakeholdertypeid  \
                left join public.eg_deonar_animal_at_arrival j on i.id = j.arrivalid  \
                left join public.eg_deonar_animal_type h on h.id = j.animaltypeid  \
            """;
    private static final String BASE_QUERY2 = """
                                select   i.arrivalid      ,i.importpermission      ,a.stakeholdername      ,TO_TIMESTAMP(i.dateofarrival / 1000)::date as dateofarrival
             ,i.timeofarrival::time as timeofarrival      ,i.vehiclenumber      ,a.mobilenumber      ,a.email      ,c.name as stakeholdertypename
             ,e.licencenumber      ,e.registrationnumber      ,TO_TIMESTAMP(dateofarrival / 1000)::date as validtodate      , h.name as animaltype
             ,j.animaltypeid ,j.count
            from public.eg_deonar_arrival i
             left join public.eg_deonar_stakeholder a on  a.id = i.stakeholderid
             left join public.eg_deonar_stakeholder_type_mapping b on a.id = b.stakeholderid
             left join public.eg_deonar_stakeholders_type c on c.id = b.stakeholdertypeid
             left join public.eg_deonar_stakeholder_licence_mapping d on a.id = d.stakeholderid
             left join public.eg_deonar_licence e on e.id = d.licenceid
             left join public.eg_deonar_stakeholder_animal_type_mapping f on c.id = f.stakeholdertypeid
             left join public.eg_deonar_animal_type h on h.id = f.animaltypeid
             left join public.eg_deonar_animal_at_arrival j on i.id = j.arrivalid and j.animaltypeid = f.animaltypeid
             where count is not null

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
            query.append(" i.arrivalid = ? ");
            preparedStmtList.add(criteria.getArrivalUuid());
        }

        if (criteria.getImportPermissionNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" i.importpermission = ? ");
            preparedStmtList.add(criteria.getImportPermissionNumber());
        }

        if (criteria.getTraderName() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" a.stakeholdername = ? ");
            preparedStmtList.add(criteria.getTraderName());
        }

        if (criteria.getLicenseNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" e.licencenumber = ? ");
            preparedStmtList.add(criteria.getLicenseNumber());
        }

        if (criteria.getVehicleNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" i.vehiclenumber = ? ");
            preparedStmtList.add(criteria.getVehicleNumber());
        }

        return query.toString();
    }
}
