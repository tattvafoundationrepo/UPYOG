package digit.repository.rowmapper;

import digit.web.models.inspection.ArrivalDetailsResponse;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ArrivalDetailsRowMapper implements ResultSetExtractor <ArrivalDetailsResponse> {
    @Override
    public ArrivalDetailsResponse extractData(ResultSet rs) throws SQLException, DataAccessException
    {   ArrivalDetailsResponse arrivalDetailsResponse=new ArrivalDetailsResponse();
        while (rs.next()){
         arrivalDetailsResponse= ArrivalDetailsResponse.builder()
                 .aId(rs.getInt("aId"))
                 .build();
            List<Integer> list=new ArrayList<>();
            list.add(rs.getInt("animalTypeId"));
            arrivalDetailsResponse.setAnimalTyeId(list);

        }

        return arrivalDetailsResponse;
    }
}
