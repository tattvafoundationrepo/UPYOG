package digit.service;

import digit.constants.DeonarConstant;
import digit.kafka.Producer;
import digit.repository.VehicleWashingRepository;
import digit.web.models.security.vehiclewashing.*;
import digit.web.models.security.vehicleparking.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

import static digit.constants.DeonarConstant.*;

@Service
public class VehicleWashingService {

    @Autowired
    private Producer producer;

    @Autowired
    VehicleWashingRepository vehicleWashingRepository;

    public VehicleWashingRequest saveWashedVehicleDetails(VehicleWashingRequest request) {
        if (request == null) {
            throw new CustomException("INVALID_DATA", "Vehicle washing request data is null or incomplete.");
        }

        long currentTimeMillis = System.currentTimeMillis();
        VehicleWashingDetails vehicleWashingDetails = request.getVehicleWashingDetails();
        if (vehicleWashingDetails.isVehicleIn()) {
            vehicleWashingDetails.setWashingTime(currentTimeMillis);
            request.setCreatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
            request.setCreatedAt(currentTimeMillis);
        } else {
            vehicleWashingDetails.setDepartureTime(currentTimeMillis);
            vehicleWashingDetails.setWashingTime(
                    vehicleWashingRepository.getWashedInTime(request.getVehicleWashingDetails().getVehicleType(),
                            request.getVehicleWashingDetails().getVehicleNumber()));
            request.setUpdatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
            request.setUpdatedAt(currentTimeMillis);
        }

        producer.push(DeonarConstant.SAVE_VEHICLE_WASHING, request);
        request.setVehicleWashingDetails(vehicleWashingDetails);
        return request;
    }

    public List<VehicleWashCheckDetails> getVehicleDetails(VehicleWashCheckCriteria criteria) {
        if (criteria == null) {
            throw new CustomException("INVALID_SEARCH_CRITERIA", "Search criteria is empty");
        }
        return vehicleWashingRepository.getVehicleWashedDetails(criteria);
    }

    public List<VehicleWashCheckDetails> getWashedInVehicleDetails() {
        return vehicleWashingRepository.getWashedInVehicleDetails();
    }

    // public VehicleParkingFeeRequest
    // saveMonthlyParkingFee(VehicleParkingFeeRequest request) {
    // long currentTimeMillis = System.currentTimeMillis();
    // long currentTimeAfterOneMonth =
    // LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis),
    // ZoneOffset.UTC)
    // .plusMonths(1)
    // .toInstant(ZoneOffset.UTC)
    // .toEpochMilli();
    // int vehicleType = request.getVehicleParkingFeeDetails().getVehicleType();
    // String parkingType = request.getVehicleParkingFeeDetails().getParkingType();
    // double parkingFee = request.getVehicleParkingFeeDetails().getMonthlyFee();
    // switch (vehicleType) {
    // case 12:
    // switch (parkingType) {
    // case DAY_ONLY:
    // if (parkingFee != TWO_WHEELER_MONTHLY_DAY_CHARGES) {
    // throw new CustomException("Monthly Parking fee for vehicle is not correct",
    // "Please provide the valid parking fee");
    // }
    // break;
    // case DAY_NIGHT:
    // if (parkingFee != TWO_WHEELER_MONTHLY_DAY_NIGHT_CHARGES) {
    // throw new CustomException("Monthly Parking fee for vehicle is not correct",
    // "Please provide the valid parking fee");
    // }
    // break;
    // }
    // break;
    // case 11:
    // switch (parkingType) {
    // case DAY_ONLY:
    // if (parkingFee != LORRY_TRUCK_TEMPO_CAR_THREE_WHEELER_MONTHLY_DAY_CHARGES) {
    // throw new CustomException("Monthly Parking fee for vehicle is not correct",
    // "Please provide the valid parking fee");
    // }
    // break;
    // case DAY_NIGHT:
    // if (parkingFee !=
    // LORRY_TRUCK_TEMPO_CAR_THREE_WHEELER_MONTHLY_DAY_NIGHT_CHARGES) {
    // throw new CustomException("Monthly Parking fee for vehicle is not correct",
    // "Please provide the valid parking fee");
    // }
    // break;
    // }

    // }
    // request.setStartDate(currentTimeMillis);
    // request.setEndDate(currentTimeAfterOneMonth);
    // request.setCreatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
    // request.setCreatedAt(currentTimeMillis);
    // request.setUpdatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
    // request.setUpdatedAt(currentTimeMillis);
    // producer.push(DeonarConstant.SAVE_MONTHLY_VEHICLE_PARKING_FEE, request);

    // return request;
    // }

    // public List<VehicleParkingFeeResponseDetails>
    // getVehicleMonthlyDetails(VehicleWashCheckCriteria criteria) {
    // if (criteria == null) {
    // throw new CustomException("INVALID_SEARCH_CRITERIA", "Search criteria is
    // empty");
    // }
    // return vehicleParkingRepository.getMonthlyVehicleParkedDetails(criteria);
    // }

    // public VehicleParkingFeeResponseDetails
    // getParkingFee(VehicleWashCheckCriteria criteria) {

    // VehicleParkingFeeResponseDetails response = new
    // VehicleParkingFeeResponseDetails();
    // List<VehicleParkingFeeResponseDetails> vehicleParkingMonthly =
    // vehicleParkingRepository
    // .getMonthlyVehicleParkedDetails(criteria);
    // if (vehicleParkingMonthly.isEmpty()) {
    // }

    // List<VehicleWashCheckCriteria> washingDetails =
    // vehicleParkingRepository.getVehicleParkedDetails(criteria);
    // LocalTime parkedInTime = washingDetails.getFirst().getParkingTime();
    // LocalTime parkedOutTime = washingDetails.getFirst().getDepartureTime();
    // LocalDate parkedIndDate = washingDetails.getFirst().getParkingDate();
    // LocalDate parkedOutDate = washingDetails.getFirst().getDepartureDate();
    // if (parkedOutDate == null) {
    // parkedOutTime = LocalTime.now();
    // parkedOutDate = LocalDate.now();
    // }
    // long vehicleId = washingDetails.getFirst().getVehicleId();
    // if (vehicleId == 11) {
    // washingDetails.getFirst().setVehicleType(THREE_WHEELER);
    // } else if (vehicleId == 12) {
    // washingDetails.getFirst().setVehicleType(TWO_WHEELER);
    // }
    // String vehicleType = washingDetails.getFirst().getVehicleType();
    // double parkingFee =
    // VehicleParkingRepository.calculateParkingFee(parkedInTime, parkedOutTime,
    // parkedIndDate,
    // parkedOutDate, vehicleType);
    // response = VehicleParkingFeeResponseDetails.builder().parkingFee(parkingFee)
    // .vehicleNumber(criteria.getVehicleNumber()).vehicleType(vehicleType).build();
    // // } else {
    // // response.setParkingFee(0);
    // // }
    // return response;
    // }

    public VehicleWashingFeesResponse getVehicleWashingFee(VehicleWashCheckCriteria criteria) {
        if (criteria == null) {
            throw new CustomException("INVALID_SEARCH_CRITERIA", "Search criteria is empty");
        }
        long washingFee;
        String vehicleType = "";
        VehicleWashingFeesResponse response;
        List<VehicleWashCheckDetails> washingDetails = vehicleWashingRepository.getWashedInVehicleDetails();

        String vehicleNumber = criteria.getVehicleNumber();
        boolean isVehicleWashed = washingDetails.stream()
                .anyMatch(details -> details.getVehicleNumber().equals(vehicleNumber));
        Long vehicleId = criteria.getVehicleType();
        if (vehicleId == 11) {
            vehicleType = THREE_WHEELER;
        } else if (vehicleId == 12) {
            vehicleType = TWO_WHEELER;
        }
        if (isVehicleWashed) {
            washingFee = CHARGES_FOR_WASHING_PRIVATE_MEAT_VAN;
            response = VehicleWashingFeesResponse.builder().vehicleNumber(criteria.getVehicleNumber())
                    .vehicleType(vehicleType).washingFee(washingFee).build();
        } else {
            response = VehicleWashingFeesResponse.builder().vehicleNumber(criteria.getVehicleNumber())
                    .vehicleType(vehicleType).washingFee(0).build();
        }
        return response;
    }

}
