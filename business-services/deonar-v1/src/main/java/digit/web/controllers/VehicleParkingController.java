package digit.web.controllers;

import digit.service.VehicleParkingService;
import digit.util.ResponseInfoFactory;
import digit.web.models.security.vehicleParking.VehicleParkedCheckDetails;
import digit.web.models.security.vehicleParking.VehicleParkedCheckRequest;
import digit.web.models.security.vehicleParking.VehicleParkedCheckResponse;
import digit.web.models.security.vehicleParking.VehicleParkingRequest;
import org.egov.common.contract.response.ResponseInfo;
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


    @PostMapping("/vehicleParkingDetails/_save")
    @CrossOrigin("*")
    public ResponseEntity<String> vehicleParkingDetails(@RequestBody VehicleParkingRequest vehicleParkingRequest) {
        try {
            vehicleParkingService.saveParkedVehicleDetails(vehicleParkingRequest);
            return new ResponseEntity<>("Vehicle parking details saved successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to save Vehicle parking details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/vehicleParkingDetails/_search")
    @CrossOrigin("*")
    public ResponseEntity<VehicleParkedCheckResponse> getVehicleParkingDetails(@RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest) {

        try {
            List<VehicleParkedCheckDetails> vehicleDetails = vehicleParkingService.getVehicleDetails(vehicleParkedCheckRequest.getVehicleParkedCheckCriteria());
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            VehicleParkedCheckResponse response;
            if(!(vehicleDetails.size() == 0)) {
                response = VehicleParkedCheckResponse.builder()
                        .vehicleParkedCheckDetails(vehicleDetails)
                        .responseInfo(responseInfo)
                        .build();
            } else {
                response = VehicleParkedCheckResponse.builder()
                        .message("Vehicle is not parked or not saved in database")
                        .responseInfo(responseInfo)
                        .build();
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            VehicleParkedCheckResponse response = VehicleParkedCheckResponse.builder()
                    .message("Error occurred while trying to retrieve parked vehicle details: " + e.getMessage())
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
