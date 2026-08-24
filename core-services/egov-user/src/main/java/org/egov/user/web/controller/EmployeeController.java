package org.egov.user.web.controller;

import org.egov.user.domain.service.EmployeeRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

import lombok.extern.slf4j.Slf4j;

/**
 * Employee-facing endpoints served by egov-user.
 *
 * Moved here from employee-service (digit.web.controllers.EmployeeServiceController), which
 * exposed the same operation at {@code POST /employee-service/employee/register}. The new path
 * is {@code POST /user/employee/register} ({@code /user} is this service's context path).
 *
 * DEPLOYMENT: registration happens BEFORE the user has a session, so this path must be in the
 * API gateway's open-endpoints whitelist, exactly as {@code /employee-service/employee/register}
 * is today.
 */
@RestController
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeRegistrationService employeeRegistrationService;

    /**
     * Registers an employee in HRMS. The privileged service token is acquired server-side, so
     * the browser sends only the employee payload — no admin credentials or bearer token.
     */
    @PostMapping("/employee/register")
    public ResponseEntity<String> registerEmployee(@RequestBody String employeePayload,
            @RequestParam(value = "tenantId", required = false) String tenantId) {
        try {
            String result = employeeRegistrationService.registerEmployee(employeePayload, tenantId);
            return new ResponseEntity<String>(result, HttpStatus.OK);
        } catch (HttpStatusCodeException e) {
            // Forward HRMS's own error (e.g. duplicate employee code) + status so the
            // registration UI still shows a meaningful message, same as before.
            log.warn("HRMS rejected employee registration: {}", e.getStatusCode());
            return new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Employee registration failed", e);
            return new ResponseEntity<String>("{\"error\":\"Employee registration failed.\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
