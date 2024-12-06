package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.GatePassQueryBuilder;
import digit.repository.rowmapper.GatePassRowmapper;
import digit.web.models.GatePassDetails;
import digit.web.models.GatePassMapper;
import digit.web.models.GatePassSearchCriteria;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class GatePassRepository {


    @Autowired
    GatePassQueryBuilder builder;

    @Autowired 
    GatePassRowmapper mapper;

     @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<GatePassMapper> getGatePassSlaughterInfo(GatePassSearchCriteria criteria) {

        List<Object> preparedStmtList = new ArrayList<>();
        String query = builder.getGatePassQuery(criteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, mapper, preparedStmtList.toArray());

    }

}
