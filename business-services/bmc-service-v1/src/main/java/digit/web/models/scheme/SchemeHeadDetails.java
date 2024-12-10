package digit.web.models.scheme;

import java.util.List;

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
public class SchemeHeadDetails{
    private String schemeHead;
    private String schemeheadDesc;
    private Long schemeHeadApplicationCount;
    private List<SchemeDetails> schemeDetails;
}
