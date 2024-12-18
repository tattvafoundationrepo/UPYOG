package digit.repository.rowmapper;

import digit.web.models.inspection.InspectionDetails;

import digit.web.models.security.AnimalDetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;

@Component
public class InspectionRowMapper implements ResultSetExtractor<List<InspectionDetails>> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<InspectionDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<InspectionDetails> detailsList = new ArrayList<>();
        while (rs.next()) {
            InspectionDetails.InspectionDetailsBuilder detailsBuilder = InspectionDetails.builder();

            if (hasColumn(rs, "arrivalid")) {
                detailsBuilder.arrivalId(rs.getString("arrivalid"));
            }
            if (hasColumn(rs, "inspectionid")) {
                detailsBuilder.inspectionId(rs.getLong("inspectionid"));
            }
            if (hasColumn(rs, "defaultremark")) {
                detailsBuilder.resultremark(rs.getString("defaultremark"));
            }
            if (hasColumn(rs, "id")) {
                detailsBuilder.inspectionDetailId(rs.getLong("id"));
            }
            if (hasColumn(rs, "type")) {
                detailsBuilder.inspectionId(rs.getLong("type"));
            }
            if (hasColumn(rs, "name")) {
                detailsBuilder.opinion(rs.getString("name"));
            }
            if (hasColumn(rs, "other")) {
                detailsBuilder.other(rs.getString("other"));
            }
            
           
            InspectionDetails details = detailsBuilder.build();
            
            if (hasColumn(rs, "result")) {
                try {
                    mapJsonToInspectionDetails(rs.getString("result"), details);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (hasColumn(rs, "animaltypeid") && hasColumn(rs, "token") ) {
                AnimalDetail animalDetail = AnimalDetail.builder()
                        .animalType(getAnimalType(rs.getLong("animaltypeid")))
                        .count(rs.getInt("token"))
                        .animalTypeId(rs.getLong("animaltypeid"))
                        .editable(true)
                        .build();
                details.setAnimalDetail(animalDetail);
            }

            detailsList.add(details);
        }
        return detailsList;
    }

    public static void mapJsonToInspectionDetails(String jsonArray, InspectionDetails details) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode arrayNode = mapper.readTree(jsonArray);
        ObjectNode mergedNode = mapper.createObjectNode();
        for (JsonNode node : arrayNode) {
            mergedNode.setAll((ObjectNode) node);
        }
        for (Field field : InspectionDetails.class.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (mergedNode.has(fieldName)) {
                field.set(details, mergedNode.get(fieldName).asText());
            }
        }
    }

    public String getAnimalType(Long animalTypeId) {
        String sql = "select name from eg_deonar_animal_type where id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, animalTypeId);
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

}
