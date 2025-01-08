package digit.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.ResultSetExtractor;

import com.fasterxml.jackson.databind.ObjectMapper;

import digit.web.models.collection.EntryFee;
import digit.web.models.collection.ParkingFee;
import digit.web.models.collection.RemovalFee;
import digit.web.models.collection.SlaughterFee;
import digit.web.models.collection.StableFee;
import digit.web.models.collection.WashFee;
import digit.web.models.collection.WeighingFee;
import digit.web.models.collection.WeighingFeeDetails;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class CollectionRowMapper<T> implements ResultSetExtractor<List<T>> {

    private final Class<T> type;

    // Constructor to specify the type of object to be mapped
    public CollectionRowMapper(Class<T> type) {
        this.type = type;
    }

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public List<T> extractData(ResultSet rs) throws SQLException {
        // if (type.equals(EntryFee.class)) {
        //     return (List<T>) extractEntryFee(rs);
        // }
        if (type.equals(StableFee.class)) {
            return (List<T>) extractStableFee(rs);
        }
        if (type.equals(ParkingFee.class)) {
            return (List<T>) extractParkingFee(rs);
        }
        if (type.equals(WashFee.class)) {
            return (List<T>) extractWashFee(rs);
        }
        if (type.equals(SlaughterFee.class)) {
            return (List<T>) extractSlaughterFee(rs);
        }
        if (type.equals(WeighingFee.class)) {
            return (List<T>) extractWeighingFee(rs);
        }
        if (type.equals(RemovalFee.class)) {
            return (List<T>) extractRemovalFee(rs);
        }
        throw new UnsupportedOperationException("Type " + type.getName() + " is not supported.");
    }

    private List<RemovalFee> extractRemovalFee(ResultSet rs) throws SQLException {

        Map<String, RemovalFee> stableFeeMap = new LinkedHashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        while (rs.next()) {
            String arrivalId = rs.getString("arrivalid");

            RemovalFee stableFee = stableFeeMap.get(arrivalId);
            if (stableFee == null) {
                stableFee = RemovalFee.builder()
                        .arrivalid(arrivalId)
                        .stakeholderId(rs.getLong("stakeholderid"))
                        .stakeholdername(rs.getString("stakeholdername"))
                        .liceneceNumber(rs.getString("licencenumber"))
                        .total(rs.getFloat("total_fee_with_stakeholder"))
                        .details(new ArrayList<>())
                        .build();
                stableFeeMap.put(arrivalId, stableFee);
            }

            String animalDetailsJson = rs.getString("animal_details");
            String animalTypeCountJson = rs.getString("animal_type_count");

            try {
                Map<Long, RemovalFee.Details> detailsMap = new HashMap<>();
                List<Map<String, Object>> animalTypeCountList = objectMapper.readValue(
                        animalTypeCountJson, new TypeReference<List<Map<String, Object>>>() {
                        });

                for (Map<String, Object> countEntry : animalTypeCountList) {
                    Long animalTypeId = ((Number) countEntry.get("animaltypeid")).longValue();
                    String animalType = countEntry.get("animaltype").toString();
                    Integer count = ((Number) countEntry.get("count")).intValue();

                    RemovalFee.Details detail = RemovalFee.Details.builder()
                            .animal(animalType)
                            .count(count)
                            .animalTypeId(animalTypeId)
                            .stableFeeDetails(new ArrayList<>())
                            .build();

                    detailsMap.put(animalTypeId, detail);
                    stableFee.getDetails().add(detail);
                }

                List<RemovalFee.RemovalFeeDetails> stableFeeDetailsList = objectMapper.readValue(
                        animalDetailsJson, new TypeReference<List<RemovalFee.RemovalFeeDetails>>() {
                        });

                for (RemovalFee.RemovalFeeDetails stableFeeDetail : stableFeeDetailsList) {
                    Long animalTypeId = (long) stableFeeDetail.getAnimalTypeId();
                    RemovalFee.Details detail = detailsMap.get(animalTypeId);

                    if (detail != null && detail.getAnimalTypeId() == stableFeeDetail.getAnimalTypeId().longValue()) {
                        detail.getStableFeeDetails().add(stableFeeDetail);
                    }
                }

                for (RemovalFee.Details detail : detailsMap.values()) {
                    double totalFee = detail.getStableFeeDetails().stream()
                            .mapToDouble(sfd -> sfd.getFeeWithStakeholder() != null ? sfd.getFeeWithStakeholder() : 0.0)
                            .sum();
                    detail.setTotalFee(totalFee);
                }
            } catch (Exception e) {
                throw new SQLException("Error parsing JSON data", e);
            }
        }

        return new ArrayList<>(stableFeeMap.values());
        
    }

    // private List<EntryFee> extractEntryFee(ResultSet rs) throws SQLException {
    //     // Use a map to store EntryFee objects by arrivalidLIC67891
    //     Map<String, EntryFee> entryFeeMap = new LinkedHashMap<>();
    //     while (rs.next()) {
    //         String arrivalId = rs.getString("arrivalid");
    //         float total = rs.getFloat("totalentryfee");

    //         // Check if an EntryFee object already exists for this arrivalId
    //         EntryFee entryFee = entryFeeMap.get(arrivalId);

    //         if (entryFee == null) {
    //             // If not, create a new EntryFee object and add it to the map
    //             entryFee = EntryFee.builder()
    //                     .arrivalid(arrivalId)
    //                     .details(new ArrayList<>()) // Initialize the list of details
    //                     .total(total)
    //                     .build();
    //             entryFeeMap.put(arrivalId, entryFee);
    //         }

    //         // Create and add Details object for each row
    //         EntryFee.Details details = EntryFee.Details.builder()
    //                 .animal(rs.getString("animal"))
    //                 .count(rs.getInt("animalcount"))
    //                 .fee(rs.getFloat("feevalue"))
    //                 .build();
    //         details.setTotalFee(details.getCount() * details.getFee());

    //         // Add the Details to the EntryFee's list
    //         entryFee.getDetails().add(details);
    //     }
    //     return new ArrayList<>(entryFeeMap.values());
    // }

    private List<StableFee> extractStableFee(ResultSet rs) throws SQLException {
        Map<String, StableFee> stableFeeMap = new LinkedHashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        while (rs.next()) {
            String arrivalId = rs.getString("arrivalid");

            StableFee stableFee = stableFeeMap.get(arrivalId);
            if (stableFee == null) {
                stableFee = StableFee.builder()
                        .arrivalid(arrivalId)
                        .stakeholderId(rs.getLong("stakeholderid"))
                        .stakeholdername(rs.getString("stakeholdername"))
                        .liceneceNumber(isColumnPresent(rs,"licencenumber")?rs.getString("licencenumber"):null)
                        .total(rs.getFloat("total_fee_with_stakeholder"))
                        .details(new ArrayList<>())
                        .build();
                stableFeeMap.put(arrivalId, stableFee);
            }

            String animalDetailsJson = rs.getString("animal_details");
            String animalTypeCountJson = rs.getString("animal_type_count");

            try {
                Map<Long, StableFee.Details> detailsMap = new HashMap<>();
                List<Map<String, Object>> animalTypeCountList = objectMapper.readValue(
                        animalTypeCountJson, new TypeReference<List<Map<String, Object>>>() {
                        });

                for (Map<String, Object> countEntry : animalTypeCountList) {
                    Long animalTypeId = ((Number) countEntry.get("animaltypeid")).longValue();
                    String animalType = countEntry.get("animaltype").toString();
                    Integer count = ((Number) countEntry.get("count")).intValue();

                    StableFee.Details detail = StableFee.Details.builder()
                            .animal(animalType)
                            .count(count)
                            .animalTypeId(animalTypeId)
                            .stableFeeDetails(new ArrayList<>())
                            .build();

                    detailsMap.put(animalTypeId, detail);
                    stableFee.getDetails().add(detail);
                }

                List<StableFee.StableFeeDetails> stableFeeDetailsList = objectMapper.readValue(
                        animalDetailsJson, new TypeReference<List<StableFee.StableFeeDetails>>() {
                        });

                for (StableFee.StableFeeDetails stableFeeDetail : stableFeeDetailsList) {
                    Long animalTypeId = (long) stableFeeDetail.getAnimalTypeId();
                    StableFee.Details detail = detailsMap.get(animalTypeId);

                    if (detail != null && detail.getAnimalTypeId() == stableFeeDetail.getAnimalTypeId().longValue()) {
                        detail.getStableFeeDetails().add(stableFeeDetail);
                    }
                }

                for (StableFee.Details detail : detailsMap.values()) {
                    double totalFee = detail.getStableFeeDetails().stream()
                            .mapToDouble(sfd -> sfd.getFeeWithStakeholder() != null ? sfd.getFeeWithStakeholder() : 0.0)
                            .sum();
                    detail.setTotalFee(totalFee);
                }
            } catch (Exception e) {
                throw new SQLException("Error parsing JSON data", e);
            }
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
                        .vehiclenumber(vehiclenumber)
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

    private List<WashFee> extractWashFee(ResultSet rs) throws SQLException {
        // Use a map to store WashFee objects by vehiclenumber
        Map<String, WashFee> washFeeMap = new LinkedHashMap<>();
        while (rs.next()) {
            String vehiclenumber = rs.getString("vehiclenumber");
            // Check if an WashFee object already exists for this arrivalId
            WashFee washFee = washFeeMap.get(vehiclenumber);
            if (washFee == null) {
                // If not, create a new WashFee object and add it to the map
                washFee = WashFee.builder()
                        .vehiclenumber(rs.getString("vehiclenumber"))
                        .vehicletype(rs.getString("vehicletype"))
                        .total(rs.getFloat("totalwashingfee"))
                        .build();
                washFeeMap.put(vehiclenumber, washFee);
            }
        }
        return new ArrayList<>(washFeeMap.values());
    }

    private List<SlaughterFee> extractSlaughterFee(ResultSet rs) throws SQLException {
        // Use a map to store slaughterFee objects by arrivalid
        Map<String, SlaughterFee> slaughterFeeMap = new LinkedHashMap<>();
        while (rs.next()) {
            String assigneeid = rs.getString("ddreference");
            float total = rs.getFloat("totalslaughterFee");

            // Check if an slaughterFee object already exists for this arrivalId
            SlaughterFee slaughterFee = slaughterFeeMap.get(assigneeid);

            if (slaughterFee == null) {
                // If not, create a new slaughterFee object and add it to the map
                slaughterFee = SlaughterFee.builder()
                        .assigneeid(assigneeid)
                        .details(new ArrayList<>()) // Initialize the list of details
                        .total(total)
                        .build();
                slaughterFeeMap.put(assigneeid, slaughterFee);
            }

            // Create and add Details object for each row
            SlaughterFee.Details details = SlaughterFee.Details.builder()
                    .animal(rs.getString("animal"))
                    .count(rs.getInt("animalcount"))
                    .fee(rs.getFloat("feevalue"))
                    .build();
            details.setTotalFee(details.getCount() * details.getFee());

            // Add the Details to the slaughterFee's list
            slaughterFee.getDetails().add(details);
        }
        return new ArrayList<>(slaughterFeeMap.values());
    }

    private List<WeighingFee> extractWeighingFee(ResultSet rs) throws SQLException {

        Map<String, WeighingFee> weighingFeeMap = new LinkedHashMap<>();
        while (rs.next()) {
            String assigneeid = rs.getString("ddreference");
            Double total = rs.getDouble("total");

            WeighingFee weighingFee = weighingFeeMap.get(assigneeid);

            if (weighingFee == null) {

                weighingFee = WeighingFee.builder()
                        .ddreference(assigneeid)
                        .details(new ArrayList<>())
                        .total(total)
                        .build();
                weighingFeeMap.put(assigneeid, weighingFee);
            }
            WeighingFeeDetails details = WeighingFeeDetails.builder()
                    .animal(rs.getString("animal"))
                    .unit(rs.getLong("unit"))
                    .fee(rs.getDouble("fee"))
                    .subtotal(rs.getDouble("subtotal"))
                    .skinunit(rs.getLong("skinunit"))
                    .skinfee(rs.getDouble("skinfee"))
                    .build();

            weighingFee.getDetails().add(details);
        }
        return new ArrayList<>(weighingFeeMap.values());

    }


    private boolean isColumnPresent(ResultSet rs, String columnName) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                if (metaData.getColumnName(i).equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
        } catch (SQLException e) {
        }
        return false;
    }
}