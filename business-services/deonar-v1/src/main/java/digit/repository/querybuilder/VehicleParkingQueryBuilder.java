package digit.repository.querybuilder;

import digit.constants.DeonarConstant;
import digit.web.models.security.vehicleparking.VehicleParkedCheckCriteria;
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
                           v.parkingtime AS parkingTime,
                           v.departuretime AS departureTime
                    FROM public.eg_deonar_vehicle_parking v
            """;

    public static String BASE_QUERY_VEHICLE_PARKED_IN = """
                 SELECT v.vehicletype AS vehicleType,
                        v.vehiclenumber AS vehicleNumber,
                        v.createdby AS createdBy,
                        v.updatedby AS updatedBy,
                        v.parkingtime AS parkingTime,
                        v.departuretime AS departureTime
                       FROM public.eg_deonar_vehicle_parking v
                        WHERE v.departuretime IS NULL
                        ORDER BY v.vehicleType
                    """;
    public static String BASE_QUERY_MONTHLY_VEHICLE_PARKING_FEE = """
                    SELECT v.vehicletype AS vehicleType,
                           v.vehiclenumber AS vehicleNumber,
                           v.startdate AS startDate,
                           v.enddate AS endDate,
                           v.monthlyfee AS monthlyFee
                    FROM public.eg_deonar_vehicleparking_monthly_fee v
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

    public String getMonthlyVehicleParkingSearchQuery(VehicleParkedCheckCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY_MONTHLY_VEHICLE_PARKING_FEE);

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

    public String getParkedInVehicleSearchQuery(List<Object> preparedStmtList) {
      return BASE_QUERY_VEHICLE_PARKED_IN;
    }
    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(DeonarConstant.WHERE);
        } else {
            query.append(DeonarConstant.AND);
        }
    }
}