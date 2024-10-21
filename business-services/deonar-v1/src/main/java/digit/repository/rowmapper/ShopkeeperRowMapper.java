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
public class ShopkeeperRowMapper implements ResultSetExtractor<List<ShopkeeperDetails>> {

    @Override
    public List<ShopkeeperDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, ShopkeeperDetails> shopkeeperMap = new HashMap<>();
        
        while (rs.next()) {
            String arrivalId = rs.getString("arrivalid");

            ShopkeeperDetails shopkeeperDetails = shopkeeperMap.get(arrivalId);
            if (shopkeeperDetails == null) {
                shopkeeperDetails = ShopkeeperDetails.builder()
                        .arrivalId(arrivalId)
                        .importPermission(rs.getString("importpermission"))
                        .traderName(rs.getString("tradername"))
                        .dateOfArrival(rs.getDate("dateofarrival"))
                        .timeOfArrival(rs.getTime("timeofarrival"))
                        .vehicleNumber(rs.getString("vehiclenumber"))
                        .mobileNumber(rs.getString("mobilenumber"))
                        .email(rs.getString("email"))
                        .stakeholderTypeName(rs.getString("stakeholdertypename"))
                        .permissionDate(rs.getDate("permissiondate"))
                        .licenceNumber(rs.getString("licencenumber"))
                        .registrationNumber(rs.getString("registrationnumber"))
                        .validToDate(rs.getDate("validtodate"))
                        .animalAssignmentDetailsList(new ArrayList<>()) // Initialize the list
                        .build();

                shopkeeperMap.put(arrivalId, shopkeeperDetails);
            }

            AnimalAssignmentDetails animalAssignmentDetails = AnimalAssignmentDetails.builder()
                    .animalTypeId(rs.getLong("animaltypeid"))
                    .tokenNum(rs.getInt("token"))
                    .removalId(rs.getLong("removalid"))
                    .assigneeId(rs.getLong("assigneeid"))
                    .assigneeMobile(rs.getString("assigneemob"))
                    .assigneeLicenceNumber(rs.getString("assigneelic"))
                    .currentStakeholder(rs.getString("currentstakeholder"))
                    .assigneeName(rs.getString("stakeholdername"))
                    .build();


            shopkeeperDetails.getAnimalAssignmentDetailsList().add(animalAssignmentDetails);
        }
        return new ArrayList<>(shopkeeperMap.values());
    }
}
