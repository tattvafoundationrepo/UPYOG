package digit.service;

import digit.constants.DeonarConstant;
import digit.kafka.Producer;
import digit.repository.VehicleParkingRepository;
import digit.web.models.security.vehicleparking.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

import static digit.constants.DeonarConstant.*;

@Service
public class VehicleParkingService {


    @Autowired
    private Producer producer;

    @Autowired
    VehicleParkingRepository vehicleParkingRepository;


    public VehicleParkingRequest saveParkedVehicleDetails(VehicleParkingRequest request) {
        if (request == null) {
            throw new CustomException("INVALID_DATA", "Vehicle parking request data is null or incomplete.");
        }

        long currentTimeMillis = System.currentTimeMillis();
        VehicleParkingDetails vehicleParkingDetails = request.getVehicleParkingDetails();
        if (vehicleParkingDetails.isVehicleIn()) {
            vehicleParkingDetails.setParkingTime(currentTimeMillis);
            request.setCreatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
            request.setCreatedAt(currentTimeMillis);
        } else {
            vehicleParkingDetails.setDepartureTime(currentTimeMillis);
            request.setUpdatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
            request.setUpdatedAt(currentTimeMillis);
        }

        producer.push(DeonarConstant.SAVE_VEHICLE_PARKING, request);
        request.setVehicleParkingDetails(vehicleParkingDetails);
        return request;
    }


    public List<VehicleParkedCheckDetails> getVehicleDetails(VehicleParkedCheckCriteria criteria) {
        if (criteria == null) {
            throw new CustomException("INVALID_SEARCH_CRITERIA", "Search criteria is empty");
        }
        return vehicleParkingRepository.getVehicleParkedDetails(criteria);
    }

    public List<VehicleParkedCheckDetails> getParkedInVehicleDetails() {
        return vehicleParkingRepository.getParkedInVehicleDetails();
    }
    public VehicleParkingFeeRequest saveMonthlyParkingFee(VehicleParkingFeeRequest request) {
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeAfterOneMonth = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis), ZoneOffset.UTC)
                .plusMonths(1)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli();
        int vehicleType = request.getVehicleParkingFeeDetails().getVehicleType();
        String parkingType = request.getVehicleParkingFeeDetails().getParkingType();
        double parkingFee = request.getVehicleParkingFeeDetails().getMonthlyFee();
        switch (vehicleType) {
            case 12:
                switch (parkingType) {
                    case DAY_ONLY:
                        if(parkingFee != TWO_WHEELER_MONTHLY_DAY_CHARGES){
                            throw new CustomException("Monthly Parking fee for vehicle is not correct","Please provide the valid parking fee");
                        }
                        break;
                    case DAY_NIGHT:
                        if(parkingFee != TWO_WHEELER_MONTHLY_DAY_NIGHT_CHARGES){
                            throw new CustomException("Monthly Parking fee for vehicle is not correct","Please provide the valid parking fee");
                        }
                        break;
                }
                break;
                case 11:
                    switch (parkingType) {
                        case DAY_ONLY:
                            if(parkingFee != LORRY_TRUCK_TEMPO_CAR_THREE_WHEELER_MONTHLY_DAY_CHARGES){
                                throw new CustomException("Monthly Parking fee for vehicle is not correct","Please provide the valid parking fee");
                            }
                            break;
                        case DAY_NIGHT:
                            if(parkingFee != LORRY_TRUCK_TEMPO_CAR_THREE_WHEELER_MONTHLY_DAY_NIGHT_CHARGES){
                                throw new CustomException("Monthly Parking fee for vehicle is not correct","Please provide the valid parking fee");
                            }
                              break;
                    }

        }
        request.setStartDate(currentTimeMillis);
        request.setEndDate(currentTimeAfterOneMonth);
        request.setCreatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
        request.setCreatedAt(currentTimeMillis);
        request.setUpdatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
        request.setUpdatedAt(currentTimeMillis);
        producer.push(DeonarConstant.SAVE_MONTHLY_VEHICLE_PARKING_FEE, request);

      return request;
    }

    public List<VehicleParkingFeeResponseDetails> getVehicleMonthlyDetails(VehicleParkedCheckCriteria criteria) {
        if (criteria == null) {
            throw new CustomException("INVALID_SEARCH_CRITERIA", "Search criteria is empty");
        }
        return vehicleParkingRepository.getMonthlyVehicleParkedDetails(criteria);
    }


    public VehicleParkingFeeResponseDetails getParkingFee(VehicleParkedCheckCriteria criteria) {

        VehicleParkingFeeResponseDetails response = new VehicleParkingFeeResponseDetails();
        List<VehicleParkingFeeResponseDetails> vehicleParkingMonthly = vehicleParkingRepository.getMonthlyVehicleParkedDetails(criteria);
        if(vehicleParkingMonthly.isEmpty()){}

        List<VehicleParkedCheckDetails> parkingDetails = vehicleParkingRepository.getVehicleParkedDetails(criteria);
        LocalTime parkedInTime = parkingDetails.getFirst().getParkingTime();
        LocalTime parkedOutTime = parkingDetails.getFirst().getDepartureTime();
        LocalDate parkedIndDate = parkingDetails.getFirst().getParkingDate();
        LocalDate parkedOutDate = parkingDetails.getFirst().getDepartureDate();
        if (parkedOutDate == null) {
            parkedOutTime = LocalTime.now();
            parkedOutDate = LocalDate.now();
        }
            long vehicleId = parkingDetails.getFirst().getVehicleId();
            if (vehicleId == 11) {
                parkingDetails.getFirst().setVehicleType(THREE_WHEELER);
            } else if (vehicleId == 12) {
                parkingDetails.getFirst().setVehicleType(TWO_WHEELER);
            }
            String vehicleType = parkingDetails.getFirst().getVehicleType();
            double parkingFee = VehicleParkingRepository.calculateParkingFee(parkedInTime, parkedOutTime, parkedIndDate, parkedOutDate, vehicleType);
            response = VehicleParkingFeeResponseDetails.builder().parkingFee(parkingFee).vehicleNumber(criteria.getVehicleNumber()).vehicleType(vehicleType).build();
        // } else {
        //     response.setParkingFee(0);
        // }
        return response;
    }

    public VehicleWashingFeesResponse getVehicleWashingFee(VehicleParkedCheckCriteria criteria) {
        if (criteria == null) {
            throw new CustomException("INVALID_SEARCH_CRITERIA", "Search criteria is empty");
        }
        long washingFee;
        String vehicleType = "";
        VehicleWashingFeesResponse response;
        List<VehicleParkedCheckDetails> parkingDetails = vehicleParkingRepository.getParkedInVehicleDetails();

        String vehicleNumber = criteria.getVehicleNumber();
        boolean isVehicleParked = parkingDetails.stream().anyMatch(details -> details.getVehicleNumber().equals(vehicleNumber));
        Long vehicleId = criteria.getVehicleType();
        if (vehicleId == 11) {
            vehicleType = THREE_WHEELER;
        } else if (vehicleId == 12) {
            vehicleType = TWO_WHEELER;
        }
        if (isVehicleParked) {
            washingFee = CHARGES_FOR_WASHING_PRIVATE_MEAT_VAN;
            response = VehicleWashingFeesResponse.builder().vehicleNumber(criteria.getVehicleNumber()).vehicleType(vehicleType).washingFee(washingFee).build();
        } else {
            response = VehicleWashingFeesResponse.builder().vehicleNumber(criteria.getVehicleNumber()).vehicleType(vehicleType).washingFee(0).build();
        }
        return response;
    }



}