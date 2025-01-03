package digit.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import digit.kafka.Producer;
import digit.repository.SaveStakeholderRepository;
import digit.web.models.stakeholders.StakeholderCheckCriteria;
import digit.web.models.stakeholders.StakeholderCheckDetails;
import digit.web.models.stakeholders.StakeholderRequest;
import digit.web.models.stakeholders.Stakeholders;

@Service
public class StakeholderService {

    @Autowired
    private Producer producer;

    @Autowired
    private SaveStakeholderRepository saveStakeholderRepository;

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

        stakeholderDetails.setValidFrom(getMillisFromDate(request.getStakeholders().getValidfromdate()));
        stakeholderDetails.setValidTo(getMillisFromDate(request.getStakeholders().getValidtodate()));

        request.setStakeholders(stakeholderDetails);

        producer.push("save-stakeholder-data", request);

        if (stakeholderDetails.getStakeholderTypeId() != 7) {
            ArrayList<Long> animalTypeIds = stakeholderDetails.getAnimalTypeIds();

            animalTypeIds.forEach(animalTypeId -> {
                request.getStakeholders().setAnimalTypeId(animalTypeId);
                producer.push("save-animaltype-data", request);
            });

            ArrayList<String> licenceNameList = stakeholderDetails.getLicenceNumbers();

            licenceNameList.forEach(licenceName -> {
                request.getStakeholders().setLicenceNumber(licenceName);
                producer.push("save-licencenumber-data", request);
            });
        }
        return request;
    }

    public List<StakeholderCheckDetails> getStakeholderDetails(RequestInfo requestInfo,
            StakeholderCheckCriteria criteria) {
        if (criteria == null) {
            criteria = new StakeholderCheckCriteria();
        }
        if (requestInfo == null) {
            requestInfo = new RequestInfo();
        }

        List<StakeholderCheckDetails> stakeholderDetails = saveStakeholderRepository.getStakeholderDetails(requestInfo,
                criteria);

        if (CollectionUtils.isEmpty(stakeholderDetails)) {
            return new ArrayList<>();
        }

        return stakeholderDetails;
    }

    private long getMillisFromDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = dateFormat.parse(dateString); // Parse the string into Date
        } catch (ParseException ex) {
            System.out.println("Invalid date format: " + ex.getMessage());
            return -1; // Return -1 to indicate an error
        }
        long millis = date.getTime(); // Convert to milliseconds
        System.out.println("Milliseconds: " + millis);
        return millis; // Return the milliseconds
    }
}
