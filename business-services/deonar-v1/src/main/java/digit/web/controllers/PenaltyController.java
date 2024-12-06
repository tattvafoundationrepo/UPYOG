package digit.web.controllers;

import java.util.List;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import digit.service.PenaltyService;
import digit.util.ResponseInfoFactory;
import digit.web.models.penalty.PenaltyRequest;
import digit.web.models.penalty.PenaltyResponse;
import digit.web.models.penalty.PenaltyTypeDetails;
import digit.web.models.penalty.RaisedPenalties;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;

@RestController
public class PenaltyController {

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private PenaltyService service;

    @PostMapping("/get/_penalties")
    public ResponseEntity<PenaltyResponse> getPenalties(
            @ApiParam(value = "Details for penalties fetch request", required = true) @Valid @RequestBody RequestInfoWrapper request) {

        List<PenaltyTypeDetails> penalties = service.getPenalties(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
                true);

        PenaltyResponse penaltyResponse = PenaltyResponse.builder()
                .details(penalties)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(penaltyResponse, HttpStatus.OK);
    }

    @PostMapping("/penalty/_save")
    public ResponseEntity<Object> saveRaisedPenalty(@RequestBody PenaltyRequest request) {
        try {
            service.saveRaisedPenalty(request);
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
                    true);
            PenaltyResponse response = PenaltyResponse.builder()
                    .message("Penalty Raised Successfully")
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error occurred while saving penalty.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/penalty/list/_get")
    public ResponseEntity<PenaltyResponse> getPenaltiesForCollection(
            @ApiParam(value = "fetch list for penalty fee collection", required = true) @Valid @RequestBody RequestInfoWrapper request) {

        List<RaisedPenalties> penalties = service.getListOfRaisedPenalties(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(),
                true);
        PenaltyResponse penaltyResponse = PenaltyResponse.builder()
                .list(penalties)
                .responseInfo(responseInfo)
                .build();
        return new ResponseEntity<>(penaltyResponse, HttpStatus.OK);
    }

}
