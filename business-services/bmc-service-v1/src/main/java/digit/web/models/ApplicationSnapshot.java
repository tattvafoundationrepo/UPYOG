package digit.web.models;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonProperty;
import digit.bmc.model.AadharUser;
import digit.bmc.model.UserOtherDetails;
import digit.bmc.model.UserSchemeApplication;
import digit.web.models.user.UserAddressDetails;
import digit.web.models.user.UserSubSchemeMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class ApplicationSnapshot {

@JsonProperty("updatedBank")
String bankDetailsList;

@JsonProperty("AadharUser")
AadharUser aadharUser;

@JsonProperty("updatedQualifications")
String qualificationDetailsList;

@JsonProperty("updatedAddress")
UserAddressDetails userAddressDetails;

@JsonProperty("UserOtherDetails")
UserOtherDetails userOtherDetails;

@JsonProperty("updatedDocument")
String updatedDocuments;

@JsonProperty("SchemeApplication")
UserSchemeApplication UserSchemeApplication;

@JsonProperty("UserSubSchemeMapping")
UserSubSchemeMapping userSubSchemeMapping;

}
