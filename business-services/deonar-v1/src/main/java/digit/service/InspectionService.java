package digit.service;

import digit.kafka.Producer;
import digit.repository.InspectionRepository;
import digit.web.models.inspection.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.google.gson.Gson;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class InspectionService {
    @Autowired
    private InspectionRepository repository;

    @Autowired
    private Producer producer;

    public ArrivalDetailsResponse getArrivalResponse(String arrivalId) {
        if (arrivalId == null) {
            throw new CustomException("INVALID_ARRIVAL_ID", "Search Arrival Id is empty");
        }
        return repository.getArrivalDetails(arrivalId);
    }

    public InspectionRequest saveInspectionDetails(InspectionRequest inspectionRequest) {
        if (inspectionRequest == null) {
            throw new CustomException("INVALID_DATA", "Inspection request data is null or incomplete.");
        }
        Long time = System.currentTimeMillis();
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String username = inspectionRequest.getRequestInfo().getUserInfo().getUserName();

        InspectionSearchRequest request = new InspectionSearchRequest();
        ArrivalDetailsResponse ere = repository.getArrivalDetails(inspectionRequest.getArrivalId());

        Inspection inspection = new Inspection();
        inspection.setArrivalId(ere.getAId());
        inspection.setEmployeeId(inspectionRequest.getRequestInfo().getUserInfo().getUuid());
        inspection.setInspectionDate(time);
        inspection.setInspectionTime(LocalTime.now().format(formatter));
        inspection.setInspectionUnitId(1);
        inspection.setCreatedAt(time);
        inspection.setUpdatedAt(time);
        inspection.setCreatedBy(username);
        inspection.setUpdatedBy(username);
        inspection.setInspectionType(inspectionRequest.getInspectionType().intValue());

        Gson gson = new Gson();
        InspectionDetail inspectionDetail = new InspectionDetail();
        inspectionDetail.setCreatedAt(time);
        inspectionDetail.setCreatedBy(username);
        if (ObjectUtils.isEmpty(inspectionRequest.getInspectionDetailsJson())
                || inspectionRequest.getInspectionDetailsJson() == null) {
            List<InspectionIndicators> details = populateListOfInspectionDetails(inspectionRequest);
            inspectionDetail.setReport(gson.toJson(details));
        } else {
            inspectionDetail.setReport(inspectionRequest.getInspectionDetailsJson());
        }
        inspectionDetail.setTokenNo(inspectionRequest.getAnimalTokenNumber());
        inspectionDetail.setUpdatedAt(time);
        inspectionDetail.setUpdatedBy(username);
        inspectionDetail.setResultRemark(inspectionRequest.getRemark());
        inspectionDetail.setAnimalTypeId(inspectionRequest.getAnimalTypeId());

        request.setInspectionRequest(inspectionRequest);
        request.setInspectionDetail(inspectionDetail);
        request.setInspection(inspection);

        producer.push("save-inspection-details", request);

        return inspectionRequest;

    }

    private List<InspectionIndicators> populateListOfInspectionDetails(InspectionRequest inspectionRequest) {
        List<InspectionIndicators> list = new ArrayList<>();
        list.add(new InspectionIndicators(IndicatorName.PULSE_RATE.getValue(), inspectionRequest.getPulseRate()));
        list.add(new InspectionIndicators(IndicatorName.EYES.getValue(), inspectionRequest.getEyes()));
        list.add(new InspectionIndicators(IndicatorName.SPECIES.getValue(), inspectionRequest.getSpecies()));
        list.add(new InspectionIndicators(IndicatorName.BREED.getValue(), inspectionRequest.getBreed()));
        list.add(new InspectionIndicators(IndicatorName.PREGNANCY.getValue(), inspectionRequest.getPregnancy()));
        list.add(new InspectionIndicators(IndicatorName.POSTURE.getValue(), inspectionRequest.getPosture()));
        list.add(new InspectionIndicators(IndicatorName.BODY_TEMPERATURE.getValue(),
                inspectionRequest.getBodyTemperature()));
        list.add(new InspectionIndicators(IndicatorName.BODY_COLOR.getValue(), inspectionRequest.getBodyColor()));
        list.add(new InspectionIndicators(IndicatorName.APPROXIMATE_AGE.getValue(),
                inspectionRequest.getApproximateAge()));
        list.add(new InspectionIndicators(IndicatorName.APPETITE.getValue(), inspectionRequest.getAppetite()));
        list.add(new InspectionIndicators(IndicatorName.OPINION.getValue(), inspectionRequest.getOpinion()));
        list.add(new InspectionIndicators(IndicatorName.GAIT.getValue(), inspectionRequest.getGait()));
        list.add(new InspectionIndicators(IndicatorName.NOSTRILS.getValue(), inspectionRequest.getNostrils()));
        list.add(new InspectionIndicators(IndicatorName.MUZZLE.getValue(), inspectionRequest.getMuzzle()));
        list.add(new InspectionIndicators(IndicatorName.SEX.getValue(), inspectionRequest.getSex()));
        list.add(new InspectionIndicators(IndicatorName.OTHER.getValue(), inspectionRequest.getOther()));
        list.add(new InspectionIndicators(IndicatorName.REMARK.getValue(), inspectionRequest.getRemark()));
        if (inspectionRequest.getSlaughterReceiptNumber() != null) {
            list.add(new InspectionIndicators(IndicatorName.SLAUGHTER_RECEIPT_NUMBER.getValue(),
                    inspectionRequest.getSlaughterReceiptNumber()));
        }
        if (inspectionRequest.getVisibleMucusMembrane() != null) {
            list.add(new InspectionIndicators(IndicatorName.VISIBLE_MUCUS_MEMBRANE.getValue(),
                    inspectionRequest.getVisibleMucusMembrane()));
        }
        if (inspectionRequest.getThoracicCavity() != null) {
            list.add(new InspectionIndicators(IndicatorName.THORACIC_CAVITY.getValue(),
                    inspectionRequest.getThoracicCavity()));
        }
        if (inspectionRequest.getAbdominalCavity() != null) {
            list.add(new InspectionIndicators(IndicatorName.ABDOMINAL_CAVITY.getValue(),
                    inspectionRequest.getAbdominalCavity()));
        }
        if (inspectionRequest.getPelvicCavity() != null) {
            list.add(new InspectionIndicators(IndicatorName.PELVIC_CAVITY.getValue(),
                    inspectionRequest.getPelvicCavity()));
        }
        if (inspectionRequest.getSpecimenCollection() != null) {
            list.add(new InspectionIndicators(IndicatorName.SPECIMEN_COLLECTION.getValue(),
                    inspectionRequest.getSpecimenCollection()));
        }
        if (inspectionRequest.getSpecialObservation() != null) {
            list.add(new InspectionIndicators(IndicatorName.SPECIAL_OBSERVATION.getValue(),
                    inspectionRequest.getSpecialObservation()));
        }

        return list;
    }

    public List<InspectionDetails> getInspectionDetails(InspectionSearchCriteria criteria) {

        Long id = repository.getArrivalId(criteria.getEntryUnitId(), criteria.getInspectionType());
        if (id != null) {
            List<InspectionDetails> details = repository.getInspectionDetails(criteria.getEntryUnitId(),
                    criteria.getInspectionType());
            return details;
        }
        List<InspectionIndicators> list = repository.getInspectionIndicatorsByType(criteria.getInspectionType());
        List<Map<String, Long>> tokens = repository.getAnimalTypeCounts(criteria.getEntryUnitId());
        Gson gson = new Gson();
        for (Map<String, Long> animal : tokens) {
            InspectionRequest request = new InspectionRequest();
            request.setRequestInfo(criteria.getRequestInfo());
            request.setArrivalId(criteria.getEntryUnitId());
            request.setInspectionDetailsJson(gson.toJson(list));
            request.setInspectionType(criteria.getInspectionType());
            request.setAnimalTokenNumber(animal.get("count"));
            request.setAnimalTypeId(animal.get("animalTypeId"));
            request.setRemark("ok");
            saveInspectionDetails(request);
        }
        List<InspectionDetails> details = repository.getInspectionDetails(criteria.getEntryUnitId(),
                criteria.getInspectionType());
        return details;
    }

}
