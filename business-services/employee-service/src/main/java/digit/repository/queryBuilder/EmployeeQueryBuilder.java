package digit.repository.queryBuilder;


import digit.web.models.request.EmployeeCriteriaRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;


import java.util.List;

@Component
public class EmployeeQueryBuilder {
    private static final String BASE_QUERY = """
     SELECT empCode , empCity, empDepartment, empDesignation, empDistrict, empDob, empEmail,  empEmptype,empFname, empGender, empJoining, empLname, empMname, empMob, empPlaceofpost, empPostal, empRetirement, empStreet1, empStreet2, status,  createdAt, updatedAt,  CreatedBy, updatedBy
     FROM eg_employee_data
     """;


    public String getEmployeeDetails(EmployeeCriteriaRequest searchCriteria, List<Object> preparedStmtList){

        StringBuilder query = new StringBuilder(BASE_QUERY);

            if (!ObjectUtils.isEmpty(searchCriteria.getEmpId())) {
                addClauseIfRequired(query, preparedStmtList);
                query.append("empCode = ? ");
                preparedStmtList.add(searchCriteria.getEmpId());
            }

        return query.toString();
    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }
}
