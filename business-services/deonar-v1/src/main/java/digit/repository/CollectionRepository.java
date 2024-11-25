package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import digit.repository.querybuilder.CollectionQueryBuilder;
import digit.repository.rowmapper.CollectionRowMapper;
import digit.web.models.collection.EntryFee;
import digit.web.models.collection.ParkingFee;
import digit.web.models.collection.SlaughterFee;
import digit.web.models.collection.StableFee;
import digit.web.models.collection.WashFee;
import digit.web.models.collection.WeighingFee;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@ToString
@Repository
@Slf4j
public class CollectionRepository {
   @Autowired
    private CollectionQueryBuilder queryBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<EntryFee>getEntryFee(CollectionSearchCriteria searchCriteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEntryFee(searchCriteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<EntryFee> entryfeerowMapper = new CollectionRowMapper<>(EntryFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
    }

    public List<StableFee> getStableFee(CollectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getStableFee(criteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<StableFee> entryfeerowMapper = new CollectionRowMapper<>(StableFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
    }

    public List<StableFee> getTradingCollectionFee(CollectionSearchCriteria criteria) {

        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getTradingFeeQuery(criteria, preparedStmtList);
        log.info("Final query: " + query);
        CollectionRowMapper<StableFee> entryfeerowMapper = new CollectionRowMapper<>(StableFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
      
    }

    public List<StableFee> getRemovalCollectionFee(CollectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getRemovalFeeQuery(criteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<StableFee> entryfeerowMapper = new CollectionRowMapper<>(StableFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
    }

    public List<ParkingFee> getParkingFee(CollectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getParkingFee(criteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<ParkingFee> entryfeerowMapper = new CollectionRowMapper<>(ParkingFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
    }

    public List<WashFee> getWashingFee(CollectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getWashingFee(criteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<WashFee> entryfeerowMapper = new CollectionRowMapper<>(WashFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
    }

    public List<SlaughterFee> getSlaughterFee(CollectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSlaughterFee(criteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<SlaughterFee> entryfeerowMapper = new CollectionRowMapper<>(SlaughterFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
    }

    public List<WeighingFee> getWeighingCollectionFee(CollectionSearchCriteria criteria) {

        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getWeighingFee(criteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<WeighingFee> entryfeerowMapper = new CollectionRowMapper<>(WeighingFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
      
    }

   

    
}
