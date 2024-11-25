package digit.web.models.collection;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeighingFee {


    private String ddreference;

    private List<WeighingFeeDetails> details;
   
    private Double  total;

}
