package digit.service;

import digit.config.EmployeeServiceConfiguration;
import digit.kafka.Producer;
import digit.repository.EmployeeRepository;
import digit.web.models.*;
import digit.web.models.request.EmployeeCriteriaRequest;
import digit.web.models.request.EmployeeDataRequest;
import digit.web.models.request.EmployeeRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;


import org.springframework.stereotype.Service;

import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeServiceConfiguration configuration;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private Producer producer;


    public void saveEmployeeData(EmployeeDataRequest request) {
        for (String empId : request.getEmpId()) {
            EmpData empData = getEmpDataFromSAP(request.getEmpId());
            String empCode = repository.getEmpCode(empId);

            EmployeeCriteriaRequest employeeCriteriaRequest = new EmployeeCriteriaRequest();
            employeeCriteriaRequest.setEmpId(empId);
            employeeCriteriaRequest.setRequestInfo(request.getRequestInfo());
            employeeCriteriaRequest.setCreatedBy(request.getCreatedBy());
            employeeCriteriaRequest.setCreatedAt(request.getCreatedAt());
            employeeCriteriaRequest.setUpdatedBy(request.getUpdatedBy());
            employeeCriteriaRequest.setUpdatedAt(request.getUpdatedAt());
            upsertEmployeeData(empData, employeeCriteriaRequest, empCode);
        }

    }



    public EmployeeData getEmployeeFromSAP(EmployeeCriteriaRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("Authorization", "Basic " + encodeCredentials(configuration.getUserName(), configuration.getPassword()));
     EmployeeData employeeData=new EmployeeData();
        for (String empId : request.getEmpCode()) {
            HttpEntity<String> reqEnt = new HttpEntity<>("{\"EMP_ID\": \"" + empId + "\"}", headers);
            System.out.println(" employee request: " + headers + " " + reqEnt);

            EmployeeResponse empRes = restTemplate.postForObject(configuration.getURL(), reqEnt, EmployeeResponse.class);
            EmpData empData = empRes.getEmpData();
             employeeData = mapData(empData);
            String empCode = repository.getEmpCode(empId);
            employeeData.setEmpCode(empId);
            employeeData.setCreatedAt(System.currentTimeMillis());
            employeeData.setCreatedBy(request.getCreatedBy());
            employeeData.setUpdatedAt(System.currentTimeMillis());
            employeeData.setUpdatedBy(request.getUpdatedBy());
            if (ObjectUtils.isEmpty(empCode)) {
                if (empCode != null) {
                    employeeData.setStatus("Update");
                } else {
                    employeeData.setStatus("New Record");
                }
                request.setEmployeeData(employeeData);
                producer.push("save-employee-data", request);

            }
        }
            return employeeData;
        }

    private static String encodeCredentials(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    private String getEmployeeSaveUrl() {
        return configuration.getHrmsHost() + configuration.getHrmsCreateEndpoint();
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


    private void upsertEmployeeData(EmpData empData, EmployeeCriteriaRequest request, String empCode) {
        EmployeeData employeeData = mapData(empData);
        employeeData.setEmpCode(request.getEmpId());
        employeeData.setCreatedAt(System.currentTimeMillis());
        employeeData.setCreatedBy(request.getCreatedBy());
        employeeData.setUpdatedAt(System.currentTimeMillis());
        employeeData.setUpdatedBy(request.getUpdatedBy());
        if (empCode != null) {
            employeeData.setStatus("Update");
        } else {
            employeeData.setStatus("New Record");
        }
        request.setEmployeeData(employeeData);
        producer.push("save-employee-data", request);
    }


    public List<EmployeeData> getEmployeeData(EmployeeCriteriaRequest request) {
        if (request == null || request.getEmpCode() == null) {
            throw new CustomException("Invalid request", "Request is null");
        }
        List<EmployeeData> employeeData = repository.getEmployeeData(request);
        //epCode set
        return employeeData;
    }

    public void saveStatus(EmployeeDataRequest request) {

        for (String empId : request.getEmpId()) {

            EmpData empData=  getEmpDataFromSAP(request.getEmpId());
            EmployeeCriteriaRequest employeeCriteriaRequest = new EmployeeCriteriaRequest();
            employeeCriteriaRequest.setEmpId(empId);

            //HRMS
            EmployeeCriteriaRequest searchCriteria = new EmployeeCriteriaRequest();
            searchCriteria.setEmpId(empId);

            Employees employees = mapEmployeesData(empData, empId, request);
            List<Employees> employees1 = new ArrayList<>();
            employees1.add(employees);


            EmployeeRequest employeeRequest1 = new EmployeeRequest();
            employeeRequest1.setRequestInfo(request.getRequestInfo());
            employeeRequest1.setEmployees(employees1);


            String hrmsSaveURL = getEmployeeSaveUrl() + "?tenantId=" + request.getTenantId();

            setStatus(hrmsSaveURL, searchCriteria, employeeRequest1, employeeCriteriaRequest);
        }
    }

    private Employees mapEmployeesData(EmpData empData, String empId, EmployeeDataRequest request) {
        Employees employees = new Employees();
        employees.setEmployeeStatus("EMPLOYED");
        employees.setEmployeeType("PERMANENT");
        employees.setCode(empId);
        employees.setTenantId(request.getTenantId());
        EmployeeUser user = new EmployeeUser();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
        user.setTenantId(request.getTenantId());


        List<EmployeeRole> role = Arrays.asList(new EmployeeRole("EMPLOYEE", "EMPLOYEE", "mh.mumbai"));
        user.setRoles(role);


        employees.setUser(user);


        List<Jurisdictions> jurisdictions = new ArrayList<>();
        Jurisdictions jurisdiction = new Jurisdictions();
        jurisdiction.setBoundary(request.getBoundary());
        jurisdiction.setTenantId(request.getTenantId());
        jurisdiction.setHierarchy(request.getHierarchy());//ADMIN
        jurisdiction.setBoundaryType(request.getBoundaryType());
        jurisdictions.add(jurisdiction);
        employees.setJurisdictions(jurisdictions);


        List<Assignments> assignments = new ArrayList<>();
        Assignments assignment = new Assignments();
        assignment.setCurrentAssignment(true);
        assignment.setDepartment(request.getEmpDepartment());
        try {

            assignment.setFromDate(dateFormat.parse(empData.getEmpJoining()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assignment.setDesignation(request.getEmpDesignationCode());
        assignments.add(assignment);
        employees.setAssignments(assignments);


        employees.setEducation(new ArrayList<>());
        employees.setTests(new ArrayList<>());
        employees.setServiceHistory(new ArrayList<>());
        return employees;

    }

    private void setStatus(String URL, EmployeeCriteriaRequest searchCriteria, EmployeeRequest employeeRequest, EmployeeCriteriaRequest employeeCriteriaRequest) {

        List<EmployeeData> employeeDataList = repository.getEmployeeData(searchCriteria);
        if (!ObjectUtils.isEmpty(employeeDataList)) {
            for (EmployeeData employeeData : employeeDataList) {
                if (employeeData.getEmpCode() != null && ("New Record".equalsIgnoreCase(employeeData.getStatus())) || ("Update".equalsIgnoreCase(employeeData.getStatus()))) {
                    try {
                        restTemplate.postForObject(URL.toString(), employeeRequest, Map.class);
                        employeeData.setStatus("PROCESSED");
                    } catch (HttpClientErrorException e) {
                       throw new CustomException("Invalid request"," : Employee already processed");
                    }
                    employeeCriteriaRequest.setEmployeeData(employeeData);
                    producer.push("save-employee-data", employeeCriteriaRequest);
                }
            }
        }

    }

    private EmployeeData mapData(EmpData empData) {
        EmployeeData employeeData = new EmployeeData();
        employeeData.setEmpCity(empData.getEmpCity().trim());
        employeeData.setEmpDepartment(empData.getEmpDepartment().trim());
        employeeData.setEmpDesignation(empData.getEmpDesignation().trim());
        employeeData.setEmpDistrict(empData.getEmpDistrict().trim());
        employeeData.setEmpDob(empData.getEmpDob().trim());
        employeeData.setEmpEmail(empData.getEmpEmail().trim());
        employeeData.setEmpEmptype(empData.getEmpEmptype().trim());
        employeeData.setEmpFname(empData.getEmpFname().trim());
        employeeData.setEmpGender(empData.getEmpGender().trim());
        employeeData.setEmpJoining(empData.getEmpJoining().trim());
        employeeData.setEmpLname(empData.getEmpLname().trim());
        employeeData.setEmpMname(empData.getEmpMname().trim());
        employeeData.setEmpMob(empData.getEmpMob().trim());
        employeeData.setEmpPlaceofpost(empData.getEmpPlaceofpost().trim());
        String postal = empData.getEmpPostal().toString().trim();
        employeeData.setEmpPostal(Long.parseLong(postal));
        employeeData.setEmpRetirement(empData.getEmpRetirement().trim());

        employeeData.setEmpStreet1(empData.getEmpStreet1().trim());
        employeeData.setEmpStreet2(empData.getEmpStreet2().trim());
        return employeeData;

    }
    private EmpData getEmpDataFromSAP(List<String> empIds) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("Authorization", "Basic " + encodeCredentials(configuration.getUserName(), configuration.getPassword()));
        EmpData empData = new EmpData();
        for (String empId : empIds) {
            HttpEntity<String> reqEnt = new HttpEntity<>("{\"EMP_ID\": \"" + empId + "\"}", headers);
            System.out.println("save employee request: " + headers + " " + reqEnt);

            EmployeeResponse empRes = restTemplate.postForObject(configuration.getURL(), reqEnt, EmployeeResponse.class);

            empData = empRes.getEmpData();
        }

        return empData;
    }

}


//    @Scheduled(cron = "@midnight")
//    public void midNightScheduledTask() {
//
//
//    }
