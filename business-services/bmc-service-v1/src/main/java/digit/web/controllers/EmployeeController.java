package digit.web.controllers;

import digit.service.EmployeeService;
import digit.web.models.employee.EmployeeDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    @PostMapping("/employee/create")
    public ResponseEntity<?> saveEmployee(@RequestBody EmployeeDataRequest employeeRequest) throws Exception {
        service.sendRequest(employeeRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

