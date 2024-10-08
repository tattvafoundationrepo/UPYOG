package digit.web.models.security;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ArrivalRequest
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class ArrivalRequest {

    @JsonProperty("RequestInfo")
    @Valid
    @Builder.Default
    private RequestInfo requestInfo = null;

    @JsonProperty("ArrivalDetails")
    @Valid
    private Arrival arrivalDetails;

    @JsonProperty("AnimalDetails")
    @Valid
    private List<AnimalAtArrival> animalDetails;

    private List<AnimalAtArrival> saveAnimalDetails;
}
