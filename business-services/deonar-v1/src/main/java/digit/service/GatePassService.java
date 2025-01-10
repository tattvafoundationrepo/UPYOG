package digit.service;

import java.util.List;

import org.egov.common.contract.models.AuditDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import digit.kafka.Producer;
import digit.repository.GatePassRepository;
import digit.util.IdgenUtil;
import digit.web.models.GatePassDetails;
import digit.web.models.GatePassMapper;
import digit.web.models.GatePassRequest;
import digit.web.models.GatePassSearchCriteria;
import digit.web.models.citizen.CitizenGatePassDetails;
import digit.web.models.citizen.CitizenGatePassRequest;
import digit.web.models.citizen.CitizenGatePassSaveRequest;


@Service
public class GatePassService {
    

     @Autowired
    private Producer producer;
    
    @Autowired
    private Gson gson;

    @Autowired
    private IdgenUtil idgenUtil;

    @Autowired
    GatePassRepository repository;

    public List<GatePassMapper> getListForGatePass(GatePassSearchCriteria criteria) {
         return  repository.getGatePassSlaughterInfo(criteria);
    }

    public GatePassDetails saveGatePassDetails(GatePassRequest request) {
        
        List<String> referenceList =idgenUtil.getIdList(request.getRequestInfo(), request.getRequestInfo().getUserInfo().getTenantId(), "deonar.gatepassid", null, 1);
        Long time  = System.currentTimeMillis();
        AuditDetails audit = AuditDetails.builder()
                .createdBy(request.getRequestInfo().getUserInfo().getUuid())
                .lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid())
                .createdTime(time)
                .lastModifiedTime(time)
                .build();
        request.getGatePassDetails().setAuditDetails(audit);
        request.getGatePassDetails().setGatePassReference(referenceList.getFirst());
        request.getGatePassDetails().setJsonAnimalDetails(gson.toJson(request.getGatePassDetails().getAnimalDetails()));
        producer.push("save-gate-pass-details",request);
        return request.getGatePassDetails();    
    }

    public List<CitizenGatePassDetails> getListOfAssignedCitizens(CitizenGatePassRequest request){
        return repository.getListOfAssignedCitizens(request);
    }

    public CitizenGatePassDetails saveCitizenGatePassDetails(CitizenGatePassSaveRequest request){
        List<String> referenceList =idgenUtil.getIdList(request.getRequestInfo(), request.getRequestInfo().getUserInfo().getTenantId(), "deonar.gatepassid", null, 1);
        Long time  = System.currentTimeMillis();
        AuditDetails audit = AuditDetails.builder()
                .createdBy(request.getRequestInfo().getUserInfo().getUuid())
                .lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid())
                .createdTime(time)
                .lastModifiedTime(time)
                .build();
        request.getCitizenGatePassDetails().setAuditDetails(audit);
        request.getCitizenGatePassDetails().setGatePassReference(referenceList.getFirst());
        request.getCitizenGatePassDetails().setJsonAnimalDetails(gson.toJson(request.getCitizenGatePassDetails().getCitizenAnimalDetails()));
        producer.push("save-citizen-gate-pass-details", request);
        return request.getCitizenGatePassDetails();
    }

}
