package digit.web.models.peprocess;

import java.sql.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionComment {

    private String value;
    private String comments;

}
