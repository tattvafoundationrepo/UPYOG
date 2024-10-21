package digit.web.controllers;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import digit.service.CollectionService;
import digit.util.ResponseInfoFactory;
import digit.web.models.collection.CollectionRequest;
import digit.web.models.collection.EntryFee;
import digit.web.models.collection.StableFee;
import digit.web.models.collection.ParkingFee;
import digit.web.models.collection.ParkingFeeResponse;
import digit.web.models.collection.EntryFeeResponse;
import digit.web.models.collection.StableFeeResponse;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/collection/fee")
public class CollectionController {
    @Autowired
    private ResponseInfoFactory responseInfoFactory;
    @Autowired
    private CollectionService service;
    private static final String ERR_MSG = "Request Failed";

    @PostMapping("/entry/_get")
    public ResponseEntity<Object> getEntryFee(
            @ApiParam(value = "Get Entry Fee Details", required = true) @Valid @RequestBody CollectionRequest request) {
        try {
            List<EntryFee> common = service.getEntryFee(request.getRequestInfo(), request.getCriteria());
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            EntryFeeResponse res = EntryFeeResponse.builder()
                    .details(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/stable/_get")
    public ResponseEntity<Object> getStableFee(
            @ApiParam(value = "Get Stable Fee Details", required = true) @Valid @RequestBody CollectionRequest request) {
        try {
            List<StableFee> common = service.getStableFee(request.getRequestInfo(), request.getCriteria());
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            StableFeeResponse res = StableFeeResponse.builder()
                    .details(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/parking/_get")
    public ResponseEntity<Object> getParkingFee(
        @ApiParam(value = "Get Parking Fee Details", required = true) @Valid @RequestBody CollectionRequest request) {
    try {
        List<ParkingFee> common = service.getParkingFee(request.getRequestInfo(), request.getCriteria());
        ResponseInfo responseInfo = responseInfoFactory
                .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        ParkingFeeResponse res = ParkingFeeResponse.builder()
                .details(common)
                .responseInfo(responseInfo)
                .build();
        return new ResponseEntity<>(res, HttpStatus.OK);
    } catch (Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

    // @PostMapping("/slaughter/_get")
    // public ResponseEntity<String> aaaignAnimal(@RequestBody
    // AnimalAssignmentRequest request) {
    // try {
    // // TODO : service.assignAnimalsToStakeholder(request);
    // return new ResponseEntity<>("Animal assigned Successfully", HttpStatus.OK);
    // } catch (Exception e) {
    // e.printStackTrace();
    // return new ResponseEntity<>(ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }

    // @PostMapping("/entry/_save")
    // public ResponseEntity<String> saveEntryFee(@RequestBody
    // AnimalAssignmentRequest request) {
    // try {
    // // TODO : service.assignAnimalsToStakeholder(request);
    // return new ResponseEntity<>("Animal assigned Successfully", HttpStatus.OK);
    // } catch (Exception e) {
    // e.printStackTrace();
    // return new ResponseEntity<>(ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }

    // @PostMapping("/stable/_save")
    // public ResponseEntity<String> aaaignAnimal(@RequestBody
    // AnimalAssignmentRequest request) {
    // try {
    // // TODO : service.assignAnimalsToStakeholder(request);
    // return new ResponseEntity<>("Animal assigned Successfully", HttpStatus.OK);
    // } catch (Exception e) {
    // e.printStackTrace();
    // return new ResponseEntity<>(ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }

    // @PostMapping("/parking/_save")
    // public ResponseEntity<String> aaaignAnimal(@RequestBody
    // AnimalAssignmentRequest request) {
    // try {
    // // TODO : service.assignAnimalsToStakeholder(request);
    // return new ResponseEntity<>("Animal assigned Successfully", HttpStatus.OK);
    // } catch (Exception e) {
    // e.printStackTrace();
    // return new ResponseEntity<>(ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }



    // @PostMapping("/slaughter/_save")
    // public ResponseEntity<String> aaaignAnimal(
    // @ApiParam(value = "List of Stakeholder", required = true) @Valid @RequestBody
    // CommonRequest commonSearchRequest) {
    // try {
    // // TODO : service.assignAnimalsToStakeholder(request);
    // return new ResponseEntity<>("Animal assigned Successfully", HttpStatus.OK);
    // } catch (Exception e) {
    // e.printStackTrace();
    // return new ResponseEntity<>(ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }

}
