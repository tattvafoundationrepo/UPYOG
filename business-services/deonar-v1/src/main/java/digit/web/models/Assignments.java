package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Assignments {

    private Long stakeholderId;
    private String ddReference;
    private String arrivalId;


}
