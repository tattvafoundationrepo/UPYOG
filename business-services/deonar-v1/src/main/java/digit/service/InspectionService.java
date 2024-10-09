package digit.service;

import digit.kafka.Producer;
import digit.repository.InspectionRepository;
import digit.web.models.inspection.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    public ArrivalDetailsResponse getArrivalResponse(String  arrivalId) {
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm:ss");
        String username = inspectionRequest.getRequestInfo().getUserInfo().getUserName();

        InspectionSearchRequest request = new InspectionSearchRequest();
        ArrivalDetailsResponse ere= repository.getArrivalDetails(inspectionRequest.getArrivalId());

        Inspection inspection=new Inspection();
        inspection.setArrivalId(ere.getAId());
        inspection.setEmployeeId(inspectionRequest.getRequestInfo().getUserInfo().getUuid());
        inspection.setInspectionDate(time);
        inspection.setInspectionTime(currentTime.format(formatter));
        inspection.setInspectionUnitId(1);
        inspection.setCreatedAt(time);
        inspection.setUpdatedAt(time);
        inspection.setCreatedBy(username);
        inspection.setUpdatedBy(username);
        inspection.setInspectionType(1);


        List<InspectionIndicators> details = populateListOfInspectionDetails(inspectionRequest);
        Gson gson = new Gson();
        InspectionDetail inspectionDetail = new InspectionDetail();
        inspectionDetail.setCreatedAt(time);
        inspectionDetail.setCreatedBy(username);
        inspectionDetail.setReport(gson.toJson(details));
        inspectionDetail.setTokenNo(inspectionRequest.getAnimalTokenNumber());
        inspectionDetail.setUpdatedAt(time);
        inspectionDetail.setUpdatedBy(username);
        inspectionDetail.setResultRemark(inspectionRequest.getRemark());
        inspectionDetail.setAnimalTypeId(inspectionRequest.getAnimalTypeId());

        request.setInspectionRequest(inspectionRequest);
        request.setInspectionDetail(inspectionDetail);
        request.setInspection(inspection);

        if (inspectionRequest.getInspectionType().equalsIgnoreCase("Ante Mortem Inspection") || inspectionRequest.getInspectionType().equalsIgnoreCase("Re-Ante Mortem Inspection")) {
            producer.push("save-inspection-details", request);
          
        }
        return inspectionRequest;

    }

    private List<InspectionIndicators> populateListOfInspectionDetails(InspectionRequest inspectionRequest) {
        List<InspectionIndicators> list = new ArrayList<>();
        list.add(new InspectionIndicators(IndicatorName.PULSE_RATE.getId(), inspectionRequest.getPulseRate().getName()));
        list.add(new InspectionIndicators(IndicatorName.EYES.getId(), inspectionRequest.getEyes().getName()));
        list.add(new InspectionIndicators(IndicatorName.SPECIES.getId(), inspectionRequest.getSpecies().getName()));
        list.add(new InspectionIndicators(IndicatorName.BREED.getId(), inspectionRequest.getBreed().getName()));
        list.add(new InspectionIndicators(IndicatorName.PREGNANCY.getId(), inspectionRequest.getPregnancy().getName()));
        list.add(new InspectionIndicators(IndicatorName.POSTURE.getId(), inspectionRequest.getPosture().getName()));
        list.add(new InspectionIndicators(IndicatorName.BODY_TEMPERATURE.getId(), inspectionRequest.getBodyTemperature().getName()));
        list.add(new InspectionIndicators(IndicatorName.BODY_COLOR.getId(), inspectionRequest.getBodyColor().getName()));
        list.add(new InspectionIndicators(IndicatorName.APPROXIMATE_AGE.getId(), inspectionRequest.getApproximateAge().getName()));
        list.add(new InspectionIndicators(IndicatorName.APPETITE.getId(), inspectionRequest.getAppetite().getName()));
        list.add(new InspectionIndicators(IndicatorName.OPINION.getId(), inspectionRequest.getOpinion().getName()));
        list.add(new InspectionIndicators(IndicatorName.GAIT.getId(), inspectionRequest.getGait().getName()));
        list.add(new InspectionIndicators(IndicatorName.NOSTRILS.getId(), inspectionRequest.getNostrils().getName()));
        list.add(new InspectionIndicators(IndicatorName.MUZZLE.getId(), inspectionRequest.getMuzzle().getName()));
        list.add(new InspectionIndicators(IndicatorName.SEX.getId(), inspectionRequest.getSex().getName()));
        list.add(new InspectionIndicators(IndicatorName.OTHER.getId(), inspectionRequest.getOther()));
        list.add(new InspectionIndicators(IndicatorName.REMARK.getId(), inspectionRequest.getRemark()));

        return list;
    }


}
