package digit.repository.rowmapper;

import digit.web.models.GatePassDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;


@Slf4j
@Component
public class GatePassRowmapper implements ResultSetExtractor<GatePassDetails> {

    @Override
    public GatePassDetails extractData(ResultSet rs) throws SQLException, DataAccessException {
        GatePassDetails gatePassDetailsList = new GatePassDetails();

        while (rs.next()) {
            GatePassDetails gatePassDetails = new GatePassDetails();
            
            gatePassDetails.setCarcasweight(rs.getDouble("carcassweight"));
            gatePassDetails.setKenaweight(rs.getDouble("kenaweight"));
            gatePassDetails.setTypeOfAnimal(rs.getString("animalType"));
            gatePassDetails.setReferenceNumber(rs.getString("ddreference"));

          //  gatePassDetailsList.add(gatePassDetails);
        }

        return gatePassDetailsList;
    }
}
