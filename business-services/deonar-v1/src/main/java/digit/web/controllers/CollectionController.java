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
import digit.web.models.collection.CollectFeeRequest;
import digit.web.models.collection.CollectedFeeResponse;
import digit.web.models.collection.CollectionRequest;
import digit.web.models.collection.EntryFee;
import digit.web.models.collection.EntryFeeResponse;
import digit.web.models.collection.FeeDetail;
import digit.web.models.collection.ParkingFee;
import digit.web.models.collection.ParkingFeeResponse;
import digit.web.models.collection.SlaughterFee;
import digit.web.models.collection.SlaughterFeeResponse;
import digit.web.models.collection.StableFee;
import digit.web.models.collection.StableFeeResponse;
import digit.web.models.collection.WashFee;
import digit.web.models.collection.WashingFeeResponse;
import digit.web.models.collection.WeighingFee;
import digit.web.models.collection.WeighingFeeResponse;
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

    @PostMapping("/trading/_get")
    public ResponseEntity<Object> getTradingFee(
            @ApiParam(value = "Get trading Fee Details", required = true) @Valid @RequestBody CollectionRequest request) {
        try {
            List<StableFee> common = service.getTradingFee(request.getRequestInfo(), request.getCriteria());
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



    @PostMapping("/removal/_get")
    public ResponseEntity<Object> getRemovalFee(
            @ApiParam(value = "Get removal  Fee Details", required = true) @Valid @RequestBody CollectionRequest request) {
        try {
            List<StableFee> common = service.getRemovalFee(request.getRequestInfo(), request.getCriteria());
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

    @PostMapping("/washing/_get")
    public ResponseEntity<Object> getWashingFee(
            @ApiParam(value = "Get Washing Fee Details", required = true) @Valid @RequestBody CollectionRequest request) {
        try {
            List<WashFee> common = service.getWashingFee(request.getRequestInfo(), request.getCriteria());
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            WashingFeeResponse res = WashingFeeResponse.builder()
                    .details(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/weighing/_get")
    public ResponseEntity<Object> getWeighingFee(
            @ApiParam(value = "Get weighing Fee Details", required = true) @Valid @RequestBody CollectionRequest request) {
        try {
            List<WeighingFee> common = service.getWeighingFee(request.getRequestInfo(), request.getCriteria());
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            WeighingFeeResponse res = WeighingFeeResponse.builder()
                    .details(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/slaughter/_get")
    public ResponseEntity<Object> getSlaughterFee(
            @ApiParam(value = "Get Slaughtering Fee Details", required = true) @Valid @RequestBody CollectionRequest request) {
        try {
            List<SlaughterFee> common = service.getSlaughterFee(request.getRequestInfo(), request.getCriteria());
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            SlaughterFeeResponse res = SlaughterFeeResponse.builder()
                    .details(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PostMapping("/_save")
    public ResponseEntity<Object> saveFee(@RequestBody CollectFeeRequest request) {
        try {
            FeeDetail common = service.saveFee(request.getRequestInfo(), request.getFeedetail());
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            CollectedFeeResponse res = CollectedFeeResponse.builder()
                    .details(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
