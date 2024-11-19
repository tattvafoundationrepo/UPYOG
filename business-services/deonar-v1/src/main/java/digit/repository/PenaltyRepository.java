package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.rowmapper.PenaltyRowmapper;
import digit.web.models.penalty.PenaltyTypeDetails;
import digit.web.models.penalty.RaisedPenalties;
import lombok.extern.slf4j.Slf4j;
import digit.repository.rowmapper.RaisedPenaltyListRowmapper;

@Repository
@Slf4j
public class PenaltyRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PenaltyRowmapper rowmapper;

    @Autowired
    RaisedPenaltyListRowmapper raisedPenaltyListRowmapper;

    public List<PenaltyTypeDetails> getAllPenaltyDetails() {

        List<Object> preparedStmtList = new ArrayList<>();
        String query = """
                   SELECT  id,
                      CONCAT(Category, ' - ', PenaltyType) AS CategoryPenaltyType,
                      Unit, FeeAmount
                   FROM
                   eg_deonar_penalties;

                """;
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, rowmapper, preparedStmtList.toArray());
    }

    public List<RaisedPenalties> getRaisedPenaltyList() {
        List<Object> preparedStmtList = new ArrayList<>();
        String query  = " select * from  eg_deonar_vlistforpenaltyfeecollection ";
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, raisedPenaltyListRowmapper, preparedStmtList.toArray());
    }

}
