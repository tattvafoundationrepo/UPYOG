package digit.web.controllers;

import digit.service.PeEnquiryService;
import digit.util.ResponseInfoFactory;
import digit.web.models.GetPeRequest;
import digit.web.models.PeEnquiryResponse;
import digit.web.models.PeEnquiryResponseWrapper;
import digit.web.models.request.CeRequest;
import digit.web.models.request.PeEnquiryRequest;

import java.util.List;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PeEnquiryController {

    @Autowired
    private PeEnquiryService service;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @PostMapping("/peEnquiry/_save")
    public ResponseEntity<?> savePeEnquiry(@RequestBody PeEnquiryRequest peEnquiryRequest) {
        try {
            service.savePeDetails(peEnquiryRequest);
            return new ResponseEntity<>("PeEnquiry  details saved successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to save peEnquiry details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/ceList/_save")
    public ResponseEntity<String> saveCeList(@RequestBody CeRequest ceRequest) {

        try {
            service.saveCeList(ceRequest);
            return new ResponseEntity<>("Culprit Employee  saved successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to save culprit employee  details: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/peEnquiry/_get")
    public ResponseEntity<PeEnquiryResponseWrapper> getPeEnquiry(@RequestBody GetPeRequest peEnquiryRequest) {

        List<PeEnquiryResponse> list = service.getPeDetails(peEnquiryRequest);
        ResponseInfo responseInfo = responseInfoFactory
                .createResponseInfoFromRequestInfo(peEnquiryRequest.getRequestInfo(), true);
        PeEnquiryResponseWrapper res = PeEnquiryResponseWrapper.builder()
                .enquiryList(list).responseInfo(responseInfo).build();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
