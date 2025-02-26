package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.CollectionQueryBuilder;
import digit.repository.rowmapper.CollectionRowMapper;
import digit.web.models.collection.ParkingFee;
import digit.web.models.collection.RemovalFee;
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

    public List<StableFee>getEntryFee(CollectionSearchCriteria searchCriteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEntryFee(searchCriteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<StableFee> entryfeerowMapper = new CollectionRowMapper<>(StableFee.class);
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

    public List<RemovalFee> getRemovalCollectionFee(CollectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getRemovalFeeQuery(criteria, preparedStmtList);
        log.info("Final query: " + query);
        CollectionRowMapper<RemovalFee> entryfeerowMapper = new CollectionRowMapper<>(RemovalFee.class);
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

    public List<StableFee> getSlaughterFee(CollectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSlaughterFeeQuery(criteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<StableFee> entryfeerowMapper = new CollectionRowMapper<>(StableFee.class);
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

    public void saveVehicleParking(long time, String vehiclenumber){
        String query = queryBuilder.saveVehicleParkDeparture(time, vehiclenumber);
        jdbcTemplate.execute(query);
    }

    public void saveVehicleWashing(long time, String vehiclenumber){
        String query = queryBuilder.saveVehicleWashDeparture(time, vehiclenumber);
        jdbcTemplate.execute(query);
    }

    public void saveEntryFee(String token, String animaltypeid, String arrivalid){
        String query = queryBuilder.saveEntryFee(token, animaltypeid, arrivalid);
        jdbcTemplate.execute(query);
    }

    public String getAnimalDetails(CollectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getAnimalFeeDetailsWithDays(criteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.queryForObject(query, String.class, preparedStmtList.toArray());
      
    }

    public void updateSlaughterPaymentFlag(String uuid) {
       String sql = " UPDATE eg_deonar_booked_for_slaughter SET flag=true WHERE ddreference = ? ";
       log.info("Final query: " + sql);
       jdbcTemplate.update(sql, uuid);
    }

    
}
