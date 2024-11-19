package digit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.kafka.Producer;
import digit.repository.GatePassRepository;
import digit.web.models.GatePassDetails;
import digit.web.models.GatePassRequest;
import digit.web.models.GatePassSearchCriteria;


@Service
public class GatePassService {
    

     @Autowired
    private Producer producer;

 

    @Autowired
    GatePassRepository repository;

    public GatePassDetails getListForGatePass(GatePassSearchCriteria criteria) {
         return  repository.getGatePassSlaughterInfo(criteria);
    }

    public GatePassDetails saveGatePassDetails(GatePassRequest request) {
        
        Long time  = System.currentTimeMillis();
        request.getGatePassDetails().setCreatedBy(request.getRequestInfo().getUserInfo().getId());
        request.getGatePassDetails().setLastModifiedBy(request.getRequestInfo().getUserInfo().getId());
        request.getGatePassDetails().setLastModifiedDate(time);
        request.getGatePassDetails().setCreatedDate(time);
        producer.push("save-gate-pass-details",request);
        return request.getGatePassDetails();    
    }

}
