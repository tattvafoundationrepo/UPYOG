package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.SecurityCheckQueryBuilder;
import digit.repository.querybuilder.ShopkeeperQueryBuilder;
import digit.repository.rowmapper.SecurityCheckDetailRowMapper;
import digit.repository.rowmapper.ShopkeeperRowMapper;
import digit.repository.rowmapper.SlaughterListRowMapper;
import digit.web.models.security.SecurityCheckDetails;
import digit.web.models.shopkeeper.ShopkeeperDetails;
import digit.web.models.shopkeeper.ShopkeeperRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class StakeholderRepository {

    @Autowired
    ShopkeeperRowMapper shopkeeperRowMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    ShopkeeperQueryBuilder shopkeeperQueryBuilder;

    @Autowired
    SlaughterListRowMapper slaughterListRowMapper;


    @Autowired
    SecurityCheckQueryBuilder securityCheckQueryBuilder;

    @Autowired
    SecurityCheckDetailRowMapper securityCheckDetailRowMapper;


    public List<ShopkeeperDetails>getShopKeeperDetails(ShopkeeperRequest request){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = shopkeeperQueryBuilder.getShopKeeperQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, shopkeeperRowMapper, preparedStmtList.toArray());
    }

    public List<ShopkeeperDetails>getSlaughterListDetails(ShopkeeperRequest request){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = " select * from eg_deonar_vslaughterlist ";
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, slaughterListRowMapper, preparedStmtList.toArray());
    }

    public List<SecurityCheckDetails> getTradingListDetails(RequestInfoWrapper request) {
  
        List<Object> preparedStmtList = new ArrayList<>();
        String query = securityCheckQueryBuilder.getTradingListQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, securityCheckDetailRowMapper, preparedStmtList.toArray());
    }

    public List<SecurityCheckDetails> getStablingListDetails(RequestInfoWrapper request) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = securityCheckQueryBuilder.getStablingListQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, securityCheckDetailRowMapper, preparedStmtList.toArray());
    }

}
