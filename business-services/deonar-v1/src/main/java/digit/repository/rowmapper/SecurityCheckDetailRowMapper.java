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
            String ddreference = isColumnPresent(rs, "ddreference") ? rs.getString("ddreference") : null;
            String arrivalId = isColumnPresent(rs, "arrivalid") ? rs.getString("arrivalid") : null;

            if (arrivalId == null) continue;  

            String uniqueKey = (ddreference != null) ? ddreference : arrivalId;
            SecurityCheckDetails details = map.get(uniqueKey);

            if (details == null) {
                details = SecurityCheckDetails.builder()
                    .entryUnitId(arrivalId)
                    .importPermission(isColumnPresent(rs, "importpermission") ? rs.getString("importpermission") : null)
                    .stakeholderId(isColumnPresent(rs, "stakeholderid") ? rs.getLong("stakeholderid") : null)
                    .traderName(isColumnPresent(rs, "stakeholdername") ? rs.getString("stakeholdername") : null)
                    .dateOfArrival(isColumnPresent(rs, "dateofarrival") ? rs.getDate("dateofarrival").toLocalDate().toString() : null)
                    .timeOfArrival(isColumnPresent(rs, "timeofarrival") ? rs.getTime("timeofarrival").toLocalTime().toString() : null)
                    .permissionDate(isColumnPresent(rs, "permissiondate") ? rs.getDate("permissiondate").toLocalDate().toString() : null)
                    .vehicleNumber(isColumnPresent(rs, "vehiclenumber") ? rs.getString("vehiclenumber") : null)
                    .mobileNumber(isColumnPresent(rs, "mobilenumber") ? rs.getLong("mobilenumber") : null)
                    .email(isColumnPresent(rs, "email") ? rs.getString("email") : null)
                    .stakeholderTypeName(isColumnPresent(rs, "stakeholdertypename") ? rs.getString("stakeholdertypename") : null)
                    .licenceNumber(isColumnPresent(rs, "licencenumber") ? rs.getString("licencenumber") : null)
                    .registrationNumber(isColumnPresent(rs, "registrationnumber") ? rs.getString("registrationnumber") : null)
                    .validToDate(isColumnPresent(rs, "validtodate") ? rs.getDate("validtodate").toLocalDate().toString() : null)
                    .ddreference(ddreference)
                    .animalDetails(new ArrayList<>())
                    .build();
                
                map.put(uniqueKey, details);
            }

            AnimalDetail animalDetail = AnimalDetail.builder()
                .animalTypeId(isColumnPresent(rs, "animaltypeid") ? rs.getLong("animaltypeid") : null)
                .animalType(isColumnPresent(rs, "animaltype") ? rs.getString("animaltype") : null)
                .count(isColumnPresent(rs, "token") ? rs.getInt("token") : null)
                .tradable(isColumnPresent(rs, "tradable") ? rs.getBoolean("tradable") : false)
                .stable(isColumnPresent(rs, "stable") ? rs.getBoolean("stable") : false)
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
