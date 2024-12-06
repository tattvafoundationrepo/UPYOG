package digit.web.controllers;

import digit.service.VehicleParkingService;
import digit.util.ResponseInfoFactory;
import digit.web.models.security.vehicleparking.*;
import org.egov.common.contract.response.ResponseInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class VehicleParkingController {

    @Autowired
    private VehicleParkingService vehicleParkingService;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @PostMapping("/vehicleParking/_save")
    public ResponseEntity<VehicleParkingResponse> vehicleParkingDetails(
            @RequestBody VehicleParkingRequest vehicleParkingRequest) {
        VehicleParkingDetails vehicleParkingDetails = null;
        VehicleParkingResponse response = new VehicleParkingResponse();
        try {
            VehicleParkingRequest savedRequest = vehicleParkingService.saveParkedVehicleDetails(vehicleParkingRequest);

            vehicleParkingDetails = savedRequest.getVehicleParkingDetails();
            long vehicleType = vehicleParkingDetails.getVehicleType();
            String vehicleNumber = vehicleParkingDetails.getVehicleNumber();
            Long parkingTime = savedRequest.getVehicleParkingDetails().getParkingTime();
            Long departureTime = savedRequest.getVehicleParkingDetails().getDepartureTime();

            response = VehicleParkingResponse.builder().vehicleType(vehicleType).vehicleNumber(vehicleNumber)
                    .parkingTime(parkingTime).departureTime(departureTime).build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/vehicleParking/_search")
    public ResponseEntity<VehicleParkedCheckResponse> getVehicleParkingDetails(
            @RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest) {

        try {
            List<VehicleParkedCheckDetails> vehicleDetails = vehicleParkingService
                    .getVehicleDetails(vehicleParkedCheckRequest.getVehicleParkedCheckCriteria());
            return getVehicleParkedCheckResponseResponseEntity(vehicleParkedCheckRequest, vehicleDetails);
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            VehicleParkedCheckResponse response = VehicleParkedCheckResponse.builder()
                    .message("Error occurred while trying to retrieve parked vehicle details: " + e.getMessage())
                    .responseInfo(responseInfo).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/vehicleParking/_parkedInVehicle")
    public ResponseEntity<VehicleParkedCheckResponse> getParkedInVehicle(
            @RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest) {

        try {
            List<VehicleParkedCheckDetails> vehicleDetails = vehicleParkingService.getParkedInVehicleDetails();
            return getVehicleParkedCheckResponseResponseEntity(vehicleParkedCheckRequest, vehicleDetails);
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            VehicleParkedCheckResponse response = VehicleParkedCheckResponse.builder()
                    .message("Error occurred while trying to retrieve parked vehicle details: " + e.getMessage())
                    .responseInfo(responseInfo).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @NotNull
    private ResponseEntity<VehicleParkedCheckResponse> getVehicleParkedCheckResponseResponseEntity(
            @RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest,
            List<VehicleParkedCheckDetails> vehicleDetails) {
        ResponseInfo responseInfo = responseInfoFactory
                .createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
        VehicleParkedCheckResponse response;
        if (!(vehicleDetails.size() == 0)) {
            response = VehicleParkedCheckResponse.builder().vehicleParkedCheckDetails(vehicleDetails)
                    .responseInfo(responseInfo).build();
        } else {
            response = VehicleParkedCheckResponse.builder().message("Vehicle is not parked or not saved in database")
                    .responseInfo(responseInfo).build();
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/vehicleParking/_saveMonthlyParkingFee")
    public ResponseEntity<VehicleParkingFeeResponse> saveParkingFee(
            @RequestBody VehicleParkingFeeRequest vehicleParkingFeeRequest) {

        VehicleParkingFeeResponse response = new VehicleParkingFeeResponse();

        try {
   
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkingFeeRequest.getRequestInfo(), true);
            response.setResponseInfo(responseInfo);
            response.setMessage("Vehicle monthly parking fee saved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkingFeeRequest.getRequestInfo(), true);
            response = VehicleParkingFeeResponse.builder()
                    .message("Error occurred while trying to saving vehicle monthly parking fee: " + e.getMessage())
                    .responseInfo(responseInfo).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/vehicleParking/_searchVehicleMonthlyParkingFee")
    public ResponseEntity<VehicleParkingFeeResponse> getVehicleMonthlyParkingDetails(
            @RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest) {

        VehicleParkingFeeResponse response;
        try {
            List<VehicleParkingFeeResponseDetails> vehicleParkingFeeResponses = vehicleParkingService
                    .getVehicleMonthlyDetails(vehicleParkedCheckRequest.getVehicleParkedCheckCriteria());
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            response = VehicleParkingFeeResponse.builder().vehicleParkingFeeResponseDetails(vehicleParkingFeeResponses)
                    .responseInfo(responseInfo).message("Vehicle monthly parking fee details fetched successfully.")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            response = VehicleParkingFeeResponse.builder()
                    .message("Error occurred while trying to retrieve parked vehicle details: " + e.getMessage())
                    .responseInfo(responseInfo).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/vehicleParking/_parkingFee")
    public ResponseEntity<VehicleParkingFeeResponse> getParkingFee(
            @RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest) {

        VehicleParkingFeeResponse response;
        try {
            VehicleParkingFeeResponseDetails vehicleParkingFeeResponseDetails = vehicleParkingService
                    .getParkingFee(vehicleParkedCheckRequest.getVehicleParkedCheckCriteria());
            List<VehicleParkingFeeResponseDetails> list = new ArrayList<>();
            list.add(vehicleParkingFeeResponseDetails);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            response = VehicleParkingFeeResponse.builder().vehicleParkingFeeResponseDetails(list)
                    .responseInfo(responseInfo)
                    .message("Vehicle fee fetched successfully").build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            response = VehicleParkingFeeResponse.builder()
                    .message("Error occurred while trying to fetch parking fee: " + e.getMessage())
                    .responseInfo(responseInfo).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/vehicleParking/_VehicleWashingFee")
    public ResponseEntity<VehicleWashingFeesResponseWraper> getVehicleWashingFee(
            @RequestBody VehicleParkedCheckRequest vehicleParkedCheckRequest) {
        VehicleWashingFeesResponse response;
        try {
            response = vehicleParkingService
                    .getVehicleWashingFee(vehicleParkedCheckRequest.getVehicleParkedCheckCriteria());
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            response.setMessage("Vehicle washing fee fetched successfully");
            VehicleWashingFeesResponseWraper wraper = VehicleWashingFeesResponseWraper.builder()
                    .responseInfo(responseInfo).response(response).build();
            return new ResponseEntity<>(wraper, HttpStatus.OK);
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(vehicleParkedCheckRequest.getRequestInfo(), true);
            response = VehicleWashingFeesResponse.builder()
                    .message("Error occurred while trying to fetch vehicle washing fee: " + e.getMessage()).build();
            VehicleWashingFeesResponseWraper wraper = VehicleWashingFeesResponseWraper.builder()
                    .responseInfo(responseInfo).response(response).build();
            return new ResponseEntity<>(wraper, HttpStatus.BAD_REQUEST);
        }
    }

}