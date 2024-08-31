package digit.service;

import java.util.ArrayList;

import java.util.Map;

import org.egov.common.contract.models.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.bmc.model.UserSchemeApplication;
import digit.kafka.Producer;
import digit.repository.SchemeApplicationRepository;

import digit.web.models.SchemeApplication;
import digit.web.models.UserSchemeApplicationRequest;
import digit.web.models.employee.ApplicationStatusRequest;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class EmployeeWorkflowService {

    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private UserSchemeApplicationRequest userSchemeApplicationRequest;
    
    @Autowired
    private SchemeApplicationRepository schemeApplicationRepository;


    @Autowired
    private Producer producer;
    
    public String updateApplicationsStatus(ApplicationStatusRequest request) {

        log.info("Received Requesttt  :"+request.toString());
        userSchemeApplicationRequest.setRequestInfo(request.getRequestInfo());
        userSchemeApplicationRequest.setSchemeApplicationList(new ArrayList<SchemeApplication>());

        Map<String, UserSchemeApplication> applicationMap = schemeApplicationRepository
                .getApplicationsByApplicationNumbers(request.getApplicationStatus().getApplicationNumbers());

        Workflow workflow = new Workflow();
        workflow.setAction(request.getApplicationStatus().getAction());
        workflow.setComments(request.getApplicationStatus().getComment());
        request.getApplicationStatus().getApplicationNumbers().forEach(application -> {
            SchemeApplication schemeApplication = new SchemeApplication();
            schemeApplication.setApplicationNumber(application);
            schemeApplication.setWorkflow(workflow);
            schemeApplication.setTenantId(request.getRequestInfo().getUserInfo().getTenantId());
            userSchemeApplicationRequest.getSchemeApplicationList().add(schemeApplication);
            UserSchemeApplication usa = new UserSchemeApplication();
            usa = applicationMap.get(application);
            usa.setModifiedOn(System.currentTimeMillis());
            userSchemeApplicationRequest.setUserSchemeApplication(updateUserSchemeApplication(usa,request.getApplicationStatus().getAction()));
            producer.push("update-user-scheme-application", userSchemeApplicationRequest);
        });
        try {
            workflowService.updateWorkflowStatus(userSchemeApplicationRequest);
            return "workflow updated successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "workflow updation failed";
        }

    }
    public UserSchemeApplication updateUserSchemeApplication(UserSchemeApplication application,String action){
        
        switch (action) {
            case "VERIFY":
                 application.setVerificationStatus(true);
                break;
            case "APPROVE":
                 application.setFinalApproval(true);
                break;
            case "RANDOMIZE":
                 application.setRandomSelection(true);
                break;
            default:
                break;
        }
        return application;
    }

}
