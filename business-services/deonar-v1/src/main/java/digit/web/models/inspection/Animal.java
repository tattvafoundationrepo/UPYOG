package digit.web.models.inspection;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Animal {
    private int id;
    private Integer animalType;
    private String animalTokenNum;
    private String status;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
