package it.polito.maddroid.lab3.common;


public class EAHCONST {


    // String cost to use as keys and values for intents extras
    public static String LAUNCH_APP_KEY = "LAUNCH_APP_KEY";
    public static String LAUNCH_APP_RESTAURATEUR = "LAUNCH_APP_RESTAURATEUR";
    public static String LAUNCH_APP_USER = "LAUNCH_APP_USER";
    public static String LAUNCH_APP_RIDER = "LAUNCH_APP_RIDER";
    
    public static String LAUNCH_ACTIVITY_KEY = "LAUNCH_ACTIVITY_KEY";
    
    public static String NOTIFICATION_KEY = "NOTIFICATION_KEY";
    
    public static String LAUNCH_EDIT_ENABLED_KEY = "LAUNCH_EDIT_ENABLED_KEY";
    public static String ACCOUNT_INFO_EMPTY = "ACCOUNT_INFO_EMPTY";
    
    public static String RESTAURANT_CATEGORY_EXTRA = "RESTAURANT_CATEGORY_EXTRA";
    
    public static final float DELIVERY_COST = 4.50f;
    
    // CONSTS RELATED TO FIREBASE ONLINE DATABASE
    public enum USER_TYPE {
        CUSTOMER,
        RESTAURATEUR,
        RIDER
    }
    
    public enum OrderStatus {
        ONGOING,
        CONFIRMED,
        WAITING_RIDER,
        PENDING,
        COMPLETED,
        DECLINED
    }
    
    public enum RiderStatus {
        ON_DUTY,
        NOT_ON_DUTY
    }

    // main subtrees
    public final static String USERS_SUB_TREE = "users";
    public final static String CATEGORIES_ASSOCIATIONS_SUB_TREE = "categories_associations";
    public final static String RESTAURANTS_SUB_TREE = "restaurants";
    public final static String CATEGORIES_SUB_TREE = "restaurant_categories";
    public final static String CUSTOMERS_SUB_TREE = "customers";
    public final static String RIDERS_SUB_TREE = "riders";
    public final static String DISHES_SUB_TREE = "dishes";
    public final static String RESTAURANTS_TIMETABLES_SUB_TREE = "restaurants_timetables";
    public final static String ORDERS_REST_SUBTREE = "orders_restaurateur";
    public final static String ORDERS_CUST_SUBTREE = "orders_customer";
    public final static String ORDERS_RIDER_SUBTREE = "orders_rider";
    public final static String RIDERS_POSITIONS_SUBTREE = "riders_positions";

    // users subtree nodes fields
    public final static String USERS_MAIL = "email";
    public final static String USERS_TYPE = "usertype";
    public final static String USERS_TOKEN = "token";
    
    // restaurants subtree nodes fields
    public final static String RESTAURANT_NAME = "name";
    public final static String RESTAURANT_ADDRESS = "address";
    public final static String RESTAURANT_DESCRIPTION = "decription";
    public final static String RESTAURANT_PHONE = "phone";
    public final static String RESTAURANT_EMAIL = "email";
    public final static String RESTAURANT_CATEGORIES = "restaurant_categories";
    public final static String RESTAURANT_TIMETABLE = "time_table";
    public final static String RESTAURANT_POSITION = "position";

    // customers subtree nodes fields
    public final static String CUSTOMER_NAME = "name";
    public final static String CUSTOMER_ADDRESS = "address";
    public final static String CUSTOMER_DESCRIPTION = "decription";
    public final static String CUSTOMER_PHONE = "phone";
    public final static String CUSTOMER_EMAIL = "email";
  
    // riders subtree nodes fields
    public final static String RIDER_NAME = "name";
    public final static String RIDER_ADDRESS = "address";
    public final static String RIDER_DESCRIPTION = "decription";
    public final static String RIDER_PHONE = "phone";
    public final static String RIDER_EMAIL = "email";
    public final static String RIDER_ON_DUTY = "on_duty_status";

    // categories subtree nodes fields
    public final static String CATEGORIES_NAME = "name";

    // dishes (menu) subtree nodes fields
    public final static String DISH_NAME = "name";
    public final static String DISH_PRICE = "price";
    public final static String DISH_DESCRIPTION = "description";
    public final static String DISH_ID = "dishID";
    
    // restaurants orders subtree nodes fields
    public final static String REST_ORDER_STATUS = "order_status";
    public final static String REST_ORDER_DATE = "date";
    public final static String REST_ORDER_DELIVERY_TIME = "delivery_time";
    public final static String REST_ORDER_CUSTOMER_ID = "customer_id";
    public final static String REST_ORDER_RIDER_ID = "riderId";
    public final static String REST_ORDER_DISHES_SUBTREE = "dishes";
    public final static String REST_ORDER_TOTAL_COST = "total_cost";
    public final static String REST_ORDER_DELIVERY_ADDRESS = "delivery_address";
    
    // customers order subtree nodes fields
    public final static String CUST_ORDER_STATUS = "order_status";
    public final static String CUST_ORDER_RESTAURATEUR_ID = "restaurateur_id";
    public final static String CUST_ORDER_RIDER_ID = "rider_id";
    
    // riders order subtree nodes fields
    public final static String RIDER_ORDER_STATUS = "order_status";
    public final static String RIDER_ORDER_RESTAURATEUR_ID = "restaurateur_id";
    public final static String RIDER_ORDER_CUSTOMER_ID = "customer_id";
    
    public static String generatePath(String... args) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < args.length; ++i) {
            sb.append(args[i]);
            if (i != args.length -1)
                sb.append("/");
        }
        
        return sb.toString();
    }
}
