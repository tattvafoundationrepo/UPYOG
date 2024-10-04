package digit.service;

import digit.kafka.Producer;
import digit.repository.InspectionRepository;
import digit.web.models.inspection.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    public void saveInspectionDetails(InspectionRequest inspectionRequest) {
        if (inspectionRequest == null) {
            throw new CustomException("INVALID_DATA", "Inspection request data is null or incomplete.");
        }
        Long time = System.currentTimeMillis();
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm:ss");
        String username = inspectionRequest.getRequestInfo().getUserInfo().getUserName();

        InspectionSearchRequest request = new InspectionSearchRequest();
        ArrivalDetailsResponse ere= repository.getArrivalDetails(inspectionRequest.getArrivalId());

        Animal animal=new Animal();
        for (int animalTypeId: ere.getAnimalTyeId())
        {
            animal.setArrivalId(ere.getAId());
            animal.setAnimalType(animalTypeId);
            animal.setUpdatedBy(username);
            animal.setCreatedBy(username);
            animal.setAnimalTokenNum(inspectionRequest.getAnimalTokenNumber().getName());
            animal.setCreatedAt(time);
            animal.setUpdatedAt(time);

        }

        Inspection inspection=new Inspection();
        inspection.setArrivalId(ere.getAId());
//        inspection.setEmployeeId(inspectionRequest.getRequestInfo().getUserInfo().getUuid());
        inspection.setInspectionDate(time);
        inspection.setInspectionTime(currentTime.format(formatter));
        inspection.setCreatedAt(time);
        inspection.setUpdatedAt(time);
        inspection.setCreatedBy(username);
        inspection.setUpdatedBy(username);

        List<InspectionDetail> list = populateListOfInspectionDetail(inspectionRequest);
        request.setInspectionRequest(inspectionRequest);
        request.setInspectionDetail(list);
        request.setAnimal(animal);
        request.setInspection(inspection);

        if (inspectionRequest.getInspectionType().equalsIgnoreCase("Ante Mortem Inspection") || inspectionRequest.getInspectionType().equalsIgnoreCase("Re-Ante Mortem Inspection")) {
            producer.push("save-inspection-details", request);

        }
    }

    private List<InspectionDetail> populateListOfInspectionDetail(InspectionRequest inspectionRequest) {
        List<InspectionDetail> list = new ArrayList<>();
        Long time = System.currentTimeMillis();
        String username = inspectionRequest.getRequestInfo().getUserInfo().getUserName();
        list.add(new InspectionDetail(IndicatorName.PULSE_RATE.getId(), inspectionRequest.getPulseRate().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.EYES.getId(), inspectionRequest.getEyes().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.SPECIES.getId(), inspectionRequest.getSpecies().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.BREED.getId(), inspectionRequest.getBreed().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.PREGNANCY.getId(), inspectionRequest.getPregnancy().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.POSTURE.getId(), inspectionRequest.getPosture().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.BODY_TEMPERATURE.getId(), inspectionRequest.getBodyTemperature().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.BODY_COLOR.getId(), inspectionRequest.getBodyColor().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.APPROXIMATE_AGE.getId(), inspectionRequest.getApproximateAge().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.APPETITE.getId(), inspectionRequest.getAppetite().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.OPINION.getId(), inspectionRequest.getOpinion().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.GAIT.getId(), inspectionRequest.getGait().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.NOSTRILS.getId(), inspectionRequest.getNostrils().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.MUZZLE.getId(), inspectionRequest.getMuzzle().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.SEX.getId(), inspectionRequest.getSex().getName(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.OTHER.getId(), inspectionRequest.getOther(), time, time, username, username));
        list.add(new InspectionDetail(IndicatorName.REMARK.getId(), inspectionRequest.getRemark(), time, time, username, username));

        return list;
    }

}
