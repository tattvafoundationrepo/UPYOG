package digit.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

import digit.repository.querybuilder.SecurityCheckQueryBuilder;
import digit.repository.rowmapper.SecurityCheckDetailRowMapper;
import digit.web.models.security.SecurityCheckDetails;
import digit.web.models.security.SecurityCheckCriteria;

@Slf4j
@Repository
public class SecurityCheckRepository {

    @Autowired
    private SecurityCheckQueryBuilder queryBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SecurityCheckDetailRowMapper rowMapper;

    /**
     * Retrieves security check details based on the given search criteria.
     * @param searchCriteria the criteria used to search for security check details
     * @return a list of security check details that match the search criteria
     */
    public List<SecurityCheckDetails> getSecurityCheckDetails(SecurityCheckCriteria searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSearchQuery(searchCriteria, preparedStmtList);
        log.info("Executing security check search with query: {}", query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }
}