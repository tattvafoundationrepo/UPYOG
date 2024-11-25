package digit.web.controllers;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import digit.service.GatePassService;
import digit.util.ResponseInfoFactory;
import digit.web.models.GatePassDetails;
import digit.web.models.GatePassDetailsResponse;
import digit.web.models.GatePassRequest;

import io.swagger.annotations.ApiParam;

@Controller
public class GatePassController {

     @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private GatePassService service;



     @PostMapping("/gatePass/_search")
    public ResponseEntity<GatePassDetailsResponse> getListsForHelkari(
            @ApiParam(value = " Weight lists of slaughter animals ", required = true)
            @Valid @RequestBody GatePassRequest request) {

        GatePassDetails securityDetails = service.getListForGatePass(request.getCriteria());
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
        true);
        GatePassDetailsResponse response = GatePassDetailsResponse.builder()
                .details(securityDetails)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


     @PostMapping("/gatePass/_save")
    public ResponseEntity<GatePassDetailsResponse> save(@RequestBody GatePassRequest request) {
        try {
            GatePassDetails common = service.saveGatePassDetails(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
                    GatePassDetailsResponse response = GatePassDetailsResponse.builder()
                    .details(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>( new GatePassDetailsResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
