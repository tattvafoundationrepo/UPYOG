package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import digit.web.models.security.AnimalType;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.HashSet;

import java.sql.ResultSetMetaData;

import digit.web.models.common.CommonDetails;
@Component
public class CommonRowMapper implements ResultSetExtractor<List<CommonDetails>>{
    @Override
    public List<CommonDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, CommonDetails> commonDetailsMap = new LinkedHashMap<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        Set<String> columns = new HashSet<>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            columns.add(rsmd.getColumnName(i).toLowerCase());
        }
        while (rs.next()) {
            Long commonID = rs.getLong("id");
            String licencenumber = (columns.contains("licencenumber")) ? rs.getString("licencenumber") : null;

            String unique = (licencenumber != null) ? licencenumber : commonID.toString();
            CommonDetails commonDetails = commonDetailsMap.get(unique);
            if (commonDetails == null) {
                commonDetails = CommonDetails.builder()
                        .name(rs.getString("name"))
                        .id(rs.getLong("id"))         
                       
                        .animalType(new ArrayList<>())

                        .build();

                        if(columns.contains("licencenumber")){
                            commonDetails.setLicenceNumber(rs.getString("licencenumber"));
                        }
                        if(columns.contains("mobilenumber")){
                            commonDetails.setMobileNumber(rs.getString("mobilenumber"));
                        }
                        if(columns.contains("tradertype")){
                            commonDetails.setTradertype(rs.getString("tradertype"));
                        }
                        if(columns.contains("registrationnumber")){
                            commonDetails.setRegistrationnumber(rs.getString("registrationnumber"));
                        }     
                    }
  
            commonDetailsMap.put(unique, commonDetails);
            if(columns.contains("animalid")){
                AnimalType animalType = AnimalType.builder().id(rs.getInt("animalid"))  
                        .name(rs.getString("animaltype")).build(); 
                commonDetails.getAnimalType().add(animalType);
            }
        }

        return new ArrayList<>(commonDetailsMap.values());
    }
}
