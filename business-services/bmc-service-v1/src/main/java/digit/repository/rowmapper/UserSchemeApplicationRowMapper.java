package digit.repository.rowmapper;
import org.springframework.jdbc.core.ResultSetExtractor;

import digit.bmc.model.UserSchemeApplication;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserSchemeApplicationRowMapper implements ResultSetExtractor<Map<String, UserSchemeApplication>> {

    @Override
    public Map<String, UserSchemeApplication> extractData(ResultSet rs) throws SQLException {
        Map<String, UserSchemeApplication> applicationMap = new HashMap<>();

        while (rs.next()) {
            UserSchemeApplication application = new UserSchemeApplication();

            application.setId(rs.getLong("id"));
            application.setApplicationNumber(rs.getString("applicationnumber"));
            application.setUserId(rs.getLong("userid"));
            application.setTenantId(rs.getString("tenantid"));
            application.setOptedId(rs.getLong("optedid"));
            application.setApplicationStatus(rs.getBoolean("applicationstatus"));
            application.setVerificationStatus(rs.getBoolean("verificationstatus"));
            application.setFirstApprovalStatus(rs.getBoolean("firstapprovalstatus"));
            application.setRandomSelection(rs.getBoolean("randomselection"));
            application.setFinalApproval(rs.getBoolean("finalapproval"));
            application.setSubmitted(rs.getBoolean("submitted"));
            application.setModifiedOn(rs.getLong("modifiedon"));
            application.setCreatedBy(rs.getString("createdby"));
            application.setModifiedBy(rs.getString("modifiedby"));
            application.setAgreeToPay(rs.getBoolean("agreetopay"));
            application.setStatement(rs.getBoolean("statement"));

            applicationMap.put(application.getApplicationNumber(), application);
        }

        return applicationMap;
    }
}
