package digit.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import digit.kafka.Producer;
import digit.repository.InspectionRepository;
import digit.web.models.AnimalAssignment;
import digit.web.models.AnimalAssignmentRequest;
import digit.web.models.inspection.Inspection;
import digit.web.models.inspection.InspectionDetails;
import digit.web.models.inspection.InspectionRequest;
import digit.web.models.inspection.InspectionSearchCriteria;

@Service
public class InspectionService {
    @Autowired
    private InspectionRepository repository;

    @Autowired
    private Producer producer;

    private static final List<Integer> DEAD_OPINION_IDS = List.of(2, 8, 15);
    private static final List<Integer> NOT_FIT_FOR_SLAUGHTER_IDS = List.of(4, 10, 13);
    

    public InspectionRequest updateInspectionDetails(InspectionRequest inspectionRequest) {
       
       Long deadRemovalTypeId = 4l; 
       Long NFSRemovalTypeId = 5l;

        if (inspectionRequest == null) {
            throw new CustomException("INVALID_DATA", "Inspection request data is null or incomplete.");
        }
        Long time = System.currentTimeMillis();
        String username = inspectionRequest.getRequestInfo().getUserInfo().getUserName();

        Inspection inspection = new Inspection();
        inspection.setUpdatedAt(time);
        inspection.setUpdatedBy(username);
        inspectionRequest.setInspection(inspection);
        inspectionRequest.getInspectionDetails().setReport(toCustomJson(inspectionRequest.getInspectionDetails()));
        producer.push("update-inspection-details", inspectionRequest);
        Integer opinionId = inspectionRequest.getInspectionDetails().getOpinionId();
       

        if(DEAD_OPINION_IDS.contains(opinionId) || NOT_FIT_FOR_SLAUGHTER_IDS.contains(opinionId)){
            List<AnimalAssignment> assignmentList = new ArrayList<>();

            AnimalAssignmentRequest request = AnimalAssignmentRequest.builder()
            .arrivalId(inspectionRequest.getInspectionDetails().getArrivalId())
            .createdAt(time)
            .createdBy(inspectionRequest.getRequestInfo().getUserInfo().getId())
            .updatedAt(time)
            .updatedBy(inspectionRequest.getRequestInfo().getUserInfo().getId())
            .animalAssignments(new ArrayList<>()).build();
            AnimalAssignment assignment = new AnimalAssignment();
            assignment.setAnimalTypeId(inspectionRequest.getInspectionDetails().getAnimalTypeId());
            assignment.setToken(inspectionRequest.getInspectionDetails().getAnimalTokenNumber());
            if (DEAD_OPINION_IDS.contains(opinionId)){
              assignment.setDeonarRemovalType(deadRemovalTypeId);
            }
            else if(NOT_FIT_FOR_SLAUGHTER_IDS.contains(opinionId)){
                assignment.setDeonarRemovalType(NFSRemovalTypeId);
            } 
            assignmentList.add(assignment);
            request.setAnimalAssignments(assignmentList);
            producer.push("save-animal-removal", request);

        }    
        
        return inspectionRequest;

    }

    public String toCustomJson(InspectionDetails details) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        Map<String, String> fieldsToInclude = new HashMap<>();
        fieldsToInclude.put("pulseRate", details.getPulseRate());
        fieldsToInclude.put("eyes", details.getEyes());
        fieldsToInclude.put("species", details.getSpecies());
        fieldsToInclude.put("breed", details.getBreed());
        fieldsToInclude.put("pregnancy", details.getPregnancy());
        fieldsToInclude.put("posture", details.getPosture());
        fieldsToInclude.put("bodyTemp", details.getBodyTemp());
        fieldsToInclude.put("bodyColor", details.getBodyColor());
        fieldsToInclude.put("approxAge", details.getApproxAge());
        fieldsToInclude.put("appetite", details.getAppetite());
        fieldsToInclude.put("gait", details.getGait());
        fieldsToInclude.put("nostrils", details.getNostrils());
        fieldsToInclude.put("muzzle", details.getMuzzle());
        fieldsToInclude.put("sex", details.getSex());
        fieldsToInclude.put("slaughterReceiptNumber", details.getSlaughterReceiptNumber());
        fieldsToInclude.put("visibleMucusMembrane", details.getVisibleMucusMembrane());
        fieldsToInclude.put("thoracicCavity", details.getThoracicCavity());
        fieldsToInclude.put("abdominalCavity", details.getAbdominalCavity());
        fieldsToInclude.put("pelvicCavity", details.getPelvicCavity());
        fieldsToInclude.put("specimenCollection", details.getSpecimenCollection());
        fieldsToInclude.put("specialObservation", details.getSpecialObservation());

        for (Map.Entry<String, String> entry : fieldsToInclude.entrySet()) {
            if (entry.getValue() != null) {
                ObjectNode fieldNode = mapper.createObjectNode();
                fieldNode.put(entry.getKey(), entry.getValue());
                arrayNode.add(fieldNode);
            }
        }

        return arrayNode.toString();
    }



    public List<InspectionDetails> getInspectionDetails(InspectionSearchCriteria criteria) throws Exception {
        
        
        List<InspectionDetails> details = new ArrayList<>();
        String id = repository.getArrivalId(criteria.getEntryUnitId(), criteria.getInspectionType());
        if (id != null) {
            criteria.setIsInitialCheck(true);
            details = repository.getInspectionDetails(criteria);
            return details;
        }
        InspectionRequest request = new InspectionRequest();
        Long time = System.currentTimeMillis();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String username = criteria.getRequestInfo().getUserInfo().getUserName();
        Inspection inspection = new Inspection();
        inspection.setArrivalId(criteria.getEntryUnitId());
        inspection.setEmployeeId(criteria.getRequestInfo().getUserInfo().getUuid());
        inspection.setInspectionDate(time);
        inspection.setInspectionTime(LocalTime.now().format(formatter));
        inspection.setInspectionUnitId(1);
        inspection.setCreatedAt(time);
        inspection.setUpdatedAt(time);
        inspection.setCreatedBy(username);
        inspection.setUpdatedBy(username);
        inspection.setInspectionAgainst(criteria.getInspectionAgainst());
        inspection.setInspectionType(criteria.getInspectionType().intValue());
        request.setInspection(inspection);
        criteria.setIsInitialCheck(false);
        details = repository.getInspectionDetails(criteria);
       
        for(InspectionDetails saveDetails : details){
            request.setInspectionDetails(saveDetails);
            producer.push("save-inspection-details", request);

        }
       
        return details;
    }

    public void submitInspectionDetails(InspectionSearchCriteria criteria) {

        producer.push("update-Submit-flag", criteria);

    }

   









    

}
