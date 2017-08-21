package ro.pub.acs.mobiway.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.gson.JsonObject;
import javafx.util.Pair;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ro.pub.acs.mobiway.dao.JourneyDAO;
import ro.pub.acs.mobiway.dao.JourneyDataDAO;
import ro.pub.acs.mobiway.dao.LocationDAO;
import ro.pub.acs.mobiway.dao.PolicyDAO;
import ro.pub.acs.mobiway.dao.TrafficEventDAO;
import ro.pub.acs.mobiway.dao.UserContactDAO;
import ro.pub.acs.mobiway.dao.UserDAO;
import ro.pub.acs.mobiway.dao.UserEventDAO;
import ro.pub.acs.mobiway.dao.UserPolicyDAO;
import ro.pub.acs.mobiway.model.*;
import ro.pub.acs.mobiway.utils.Constants;

@SuppressWarnings("deprecation")
@RestController
@Transactional
@RequestMapping("/services")
public class ServicesController {

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private LocationDAO locationDAO;

	@Autowired
	private UserContactDAO userContactDAO;

	@Autowired
	private JourneyDAO journeyDAO;

	@Autowired
	private JourneyDataDAO journeyDataDAO;
	
	@Autowired
	private PolicyDAO policyDAO;
	
	@Autowired
	private UserPolicyDAO userPolicyDAO;
	
	@Autowired
	private TrafficEventDAO trafficEventDAO;

	@Autowired
	private UserEventDAO userEventDAO;

	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/location/getEvent", method = RequestMethod.PUT)
	public @ResponseBody List<UserEvent> getEvent(
			@RequestBody Location location,
			@RequestHeader("X-Auth-Token") String authToken) {

		User user = userDAO.get(authToken, location.getIdUser());
		if (user == null) {
			return null;
		}

		String osmId = getOSMId(location);
		List<UserEvent> event = userEventDAO.get(osmId);

		return event;
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/location/postEvent/{eventName}/{distance}/{timeSinceEvent}/{spaceAccuracy}/{timeAccuracy}", method = RequestMethod.PUT)
	public boolean postEvent(
			@PathVariable String eventName,
			@PathVariable Float distance,
			@PathVariable Float timeSinceEvent,
			@PathVariable Float spaceAccuracy,
			@PathVariable Float timeAccuracy,
			@RequestBody Location location,
			@RequestHeader("X-Auth-Token") String authToken
			) {

		User user = userDAO.get(authToken, location.getIdUser());
		if (user == null) {
			return false;
		}

		TrafficEvent te = trafficEventDAO.get(eventName);
		if (te == null) {
			return false;
		}

		Date currentDate = new Date();
		String osmWayId = getOSMId(location);

		UserEvent event = new UserEvent();
		event.setIdUser(user);
		event.setIdTrafficEvent(te);
		event.setTimestamp(currentDate);
		event.setDistance(distance);
		event.setSpaceAccuracy(spaceAccuracy);
		event.setTimeAccuracy(timeAccuracy);
		event.setLatitude((double)location.getLatitude());
		event.setLongitude((double)location.getLongitude());
		event.setOsmWayId(osmWayId);
		userEventDAO.add(event);


		return true;
	}
	
	@RequestMapping(value = "/checkServerConn", method = RequestMethod.GET)
	public @ResponseBody boolean checkServerConn() {
		return true;
	}
	
	@RequestMapping(value = "user/getUser/{userId}", method = RequestMethod.GET)
	public @ResponseBody User getUser(@PathVariable int userId) {
		User user = userDAO.get(userId);

		return user;
	}

	@RequestMapping(value = "/signup/userpass", method = RequestMethod.POST)
	public @ResponseBody User userPassCreateAccount(@RequestBody User user) {
		User userExists = userDAO.get(user.getUsername());
		if (userExists != null) {
			return null;
		}

		String uuid = UUID.randomUUID().toString();
		DateTime dateTime = new DateTime();
		DateTime threeMontsLater = dateTime.plusMonths(3);
		long seconds = Seconds.secondsBetween(dateTime, threeMontsLater)
				.getSeconds();
		user.setAuth_token(uuid);
		user.setUuid(uuid);
		user.setAuthExpiresIn((int)seconds);
		user.setFacebookExpiresIn(0);

		/* Save and return the new user */
		int userId = userDAO.add(user);
		User newUser = userDAO.get(userId);

		return newUser;
	}

	@RequestMapping(
		value = "/authenticate/getPolicyListForApp/{appId}",
		method = RequestMethod.GET)
	public @ResponseBody List<Policy> getPolicyListForApp(
			@PathVariable String appId) {

		List<Policy> policies = policyDAO.list(appId);
		return policies;
	}

	@RequestMapping(
			value = "/authenticate/getUserPolicyListForApp/{userId}/{appId}",
			method = RequestMethod.GET)
	public @ResponseBody List<Policy> getUserPolicyListForApp(
			@PathVariable Integer userId,
			@PathVariable String appId,
			@RequestHeader("X-Auth-Token") String authToken) {

		List <Policy> acceptedPolicies = new ArrayList<Policy>();
		User user = userDAO.get(authToken, userId);
		
		if (user != null) {
			List<UserPolicy> userPolicies =
			userPolicyDAO.getUserAcceptedPoliciesByApp(user, appId);
			
			for (UserPolicy userp : userPolicies) {
				Policy policy = policyDAO.get(userp.getId());
				acceptedPolicies.add(policy);
			}
		}

		return acceptedPolicies;
	}

	@RequestMapping(value = "/authenticate/acceptUserPolicyListForApp/{userId}/{appId}",
			method = RequestMethod.POST)
	public @ResponseBody boolean acceptPolicyListForApp(
			@PathVariable Integer userId,
			@PathVariable String appId,
			@RequestHeader("X-Auth-Token") String authToken,
			@RequestBody List<String> policyList) {

		User user = userDAO.get(authToken, userId);
		
		if (user != null) {
			userPolicyDAO.clearPolicies(user, appId);
			
			for (String policyName : policyList) {
				Policy policy = policyDAO.get(policyName, appId);
				UserPolicy up = new UserPolicy();
				
				up.setIdUser(user);
				up.setAppId(appId);
				up.setIdPolicy(policy);
				userPolicyDAO.add(up);
			}
		}
		
		return true;
	}

	@RequestMapping(value = "/authenticate/facebook", method = RequestMethod.POST)
	public @ResponseBody User loginWithFacebook(@RequestBody User user) {
		/* Get Facebook profile */
			Facebook facebook = new FacebookTemplate(user.getFacebook_token());
			org.springframework.social.facebook.api.User profile = facebook.userOperations().getUserProfile();

			if (profile == null || profile.getEmail() == null
					|| !user.getUsername().equals(profile.getEmail())) {
				return null;
			}

			String uuid = UUID.randomUUID().toString();
			DateTime dateTime = new DateTime(0);
			DateTime threeMonthsLater = new DateTime().plusMonths(3);
			long seconds = Seconds.secondsBetween(dateTime, threeMonthsLater)
					.getSeconds();
			user.setAuth_token(uuid);
			user.setAuthExpiresIn((int) seconds);
			user.setUuid(uuid);

		/* Save and return the new user */
			int userId;

		/* Update user into database */
			User oldUser = userDAO.get(user.getUsername());
			if (oldUser != null) {
				userDAO.delete(oldUser);
			}

			userId = userDAO.add(user);
			User newUser = userDAO.get(userId);

		/* Get friend from Facebook and insert into database */
			List<String> friendIds = facebook.friendOperations().getFriendIds();

			for (String friendId : friendIds) {
				User userFriend = userDAO.get(friendId);
				if (userFriend != null) {
					UserContact userContact1 = new UserContact();
					UserContact userContact2 = new UserContact();
					userContact1.setIdUser(newUser);
					userContact1.setIdFriendUser(userFriend);
					userContact2.setId(userFriend.getId());
					userContact2.setIdFriendUser(newUser);

					userContactDAO.addFriend(userContact1);
					userContactDAO.addFriend(userContact2);
				}
			}

			return newUser;
	}

	@RequestMapping(value = "/authenticate/userpass", method = RequestMethod.POST)
	public @ResponseBody User loginWithUserAndPass(@RequestBody User user) {
		User oldUser = userDAO.get(user.getUsername(), user.getPassword());
		if (oldUser == null) {
			return null;
		}

		String uuid = UUID.randomUUID().toString();
		DateTime dateTime = new DateTime(0);
		DateTime threeMonthsLater = new DateTime().plusMonths(3);
		long seconds = Seconds.secondsBetween(dateTime, threeMonthsLater)
				.getSeconds();
		oldUser.setAuth_token(uuid);
		oldUser.setAuthExpiresIn((int)seconds);

		/* Save and return the new user */
		userDAO.update(oldUser);

		return oldUser;
	}
	
	@RequestMapping(value = "/authenticate/resetPassword", method = RequestMethod.POST)
	public @ResponseBody User resetPassword(@RequestBody User user) {
		User oldUser = userDAO.get(user.getUsername());
		if (oldUser == null) {
			return null;
		}

                oldUser.setPassword(user.getPassword());
		userDAO.update(oldUser);
		return oldUser;
	}

	@RequestMapping(value = "/location/newJourney", method = RequestMethod.POST)
	public @ResponseBody boolean newJourney(@RequestBody Integer userId,
			@RequestHeader("X-Auth-Token") String authToken) {
		User user = userDAO.get(authToken, userId);

		if (user != null) {
			Journey journey = new Journey();
			journey.setIdUser(user);
			journey.setJourneyName("journey_" + Calendar.getInstance().getTimeInMillis());
			journeyDAO.add(journey);

			return true;
		}

		return false;
	}
	
	private void conformDataToPolicy(User user, String appId, Location location) {
		List<UserPolicy> userAcceptedPolicies =
				userPolicyDAO.getUserAcceptedPoliciesByApp(user, appId);
	
		boolean acceptedShareLocPolicy   = false;
		boolean acceptedShareSpeedPolicy = false;
	
		for (UserPolicy up : userAcceptedPolicies) {
			String policyName = up.getIdPolicy().getPolicyName();
	
			if (policyName.equals("Share Location")) {
				acceptedShareLocPolicy = true;
			}
	
			if (policyName.equals("Share Speed")) {
				acceptedShareSpeedPolicy = true;
			}
		}
	
		if (!acceptedShareLocPolicy) {
			location.setLatitude(null);
			location.setLongitude(null);
		}
		
		if (!acceptedShareSpeedPolicy) {
			location.setSpeed(null);
		}
	}

	@Autowired
	private StreetSpeeds streetSpeeds;

	@RequestMapping(value = "/position/update", method = RequestMethod.PUT)
	public @ResponseBody boolean updatePosition(@RequestBody Position position,
												@RequestHeader("X-Auth-Token") String authToken) {

		HttpClient httpClient = new DefaultHttpClient();

		/* Perform Reverse Geocoding for a location */
		StringBuilder url = new StringBuilder();

		url.append("https://maps.googleapis.com/maps/api/geocode/json?latlng=");
		url.append(position.getLatitude());
		url.append(",");
		url.append(position.getLongitude());

		url.append("&key=");
		url.append(Constants.GOOGLE_MAPS_GEOCODING_API_KEY);

		url.append("&location_type=ROOFTOP");
		url.append("&result_type=street_address");
		url.append("&components=route");

		HttpGet httpGet = new HttpGet(url.toString());

		HttpResponse httpGetResponse = null;
		try {
			httpGetResponse = httpClient.execute(httpGet);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		HttpEntity httpGetEntity = httpGetResponse.getEntity();

		String addressName = null;

		if (httpGetEntity != null) {
			String response = null;
			try {
				response = EntityUtils.toString(httpGetEntity);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			try {
				JSONObject data = new JSONObject(response);
				JSONArray results = data.getJSONArray("results");
				if (results.length() > 0) {
					JSONObject firstResult = results.getJSONObject(0);
					JSONArray addressComponents = firstResult.getJSONArray("address_components");
					if (addressComponents.length() > 0) {
						for (int j = 0; j < addressComponents.length(); j++) {
							JSONObject addressComponent = addressComponents.getJSONObject(j);
							JSONArray types = addressComponent.getJSONArray("types");

							boolean hasRouteType = false;
							if (types.length() > 0) {
								for (int k = 0; k < types.length(); k++) {
									String type = types.getString(k);

									if (type.equals("route")) {
										hasRouteType = true;
									}
								}
							}

							if (hasRouteType) {
								addressName = addressComponent.getString("short_name");
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (addressName != null) {
				System.out.println(addressName);
				System.out.println(position.getSpeed());

				Map<String, Pair<Float, Integer>> streetToSpeedAndNumberOfMeasures = streetSpeeds.getStreetToSpeed();

				if (streetToSpeedAndNumberOfMeasures.containsKey(addressName)) {
					Pair<Float, Integer> speedAndNumberOfMeasures = streetToSpeedAndNumberOfMeasures.get(addressName);
					Float oldSpeedMean = speedAndNumberOfMeasures.getKey();
					Integer oldNoOfMeasures = speedAndNumberOfMeasures.getValue();
					Float newSpeedMean = oldSpeedMean + (position.getSpeed() - oldSpeedMean) / oldNoOfMeasures;
					streetToSpeedAndNumberOfMeasures.put(addressName, new Pair<>(newSpeedMean, oldNoOfMeasures + 1));
				} else {
					streetToSpeedAndNumberOfMeasures.put(addressName, new Pair<Float, Integer>(position.getSpeed(), 1));
				}
			} else {
				System.out.println("nu merge");
			}
		}
		return false;
	}

	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/location/update", method = RequestMethod.PUT)
	public @ResponseBody boolean updateLocation(@RequestBody Location location,
			@RequestHeader("X-Auth-Token") String authToken) {
		User user = userDAO.get(authToken, location.getIdUser());

		if (user != null) {
	
			String appId = "Mobiway";
			/* Make sure the published data conform to the user
			* accepted policy */
			conformDataToPolicy(user, appId, location);
			
			Location locationUser = locationDAO.getLocation(user);
			Date currentDate = new Date();

			if (locationUser != null) {
				locationUser.setLatitude(location.getLatitude());
				locationUser.setLongitude(location.getLongitude());
				locationUser.setSpeed(location.getSpeed());
				locationUser.setTimestamp(currentDate);
				locationDAO.updateLocation(locationUser);
			} else {
				location.setIdUser(user.getId());
				location.setTimestamp(new Date());
				locationDAO.addLocation(location);
			}

			Journey lastJourney = journeyDAO.getCurrentJourney(user);
			if (lastJourney != null) {
				JourneyData journeyData = new JourneyData();
				journeyData.setJourneyId(lastJourney);
				journeyData.setLatitude(location.getLatitude());
				journeyData.setLongitude(location.getLongitude());
				journeyData.setSpeed(location.getSpeed());
				journeyData.setTimestamp(currentDate);
				
				/* Set the OSM id (used later)*/
				String osmId = getOSMId(locationUser);
				
				journeyData.setOsmWayId(osmId);
				journeyDataDAO.add(journeyData);
			}

			return true;
		}

		return false;
	}

	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/location/saveHistory", method = RequestMethod.POST)
	public @ResponseBody boolean saveHistory(@RequestBody ArrayList<Location> locations,
			@RequestHeader("X-Auth-Token") String authToken) {

		if (locations == null || locations.size() == 0) {
			return true;
		}

		// The id of the user is contained inside the location
		// TODO: add it as a parameter instead
		User user = userDAO.get(authToken, locations.get(0).getIdUser());
		if (user == null) {
			return false;
		}

		// Create the Journey Object
		Journey newJourney = new Journey();
		newJourney.setIdUser(user);
		newJourney.setJourneyName("journey_" + Calendar.getInstance().getTimeInMillis());
		journeyDAO.add(newJourney);

		for (Location location : locations) {
			JourneyData journeyData = new JourneyData();
			journeyData.setJourneyId(newJourney);
			journeyData.setLatitude(location.getLatitude());
			journeyData.setLongitude(location.getLongitude());
			journeyData.setSpeed(location.getSpeed());
			journeyData.setTimestamp(location.getTimestamp());

			// Set the OSM id (used later)
			String osmId = getOSMId(location);
			journeyData.setOsmWayId(osmId);
			journeyDataDAO.add(journeyData);
		}

		return true;
	}

	private String getOSMId(Location location) {
		/* Set the OSM id (used later)*/
		String osmId = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();

			/* Perform Reverse Geocoding for a location */
			StringBuilder url = new StringBuilder();
			//url.append(Constants.URL_NOMINATIM_API_LOCAL);
			 url.append(Constants.URL_NOMINATIM_API);
			 url.append("/reverse?format=json&zoom=18&addressdetails=0");
			//url.append("/reverse.php?format=json&zoom=18&addressdetails=0");
			url.append("&lat=" + location.getLatitude());
			url.append("&lon=" + location.getLongitude());

			HttpGet httpGet = new HttpGet(url.toString());
			HttpResponse httpGetResponse = httpClient.execute(httpGet);
			HttpEntity httpGetEntity = httpGetResponse.getEntity();

			if (httpGetEntity != null) {
				String response = EntityUtils.toString(httpGetEntity);
				JSONObject nodeData = new JSONObject(response);
				osmId = nodeData.getString("osm_id");
			}
		} catch (Exception exception) {
			// Request can fail for a number of reasons
			// Mainly if the data is not available for the specified coordinates
			// exception.printStackTrace();
            int a = 3;
		}
		return osmId;
	}
	
	private void saveRouteToFile(String tag, List<Location> routePoints, Calendar start, Calendar end) {
		
		String logPath = "/var/log/routes/";

		try {
			File file = new File(logPath + new Date().getTime());

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("Route tag: " + tag);
			bw.newLine();

			for (Location loc: routePoints) {
				//bw.write(loc.getLatitude() + " " + loc.getLongitude() + " " + getOSMId(loc));
				bw.newLine();
			}

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/social/getFriendsNames", method = RequestMethod.GET)
	public @ResponseBody List<User> getFriendsNames(
			@RequestHeader("X-Auth-Token") String authToken) {
		User user = userDAO.get(authToken, 0);
		List<User> friends = new ArrayList<User>();

		if (user != null) {
			friends = userContactDAO.getFriends(user);
		}

		return friends;
	}
	
	/* Check if the reported location is within a near timeframe */
	private boolean locationIsOk(Location location) {
		
		if (location != null && location.getTimestamp() != null) {
			Date now = new Date();
			Date last = location.getTimestamp();
	
			// Only return locations if reported in the last minute
			long secondsBetween =
					(now.getTime() - last.getTime()) / 1000;
	
			if (secondsBetween >= 0 && secondsBetween <= 60) {
				return true;
			}
		}

		return false;
		
	}

	@RequestMapping(value = "/social/getFriendsLocations", method = RequestMethod.GET)
	public @ResponseBody List<Location> getFriendsLocations(
			@RequestHeader("X-Auth-Token") String authToken) {
		User user = userDAO.get(authToken, 0);
		List<User> friends = new ArrayList<User>();
		List<Location> locations = new ArrayList<Location>();

		if (user != null) {
			friends = userContactDAO.getFriends(user);
			for (User friend : friends) {
				Location location = locationDAO.getLocation(friend);
				
				if (locationIsOk(location)) {
					locations.add(location);
				}
			}
		}

		return locations;
	}

	@RequestMapping(value = "/social/getUsersWithPhone", method = RequestMethod.GET)
	public @ResponseBody List<User> getUsersWithPhone(
			@RequestHeader("X-Auth-Token") String authToken) {
		User user = userDAO.get(authToken, 0);
		List<User> users = new ArrayList<User>();

		if (user != null) {
			users = userDAO.getUsersWithPhone();
		}

		return users;
	}

	@RequestMapping(value = "/social/addFriends", method = RequestMethod.PUT)
	public @ResponseBody boolean addFriends(
			@RequestHeader("X-Auth-Token") String authToken,
			@RequestBody List<User> friends) {
		User user = userDAO.get(authToken, 0);
		List<String> oldFriends = new ArrayList<String>();
		
		if (user == null) {
			return false;
		} else {
			for (User friend : friends) {
				oldFriends = userContactDAO.getFriendsEmails(user);
				if (user.getId() != friend.getId() && !oldFriends.contains(friend.getUsername())) {
					UserContact userContact1 = new UserContact();
					UserContact userContact2 = new UserContact();
					userContact1.setIdUser(user);
					userContact1.setIdFriendUser(friend);
					userContact2.setIdUser(friend);
					userContact2.setIdFriendUser(user);

					userContactDAO.addFriend(userContact1);
					userContactDAO.addFriend(userContact2);
				}
			}
		}

		return true;
	}

	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/social/getNearbyLocations", method = RequestMethod.POST)
	public @ResponseBody List<Place> getNearbyLocations(
			@RequestHeader("X-Auth-Token") String authToken,
			@RequestBody List<String> types) {
		ArrayList<Place> aPlace = new ArrayList<Place>();
		
		for(String type : types){
			JSONArray places = new JSONArray();
			
			try {
				HttpClient httpClient = new DefaultHttpClient();
				StringBuilder url = new StringBuilder();
				url.append(Constants.URL_NOMINATIM_API + "/search?format=json&q=bucharest+");
				url.append(type + "&limit=50");
				
				HttpGet httpGet = new HttpGet(url.toString());
			    HttpResponse httpGetResponse = httpClient.execute(httpGet);
			    HttpEntity httpGetEntity = httpGetResponse.getEntity();
			    
			    if (httpGetEntity != null) {  
			    	String response = EntityUtils.toString(httpGetEntity);
			    	places = new JSONArray(response);
			    	
			    	for(int i = 0; i < places.length(); i++){
			    		JSONObject placeObj = places.getJSONObject(i);
			    		Place place = new Place();
			    		place.setType(placeObj.getString("type"));
			    		place.setName(placeObj.getString("display_name"));
			    		place.setLatitude(Float.parseFloat(placeObj.getString("lat")));
			    		place.setLongitude(Float.parseFloat(placeObj.getString("lon")));
			    		
			    		aPlace.add(place);
			    	}
			    }            
			} catch (Exception exception) {
//			    exception.printStackTrace();
			}
		}

		return aPlace;
	}

	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/social/getRoutePG", method = RequestMethod.POST)
	public @ResponseBody List<Location> getRoutePgRouting(
			@RequestHeader("X-Auth-Token") String authToken,
			@RequestBody ArrayList<Location> locations) {
		ArrayList<Location> routePoints = new ArrayList<Location>();
		System.out.println("get PG route------------");
		
		Calendar start = Calendar.getInstance();
		
		try {
			Calendar rightNow = Calendar.getInstance();
			int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);

			HttpClient httpClient = new DefaultHttpClient();
			StringBuilder url = new StringBuilder();
			url.append(Constants.URL_PGROUTING_API + "/pgroute.php?");
			//url.append("src=" + locations.get(0).getLatitude() + "," + locations.get(0).getLongitude());
			//url.append("&dst=" + locations.get(1).getLatitude() + "," + locations.get(1).getLongitude());
			url.append("&hour=" + currentHour);
			System.out.println("URL------------" + url.toString());
			HttpGet httpGet = new HttpGet(url.toString());
			HttpResponse httpGetResponse = httpClient.execute(httpGet);
			HttpEntity httpGetEntity = httpGetResponse.getEntity();

			if (httpGetEntity != null) {
				String response = EntityUtils.toString(httpGetEntity);

				String[] pointLines =
					response.split(System.getProperty("line.separator"));
				for (String line : pointLines) {
					String[] latLng = line.split(" ");
					Location point = new Location();
					point.setIdUser(0);
					point.setLatitude(new Float(latLng[1]));
					point.setLongitude(new Float(latLng[0]));
					routePoints.add(point);
				}
			}

		} catch (Exception exception) {
			// Request can fail for a number of reasons
			// Mainly if the data is not available for the specified coordinates
			// exception.printStackTrace();
		}

Calendar end = Calendar.getInstance();
		
		if (Constants.DEBUG_MODE) {
			saveRouteToFile("pgRouting", routePoints, start, end);
		}

		return routePoints;
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/social/getRoute", method = RequestMethod.POST)
	public @ResponseBody List<Location> getRoute(
			@RequestHeader("X-Auth-Token") String authToken,
			@RequestBody ArrayList<Location> locations) {
		ArrayList<Location> routePoints = new ArrayList<Location>();
		System.out.println("get route------------");
		
		Calendar start = Calendar.getInstance();
		
		try {
			HttpClient httpClient = new DefaultHttpClient();
			StringBuilder url = new StringBuilder();
			url.append(Constants.URL_OSRM_API_LOCAL + "/viaroute?loc=");
			url.append(locations.get(0).getLatitude()+","+locations.get(0).getLongitude()+"&loc=");
			url.append(locations.get(1).getLatitude()+","+locations.get(1).getLongitude()+"&instructions=true&compression=false");
			System.out.println("URL2------------" + url.toString());
			HttpGet httpGet = new HttpGet(url.toString());
		    HttpResponse httpGetResponse = httpClient.execute(httpGet);
		    HttpEntity httpGetEntity = httpGetResponse.getEntity();
		    
		    if (httpGetEntity != null) {  
		    	String response = EntityUtils.toString(httpGetEntity);

		    	JSONObject route = new JSONObject(response);
		    	JSONArray viaPoints = route.getJSONArray("route_geometry");
		    	
		    	if(viaPoints != null){
		    		routePoints.add(locations.get(0));
		    		for(int i = 0; i < viaPoints.length(); i++){
			    		String point = viaPoints.getString(i);
			    	
			    		Location location = new Location();
			    		location.setIdUser(0);

			    		JSONArray coord = viaPoints.getJSONArray(i);
			    		location.setLatitude((float)coord.getDouble(0));
	  		            location.setLongitude((float)coord.getDouble(1));
			    		location.setSpeed(0);
			    		routePoints.add(location);
			    	}
		    		routePoints.add(locations.get(1));
		    	}
		    	
		    }            
		} catch (Exception exception) {
			// Request can fail for a number of reasons
			// Mainly if the data is not available for the specified coordinates
			// exception.printStackTrace();
		}
		
Calendar end = Calendar.getInstance();
		
		if (Constants.DEBUG_MODE) {
			saveRouteToFile("OSRM", routePoints, start, end);
		}

		
		return routePoints;
	}
	
}

