package digit.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.bmc.model.VerificationDetails;
import digit.repository.SchemeApplicationRepository;
import digit.web.models.SchemeApplicationSearchCriteria;
import digit.web.models.VerifierRequest;
import digit.web.models.employee.ApplicationStatus;
import digit.web.models.employee.ApplicationStatusRequest;

@Service
public class VerifierService {
    
    @Autowired
    private SchemeApplicationRepository repository;

    @Autowired
    private SchemeApplicationSearchCriteria criteria;
    
    @Autowired
    private EmployeeWorkflowService ewfs;


    public List<VerificationDetails> getApplicationDetails(VerifierRequest request){

      criteria.setMachineId(null);
      criteria.setCourseId(null); 
       criteria.setUuid(request.getRequestInfo().getUserInfo().getUuid());
       if("machine".equalsIgnoreCase(request.getType())) {
          criteria.setMachineId(request.getDetailId());
       }
       if("course".equalsIgnoreCase(request.getType())) {
          criteria.setCourseId(request.getDetailId());
       }
       criteria.setSchemeId(request.getSchemeId());

       criteria.setState(request.getAction());

       criteria.setRandomizationNumber(request.getNumber());
       criteria.setUuid(request.getRequestInfo().getUserInfo().getUuid());
       criteria.setTenantId(request.getRequestInfo().getUserInfo().getTenantId());
       List<VerificationDetails> details = repository.getApplicationForVerification(criteria);
       if (details.size() != 0) {
          if (request.getAction().contains("RANDOMIZE")) {
             updateRandomizeState("SELECTED", details, request);
             updateRandomizeState("NOTSELECTED", details, request);
             List<VerificationDetails> selectedDetails =  details.stream()
                  .filter(detail->"SELECTED".equalsIgnoreCase(detail.getSelectionCase()))
                  .collect(Collectors.toList());
             return selectedDetails;   
          }
       }

       return details;
    }

    public void updateRandomizeState(String selection, List<VerificationDetails> details, VerifierRequest request) {

       List<String> filteredList = details.stream()
             .filter(detail -> selection.equals(detail.getSelectionCase()))
             .map(VerificationDetails::getApplicationNumber)
             .collect(Collectors.toList());
       if (filteredList.size() != 0) {
          ApplicationStatus applicationStatus = ApplicationStatus.builder()
                .action(selection)
                .applicationNumbers(filteredList)
                .build();
          ApplicationStatusRequest asr = ApplicationStatusRequest.builder()
                .applicationStatus(applicationStatus)
                .requestInfo(request.getRequestInfo())
                .build();
          ewfs.updateApplicationsStatus(asr);
       }

    }

}
