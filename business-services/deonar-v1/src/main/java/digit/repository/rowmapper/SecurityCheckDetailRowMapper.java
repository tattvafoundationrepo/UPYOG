package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.security.SecurityCheckDetails;
import digit.web.models.security.AnimalDetail;

@Component
public class SecurityCheckDetailRowMapper implements ResultSetExtractor<List<SecurityCheckDetails>> {

    @Override
    public List<SecurityCheckDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, SecurityCheckDetails> map = new LinkedHashMap<>();

        while (rs.next()) {
            String ddreference = null;

            if (isColumnPresent(rs, "ddreference")) {
                ddreference = rs.getString("ddreference");
            }
        
            String arrivalId = rs.getString("arrivalid");
            String uniqueKey = (ddreference != null) ? ddreference : arrivalId;
            

            if (arrivalId == null) continue;  

            SecurityCheckDetails details = map.get(uniqueKey);

            if (details == null) {
                details = SecurityCheckDetails.builder()
                    .entryUnitId(arrivalId)
                    .importPermission(rs.getString("importpermission"))
                    .stakeholderId(rs.getLong("stakeholderid"))
                    .traderName(rs.getString("stakeholdername"))
                    .dateOfArrival(rs.getDate("dateofarrival").toLocalDate().toString())  // Correct date handling
                    .timeOfArrival(rs.getTime("timeofarrival").toLocalTime().toString())  // Correct time handling
                    .permissionDate(rs.getDate("permissiondate").toLocalDate().toString())
                    .vehicleNumber(rs.getString("vehiclenumber"))
                    .mobileNumber(rs.getLong("mobilenumber"))
                    .email(rs.getString("email"))
                    .stakeholderTypeName(rs.getString("stakeholdertypename"))
                    .licenceNumber(rs.getString("licencenumber"))
                    .registrationNumber(rs.getString("registrationnumber"))
                    .validToDate(rs.getDate("validtodate").toLocalDate().toString())
                    .ddreference(ddreference)  // Correct date handling  
                    .animalDetails(new ArrayList<>())
                    .build();
                    
                 map.put(uniqueKey, details);
            }

            AnimalDetail animalDetail = AnimalDetail.builder()
                .animalTypeId(rs.getLong("animaltypeid"))
                .animalType(rs.getString("animaltype"))
                .count(rs.getInt("token"))
                .tradable(rs.getBoolean("tradable"))
                .stable(rs.getBoolean("stable"))
                .build();
            details.getAnimalDetails().add(animalDetail);
        }

        return new ArrayList<>(map.values());
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
