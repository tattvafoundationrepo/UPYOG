package digit.repository.rowmapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.bmc.model.VerificationDetails;
import digit.repository.UserRepository;

import digit.web.models.SnapshotSearchcriteria;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class VerificationDetailsRowMapper implements ResultSetExtractor<List<VerificationDetails>> {

    @Autowired
    UserRepository userRepository;

     @Override
    public List<VerificationDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, VerificationDetails> verificationDetailsMap = new LinkedHashMap<>();
         ResultSetMetaData rsmd = rs.getMetaData();
        Set<String> columns = new HashSet<>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            columns.add(rsmd.getColumnName(i).toLowerCase());
        }


        while (rs.next()) {
            String applicationNumber = rs.getString("applicationnumber");
            VerificationDetails verificationDetails = verificationDetailsMap.get(applicationNumber);

            if (verificationDetails == null) {
                verificationDetails = new VerificationDetails();
                verificationDetails.setApplicationNumber(applicationNumber);
                verificationDetails.setAgreeToPay(rs.getBoolean("agreetopay"));
                verificationDetails.setStatement(rs.getBoolean("statement"));
                verificationDetails.setUserId(rs.getLong("userid"));
                verificationDetails.setTenantId(rs.getString("tenantid"));
                verificationDetails.setScheme(rs.getString("scheme"));
                verificationDetails.setMachine(getFieldValue(rs, "machine"));
                verificationDetails.setCourse(getFieldValue(rs, "course"));
                if (columns.contains("case")){
                  verificationDetails.setSelectionCase(rs.getString("case"));
                }
  //               UserSearchCriteria criteria  = new UserSearchCriteria("full",verificationDetails.getUserId(),verificationDetails.getTenantId(),null);
  //              List<UserDetails> userDetails = userRepository.getUserDetails(criteria);
                SnapshotSearchcriteria criteria  = new SnapshotSearchcriteria(verificationDetails.getUserId(), verificationDetails.getTenantId(), applicationNumber) ;
                List<VerificationDetails> userDetails = userRepository.getSnapshotData(criteria);
                if (userDetails != null && !userDetails.isEmpty()) {
                verificationDetails.setUserDetails(userDetails.get(0).getUserDetails());
                if(userDetails.get(0).getUserDetails().get(0).getUserOtherDetails().getOccupation() == null)
                   verificationDetails.setEmployed(false);
                else
                   verificationDetails.setEmployed(true);   
                } 
                verificationDetailsMap.put(applicationNumber, verificationDetails);
            }
        }

        return new ArrayList<>(verificationDetailsMap.values());
    }

    private List<String> getFieldValue(ResultSet rs, String fieldName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            if (metaData.getColumnName(i).equalsIgnoreCase(fieldName)) {
                int columnType = metaData.getColumnType(i);
                if (columnType == java.sql.Types.ARRAY) {
                    String[] array = (String[]) rs.getArray(fieldName).getArray();
                    return Arrays.asList(array);
                } else if (columnType == java.sql.Types.VARCHAR) {
                    return Collections.singletonList(rs.getString(fieldName));
                }
            }
        }
        return Collections.emptyList();
    }
}