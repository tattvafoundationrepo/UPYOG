package digit.web.models.inspection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArrivalDetailsResponse {
    private int aId;
    private List<Integer> animalTyeId;

}
