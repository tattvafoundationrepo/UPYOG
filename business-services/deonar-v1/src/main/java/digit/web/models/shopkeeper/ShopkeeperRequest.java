package digit.web.models.shopkeeper;


import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopkeeperRequest {


    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    private Long mobileNo;

    private String licenseNo;

    private String registrationNo;





}
