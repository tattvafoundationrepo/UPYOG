package digit.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.kafka.Producer;
import digit.web.models.AnimalAssignment;
import digit.web.models.AnimalAssignmentRequest;


@Service
public class AnimalAssignmentService {

     
     @Autowired
     private Producer producer;

     public void assignAnimalsToStakeholder(AnimalAssignmentRequest request) {
       
        for(AnimalAssignment animal:request.getAnimalAssignments()){
            Long time = System.currentTimeMillis();
            animal.setCreatedAt(time);
            animal.setCreatedBy(request.getRequestInfo().getUserInfo().getId());
            animal.setUpdatedAt(time);
            animal.setUpdatedBy(request.getRequestInfo().getUserInfo().getId());
        }
        producer.push("save-animal-assignment", request);

    }

    public void saveAnimalRemoval(AnimalAssignmentRequest request){

        for(AnimalAssignment animal:request.getAnimalAssignments()){
            Long time = System.currentTimeMillis();
            animal.setCreatedAt(time);
            animal.setCreatedBy(request.getRequestInfo().getUserInfo().getId());
            animal.setUpdatedAt(time);
            animal.setUpdatedBy(request.getRequestInfo().getUserInfo().getId());
        }
        producer.push("save-animal-removal", request);

    }


}
