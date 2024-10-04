package digit.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.egov.tracer.model.CustomException;
import digit.repository.SecurityCheckRepository;
import digit.web.models.security.SecurityCheckDetails;
import digit.web.models.security.ArrivalRequest;
import digit.web.models.security.SecurityCheckCriteria;
import digit.kafka.Producer;
@Service
public class SecurityCheckService {

    @Autowired
    private SecurityCheckRepository securityCheckRepository;

    @Autowired
    private Producer producer;
    /**
     * Fetch security check details based on search criteria.
     *
     * @param criteria The search criteria for fetching security check details.
     * @return List of SecurityCheckDetails.
     */
    public List<SecurityCheckDetails> getSecurityDetails(SecurityCheckCriteria criteria) {
        if (criteria == null) {
            throw new CustomException("INVALID_SEARCH_CRITERIA", "Search criteria is empty");
        }

        return securityCheckRepository.getSecurityCheckDetails(criteria);
    }

    /**
     * Save or update security check details in the database.
     *
     * @param securityCheckDetails The details to be saved or updated.
     */
    public void saveArrivalDetails(ArrivalRequest arrivalRequest) {
        if (arrivalRequest == null || arrivalRequest.getArrivalDetails() == null || arrivalRequest.getAnimalDetails() == null) {
            throw new CustomException("INVALID_DATA", "Arrival request data is null or incomplete.");
        }

        // Serialize the entire ArrivalRequest which includes both arrival and animal details.
        //String arrivalData = serializeArrivalRequest(arrivalRequest);  // Assume serializeArrivalRequest handles JSON conversion.

        // Push serialized data to a single Kafka topic.
        producer.push("topic_deonar_arrival", arrivalRequest);

        // Optionally, you can handle the response or further processing here if needed.
    }

}