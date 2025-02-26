package digit.web.models;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnimalAssignmentRequest {
    
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("animalAssignments")
    private List<AnimalAssignment> animalAssignments;
   
    private List<Assignments> assignments;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private String arrivalId;


   

}
