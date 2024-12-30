package digit.web.models.collectionfee;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CollectionFee {

    private long animalid;
    private long feetype;
    private long feevalue;

}
