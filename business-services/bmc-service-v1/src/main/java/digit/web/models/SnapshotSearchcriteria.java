package digit.web.models;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SnapshotSearchcriteria {

    private Long userId;

    private String tenantId;

    private String applicationNumber;


}
