package digit.repository.querybuilder;

import java.util.List;

import org.springframework.stereotype.Component;

import digit.repository.CollectionSearchCriteria;

@Component
public class CollectionQueryBuilder {
    public String getEntryFee(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {
        final String ENTRYFEE_QUERY = """
                SELECT arrivalid,stakeholdername,licencenumber,stakeholderid,animal_details,animal_type_count,total_fee_with_stakeholder
                FROM eg_deonar_collection_entry_fee
                """;
        StringBuilder query = new StringBuilder(ENTRYFEE_QUERY);
   
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        if (criteria.getLiceneceNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" licencenumber = ? ");
            preparedStmtList.add(criteria.getLiceneceNumber());
        }

        return query.toString();
    }

    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }

    public String getStableFee(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {
        final String STABLEFEE_QUERY = """
                SELECT arrivalid,stakeholdername,licencenumber,stakeholderid,animal_details,animal_type_count,total_fee_with_stakeholder
                FROM eg_deonar_collection_stabling_fee
                """;
        StringBuilder query = new StringBuilder(STABLEFEE_QUERY);
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        if (criteria.getLiceneceNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" licencenumber = ? ");
            preparedStmtList.add(criteria.getLiceneceNumber());
        }

        return query.toString();
    }

    public String getRemovalFeeQuery(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {
        final String REMOVALFEE_QUERY = """
                SELECT arrivalid,stakeholdername,licencenumber,stakeholderid,animal_details,animal_type_count,total_fee_with_stakeholder
                FROM eg_deonar_collection_removal_fee
                """;
        StringBuilder query = new StringBuilder(REMOVALFEE_QUERY);
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" arrivalid = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        if (criteria.getLiceneceNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" licencenumber = ? ");
            preparedStmtList.add(criteria.getLiceneceNumber());
        }
        if (criteria.getMobileNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" mobilenumber = ? ");
            preparedStmtList.add(Long.parseLong(criteria.getMobileNumber()));
        }
        return query.toString();
    }

    public String getSlaughterFeeQuery(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {
        final String SLAUGHTERFEE_QUERY = """
                SELECT arrivalid,ddreference,stakeholdername,stakeholderid,animal_details,animal_type_count,total_fee_with_stakeholder
                FROM eg_deonar_collection_slaughter_fee
                """;
        StringBuilder query = new StringBuilder(SLAUGHTERFEE_QUERY);
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" ddreference = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        if (criteria.getLiceneceNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" licencenumber = ? ");
            preparedStmtList.add(criteria.getLiceneceNumber());
        }
        if (criteria.getMobileNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" mobilenumber = ? ");
            preparedStmtList.add(Long.parseLong(criteria.getMobileNumber()));
        }
        return query.toString();
    }


    public String getParkingFee(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {
        final String PARKINGFEE_QUERY = """
                SELECT
                vehicletype,
                vehiclenumber,
                parkingdate,
                parkingtime,
                departuredate,
                departuretime,
                parking_hours,
                totalparkingfee
                FROM eg_deonar_vparkingfee
                """;
        StringBuilder query = new StringBuilder(PARKINGFEE_QUERY);
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" vehiclenumber = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        return query.toString();
    }

    public String getWashingFee(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {
        final String WASHINGFEE_QUERY = """
                SELECT
                    vehicletype,
                    vehiclenumber,
                    totalwashingfee
                    FROM eg_deonar_vwashingfee
                """;
        StringBuilder query = new StringBuilder(WASHINGFEE_QUERY);
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" vehiclenumber = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        return query.toString();
    }

    // public String getSlaughterFee(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {
    //     final String SLAUGHTERFEE_QUERY_OLD = """
    //              SELECT ddreference,assigneelic,animal,animalcount,feevalue,totalslaughterFee
    //                 FROM eg_deonar_vslaughterfee
    //             """;

    //     final String SLAUGHTERFEE_QUERY_NEW = """

    //                     SELECT DISTINCT
    //                 v.ddreference,
    //                 a.assigneelic,
    //                 a.assigneeid,
    //                 d.name AS animal,
    //                 COUNT(a.animaltypeid) OVER (PARTITION BY v.ddreference, a.animaltypeid) AS animalcount,
    //                 (SELECT charges
    //                  FROM eg_deonar_vslaughterunitcharges
    //                  WHERE animaltypeid = a.animaltypeid
    //                    AND name = ?
    //                    AND opentime = ?
    //                    AND closetime = ?) AS feevalue,
    //                 SUM(
    //                     (SELECT charges
    //                      FROM eg_deonar_vslaughterunitcharges
    //                      WHERE animaltypeid = a.animaltypeid
    //                        AND name = ?
    //                        AND opentime = ?
    //                        AND closetime = ?)
    //                 ) OVER (PARTITION BY v.ddreference) AS totalslaughterfee
    //             FROM eg_deonar_vmainshopkeeper a
    //             JOIN eg_deonar_animal_removal b
    //                 ON b.arrivalid::text = a.arrivalid::text
    //                 AND b.animaltypeid = a.animaltypeid
    //                 AND b.tokennum = a.token
    //                 AND b.removalid = 1
    //             JOIN eg_deonar_vcurrentassignmentlist v
    //                 ON v.animaltypeid = a.animaltypeid
    //                 AND a.token = v.token
    //                 AND a.arrivalid::text = v.arrivalid::text
    //             LEFT JOIN eg_deonar_animal_type d
    //                 ON d.id = a.animaltypeid
                

    //                             """;

    //     StringBuilder query = new StringBuilder(SLAUGHTERFEE_QUERY_NEW);
        
    //     if (criteria.getSlaughterUnit() != null && criteria.getOpenTime() != null && criteria.getCloseTime() != null ) {
    //         preparedStmtList.add(criteria.getSlaughterUnit().toLowerCase());
    //         preparedStmtList.add(criteria.getOpenTime());
    //         preparedStmtList.add(criteria.getCloseTime());
    //         preparedStmtList.add(criteria.getSlaughterUnit().toLowerCase());
    //         preparedStmtList.add(criteria.getOpenTime());
    //         preparedStmtList.add(criteria.getCloseTime());
    //     }
    //     if (criteria.getSearch() != null) {
    //         query.append(" where  v.ddreference = ? ORDER BY v.ddreference ");
    //         preparedStmtList.add(criteria.getSearch());
    //     }
        
    //     return query.toString();
    // }

    public String getTradingFeeQuery(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {

        final String REMOVALFEE_QUERY = """
                SELECT ddreference,arrivalid,animal,animalcount,feevalue,totalstablefee
                FROM eg_deonar_vtradingfee
                """;
        StringBuilder query = new StringBuilder(REMOVALFEE_QUERY);
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" ddreference = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        return query.toString();

    }

    public String getWeighingFee(CollectionSearchCriteria criteria, List<Object> preparedStmtList) {

        final String WEIGHING_FEE_QUERY = """
                SELECT *
                FROM eg_deonar_vweighingfee
                """;
        StringBuilder query = new StringBuilder(WEIGHING_FEE_QUERY);
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" ddreference = ? ");
            preparedStmtList.add(criteria.getSearch());
        }
        return query.toString();

    }

    public String saveVehicleParkDeparture(long time, String vehiclenumber){
        final String SET_DEPARTURE_TIME = """
                UPDATE eg_deonar_vehicle_parking
                """;
        StringBuilder query = new StringBuilder(SET_DEPARTURE_TIME);
        query.append(" SET departuretime = ").append(String.valueOf(time)).append(" ");
        query.append(" WHERE vehiclenumber = '").append(String.valueOf(vehiclenumber)).append("' ");
        return query.toString();
    }

    public String saveVehicleWashDeparture(long time, String vehiclenumber){
        final String SET_DEPARTURE_TIME = """
                UPDATE eg_deonar_vehicle_washing
                """;
        StringBuilder query = new StringBuilder(SET_DEPARTURE_TIME);
        query.append(" SET departuretime = ").append(String.valueOf(time)).append(" ");
        query.append(" WHERE vehiclenumber = '").append(String.valueOf(vehiclenumber)).append("' ");
        return query.toString();
    }

    public String getAnimalFeeDetailsWithDays(CollectionSearchCriteria criteria, List<Object> preparedStmtList){

        final String REMOVALFEE_QUERY = """
                select animal_details 
                """;
        StringBuilder query = new StringBuilder(REMOVALFEE_QUERY);       
        switch(criteria.getCollectionType())   {

            case  1:
                query.append(" from eg_deonar_collection_entry_fee  ");
                break;
            case  2:
                query.append(" from eg_deonar_collection_stabling_fee  ");
                break;
            case  3:
                query.append(" from eg_deonar_collection_removal_fee  ");
                break;
            case  4:
                query.append(" from eg_deonar_collection_slaughter_fee  ");
                break;            
            default:
                break;    
        }     
        if (criteria.getSearch() != null) {
            addClauseIfRequired(query, preparedStmtList);
            if(criteria.getCollectionType() == 4){
                query.append(" ddreference = ? ");
            }else{
                query.append(" arrivalid = ? ");
            }
            
            preparedStmtList.add(criteria.getSearch());
        }
        if (criteria.getLiceneceNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" licencenumber = ? ");
            preparedStmtList.add(criteria.getLiceneceNumber());
        }
        if (criteria.getMobileNumber() != null) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" mobilenumber = ? ");
            preparedStmtList.add(Long.parseLong(criteria.getMobileNumber()));
        }
        return query.toString();
    }

    public String saveEntryFee(String token, String animaltypeid, String arrivalid){
        final String SET_ACTIVE_AT_ARRIVAL = """
                UPDATE eg_deonar_animal_at_arrival an
                """;
        StringBuilder query = new StringBuilder(SET_ACTIVE_AT_ARRIVAL);
        query.append(" SET active = true ");
        query.append(" FROM eg_deonar_arrival a ");
        query.append(" WHERE a.id = an.arrivalid ").append(" AND a.arrivalid = '").append(arrivalid).append("' AND an.animaltypeid = ").append(animaltypeid).append(" AND an.token = ").append(token);
        return query.toString();
    }

    }
