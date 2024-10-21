package digit.service;

import digit.constants.DeonarConstant;
import digit.kafka.Producer;
import digit.repository.VehicleParkingRepository;
import digit.web.models.security.vehicleParking.VehicleParkedCheckCriteria;
import digit.web.models.security.vehicleParking.VehicleParkedCheckDetails;
import digit.web.models.security.vehicleParking.VehicleParkingDetails;
import digit.web.models.security.vehicleParking.VehicleParkingRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class VehicleParkingService {


    @Autowired
    private Producer producer;

    @Autowired
    VehicleParkingRepository vehicleParkingRepository;


    public void saveParkedVehicleDetails(VehicleParkingRequest request) {
        if (request == null) {
            throw new CustomException("INVALID_DATA", "Vehicle parking request data is null or incomplete.");
        }
        Long time = LocalTime.now().atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long date = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        VehicleParkingDetails vehicleParkingDetails = request.getVehicleParkingDetails();
            if (vehicleParkingDetails.isVehicleIn()) {
                request.setParkingDate(date);
                request.setParkingTime(time);
                request.setCreatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
                request.setCreatedAt(time);
            } else {
                request.setDepartureDate(date);
                request.setDepartureTime(time);
                request.setUpdatedBy(request.getRequestInfo().getUserInfo().getId().intValue());
                request.setUpdatedAt(time);
            }

            producer.push(DeonarConstant.SAVE_VEHICLE_PARKING, request);
        }


    public List<VehicleParkedCheckDetails> getVehicleDetails(VehicleParkedCheckCriteria criteria) {
        if (criteria == null) {
            throw new CustomException("INVALID_SEARCH_CRITERIA", "Search criteria is empty");
        }

        return vehicleParkingRepository.getVehicleParkedDetails(criteria);
    }

}
