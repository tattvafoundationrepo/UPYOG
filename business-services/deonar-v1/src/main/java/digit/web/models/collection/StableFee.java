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
public class StableFee {
    
    private String arrivalid;
    private List<Details> details;
    private Long stakeholderId;
    private String liceneceNumber;
    private Float total;
   

    // // Calculate total fees based on each detail's totalFee
    // public void calculateTotal() {
    //     if (details != null && !details.isEmpty()) {
    //         this.total = details.stream()
    //                             .map(Details::getTotalFee)
    //                             .reduce(0.0f, Float::sum);
    //     } else {
    //         this.total = 0.0f;
    //     }
    // }

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
        private List<StableFeeDetails> stableFeeDetails;
        // Automatically calculate totalFee when count or fee is set
        // public void setCount(Integer count) {
        //     this.count = count;
        //     updateTotalFee();
        // }

        // public void setFee(Float fee) {
        //     this.fee = fee;
        //     updateTotalFee();
        // }

        // // Calculate totalFee based on count * fee
        // private void updateTotalFee() {
        //     if (count != null && fee != null) {
        //         this.totalFee = count * fee;
        //     } else {
        //         this.totalFee = 0.0f;
        //     }
        // }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class StableFeeDetails {
        private Integer token;
         @JsonProperty("animaltypeid")
        private Integer animalTypeId;
        @JsonProperty("days_with_stakeholder")
        private Integer daysWithStakeholder;
        @JsonProperty("fee_with_stakeholder")
        private Float feeWithStakeholder;
    }
}