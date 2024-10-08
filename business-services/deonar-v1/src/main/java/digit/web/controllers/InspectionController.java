package digit.web.controllers;

import digit.service.InspectionService;
import digit.util.ResponseInfoFactory;
import digit.web.models.inspection.*;
import digit.web.models.security.SecurityCheckRequest;
import io.swagger.annotations.ApiParam;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

@Controller
public class InspectionController {
    @Autowired
    private InspectionService inspectionService;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @PostMapping("/inspection/ _search")

    public ResponseEntity<InspectionResponse> getInspectionOnArrival(
            @ApiParam(value = "Details for inspection ", required = true)
            @Valid @RequestBody InspectionCheckRequest inspectionCheckRequest) {

        List<InspectionDetails> details = inspectionService.getInspectionDetails(inspectionCheckRequest.getArrivalId());
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(inspectionCheckRequest.getRequestInfo(), true);
        InspectionResponse response = InspectionResponse.builder()
                .inspectionDetails(details)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/arrival/_search")

    public ResponseEntity<ArrivalResponse> getArrivalResponseOnArrival(
            @ApiParam(value = "Details for inspection ", required = true)
            @Valid @RequestBody SecurityCheckRequest request
            ){
      ArrivalDetailsResponse response=  inspectionService.getArrivalResponse(request.getSecurityCheckCriteria().getArrivalUuid());
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
      ArrivalResponse arrivalResponse=    ArrivalResponse.builder().arrivalDetailsResponse(response).responseInfo(responseInfo).message("fetch arrival details successfully").build();
         return  new ResponseEntity<>(arrivalResponse, HttpStatus.OK);
    }


    @PostMapping("/inspection/save")

    public ResponseEntity<InspectionRequest> saveInspectionOnArrival(@RequestBody InspectionRequest inspectionRequest){
        try {
            InspectionRequest request= inspectionService.saveInspectionDetails(inspectionRequest);
            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new InspectionRequest(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
