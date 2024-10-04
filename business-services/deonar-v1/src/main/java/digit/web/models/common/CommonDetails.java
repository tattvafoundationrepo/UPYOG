package digit.web.models.common;

import digit.web.models.security.AnimalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CommonDetails {
    private long id;
    private String name;
    private String licenceNumber;
    private List<AnimalType> animalType;
    private String mobileNumber;
    private String tradertype;
    private String registrationnumber;
}
