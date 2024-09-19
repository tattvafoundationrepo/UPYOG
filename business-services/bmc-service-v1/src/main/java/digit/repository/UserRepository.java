package digit.repository;


import java.util.ArrayList;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Repository;

import digit.bmc.model.VerificationDetails;
import digit.repository.querybuilder.SnapshotQueryBuilder;
import digit.repository.querybuilder.UserQueryBuilder;
import digit.repository.rowmapper.SnapshotRowmapper;
import digit.repository.rowmapper.UserDetailRowMapper;
import digit.web.models.SnapshotSearchcriteria;
import digit.web.models.user.UserDetails;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class UserRepository {

    @Autowired
    private UserQueryBuilder queryBuilder;

    @Autowired
    private SnapshotQueryBuilder snapshotQueryBuilder;

    @Autowired
    private SnapshotRowmapper snapshotRowmapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDetailRowMapper rowMapper;

    public List<UserDetails> getUserDetails(UserSearchCriteria searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getUserSearchQuery(searchCriteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    public Long getUserAddressMaxId() {
        String sql = "SELECT MAX(id) FROM eg_address";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public List<VerificationDetails> getSnapshotData(SnapshotSearchcriteria criteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = snapshotQueryBuilder.getApplicationSearchQuery(criteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, snapshotRowmapper, preparedStmtList.toArray());

    }
    

}
