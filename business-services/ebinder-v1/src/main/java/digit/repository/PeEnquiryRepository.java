package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.PeEnquiryQueryBuilder;
import digit.repository.rowmapper.PeEnquiryRowMapper;
import digit.web.models.GetPeRequest;
import digit.web.models.PeEnquiryResponse;
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

}
