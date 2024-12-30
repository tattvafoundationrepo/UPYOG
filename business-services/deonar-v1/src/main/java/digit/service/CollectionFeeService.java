package digit.service;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.kafka.Producer;
import digit.web.models.collectionfee.CollectionFeeRequest;
import digit.web.models.collectionfee.SlaughterUnitFee;
import digit.web.models.collectionfee.SlaughterUnitFeeRequest;

@Service
public class CollectionFeeService {

    @Autowired
    private Producer producer;

    public CollectionFeeRequest saveCollectionFee(CollectionFeeRequest request){

        if(request == null || request.getCollectionFee() == null){
            throw new CustomException("INVALID_DATA", "CollectionFee request data is null or incomplete.");
        }

        producer.push("save-collectionfee-data", request);

        return request;
    }

    public SlaughterUnitFeeRequest saveSlaughterUnitFee(SlaughterUnitFeeRequest request){

        if(request == null || request.getSlaughterUnitFee() == null) {
            throw new CustomException("INVALID_DATA", "SlaughterUnitFee request data is null or incomplete.");
        }
        SlaughterUnitFee slaughterUnitFee = request.getSlaughterUnitFee();
        long createdAt = System.currentTimeMillis();
        long createdby = request.getRequestInfo().getUserInfo().getId();

        slaughterUnitFee.setCreatedat(createdAt);
        slaughterUnitFee.setUpdatedat(createdAt);
        slaughterUnitFee.setCreatedby(createdby);
        slaughterUnitFee.setUpdatedby(createdby);
        
        request.setSlaughterUnitFee(slaughterUnitFee);
        producer.push("save-slaughterunitfee-data", request);

        return request;
    }
}
