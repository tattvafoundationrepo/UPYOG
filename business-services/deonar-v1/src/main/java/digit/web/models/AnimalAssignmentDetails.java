package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalAssignmentDetails {

    private Long animalTypeId;
    private Integer tokenNum;
    private Long removalId;
    private Long assigneeId;
    private String assigneeMobile;
    private String assigneeLicenceNumber;
    private String currentStakeholder;
    private String assigneeName;
}