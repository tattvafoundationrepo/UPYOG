package digit.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import digit.service.AnimalAssignmentService;
import digit.web.models.AnimalAssignmentRequest;

@Controller
public class AnimalAssignmentController {
    
    @Autowired
    AnimalAssignmentService service;

    @PostMapping("/assign/animal")
    public ResponseEntity<String> aaaignAnimal(@RequestBody AnimalAssignmentRequest request){
        try {
            service.assignAnimalsToStakeholder(request);
            return new ResponseEntity<>("Animal assigned Successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("assignment failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/remove/animal")
    public ResponseEntity<String> removalAnimal(@RequestBody AnimalAssignmentRequest request){
        try {
            service.saveAnimalRemoval(request);
            return new ResponseEntity<>("Animal removed Successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("removal failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
