package digit.web.controllers;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import digit.service.CommonService;
import digit.util.ResponseInfoFactory;
import digit.web.models.common.CommonDetails;
import digit.web.models.common.CommonRequest;
import digit.web.models.common.CommonResponse;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-06-05T23:24:36.608+05:30")

@Controller
@RequestMapping("")
public class CommonApiController {

   
    @Autowired
    private CommonService commonService;
    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    
    @PostMapping("/common/_get")
    public ResponseEntity<CommonResponse>registrationSearchPost(
            @ApiParam(value = "Common Details", required = true) 
            @Valid @RequestBody CommonRequest commonSearchRequest) {
        List<CommonDetails> common = commonService.getcommon(
            commonSearchRequest.getRequestInfo(),
            commonSearchRequest.getCommonSearchCriteria());
        ResponseInfo responseInfo = responseInfoFactory
                .createResponseInfoFromRequestInfo(commonSearchRequest.getRequestInfo(), true);
                CommonResponse res = CommonResponse.builder()
                .commonDetails(common).responseInfo(responseInfo).build();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/stakeholder/_get")
    public ResponseEntity<CommonResponse>stakeholderList(
            @ApiParam(value = "List of Stakeholder", required = true) 
            @Valid @RequestBody CommonRequest commonSearchRequest) {
        List<CommonDetails> common = commonService.getStakeholder(
            commonSearchRequest.getRequestInfo(),
            commonSearchRequest.getCommonSearchCriteria());
        ResponseInfo responseInfo = responseInfoFactory
                .createResponseInfoFromRequestInfo(commonSearchRequest.getRequestInfo(), true);
                CommonResponse res = CommonResponse.builder()
                .commonDetails(common).responseInfo(responseInfo).build();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
