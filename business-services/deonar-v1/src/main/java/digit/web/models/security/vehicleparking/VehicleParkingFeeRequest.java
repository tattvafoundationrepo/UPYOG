package digit.web.models.security.vehicleparking;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class VehicleParkingFeeRequest {

    @JsonProperty("RequestInfo")
    @Valid
    @Builder.Default
    private RequestInfo requestInfo = null;

    @JsonProperty("vehicleParkingFeeDetails")
    private VehicleParkingFeeDetails vehicleParkingFeeDetails;

    private long startDate;

    private long endDate;

    private Long createdAt;

    private int createdBy;

    private int updatedBy;

    private Long updatedAt;

}
