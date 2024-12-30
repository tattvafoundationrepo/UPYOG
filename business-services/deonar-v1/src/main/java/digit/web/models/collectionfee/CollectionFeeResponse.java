package digit.web.models.collectionfee;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CollectionFeeResponse {

    private long animalid;
    private long feetype;
    private long feevalue;

}
