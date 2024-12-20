package digit.service;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import digit.kafka.Producer;
import digit.web.models.stakeholders.StakeholderRequest;
import digit.web.models.stakeholders.Stakeholders;
import digit.web.models.stakeholders.Stakeholders;

@Service
public class StakeholderService {


    @Autowired
    private Producer producer;



    public StakeholderRequest saveStakeholderDetails(StakeholderRequest request) {
        // Validate the request
        if (request == null || request.getStakeholders() == null) {
            throw new CustomException("INVALID_DATA", "Stakeholder request data is null or incomplete.");
        }

        long currentTimeMillis = System.currentTimeMillis();
        Stakeholders stakeholderDetails = request.getStakeholders();


        stakeholderDetails.setCreatedAt(currentTimeMillis);
        stakeholderDetails.setCreatedBy(request.getRequestInfo().getUserInfo().getId().toString());
        stakeholderDetails.setUpdatedAt(currentTimeMillis);
        stakeholderDetails.setUpdatedBy(request.getRequestInfo().getUserInfo().getId().toString());


        request.setStakeholders(stakeholderDetails);




        producer.push("save-stakeholder-data", request);


        // ArrayList<Long> animalTypeIds = stakeholderDetails.getAnimalTypeIds();

        // animalTypeIds.forEach(animalTypeId -> {
        //         request.getStakeholders().setAnimalTypeId(animalTypeId);
        //         producer.push("save-animaltype-data", request);
        //     }
        // );
        
        // ArrayList<String> licenceNameList = stakeholderDetails.getLicenceNumbers();

        // licenceNameList.forEach(licenceName -> {
        //         request.getStakeholders().setLicenceNumber(licenceName);
        //         producer.push("save-licencenumber-data", request);
        //     }
        // );


        // ArrayList<Long> animalTypeIds = stakeholderDetails.getAnimalTypeIds();

        // animalTypeIds.forEach(animalTypeId -> {
        //         request.getStakeholders().setAnimalTypeId(animalTypeId);
        //         producer.push("save-animaltype-data", request);
        //     }
        // );
        
        // ArrayList<String> licenceNameList = stakeholderDetails.getLicenceNumbers();

        // licenceNameList.forEach(licenceName -> {
        //         request.getStakeholders().setLicenceNumber(licenceName);
        //         producer.push("save-licencenumber-data", request);
        //     }
        // );
        return request;
    }
}
