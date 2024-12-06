package digit.service;

import java.util.List;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.kafka.Producer;
import digit.repository.PenaltyRepository;
import digit.util.IdgenUtil;
import digit.web.models.penalty.PenaltyRequest;
import digit.web.models.penalty.PenaltyTypeDetails;
import digit.web.models.penalty.RaisedPenalties;

@Service
public class PenaltyService {

    @Autowired
    IdgenUtil idgenUtil;

    @Autowired
    PenaltyRepository repository;

    @Autowired
    Producer producer;

    public List<PenaltyTypeDetails> getPenalties(RequestInfoWrapper request) {

        return repository.getAllPenaltyDetails();

    }

    public PenaltyRequest saveRaisedPenalty(PenaltyRequest request) {

        RequestInfo requestInfo = request.getRequestInfo();

        long epochTime = System.currentTimeMillis();
        List<String> penaltyReference = idgenUtil.getIdList(requestInfo, requestInfo.getUserInfo().getTenantId(),
                "deonar.penaltyid", "", 1);

        AuditDetails audit = AuditDetails.builder()
                .createdBy(requestInfo.getUserInfo().getUuid())
                .lastModifiedBy(requestInfo.getUserInfo().getUuid())
                .createdTime(epochTime)
                .lastModifiedTime(epochTime)
                .build();
        request.setPenaltyReference(penaltyReference.getFirst());
        request.setAuditDetails(audit);
        
        producer.push("topic_deonar_savepenalty", request);
        return request;

    }

    public List<RaisedPenalties> getListOfRaisedPenalties(RequestInfoWrapper request) {

        return repository.getRaisedPenaltyList();


    }

}
