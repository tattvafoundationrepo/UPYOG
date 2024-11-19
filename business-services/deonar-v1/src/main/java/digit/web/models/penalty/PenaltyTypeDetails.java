package digit.web.models.penalty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyTypeDetails {

   private Long  id;
    private String penaltyType;
    private Boolean perUnit;
   private Double  feeAmount;

}
