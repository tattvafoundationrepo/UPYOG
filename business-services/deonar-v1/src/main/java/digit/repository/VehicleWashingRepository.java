package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.VehicleWashingQueryBuilder;
import digit.repository.rowmapper.VehicleWashingRowMapper;
import digit.repository.rowmapper.WashedInVehicleRowMapper;
import digit.web.models.security.vehiclewashing.VehicleWashCheckCriteria;
import digit.web.models.security.vehiclewashing.VehicleWashCheckDetails;
import digit.web.models.security.vehiclewashing.VehicleWashCheckCriteria;
import digit.web.models.security.vehiclewashing.VehicleWashCheckDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class VehicleWashingRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private VehicleWashingRowMapper rowMapper;

    @Autowired
    private VehicleWashingQueryBuilder queryBuilder;

    @Autowired
    private WashedInVehicleRowMapper washedInVehicleRowMapper;

    public List<VehicleWashCheckDetails> getVehicleWashedDetails(VehicleWashCheckCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        List<VehicleWashCheckDetails> VehicleWashCheckDetails;
        try {
            String query = queryBuilder.getVehicleWashingSearchQuery(criteria, preparedStmtList);
            log.info("Executing washed In vehicle check with query: {}", query);
            VehicleWashCheckDetails = jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
        } catch (Exception e) {
            log.error(
                    "Exception occurred while trying to call database to get the washed In vehicle: " + e.getMessage());
            throw e;
        }
        return VehicleWashCheckDetails;
    }

    public List<VehicleWashCheckDetails> getWashedInVehicleDetails() {
        List<Object> preparedStmtList = new ArrayList<>();
        List<VehicleWashCheckDetails> VehicleWashCheckDetails;
        try {
            String query = queryBuilder.getWashedInVehicleSearchQuery(preparedStmtList);
            log.info("Executing washed vehicle check with query: {}", query);
            VehicleWashCheckDetails = jdbcTemplate.query(query, washedInVehicleRowMapper, preparedStmtList.toArray());
        } catch (Exception e) {
            log.error("Exception occurred while trying to call database to get the washed vehicle: " + e.getMessage());
            throw e;
        }
        return VehicleWashCheckDetails;
    }

    public Long getWashedInTime(long vehicleType, String vehicleNumber) {
        String query = " select washingtime from eg_deonar_vehicle_washing where vehicletype = ? and vehiclenumber = ? and departuretime is null ";
        log.info("Final query: " + query);
        return jdbcTemplate.queryForObject(query, Long.class, vehicleType, vehicleNumber);
    }
}
