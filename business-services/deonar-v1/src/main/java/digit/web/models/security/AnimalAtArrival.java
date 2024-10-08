package digit.web.models.security;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AnimalAtArrival {
    private Long id;
    private Long arrivalId;
    private Integer animalTypeId;
    private List<Integer> count;
    private Integer tokenNum;
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
}