package digit.service;

import digit.constants.DeonarConstant;
import digit.kafka.Producer;
import digit.repository.VehicleParkingRepository;
import digit.web.models.security.vehicleparking.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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


}
