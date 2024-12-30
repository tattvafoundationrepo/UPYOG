package digit.repository.rowmapper;


import digit.web.models.SlaughterList;
import digit.web.models.security.AnimalDetail;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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


            String ddreference = isColumnPresent(rs, "ddreference") ? rs.getString("ddreference") : null;
            String arrivalId = isColumnPresent(rs, "arrivalid") ? rs.getString("arrivalid") : null;
            String uniqueKey = (ddreference != null) ? ddreference : arrivalId;

            SlaughterList shopkeeperDetails = shopkeeperMap.get(uniqueKey);
            if (shopkeeperDetails == null) {
                shopkeeperDetails = SlaughterList.builder()
                        .ddReference(ddreference)
                       // .stakeholderId(rs.getInt("stakeholderid"))
                        .shopkeeperName(rs.getString("stakeholdername"))
                        .licenceNumber(rs.getString("licencenumber"))
                        .mobileNumber(rs.getString("mobilenumber"))
                        .arrivalId(arrivalId)
                        .animalAssignmentDetailsList(new ArrayList<>()) 
                        .purchaseDate(rs.getString("purchasedate"))
                        .purchaseTime(rs.getString("purchasetime"))
                       // .opinion(rs.getString("opinion"))
                        .build();

                shopkeeperMap.put(uniqueKey, shopkeeperDetails);
            }

             AnimalDetail animalAssignmentDetails = AnimalDetail.builder()
                     .animalTypeId(rs.getLong("animaltypeid"))
                     .count(rs.getInt("token"))
                    // .animalType(rs.getString("animaltype"))
                     .build();

             shopkeeperDetails.getAnimalAssignmentDetailsList().add(animalAssignmentDetails);
        }

        return new ArrayList<>(shopkeeperMap.values());
    }

     private boolean isColumnPresent(ResultSet rs, String columnName) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                if (metaData.getColumnName(i).equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
        } catch (SQLException e) {
        }
        return false;
    }
}
