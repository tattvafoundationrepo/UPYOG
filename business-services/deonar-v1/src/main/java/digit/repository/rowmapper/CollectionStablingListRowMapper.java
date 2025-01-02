package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.collection.CollectionStablingListDetails;
@Component
public class CollectionStablingListRowMapper implements ResultSetExtractor<List<CollectionStablingListDetails>>{

     @Override
    public List<CollectionStablingListDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<CollectionStablingListDetails> detailsList = new ArrayList<>();
       
        while (rs.next()) {
            String ddreference = isColumnPresent(rs, "ddreference") ? rs.getString("ddreference") : null;
            String arrivalId = isColumnPresent(rs, "arrivalid") ? rs.getString("arrivalid") : null;

      
            CollectionStablingListDetails  details = CollectionStablingListDetails.builder()
                        .entryUnitId(arrivalId)
                        .stakeholderId(isColumnPresent(rs, "stakeholderid") ? rs.getLong("stakeholderid") : null)
                        .traderName(isColumnPresent(rs, "stakeholdername") ? rs.getString("stakeholdername") : null)
                        .mobileNumber(isColumnPresent(rs, "mobilenumber") ? rs.getLong("mobilenumber") : null)
                        .email(isColumnPresent(rs, "email") ? rs.getString("email") : null)
                        .stakeholderTypeName(
                                isColumnPresent(rs, "stakeholdertypename") ? rs.getString("stakeholdertypename") : null)
                        .licenceNumber(isColumnPresent(rs, "licencenumber") ? rs.getString("licencenumber") : null)
                        .registrationNumber(
                                isColumnPresent(rs, "registrationnumber") ? rs.getString("registrationnumber") : null)
                        .ddreference(ddreference)
                        .build();

                        detailsList.add(details);
            }

        return detailsList;
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
