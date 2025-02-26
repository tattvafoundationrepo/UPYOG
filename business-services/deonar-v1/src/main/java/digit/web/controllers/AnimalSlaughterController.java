package digit.web.controllers;

import java.util.List;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import digit.service.AnimalSlaughterService;
import digit.util.ResponseInfoFactory;
import digit.web.SlaughterBookingResponse;
import digit.web.models.Slaughter;
import digit.web.models.SlaughterBookingDetails;
import digit.web.models.SlaughterBookingRequest;
import digit.web.models.SlaughterList;
import digit.web.models.SlaughterRequest;
import digit.web.models.SlaughterResponse;
import digit.web.models.SlaughterUnit;
import digit.web.models.SlaughterUnitRequest;
import digit.web.models.SlaughterUnitShiftResponse;

@Controller
public class AnimalSlaughterController {

    private static final String ERR_MSG = "Request Failed";

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    AnimalSlaughterService service;

    @PostMapping("/animal/slaughter/_save")
    public ResponseEntity<Object> saveAnimalSlaughtering(@RequestBody SlaughterRequest request) {
        try {
            Slaughter common = service.saveSlaughteredAnimals(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            SlaughterResponse res = SlaughterResponse.builder()
                    .details(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/animal/slaughter/_shifts")
    public ResponseEntity<Object> getAnimalSlaughterunitShift(@RequestBody SlaughterUnitRequest request) {
        try {
            List<SlaughterUnit> common = service.getSlaughterUnitShifts(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            SlaughterUnitShiftResponse res = SlaughterUnitShiftResponse.builder()
                    .unit(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/animal/slaughterList/_emergency")
    public ResponseEntity<Object> getAnimalListForEmergency(@RequestBody RequestInfoWrapper request) {
        try {
            List<SlaughterList> common = service.getSlaughterListEmergency(request);
            ResponseInfo responseInfo = responseInfoFactory
            .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            SlaughterResponse res = SlaughterResponse.builder()
            .detailsList(common)
            .responseInfo(responseInfo)
            .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    @PostMapping("/animal/slaughterList/_normal")
    public ResponseEntity<Object> getAnimalListForNormal(@RequestBody RequestInfoWrapper request) {
        try {
            List<SlaughterList> common = service.getSlaughterListNormal(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            SlaughterResponse res = SlaughterResponse.builder()
                    .detailsList(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/animal/slaughter/_export")
    public ResponseEntity<Object> getAnimalListForExport(@RequestBody RequestInfoWrapper request) {
        try {
            List<SlaughterList> common = service.getSlaughterListExport(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            SlaughterResponse res = SlaughterResponse.builder()
                    .detailsList(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/animal/slaughter/_booking")
    public ResponseEntity<Object> saveSlaughterBooking(@RequestBody SlaughterBookingRequest request) {
        try {
            List<SlaughterBookingDetails> common = service.saveSlaughterBookings(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            SlaughterBookingResponse res = SlaughterBookingResponse.builder()
                    .detailsList(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/slaughter/list/_booking")
    public ResponseEntity<Object> getAnimalListForbooking(@RequestBody RequestInfoWrapper request) {
        try {
            List<SlaughterList> common = service.getSlaughterListForBooking(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            SlaughterResponse res = SlaughterResponse.builder()
                    .detailsList(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
