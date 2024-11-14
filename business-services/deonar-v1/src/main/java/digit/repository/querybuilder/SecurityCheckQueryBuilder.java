package digit.repository.querybuilder;

import java.util.List;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.springframework.stereotype.Component;

import digit.web.models.security.SecurityCheckCriteria;
import digit.web.models.shopkeeper.ShopkeeperRequest;

@Component
public class SecurityCheckQueryBuilder {

    private static final String BASE_QUERY2 = """
            select   arrivalid ,importpermission  ,stakeholdername ,stakeholderid ,dateofarrival,
              timeofarrival      ,vehiclenumber      ,mobilenumber      ,email      , stakeholdertypename,
              permissiondate,licencenumber      ,registrationnumber      , validtodate      ,  animaltype
             ,animaltypeid ,token,tradable,stable
              from eg_deonar_vmain
                                """;

    private static final String BASE_QUERY_INSPECTION = """
                       SELECT *
            FROM (
                SELECT b.*,
                       ROW_NUMBER() OVER (PARTITION BY b.animaltypeid, b."token", b.arrivalid ORDER BY b.arrivalid) AS rn
                FROM public.eg_deonar_vmain b
                WHERE EXISTS (
                    SELECT *
                    FROM public.eg_deonar_vinspection vi
                    WHERE b.animaltypeid = vi.animaltypeid
                      AND b."token" = vi."tokenno"
                      AND b.arrivalid = vi.arrivalid
                      %s
                      )
            ) AS subquery
            WHERE rn = 1;
                        """;

    private static final String BASE_QUERY_TRADING_LIST = """
            SELECT * FROM eg_deonar_vtradablelist

            """;

    private static final String BASE_QUERY_STABLING_LIST = """
            SELECT * FROM eg_deonar_vstablelist
            """;
    private static final String BASE_QUERY_SLAUGHTER_LIST = """

             select * from eg_deonar_vslaughterlist
            """;
    private static final String BASE_QUERY_DAWANWALA_ASSIGNMENT_LIST = """

             select * from eg_deonar_vlistfordawanwalaassignment
            """;

    private static final String BASE_QUERY_HELKARI_ASSIGNMENT_LIST = """
            select * from eg_deonar_vlistforhelkariassignment
            """;;

    private static final String BASE_QUERY_REMOVAL_LIST = """
            SELECT * FROM eg_deonar_vremovallist
            """;

    private static final  String ENTRY_FEE_COLLECTION_LIST = """
            select * from eg_deonar_vlistforentryfeecollection
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

    public String getSearchQueryForInspection(SecurityCheckCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder();
        // preparedStmtList.add(criteria.getInspectionId());
        if (criteria.getInspectionId() == 1) {

            query.append("SELECT * FROM public.eg_deonar_vmain b\n" + //
                    "where not  Exists (Select * from public.eg_deonar_vinspection where  b.arrivalid= arrivalid and inspectiontype in (4))");
        }
        if (criteria.getInspectionId() == 2) {
            query.append(String.format(BASE_QUERY_INSPECTION, " and inspectiontype  in (1,2)  "));
        }
        if (criteria.getInspectionId() == 3) {
            query.append(String.format(BASE_QUERY_INSPECTION, " and  inspectiontype not in (4)  "));
        }
        if (criteria.getInspectionId() == 4) {
            query.append(String.format(BASE_QUERY_INSPECTION, " and inspectiontype  in (3) "));
        }
        return query.toString();
    }

    public String getSearchQuery(SecurityCheckCriteria criteria, List<Object> preparedStmtList) {
        if (criteria.getInspectionId() != null) {
            String inspectionQuery = getSearchQueryForInspection(criteria, preparedStmtList);
            return inspectionQuery;
        }
        StringBuilder query = new StringBuilder(BASE_QUERY2);
        
        if(criteria.getForCollection() == true){
            query = new StringBuilder(ENTRY_FEE_COLLECTION_LIST);
            return query.toString();
        }
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
        if (criteria.getTradable() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" tradable = ? ");
            preparedStmtList.add(criteria.getTradable());
        }
        if (criteria.getStable() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" stable = ? ");
            preparedStmtList.add(criteria.getStable());
        }

        addClauseIfRequired(query, preparedStmtList);
        query.append(" token is not null ");
        query.append(" order by arrivalid ");

        return query.toString();
    }

    public String getTradingListQuery(RequestInfoWrapper request, List<Object> preparedStmtList) {
        return BASE_QUERY_TRADING_LIST;
    }

    public String getStablingListQuery(RequestInfoWrapper request, List<Object> preparedStmtList) {
        return BASE_QUERY_STABLING_LIST;
    }

    public String getSlaughterListQuery(ShopkeeperRequest request, List<Object> preparedStmtList) {
        return BASE_QUERY_SLAUGHTER_LIST;

    }

    public String getListForDawanwalaQuery(RequestInfoWrapper request, List<Object> preparedStmtList) {
        return BASE_QUERY_DAWANWALA_ASSIGNMENT_LIST;
    }

    public String getListForHelkariQuery(RequestInfoWrapper request, List<Object> preparedStmtList) {
        return BASE_QUERY_HELKARI_ASSIGNMENT_LIST;
    }

    public String getRemovalListQuery(RequestInfoWrapper request, List<Object> preparedStmtList) {
        return BASE_QUERY_REMOVAL_LIST;
    }
}
