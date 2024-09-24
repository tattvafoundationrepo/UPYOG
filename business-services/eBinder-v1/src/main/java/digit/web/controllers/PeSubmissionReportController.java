package digit.web.controllers;


import digit.service.PeSubmissionReportService;
import digit.web.models.request.PeSubmissionReportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PeSubmissionReportController {

    @Autowired
    private PeSubmissionReportService service;
    @PostMapping("/submissionReport/_save")
    public ResponseEntity<String> submissionReportSave(@RequestBody PeSubmissionReportRequest request){

        try {
            service.saveSubmissionReport(request);
            return new ResponseEntity<>("PeSubmissionReport  details saved successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to save peSubmissionReport details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
