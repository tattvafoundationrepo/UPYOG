package digit.repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import digit.bmc.model.SchemeCriteria;
import digit.bmc.model.UserSchemeApplication;
import digit.bmc.model.VerificationDetails;
import digit.repository.querybuilder.EmployeeGetApplicationQueryBuilder;
import digit.repository.querybuilder.SchemeApplicationQueryBuilder;
import digit.repository.querybuilder.SchemeBenificiaryBuilder;
import digit.repository.querybuilder.VerifierQueryBuilder;
import digit.repository.rowmapper.SchemeApplicationRowMapper;
import digit.repository.rowmapper.SchemeApplicationStatusRowMapper;
import digit.repository.rowmapper.SchemeBeneficiaryRowMapper;
import digit.repository.rowmapper.UserSchemeApplicationRowMapper;
import digit.repository.rowmapper.VerificationDetailsRowMapper;
import digit.web.models.SchemeApplication;
import digit.web.models.SchemeApplicationSearchCriteria;
import digit.web.models.SchemeApplicationStatus;
import digit.web.models.SchemeBeneficiaryDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SchemeApplicationRepository {

    @Autowired
    private  SchemeApplicationQueryBuilder queryBuilder;
    @Autowired
    private  JdbcTemplate jdbcTemplate;
    @Autowired
    private  SchemeApplicationRowMapper rowMapper;
    @Autowired
    private SchemeBeneficiaryRowMapper schemeBeneficiaryRowMapper;
    @Autowired
    private SchemeBenificiaryBuilder schemeBenificiaryBuilder;
    
    @Autowired
    private VerifierQueryBuilder verifierQueryBuilder;
    
    @Autowired
    private VerificationDetailsRowMapper verificationDetailsRowMapper;
    
    @Autowired
    private EmployeeGetApplicationQueryBuilder builder;
    
    @Autowired
    private SchemeApplicationStatusRowMapper schemeApplicationStatusRowMapper;
   

    // // Constructor-based dependency injection
    // @Inject
    // public SchemeApplicationRepository(SchemeApplicationQueryBuilder queryBuilder, JdbcTemplate jdbcTemplate, SchemeApplicationRowMapper rowMapper) {
    //     this.queryBuilder = queryBuilder;
    //     this.jdbcTemplate = jdbcTemplate;
    //     this.rowMapper = rowMapper;
    // }

    /**
     * Retrieves a list of SchemeApplication objects based on the given search criteria.
     *
     * @param searchCriteria The criteria to filter the SchemeApplications.
     * @return A list of SchemeApplication objects.
     */
    public List<SchemeApplication> getApplications(SchemeApplicationSearchCriteria searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSchemeApplicationSearchQuery(searchCriteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    public List<SchemeBeneficiaryDetails> initialEligibilityCheck(SchemeBeneficiarySearchCritaria searchCriteria){

        List<Object> preparedStmtList = new ArrayList<>();
        String query = schemeBenificiaryBuilder.getSchemeDetailsSearchQuery(searchCriteria, preparedStmtList);
        log.info("Final query : "+query);
        return jdbcTemplate.query(query, schemeBeneficiaryRowMapper, preparedStmtList.toArray());

    }
    
    public List<SchemeCriteria> getCriteriaBySchemeIdAndType(Long schemeId) {
        String sql = "SELECT ct.criteriatype, c.criteriavalue, cc.criteriacondition " +
                     "FROM eg_bmc_criteria c " +
                     "LEFT JOIN eg_bmc_scheme_criteria sc ON c.id = sc.criteriaid " +
                     "LEFT JOIN eg_bmc_criteriatype ct on c.criteriatype = ct.id " +
                     "LEFT JOIN eg_bmc_criteriacondition cc on c.criteriacondition = cc.id " +
                     "LEFT JOIN eg_bmc_schemes s on sc.schemeid = s.id " +
                     "WHERE s.id = ?";

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SchemeCriteria.class), schemeId);
    }

    public String getSchemeById(Long schemeId) {
       String sql = "SELECT s.name from eg_bmc_schemes s where s.id = ?";
       return jdbcTemplate.queryForObject(sql,String.class,schemeId);

    }


    public List<VerificationDetails> getApplicationForVerification(SchemeApplicationSearchCriteria searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = builder.getQueryBasedOnAction(preparedStmtList,searchCriteria);
        log.info("Final Query: " + query);
        return jdbcTemplate.query(query, verificationDetailsRowMapper,preparedStmtList.toArray());
    }

    public List<String> getPreviousStatesByActionAndTenant(String action, String tenantId) {
        String sql = """
            SELECT ewsv.state 
            FROM public.eg_wf_state_v2 ewsv 
            LEFT JOIN public.eg_wf_action_v2 ewav 
            ON ewsv."uuid" = ewav.currentstate 
            WHERE ewav."action" = ? AND ewav.tenantid = ?
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("state"), action, tenantId);
    }

     public Map<String, UserSchemeApplication> getApplicationsByApplicationNumbers(List<String> applicationNumbers) {
        String sql = """
            SELECT * 
            FROM public.eg_bmc_userschemeapplication 
            WHERE applicationnumber IN (%s)
        """;
        String placeholders = applicationNumbers.stream()
                .map(num -> "?")
                .collect(Collectors.joining(", "));
        sql = String.format(sql, placeholders);
        log.info("Final Query: " + sql);
        return jdbcTemplate.query(sql, new UserSchemeApplicationRowMapper(), applicationNumbers.toArray());
    }  
    public List<String> getDistinctActionsByTenant(String tenantId) {
        String sql = """
                   SELECT DISTINCT ewa.action 
            FROM eg_wf_action_v2  ewa
            left join eg_wf_state_v2 ewsv on ewa.currentstate = ewsv."uuid"
            left join eg_wf_businessservice_v2 ewbv on ewsv.businessserviceid = ewbv."uuid"
            WHERE ewa.tenantid = ? and ewbv.business = 'BMC'
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("action"), tenantId);
    }

    public List<SchemeApplicationStatus> getSchemeApplicationByUserIdAndTenantId(Long userid, String tenantid) {

        String sql = """
                            with data as(
                      select ewpv.businessid,ewpv.lastmodifiedtime,ewsv.state,ewpv.comment,
                      RANK() over (PARTITION BY ewpv.businessid ORDER BY ewpv.createdtime DESC) as rank1
                      from eg_wf_processinstance_v2 ewpv
                      left join eg_wf_state_v2 ewsv on ewpv.status = ewsv.uuid
                      )
                select ebu.applicationnumber , ebs."name", ebc.coursename , ebm."name" as machine ,b.state as currentStatus ,b.lastmodifiedtime,b.comment
                from data b
                              inner JOIN eg_bmc_userschemeapplication ebu ON b.businessid = ebu.applicationnumber
                              left join eg_bmc_usersubschememapping ebu2 on ebu2.applicationnumber = ebu.applicationnumber
                              left join eg_bmc_schemes ebs on ebu.optedid = ebs.id
                              left join eg_bmc_courses ebc on ebu2.courseid = ebc.id
                              left join eg_bmc_machines ebm on ebu2.machineid = ebm.id
                              where ebu.userid =? and ebu.tenantid = ? and b.rank1=1
                                    """;
        log.info("Final Query: " + sql);
        return jdbcTemplate.query(sql, schemeApplicationStatusRowMapper, userid, tenantid);
    }

    public Map<String, Long> getApplicationCountss(String tenantId, List<String> actions) {
        String sql = """
            WITH data AS (
                SELECT 
                    *, 
                    RANK() OVER (PARTITION BY businessid ORDER BY createdtime DESC) AS rnk
                FROM eg_wf_processinstance_v2 
                WHERE businessservice='bmc-services' AND tenantid=?
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

}