package digit.web.controllers;

import digit.service.EmployeeService;
import digit.web.models.EmployeeData;

import digit.web.models.request.EmployeeCriteriaRequest;
import digit.web.models.request.EmployeeDataRequest;
import digit.web.models.response.EmployeeResponse;
import digit.web.models.response.ResponseInfoFactory;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
public class EmployeeServiceController {

    @Autowired
    private EmployeeService service;
    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @PostMapping("/employee/create")
    public ResponseEntity<String> saveEmployee(@RequestBody EmployeeDataRequest employeeRequest) throws Exception {
        try {
            service.saveEmployeeData(employeeRequest);
            return new ResponseEntity<>("employee request  details saved successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to save employee request  details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/employee/_get")
    public ResponseEntity<EmployeeResponse> getEmployee(@Valid @RequestBody EmployeeCriteriaRequest request) {
        try {

            if (request.getEmpCode() == null ||
                    StringUtils.isEmpty(request.getEmpCode())) {
                return new ResponseEntity<>(null, HttpStatus.OK);
            }
            List<EmployeeData> employeeDetails = service.getEmployeeData(request);
            if(ObjectUtils.isEmpty(employeeDetails)){
                EmployeeData empData=  service.getEmployeeFromSAP(request);
                if(empData!=null){
                    employeeDetails=new ArrayList<>();
                    employeeDetails.add(empData);
                }
            }
            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
            EmployeeResponse response = EmployeeResponse.builder()
                    .employeeData(employeeDetails)
                    .responseInfo(responseInfo).message("Fetch successfully employee data")
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            ResponseInfo responseInfo = responseInfoFactory
                    .createResponseInfoFromRequestInfo(request.getRequestInfo(), false);
            return new ResponseEntity<>(new EmployeeResponse(responseInfo, null, "Invalid emp code"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/employee/updateStatus")
    public ResponseEntity<String> updateStatus(@RequestBody EmployeeDataRequest employeeDataRequest){
      try
      {
          service.saveStatus(employeeDataRequest);
          return new ResponseEntity<>("employee Processed  successfully.", HttpStatus.OK);

      }
      catch (Exception e){
          return new ResponseEntity<>("Failed to Processed employee " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
}