package digit.service;

import java.util.List;

import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.kafka.Producer;
import digit.repository.CommonRepository;
import digit.repository.SlaughterRepository;
import digit.web.SlaughterSearchCriteria;
import digit.web.models.Slaughter;
import digit.web.models.SlaughterList;
import digit.web.models.SlaughterRequest;
import digit.web.models.SlaughterUnit;
import digit.web.models.SlaughterUnitRequest;
import digit.web.models.security.AnimalDetail;

@Service
public class AnimalSlaughterService {

    @Autowired
    private Producer producer;

    @Autowired
    private CommonRepository commonRepository;

    @Autowired
    private SlaughterRepository slaughterRepository;

    public Slaughter saveSlaughteredAnimals(SlaughterRequest request) {

        long epochTime = System.currentTimeMillis();

        AuditDetails audit = AuditDetails.builder()
                .createdBy(request.getRequestInfo().getUserInfo().getUuid())
                .lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid())
                .createdTime(epochTime)
                .lastModifiedTime(epochTime)
                .build();
        for (AnimalDetail details : request.getSlaughterDetails().getDetails()) {
            request.setAudit(audit);
            request.getSlaughterDetails().setAnimalTypeId(details.getAnimalTypeId().intValue());
            request.getSlaughterDetails().setToken(details.getCount());
            producer.push("topic_deonar_slaughtered_animals", request);
        }
        return request.getSlaughterDetails();

    }

    public List<SlaughterUnit> getSlaughterUnitShifts(SlaughterUnitRequest request) {

        return commonRepository.getSlaughterUnitShifts(request);

    }

    public List<SlaughterList> getSlaughterListEmergency(RequestInfoWrapper request) {
        SlaughterSearchCriteria criteria = SlaughterSearchCriteria.builder().slaughterUnitType("emergency").build();
        return slaughterRepository.getSlaughterListDetails(criteria);

    }

    public List<SlaughterList> getSlaughterListNormal(RequestInfoWrapper request) {
        SlaughterSearchCriteria criteria = SlaughterSearchCriteria.builder().slaughterUnitType("normal").build();
        return slaughterRepository.getSlaughterListDetails(criteria);

    }

    public List<SlaughterList> getSlaughterListExport(RequestInfoWrapper request) {
        SlaughterSearchCriteria criteria = SlaughterSearchCriteria.builder().slaughterUnitType("export").build();
        return slaughterRepository.getSlaughterListDetails(criteria);

    }

}
