package digit.web.controllers;

import digit.service.VehicleParkingService;
import digit.util.ResponseInfoFactory;
import digit.web.models.security.vehicleparking.*;
import org.egov.common.contract.response.ResponseInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VehicleParkingController {

    @Autowired
    private VehicleParkingService vehicleParkingService;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;


    @PostMapping("/vehicleParking/_save")
    public ResponseEntity<VehicleParkingResponse> vehicleParkingDetails(@RequestBody VehicleParkingRequest vehicleParkingRequest) {
        VehicleParkingDetails vehicleParkingDetails = null;
        VehicleParkingResponse response = new VehicleParkingResponse();
        try {
            VehicleParkingRequest savedRequest = vehicleParkingService.saveParkedVehicleDetails(vehicleParkingRequest);

            vehicleParkingDetails = savedRequest.getVehicleParkingDetails();
            long vehicleType = vehicleParkingDetails.getVehicleType();
            String vehicleNumber = vehicleParkingDetails.getVehicleNumber();
            Long parkingTime = savedRequest.getVehicleParkingDetails().getParkingTime();
            Long departureTime = savedRequest.getVehicleParkingDetails().getDepartureTime();

            response = VehicleParkingResponse.builder().vehicleType(vehicleType).vehicleNumber(vehicleNumber).parkingTime(parkingTime).departureTime(departureTime).build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/vehicleParking/_search")
    public ResponseEntity<VehicleParkedCheckResponse> getVehicleParkingDetails(@RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest) {

        try {
            List<VehicleParkedCheckDetails> vehicleDetails = vehicleParkingService.getVehicleDetails(vehicleParkedCheckRequest.getVehicleParkedCheckCriteria());
            return getVehicleParkedCheckResponseResponseEntity(vehicleParkedCheckRequest, vehicleDetails);
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            VehicleParkedCheckResponse response = VehicleParkedCheckResponse.builder().message("Error occurred while trying to retrieve parked vehicle details: " + e.getMessage()).responseInfo(responseInfo).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/vehicleParking/_parkedInVehicle")
    public ResponseEntity<VehicleParkedCheckResponse> getParkedInVehicle(@RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest) {

        try {
            List<VehicleParkedCheckDetails> vehicleDetails = vehicleParkingService.getParkedInVehicleDetails();
            return getVehicleParkedCheckResponseResponseEntity(vehicleParkedCheckRequest, vehicleDetails);
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            VehicleParkedCheckResponse response = VehicleParkedCheckResponse.builder().message("Error occurred while trying to retrieve parked vehicle details: " + e.getMessage()).responseInfo(responseInfo).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @NotNull
    private ResponseEntity<VehicleParkedCheckResponse> getVehicleParkedCheckResponseResponseEntity(@RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest, List<VehicleParkedCheckDetails> vehicleDetails) {
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
        VehicleParkedCheckResponse response;
        if (!(vehicleDetails.size() == 0)) {
            response = VehicleParkedCheckResponse.builder().vehicleParkedCheckDetails(vehicleDetails).responseInfo(responseInfo).build();
        } else {
            response = VehicleParkedCheckResponse.builder().message("Vehicle is not parked or not saved in database").responseInfo(responseInfo).build();
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
