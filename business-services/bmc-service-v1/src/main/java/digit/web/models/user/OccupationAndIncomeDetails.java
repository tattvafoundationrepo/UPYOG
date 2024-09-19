package digit.web.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import digit.web.models.scheme.IncomeDTO;
import digit.web.models.scheme.OccupationDTO;
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
public class OccupationAndIncomeDetails {

    @JsonProperty("occupation")
    private OccupationDTO occupation;
    
    @JsonProperty("income")
    private IncomeDTO income;

}
