package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import digit.web.models.DashboardApplication;
import digit.web.models.SchemeApplication;

@Component
public class DashboardApplicationRowMapper implements ResultSetExtractor<List<DashboardApplication>> {

    @Override
    public List<DashboardApplication> extractData(@NonNull ResultSet rs) throws SQLException, DataAccessException {
        // Map to hold SchemeApplication objects with their IDs as keys
        Map<String, DashboardApplication> dashboardApplicationMap = new LinkedHashMap<>();

        // Iterate through the ResultSet
        while (rs.next()) {
            String id = rs.getString("id");
            DashboardApplication dashboardApplication = dashboardApplicationMap.get(id);
            if (dashboardApplication == null) {
            dashboardApplication = DashboardApplication.builder()
                        .id(rs.getString("id"))
                        .name(rs.getString("Name"))
                        .applicationNumber(rs.getString("ApplicationNumber"))
                        .schemeName(rs.getString("SchemeName"))
                        .createdDate(getDate(rs.getLong("lastmodifiedtime")))
                        .module(rs.getString("ModuleName"))
                        .status(rs.getString("State"))
                        .wards(rs.getString("Ward"))
                        .machineName(rs.getString("MachineName"))
                        .courseName(rs.getString("CourseName"))
                        .build();

                // Add the children properties to the SchemeApplication object
                //addChildrenToProperty(rs, schemeApplication);

                // Add the SchemeApplication object to the map
                dashboardApplicationMap.put(id, dashboardApplication);
            }
        }
        // Return a list of SchemeApplication objects
        return new ArrayList<>(dashboardApplicationMap.values());
    }

    private String getDate(Long lastmodifiedtime){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(lastmodifiedtime);
        return sdf.format(date);
    }

}
