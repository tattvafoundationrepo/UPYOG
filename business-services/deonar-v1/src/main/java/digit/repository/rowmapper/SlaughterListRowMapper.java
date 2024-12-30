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
                        .stakeholderId(isColumnPresent(rs, "stakeholderid") ? rs.getInt("stakeholderid") : null)
                        .shopkeeperName(isColumnPresent(rs, "stakeholdername") ? rs.getString("stakeholdername") : null)
                        .licenceNumber(isColumnPresent(rs, "licencenumber") ? rs.getString("licencenumber") : null)
                        .mobileNumber(isColumnPresent(rs, "mobilenumber") ? rs.getString("mobilenumber") : null)
                        .arrivalId(arrivalId)
                        .animalAssignmentDetailsList(new ArrayList<>())
                        .purchaseDate(isColumnPresent(rs, "purchasedate") ? rs.getString("purchasedate") : null)
                        .purchaseTime(isColumnPresent(rs, "purchasetime") ? rs.getString("purchasetime") : null)
                        .opinion(isColumnPresent(rs, "opinion") ? rs.getString("opinion") : null)
                        .build();

                shopkeeperMap.put(uniqueKey, shopkeeperDetails);
            }

            AnimalDetail animalAssignmentDetails = AnimalDetail.builder()
                    .animalTypeId(isColumnPresent(rs, "animaltypeid") ? rs.getLong("animaltypeid") : null)
                    .count(isColumnPresent(rs, "token") ? rs.getInt("token") : null)
                    .animalType(isColumnPresent(rs, "animaltype") ? rs.getString("animaltype") : null)
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
