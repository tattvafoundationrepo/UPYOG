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
            InspectionDetails details = InspectionDetails.builder()
                        .arrivalId(rs.getString("arrivalid"))
                        .inspectionId(rs.getLong("inspectionid"))
                        .animalDetail(null)
                        .remark(rs.getString("resultremark"))
                        .inspectionDetailId(rs.getLong("id"))
                     //   .report(rs.getString("report"))
                        .inspectionId(rs.getLong("inspectiontype"))
                        .opinion(rs.getString("opinion"))
                        .other(rs.getString("other"))
                        .build();
                        try {
                            mapJsonToInspectionDetails(rs.getString("report"),details);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
            AnimalDetail animalDetail = AnimalDetail.builder()
                    .animalType(getAnimalType(rs.getLong("animaltypeid")))
                    .count(rs.getInt("tokenno"))
                    .animalTypeId(rs.getLong("animaltypeid"))
                    .editable(rs.getBoolean("flag"))
                    .build();
            details.setAnimalDetail(animalDetail);;
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

    public  String getAnimalType(Long long1) {
        String sql = "select name from eg_deonar_animal_type where id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, long1);
    }

}
