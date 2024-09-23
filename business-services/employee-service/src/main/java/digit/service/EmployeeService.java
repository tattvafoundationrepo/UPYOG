package digit.service;

import digit.config.EmployeeServiceConfiguration;
import digit.kafka.Producer;
import digit.repository.EmployeeRepository;
import digit.web.models.*;
import digit.web.models.request.EmployeeCriteriaRequest;
import digit.web.models.request.EmployeeDataRequest;
import digit.web.models.request.EmployeeRequest;
import org.egov.tracer.model.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
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
            employeeCriteriaRequest.setEmpCode(request.getEmpId());
            employeeCriteriaRequest.setRequestInfo(request.getRequestInfo());
            employeeCriteriaRequest.setCreatedBy(String.valueOf(request.getRequestInfo().getUserInfo().getId()));
            employeeCriteriaRequest.setCreatedAt(System.currentTimeMillis());
            employeeCriteriaRequest.setUpdatedBy(String.valueOf(request.getRequestInfo().getUserInfo().getId()));
            employeeCriteriaRequest.setUpdatedAt(System.currentTimeMillis());
            upsertEmployeeData(empData, employeeCriteriaRequest, empCode);
        }

    }


    public EmployeeData getEmployeeFromSAP(EmployeeCriteriaRequest request, List<String> existingEmpCodes) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("Authorization",
                "Basic " + encodeCredentials(configuration.getUserName(), configuration.getPassword()));
        EmployeeData employeeData = new EmployeeData();
        List<String> empCodes = request.getEmpCode().stream().filter(empCode->!existingEmpCodes.contains(empCode)).collect(Collectors.toList());
        for (String empId : empCodes) {
            HttpEntity<String> reqEnt = new HttpEntity<>("{\"EMP_ID\": \"" + empId + "\"}", headers);
            System.out.println(" employee request: " + headers + " " + reqEnt);

            EmployeeResponse empRes = restTemplate.postForObject(configuration.getURL(), reqEnt,
                    EmployeeResponse.class);
            EmpData empData = empRes.getEmpData();
            employeeData = mapData(empData);

            employeeData.setEmpCode(empId);
            employeeData.setCreatedAt(System.currentTimeMillis());
            employeeData.setCreatedBy(String.valueOf(request.getRequestInfo().getUserInfo().getId()));
            employeeData.setUpdatedAt(System.currentTimeMillis());
            employeeData.setUpdatedBy(String.valueOf(request.getRequestInfo().getUserInfo().getId()));

            employeeData.setStatus("New Record");
            request.setEmployeeData(employeeData);
            producer.push("save-employee-data", request);


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
        for (String empId : request.getEmpCode()) {
            employeeData.setEmpCode(empId);

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
        producer.push("save-employee-data", request);}
    }


    public List<EmployeeData> getEmployeeData(EmployeeCriteriaRequest request) {
        if (request == null || request.getEmpCode() == null) {
            throw new CustomException("Invalid request", "Request is null");
        }
        List<EmployeeData> employeeData = repository.getEmployeeData(request);
        return employeeData;
    }

    public void saveStatus(EmployeeDataRequest request) {

        for (String empId : request.getEmpId()) {

            EmpData empData = getEmpDataFromSAP(request.getEmpId());

            // HRMS
            EmployeeCriteriaRequest searchCriteria = new EmployeeCriteriaRequest();
            searchCriteria.setEmpCode(request.getEmpId());

            Employees employees = mapEmployeesData(empData, empId, request);
            List<Employees> employeesList = new ArrayList<>();
            employeesList.add(employees);


            EmployeeRequest employeeRequest = new EmployeeRequest();
            employeeRequest.setRequestInfo(request.getRequestInfo());
            employeeRequest.setEmployees(employeesList);
            String hrmsSaveURL = getEmployeeSaveUrl() + "?tenantId=" + request.getTenantId();

            setStatus(hrmsSaveURL, searchCriteria, employeeRequest);
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


        try {
            user.setDob(dateFormat.parse(empData.getEmpDob()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        user.setName(empData.getEmpFname() + " " + empData.getEmpMname() + " " + empData.getEmpLname());
        user.setMobileNumber(empData.getEmpMob());
        user.setGender(getGender(empData.getEmpGender()));
        user.setFatherOrHusbandName(empData.getEmpMname() + " " + empData.getEmpLname());
        user.setCorrespondenceAddress(empData.getEmpPlaceofpost() + " " + empData.getEmpStreet1() + " " + empData.getEmpStreet2() + " " + empData.getEmpCity() + " " + empData.getEmpPostal());
        user.setTenantId(request.getTenantId());


        List<EmployeeRole> role = Arrays.asList(new EmployeeRole("EMPLOYEE", "EMPLOYEE", "mh.mumbai"));
        user.setRoles(role);


        employees.setUser(user);
        employees.setId(request.getId());
        employees.setUuid(request.getUuid());

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

    private void setStatus(String URL, EmployeeCriteriaRequest searchCriteria, EmployeeRequest employeeRequest) {

        List<EmployeeData> employeeDataList = repository.getEmployeeDataByStatus(searchCriteria);
        if (!ObjectUtils.isEmpty(employeeDataList)) {
            for (EmployeeData employeeData : employeeDataList) {
                if (employeeData.getEmpCode() != null) {
                    if (("New Record".equalsIgnoreCase(employeeData.getStatus())) || "FAILED".equalsIgnoreCase(employeeData.getStatus())) {
                        try {
                            log.info("Receive status {} for emp code {}. Processing this record", employeeData.getStatus(), employeeData.getEmpCode());
                            restTemplate.postForObject(URL.toString(), employeeRequest, Map.class);
                            employeeData.setStatus("PROCESSED");
                            employeeData.setRemark("Success");
                            searchCriteria.setEmployeeData(employeeData);
                            producer.push("save-employee-data", searchCriteria);
                        } catch (HttpClientErrorException e) {
                            employeeData.setStatus("FAILED");
                            employeeData.setRemark("Failed process due to: " +  getErrorMessage(e.getMessage()));
                            searchCriteria.setEmployeeData(employeeData);
                            producer.push("save-employee-data", searchCriteria);
                            log.info("Failed to process for emp code {} reason {}.", employeeData.getEmpCode(), e.getMessage());
                            throw new CustomException("Invalid request", " : Employee not processed");
                        }

                    } else if (("Update".equalsIgnoreCase(employeeData.getStatus()))) {
                        String url = updateHrmsEndpoint();
                        try {
                            restTemplate.postForObject(url.toString(), employeeRequest, Map.class);
                            employeeData.setStatus("PROCESSED");
                            employeeData.setRemark("Success");
                            searchCriteria.setEmployeeData(employeeData);
                            producer.push("save-employee-data", searchCriteria);

                        } catch (HttpClientErrorException e) {
                            throw new CustomException("Invalid request", " : Employee not  processed");
                        }
                    }

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
        headers.set("Authorization",
                "Basic " + encodeCredentials(configuration.getUserName(), configuration.getPassword()));
        EmpData empData = new EmpData();
        for (String empId : empIds) {
            HttpEntity<String> reqEnt = new HttpEntity<>("{\"EMP_ID\": \"" + empId + "\"}", headers);
            System.out.println("save employee request: " + headers + " " + reqEnt);

            EmployeeResponse empRes = restTemplate.postForObject(configuration.getURL(), reqEnt,
                    EmployeeResponse.class);

            empData = empRes.getEmpData();
        }

        return empData;
    }


    public String updateHrmsEndpoint() {
        String url = configuration.getHrmsHost() + configuration.getHrmsUpdatePoint();
        return url;
    }

    private String getHrmsSearchEndpoint() {
        String url = configuration.getHrmsHost() + configuration.getHrmsEndPoint();
        return url;
    }
   private String getErrorMessage(String response){

       String regex = "\"message\":\"([^\"]*)\"";
       Pattern pattern = Pattern.compile(regex);
       Matcher matcher = pattern.matcher(response);

       if (matcher.find()) {
           // Extracted message
           String message = matcher.group(1);
           log.info("Returning the error message"," ");
          return message;
       } else {
           log.info("Could not find the error message ");
           return response;
       }
   }

}


//    @Scheduled(cron = "@midnight")
//    public void midNightScheduledTask() {
//
//
//    }
