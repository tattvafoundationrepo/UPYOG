package digit.repository;

import digit.repository.querybuilder.InspectionQueryBuilder;

import digit.repository.rowmapper.InspectionRowMapper;

import digit.web.models.inspection.InspectionDetails;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Repository;



import java.util.ArrayList;
import java.util.List;


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
 


    public List<InspectionDetails> getInspectionDetails(String arrivalId, Long inspectionType) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSearchQuery(arrivalId, inspectionType, preparedStmtList);
        log.info("Executing security check search with query: {}", query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
    }

   
    public String getInspectionIndicatorsByType(Long type, Long animaltypeid) {
        String sql = "SELECT result FROM eg_deonar_default_inspection_record WHERE type = ? and animaltypeid = ?";
        return jdbcTemplate.queryForObject(sql, String.class, type, animaltypeid);
    }

    public Long getArrivalId(String arrivalId,Long type) {
        String sql = "SELECT edi.arrivalid " +
                "FROM eg_deonar_inspection edi " +
                "WHERE edi.arrivalid = ? and edi.inspectiontype = ?";
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


}
