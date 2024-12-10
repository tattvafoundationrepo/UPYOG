package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.scheme.CourseDetails;

import digit.web.models.scheme.EventDetails;
import digit.web.models.scheme.MachineDetails;
import digit.web.models.scheme.SchemeDetails;
import digit.web.models.scheme.SchemeHeadDetails;

@Component
public class SchemeWiseApplicationCountRowmapper implements ResultSetExtractor<List<EventDetails>>{

    @Override
    public List<EventDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
       

         Map<String, EventDetails> eventDetailsMap = new LinkedHashMap<>();
        while (rs.next()) {
            
            String eventName = "CountEvenet";
            EventDetails eventDetails = eventDetailsMap.get(eventName);
            if (eventDetails == null) {
                eventDetails = EventDetails.builder()
                        .schemeshead(new ArrayList<>())
                        .build();
                eventDetailsMap.put(eventName, eventDetails);
            }
            String schemeHeadName = rs.getString("schemeGroupName");
            SchemeHeadDetails schemeHeadDetails = eventDetails.getSchemeshead().stream()
                    .filter(sh -> sh.getSchemeHead().equals(schemeHeadName))
                    .findFirst()
                    .orElse(null);

            if (schemeHeadDetails == null) {
                schemeHeadDetails = SchemeHeadDetails.builder()
                        .schemeHead(schemeHeadName)
                        .schemeHeadApplicationCount(rs.getLong("schemeGroupCount"))
                        .schemeDetails(new ArrayList<>()) 
                        .build();
                eventDetails.getSchemeshead().add(schemeHeadDetails);
            }

            Long schemeID = rs.getLong("schemeid");
            SchemeDetails schemeDetails = schemeHeadDetails.getSchemeDetails().stream()
                    .filter(s -> s.getSchemeID().equals(schemeID))
                    .findFirst()
                    .orElse(null);

            if (schemeDetails == null) {
                schemeDetails = SchemeDetails.builder()
                        .schemeID(schemeID)
                        .schemeApplicationCount(rs.getLong("sschemeCount"))
                        .courses(new ArrayList<>())
                        .machines(new ArrayList<>())
                        .build();
                schemeHeadDetails.getSchemeDetails().add(schemeDetails);
            }

           
            Long courseID = rs.getLong("courseid");
            if (courseID != 0) { // Check if course details are present
                CourseDetails courseDetails = schemeDetails.getCourses().stream()
                        .filter(c -> c.getCourseID().equals(courseID))
                        .findFirst()
                        .orElse(null);

                if (courseDetails == null) {
                    courseDetails = CourseDetails.builder()
                            .courseID(courseID)
                            .courseWiseApplicationCount(rs.getLong("courseCount"))
                            .build();
                    schemeDetails.getCourses().add(courseDetails);

                }
            }

            Long machID = rs.getLong("machineid");
            if (machID != 0) { // Check if course details are present
                MachineDetails machDetails = schemeDetails.getMachines().stream()
                        .filter(c -> c.getMachID().equals(machID))
                        .findFirst()
                        .orElse(null);

                if (machDetails == null) {
                    machDetails = MachineDetails.builder()
                            .machID(machID)
                            .machineWiseApplicationCount(rs.getLong("machineCount"))
                            .build();
                    schemeDetails.getMachines().add(machDetails);

                }
            }
        }
        return new ArrayList<>(eventDetailsMap.values());
    }

}
