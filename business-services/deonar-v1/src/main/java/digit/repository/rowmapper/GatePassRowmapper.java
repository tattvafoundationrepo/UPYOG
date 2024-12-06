package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.GatePassMapper;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class GatePassRowmapper implements ResultSetExtractor<List<GatePassMapper>> {

    @Override
    public List<GatePassMapper> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<GatePassMapper> map = new ArrayList<>();

        while (rs.next()) {

            String licenceNumber = rs.getString("licencenumber"); 

            GatePassMapper details = GatePassMapper.builder()
                    .ddReference(rs.getString("ddreference")) 
                    .licenceNumber(licenceNumber)
                    .shopkeeperName(rs.getString("shopkeepername"))
                    .animalType((rs.getString("animalType")))
                    .carcassKenaecount((rs.getLong("count")) )
                    .build();
                map.add(details);
        }
        return map;
    }
}
