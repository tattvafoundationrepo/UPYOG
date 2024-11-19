
package digit.repository.rowmapper;


import digit.web.models.RemovalList;
import digit.web.models.security.AnimalDetail;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RemovalListRowmapper implements ResultSetExtractor<List<RemovalList>> {

    @Override
    public List<RemovalList> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, RemovalList> removalMap = new HashMap<>();

        while (rs.next()) {
            String ddReference = rs.getString("ddreference");
            Long assigneeId = rs.getLong("assigneeid");
            String arrivalId = rs.getString("arrivalid");

            RemovalList removalDetails = removalMap.get(ddReference);
            if (removalDetails == null) {
                removalDetails = RemovalList.builder()
                        .entryUnitId(ddReference)
                        .stakeholderId(assigneeId)
                        .traderName(rs.getString("shopkeeper"))
                        .licenceNumber(rs.getString("assigneelic"))
                        .arrivalId(arrivalId)
                        .dateOfArrival(rs.getDate("dateofarrival").toLocalDate().toString())  
                        .timeOfArrival(rs.getTime("timeofarrival").toLocalTime().toString())  
                        .animalDetails(new ArrayList<>()) 
                        .build();

                        removalMap.put(ddReference, removalDetails);
            }

            AnimalDetail animalAssignmentDetails = AnimalDetail.builder()
                    .animalTypeId(rs.getLong("animaltypeid"))
                    .count(rs.getInt("token"))
                    .animalType(rs.getString("animaltype"))
                    .build();

                    removalDetails.getAnimalDetails().add(animalAssignmentDetails);
        }

        return new ArrayList<>(removalMap.values());
    }
}
