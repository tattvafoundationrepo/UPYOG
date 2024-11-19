package digit.web.models.security.vehicleparking;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleParkingFeeResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("vehicleParkingFeeResponseDetails")
    private List<VehicleParkingFeeResponseDetails> vehicleParkingFeeResponseDetails;

    @JsonProperty
    private String message;
}
