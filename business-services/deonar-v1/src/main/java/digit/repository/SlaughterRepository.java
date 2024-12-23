package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.SlaughterQueryBuilder;
import digit.repository.rowmapper.SlaughterListRowMapper;
import digit.web.SlaughterSearchCriteria;
import digit.web.models.SlaughterList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SlaughterRepository {

    
     @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    SlaughterListRowMapper slaughterListRowMapper;

    @Autowired
    SlaughterQueryBuilder queryBuilder;

     public List<SlaughterList>getSlaughterListDetails(SlaughterSearchCriteria request){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSlaughterListQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, slaughterListRowMapper, preparedStmtList.toArray());
    }


}
