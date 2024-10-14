package digit.repository;

import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.PeEnquiryQueryBuilder;
import digit.repository.rowmapper.PeEnquiryRowMapper;
import digit.web.models.GetPeRequest;
import digit.web.models.PeEnquiryResponse;
import digit.web.models.UpdateCeListRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PeEnquiryRepository {

    @Autowired
    private PeEnquiryQueryBuilder queryBuilder;
    
    @Autowired
    private PeEnquiryRowMapper rowmapper;

    @Autowired
    JdbcTemplate jdbcTemplate;



    public List<PeEnquiryResponse> getPeEnqData(GetPeRequest criteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEnquirySearchQuery(criteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, rowmapper, preparedStmtList.toArray());

    }

    public List<String> getDistinctActionsByTenant(String tenantId) {
        String sql = """
                   SELECT DISTINCT ewa.action 
            FROM eg_wf_action_v2  ewa
            left join eg_wf_state_v2 ewsv on ewa.currentstate = ewsv."uuid"
            left join eg_wf_businessservice_v2 ewbv on ewsv.businessserviceid = ewbv."uuid"
            WHERE ewa.tenantid = ? and ewbv.business = 'EBE'
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("action"), tenantId);
    }

    public Map<String, Long> getApplicationCounts(String tenantId, List<String> actions) {
        String sql = """
            WITH data AS (
                SELECT 
                    *, 
                    RANK() OVER (PARTITION BY businessid ORDER BY createdtime DESC) AS rnk
                FROM eg_wf_processinstance_v2 
                WHERE businessservice='PE-Service' AND tenantid=?
            )
            SELECT COUNT(*), action 
            FROM data 
            WHERE rnk = 1
            GROUP BY action 
            HAVING action IN (%s)
            """;

        String inClause = String.join(",", actions.stream().map(a -> "?").toList());
        sql = String.format(sql, inClause);
        PreparedStatementSetter preparedStatementSetter = ps -> {
            ps.setString(1, tenantId);
            for (int i = 0; i < actions.size(); i++) {
                ps.setString(i + 2, actions.get(i)); 
            }
        };
        Map<String, Long> resultMap = new HashMap<>();
        log.info("Final Query: " + sql);
        jdbcTemplate.query(sql, preparedStatementSetter, (ResultSet rs) -> {
            String action = rs.getString("action");
            Long count = rs.getLong(1);
            resultMap.put(action, count);
        });
    
        return resultMap;
    }


    public void removeCe(UpdateCeListRequest request) {

        String sql = "update eg_ebinder_ce_list set cestatus = false where peenquiryid = ? and cecode = ?";
        String peEnquiryId = request.getEnqId(); 
        String ceCode = request.getEmpCode();  
        jdbcTemplate.update(sql, peEnquiryId, ceCode);
    }

}
