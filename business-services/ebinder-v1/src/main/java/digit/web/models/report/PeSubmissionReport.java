package digit.web.models.report;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PeSubmissionReport {
    private int id;
    private String peNumber;
    private Date reportNumber;
    private String comment;
    private Date reportSubmissionDate;
    private String designationname;
    private String competentauthname;
    private String orderType;
    private String caseType;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer reportNo;

}

