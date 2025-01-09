package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.GatePassMapper;
import digit.web.models.citizen.CitizenAnimalDetails;
import digit.web.models.citizen.CitizenGatePassDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CitizenGatePassRowMapper implements ResultSetExtractor<List<CitizenGatePassDetails>>{

    @Override
    public List<CitizenGatePassDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, CitizenGatePassDetails> detailsMap = new HashMap<>();
        while (rs.next()) {
            
            long arrivalId = rs.getLong("arrivalid");

            // Create or get existing CitizenGatePassDetails
            CitizenGatePassDetails details = detailsMap.getOrDefault(arrivalId, 
                CitizenGatePassDetails.builder()
                    .arrivalid(arrivalId)
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
