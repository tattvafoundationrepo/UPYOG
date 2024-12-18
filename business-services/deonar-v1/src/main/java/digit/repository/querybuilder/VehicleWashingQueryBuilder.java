package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.constants.DeonarConstant;
import digit.web.models.security.vehiclewashing.VehicleWashCheckCriteria;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class VehicleWashingQueryBuilder {

    public static String BASE_QUERY_VEHICLE_WASHING = """
                    SELECT edvw.vehicletype AS vehicletype,
                           edvw.vehiclenumber AS vehiclenumber,
                           edvw.createdby AS createdBy,
                           edvw.updatedby AS updatedBy,
                           edvw.washingtime AS washingtime,
                           edvw.departuretime AS departuretime,
                           vt.name AS vehicletypename
                    FROM public.eg_deonar_vehicle_washing edvw
                    left join eg_deonar_vehicle_type vt on edvw.vehicletype = vt.id
            """;

    public static String BASE_QUERY_VEHICLE_WASHED_IN = """
            SELECT edvw.vehicletype AS vehicleType,
                   edvw.vehiclenumber AS vehicleNumber,
                   edvw.createdby AS createdBy,
                   edvw.updatedby AS updatedBy,
                   edvw.washingtime AS washingTime,
                   edvw.departuretime AS departureTime,
                   vt.name AS vehicletypename
                  FROM public.eg_deonar_vehicle_washing edvw
                  left join eg_deonar_vehicle_type vt on edvw.vehicletype = vt.id
                   WHERE edvw.departuretime IS NULL
                   ORDER BY edvw.vehiclenumber
               """;
    // public static String BASE_QUERY_MONTHLY_VEHICLE_PARKING_FEE = """
    // SELECT v.vehicletype AS vehicleType,
    // v.vehiclenumber AS vehicleNumber,
    // v.startdate AS startDate,
    // v.enddate AS endDate,
    // v.monthlyfee AS monthlyFee
    // FROM public.eg_deonar_vehicleparking_monthly_fee v
    // """;

    public String getVehicleWashingSearchQuery(VehicleWashCheckCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY_VEHICLE_WASHING);

        if (criteria.getVehicleType() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" edvw.vehicletype = ? ");
            preparedStmtList.add(criteria.getVehicleType());
        }
        if (criteria.getVehicleNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" edvw.vehiclenumber = ? ");
            preparedStmtList.add(criteria.getVehicleNumber());
        }

        log.info("Prepared parameters: {}", preparedStmtList);
        query.append("order by edvw.vehiclenumber");
        return query.toString();
    }

    // public String getMonthlyVehicleParkingSearchQuery(VehicleParkedCheckCriteria
    // criteria,
    // List<Object> preparedStmtList) {
    // StringBuilder query = new
    // StringBuilder(BASE_QUERY_MONTHLY_VEHICLE_PARKING_FEE);

    // if (criteria.getVehicleType() != null) {
    // addClauseIfRequired(query, preparedStmtList);
    // query.append("v.vehicletype = ? ");
    // preparedStmtList.add(criteria.getVehicleType());
    // }
    // if (criteria.getVehicleNumber() != null) {
    // addClauseIfRequired(query, preparedStmtList);
    // query.append(" v.vehiclenumber = ? ");
    // preparedStmtList.add(criteria.getVehicleNumber());
    // }

    // log.info("Prepared parameters: {}", preparedStmtList);
    // query.append("order by v.vehicleType");
    // return query.toString();
    // }

    public String getWashedInVehicleSearchQuery(List<Object> preparedStmtList) {
        return BASE_QUERY_VEHICLE_WASHED_IN;
    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(DeonarConstant.WHERE);
        } else {
            query.append(DeonarConstant.AND);
        }
    }
}