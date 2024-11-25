package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.SecurityCheckQueryBuilder;
import digit.repository.querybuilder.ShopkeeperQueryBuilder;
import digit.repository.rowmapper.RemovalListRowmapper;
import digit.repository.rowmapper.SecurityCheckDetailRowMapper;
import digit.repository.rowmapper.ShopkeeperRowMapper;
import digit.repository.rowmapper.SlaughterListRowMapper;
import digit.web.models.GetListRequest;
import digit.web.models.RemovalList;
import digit.web.models.SlaughterList;
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


    @Autowired
    RemovalListRowmapper removalListRowmapper;


    public List<ShopkeeperDetails>getShopKeeperDetails(ShopkeeperRequest request){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = shopkeeperQueryBuilder.getShopKeeperQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, shopkeeperRowMapper, preparedStmtList.toArray());
    }

    public List<SlaughterList>getSlaughterListDetails(ShopkeeperRequest request){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = securityCheckQueryBuilder.getSlaughterListQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, slaughterListRowMapper, preparedStmtList.toArray());
    }

    public List<SecurityCheckDetails> getTradingListDetails(GetListRequest request) {
  
        List<Object> preparedStmtList = new ArrayList<>();
        String query = securityCheckQueryBuilder.getTradingListQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, securityCheckDetailRowMapper, preparedStmtList.toArray());
    }

    public List<SecurityCheckDetails> getStablingListDetails(GetListRequest request) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = securityCheckQueryBuilder.getStablingListQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, securityCheckDetailRowMapper, preparedStmtList.toArray());
    }

    public List<SecurityCheckDetails> getListForDawanwalaAssingnmet(GetListRequest request) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = securityCheckQueryBuilder.getListForDawanwalaQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, securityCheckDetailRowMapper, preparedStmtList.toArray());
    }

    public List<SecurityCheckDetails> getListForHelkariAssingnmet(GetListRequest request) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = securityCheckQueryBuilder.getListForHelkariQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, securityCheckDetailRowMapper, preparedStmtList.toArray());
    }

    public List<RemovalList> getRemovalListDetails(GetListRequest request) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = securityCheckQueryBuilder.getRemovalListQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, removalListRowmapper, preparedStmtList.toArray());
    }

    public List<SlaughterList> getWeighingListDetails(ShopkeeperRequest request) {
 
        List<Object> preparedStmtList = new ArrayList<>();
        String query = securityCheckQueryBuilder.getWeighingListQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, slaughterListRowMapper, preparedStmtList.toArray());
    }

}
