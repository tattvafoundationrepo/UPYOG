

package digit.repository.rowmapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.penalty.PenaltyTypeDetails;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PenaltyRowmapper implements ResultSetExtractor<List<PenaltyTypeDetails>> {

    @Override
    public List<PenaltyTypeDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<PenaltyTypeDetails> penaltyTypeDetailsList = new ArrayList<>();

        while (rs.next()) {
            PenaltyTypeDetails penaltyTypeDetails = PenaltyTypeDetails.builder()
                .id(rs.getLong("id"))
                .penaltyType(rs.getString("CategoryPenaltyType"))
                .perUnit("per unit".equalsIgnoreCase(rs.getString("Unit"))) 
                .feeAmount(rs.getDouble("FeeAmount"))
                .build();

            penaltyTypeDetailsList.add(penaltyTypeDetails);
        }

        return penaltyTypeDetailsList;
    }
}

