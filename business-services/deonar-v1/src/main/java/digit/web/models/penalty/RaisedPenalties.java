package digit.web.models.penalty;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RaisedPenalties {
    
    @JsonProperty("total")
    private Double amount;
    
    private String penaltyReference;
    private String unit;
    private String licenceNumber;
    private String stakeholderName;
    private String penaltyType;

}
