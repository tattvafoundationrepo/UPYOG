package digit.web.controllers;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import digit.service.AnimalSlaughterService;
import digit.util.ResponseInfoFactory;
import digit.web.models.Slaughter;
import digit.web.models.SlaughterRequest;
import digit.web.models.SlaughterResponse;

@Controller
public class AnimalSlaughterController {

    private static final String ERR_MSG = "Request Failed";

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    AnimalSlaughterService service;

    @PostMapping("/animal/slaughter/_save")
    public ResponseEntity<Object> saveAnimalSlaughtering(@RequestBody SlaughterRequest request) {
        try {
            Slaughter common = service.saveSlaughteredAnimals(request);
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            SlaughterResponse res = SlaughterResponse.builder()
                    .details(common)
                    .responseInfo(responseInfo)
                    .build();
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
