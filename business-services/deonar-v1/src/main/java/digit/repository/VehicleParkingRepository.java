package digit.repository;

import digit.repository.querybuilder.VehicleParkingQueryBuilder;
import digit.repository.rowmapper.SecurityCheckDetailRowMapper;
import digit.repository.rowmapper.VehicleParkingRowMapper;
import digit.web.models.security.vehicleParking.VehicleParkedCheckCriteria;
import digit.web.models.security.vehicleParking.VehicleParkedCheckDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class VehicleParkingRepository {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private VehicleParkingRowMapper rowMapper;

    @Autowired
    private VehicleParkingQueryBuilder vehicleParkingQueryBuilder;


    public List<VehicleParkedCheckDetails> getVehicleParkedDetails(VehicleParkedCheckCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        List<VehicleParkedCheckDetails> VehicleParkedCheckDetails;
        try {
            String query = vehicleParkingQueryBuilder.getVehicleParkingSearchQuery(criteria, preparedStmtList);
            log.info("Executing parked vehicle check with query: {}", query);
            VehicleParkedCheckDetails = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        }catch (Exception e) {
            log.error("Exception occurred while trying to call database to get the parked vehicle: " + e.getMessage());
            throw  e;
        }
        return VehicleParkedCheckDetails;
    }
}
