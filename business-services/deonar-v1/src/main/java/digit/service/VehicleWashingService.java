package digit.service;

import java.util.List;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.constants.DeonarConstant;
import static digit.constants.DeonarConstant.CHARGES_FOR_WASHING_PRIVATE_MEAT_VAN;
import static digit.constants.DeonarConstant.THREE_WHEELER;
import static digit.constants.DeonarConstant.TWO_WHEELER;
import digit.kafka.Producer;
import digit.repository.VehicleWashingRepository;
import digit.web.models.security.vehicleparking.VehicleWashingFeesResponse;
import digit.web.models.security.vehiclewashing.VehicleWashCheckCriteria;
import digit.web.models.security.vehiclewashing.VehicleWashCheckDetails;
import digit.web.models.security.vehiclewashing.VehicleWashingDetails;
import digit.web.models.security.vehiclewashing.VehicleWashingRequest;

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
