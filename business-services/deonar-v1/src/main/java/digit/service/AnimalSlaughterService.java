package digit.service;

import java.util.List;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.kafka.Producer;
import digit.repository.CommonRepository;
import digit.web.models.Slaughter;
import digit.web.models.SlaughterRequest;
import digit.web.models.SlaughterUnit;
import digit.web.models.SlaughterUnitRequest;

@Service
public class AnimalSlaughterService {

    @Autowired
    private Producer producer;

    @Autowired
    private CommonRepository commonRepository;

    public Slaughter saveSlaughteredAnimals(SlaughterRequest request) {

        long epochTime = System.currentTimeMillis();

        AuditDetails audit = AuditDetails.builder()
                .createdBy(request.getRequestInfo().getUserInfo().getUuid())
                .lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid())
                .createdTime(epochTime)
                .lastModifiedTime(epochTime)
                .build();

        request.setAudit(audit);
        producer.push("topic_deonar_slaughtered_animals", request);
        return request.getSlaughterDetails();

    }

    public List<SlaughterUnit> getSlaughterUnitShifts(SlaughterUnitRequest request) {

        return commonRepository.getSlaughterUnitShifts(request);

    }

}
