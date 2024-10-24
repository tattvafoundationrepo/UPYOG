package digit.repository;

import digit.repository.querybuilder.VehicleParkingQueryBuilder;
import digit.repository.rowmapper.ParkedInVehicleRowMapper;
import digit.repository.rowmapper.VehicleParkingRowMapper;
import digit.web.models.security.vehicleparking.VehicleParkedCheckCriteria;
import digit.web.models.security.vehicleparking.VehicleParkedCheckDetails;
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
    private VehicleParkingQueryBuilder queryBuilder;

    @Autowired
    private ParkedInVehicleRowMapper parkedInVehicleRowMapper;


    public List<VehicleParkedCheckDetails> getVehicleParkedDetails(VehicleParkedCheckCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        List<VehicleParkedCheckDetails> VehicleParkedCheckDetails;
        try {
            String query = queryBuilder.getVehicleParkingSearchQuery(criteria, preparedStmtList);
            log.info("Executing parked In vehicle check with query: {}", query);
            VehicleParkedCheckDetails = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        }catch (Exception e) {
            log.error("Exception occurred while trying to call database to get the parked In vehicle: " + e.getMessage());
            throw  e;
        }
        return VehicleParkedCheckDetails;
    }


    public List<VehicleParkedCheckDetails> getParkedInVehicleDetails( ) {
        List<Object> preparedStmtList = new ArrayList<>();
        List<VehicleParkedCheckDetails> VehicleParkedCheckDetails;
        try {
            String query = queryBuilder.getParkedInVehicleSearchQuery(preparedStmtList);
            log.info("Executing parked vehicle check with query: {}", query);
            VehicleParkedCheckDetails = jdbcTemplate.query(query, preparedStmtList.toArray(), parkedInVehicleRowMapper);
        }catch (Exception e) {
            log.error("Exception occurred while trying to call database to get the parked vehicle: " + e.getMessage());
            throw  e;
        }
        return VehicleParkedCheckDetails;
    }

}
