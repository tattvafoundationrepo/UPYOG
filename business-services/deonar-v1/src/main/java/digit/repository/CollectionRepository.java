package digit.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonProperty;

import digit.repository.querybuilder.CollectionQueryBuilder;
import digit.repository.querybuilder.CommonQueryBuilder;
import digit.repository.rowmapper.CollectionRowMapper;
import digit.repository.rowmapper.CommonRowMapper;
import digit.web.models.collection.EntryFee;
import digit.web.models.collection.StableFee;
import digit.web.models.common.CommonDetails;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@ToString
@Repository
@Slf4j
public class CollectionRepository {
   @Autowired
    private CollectionQueryBuilder queryBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<EntryFee>getEntryFee(CollectionSearchCriteria searchCriteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEntryFee(searchCriteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<EntryFee> entryfeerowMapper = new CollectionRowMapper<>(EntryFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
    }

    public List<StableFee> getStableFee(CollectionSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getStableFee(criteria, preparedStmtList);
        log.info("Final query: " + query);
        // Create a new instance of the row mapper with the correct type
        CollectionRowMapper<StableFee> entryfeerowMapper = new CollectionRowMapper<>(StableFee.class);
        return jdbcTemplate.query(query, entryfeerowMapper, preparedStmtList.toArray());
    }
}
