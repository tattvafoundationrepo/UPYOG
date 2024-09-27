package digit.web.models.peprocess;

import java.sql.Date;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PeReportSubmissionRequest {    
    private Date submissiondate;
    private String submissionComment;
    private Map<String, String> ordertype;  
    private Map<String, String> casetype;  

}
