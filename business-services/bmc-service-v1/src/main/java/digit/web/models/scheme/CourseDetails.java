package digit.web.models.scheme;

import java.util.Date;

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
public class CourseDetails {
    private Long courseID;
    private String courseName;
    private String courseDesc;
    private String courseDuration;
    private Date coursestartdate;
    private Date courseenddate;
    private String courseUrl;
    private String courseImageUrl;
    private String courseInstitute;
    private String instituteAddress;
    private Double courseAmount;
    private Long courseWiseApplicationCount;
}
