package digit.web.models.citizen;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.websocket.server.ServerEndpoint;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CitizenGatePassCriteria {

    @JsonProperty("StakeholderId")
    long stakeholderId;

}
