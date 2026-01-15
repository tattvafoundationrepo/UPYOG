package org.egov.demand.helper;

import org.egov.demand.model.AdvSettlementRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class MarketServiceClient {
   
@Autowired
   private  RestTemplate restTemplate;

    @Value("${egov.emarket.host}")
    private String emarketHost;
    
    @Value("${egov.emarket.penalty.create.host}")
    private String penaltyCreateHost;



    public void pushPenaltySettlements(AdvSettlementRequest settlements) {

        if (settlements == null ) {
            return;
        }

        String url = emarketHost + penaltyCreateHost;

        try {
            restTemplate.postForEntity(url, settlements, String.class);
            log.info("Successfully sent {} settlements to market service", settlements);

        } catch (Exception e) {
            log.error("Failed to call market penalty API", e);
            throw e; // optional: or handle retry
        }
    }
}

