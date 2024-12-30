package digit.web.models.collectionfee;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SlaughterUnitFee {

    @JsonProperty("slaughterunitshiftid")
    private long slaughterunitshiftid;

    @JsonProperty("charges")
    private long charges;

    @JsonProperty("animaltypeid")
    private long animaltypeid;

    private long createdat;

    private long updatedat;

    private long createdby;

    private long updatedby;

    private boolean bybmc;

}
