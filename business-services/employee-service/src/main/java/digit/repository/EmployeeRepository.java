package digit.repository;

import digit.repository.queryBuilder.EmployeeQueryBuilder;
import digit.repository.rowMapper.EmployeeRowMapper;
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

//    public EmployeeData getEmployeeData(EmployeeSearchCriteria searchCriteria) {
//        List<Object> preparedStmtList = new ArrayList<>();
//        String query = queryBuilder.getEmployeeDetails(searchCriteria, preparedStmtList);
//        log.info(" query: " + query);
//        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());
//
//    }
    public List<EmployeeData> getEmployeeData(EmployeeCriteriaRequest searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEmployeeDetails(searchCriteria, preparedStmtList);
        log.info(" query: " + query);
        return jdbcTemplate.query(query, rowMapper, preparedStmtList.toArray());

    }

    public String getEmpCode(String empCode) {
        String query = "select e.empCode from eg_employee_data e where e.empCode = ? ";
        try {

            return jdbcTemplate.queryForObject(query, String.class, empCode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
