package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.constants.DeonarConstant;
import digit.web.models.security.vehiclewashing.VehicleWashCheckDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WashedInVehicleRowMapper implements ResultSetExtractor<List<VehicleWashCheckDetails>> {

        @Override
        public List<VehicleWashCheckDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<VehicleWashCheckDetails> washedVehicles = new ArrayList<>();

                while (rs.next()) {
                        long parkingTimeMillis = rs.getLong(DeonarConstant.WASHING_TIME);
                        if (parkingTimeMillis == 0)
                                continue;
                        VehicleWashCheckDetails details = VehicleWashCheckDetails.builder()
                                        .vehicleType(rs.getString("vehicletypename"))
                                        .vehicleId(rs.getLong(DeonarConstant.VEHICLE_TYPE))
                                        .vehicleNumber(rs.getString(DeonarConstant.VEHICLE_NUMBER))
                                        .washingDate(Instant.ofEpochMilli(parkingTimeMillis)
                                                        .atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA))
                                                        .toLocalDate())
                                        .washingTime(Instant.ofEpochMilli(parkingTimeMillis)
                                                        .atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA))
                                                        .toLocalTime())
                                        .departureDate((rs.getLong(DeonarConstant.DEPARTURE_TIME) == 0) ? null
                                                        : Instant.ofEpochMilli(
                                                                        rs.getLong(DeonarConstant.DEPARTURE_TIME))
                                                                        .atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA))
                                                                        .toLocalDate())
                                        .departureTime((rs.getLong(DeonarConstant.DEPARTURE_TIME) == 0) ? null
                                                        : Instant.ofEpochMilli(
                                                                        rs.getLong(DeonarConstant.DEPARTURE_TIME))
                                                                        .atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA))
                                                                        .toLocalTime())
                                        .build();

                        washedVehicles.add(details);
                }

                return washedVehicles;
        }
}