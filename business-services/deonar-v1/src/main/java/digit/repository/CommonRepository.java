package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.CommonQueryBuilder;
import digit.repository.rowmapper.CommonRowMapper;
import digit.repository.rowmapper.SlaughterUnitShiftRowmapper;
import digit.web.models.SlaughterUnit;
import digit.web.models.SlaughterUnitRequest;
import digit.web.models.common.CommonDetails;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Repository
public class CommonRepository {

    @Autowired
    private CommonQueryBuilder queryBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommonRowMapper rowMapper;

    @Autowired
    private SlaughterUnitShiftRowmapper slaughterUnitShiftRowmapper;

    public List<CommonDetails>getCommonDetails(CommonSearchCriteria searchCriteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSchemeSearchQuery(searchCriteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    public List<CommonDetails>getStakeholder(CommonSearchCriteria searchCriteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getStakeHolderQuery(searchCriteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    public List<SlaughterUnit>getSlaughterUnitShifts(SlaughterUnitRequest request){
        CommonSearchCriteria criteria = new CommonSearchCriteria();
        criteria.setId(request.getSlaughterUnitId());
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSlaughterUnitShift( criteria,preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, slaughterUnitShiftRowmapper, preparedStmtList.toArray());
    }
}