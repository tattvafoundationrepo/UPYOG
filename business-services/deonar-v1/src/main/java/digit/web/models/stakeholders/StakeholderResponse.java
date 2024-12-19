package digit.web.models.stakeholders;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class StakeholderResponse {

    private String stakeholderName;
    private String address1;
    private long contactNumber;
    private String email;
    private long createdAt;

}
