package ro.pub.acs.traffic.utils;

public interface Constants {
	public static final String URL_OSRM_MAPZEN_API = "http://osrm.mapzen.com";

	public static final String URL_OSRM_API = "http://osrm.mapzen.com/foot";
	public static final String URL_OSRM_API_LOCAL = "http://192.168.122.136:5000";
	public static final String URL_PGROUTING_API = "http://192.168.122.136:80";
	
	public static final int OSRM_CAR_PORT = 5000;
	public static final int OSRM_BICYCLE_PORT = 5001;
	public static final int OSRM_FOOT_PORT = 5002;
	public static final String OSRM_BICYCLE_PARAMETER = "Bicycle";
	public static final String OSRM_FOOT_PARAMETER = "Foot";
	public static final String URL_NOMINATIM_API = "https://nominatim.openstreetmap.org";
	public static final String URL_NOMINATIM_API_LOCAL = "http://osm-server/nominatim";

}