package digit.web.controllers;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import digit.service.AnimalAssignmentService;
import digit.service.SecurityCheckService;
import digit.util.ResponseInfoFactory;
import digit.web.models.AnimalAssignmentRequest;
import digit.web.models.security.SecurityCheckDetails;
import digit.web.models.security.SecurityCheckRequest;
import digit.web.models.security.SecurityCheckResponse;
import digit.web.models.shopkeeper.ShopkeeperDetails;
import digit.web.models.shopkeeper.ShopkeeperRequest;
import digit.web.models.shopkeeper.ShopkeeperResponse;
import io.swagger.annotations.ApiParam;

@Controller
public class AnimalAssignmentController {

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

      @Autowired
    private SecurityCheckService securityCheckService;

    @Autowired
    AnimalAssignmentService service;

    @PostMapping("/assign/animal")
    public ResponseEntity<String> aaaignAnimal(@RequestBody AnimalAssignmentRequest request) {
        try {
            service.assignAnimalsToStakeholder(request);
            return new ResponseEntity<>("Animal assigned Successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("assignment failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // @PostMapping("/remove/animal")
    // public ResponseEntity<String> removalAnimal(@RequestBody AnimalAssignmentRequest request) {
    //     try {
    //         service.saveAnimalRemoval(request);
    //         return new ResponseEntity<>("Animal removed Successfully", HttpStatus.OK);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return new ResponseEntity<>("removal failed", HttpStatus.INTERNAL_SERVER_ERROR);
    //     }
    // }

    @PostMapping("/get/shopkeepers")
    public ResponseEntity<ShopkeeperResponse> getShopkeeper(
            @ApiParam(value = "Details for inspection ", required = true) @Valid @RequestBody ShopkeeperRequest request) {
        List<ShopkeeperDetails> ShopkeeperDetails = service.getShopkeepers(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
                true);
        ShopkeeperResponse shopkeeperResponse = ShopkeeperResponse.builder().ShopkeeperDetails(ShopkeeperDetails)
                .responseInfo(responseInfo).build();
        return new ResponseEntity<>(shopkeeperResponse, HttpStatus.OK);
    }

    @PostMapping("/get/slaughter/_list")
    public ResponseEntity<ShopkeeperResponse> getSlaughterList(
            @ApiParam(value = "Details for inspection ", required = true) @Valid @RequestBody ShopkeeperRequest request) {
        List<ShopkeeperDetails> ShopkeeperDetails = service.getListForSlaughter(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
                true);
        ShopkeeperResponse shopkeeperResponse = ShopkeeperResponse.builder().ShopkeeperDetails(ShopkeeperDetails)
                .responseInfo(responseInfo).build();
        return new ResponseEntity<>(shopkeeperResponse, HttpStatus.OK);
    }

    
    @PostMapping("/get/trading/_list")
    public ResponseEntity<SecurityCheckResponse> getTradingLists(
            @ApiParam(value = "Lists for animal trading", required = true)
            @Valid @RequestBody RequestInfoWrapper request) {

        List<SecurityCheckDetails> securityDetails = service.getListForTrading(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
        true);
        SecurityCheckResponse response = SecurityCheckResponse.builder()
                .securityCheckDetails(securityDetails)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
             
    @PostMapping("/get/stabling/_list")
    public ResponseEntity<SecurityCheckResponse> getStablingLists(
            @ApiParam(value = "Lists for animal stabling", required = true)
            @Valid @RequestBody RequestInfoWrapper request) {

        List<SecurityCheckDetails> securityDetails = service.getListForTrading(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
        true);
        SecurityCheckResponse response = SecurityCheckResponse.builder()
                .securityCheckDetails(securityDetails)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }





}
