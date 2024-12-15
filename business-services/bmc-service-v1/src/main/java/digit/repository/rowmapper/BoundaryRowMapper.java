package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;


import digit.web.models.Boundary;

@Component
public class BoundaryRowMapper implements ResultSetExtractor<List<Boundary>> {

    @Override
    public List<Boundary> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Boundary> boundaryList = new ArrayList<>();
        while (rs.next()) {
            Boundary boundary = Boundary.builder()
                .id(rs.getLong("id"))
                .district(rs.getString("district"))
                .divisionname(rs.getString("divisionname"))
                .latitude(rs.getBigDecimal("latitude"))
                .longitude(rs.getBigDecimal("longitude"))
                .officename(rs.getString("officename"))
                .wardname(rs.getString("wardname"))
                .statename(rs.getString("statename"))
                .pincode(rs.getLong("pincode"))
                .subwardno(rs.getLong("subwardno"))
                .wardno(rs.getLong("wardno"))
                .build();
                boundaryList.add(boundary);
        }

        return boundaryList;
    
    }
}
