package ro.pub.acs.mobiway.utils;

public interface Constants {
	public static final String URL_OSRM_MAPZEN_API = "http://osrm.mapzen.com";

	public static final String URL_OSRM_API = "http://osrm.mapzen.com/foot";

	// Note: You may need to change the URL to point to your local server
	// If you want to test things locally
	public static final String URL_OSRM_API_LOCAL = "http://192.168.122.136:5000";
	public static final String URL_PGROUTING_API = "http://192.168.122.136:80";
	
	public static final int OSRM_CAR_PORT = 5000;
	public static final int OSRM_BICYCLE_PORT = 5001;
	public static final int OSRM_FOOT_PORT = 5002;
	public static final String OSRM_BICYCLE_PARAMETER = "Bicycle";
	public static final String OSRM_FOOT_PARAMETER = "Foot";
	public static final String URL_NOMINATIM_API = "http://nominatim.openstreetmap.org";
	public static final String URL_NOMINATIM_API_LOCAL = "http://osm-server/nominatim";

	public static final String GOOGLE_MAPS_GEOCODING_API_KEY = "AIzaSyDZUv7GWh1h7Cuvw0pNVw4g3NkDV7SIlIs";
	
	public static final boolean DEBUG_MODE = true;

	public static final String FACEBOOK_PAGE_ID = "248250162360256";
	public static final String MOBIWAY_PAGE_ACCESS_TOKEN = "EAAXClqgaZBY0BAELwB3F4JRtUSzKIRUBOdc9vuRX8N3pLRqYh9YW9is2G6FH0DY6SohIxMS9qPsWSAL6GLc8NLhD5rS68rqTlFjZA3A2ZAm3CwRKU3Dt8bZBRR14yINvJRCZAANkA4iyc5lYxtdRuyf8JihXVE2XGqKrW9wEARAZDZD";

}
