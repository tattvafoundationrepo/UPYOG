package digit.repository.querybuilder;

import digit.constants.DeonarConstant;
import digit.web.models.security.vehicleParking.VehicleParkedCheckCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class VehicleParkingQueryBuilder {

    public static String BASE_QUERY_VEHICLE_PARKING = """
                    SELECT v.vehicletype AS vehicleType,
                              v.vehiclenumber AS vehicleNumber,
                              v.createdby AS createdBy,
                              v.updatedby AS updatedBy,
                              TO_TIMESTAMP(v.parkingdate / 1000)::date AS parkingDate,
                              TO_CHAR(TO_TIMESTAMP(v.parkingtime / 1000), 'HH24:MI:SS')::time AS parkingTime,
                              TO_TIMESTAMP(v.departuredate / 1000)::date AS departureDate,
                              TO_CHAR(TO_TIMESTAMP(v.departuretime / 1000), 'HH24:MI:SS')::time AS departureTime
                       FROM public.eg_deonar_vehicle_parking v
            """;

    public String getVehicleParkingSearchQuery(VehicleParkedCheckCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY_VEHICLE_PARKING);

        if (criteria.getVehicleType() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append("v.vehicletype = ? ");
            preparedStmtList.add(criteria.getVehicleType());
        }
        if (criteria.getVehicleNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" v.vehiclenumber = ? ");
            preparedStmtList.add(criteria.getVehicleNumber());
        }

        log.info("Prepared parameters: {}", preparedStmtList);
        query.append("order by v.vehicleType");
        return query.toString();
    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(DeonarConstant.WHERE);
        } else {
            query.append(DeonarConstant.AND);
        }
    }
}
