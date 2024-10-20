package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnimalAssignment {
    
    private Long animalTypeId;
    private Long token;
    private Long assignedStakeholder;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Long deonarRemovalType;

}
