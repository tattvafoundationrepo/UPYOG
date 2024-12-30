package digit.web.models.collectionfee;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SlaughterUnitFeeResponse {
    
    private long slaughterunitshiftid;
    private long charges;
    private long animaltypeid;
    private long createdat;
    private boolean bybmc;

}
