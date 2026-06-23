package org.egov.model;

import com.netflix.zuul.context.RequestContext;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.egov.constants.RequestContextConstants.CURRENT_REQUEST_START_TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EventLogRequestTest {

    private RequestCaptureCriteria criteria;

    @Before
    public void before() {
        RequestContext.getCurrentContext().clear();
        criteria = RequestCaptureCriteria.builder()
            .captureInputBody(false)
            .captureOutputBody(false)
            .captureOutputBodyOnlyForError(false)
            .build();
    }

    @Test
    public void test_should_capture_x_real_ip_as_client_ip() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Real-IP", "27.60.40.75");
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");

        EventLogRequest eventLogRequest = EventLogRequest.fromRequestContext(context(request), criteria);

        assertEquals("27.60.40.75", eventLogRequest.getClientIp());
    }

    @Test
    public void test_should_capture_first_x_forwarded_for_ip_when_real_ip_missing() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", "27.60.40.75, 10.0.0.2");

        EventLogRequest eventLogRequest = EventLogRequest.fromRequestContext(context(request), criteria);

        assertEquals("27.60.40.75", eventLogRequest.getClientIp());
    }

    @Test
    public void test_should_fallback_to_remote_addr_when_proxy_headers_missing() {
        MockHttpServletRequest request = request();
        request.setRemoteAddr("10.0.0.10");

        EventLogRequest eventLogRequest = EventLogRequest.fromRequestContext(context(request), criteria);

        assertEquals("10.0.0.10", eventLogRequest.getClientIp());
    }


    @Test
    public void test_should_capture_only_mobile_as_login_user_and_drop_login_request_body() {
        MockHttpServletRequest request = request();
        request.setContentType("application/json");
        request.setContent(("{\"otp\":{"
            + "\"mobileNumber\":\"9082697462\","
            + "\"name\":\"ANUJA VENKATGIRI\","
            + "\"dob\":\"2007-08-15\","
            + "\"type\":\"register\"}}").getBytes());
        RequestCaptureCriteria captureInputCriteria = RequestCaptureCriteria.builder()
            .captureInputBody(true)
            .captureOutputBody(false)
            .captureOutputBodyOnlyForError(false)
            .build();

        EventLogRequest eventLogRequest = EventLogRequest.fromRequestContext(context(request), captureInputCriteria);

        assertEquals("9082697462", eventLogRequest.getLoginUser());
        assertNull(eventLogRequest.getRequestBody());
    }

    @Test
    public void test_should_capture_login_user_when_input_body_capture_is_disabled() {
        MockHttpServletRequest request = request();
        request.setContentType("application/json");
        request.setContent("{\"otp\":{\"mobileNumber\":\"9876543210\"}}".getBytes());

        EventLogRequest eventLogRequest = EventLogRequest.fromRequestContext(context(request), criteria);

        assertEquals("9876543210", eventLogRequest.getLoginUser());
        assertNull(eventLogRequest.getRequestBody());
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/user-otp/v1/_send");
        return request;
    }

    private RequestContext context(MockHttpServletRequest request) {
        RequestContext context = RequestContext.getCurrentContext();
        context.clear();
        context.setRequest(request);
        context.setResponse(new MockHttpServletResponse());
        context.setResponseStatusCode(201);
        context.set(CURRENT_REQUEST_START_TIME, System.currentTimeMillis());
        return context;
    }
}
