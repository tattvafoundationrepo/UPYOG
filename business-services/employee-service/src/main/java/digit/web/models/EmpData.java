package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpData {

    @JsonProperty("EMP_CITY")
    private String empCity;

    @JsonProperty("EMP_DEPARTMENT")
    private String empDepartment;

    @JsonProperty("EMP_DESIGNATION")
    private String empDesignation;

    @JsonProperty("EMP_DISTRICT")
    private String empDistrict;

    @JsonProperty("EMP_DOB")
    private String empDob;

    @JsonProperty("EMP_EMAIL")
    private String empEmail;

    @JsonProperty("EMP_EMPTYPE")
    private String empEmptype;

    @JsonProperty("EMP_FNAME")
    private String empFname;

    @JsonProperty("EMP_GENDER")
    private String empGender;

    @JsonProperty("EMP_JOINING")
    private String empJoining;

    @JsonProperty("EMP_LNAME")
    private String empLname;

    @JsonProperty("EMP_MNAME")
    private String empMname;

    @JsonProperty("EMP_MOB")
    private String empMob;

    @JsonProperty("EMP_PLACEOFPOST")
    private String empPlaceofpost;

    @JsonProperty("EMP_POSTAL")
    private Long empPostal;

    @JsonProperty("EMP_RETIREMENT")
    private String empRetirement;

    @JsonProperty("EMP_STREET1")
    private String empStreet1;

    @JsonProperty("EMP_STREET2")
    private String empStreet2;
}
