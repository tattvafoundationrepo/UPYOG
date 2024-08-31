package digit.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.bmc.model.VerificationDetails;
import digit.repository.SchemeApplicationRepository;
import digit.web.models.SchemeApplicationSearchCriteria;
import digit.web.models.VerifierRequest;

@Service
public class VerifierService {
    
    @Autowired
    private SchemeApplicationRepository repository;

    @Autowired
    private SchemeApplicationSearchCriteria criteria;


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
   
       return details; 
    }

}
