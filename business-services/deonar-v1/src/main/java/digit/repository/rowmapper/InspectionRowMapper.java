package digit.repository.rowmapper;

import digit.web.models.inspection.InspectionDetails;
import digit.web.models.inspection.InspectionIndicators;
import digit.web.models.security.AnimalDetail;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
@Component
public class InspectionRowMapper implements ResultSetExtractor<List<InspectionDetails>> {

    @Override
    public List<InspectionDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<InspectionDetails> detailsList = new ArrayList<>();
        while (rs.next()) {
            InspectionDetails details = InspectionDetails.builder()
                        .arrivalId(rs.getString("arrivalid"))
                        .inspectionDate(rs.getString("inspectiondate"))
                        .inspectiontime(rs.getString("inspectiontime"))
                        .inspectionid(rs.getLong("inspectionid"))
                        .animalDetails(null)
                        .resultmark(rs.getString("resultremark"))
                        .id(rs.getLong("id"))
                        .report(new ArrayList<>())
                        .inspectionType(rs.getLong("inspectiontype"))
                        .build();
            Gson gson  = new Gson();
            List<InspectionIndicators> indicators = new  ArrayList<>();
            Type listType = new TypeToken<List<InspectionIndicators>>() {}.getType();
            indicators = gson.fromJson(rs.getString("report"), listType);
            details.setReport(indicators);

            AnimalDetail animalDetail = AnimalDetail.builder()
                    .animalType(rs.getString("animal"))
                    .count(rs.getInt("tokenno"))
                    .animalTypeId(rs.getLong("animaltypeid"))
                    .build();
            details.setAnimalDetails(animalDetail);;
            detailsList.add(details);
        }
        return detailsList;
    }
}
