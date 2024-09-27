package digit.web.models.peprocess;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessApplicationInfo {

    private String action;
    private String businessId;

}
