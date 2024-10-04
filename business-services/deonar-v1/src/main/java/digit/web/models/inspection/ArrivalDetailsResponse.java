package digit.web.models.inspection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArrivalDetailsResponse {
    private String arrivalId;
    private int aliveAnimalCount;
    private String traderName;

}
