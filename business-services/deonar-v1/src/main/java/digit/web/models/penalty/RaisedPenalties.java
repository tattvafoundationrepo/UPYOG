package digit.web.models.penalty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RaisedPenalties {

    private Double amount;
    private String penaltyReference;
    private String unit;
    private String licenceNumber;
    private String stakeholderName;
    private String penaltyType;

}
