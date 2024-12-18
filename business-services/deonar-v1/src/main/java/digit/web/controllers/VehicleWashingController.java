package digit.web.controllers;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import digit.service.VehicleWashingService;
import digit.util.ResponseInfoFactory;
import digit.web.models.security.vehicleparking.VehicleWashingFeesResponse;
import digit.web.models.security.vehicleparking.VehicleWashingFeesResponseWraper;
import digit.web.models.security.vehiclewashing.VehicleWashCheckDetails;
import digit.web.models.security.vehiclewashing.VehicleWashedCheckRequest;
import digit.web.models.security.vehiclewashing.VehicleWashedCheckResponse;
import digit.web.models.security.vehiclewashing.VehicleWashingDetails;
import digit.web.models.security.vehiclewashing.VehicleWashingRequest;
import digit.web.models.security.vehiclewashing.VehicleWashingResponse;

@RestController
public class VehicleWashingController {

        @Autowired
        private VehicleWashingService vehicleWashingService;

        @Autowired
        private ResponseInfoFactory responseInfoFactory;

        @CrossOrigin("*")
        @PostMapping("/vehicleWashing/_save")
        public ResponseEntity<VehicleWashingResponse> vehicleWashingDetails(
                        @RequestBody VehicleWashingRequest vehicleWashingRequest) {
                VehicleWashingDetails vehicleWashingDetails = null;
                VehicleWashingResponse response = new VehicleWashingResponse();
                try {
                        VehicleWashingRequest savedRequest = vehicleWashingService
                                        .saveWashedVehicleDetails(vehicleWashingRequest);

                        vehicleWashingDetails = savedRequest.getVehicleWashingDetails();
                        long vehicleType = vehicleWashingDetails.getVehicleType();
                        String vehicleNumber = vehicleWashingDetails.getVehicleNumber();
                        Long washingTime = savedRequest.getVehicleWashingDetails().getWashingTime();
                        Long departureTime = savedRequest.getVehicleWashingDetails().getDepartureTime();

                        response = VehicleWashingResponse.builder().vehicleType(vehicleType)
                                        .vehicleNumber(vehicleNumber)
                                        .washingTime(washingTime).departureTime(departureTime).build();

                        return new ResponseEntity<>(response, HttpStatus.OK);
                } catch (Exception e) {
                        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        @CrossOrigin("*")
        @PostMapping("/vehicleWashing/_search")
        public ResponseEntity<VehicleWashedCheckResponse> getVehicleWashingDetails(
                        @RequestBody VehicleWashedCheckRequest vehicleWashedCheckRequest) {

                try {
                        List<VehicleWashCheckDetails> vehicleDetails = vehicleWashingService
                                        .getVehicleDetails(vehicleWashedCheckRequest.getVehicleWashCheckCriteria());
                        return getVehicleWashedCheckResponseResponseEntity(vehicleWashedCheckRequest, vehicleDetails);
                } catch (Exception e) {
                        ResponseInfo responseInfo = responseInfoFactory
                                        .createResponseInfoFromRequestInfo(vehicleWashedCheckRequest.getRequestInfo(),
                                                        true);
                        VehicleWashedCheckResponse response = VehicleWashedCheckResponse.builder()
                                        .message("Error occurred while trying to retrieve washed vehicle details: "
                                                        + e.getMessage())
                                        .responseInfo(responseInfo).build();
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
        }

        @CrossOrigin("*")
        @PostMapping("/vehicleWashing/_washInVehicle")
        public ResponseEntity<VehicleWashedCheckResponse> getWashedInVehicle(
                        @RequestBody VehicleWashedCheckRequest vehicleWashedCheckRequest) {

                try {
                        List<VehicleWashCheckDetails> vehicleDetails = vehicleWashingService
                                        .getWashedInVehicleDetails();
                        return getVehicleWashedCheckResponseResponseEntity(vehicleWashedCheckRequest, vehicleDetails);
                } catch (Exception e) {
                        ResponseInfo responseInfo = responseInfoFactory
                                        .createResponseInfoFromRequestInfo(vehicleWashedCheckRequest.getRequestInfo(),
                                                        true);
                        VehicleWashedCheckResponse response = VehicleWashedCheckResponse.builder()
                                        .message("Error occurred while trying to retrieve washed vehicle details: "
                                                        + e.getMessage())
                                        .responseInfo(responseInfo).build();
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
        }

        @NotNull
        private ResponseEntity<VehicleWashedCheckResponse> getVehicleWashedCheckResponseResponseEntity(
                        @RequestBody VehicleWashedCheckRequest vehicleWashedCheckRequest,
                        List<VehicleWashCheckDetails> vehicleDetails) {
                ResponseInfo responseInfo = responseInfoFactory
                                .createResponseInfoFromRequestInfo(vehicleWashedCheckRequest.getRequestInfo(), true);
                VehicleWashedCheckResponse response;
                if (!(vehicleDetails.size() == 0)) {
                        response = VehicleWashedCheckResponse.builder().vehicleWashedCheckDetails(vehicleDetails)
                                        .responseInfo(responseInfo).build();
                } else {
                        response = VehicleWashedCheckResponse.builder()
                                        .message("Vehicle is not washed or not saved in database")
                                        .responseInfo(responseInfo).build();
                }
                return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // @PostMapping("/vehicleParking/_saveMonthlyParkingFee")
        // public ResponseEntity<VehicleParkingFeeResponse> saveParkingFee(
        // @RequestBody VehicleParkingFeeRequest vehicleParkingFeeRequest) {

        // VehicleParkingFeeResponse response = new VehicleParkingFeeResponse();

        // try {

        // ResponseInfo responseInfo = responseInfoFactory
        // .createResponseInfoFromRequestInfo(vehicleParkingFeeRequest.getRequestInfo(),
        // true);
        // response.setResponseInfo(responseInfo);
        // response.setMessage("Vehicle monthly parking fee saved successfully");
        // return new ResponseEntity<>(response, HttpStatus.OK);
        // } catch (Exception e) {
        // ResponseInfo responseInfo = responseInfoFactory
        // .createResponseInfoFromRequestInfo(vehicleParkingFeeRequest.getRequestInfo(),
        // true);
        // response = VehicleParkingFeeResponse.builder()
        // .message("Error occurred while trying to saving vehicle monthly parking fee:
        // " + e.getMessage())
        // .responseInfo(responseInfo).build();
        // return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        // }
        // }

        // @PostMapping("/vehicleParking/_searchVehicleMonthlyParkingFee")
        // public ResponseEntity<VehicleParkingFeeResponse>
        // getVehicleMonthlyParkingDetails(
        // @RequestBody VehicleWashedCheckRequest vehicleWashedCheckRequest) {

        // VehicleParkingFeeResponse response;
        // try {
        // List<VehicleParkingFeeResponseDetails> vehicleParkingFeeResponses =
        // vehicleWashingService
        // .getVehicleMonthlyDetails(vehicleWashedCheckRequest.getVehicleParkedCheckCriteria());
        // ResponseInfo responseInfo = responseInfoFactory
        // .createResponseInfoFromRequestInfo(vehicleWashedCheckRequest.getRequestInfo(),
        // true);
        // response =
        // VehicleParkingFeeResponse.builder().vehicleParkingFeeResponseDetails(vehicleParkingFeeResponses)
        // .responseInfo(responseInfo).message("Vehicle monthly parking fee details
        // fetched successfully.")
        // .build();
        // return new ResponseEntity<>(response, HttpStatus.OK);
        // } catch (Exception e) {
        // ResponseInfo responseInfo = responseInfoFactory
        // .createResponseInfoFromRequestInfo(vehicleWashedCheckRequest.getRequestInfo(),
        // true);
        // response = VehicleParkingFeeResponse.builder()
        // .message("Error occurred while trying to retrieve parked vehicle details: " +
        // e.getMessage())
        // .responseInfo(responseInfo).build();
        // return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        // }
        // }

        @CrossOrigin("*")
        @PostMapping("/vehicleWashing/_VehicleWashingFee")
        public ResponseEntity<VehicleWashingFeesResponseWraper> getVehicleWashingFee(
                        @RequestBody VehicleWashedCheckRequest vehicleWashedCheckRequest) {
                VehicleWashingFeesResponse response;
                try {
                        response = vehicleWashingService
                                        .getVehicleWashingFee(
                                                        vehicleWashedCheckRequest.getVehicleWashCheckCriteria());
                        ResponseInfo responseInfo = responseInfoFactory
                                        .createResponseInfoFromRequestInfo(vehicleWashedCheckRequest.getRequestInfo(),
                                                        true);
                        response.setMessage("Vehicle washing fee fetched successfully");
                        VehicleWashingFeesResponseWraper wraper = VehicleWashingFeesResponseWraper.builder()
                                        .responseInfo(responseInfo).response(response).build();
                        return new ResponseEntity<>(wraper, HttpStatus.OK);
                } catch (Exception e) {
                        ResponseInfo responseInfo = responseInfoFactory
                                        .createResponseInfoFromRequestInfo(vehicleWashedCheckRequest.getRequestInfo(),
                                                        true);
                        response = VehicleWashingFeesResponse.builder()
                                        .message("Error occurred while trying to fetch vehicle washing fee: "
                                                        + e.getMessage())
                                        .build();
                        VehicleWashingFeesResponseWraper wraper = VehicleWashingFeesResponseWraper.builder()
                                        .responseInfo(responseInfo).response(response).build();
                        return new ResponseEntity<>(wraper, HttpStatus.BAD_REQUEST);
                }
        }

}