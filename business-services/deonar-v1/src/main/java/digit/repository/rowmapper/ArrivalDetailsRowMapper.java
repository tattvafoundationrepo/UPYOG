package digit.repository.rowmapper;

import digit.web.models.inspection.ArrivalDetailsResponse;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ArrivalDetailsRowMapper implements ResultSetExtractor <ArrivalDetailsResponse> {
    @Override
    public ArrivalDetailsResponse extractData(ResultSet rs) throws SQLException, DataAccessException
    {   ArrivalDetailsResponse arrivalDetailsResponse=new ArrivalDetailsResponse();
        while (rs.next()){
         arrivalDetailsResponse= ArrivalDetailsResponse.builder()
                .arrivalId(rs.getString("arrivalId"))
                .aliveAnimalCount(rs.getInt("count"))
                .traderName(rs.getString("stakeholdername")).build();}

        return arrivalDetailsResponse;
    }
}
