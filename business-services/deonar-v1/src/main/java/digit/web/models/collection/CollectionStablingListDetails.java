package digit.web.models.collection;



import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollectionStablingListDetails {

     @JsonProperty("entryUnitId")
    private String entryUnitId;

    @JsonProperty("importPermission")
    private String importPermission;

    @JsonProperty("traderName")
    private String traderName;


    @JsonProperty("mobileNumber")
    private Long mobileNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("stakeholderTypeName")
    private String stakeholderTypeName;

    @JsonProperty("licenceNumber")
    private String licenceNumber;

    @JsonProperty("registrationNumber")
    private String registrationNumber;

    private Long stakeholderId;

    private String ddreference;



}
