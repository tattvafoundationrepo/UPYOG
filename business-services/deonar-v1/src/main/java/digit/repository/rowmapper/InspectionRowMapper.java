package digit.repository.rowmapper;

import digit.web.models.inspection.InspectionDetails;
import digit.web.models.security.AnimalDetail;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class InspectionRowMapper implements ResultSetExtractor<List<InspectionDetails>> {

    @Override
    public List<InspectionDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, InspectionDetails> map = new LinkedHashMap<>();
        while (rs.next()) {
            String arrivalId = rs.getString("arrivalid");
            if (arrivalId == null) continue;  // Skip if arrivalId is null

            InspectionDetails details = map.get(arrivalId);

            if (details == null) {
                details = InspectionDetails.builder()
                        .arrivalId(arrivalId)
                        .importPermission(rs.getString("importpermission"))
                        .traderName(rs.getString("stakeholdername"))
                        .licenceNumber(rs.getString("licencenumber"))
                        .animalStabling(rs.getString("animalStabling"))
                        .animalTokenNumber(rs.getString("animaltokenumber"))
                        .indicatorValue(rs.getString("inspectionindicatorvalue"))
                        .inspectionDay(rs.getString("day"))
                        .veterinaryOfficerName(rs.getString(""))
                        .inspectionDate(rs.getString("inspectiondate"))
                        .animalDetails(new ArrayList<>())
                        .build();
                map.put(arrivalId, details);
            }

            AnimalDetail animalDetail = AnimalDetail.builder()
                    .animalType(rs.getString("animaltype"))
                    .count(rs.getInt("count"))
                    .build();
            details.getAnimalDetails().add(animalDetail);
        }
        return new ArrayList<>(map.values());
    }
}
