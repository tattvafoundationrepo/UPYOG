package digit.web.models.inspection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Animal {
    private int id;
    private Integer animalType;
    private String animalTokenNum;
    private int arrivalId;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
