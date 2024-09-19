package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.SchemeApplicationStatus;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class SchemeApplicationStatusRowMapper implements ResultSetExtractor<List<SchemeApplicationStatus>> {

    @Override
    public List<SchemeApplicationStatus> extractData(ResultSet rs) throws SQLException, DataAccessException {

        List<SchemeApplicationStatus> list = new ArrayList<>();
        while (rs.next()) {
            long timestamp = rs.getLong("lastmodifiedtime");
            Date date = new Date(timestamp);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = formatter.format(date);

            SchemeApplicationStatus applicationStatus = SchemeApplicationStatus.builder()
                    .applicationNumber(rs.getString("applicationnumber"))
                    .name(rs.getString("name"))
                    .courseName(rs.getString("coursename"))
                    .machine(rs.getString("machine"))
                    .currentStatus(rs.getString("currentStatus"))
                    .lastModifiedTime(formattedDate)
                    .comment(rs.getString("comment"))
                    .build();
            list.add(applicationStatus);
        }

        return list;
    }
}
