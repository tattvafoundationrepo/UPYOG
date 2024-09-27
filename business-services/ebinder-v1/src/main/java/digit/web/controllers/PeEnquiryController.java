package digit.web.controllers;

import digit.service.PeEnquiryService;
import digit.util.ResponseInfoFactory;
import digit.web.models.GetPeRequest;
import digit.web.models.PeEnquiryResponse;
import digit.web.models.PeEnquiryResponseWrapper;
import digit.web.models.peprocess.ActionComment;
import digit.web.models.peprocess.ActionComment1;
import digit.web.models.peprocess.PeEnquiryRecord;
import digit.web.models.peprocess.PeProcessRequest;
import digit.web.models.peprocess.ProcessApplicationInfo;
import digit.web.models.peprocess.TestCard;
import digit.web.models.request.CeRequest;
import digit.web.models.request.PeEnquiryRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class PeEnquiryController {

    @Autowired
    private PeEnquiryService service;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/peEnquiry/_save")
    public ResponseInfo savePeEnquiry(@RequestBody PeEnquiryRequest peEnquiryRequest) {
        try {
            service.savePeDetails(peEnquiryRequest);
            ResponseInfo responseInfo = responseInfoFactory
            .createResponseInfoFromRequestInfo(peEnquiryRequest.getRequestInfo(), true);
            return responseInfo;
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory
            .createResponseInfoFromRequestInfo(peEnquiryRequest.getRequestInfo(),false);
            return responseInfo;
        }
    }

    @PostMapping("/ceList/_save")
    public ResponseInfo saveCeList(@RequestBody CeRequest ceRequest) {
       
        try {
            service.saveCeList(ceRequest);
            ResponseInfo responseInfo = responseInfoFactory
            .createResponseInfoFromRequestInfo(ceRequest.getRequestInfo(), true);
            return responseInfo;
        } catch (Exception e) {
            ResponseInfo responseInfo = responseInfoFactory
            .createResponseInfoFromRequestInfo(ceRequest.getRequestInfo(),false);
            return responseInfo;
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

    @PostMapping("/peEnquiry/_process")
    public ResponseEntity<ResponseInfo> processPeEnquiry(@RequestBody PeProcessRequest request) {
        Map<String, Object> data = request.getData();
        ProcessApplicationInfo applicationInfo = null;
        List<PeEnquiryRecord> records = new ArrayList<>();
        if (data.containsKey("application")){
           applicationInfo =  objectMapper.convertValue(data.get("application"), ProcessApplicationInfo.class);
        }
        if (data.containsKey("TestCard")) {
            TestCard testCard = objectMapper.convertValue(data.get("TestCard"), TestCard.class);
            PeEnquiryRecord record = PeEnquiryRecord.builder()
            .comment(testCard.getComments1())
            .dates(testCard.getName())
            .build();
            records.add(record);
            ActionComment actionComment = objectMapper.convertValue(data.get("ActionComment"), ActionComment.class);

        } if (data.containsKey("ActionComment1")) {
            ActionComment1 actionComment1 = objectMapper.convertValue(data.get("ActionComment1"), ActionComment1.class);
            PeEnquiryRecord record = PeEnquiryRecord.builder()
            .comment(actionComment1.getComments())
            .dates(actionComment1.getValue())
            .build();
            records.add(record);
        }
        if( records.size()!= 0 && applicationInfo != null ){
            service.savePeEnqRecords(applicationInfo,records,request.getRequestInfo());
        }
        ResponseInfo responseInfo = responseInfoFactory
        .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
       
        return new ResponseEntity<>(responseInfo, HttpStatus.OK);
    }

}
