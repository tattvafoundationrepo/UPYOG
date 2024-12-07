package digit.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import digit.repository.querybuilder.InspectionQueryBuilder;
import digit.repository.rowmapper.InspectionRowMapper;
import digit.web.models.inspection.InspectionDetails;
import digit.web.models.inspection.InspectionSearchCriteria;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class InspectionRepository {

    @Autowired
    private InspectionQueryBuilder queryBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InspectionRowMapper rowMapper;
 


    public List<InspectionDetails> getInspectionDetails(InspectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getSearchQuery(criteria, preparedStmtList);
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
    public  String getAnimalType(Long long1) {
       String sql = "select name from eg_deonar_animal_type where id = ?";
       return jdbcTemplate.queryForObject(sql, String.class, long1);
    }


    public List<InspectionDetails> getReantemortemInspection(String entryUnitId) {
        String sql = "SELECT DISTINCT animaltypeid, token FROM eg_deonar_vlistforreantemortem WHERE arrivalid = ?";
        List<InspectionDetails> finalList = new ArrayList<>();
    
        jdbcTemplate.query(sql, ps -> ps.setString(1, entryUnitId), (rs, rowNum) -> {
            InspectionSearchCriteria inspection = new InspectionSearchCriteria();
            inspection.setEntryUnitId(entryUnitId); 
            inspection.setAnimalTypeId(rs.getLong("animaltypeid"));
            inspection.setToken(rs.getLong("token"));
            inspection.setInspectionType(1L);
    
    
            List<InspectionDetails> details = getInspectionDetails(inspection);
            if (!details.isEmpty()) {
                finalList.add(details.get(0));
            }
            return null;
        });
    
        return finalList;
    }
    
    

}
