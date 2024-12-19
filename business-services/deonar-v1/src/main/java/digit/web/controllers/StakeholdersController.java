package digit.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import digit.service.StakeholderService;
import digit.web.models.stakeholders.StakeholderRequest;
import digit.web.models.stakeholders.StakeholderResponse;
import digit.web.models.stakeholders.Stakeholders;

// Annotate as a REST controller
@RestController
@RequestMapping("/stakeholders")
public class StakeholdersController {

    @Autowired
    private StakeholderService stakeholderService;

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

}
