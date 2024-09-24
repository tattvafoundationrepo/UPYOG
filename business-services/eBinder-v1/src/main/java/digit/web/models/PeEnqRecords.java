package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PeEnqRecords {
    private int id;
    private int peEnquiryId;
    private String specialComment;
    private Long dates;
    private String actions;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;

}
