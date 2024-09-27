package digit.web.models.peprocess;

import java.sql.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PeEnquiryRecord {

    private Long id;
    private String peEnquiryId;
    private String comment;
    private Date dates;
    private String actions;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;



}
