package org.egov.user.domain.service;

import java.util.List;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.user.persistence.repository.OauthCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Server-side employee registration against HRMS.
 *
 * Moved here from employee-service (digit.service.EmployeeService#registerEmployee /
 * #acquireServiceToken) — behaviour is unchanged: the privileged token is acquired
 * server-side from the encrypted service-account credentials, injected into the payload's
 * RequestInfo, and the payload is forwarded to HRMS {@code _create}. The browser never sees
 * admin credentials or a bearer token.
 *
 * Exposed by {@link org.egov.user.web.controller.EmployeeController} at
 * {@code POST /user/employee/register}.
 */
@Service
public class EmployeeRegistrationService {

    @Autowired
    private OauthCredentialRepository oauthCredentialRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${egov.hrms.host}")
    private String hrmsHost;

    @Value("${egov.hrms.create.endpoint}")
    private String hrmsCreateEndpoint;

    @Value("${egov.enc.host}")
    private String encHost;

    @Value("${egov.enc.decrypt.endpoint}")
    private String decryptEndpoint;

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.ouath.path}")
    private String userOauthPath;

    /**
     * Acquires a privileged service access token using the encrypted OAuth credentials
     * stored in the database. Kept private so the token never leaves this service.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> acquireServiceToken() {
        List<Map<String, Object>> oauthDetails = oauthCredentialRepository.getOauthDetails();
        if (oauthDetails.isEmpty()) {
            throw new CustomException("OAUTH_DETAILS_NOT_FOUND", "OAuth details not found");
        }
        Map<String, Object> creds = oauthDetails.get(0);

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

        String decUsername = restTemplate.postForObject(
                encHost + decryptEndpoint,
                new HttpEntity<String>("\"" + creds.get("enc_username") + "\"", jsonHeaders), String.class);
        String decPassword = restTemplate.postForObject(
                encHost + decryptEndpoint,
                new HttpEntity<String>("\"" + creds.get("enc_password") + "\"", jsonHeaders), String.class);
        String tenantId = (String) creds.get("tenantid");

        HttpHeaders formHeaders = new HttpHeaders();
        formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        formHeaders.set("Authorization", "Basic ZWdvdi11c2VyLWNsaWVudDo=");
        formHeaders.set("Accept", "application/json, text/plain, */*");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("username", decUsername);
        form.add("password", decPassword);
        form.add("grant_type", "password");
        form.add("scope", "read");
        form.add("tenantId", tenantId);
        form.add("userType", "EMPLOYEE");

        // Points at this service's own /user/oauth/token (egov.user.host + egov.user.ouath.path),
        // exactly as employee-service called it — the grant, the client and the response shape are
        // therefore identical to the previous behaviour.
        String url = userHost + userOauthPath;
        return restTemplate.postForObject(url, new HttpEntity<MultiValueMap<String, String>>(form, formHeaders),
                Map.class);
    }

    /**
     * Obtains the privileged token internally, injects it into the request's RequestInfo, and
     * forwards the payload to HRMS {@code _create}. Returns HRMS's raw response body.
     *
     * @param employeePayloadJson the HRMS {@code Employees} payload as sent by the browser
     * @param tenantId            optional tenant appended as a query parameter, as before
     */
    public String registerEmployee(String employeePayloadJson, String tenantId) {
        Map<String, Object> token = acquireServiceToken();
        String accessToken = token != null ? (String) token.get("access_token") : null;
        Object userInfo = token != null ? token.get("UserRequest") : null;
        if (accessToken == null) {
            throw new CustomException("TOKEN_ACQUISITION_FAILED", "Could not acquire service token");
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root;
        try {
            root = (ObjectNode) mapper.readTree(employeePayloadJson);
        } catch (Exception e) {
            throw new CustomException("INVALID_PAYLOAD", "Invalid employee payload");
        }

        ObjectNode requestInfo = (root.has("RequestInfo") && root.get("RequestInfo").isObject())
                ? (ObjectNode) root.get("RequestInfo")
                : mapper.createObjectNode();
        requestInfo.put("authToken", accessToken);
        if (userInfo != null) {
            requestInfo.set("userInfo", mapper.valueToTree(userInfo));
        }
        root.set("RequestInfo", requestInfo);

        String createUrl = hrmsHost + hrmsCreateEndpoint;
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            createUrl += "?tenantId=" + tenantId;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("auth-token", accessToken);

        String body;
        try {
            body = mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new CustomException("PAYLOAD_SERIALIZE_FAILED", "Could not serialize payload");
        }

        ResponseEntity<String> response = restTemplate.postForEntity(
                createUrl, new HttpEntity<String>(body, headers), String.class);
        return response.getBody();
    }
}
