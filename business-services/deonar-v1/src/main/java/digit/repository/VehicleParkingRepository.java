package digit.repository;

import static digit.constants.DeonarConstant.LORRY_TRUCK_TEMPO_CAR_THREE_WHEELER_DAY_RATE;
import static digit.constants.DeonarConstant.THREE_WHEELER;
import static digit.constants.DeonarConstant.THREE_WHEELER_OVERNIGHT_AFTER_6AM;
import static digit.constants.DeonarConstant.THREE_WHEELER_OVERNIGHT_LESS_2H;
import static digit.constants.DeonarConstant.THREE_WHEELER_OVERNIGHT_MORE_2H;
import static digit.constants.DeonarConstant.TWO_WHEELER;
import static digit.constants.DeonarConstant.TWO_WHEELER_DAY_RATE;
import static digit.constants.DeonarConstant.TWO_WHEELER_OVERNIGHT_AFTER_6AM;
import static digit.constants.DeonarConstant.TWO_WHEELER_OVERNIGHT_LESS_2H;
import static digit.constants.DeonarConstant.TWO_WHEELER_OVERNIGHT_MORE_2H;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.VehicleParkingQueryBuilder;
import digit.repository.rowmapper.ParkedInVehicleRowMapper;
import digit.repository.rowmapper.VehicleMonthlyFeeRowMapper;
import digit.repository.rowmapper.VehicleParkingRowMapper;
import digit.web.models.security.vehicleparking.VehicleParkedCheckCriteria;
import digit.web.models.security.vehicleparking.VehicleParkedCheckDetails;
import digit.web.models.security.vehicleparking.VehicleParkingFeeResponseDetails;
import lombok.extern.slf4j.Slf4j;

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

    @Autowired
    private  VehicleMonthlyFeeRowMapper vehicleMonthlyFeeRowMapper;


    public List<VehicleParkedCheckDetails> getVehicleParkedDetails(VehicleParkedCheckCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        List<VehicleParkedCheckDetails> VehicleParkedCheckDetails;
        try {
            String query = queryBuilder.getVehicleParkingSearchQuery(criteria, preparedStmtList);
            log.info("Executing parked In vehicle check with query: {}", query);
            VehicleParkedCheckDetails =  jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
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
            VehicleParkedCheckDetails = jdbcTemplate.query(query, parkedInVehicleRowMapper, preparedStmtList.toArray());
        }catch (Exception e) {
            log.error("Exception occurred while trying to call database to get the parked vehicle: " + e.getMessage());
            throw  e;
        }
        return VehicleParkedCheckDetails;
    }

    public List<VehicleParkingFeeResponseDetails> getMonthlyVehicleParkedDetails(VehicleParkedCheckCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        List<VehicleParkingFeeResponseDetails> vehicleParkingFeeResponses;
        try {
            String query = queryBuilder.getMonthlyVehicleParkingSearchQuery(criteria, preparedStmtList);
            log.info("Executing monthly vehicle parking fee check with query: {}", query);
            vehicleParkingFeeResponses = jdbcTemplate.query(query, vehicleMonthlyFeeRowMapper, preparedStmtList.toArray());
        }catch (Exception e) {
            log.error("Exception occurred while trying to call database to get the monthly vehicle parking fee: " + e.getMessage());
            throw  e;
        }
        return vehicleParkingFeeResponses;
    }


    // Method to calculate the parking fee
    public static double calculateParkingFee(
            LocalTime parkedInTime, LocalTime parkedOutTime,
            LocalDate parkedIndDate, LocalDate parkedOutDate,
            String vehicleType) {

        double fee = 0.0;

        
        boolean isDayOnly = false;
        boolean isNightOnly = false;
        boolean isDayAndNight = false;

        // // Handle scenario 1: Same day parking (same date for both in and out times)
        // log.info("In Date : " + String.valueOf(parkedIndDate));
        // log.info("Out Date : " + String.valueOf(parkedOutDate));
        if (parkedIndDate.isEqual(parkedOutDate)) {
            // If vehicle is parked during the day (6 AM to 12 AM)
            // Condition for Day parking (6 AM to 12 AM)
            if (parkedInTime.isAfter(LocalTime.of(5, 59)) && parkedOutTime.isBefore(LocalTime.of(23, 59, 59, 999999999))) {
                isDayOnly = true;
            }


            // If vehicle is parked during the night (12 AM to 6 AM)
            if (parkedInTime.isAfter(LocalTime.of(23, 59, 59, 999999999)) && parkedOutTime.isBefore(LocalTime.of(6, 0, 0, 0))) {
                isNightOnly = true;
            }

        } else {
            // Handle scenario 2: Vehicle parked across multiple days (Day and Night)
            isDayAndNight = true;
        }

        // Calculate parking fee based on the conditions
        if (isDayOnly) {
            // Day-only charges
            fee = calculateDayCharges(vehicleType);
        } else if (isNightOnly) {
            // Night-only charges
            fee = calculateNightCharges(vehicleType, parkedIndDate, parkedInTime, parkedOutDate, parkedOutTime);
        } else if (isDayAndNight) {

            fee+=calculateDayCharges(vehicleType);
            // For vehicles parked for both day and night across multiple days
            if (parkedIndDate.isBefore(parkedOutDate)) {
                // Day charge for first day (6 AM to 12 AM)
                if(parkedOutTime.isAfter(LocalTime.of(6, 0))){
                    fee += calculateDayCharges(vehicleType);
                }
                fee += getDayCount(parkedInTime, parkedIndDate, parkedOutTime, parkedOutDate, false) * calculateDayCharges(vehicleType);
                
                                // If the vehicle is parked out between 12 AM - 6 AM, treat as night parking
                                if (parkedOutTime.isAfter(LocalTime.of(23, 59, 59, 999999999)) && parkedOutTime.isBefore(LocalTime.of(6, 0, 0, 0))) {
                                    fee += calculateNightCharges(vehicleType, parkedIndDate, parkedInTime, parkedOutDate, parkedOutTime);
                                } else {
                                    // Night charge for the last day (12 AM to 6 AM)
                                    fee += calculateNightCharges(vehicleType, parkedIndDate, parkedInTime, parkedOutDate, parkedOutTime);
                                }
                            }
                        }
                
                        return fee;
                    }
                
                    // Calculate day charges for Lorry/Truck/Tempo/Car/Three Wheeler (6 AM to 12 AM)
                    private static double calculateDayCharges(String vehicleType) {
                        switch (vehicleType) {
                            case THREE_WHEELER:
                                return LORRY_TRUCK_TEMPO_CAR_THREE_WHEELER_DAY_RATE; // Rs. 83
                            case TWO_WHEELER:
                                return TWO_WHEELER_DAY_RATE; // Rs. 33
                            default:
                                return 0;
                        }
                    }
                
                    private static double calculateNightCharges(String vehicleType, LocalDate parkedInDate, LocalTime parkedInTime, LocalDate parkedOutDate, LocalTime parkedOutTime) {
                
                        long hoursParked = 0;
                        long minutesParked = 0;
                        double overnightCharges = 0;
                
                        // If the vehicle is parked for more than one day, we need to consider both dates.
                        if (!parkedInDate.isEqual(parkedOutDate)) {
                            // Calculate the time difference for the entire period (spanning days)
                            hoursParked = ChronoUnit.HOURS.between(parkedInTime.atDate(parkedInDate), parkedOutTime.atDate(parkedOutDate));
                            minutesParked = ChronoUnit.MINUTES.between(parkedInTime.atDate(parkedInDate), parkedOutTime.atDate(parkedOutDate));
                        } else {
                            // If parked on the same day, calculate as usual
                            hoursParked = ChronoUnit.HOURS.between(parkedInTime, parkedOutTime);
                            minutesParked = ChronoUnit.MINUTES.between(parkedInTime, parkedOutTime);
                        }

                        
                
                        // Logic for determining overnight charges based on vehicle type and time parked
                        switch (vehicleType) {
                            case THREE_WHEELER:
                                // if(getDayCount(parkedInTime, parkedInDate, parkedOutTime, parkedOutDate, false) < getDayCount(parkedInTime, parkedInDate, parkedOutTime, parkedOutDate, true)){
                                //     overnightCharges += THREE_WHEELER_OVERNIGHT_MORE_2H;
                                // }
                                if (parkedOutTime.isAfter(LocalTime.of(0, 0)) && parkedOutTime.isBefore(LocalTime.of(2, 0))) {
                                    overnightCharges += THREE_WHEELER_OVERNIGHT_LESS_2H;
                                } 
                                if (hoursParked >= 2) {
                                    overnightCharges += getDayCount(parkedInTime, parkedInDate, parkedOutTime, parkedOutDate, true) * THREE_WHEELER_OVERNIGHT_MORE_2H;
                                } else {
                                    overnightCharges += getDayCount(parkedInTime, parkedInDate, parkedOutTime, parkedOutDate, true) * THREE_WHEELER_OVERNIGHT_AFTER_6AM;
                                }
                                break;
                
                            case TWO_WHEELER:
                                // if(getDayCount(parkedInTime, parkedInDate, parkedOutTime, parkedOutDate, false) < getDayCount(parkedInTime, parkedInDate, parkedOutTime, parkedOutDate, true)){
                                //     overnightCharges += TWO_WHEELER_OVERNIGHT_MORE_2H;
                                // }
                                if (parkedOutTime.isAfter(LocalTime.of(0, 0)) && parkedOutTime.isBefore(LocalTime.of(2, 0))) {
                                    overnightCharges += TWO_WHEELER_OVERNIGHT_LESS_2H;
                                } 
                                 if (hoursParked >= 2) {
                                    overnightCharges += getDayCount(parkedInTime, parkedInDate, parkedOutTime, parkedOutDate, true)  * TWO_WHEELER_OVERNIGHT_MORE_2H;
                                } else {
                                    overnightCharges += getDayCount(parkedInTime, parkedInDate, parkedOutTime, parkedOutDate, true) * TWO_WHEELER_OVERNIGHT_AFTER_6AM;
                                }
                                break;
                
                            default:
                                // For any other vehicle type, default to no charge
                                break;
                        }
                
                        return overnightCharges;
                    }
                
                     public Long getParkedInTime(long vehicleType,String vehicleNumber) {
                        String query = " select parkingtime from eg_deonar_vehicle_parking where vehicletype = ? and vehiclenumber = ? and departuretime is null ";
                        log.info("Final query: " + query);
                        return jdbcTemplate.queryForObject(query, Long.class, vehicleType,vehicleNumber );
                    }
                
    private static long getDayCount( LocalTime parkedInTime, LocalDate parkedIndDate,LocalTime parkedOutTime, LocalDate parkedOutDate, boolean flag){



        // Combine LocalTime and LocalDate to LocalDateTime
        LocalDateTime start = parkedIndDate.atTime(parkedInTime);
        LocalDateTime end = parkedOutDate.atTime(parkedOutTime);

        // Count exact day time (day and night periods)
        long[] dayAndNightCounts = countExactDayTime(start, end);

        // System.out.println("Days : " + dayAndNightCounts[0]);
        // System.out.println("Nights : " + dayAndNightCounts[1]);


        if(flag){
            return dayAndNightCounts[1];
        } else {
            return dayAndNightCounts[0];
        }
    }

    // Method to count exact day and night time between two timestamps
    private static long[] countExactDayTime(LocalDateTime start, LocalDateTime end) {

    
        long daysCount = ChronoUnit.DAYS.between(start, end); 
        long nightsCount = countNights(start, end);

        // Return day and night time in an array: [dayTime, nightTime]
        return new long[] {daysCount, nightsCount};
    }

     // Method to count nights (12:00 AM to 5:59 AM) between two LocalDateTime objects
     private static long countNights(LocalDateTime start, LocalDateTime end) {
        long nightCount = 0;

        // Loop through each day from start to end
        while (start.isBefore(end)) {
            // Define the night time period for the current day (12:00 AM to 5:59 AM)
            LocalDateTime nightStart = start.toLocalDate().atTime(LocalTime.MIDNIGHT); // 12:00 AM
            LocalDateTime nightEnd = start.toLocalDate().atTime(5, 59, 59, 999999999); // 5:59 AM

            // Check if the night period overlaps with the given time range
            if (start.isBefore(nightEnd) && end.isAfter(nightStart)) {
                // Calculate the actual overlap of the night period with the start and end time
                LocalDateTime actualNightStart = start.isBefore(nightStart) ? nightStart : start;
                LocalDateTime actualNightEnd = end.isAfter(nightEnd) ? nightEnd : end;
                
                // If there is an overlap, count this night as 1
                if (actualNightStart.isBefore(actualNightEnd)) {
                    nightCount++;
                }
            }

            // Move to the next day at 12:00 AM
            start = start.plusDays(1).toLocalDate().atTime(LocalTime.MIDNIGHT);
        }

        return nightCount;
    }

}