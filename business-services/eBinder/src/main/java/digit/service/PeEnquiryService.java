package digit.service;

import digit.config.EBinderConfiguration;
import digit.kafka.Producer;
import digit.web.models.CeList;
import digit.web.models.request.CeRequest;
import digit.web.models.request.PeEnquiryRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PeEnquiryService {
    @Autowired
    private Producer producer;

    @Autowired
    private EBinderConfiguration configuration;

    @Autowired
    private RestTemplate restTemplate;

    public void savePeDetails(PeEnquiryRequest request) {

        if (request == null || request.getPeEnquiry() == null) {
            throw new CustomException("Invalid request", "PeEnquiry can not be null or empty");

        }
        producer.push("save-pe-enquiry", request);
    }

    public void  saveCeList(CeRequest request){
        if (request == null || request.getCeList() == null) {
            throw new CustomException("Invalid request", "CeRequest can not be null or empty");

        }
        if(!request.isRemove()){
            CeList ceList=request.getCeList();
            ceList.setCeStatus(true);
            request.setCeList(ceList);
            producer.push("save-eBinder-ceList", request);
        }

        else{
            CeList ceList=request.getCeList();
            ceList.setCeStatus(false);
            request.setCeList(ceList);
            producer.push("save-eBinder-ceList", request);
        }

    }

}
