package digit.service;

import digit.config.BmcConfiguration;
import digit.web.models.employee.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service

public class EmployeeService {

    @Autowired
    private BmcConfiguration bmcConfiguration;

    @Autowired
    private RestTemplate restTemplate;


    private String getEmployeeSaveUrl() {
        return bmcConfiguration.getHrmsHost() + bmcConfiguration.getHrmsCreateEndpoint();
    }


    public void sendRequest(EmployeeDataRequest employeeRequest) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("Authorization", "Basic " + encodeCredentials(bmcConfiguration.getUserName(), bmcConfiguration.getPassword()));
        for (String empId : employeeRequest.getEmpId()) {
            HttpEntity<String> reqEnt = new HttpEntity<>("{\"EMP_ID\": \"" + empId + "\"}", headers);
            System.out.println("save employee request: " + headers + " " + reqEnt);

            EmployeeResponse empRes = restTemplate.postForObject(bmcConfiguration.getURL(), reqEnt, EmployeeResponse.class);
            EmpData empData = empRes.getEmpData();

            Employees employees = new Employees();
            employees.setEmployeeStatus("EMPLOYED");
            employees.setEmployeeType("PERMANENT");
            employees.setCode(empId);
            employees.setTenantId(employeeRequest.getTenantId());
            EmployeeUser user = new EmployeeUser();

            String encodedName = Base64.getEncoder().encodeToString(
                    (empData.getEmpFname() + " " + empData.getEmpMname() + " " + empData.getEmpLname()).getBytes()
            );
            String encodedMobileNumber = Base64.getEncoder().encodeToString(empData.getEmpMob().getBytes());
            String encodedDob = Base64.getEncoder().encodeToString(empData.getEmpDob().getBytes());
            String encodedGender = Base64.getEncoder().encodeToString(
                    (getGender(empData.getEmpGender())).getBytes()
            );
            String encodedFatherOrHusbandName = Base64.getEncoder().encodeToString(
                    (empData.getEmpMname() + " " + empData.getEmpLname()).getBytes()
            );
            String encodedCorrespondenceAddress = Base64.getEncoder().encodeToString(
                    (empData.getEmpPlaceofpost() + " " + empData.getEmpStreet1() + " " + empData.getEmpStreet2() + " " + empData.getEmpCity() + " " + empData.getEmpPostal()).getBytes()
            );


            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                String decodedDob = new String(Base64.getDecoder().decode(encodedDob));
                user.setDob(dateFormat.parse(decodedDob).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            user.setName(new String(Base64.getDecoder().decode(encodedName)));
            user.setMobileNumber(new String(Base64.getDecoder().decode(encodedMobileNumber)));
            user.setGender(new String(Base64.getDecoder().decode(encodedGender)));
            user.setFatherOrHusbandName(new String(Base64.getDecoder().decode(encodedFatherOrHusbandName)));
            user.setCorrespondenceAddress(new String(Base64.getDecoder().decode(encodedCorrespondenceAddress)));
            user.setTenantId(employeeRequest.getTenantId());


            List<EmployeeRole> role = Arrays.asList(new EmployeeRole("EMPLOYEE", "EMPLOYEE", "mh.mumbai"));
            user.setRoles(role);


            employees.setUser(user);


            List<Jurisdictions> jurisdictions = new ArrayList<>();
            Jurisdictions jurisdiction = new Jurisdictions();
            jurisdiction.setBoundary(employeeRequest.getBoundary());
            jurisdiction.setTenantId(employeeRequest.getTenantId());
            jurisdiction.setHierarchy(employeeRequest.getHierarchy());//ADMIN
            jurisdiction.setBoundaryType(employeeRequest.getBoundaryType());
            jurisdictions.add(jurisdiction);
            employees.setJurisdictions(jurisdictions);


            List<Assignments> assignments = new ArrayList<>();
            Assignments assignment = new Assignments();
            assignment.setCurrentAssignment(true);
            assignment.setDepartment(employeeRequest.getEmpDepartment());
            try {

                assignment.setFromDate(dateFormat.parse(empData.getEmpJoining()).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assignment.setDesignation(employeeRequest.getEmpDesignationCode());
            assignments.add(assignment);
            employees.setAssignments(assignments);

            List<Employees> employees1 = new ArrayList<>();
            employees1.add(employees);


            EmployeeRequest employeeRequest1 = new EmployeeRequest();
            employeeRequest1.setRequestInfo(employeeRequest.getRequestInfo());
            employeeRequest1.setEmployees(employees1);


            String URL = getEmployeeSaveUrl() + "?tenantId=" + employeeRequest.getTenantId();
            restTemplate.postForObject(URL.toString(), employeeRequest1, Map.class);
        }
    }

    private static String encodeCredentials(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    private String getGender(String gender) {
        if (gender.equalsIgnoreCase("M")) {
            return "MALE";
        } else if (gender.equalsIgnoreCase("F")) {
            return "FEMALE";
        } else {
            return "TRANSGENDER";
        }
    }
}

