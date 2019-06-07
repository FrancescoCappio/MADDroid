package it.polito.maddroid.lab3.common;


import java.io.Serializable;


public class EAHCONST {



    // String cost to use as keys and values for intents extras
    public static String LAUNCH_APP_KEY = "LAUNCH_APP_KEY";
    public static String LAUNCH_APP_RESTAURATEUR = "LAUNCH_APP_RESTAURATEUR";
    public static String LAUNCH_APP_USER = "LAUNCH_APP_USER";
    public static String LAUNCH_APP_RIDER = "LAUNCH_APP_RIDER";
    public static String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String LAUNCH_ACTIVITY_KEY = "LAUNCH_ACTIVITY_KEY";
    
    public static String NOTIFICATION_KEY = "NOTIFICATION_KEY";
    public static String ARRAY_INCOME_KEY = "ARRAY_INCOME_KEY";
    public static String LAUNCH_EDIT_ENABLED_KEY = "LAUNCH_EDIT_ENABLED_KEY";
    public static String ACCOUNT_INFO_EMPTY = "ACCOUNT_INFO_EMPTY";
    
    public static String RESTAURANT_CATEGORY_EXTRA = "RESTAURANT_CATEGORY_EXTRA";
    
    public static final float DELIVERY_COST = 2.00f;
    
    public static final int DEFAULT_IMAGE_SIZE = 1000;
    
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
    
    public static class GeoLocation implements Serializable {
        private double latitude;
        private double longitude;
    
        public GeoLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    
        public double getLatitude() {
            return latitude;
        }
    
        public double getLongitude() {
            return longitude;
        }
    }
    
    public static class Review implements Comparable<Review>{
        private String authorUID;
        private int rate;
        private String date;
        private String comment;
        private String authorName;
    
        public Review(String authorUID, int rate, String comment, String date) {
            this.authorUID = authorUID;
            this.rate = rate;
            this.comment = comment;
            this.date = date;
        }
        
        public Review(){}
    
        public String getAuthorUID() {
            return authorUID;
        }
    
        public int getRate() {
            return rate;
        }
    
        public String getComment() {
            return comment;
        }
    
        public String getDate() {
            return date;
        }
    
        public String getAuthorName() {
            return authorName;
        }
    
        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }
    
        @Override
        public int compareTo(Review o) {
            return this.getDate().compareTo(o.getDate());
        }
    }
    
    // main subtrees
    public final static String USERS_SUB_TREE = "users";
    public final static String CATEGORIES_ASSOCIATIONS_SUB_TREE = "categories_associations";
    public final static String RESTAURANTS_SUB_TREE = "restaurants";
    public final static String CATEGORIES_SUB_TREE = "restaurant_categories";
    public final static String CUSTOMERS_SUB_TREE = "customers";
    public final static String RIDERS_SUB_TREE = "riders";
    public final static String RIDERS_INCOME_SUB_TREE = "riders_income";
    public final static String DISHES_SUB_TREE = "dishes";
    public final static String RESTAURANTS_TIMETABLES_SUB_TREE = "restaurants_timetables";
    public final static String ORDERS_REST_SUBTREE = "orders_restaurateur";
    public final static String ORDERS_CUST_SUBTREE = "orders_customer";
    public final static String ORDERS_RIDER_SUBTREE = "orders_rider";
    public final static String RIDERS_POSITIONS_SUBTREE = "riders_positions";
    public final static String RIDERS_RATINGS_SUBTREE = "riders_ratings";
    public final static String RESTAURANTS_RATINGS_SUBTREE = "restaurants_ratings";
    public final static String RATINGS_OF_CUSTOMERS_SUBTREE = "ratings_of_customers";
    
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
    public final static String RESTAURANT_REVIEW_COUNT = "review_count";
    public final static String RESTAURANT_REVIEW_AVG = "review_avg";
    public final static String RESTAURANT_AVG_ORDER_TIME = "average_order_minutes";

    // customers subtree nodes fields
    public final static String CUSTOMER_NAME = "name";
    public final static String CUSTOMER_ADDRESS = "address";
    public final static String CUSTOMER_ADDRESS_NOTES = "address_notes";
    public final static String CUSTOMER_DESCRIPTION = "decription";
    public final static String CUSTOMER_PHONE = "phone";
    public final static String CUSTOMER_EMAIL = "email";
    public final static String CUSTOMER_POSITION = "position";
    public final static String CUSTOMER_FAVORITE_RESTAURANT = "favorite_restaurant";

    // riders subtree nodes fields
    public final static String RIDER_NAME = "name";
    public final static String RIDER_ADDRESS = "address";
    public final static String RIDER_DESCRIPTION = "decription";
    public final static String RIDER_PHONE = "phone";
    public final static String RIDER_EMAIL = "email";
    public final static String RIDER_ON_DUTY = "on_duty_status";
    public final static String RIDER_REVIEW_COUNT = "review_count";
    public final static String RIDER_REVIEW_AVG = "review_avg";
    public final static String RIDER_DAILY_INCOME = "rider_income";

    // categories subtree nodes fields
    public final static String CATEGORIES_NAME = "name";

    // dishes (menu) subtree nodes fields
    public final static String DISH_NAME = "name";
    public final static String DISH_PRICE = "price";
    public final static String DISH_DESCRIPTION = "description";
    public final static String DISH_COUNT = "dish_count";
    public final static String DISH_ID = "dishID";
    
    // restaurants orders subtree nodes fields
    public final static String REST_ORDER_STATUS = "order_status";
    public final static String REST_ORDER_DATE = "date";
    public final static String REST_ORDER_DELIVERY_TIME = "delivery_time";
    public final static String REST_ORDER_CUSTOMER_ID = "customer_id";
    public final static String REST_ORDER_RIDER_ID = "riderId";
    public final static String REST_ORDER_CONTROL_STRING = "string_order";
    public final static String REST_ORDER_DISHES_SUBTREE = "dishes";
    public final static String REST_ORDER_TOTAL_COST = "total_cost";
    public final static String REST_ORDER_DELIVERY_ADDRESS = "delivery_address";
    public final static String REST_ORDER_DELIVERY_ADDRESS_NOTES = "delivery_address_notes";
    public final static String REST_ORDER_TIME_FOR_DELIVERY = "time_for_delivery";
    public final static String REST_ORDER_RIDER_RATED = "rider_rated";
    
    // customers order subtree nodes fields
    public final static String CUST_ORDER_STATUS = "order_status";
    public final static String CUST_ORDER_RESTAURATEUR_ID = "restaurateur_id";
    public final static String CUST_ORDER_RIDER_ID = "rider_id";
    public final static String CUST_ORDER_DELIVERY_POS = "position";
    public final static String CUST_ORDER_RIDER_RATED = "rider_rated";
    public final static String CUST_ORDER_RESTAURANT_RATED = "restaurant_rated";

    // riders order subtree nodes fields
    public final static String RIDER_ORDER_STATUS = "order_status";
    public final static String RIDER_KM_REST = "order_km_rider_restaurant";
    public final static String RIDER_ORDER_DATE = "order_rider_date";
    public final static String RIDER_KM_REST_CUST = "order_km_restaurant_cust";
    public final static String RIDER_INCOME = "rider_income_tot";
    public final static String RIDER_ORDER_RESTAURATEUR_ID = "restaurateur_id";
    public final static String RIDER_ORDER_CUSTOMER_ID = "customer_id";
    
    // ratings of customers nodes fields
    public final static String RIDERS_RATINGS = "riders_ratings";
    public final static String RESTAURANT_RATINGS = "restaurant_ratings";
    
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
