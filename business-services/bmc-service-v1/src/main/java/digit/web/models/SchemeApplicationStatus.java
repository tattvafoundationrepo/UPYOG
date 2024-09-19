package digit.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemeApplicationStatus {

  private String applicationNumber;

  private String name;

  private String courseName;

  private String machine;

  private String currentStatus;

  private String lastModifiedTime;

  private String comment;

}
