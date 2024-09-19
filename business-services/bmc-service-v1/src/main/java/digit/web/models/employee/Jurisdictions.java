package digit.web.models.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Jurisdictions {
    private String hierarchy;
    private String boundaryType;
    private String boundary;
    private String tenantId;

}
