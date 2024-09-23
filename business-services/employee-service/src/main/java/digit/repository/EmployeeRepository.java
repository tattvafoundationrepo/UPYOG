package digit.repository;


import digit.repository.querybuilder.EmployeeQueryBuilder;
import digit.repository.rowmapper.EmployeeRowMapper;
import digit.web.models.EmployeeData;
import digit.web.models.EmployeeSearchCriteria;
import digit.web.models.request.EmployeeCriteriaRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class EmployeeRepository {
    @Autowired
    private EmployeeQueryBuilder queryBuilder;
    @Autowired
    private EmployeeRowMapper rowMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<EmployeeData> getEmployeeData(EmployeeCriteriaRequest searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEmployeeDetails(searchCriteria, preparedStmtList);
        log.info(" query: {}" , query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());

    }

    public String getEmpCode(String empCode) {
        List<Object> preparedStmtList = new ArrayList<>();
        try {
            String query = queryBuilder.getEmpCode(empCode, preparedStmtList);
            log.info(" query: {}", query);
            return jdbcTemplate.queryForObject(query, String.class, empCode);
        } catch (Exception e) {
            log.error("Emp code not found");
            return null;
        }

    }

    public List<EmployeeData> getEmployeeDataByStatus(EmployeeCriteriaRequest searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEmployeeByStatus(searchCriteria, preparedStmtList);
        log.info(" query: {}" , query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());

    }

}
