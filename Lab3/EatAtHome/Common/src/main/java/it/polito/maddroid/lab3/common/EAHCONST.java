package it.polito.maddroid.lab3.common;


public class EAHCONST {
    
    // String cost to use as keys and values for intents extras
    public static String LAUNCH_APP_KEY = "LAUNCH_APP_KEY";
    public static String LAUNCH_APP_RESTAURATEUR = "LAUNCH_APP_RESTAURATEUR";
    public static String LAUNCH_APP_USER = "LAUNCH_APP_USER";
    public static String LAUNCH_APP_RIDER = "LAUNCH_APP_RIDER";
    
    public static String LAUNCH_ACTIVITY_KEY = "LAUNCH_ACTIVITY_KEY";
    
    public static String LAUNCH_EDIT_ENABLED_KEY = "LAUNCH_EDIT_ENABLED_KEY";
    public static String ACCOUNT_INFO_EMPTY = "ACCOUNT_INFO_EMPTY";
    
    public static String RESTAURANT_CATEGORY_EXTRA = "RESTAURANT_CATEGORY_EXTRA";
    
    
    // CONSTS RELATED TO FIREBASE ONLINE DATABASE
    public enum USER_TYPE {
        CUSTOMER,
        RESTAURATEUR,
        RIDER
    }
    
    // main subtrees
    public final static String USERS_SUB_TREE = "users";
    public final static String CATEGORIES_ASSOCIATIONS_SUB_TREE = "categories_associations";
    public final static String RESTAURANTS_SUB_TREE = "restaurants";
    public final static String CATEGORIES_SUB_TREE = "restaurant_categories";
    public final static String CUSTOMERS_SUB_TREE = "customers";
    public final static String RIDERS_SUB_TREE = "riders";
    
    // users subtree nodes fields
    public final static String USERS_MAIL = "email";
    public final static String USERS_TYPE = "usertype";
    
    // restaurants subtree nodes fields
    public final static String RESTAURANT_NAME = "name";
    public final static String RESTAURANT_ADDRESS = "address";
    public final static String RESTAURANT_DESCRIPTION = "decription";
    public final static String RESTAURANT_PHONE = "phone";
    public final static String RESTAURANT_EMAIL = "email";
    public final static String RESTAURANT_TIMETABLE = "time_table";

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
    
    // categories subtree nodes fields
    public final static String CATEGORIES_NAME = "name";

    
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
