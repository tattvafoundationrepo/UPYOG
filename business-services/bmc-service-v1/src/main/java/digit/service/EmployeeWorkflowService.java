package digit.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.models.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.bmc.model.UserSchemeApplication;
import digit.repository.SchemeApplicationRepository;
import digit.web.models.ProcessInstance;
import digit.web.models.SchemeApplication;
import digit.web.models.UserSchemeApplicationRequest;
import digit.web.models.employee.ApplicationStatusRequest;

@Service
public class EmployeeWorkflowService {

    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private UserSchemeApplicationRequest userSchemeApplicationRequest;
    
    @Autowired
    private SchemeApplicationRepository schemeApplicationRepository;


    
    public String updateApplicationsStatus(ApplicationStatusRequest request) {

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
            schemeApplication.setTenantId(applicationMap.get(application).getTenantId());
            userSchemeApplicationRequest.getSchemeApplicationList().add(schemeApplication);
        });
        try {
            workflowService.updateWorkflowStatus(userSchemeApplicationRequest);
            return "workflow updated successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "workflow updation failed";
        }

    }

}
