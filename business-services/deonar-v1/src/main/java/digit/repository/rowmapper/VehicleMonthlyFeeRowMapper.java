package digit.repository.rowmapper;


import digit.constants.DeonarConstant;
import digit.web.models.security.vehicleparking.VehicleParkedCheckDetails;
import digit.web.models.security.vehicleparking.VehicleParkingFeeResponse;
import digit.web.models.security.vehicleparking.VehicleParkingFeeResponseDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static digit.constants.DeonarConstant.*;

@Slf4j
@Component
public class VehicleMonthlyFeeRowMapper implements ResultSetExtractor<List<VehicleParkingFeeResponseDetails>> {


    @Override
    public List<VehicleParkingFeeResponseDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, VehicleParkingFeeResponseDetails> map = new LinkedHashMap<>();

        while (rs.next()) {
            String vehicleNumber = rs.getString(DeonarConstant.VEHICLE_NUMBER);
            if (vehicleNumber == null) continue;

            VehicleParkingFeeResponseDetails details = map.get(vehicleNumber);
            if (details == null) {
                details = VehicleParkingFeeResponseDetails.builder()
                        .vehicleType(rs.getString(DeonarConstant.VEHICLE_TYPE))
                        .vehicleNumber(vehicleNumber)
                        .parkingFee(rs.getDouble(MONTHLY_FEE))
                        .startDate((rs.getLong(START_DATE) == 0) ? null : Instant.ofEpochMilli(rs.getLong(START_DATE)).atZone(ZoneId.of(ASIA_KOLKATA)).toLocalDate())
                        .endDate((rs.getLong(END_DATE) == 0) ? null : Instant.ofEpochMilli(rs.getLong(END_DATE)).atZone(ZoneId.of(ASIA_KOLKATA)).toLocalDate())
                        .build();
                map.put(vehicleNumber, details);
            }
        }

        return new ArrayList<>(map.values());
    }

}
