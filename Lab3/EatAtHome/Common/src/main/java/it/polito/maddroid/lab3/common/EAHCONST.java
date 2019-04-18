package it.polito.maddroid.lab3.common;


public class EAHCONST {
    
    // String cost to use as keys and values for intents extras
    public static String LAUNCH_APP_KEY = "LAUNCH_APP_KEY";
    public static String LAUNCH_APP_RESTAURATEUR = "LAUNCH_APP_RESTAURATEUR";
    public static String LAUNCH_APP_USER = "LAUNCH_APP_USER";
    public static String LAUNCH_APP_RIDER = "LAUNCH_APP_RIDER";
    
    public static String LAUNCH_ACTIVITY_KEY = "LAUNCH_ACTIVITY_KEY";
    
    // main subtrees
    final static String USERS_SUB_TREE = "users";
    final static String RESTAURANTS_SUB_TREE = "restaurants";
    
    // users subtree nodes fields
    final static String USERS_NAME = "username";
    final static String USERS_UUID = "uuid";
    final static String USERS_TYPE = "usertype";
    
    // restaurants subtree nodes fields
    final static String RESTAURANT_NAME = "name";
    final static String RESTAURANT_ADDRESS = "address";
    final static String RESTAURANT_DESCRIPTION = "decription";
    
    
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
