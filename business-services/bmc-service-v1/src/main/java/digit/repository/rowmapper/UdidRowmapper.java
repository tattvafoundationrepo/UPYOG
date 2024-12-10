package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import digit.bmc.model.VerificationDetails;
import digit.web.models.user.DivyangDetails;
@Component
public class UdidRowmapper implements ResultSetExtractor<DivyangDetails>{

    @Override
    public DivyangDetails extractData(ResultSet rs) throws SQLException, DataAccessException {
        
        while(rs.next()){
           


        }
        
      return new DivyangDetails();
    }

}
