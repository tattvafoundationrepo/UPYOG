package digit.web.models.collection;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RemovalFee {

    
    private String arrivalid;
    private List<Details> details;
    private Long stakeholderId;
    private String liceneceNumber;
    private Float total;
   

   

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class Details {
        
        private String animal;
        private Long animalTypeId;
        private Integer count;
        private Float fee;
        private Double totalFee;
        private List<RemovalFeeDetails> stableFeeDetails;


       
        
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class RemovalFeeDetails {
        private Integer token;
         @JsonProperty("animaltypeid")
        private Integer animalTypeId;
        @JsonProperty("days_with_stakeholder")
        private Integer daysWithStakeholder;
        @JsonProperty("fee_with_stakeholder") 
        private Float feeWithStakeholder;
        @JsonProperty("removalid")
        private Integer removalid;
        @JsonProperty("removal_type")
        private String removalType;
    }
}


