package digit.service;

import digit.kafka.Producer;
import digit.repository.InspectionRepository;
import digit.repository.rowmapper.InspectionRowMapper;
import digit.web.models.inspection.*;
import digit.web.models.security.AnimalDetail;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;package digit.service;

import digit.kafka.Producer;
import digit.repository.InspectionRepository;
import digit.repository.rowmapper.InspectionRowMapper;
import digit.web.models.inspection.*;
import digit.web.models.security.AnimalDetail;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class InspectionService {
    @Autowired
    private InspectionRepository repository;

    @Autowired
    private Producer producer;

  

    public InspectionRequest updateInspectionDetails(InspectionRequest inspectionRequest) {
        if (inspectionRequest == null) {
            throw new CustomException("INVALID_DATA", "Inspection request data is null or incomplete.");
        }
        Long time = System.currentTimeMillis();
        String username = inspectionRequest.getRequestInfo().getUserInfo().getUserName();

        Inspection inspection = new Inspection();
        inspection.setUpdatedAt(time);
        inspection.setUpdatedBy(username);
        inspectionRequest.setInspection(inspection);
        inspectionRequest.getInspectionDetails().setReport(toCustomJson(inspectionRequest.getInspectionDetails())) ;
        producer.push("update-inspection-details", inspectionRequest);
        return inspectionRequest;

    }

        public List<InspectionDetails> getInspectionDetails(InspectionSearchCriteria criteria) throws Exception {
            List<InspectionDetails> details = new ArrayList<>();
            Long id = repository.getArrivalId(criteria.getEntryUnitId(), criteria.getInspectionType());
            if (id != null) {
               details = repository.getInspectionDetails(criteria.getEntryUnitId(),
                        criteria.getInspectionType());
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
            inspection.setInspectionType(criteria.getInspectionType().intValue());
            request.setInspection(inspection);
            producer.push("save-inspection-details", request);
            
            List<Map<String, Long>> tokens = repository.getAnimalTypeCounts(criteria.getEntryUnitId());

            for (Map<String, Long> animal : tokens) {
                InspectionDetails iDetail = new InspectionDetails();
                String animalType = repository.getAnimalType(animal.get("animalTypeId"));
                String  result = repository.getInspectionIndicatorsByType(criteria.getInspectionType(),animal.get("animalTypeId"));
                InspectionRowMapper.mapJsonToInspectionDetails(result, iDetail);
                iDetail.setArrivalId(criteria.getEntryUnitId());
                iDetail.setInspectionId(criteria.getInspectionType());
                iDetail.setAnimalTokenNumber(animal.get("count"));
                AnimalDetail animalDetail = AnimalDetail.builder()
                  .animalType(animalType)
                  .animalTypeId(animal.get("animalTypeId"))
                  .count(animal.get("count").intValue())
                  .editable(true)
                  .build();
                iDetail.setAnimalDetail(animalDetail); 
                details.add(iDetail);
            } 
            return details;
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
        fieldsToInclude.put("bodyTemp", details.getBodyTemperature());
        fieldsToInclude.put("bodyColor", details.getBodyColor());
        fieldsToInclude.put("approxAge", details.getApproximateAge());
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
    

    

}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class InspectionService {
    @Autowired
    private InspectionRepository repository;

    @Autowired
    private Producer producer;

  

    public InspectionRequest updateInspectionDetails(InspectionRequest inspectionRequest) {
        if (inspectionRequest == null) {
            throw new CustomException("INVALID_DATA", "Inspection request data is null or incomplete.");
        }
        Long time = System.currentTimeMillis();
        String username = inspectionRequest.getRequestInfo().getUserInfo().getUserName();

        Inspection inspection = new Inspection();
        inspection.setUpdatedAt(time);
        inspection.setUpdatedBy(username);
        inspectionRequest.setInspection(inspection);
        inspectionRequest.getInspectionDetails().setReport(toCustomJson(inspectionRequest.getInspectionDetails())) ;
        producer.push("update-inspection-details", inspectionRequest);
        return inspectionRequest;

    }

        public List<InspectionDetails> getInspectionDetails(InspectionSearchCriteria criteria) throws Exception {
            List<InspectionDetails> details = new ArrayList<>();
            Long id = repository.getArrivalId(criteria.getEntryUnitId(), criteria.getInspectionType());
            if (id != null) {
               details = repository.getInspectionDetails(criteria.getEntryUnitId(),
                        criteria.getInspectionType());
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
            inspection.setInspectionType(criteria.getInspectionType().intValue());
            request.setInspection(inspection);
            producer.push("save-inspection-details", request);
            
            List<Map<String, Long>> tokens = repository.getAnimalTypeCounts(criteria.getEntryUnitId());

            for (Map<String, Long> animal : tokens) {
                InspectionDetails iDetail = new InspectionDetails();
                String  result = repository.getInspectionIndicatorsByType(criteria.getInspectionType(),animal.get("animalTypeId"));
                InspectionRowMapper.mapJsonToInspectionDetails(result, iDetail);
                iDetail.setArrivalId(criteria.getEntryUnitId());
                iDetail.setInspectionId(criteria.getInspectionType());
                iDetail.setAnimalTokenNumber(animal.get("count"));
                 AnimalDetail animalDetail = AnimalDetail.builder()
                  .animalTypeId(animal.get("animalTypeId"))
                  .count(animal.get("count").intValue())
                  .editable(true)
                  .build();
                 iDetail.setAnimalDetail(animalDetail); 
                details.add(iDetail);
            } 
            return details;
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
        fieldsToInclude.put("bodyTemp", details.getBodyTemperature());
        fieldsToInclude.put("bodyColor", details.getBodyColor());
        fieldsToInclude.put("approxAge", details.getApproximateAge());
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
    

    

}
