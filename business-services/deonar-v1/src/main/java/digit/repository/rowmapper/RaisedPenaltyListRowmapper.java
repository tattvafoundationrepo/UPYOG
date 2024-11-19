


package digit.repository.rowmapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.penalty.RaisedPenalties;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RaisedPenaltyListRowmapper implements ResultSetExtractor<List<RaisedPenalties>> {

    @Override
    public List<RaisedPenalties> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<RaisedPenalties> penaltyDetailsList = new ArrayList<>();

        while (rs.next()) {
            RaisedPenalties penaltyDetails = RaisedPenalties.builder()

                .amount(rs.getDouble("amount"))
                .penaltyReference(rs.getString("penalty_reference"))
                .unit(rs.getString("unit"))
                .licenceNumber(rs.getString("licencenumber"))
                .stakeholderName(rs.getString("stakeholdername"))
                .penaltyType(rs.getString("category") +" - "+ rs.getString("penaltytype"))
                .build();

            penaltyDetailsList.add(penaltyDetails);
        }

        return penaltyDetailsList;
    }
}

