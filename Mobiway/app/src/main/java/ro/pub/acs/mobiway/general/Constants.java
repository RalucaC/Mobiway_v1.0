package ro.pub.acs.mobiway.general;

public abstract class Constants {
    /*   Application Settings   */

    /*   Application Constants   */
    public static final String LANG_EN = "en";
    public static final String EMPTY_STRING = "";
    public static final String EMPTY_ARRAY = "[]";
    public static final String ENCODING = "UTF-8";


    /* Google Map */
    public static final int CAMERA_ZOOM = 14;
    public static long LOCATION_REQUEST_INTERVAL = 30000;
    public static long LOCATION_REQUEST_FASTEST_INTERVAL = 30000;

    /*   Application Keys and IDs   */


    /* URLs for WEB Services */
    public static final String APP_NAME = "Mobiway";
    public static final String SERVICES_URL = "http://192.168.100.5:8080/MobiwayServer/services";
//    public static final String SERVICES_URL = "http://mobiway.hpc.pub.ro:8082/TrafficCollectorServer/services";

    public static final String URL_CHECK_SERVER_CONN = "/checkServerConn";
    public static final String URL_GET_POLICY_APP = "/authenticate/getPolicyListForApp";
    public static final String URL_GET_POLICY_USER_APP = "/authenticate/getUserPolicyListForApp";
    public static final String URL_ACCEPT_POLICY_APP = "/authenticate/acceptUserPolicyListForApp";


    public static final String MAPS_API_GEOCODE = "http://maps.google.com/maps/api/geocode/json?address=";

    public static final String URL_LOG_IN = "/authenticate/userpass";
    public static final String URL_AUTH_FACEBOOK = "/authenticate/facebook";
    public static final String URL_CREATE_ACCOUNT = "/signup/userpass";
    public static final String URL_UPDATE_USER = "/user/update";
    public static final String URL_GET_USER = "/user/getUser";
    public static final String URL_UPDATE_LOCATION = "/location/update";
    public static final String URL_UPDATE_LOCATIONS = "/location/saveHistory";
    public static final String URL_GET_SPEED_STATISTICS = "/location/getSpeedStatistics";
    public static final String URL_GET_FRIENDS_NAMES = "/social/getFriendsNames";
    public static final String URL_GET_FRIENDS_LOCATIONS = "/social/getFriendsLocations";
    public static final String URL_GET_USERS_WITH_PHONE = "/social/getUsersWithPhone";
    public static final String URL_ADD_FRIENDS = "/social/addFriends";
    public static final String URL_GET_NEARBY_LOCATIONS = "/social/getNearbyLocations";
    public static final String URL_GET_ROUTE = "/social/getRoute";
    public static final String URL_GET_ROUTE_PG = "/social/getRoutePG";
    public static final String URL_GET_ROUTE_FROM_MAPZEN = "/social/getRouteFromMapzen";

    public static final String URL_NEW_JOURNEY = "/location/newJourney";

    /* SharedPreferences file name */
    public static final String PREF_NAME = "mobiwayPreferences";

    /* All Shared Preferences Keys */
    public static final String IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_EMAIL = "emailAddress";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_LASTNAME = "lastName";
    public static final String KEY_FIRSTNAME = "firstName";
    public static final String KEY_AUTH_TOKEN = "token";
    public static final String KEY_AUTH_TOKEN_FB = "tokenFB";
    public static final String KEY_EXPIRES_ON = "expiresOn";
    public static final String KEY_EXPIRES_IN_FB = "expiresInFB";
    public static final String KEY_CURRENT_LANGUAGE = "currentLanguage";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_NOTIFICATIONS_ENABLED = "notifEnabled";
    public static final String KEY_SHARE_LOCATION = "Share Location";
    public static final String KEY_SHARE_SPEED = "Share Speed";
    public static final String KEY_FIRST_TIME_USE = "firstTimeUse";
    public static final String KEY_MAP_TYPE = "mapType";
    public static final String KEY_ROUTE_TYPE = "routeType";
    public static final String KEY_LOCATION_HISTORY = "locationHistory";
    public static final String KEY_LOC_PREFERENCES = "locationPreferences";
    public static final String KEY_POLICY_PREFERENCES = "policyPreferences";
}