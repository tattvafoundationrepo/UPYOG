package digit.repository;

import digit.repository.querybuilder.InspectionQueryBuilder;
import digit.repository.rowmapper.ArrivalDetailsRowMapper;
import digit.repository.rowmapper.InspectionRowMapper;
import digit.web.models.inspection.ArrivalDetailsResponse;
import digit.web.models.inspection.InspectionDetails;
import digit.web.models.inspection.InspectionIndicators;
import digit.web.models.security.SecurityCheckCriteria;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Repository
public class InspectionRepository {

    @Autowired
    private InspectionQueryBuilder queryBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InspectionRowMapper rowMapper;
    @Autowired
    private ArrivalDetailsRowMapper arrivalDetailsRowMapper;


    public List<InspectionDetails> getInspectionDetails(String arrivalId, Long inspectionType) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSearchQuery(arrivalId, inspectionType, preparedStmtList);
        log.info("Executing security check search with query: {}", query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

    public ArrivalDetailsResponse getArrivalDetails(String arrivalId) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSearchBaseQuery(arrivalId, preparedStmtList);
        log.info("Executing Arrival search with query: {}", query);
        return jdbcTemplate.query(query, arrivalDetailsRowMapper, preparedStmtList.toArray());
    }

    public List<InspectionIndicators> getInspectionIndicatorsByType(Long type,Long animaltypeid) {
        String sql = "SELECT result FROM eg_deonar_default_inspection_record WHERE type = ? and animaltypeid = ?";
        return jdbcTemplate.query(sql, new InspectionIndicatorsRowMapper(), type,animaltypeid);
    }

    public Long getArrivalId(String arrivalId,Long type) {
        String sql = "SELECT edi.arrivalid " +
                "FROM eg_deonar_inspection edi " +
                "LEFT JOIN eg_deonar_arrival eda ON edi.arrivalid = eda.id " +
                "WHERE eda.arrivalid = ? and edi.inspectiontype = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, arrivalId,type);
        } catch (EmptyResultDataAccessException e) {
            return null; 
        }
    }

    public List<Map<String, Long>> getAnimalTypeCounts(String arrivalId) {
        String sql = "SELECT edaaa.animaltypeid, edaaa.token " +
                "FROM eg_deonar_animal_at_arrival edaaa " +
                "LEFT JOIN eg_deonar_arrival eda ON edaaa.arrivalid = eda.id " +
                "WHERE eda.arrivalid = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Long> result = new HashMap<>();
            result.put("animalTypeId", rs.getLong("animaltypeid"));
            result.put("count", rs.getLong("token"));
            return result;
        }, arrivalId);
    }

    private class InspectionIndicatorsRowMapper implements ResultSetExtractor<List<InspectionIndicators>> {

        @Override
        public List<InspectionIndicators> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<InspectionIndicators> detailsList = new ArrayList<>();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<InspectionIndicators>>() {
            }.getType();

            while (rs.next()) {
                String jsonReport = rs.getString("result");
                List<InspectionIndicators> indicators = gson.fromJson(jsonReport, listType);
                detailsList.addAll(indicators);
            }
            return detailsList;
        }
    }

}
