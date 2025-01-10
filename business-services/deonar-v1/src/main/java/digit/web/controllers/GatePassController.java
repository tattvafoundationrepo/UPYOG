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

import digit.service.GatePassService;
import digit.util.ResponseInfoFactory;
import digit.web.models.GatePassDetails;
import digit.web.models.GatePassDetailsResponse;
import digit.web.models.GatePassMapper;
import digit.web.models.GatePassRequest;
import digit.web.models.citizen.CitizenGatePassDetails;
import digit.web.models.citizen.CitizenGatePassRequest;
import digit.web.models.citizen.CitizenGatePassResponse;
import digit.web.models.citizen.CitizenGatePassSaveRequest;
import digit.web.models.citizen.CitizenGatePassSaveResponse;
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

        List<GatePassMapper> details = service.getListForGatePass(request.getCriteria());
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
        true);
        GatePassDetailsResponse response = GatePassDetailsResponse.builder()
                .details(details)
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
                    .saveDetails(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>( new GatePassDetailsResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/citizenGatePass/_get")
    public ResponseEntity<CitizenGatePassResponse> getListOfAssignedCitizens(
        @ApiParam(value = " List of Assigned Citizens ", required = true)
        @RequestBody CitizenGatePassRequest request){
            List<CitizenGatePassDetails> details = service.getListOfAssignedCitizens(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            CitizenGatePassResponse response = CitizenGatePassResponse.builder()
                    .responseInfo(responseInfo)
                    .details(details)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @PostMapping("/citizenGatePass/_save")
    public ResponseEntity<CitizenGatePassSaveResponse> saveAssignedCitizen(@RequestBody CitizenGatePassSaveRequest request){
        try {
            CitizenGatePassDetails common = service.saveCitizenGatePassDetails(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            CitizenGatePassSaveResponse response = CitizenGatePassSaveResponse.builder()
                    .responseInfo(responseInfo)
                    .details(common)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>( new CitizenGatePassSaveResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
    
    


