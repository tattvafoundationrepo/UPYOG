package digit.repository.querybuilder;

import digit.web.models.request.EmployeeCriteriaRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;


import java.util.List;
@Slf4j
@Component
public class EmployeeQueryBuilder {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String BASE_QUERY = """
            SELECT \
            empCode , empCity, empDepartment, empDesignation, empDistrict, \
            empDob, empEmail,  empEmptype,empFname, empGender, empJoining, \
            empLname, empMname, empMob, empPlaceofpost, empPostal, empRetirement, \
            empStreet1, empStreet2, status,  createdAt, updatedAt,  CreatedBy, updatedBy \
            FROM eg_employee_data \
            """;


    private static final String QUERY = """
            SELECT \
             empCode from eg_employee_data \
              """;

    public String getEmployeeDetails(EmployeeCriteriaRequest searchCriteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);

        if (!ObjectUtils.isEmpty(searchCriteria.getEmpCode())) {
            addClauseIfRequired(query, preparedStmtList);
            query.append("empCode IN (");


            for (int i = 0; i < searchCriteria.getEmpCode().size(); i++) {
                query.append("?");
                preparedStmtList.add(searchCriteria.getEmpCode().get(i));
                if (i < searchCriteria.getEmpCode().size() - 1) {
                    query.append(", ");
                }
            }
            query.append(")");
          return   query.toString();
        } else {
            return null;
        }

    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }

    public String getEmpCode(String empCode,List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(QUERY);
        if (!ObjectUtils.isEmpty(empCode)){
            addClauseIfRequired(query,preparedStmtList);
            query.append("empcode = ? ");
            preparedStmtList.add(empCode);
        }
        return  query.toString();

    }

    public String getEmployeeByStatus(EmployeeCriteriaRequest searchCriteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        searchCriteria.setStatus("PROCESSED");
        if (!ObjectUtils.isEmpty(searchCriteria.getStatus())) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" status != ?");
            preparedStmtList.add(searchCriteria.getStatus());

        }
        return query.toString();
    }

}
