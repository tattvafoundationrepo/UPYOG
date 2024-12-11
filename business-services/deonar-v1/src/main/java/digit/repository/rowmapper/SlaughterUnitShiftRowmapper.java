package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.SlaughterUnit;

@Component
public class SlaughterUnitShiftRowmapper implements ResultSetExtractor<List<SlaughterUnit>> {

    @Override
    public List<SlaughterUnit> extractData(ResultSet rs) throws SQLException, DataAccessException {

      List<SlaughterUnit> unitList = new ArrayList<>();

        while (rs.next()) {


            SlaughterUnit shift = SlaughterUnit.builder()
            .openTime(rs.getString("opentime"))
            .closeTime(rs.getString("closetime")).build();
          
            unitList.add(shift);
        }

 
        return unitList;
    }
}


