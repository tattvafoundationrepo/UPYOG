package digit.constants;

public class DeonarConstant {


    // Vehicle Parking
    public static String SAVE_VEHICLE_PARKING = "save-vehicle-parking";
    public static String VEHICLE_NUMBER = "vehiclenumber";
    public static String VEHICLE_TYPE = "vehicletype";
    public static String PARKING_TIME = "parkingtime";
    public static String DEPARTURE_TIME = "departuretime";
    public static String WHERE = " WHERE ";
    public static String AND = " AND ";
    public static String ASIA_KOLKATA = "Asia/Kolkata";

    public static final String THREE_WHEELER = "Lorry/Truck/Tempo/Car/Three Wheeler";
    public static final String TWO_WHEELER = "Two Wheeler (Apart from Bicycle)";

    public static String SAVE_MONTHLY_VEHICLE_PARKING_FEE = "topic-monthly-parking-savefee";

    public static String START_DATE = "startdate";
    public static String END_DATE = "enddate";
    public static String MONTHLY_FEE = "monthlyfee";

    //parking type
    public static final String DAY_ONLY = "DAY_ONLY";
    public static final String DAY_NIGHT = "DAY_NIGHT";

    //monthly charges
    public static final long LORRY_TRUCK_TEMPO_CAR_THREE_WHEELER_MONTHLY_DAY_CHARGES = 1820;
    public static final long TWO_WHEELER_MONTHLY_DAY_CHARGES = 640;

    public static final long LORRY_TRUCK_TEMPO_CAR_THREE_WHEELER_MONTHLY_DAY_NIGHT_CHARGES = 4920;
    public static final long TWO_WHEELER_MONTHLY_DAY_NIGHT_CHARGES = 1960;


    //day parking fee
    public static final long LORRY_TRUCK_TEMPO_CAR_THREE_WHEELER_DAY_RATE = 83;
    public static final long TWO_WHEELER_DAY_RATE = 33;

   //night parking fee
    public static final long THREE_WHEELER_OVERNIGHT_LESS_2H = 83;
    public static final long THREE_WHEELER_OVERNIGHT_MORE_2H = 130;
    public static final long THREE_WHEELER_OVERNIGHT_AFTER_6AM = 213;

    public static final long TWO_WHEELER_OVERNIGHT_LESS_2H = 33;
    public static final long TWO_WHEELER_OVERNIGHT_MORE_2H = 60;
    public static final long TWO_WHEELER_OVERNIGHT_AFTER_6AM = 93;

    //washing fee
    public static final long CHARGES_FOR_WASHING_PRIVATE_MEAT_VAN = 129;
    public static final long PENALTY_FOR_WASHING_UNAUTHORIZED_VAN = 720;
}