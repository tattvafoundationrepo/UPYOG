package digit.repository.rowmapper;


import digit.web.models.SlaughterList;
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
public class SlaughterListRowMapper implements ResultSetExtractor<List<SlaughterList>> {

    @Override
    public List<SlaughterList> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, SlaughterList> shopkeeperMap = new HashMap<>();

        while (rs.next()) {
            String ddReference = rs.getString("ddreference");
            String assigneeId = rs.getString("assigneeid");
            String arrivalId = rs.getString("arrivalid");

            SlaughterList shopkeeperDetails = shopkeeperMap.get(ddReference);
            if (shopkeeperDetails == null) {
                shopkeeperDetails = SlaughterList.builder()
                        .ddReference(ddReference)
                        .stakeholderId(assigneeId)
                        .shopkeeperName(rs.getString("shopkeeper"))
                        .licenceNumber(rs.getString("assigneelic"))
                        .mobileNumber(rs.getString("mobile"))
                        .arrivalId(arrivalId)
                        .animalAssignmentDetailsList(new ArrayList<>()) 
                        .build();

                shopkeeperMap.put(assigneeId, shopkeeperDetails);
            }

            AnimalDetail animalAssignmentDetails = AnimalDetail.builder()
                    .animalTypeId(rs.getLong("animaltypeid"))
                    .count(rs.getInt("token"))
                    .animalType(rs.getString("animal"))
                    .build();

            shopkeeperDetails.getAnimalAssignmentDetailsList().add(animalAssignmentDetails);
        }

        return new ArrayList<>(shopkeeperMap.values());
    }
}
