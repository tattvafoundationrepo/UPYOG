package digit.web.models.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnimalAtArrival {
    private Long id;
    private Long arrivalId;
    private Integer animalTypeId;
    private Integer count;
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
}