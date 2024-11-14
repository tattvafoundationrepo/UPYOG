package digit.web.models.security.vehicleparking;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
public class VehicleWashingFeesResponseWraper {
    
    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("VehicleVehicleWashingFeesResponse")
    private VehicleWashingFeesResponse response;



}
