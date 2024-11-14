package digit.web.models.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityCheckCriteria {

    @JsonProperty("ArrivalUUID")
    private String arrivalUuid;

    @JsonProperty("ImportPermissionNumber")
    private String importPermissionNumber;

    @JsonProperty("TraderName")
    private String traderName;

    @JsonProperty("LicenseNumber")
    private String licenseNumber;

    @JsonProperty("VehicleNumber")
    private String vehicleNumber;
    
    @JsonProperty("Tradable")
    public Boolean tradable;

    @JsonProperty("Stable")
    public Boolean stable;

    @JsonProperty("inspectionid")
    private Long inspectionId; 

    private Boolean forCollection;

}