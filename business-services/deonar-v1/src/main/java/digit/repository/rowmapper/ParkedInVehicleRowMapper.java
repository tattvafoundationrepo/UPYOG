package digit.repository.rowmapper;

import digit.constants.DeonarConstant;
import digit.web.models.security.vehicleparking.VehicleParkedCheckDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ParkedInVehicleRowMapper implements ResultSetExtractor<List<VehicleParkedCheckDetails>> {


    @Override
    public List<VehicleParkedCheckDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<VehicleParkedCheckDetails> parkedVehicles = new ArrayList<>();

        while (rs.next()) {
            long parkingTimeMillis = rs.getLong(DeonarConstant.PARKING_TIME);
            if (parkingTimeMillis == 0) continue;
            VehicleParkedCheckDetails details = VehicleParkedCheckDetails.builder()
                    .vehicleType(rs.getLong(DeonarConstant.VEHICLE_TYPE))
                    .vehicleNumber(rs.getString(DeonarConstant.VEHICLE_NUMBER))
                    .parkingDate(Instant.ofEpochMilli(parkingTimeMillis).atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalDate())
                    .parkingTime(Instant.ofEpochMilli(parkingTimeMillis).atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalTime())
                    .departureDate((rs.getLong(DeonarConstant.DEPARTURE_TIME) == 0) ? null : Instant.ofEpochMilli(rs.getLong(DeonarConstant.DEPARTURE_TIME)).atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalDate())
                    .departureTime((rs.getLong(DeonarConstant.DEPARTURE_TIME) == 0) ? null : Instant.ofEpochMilli(rs.getLong(DeonarConstant.DEPARTURE_TIME)).atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalTime())
                    .build();

            parkedVehicles.add(details);
        }

        return parkedVehicles;
    }
}
