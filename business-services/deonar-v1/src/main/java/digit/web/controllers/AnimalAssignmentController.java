package digit.web.controllers;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import digit.service.AnimalAssignmentService;
import digit.util.ResponseInfoFactory;
import digit.web.models.AnimalAssignmentRequest;
import digit.web.models.GetListRequest;
import digit.web.models.RemovalList;
import digit.web.models.RemovalListResponse;
import digit.web.models.SlaughterList;
import digit.web.models.security.SecurityCheckDetails;
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
        List<SlaughterList> ShopkeeperDetails = service.getListForSlaughter(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
                true);
        ShopkeeperResponse shopkeeperResponse = ShopkeeperResponse.builder().slaughterListDetails(ShopkeeperDetails)
                .responseInfo(responseInfo).build();
        return new ResponseEntity<>(shopkeeperResponse, HttpStatus.OK);
    }


    @PostMapping("/get/weighing/_list")
 
    public ResponseEntity<ShopkeeperResponse> getWeighingList(
            @ApiParam(value = " list for weighing collection fee ", required = true) @Valid @RequestBody ShopkeeperRequest request) {
        List<SlaughterList> ShopkeeperDetails = service.getListForWeighing(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
                true);
        ShopkeeperResponse shopkeeperResponse = ShopkeeperResponse.builder().slaughterListDetails(ShopkeeperDetails)
                .responseInfo(responseInfo).build();
        return new ResponseEntity<>(shopkeeperResponse, HttpStatus.OK);
    }



    
    @PostMapping("/get/trading/_list")
    public ResponseEntity<SecurityCheckResponse> getTradingLists(
            @ApiParam(value = "Lists for animal trading", required = true)
            @Valid @RequestBody GetListRequest request) {

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
            @Valid @RequestBody GetListRequest request) {

        List<SecurityCheckDetails> securityDetails = service.getListForStabling(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
        true);
        SecurityCheckResponse response = SecurityCheckResponse.builder()
                .securityCheckDetails(securityDetails)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/get/removal/_list")
    public ResponseEntity<RemovalListResponse> getRemovalLists(
            @ApiParam(value = "Lists of removed animal", required = true)
            @Valid @RequestBody GetListRequest request) {

        List<RemovalList> securityDetails = service.getListOfRemovedAnimals(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
        true);
        RemovalListResponse response = RemovalListResponse.builder()
                .securityCheckDetails(securityDetails)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/get/animalList/_dawanwala")
    public ResponseEntity<SecurityCheckResponse> getListsForDawanawala(
            @ApiParam(value = "Lists for Assigningf Dawanwala", required = true)
            @Valid @RequestBody GetListRequest request) {

        List<SecurityCheckDetails> securityDetails = service.getListForDawanwala(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
        true);
        SecurityCheckResponse response = SecurityCheckResponse.builder()
                .securityCheckDetails(securityDetails)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/get/animalList/_helkari")
    public ResponseEntity<SecurityCheckResponse> getListsForHelkari(
            @ApiParam(value = "Lists for Assigningf helkari", required = true)
            @Valid @RequestBody GetListRequest request) {

        List<SecurityCheckDetails> securityDetails = service.getListForHelkari(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
        true);
        SecurityCheckResponse response = SecurityCheckResponse.builder()
                .securityCheckDetails(securityDetails)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    









}
