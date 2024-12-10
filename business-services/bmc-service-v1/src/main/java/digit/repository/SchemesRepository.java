package digit.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.SchemeDetailQueryBuilder;
import digit.repository.rowmapper.SchemeRowMapper;
import digit.repository.rowmapper.SchemeWiseApplicationCountRowmapper;
import digit.web.models.scheme.EventDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SchemesRepository {

    @Autowired
    private SchemeDetailQueryBuilder queryBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SchemeRowMapper rowMapper;

    @Autowired
    private SchemeWiseApplicationCountRowmapper countRowmapper;

    public List<EventDetails> getSchemeDetails(SchemeSearchCriteria searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSchemeSearchQuery(searchCriteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    public List<EventDetails> getSchemeWiseCounts(SchemeSearchCriteria searchCriteria) {
  
        if (searchCriteria.getUuid() == null) {
            return new ArrayList<>();
        }
    
        List<String> actions = searchCriteria.getAction();
        if (actions == null || actions.isEmpty()) {
            return new ArrayList<>();
        }
    
        String sql;
        Object[] params;
    
        if(actions.contains("APPLY")) {
            sql = "SELECT * FROM schemecounts_for_verify WHERE uuid = ?";
            params = new Object[]{searchCriteria.getUuid()};
        } else if (actions.contains("RANDOMIZE")) {
            sql = "SELECT * FROM schemecounts_for_randomize";
            params = new Object[]{}; 
        } else {
            sql = "SELECT * FROM schemecounts_for_approve";
            params = new Object[]{}; 
        }
        log.info("Final query: " + sql);
        return jdbcTemplate.query(sql, countRowmapper, params);
    }
    

}
