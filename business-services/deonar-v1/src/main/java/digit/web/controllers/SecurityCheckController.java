package digit.web.controllers;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import digit.service.SecurityCheckService;
import digit.util.ResponseInfoFactory;
import digit.web.models.security.SecurityCheckRequest;
import digit.web.models.security.SecurityCheckResponse;
import digit.web.models.security.ArrivalRequest;
import digit.web.models.security.SecurityCheckDetails;
import io.swagger.annotations.ApiParam;

import javax.validation.Valid;
import java.util.List;

@RestController
public class SecurityCheckController {

    @Autowired
    private SecurityCheckService securityCheckService;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @PostMapping("/securityCheck/_search")
    public ResponseEntity<SecurityCheckResponse> searchSecurityCheck(
            @ApiParam(value = "Details for Security Check", required = true)
            @Valid @RequestBody SecurityCheckRequest securityCheckRequest) {

        List<SecurityCheckDetails> securityDetails = securityCheckService.getSecurityDetails(securityCheckRequest.getSecurityCheckCriteria());
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(securityCheckRequest.getRequestInfo(), true);
        SecurityCheckResponse response = SecurityCheckResponse.builder()
                .securityCheckDetails(securityDetails)
                .responseInfo(responseInfo)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/securityCheck/_save")
    public ResponseEntity<String> saveSecurityCheckDetails(@RequestBody ArrivalRequest arrivalRequest) {
        try {
            securityCheckService.saveArrivalDetails(arrivalRequest);
            return new ResponseEntity<>("Security check details saved successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to save security check details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}