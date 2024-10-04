package digit.repository;

import digit.repository.querybuilder.InspectionQueryBuilder;
import digit.repository.rowmapper.ArrivalDetailsRowMapper;
import digit.repository.rowmapper.InspectionRowMapper;
import digit.web.models.inspection.ArrivalDetailsResponse;
import digit.web.models.inspection.InspectionDetails;
import digit.web.models.security.SecurityCheckCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class InspectionRepository {

    @Autowired
    private InspectionQueryBuilder queryBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InspectionRowMapper rowMapper;
    @Autowired
    private ArrivalDetailsRowMapper arrivalDetailsRowMapper;

    public List<InspectionDetails> getInspectionDetails(String arrivalId) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSearchQuery(arrivalId, preparedStmtList);
        log.info("Executing security check search with query: {}", query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
    }

    public ArrivalDetailsResponse getArrivalDetails(String arrivalId){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSearchBaseQuery(arrivalId, preparedStmtList);
        log.info("Executing Arrival  search with query: {}", query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), arrivalDetailsRowMapper);
    }
}
