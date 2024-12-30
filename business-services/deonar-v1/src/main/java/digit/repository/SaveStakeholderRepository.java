package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.SaveStakeholderQueryBuilder;
import digit.repository.rowmapper.StakeholderRowMapper;
import digit.web.models.stakeholders.StakeholderCheckCriteria;
import digit.web.models.stakeholders.StakeholderCheckDetails;
import digit.web.models.stakeholders.StakeholderRequest;
import digit.web.models.stakeholders.Stakeholders;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SaveStakeholderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SaveStakeholderQueryBuilder queryBuilder;

    @Autowired
    private StakeholderRowMapper rowMapper;

    public void saveStakeholderDetails(StakeholderRequest request) {
        List<Object> preparedStmtList = new ArrayList<>();

        Stakeholders stakeholderDetails = request.getStakeholders();
        try {
            // Build the query using QueryBuilder
            String stakeholderQuery = queryBuilder.getSaveStakeholderQuery(stakeholderDetails);

            // Log the query for debugging purposes
            log.info("Executing saveStakeholderQuery query: {}", stakeholderQuery);

            preparedStmtList.add(stakeholderDetails.getId());
            preparedStmtList.add(stakeholderDetails.getStakeholderName());
            preparedStmtList.add(stakeholderDetails.getAddress1());
            preparedStmtList.add(stakeholderDetails.getAddress2());
            preparedStmtList.add(stakeholderDetails.getPincode());
            preparedStmtList.add(stakeholderDetails.getMobileNumber());
            preparedStmtList.add(stakeholderDetails.getContactNumber());
            preparedStmtList.add(stakeholderDetails.getEmail());
            preparedStmtList.add(stakeholderDetails.getCreatedAt());
            preparedStmtList.add(stakeholderDetails.getUpdatedAt());
            preparedStmtList.add(stakeholderDetails.getCreatedBy());
            preparedStmtList.add(stakeholderDetails.getUpdatedBy());

            jdbcTemplate.update(stakeholderQuery, preparedStmtList.toArray());

            preparedStmtList = new ArrayList<>();

            String stakeholderTypeQuery = queryBuilder.getStakeholderTypeQuery(stakeholderDetails);

            // Log the query for debugging purposes
            log.info("Executing saveStakeholderTypeQuery query: {}", stakeholderTypeQuery);

            preparedStmtList.add(stakeholderDetails.getStakeholderTypeId());
            preparedStmtList.add(stakeholderDetails.getId());
            preparedStmtList.add(stakeholderDetails.getCreatedAt());
            preparedStmtList.add(stakeholderDetails.getUpdatedAt());
            preparedStmtList.add(stakeholderDetails.getCreatedBy());
            preparedStmtList.add(stakeholderDetails.getUpdatedBy());

            jdbcTemplate.update(stakeholderTypeQuery, preparedStmtList.toArray());

            preparedStmtList = new ArrayList<>();

            String animalTypeQuery = queryBuilder.getAnimalTypeQuery(stakeholderDetails);

            // Log the query for debugging purposes
            log.info("Executing saveAnimalTypeQuery query: {}", animalTypeQuery);

            preparedStmtList.add(stakeholderDetails.getStakeholderTypeId());
            preparedStmtList.add(stakeholderDetails.getAnimalTypeId());
            preparedStmtList.add(stakeholderDetails.getCreatedAt());
            preparedStmtList.add(stakeholderDetails.getUpdatedAt());
            preparedStmtList.add(stakeholderDetails.getCreatedBy());
            preparedStmtList.add(stakeholderDetails.getUpdatedBy());

            jdbcTemplate.update(animalTypeQuery, preparedStmtList.toArray());

            preparedStmtList = new ArrayList<>();

            String licenseIdQuery = queryBuilder.getSaveLicenseIdQuery(stakeholderDetails);

            // Log the query for debugging purposes
            log.info("Executing saveLicenseIdQueryquery: {}", licenseIdQuery);

            preparedStmtList.add(stakeholderDetails.getLicenceNumber());
            preparedStmtList.add(stakeholderDetails.getAnimalTypeId());
            preparedStmtList.add(stakeholderDetails.getCreatedAt());
            preparedStmtList.add(stakeholderDetails.getUpdatedAt());
            preparedStmtList.add(stakeholderDetails.getCreatedBy());
            preparedStmtList.add(stakeholderDetails.getUpdatedBy());

            jdbcTemplate.update(licenseIdQuery, preparedStmtList.toArray());

            // Execute the update query
            jdbcTemplate.update(stakeholderQuery, preparedStmtList.toArray());
        } catch (Exception e) {
            log.error("Exception occurred while trying to save stakeholder: {}", e.getMessage());
        }
    }

    public List<StakeholderCheckDetails> getStakeholderDetails(RequestInfo requestInfo, StakeholderCheckCriteria criteria){
        List<Object> preparedStmtList = new ArrayList<>();


        String query = queryBuilder.getStakeholderQuery(criteria, preparedStmtList);
        log.info("Final query: " + query);
        return jdbcTemplate.query(query, rowMapper);
    }

}
