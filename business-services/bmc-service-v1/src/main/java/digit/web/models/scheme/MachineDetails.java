package digit.web.models.scheme;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Setter
public class MachineDetails {
    private Long machID;
    private String machName;
    private String machDesc;
    private Double machAmount;
    private Long machineWiseApplicationCount;
}
