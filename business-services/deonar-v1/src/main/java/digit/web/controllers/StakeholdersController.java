package digit.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import digit.service.StakeholderService;
import digit.util.ResponseInfoFactory;
import digit.web.models.stakeholders.StakeholderCheckCriteria;
import digit.web.models.stakeholders.StakeholderCheckDetails;
import digit.web.models.stakeholders.StakeholderCheckRequest;
import digit.web.models.stakeholders.StakeholderCheckResponse;
import digit.web.models.stakeholders.StakeholderRequest;
import digit.web.models.stakeholders.StakeholderResponse;
import digit.web.models.stakeholders.Stakeholders;

// Annotate as a REST controller
@RestController
@RequestMapping("/stakeholders")
public class StakeholdersController {

    @Autowired
    private StakeholderService stakeholderService;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    // POST endpoint for saving stakeholder details
    @PostMapping("/_save")
    public ResponseEntity<StakeholderResponse> saveStakeholderDetails(
            @RequestBody StakeholderRequest stakeholderRequest) {

        Stakeholders stakeholderDetails = null;
        StakeholderResponse response = new StakeholderResponse();

        try {

            stakeholderDetails = stakeholderRequest.getStakeholders();

            stakeholderService.saveStakeholderDetails(stakeholderRequest);

            // Build the response object
            response = StakeholderResponse.builder()
                    .stakeholderName(stakeholderDetails.getStakeholderName())
                    .address1(stakeholderDetails.getAddress1())
                    .contactNumber(stakeholderDetails.getContactNumber())
                    .email(stakeholderDetails.getEmail())
                    .createdAt(stakeholderDetails.getCreatedAt())
                    .build();
            // Return the response with HTTP status OK
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            // Log the error and return HTTP INTERNAL SERVER ERROR
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/_getAll")
    public ResponseEntity<StakeholderCheckResponse> getStakeholderDetails( @RequestBody StakeholderCheckRequest stakeholderCheckRequest){
        List<StakeholderCheckDetails> stakeholderDetails = new ArrayList<>();
        String message = null;
        try {
            StakeholderCheckCriteria stakeholderCheckCriteria = stakeholderCheckRequest.getStakeholderCheckCriteria();
            if(stakeholderCheckCriteria == null){
                stakeholderCheckCriteria = new StakeholderCheckCriteria();
            }
            stakeholderDetails = stakeholderService
                                .getStakeholderDetails(stakeholderCheckRequest.getRequestInfo(), stakeholderCheckCriteria);
        } catch (Exception e) {
                        e.printStackTrace();
                        message = e.getMessage();
                        System.out.println("Error while fetching stakeholders: " + message);
        }
        ResponseInfo responseInfo = responseInfoFactory
                                        .createResponseInfoFromRequestInfo(stakeholderCheckRequest.getRequestInfo(),
                                                        true);
        StakeholderCheckResponse response = StakeholderCheckResponse.builder()
                                        .stakeholderCheckDetails(stakeholderDetails)
                                        .message(message)
                                        .responseInfo(responseInfo)
                                        .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
// /stakeholders/_get