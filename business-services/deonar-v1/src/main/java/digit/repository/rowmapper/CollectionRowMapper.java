package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.ResultSetExtractor;
import digit.web.models.collection.EntryFee;
import digit.web.models.collection.ParkingFee;
import digit.web.models.collection.StableFee;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectionRowMapper<T> implements ResultSetExtractor<List<T>> {

    private final Class<T> type;

    // Constructor to specify the type of object to be mapped
    public CollectionRowMapper(Class<T> type) {
        this.type = type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> extractData(ResultSet rs) throws SQLException {
        if (type.equals(EntryFee.class)) {
            return (List<T>) extractEntryFee(rs);
        }
        if (type.equals(StableFee.class)) {
            return (List<T>) extractStableFee(rs);
        }
        if (type.equals(ParkingFee.class)) {
            return (List<T>) extractParkingFee(rs);
        }
        throw new UnsupportedOperationException("Type " + type.getName() + " is not supported.");
    }

    private List<EntryFee> extractEntryFee(ResultSet rs) throws SQLException {
        // Use a map to store EntryFee objects by arrivalid
        Map<String, EntryFee> entryFeeMap = new LinkedHashMap<>();
        while (rs.next()) {
            String arrivalId = rs.getString("arrivalid");
            float total = rs.getFloat("totalentryfee");

            // Check if an EntryFee object already exists for this arrivalId
            EntryFee entryFee = entryFeeMap.get(arrivalId);

            if (entryFee == null) {
                // If not, create a new EntryFee object and add it to the map
                entryFee = EntryFee.builder()
                        .arrivalid(arrivalId)
                        .details(new ArrayList<>()) // Initialize the list of details
                        .total(total)
                        .build();
                entryFeeMap.put(arrivalId, entryFee);
            }

            // Create and add Details object for each row
            EntryFee.Details details = EntryFee.Details.builder()
                    .animal(rs.getString("animal"))
                    .count(rs.getInt("animalcount"))
                    .fee(rs.getFloat("feevalue"))
                    .build();
            details.setTotalFee(details.getCount() * details.getFee());

            // Add the Details to the EntryFee's list
            entryFee.getDetails().add(details);
        }
        return new ArrayList<>(entryFeeMap.values());
    }

    private List<StableFee> extractStableFee(ResultSet rs) throws SQLException {
        // Use a map to store EntryFee objects by arrivalid
        Map<String, StableFee> stableFeeMap = new LinkedHashMap<>();
        while (rs.next()) {
            String arrivalId = rs.getString("arrivalid");
            float total = rs.getFloat("totalstablefee");

            // Check if an stableFee object already exists for this arrivalId
            StableFee stableFee = stableFeeMap.get(arrivalId);

            if (stableFee == null) {
                // If not, create a new stableFee object and add it to the map
                stableFee = StableFee.builder()
                        .arrivalid(arrivalId)
                        .details(new ArrayList<>()) // Initialize the list of details
                        .total(total)
                        .build();
                stableFeeMap.put(arrivalId, stableFee);
            }

            // Create and add Details object for each row
            StableFee.Details details = StableFee.Details.builder()
                    .animal(rs.getString("animal"))
                    .count(rs.getInt("animalcount"))
                    .fee(rs.getFloat("feevalue"))
                    .build();
            details.setTotalFee(details.getCount() * details.getFee());

            // Add the Details to the stableFee's list
            stableFee.getDetails().add(details);
        }
        return new ArrayList<>(stableFeeMap.values());
    }

    private List<ParkingFee> extractParkingFee(ResultSet rs) throws SQLException {
        // Use a map to store ParkingFee objects by vehiclenumber
        Map<String, ParkingFee> parkingFeeMap = new LinkedHashMap<>();
        while (rs.next()) {
            String vehiclenumber = rs.getString("vehiclenumber");
            // Check if an parkingFee object already exists for this arrivalId
            ParkingFee parkingFee = parkingFeeMap.get(vehiclenumber);
            if (parkingFee == null) {
                // If not, create a new parkingFee object and add it to the map
                parkingFee = ParkingFee.builder()
                        .vehiclenumber(rs.getString("vehiclenumber"))
                        .vehicletype(rs.getString("vehicletype"))
                        .parkingdate(rs.getString("parkingdate"))
                        .parkingtime(rs.getString("parkingtime"))
                        .departuredate(rs.getString("departuredate"))
                        .departuretime(rs.getString("departuretime"))
                        .totalhours(rs.getInt("parking_hours"))
                        .total(rs.getFloat("totalparkingfee"))
                        .build();
                        parkingFeeMap.put(vehiclenumber, parkingFee);
            }
        }
        return new ArrayList<>(parkingFeeMap.values());
    }

}