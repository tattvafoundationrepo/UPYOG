package digit.web.models.scheme;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Setter
@Getter
public class SchemeDetails {
    private Long schemeID;
    private String schemeName;
    private String schemeDesc;
    private Long schemeApplicationCount;
    private List<CriteriaDetails> criteria;
    private List<CourseDetails> courses;
    private List<MachineDetails> machines;
}