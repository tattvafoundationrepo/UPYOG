package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.CeList;
import digit.web.models.Department;
import digit.web.models.Designation;
import digit.web.models.EmployeeData;
import digit.web.models.PeEnquiry;
import digit.web.models.PeEnquiryResponse;
import digit.web.models.SuspensionStatus;
@Component
public class PeEnquiryRowMapper implements ResultSetExtractor<List<PeEnquiryResponse>> {

    @Override
    public List<PeEnquiryResponse> extractData(ResultSet rs) throws SQLException {

        Map<String, PeEnquiryResponse> enquiryDetailsMap = new LinkedHashMap<>();
        List<EmployeeData> empDataList = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        Set<String> columns = new HashSet<>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            columns.add(rsmd.getColumnName(i).toLowerCase());
        }

        while (rs.next()) {
            String newCode = rs.getString("newcode");
            PeEnquiryResponse peEnquiryResponse = enquiryDetailsMap.get(newCode);
            if (peEnquiryResponse == null) {
                peEnquiryResponse = new PeEnquiryResponse();
                PeEnquiry peEnquiry = new PeEnquiry();
                peEnquiry.setEnquiryCode(rs.getString("newcode"));
                peEnquiry.setOldenquiryCode(rs.getString("oldcode"));
                peEnquiry.setOrderDate(rs.getDate("orderdate"));
                peEnquiry.setDepartment(new Department(rs.getString("empDept")));
                peEnquiry.setDesignation(new Designation(rs.getString("empDesig")));
                peEnquiry.setOrderNo(rs.getString("orderno"));
                peEnquiry.setEmployee(rs.getString("ceempcode"));
                peEnquiry.setEnquirySubject(rs.getString("enqsubject"));
                peEnquiry.setEmployeeName(rs.getString("emplname"));
                peEnquiryResponse.setPeEnquiry(peEnquiry);

            }
            if (columns.contains("cecode")) {
                EmployeeData empData = new EmployeeData();
                empData.setCasetype(rs.getString("casetype"));
                empData.setCestatus(rs.getBoolean("cestatus"));
                if(rs.getBoolean("cesuspended") ==true){
                    empData.setCeSuspended(new SuspensionStatus(1,"Yes"));
                }else
                    empData.setCeSuspended(new SuspensionStatus(0,"No"));
               // empData.setCesuspended(rs.getBoolean("cesuspended"));
                empData.setCesuspensionorder(rs.getString("cesuspensionorder"));
                empData.setDepartment(new Department(rs.getString("cedept")));
                empData.setDesignation(new Designation(rs.getString("cedept")));
                empData.setEmployeeCode(rs.getString("cecode"));
                empData.setEmployeename(rs.getString("cename"));
                empData.setEnqordertype(rs.getString("enqordertype"));
                empData.setMobileNumber(rs.getString("mobileno"));
                empData.setEmail(rs.getString("email"));
                empData.getDepartment().setI18key(rs.getString("cedept"));
                empData.getDesignation().setI18key(rs.getString("cedesig"));
                empDataList.add(empData);
            }
            peEnquiryResponse.setEmpData(empDataList);
            enquiryDetailsMap.put(newCode,peEnquiryResponse);

        }
        return new ArrayList<>(enquiryDetailsMap.values());
    }

}
