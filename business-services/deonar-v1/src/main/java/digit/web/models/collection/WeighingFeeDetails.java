package digit.web.models.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeighingFeeDetails {


    private String animal;
    private Long unit;
    private Double fee;
    private Double  subtotal;
    private Long skinunit;
    private Double skinfee;

}
