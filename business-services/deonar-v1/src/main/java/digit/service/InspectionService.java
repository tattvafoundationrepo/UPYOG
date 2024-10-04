package digit.service;

import digit.kafka.Producer;
import digit.repository.InspectionRepository;
import digit.web.models.inspection.InspectionRequest;
import digit.web.models.inspection.ArrivalDetailsResponse;
import digit.web.models.inspection.InspectionDetails;

import digit.web.models.security.SecurityCheckCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InspectionService {
    @Autowired
    private InspectionRepository repository;

    @Autowired
    private Producer producer;

    public List<InspectionDetails> getInspectionDetails(String arrivalId) {
        if (arrivalId == null) {
            throw new CustomException("INVALID_ARRIVAL_ID", "Search Arrival Id is empty");
        }
        return repository.getInspectionDetails(arrivalId);
    }

    public ArrivalDetailsResponse getArrivalResponse(SecurityCheckCriteria securityCheckCriteria) {
        if (securityCheckCriteria.getArrivalUuid() == null) {
            throw new CustomException("INVALID_ARRIVAL_ID", "Search Arrival Id is empty");
        }
        return repository.getArrivalDetails(securityCheckCriteria);
    }


    public void saveInspectionDetails(InspectionRequest inspectionRequest) {
        if (inspectionRequest == null || inspectionRequest.getInspection() == null || inspectionRequest.getInspectionIndicators() == null || inspectionRequest.getInspectionDetail() == null) {
            throw new CustomException("INVALID_DATA", "Inspection request data is null or incomplete.");
        }
        Long time = System.currentTimeMillis();
        inspectionRequest.getAnimal().setCreatedAt(time);
        inspectionRequest.getAnimal().setUpdatedAt(time);
        inspectionRequest.getAnimal().setCreatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());
        inspectionRequest.getAnimal().setUpdatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());

        inspectionRequest.getInspectionUnit().setCreatedAt(time);
        inspectionRequest.getInspectionUnit().setUpdatedAt(time);
        inspectionRequest.getInspectionUnit().setCreatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());
        inspectionRequest.getInspectionUnit().setUpdatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());

        inspectionRequest.getInspection().setCreatedAt(time);
        inspectionRequest.getInspection().setUpdatedAt(time);
        inspectionRequest.getInspection().setCreatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());
        inspectionRequest.getInspection().setUpdatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());


        inspectionRequest.getInspectionIndicators().setCreatedAt(time);
        inspectionRequest.getInspectionIndicators().setUpdatedAt(time);
        inspectionRequest.getInspectionIndicators().setCreatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());
        inspectionRequest.getInspectionIndicators().setUpdatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());

        inspectionRequest.getInspectionDetail().setCreatedAt(time);
        inspectionRequest.getInspectionDetail().setUpdatedAt(time);
        inspectionRequest.getInspectionDetail().setCreatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());
        inspectionRequest.getInspectionDetail().setUpdatedBy(inspectionRequest.getRequestInfo().getUserInfo().getUserName());



        if (inspectionRequest.getInspectionType().equalsIgnoreCase("Ante Mortem Inspection") || inspectionRequest.getInspectionType().equalsIgnoreCase("Re-Ante Mortem Inspection")) {
            producer.push("save-inspection-details", inspectionRequest);

        }
    }
}
