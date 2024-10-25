package digit.repository.rowmapper;

import digit.web.models.AnimalAssignmentDetails;
import digit.web.models.shopkeeper.ShopkeeperDetails;
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
public class SlaughterListRowMapper implements ResultSetExtractor<List<ShopkeeperDetails>> {

    @Override
    public List<ShopkeeperDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, ShopkeeperDetails> shopkeeperMap = new HashMap<>();

        while (rs.next()) {
            String ddReference = rs.getString("ddreference");
            String assigneeId = rs.getString("assigneeid");
            String arrivalId = rs.getString("arrivalid");

            ShopkeeperDetails shopkeeperDetails = shopkeeperMap.get(ddReference);
            if (shopkeeperDetails == null) {
                shopkeeperDetails = ShopkeeperDetails.builder()
                        .ddReference(ddReference)
                        .assigneeId(assigneeId)
                        .shopkeeperName(rs.getString("shopkeeper"))
                        .licenceNumber(rs.getString("licence"))
                        .mobileNumber(rs.getString("mobile"))
                        .arrivalId(arrivalId)
                        .animalAssignmentDetailsList(new ArrayList<>()) 
                        .build();

                shopkeeperMap.put(assigneeId, shopkeeperDetails);
            }

            AnimalAssignmentDetails animalAssignmentDetails = AnimalAssignmentDetails.builder()
                    .animalTypeId(rs.getLong("animaltypeid"))
                    .tokenNum(rs.getInt("token"))
                    .animalName(rs.getString("animal"))
                    .build();

            shopkeeperDetails.getAnimalAssignmentDetailsList().add(animalAssignmentDetails);
        }

        return new ArrayList<>(shopkeeperMap.values());
    }
}
