package digit.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import digit.bmc.model.UserSchemeApplication;
import digit.service.BmcApplicationService;
import digit.util.ResponseInfoFactory;
import digit.web.models.ApplicationStatusResponse;
import digit.web.models.DashboardApplication;
import digit.web.models.DashboardCriteria;
import digit.web.models.DashboardCriteriaRequest;
import digit.web.models.SchemeApplication;
import digit.web.models.SchemeApplicationListResponse;
import digit.web.models.SchemeApplicationRequest;
import digit.web.models.SchemeApplicationResponse;
import digit.web.models.SchemeApplicationSearchRequest;
import digit.web.models.SchemeApplicationStatus;
import digit.web.models.UserSchemeApplicationRequest;
import digit.web.models.employee.ApplicationCountRequest;
import digit.web.models.employee.ApplicationCountResponse;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/application")
public class SchemeApplicationController {

        private final ObjectMapper objectMapper;

        private final HttpServletRequest request;

        private final BmcApplicationService schemeApplicationService;

        private UserSchemeApplication application;

        @Autowired
        private ResponseInfoFactory responseInfoFactory;

        public SchemeApplicationController(ObjectMapper objectMapper, HttpServletRequest request,
                        BmcApplicationService schemeApplicationService) {
                this.objectMapper = objectMapper;
                this.request = request;
                this.schemeApplicationService = schemeApplicationService;
        }

        @PostMapping(value = "/v1/_search")
        public ResponseEntity<SchemeApplicationResponse> SchemeApplicationSearchPost(
                        @ApiParam(value = "Details for searching Scheme Applications + RequestInfo meta data.", required = true) @Valid @RequestBody SchemeApplicationSearchRequest schemeApplicationSearchRequest) {
                List<SchemeApplication> applications = schemeApplicationService.searchSchemeApplications(
                                schemeApplicationSearchRequest.getRequestInfo(),
                                schemeApplicationSearchRequest.getSchemeApplicationSearchCriteria());
                ResponseInfo responseInfo = responseInfoFactory
                                .createResponseInfoFromRequestInfo(schemeApplicationSearchRequest.getRequestInfo(),
                                                true);
                SchemeApplicationResponse response = SchemeApplicationResponse.builder()
                                .schemeApplications(applications)
                                .responseInfo(responseInfo).build();
                return new ResponseEntity<>(response, HttpStatus.OK);
        }

        @PostMapping(value = "/v1/_rendomize")
        public ResponseEntity<SchemeApplicationResponse> schemeApplicationRandomizePost(
                        @ApiParam(value = "Details for Randomize citizen for Scheme Application(s) + RequestInfo meta data.", required = true) @Valid @RequestBody SchemeApplicationRequest schemeApplicationRequest) {

                List<UserSchemeApplication> randomizedCitizens = schemeApplicationService
                                .rendomizeCitizens(schemeApplicationRequest);
                List<SchemeApplication> schemeApplications = randomizedCitizens.stream()
                                .map(this::convertToSchemeApplication)
                                .collect(Collectors.toList());

                ResponseInfo responseInfo = responseInfoFactory
                                .createResponseInfoFromRequestInfo(schemeApplicationRequest.getRequestInfo(), true);
                SchemeApplicationResponse response = SchemeApplicationResponse.builder()
                                .schemeApplications(schemeApplications)
                                .randomizedCitizens(randomizedCitizens)
                                .responseInfo(responseInfo)
                                .build();

                return new ResponseEntity<>(response, HttpStatus.OK);
        }

        private SchemeApplication convertToSchemeApplication(UserSchemeApplication userSchemeApplication) {
                SchemeApplication schemeApplication = new SchemeApplication();
                return schemeApplication;
        }

        @PostMapping("/_save")
        public ResponseEntity<SchemeApplicationResponse> saveSchemeApplication(
                        @ApiParam(value = "Details for the new Scheme Application(s) + RequestInfo meta data.", required = true) @Valid @RequestBody UserSchemeApplicationRequest schemeApplicationRequest)
                        throws Exception {
                String message = null;
                try {
                        application = schemeApplicationService.saveApplicationDetails(schemeApplicationRequest);
                } catch (Exception e) {
                        message = e.getMessage();
                        System.out.println("Message: " + message);
                }
                ResponseInfo responseInfo = responseInfoFactory
                                .createResponseInfoFromRequestInfo(schemeApplicationRequest.getRequestInfo(), true);
                SchemeApplicationResponse response = SchemeApplicationResponse.builder()
                                .userSchemeApplication(application)
                                .message(message)
                                .responseInfo(responseInfo).build();
                return new ResponseEntity<>(response, HttpStatus.OK);

        }

        @PostMapping("/_getcount")
        public ResponseEntity<ApplicationCountResponse> countSchemeApplication(
                        @ApiParam(value = "Count scheme applications based on action", required = true) @Valid @RequestBody ApplicationCountRequest request)  {

                Map<String, Long> number =schemeApplicationService.countSchemeApplications(request);
                ResponseInfo responseInfo = responseInfoFactory
                                .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
                ApplicationCountResponse response = ApplicationCountResponse.builder()
                                .count(number)
                                .responseInfo(responseInfo).build();

                return new ResponseEntity<>(response, HttpStatus.OK);
                 
        }

        @PostMapping("/_status")
        public ResponseEntity<ApplicationStatusResponse> getSchemeApplicationStatus(
                        @ApiParam(value = "get scheme applications based on user", required = true) @Valid @RequestBody RequestInfoWrapper request)  {

               List<SchemeApplicationStatus> applicationStatusList =schemeApplicationService.getAllSchemeApplicationsOfUser(request.getRequestInfo());
                ResponseInfo responseInfo = responseInfoFactory
                                .createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
               ApplicationStatusResponse response = ApplicationStatusResponse.builder()
                                .list(applicationStatusList)
                                .responseInfo(responseInfo).build();
                return new ResponseEntity<>(response, HttpStatus.OK);
                 
        }
        

        @PostMapping("/_getAll")
        public ResponseEntity<SchemeApplicationListResponse> getAllSchemeApplications(
                @ApiParam(value = "Request information for fetching scheme applications", required = true) @Valid @RequestBody DashboardCriteriaRequest dashboardCriteriaRequest) {
                List<DashboardApplication> applications = new ArrayList<>();
                String message = null;
                try {

                        log.info("Entering the try block");
                        DashboardCriteria searchCriteria = dashboardCriteriaRequest.getDashboardCriteria();
        
                        if(searchCriteria == null){
                                searchCriteria = new DashboardCriteria();
                        }
                        applications = schemeApplicationService.getAllApplications(
                        dashboardCriteriaRequest.getRequestInfo(),
                        searchCriteria
                );
                        log.info("Exiting the try block");
                } catch (Exception e) {
                        e.printStackTrace();
                        message = e.getMessage();
                        System.out.println("Error while fetching applications: " + message);
                }

                ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(
                        dashboardCriteriaRequest.getRequestInfo(), true);

                SchemeApplicationListResponse response = SchemeApplicationListResponse.builder()
                        .applications(applications)
                        .message(message)
                        .responseInfo(responseInfo)
                        .build();

                return new ResponseEntity<>(response, HttpStatus.OK);
        }



}
