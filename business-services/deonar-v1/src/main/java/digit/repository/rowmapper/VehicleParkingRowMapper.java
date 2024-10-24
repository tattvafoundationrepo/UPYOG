package digit.repository.rowmapper;

import digit.constants.DeonarConstant;
import digit.web.models.security.vehicleparking.VehicleParkedCheckDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class VehicleParkingRowMapper implements ResultSetExtractor<List<VehicleParkedCheckDetails>> {


    @Override
    public List<VehicleParkedCheckDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, VehicleParkedCheckDetails> map = new LinkedHashMap<>();

        while (rs.next()) {
            String vehicleNumber = rs.getString(DeonarConstant.VEHICLE_NUMBER);
            if (vehicleNumber == null) continue;

            VehicleParkedCheckDetails details = map.get(vehicleNumber);
            if (details == null) {
                details = VehicleParkedCheckDetails.builder()
                        .vehicleType(rs.getLong(DeonarConstant.VEHICLE_TYPE))
                        .vehicleNumber(vehicleNumber)
                        .parkingDate((rs.getLong(DeonarConstant.PARKING_TIME) == 0) ? null : Instant.ofEpochMilli(rs.getLong(DeonarConstant.PARKING_TIME)).atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalDate())
                        .parkingTime((rs.getLong(DeonarConstant.PARKING_TIME) == 0) ? null : Instant.ofEpochMilli(rs.getLong(DeonarConstant.PARKING_TIME)).atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalTime())
                        .departureDate((rs.getLong(DeonarConstant.DEPARTURE_TIME) == 0) ? null : Instant.ofEpochMilli(rs.getLong(DeonarConstant.DEPARTURE_TIME)).atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalDate())
                        .departureTime((rs.getLong(DeonarConstant.DEPARTURE_TIME) == 0) ? null : Instant.ofEpochMilli(rs.getLong(DeonarConstant.DEPARTURE_TIME)).atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalTime())
                        .build();
                map.put(vehicleNumber, details);
            }
        }

        return new ArrayList<>(map.values());
    }



}