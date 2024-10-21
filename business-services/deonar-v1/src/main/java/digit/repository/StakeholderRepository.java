package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.ShopkeeperQueryBuilder;
import digit.repository.rowmapper.ShopkeeperRowMapper;
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


    public List<ShopkeeperDetails>getShopKeeperDetails(ShopkeeperRequest request){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = shopkeeperQueryBuilder.getShopKeeperQuery(request, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, shopkeeperRowMapper, preparedStmtList.toArray());
    }


}
