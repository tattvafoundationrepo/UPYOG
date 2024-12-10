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
import digit.web.models.user.RemoveRequest;
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

    public List<VerificationDetails> getSnapshotData(SnapshotSearchcriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = snapshotQueryBuilder.getApplicationSearchQuery(criteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, snapshotRowmapper, preparedStmtList.toArray());

    }

    public Integer removeUserDocument(RemoveRequest userRequest) {
        Long modifiedOn = System.currentTimeMillis();;
        String modifiedBy = userRequest.getRequestInfo().getUserInfo().getName();
        Long userId = userRequest.getRequestInfo().getUserInfo().getId();
        String query = """
                 UPDATE eg_bmc_userdocument SET
                available=false, modifiedon=?, modifiedby=? WHERE userid = ?  AND documentid = ? 
                                """;
     return jdbcTemplate.update(query, modifiedOn, modifiedBy, userId, userRequest.getRemovalcriteria().getId());               

    }

    public Integer removeUserQualification(RemoveRequest userRequest) {
        Long modifiedOn = System.currentTimeMillis();;
        String modifiedBy = userRequest.getRequestInfo().getUserInfo().getName();
        Long userId = userRequest.getRequestInfo().getUserInfo().getId();
        String query = """
                 UPDATE eg_bmc_userqualification SET
                available=false, modifiedon=?, modifiedby=? WHERE "userID" = ?  AND "qualificationID" = ? 
                                """;
     return jdbcTemplate.update(query, modifiedOn, modifiedBy, userId, userRequest.getRemovalcriteria().getId());    
    }

    public Integer removeUserBank(RemoveRequest userRequest) {
        Long modifiedOn = System.currentTimeMillis();;
        String modifiedBy = userRequest.getRequestInfo().getUserInfo().getName();
        Long userId = userRequest.getRequestInfo().getUserInfo().getId();
        String query = """
                 UPDATE eg_bmc_userbank SET
                isactive=false, modifiedon=?, modifiedby=? WHERE userid = ?  AND accountnumber = ? 
                                """;
     return jdbcTemplate.update(query, modifiedOn, modifiedBy, userId, userRequest.getRemovalcriteria().getId().toString());    
    }

}
