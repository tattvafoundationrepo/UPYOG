package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.constants.DeonarConstant;
import digit.web.models.security.vehiclewashing.VehicleWashCheckDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class VehicleWashingRowMapper implements ResultSetExtractor<List<VehicleWashCheckDetails>> {

    @Override
    public List<VehicleWashCheckDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, VehicleWashCheckDetails> map = new LinkedHashMap<>();

        while (rs.next()) {
            String vehicleNumber = rs.getString(DeonarConstant.VEHICLE_NUMBER);
            if (vehicleNumber == null)
                continue;

            VehicleWashCheckDetails details = map.get(vehicleNumber);
            if (details == null) {
                details = VehicleWashCheckDetails.builder()
                        .vehicleType(rs.getString("vehicletypename"))
                        .vehicleId(rs.getLong(DeonarConstant.VEHICLE_TYPE))
                        .vehicleNumber(vehicleNumber)
                        .washingDate((rs.getLong(DeonarConstant.WASHING_TIME) == 0) ? null
                                : Instant.ofEpochMilli(rs.getLong(DeonarConstant.WASHING_TIME))
                                        .atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalDate())
                        .washingTime((rs.getLong(DeonarConstant.WASHING_TIME) == 0) ? null
                                : Instant.ofEpochMilli(rs.getLong(DeonarConstant.WASHING_TIME))
                                        .atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalTime())
                        .departureDate((rs.getLong(DeonarConstant.DEPARTURE_TIME) == 0) ? null
                                : Instant.ofEpochMilli(rs.getLong(DeonarConstant.DEPARTURE_TIME))
                                        .atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalDate())
                        .departureTime((rs.getLong(DeonarConstant.DEPARTURE_TIME) == 0) ? null
                                : Instant.ofEpochMilli(rs.getLong(DeonarConstant.DEPARTURE_TIME))
                                        .atZone(ZoneId.of(DeonarConstant.ASIA_KOLKATA)).toLocalTime())
                        .build();
                map.put(vehicleNumber, details);
            }
        }

        return new ArrayList<>(map.values());
    }

}