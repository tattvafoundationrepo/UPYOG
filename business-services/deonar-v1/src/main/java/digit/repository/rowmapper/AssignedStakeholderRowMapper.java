package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.citizen.CitizenAnimalDetails;
import digit.web.models.stakeholders.StakeholderAssignedDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AssignedStakeholderRowMapper implements ResultSetExtractor<List<StakeholderAssignedDetails>>{

     @Override
    public List<StakeholderAssignedDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, StakeholderAssignedDetails> detailsMap = new HashMap<>();
        while (rs.next()) {
            
            long arrivalId = rs.getLong("arrivalid");

            // Create or get existing StakeholderAssignedDetails
            StakeholderAssignedDetails details = detailsMap.getOrDefault(arrivalId,
            StakeholderAssignedDetails.builder()
                    .arrivalid(arrivalId)
                    .licenceNumber(rs.getString("licencenumber"))
                    .ddReference(rs.getString("ddreference"))
                    .stakeholderName(rs.getString("stakeholdername"))
                    .stakeholdertype(rs.getString("stakeholdertype"))
                    .assigndate(rs.getString("assigndate"))
                    .assigntime(rs.getString("assigntime"))
                    .citizenAnimalDetails(new ArrayList<>()) // Initialize empty list for animal details
                    .build()
            );

            CitizenAnimalDetails animalDetails = CitizenAnimalDetails.builder()
                    .stakeholderName(rs.getString("stakeholdername"))
                    .animaltype(rs.getString("name"))
                    .token(rs.getLong("token"))
                    .build();

            // Add animalDetails to the list in details
            details.getCitizenAnimalDetails().add(animalDetails);

            // Put the updated details back into the map
            detailsMap.put(arrivalId, details);
        }
        // Return the list of grouped CitizenGatePassDetails
        return new ArrayList<>(detailsMap.values());
    }

}
