package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.web.models.stakeholders.*;

@Component
public class StakeholderRowMapper implements  ResultSetExtractor<List<StakeholderCheckDetails>>{

    @Override
    public List<StakeholderCheckDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<StakeholderCheckDetails> detailsList = new ArrayList<>();
        while(rs.next()){
            StakeholderCheckDetails details = StakeholderCheckDetails.builder()
                                            .stakeholderName(rs.getString("stakeholdername"))
                                            .stakeholdertype(rs.getString("stakeholdertype"))
                                            .animaltype(rs.getString("animaltype"))
                                            .licencenumber(rs.getString("licencenumber"))
                                            .mobilenumber(rs.getLong("mobilenumber"))
                                            .registrationnumber(rs.getString("registrationnumber"))
                                            .email(rs.getString("email"))
                                            .build();
            detailsList.add(details);
        }
        return detailsList;
    }

}
