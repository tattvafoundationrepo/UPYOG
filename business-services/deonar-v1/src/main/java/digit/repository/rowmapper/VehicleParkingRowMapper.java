package digit.repository.rowmapper;

import digit.constants.DeonarConstant;
import digit.web.models.security.vehicleParking.VehicleParkedCheckDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        try {
            while (rs.next()) {
                String vehicleNumber = rs.getString(DeonarConstant.VEHICLE_NUMBER);
                if (vehicleNumber == null) continue;
                VehicleParkedCheckDetails details = map.get(vehicleNumber);
                if (details == null) {
                    details = VehicleParkedCheckDetails.builder()
                            .vehicleType(rs.getString(DeonarConstant.VEHICLE_TYPE))
                            .vehicleNumber(vehicleNumber)
                            .parkingDate(rs.getDate(DeonarConstant.PARKING_DATE) != null ? rs.getDate(DeonarConstant.PARKING_DATE).toLocalDate() : null)
                            .parkingTime(rs.getTime(DeonarConstant.PARKING_TIME) != null ? rs.getTime(DeonarConstant.PARKING_TIME).toLocalTime() : null)
                            .departureDate(rs.getDate(DeonarConstant.DEPARTURE_DATE) != null ? rs.getDate(DeonarConstant.DEPARTURE_DATE).toLocalDate() : null)
                            .departureTime(rs.getTime(DeonarConstant.DEPARTURE_TIME) != null ? rs.getTime(DeonarConstant.DEPARTURE_TIME).toLocalTime() : null)
                            .build();
                    map.put(vehicleNumber, details);
                }
            }
        }catch (Exception e){
            log.error("Exception occurred while trying to map the response data from DataBase: " + e.getMessage());
        }
        return new ArrayList<>(map.values());
    }
}