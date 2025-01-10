
package digit.repository.rowmapper;


import digit.web.models.RemovalList;
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
public class RemovalListRowmapper implements ResultSetExtractor<List<RemovalList>> {

    @Override
    public List<RemovalList> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, RemovalList> removalMap = new HashMap<>();

        while (rs.next()) {
            String ddReference = isColumnPresent(rs, "ddreference") ? rs.getString("ddreference") : null;
            String arrivalId = isColumnPresent(rs, "arrivalid") ? rs.getString("arrivalid") : null;
            String uniqueKey = (ddReference != null) ? ddReference : arrivalId;


            RemovalList removalDetails = removalMap.get(uniqueKey);
            if (removalDetails == null) {
                removalDetails = RemovalList.builder()
                        .entryUnitId(arrivalId)
                        .stakeholderId(rs.getLong("stakeholderid"))
                        .shopkeepername(rs.getString("shopkeepername"))
                        .licenceNumber(rs.getString("licencenumber"))
                        .ddreference (ddReference)
                        .mobilenumber(rs.getString("mobilenumber"))
                        .removaldate(rs.getString("removaldate"))  
                        .removaltime(rs.getString("removaltime"))  
                        .stakeholderTypeName(rs.getString("stakeholdertypename"))
                        .animalDetails(new ArrayList<>()) 
                        .build();

                        removalMap.put(ddReference, removalDetails);
            }

            AnimalDetail animalAssignmentDetails = AnimalDetail.builder()
                    .animalTypeId(rs.getLong("animaltypeid"))
                    .count(rs.getInt("token"))
                    .animalType(rs.getString("animaltype"))
                    .removaltype(rs.getString("removaltype"))
                    .build();

                    removalDetails.getAnimalDetails().add(animalAssignmentDetails);
        }

        return new ArrayList<>(removalMap.values());
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
