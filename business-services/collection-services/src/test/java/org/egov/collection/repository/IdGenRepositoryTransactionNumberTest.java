package org.egov.collection.repository;

import org.egov.collection.config.ApplicationProperties;
import org.egov.collection.model.IdGenerationRequest;
import org.egov.collection.model.IdGenerationResponse;
import org.egov.collection.model.IdRequest;
import org.egov.collection.model.IdResponse;
import org.egov.common.contract.request.RequestInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifies the format sent to id-gen while generating a payment transaction number.
 */
@RunWith(MockitoJUnitRunner.class)
public class IdGenRepositoryTransactionNumberTest {

    private static final String TENANT_ID = "mh.mumbai";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApplicationProperties applicationProperties;

    private IdGenRepository idGenRepository;

    @Before
    public void setUp() {
        idGenRepository = new IdGenRepository();
        ReflectionTestUtils.setField(idGenRepository, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(idGenRepository, "applicationProperties", applicationProperties);

        when(applicationProperties.getIdGenServiceHost()).thenReturn("http://localhost:8080");
        when(applicationProperties.getIdGeneration()).thenReturn("/egov-idgen/id/_generate");

        IdGenerationResponse response = new IdGenerationResponse(null,
                Collections.singletonList(new IdResponse("market1234567890")));
        when(restTemplate.postForObject(anyString(), any(), eq(IdGenerationResponse.class))).thenReturn(response);
    }

    private IdRequest captureIdRequest() {
        ArgumentCaptor<IdGenerationRequest> captor = ArgumentCaptor.forClass(IdGenerationRequest.class);
        org.mockito.Mockito.verify(restTemplate).postForObject(anyString(), captor.capture(),
                eq(IdGenerationResponse.class));
        return captor.getValue().getIdRequests().get(0);
    }

    @Test
    public void shouldUseConfiguredPrefixWhenPresent() {
        when(applicationProperties.getTransactionNumberPrefix()).thenReturn("market");

        idGenRepository.generateTransactionNumber(new RequestInfo(), TENANT_ID);

        IdRequest idRequest = captureIdRequest();
        assertEquals("market[d{10}]", idRequest.getFormat());
        assertEquals("collection.transactionno", idRequest.getIdName());
        assertEquals(TENANT_ID, idRequest.getTenantId());
    }

    @Test
    public void shouldFallBackToTenantDerivedPrefixWhenPrefixNotConfigured() {
        when(applicationProperties.getTransactionNumberPrefix()).thenReturn("");

        idGenRepository.generateTransactionNumber(new RequestInfo(), TENANT_ID);

        assertEquals("mumbai[d{10}]", captureIdRequest().getFormat());
    }

    @Test
    public void shouldFallBackToTenantDerivedPrefixWhenPrefixIsNull() {
        when(applicationProperties.getTransactionNumberPrefix()).thenReturn(null);

        idGenRepository.generateTransactionNumber(new RequestInfo(), TENANT_ID);

        assertEquals("mumbai[d{10}]", captureIdRequest().getFormat());
    }

    @Test
    public void shouldFallBackToWholeTenantIdWhenTenantHasNoDot() {
        when(applicationProperties.getTransactionNumberPrefix()).thenReturn("   ");

        idGenRepository.generateTransactionNumber(new RequestInfo(), "mh");

        assertEquals("mh[d{10}]", captureIdRequest().getFormat());
    }
}
