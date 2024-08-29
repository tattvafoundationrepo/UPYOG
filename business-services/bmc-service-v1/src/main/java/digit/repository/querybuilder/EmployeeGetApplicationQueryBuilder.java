package digit.repository.querybuilder;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import digit.web.models.SchemeApplicationSearchCriteria;

@Component
public class EmployeeGetApplicationQueryBuilder {


    private static final String BASE_QUERY = """
         with data as(
            Select
            *,
            RANK() over (PARTITION BY businessid ORDER BY createdtime DESC) as rank1
            from eg_wf_processinstance_v2
            )
                    SELECT  a.applicationnumber ,a.userid ,a.tenantid,a.agreetopay,a.statement,
                     bs.name as scheme,
                    ROW_NUMBER() OVER (PARTITION BY e.ward, optedid ORDER BY RANDOM()) AS rn
    """;

    private static final String FROM_TABLES = """
        FROM data b
        INNER JOIN eg_bmc_userschemeapplication a ON b.businessid = a.applicationnumber
        LEFT JOIN eg_bmc_usersubschememapping f ON f.applicationnumber = a.applicationnumber 
        LEFT JOIN eg_bmc_schemes bs ON a.optedid = bs.id
        LEFT JOIN eg_bmc_userotherdetails d ON a.userid = d.userid AND a.tenantid = d.tenantid
        INNER JOIN eg_bmc_employeewardmapper e ON e.uuid = ? AND e.ward = d.ward
        WHERE b.previousstatus IN 
        (SELECT ewsv.state FROM public.eg_wf_state_v2 ewsv 
           LEFT JOIN public.eg_wf_action_v2 ewav ON ewsv."uuid" = ewav.currentstate 
            WHERE ewav."action" = ? AND ewav.tenantid = ?) and b.rank1=1

    """;

    private static final String RANKED_QUERY = """
        WITH RankedData AS (
            %s %s
    """;
    private static final String RANKED_QUERY_SELECT = """
            )
        SELECT *,
        CASE WHEN rn <= 1 THEN 'Selected' ELSE 'NotSelected' END
        FROM RankedData
        WHERE rn <= ?
    """;

    public String getQueryBasedOnAction(List<Object> preparedStmtList, SchemeApplicationSearchCriteria criteria) {
        StringBuilder query = new StringBuilder();


        if (!ObjectUtils.isEmpty(criteria.getState())) {

            if ("randomize".equalsIgnoreCase(criteria.getState())) {

                query.append(String.format(RANKED_QUERY, BASE_QUERY, FROM_TABLES));
            }
            else{
                query.append(BASE_QUERY)
                    .append(FROM_TABLES); 
            }
        }
        if (!ObjectUtils.isEmpty(criteria.getUuid())) {
            preparedStmtList.add(criteria.getUuid());
        }
        if (!ObjectUtils.isEmpty(criteria.getState())) {
            preparedStmtList.add(criteria.getState().toUpperCase());
        }
        preparedStmtList.add(criteria.getTenantId());

        if (!ObjectUtils.isEmpty(criteria.getSchemeId())) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" a.optedid = ? ");
            preparedStmtList.add(criteria.getSchemeId());
        }
        if (!ObjectUtils.isEmpty(criteria.getMachineId())) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" f.machineid = ? ");
            preparedStmtList.add(criteria.getMachineId());
        }
        if (!ObjectUtils.isEmpty(criteria.getCourseId())) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" f.courseId = ?");
            preparedStmtList.add(criteria.getCourseId());
        }

        if ("randomize".equalsIgnoreCase(criteria.getState())) {
            query.append(RANKED_QUERY_SELECT);
            preparedStmtList.add(criteria.getRandomizationNumber());
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


