package ro.pub.acs.traffic.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

import ro.pub.acs.traffic.dao.JourneyDAO;
import ro.pub.acs.traffic.dao.JourneyDataDAO;
import ro.pub.acs.traffic.dao.LocationDAO;
import ro.pub.acs.traffic.dao.UserContactDAO;
import ro.pub.acs.traffic.dao.UserDAO;
import ro.pub.acs.traffic.model.Journey;
import ro.pub.acs.traffic.model.JourneyData;
import ro.pub.acs.traffic.model.Location;
import ro.pub.acs.traffic.model.Place;
import ro.pub.acs.traffic.model.User;
import ro.pub.acs.traffic.model.UserContact;
import ro.pub.acs.traffic.utils.Constants;

@RestController
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

	@RequestMapping(value = "/authenticate/facebook", method = RequestMethod.POST)
	public @ResponseBody User loginWithFacebook(@RequestBody User user) {
		/* Get Facebook profile */
		Facebook facebook = new FacebookTemplate(user.getFacebook_token());
		org.springframework.social.facebook.api.User profile = facebook
				.userOperations().getUserProfile();
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
		user.setAuthExpiresIn((int)seconds);
		user.setUuid(uuid);

		/* Save and return the new user */
		int userId;
		
		/* Update user into database */
		User oldUser = userDAO.get(user.getUsername());
		if (oldUser != null) {
			user.setId(oldUser.getId());
			userId = userDAO.update(user);
		} else {
			userId = userDAO.add(user);
		}
		
		User newUser = userDAO.get(userId);

		/* Get friend from Facebook and insert into database */
		List<String> friendIds = facebook.friendOperations().getFriendIds();

		for (String friendId : friendIds) {
			User userFried = userDAO.get(friendId);
			UserContact userContact1 = new UserContact();
			UserContact userContact2 = new UserContact();
			userContact1.setIdUser(newUser);
			userContact1.setIdFriendUser(userFried);
			userContact2.setId(userFried.getId());
			userContact2.setIdFriendUser(newUser);

			userContactDAO.addFriend(userContact1);
			userContactDAO.addFriend(userContact2);
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
	
	@RequestMapping(value = "/location/getSpeedStatistics", method = RequestMethod.GET)
	public @ResponseBody List<Double> getSpeedStatistics(
			@RequestHeader("X-Auth-Token") String authToken) {
		User user = userDAO.get(authToken, 0);
		List<Journey> journeys = journeyDAO.getByUserId(user);
		List<Double> speeds = new ArrayList();

		if(journeys != null){
			speeds.add(journeyDataDAO.maxSpeed(journeys.get(journeys.size()-1)));
			speeds.add(journeyDataDAO.minSpeed(journeys.get(journeys.size()-1)));
		}

		return speeds;
	}
	
	@RequestMapping(value = "/location/update", method = RequestMethod.PUT)
	public @ResponseBody boolean updateLocation(@RequestBody Location location,
			@RequestHeader("X-Auth-Token") String authToken) {
		User user = userDAO.get(authToken, location.getIdUser());

		if (user != null) {
			/* First update user's location */
			Location locationUser = locationDAO.getLocation(user);
			if (locationUser != null) {
				locationUser.setLatitude(location.getLatitude());
				locationUser.setLongitude(location.getLongitude());
				locationUser.setSpeed(location.getSpeed());
				locationUser.setTimestamp(new Date());
				locationDAO.updateLocation(locationUser);
			} else {
				location.setIdUser(user.getId());
				location.setTimestamp(new Date());
				locationDAO.addLocation(location);
			}
			
			/* Add new location to journey_data */
			List<Journey> journeys = journeyDAO.getByUserId(user);
			Journey journey = new Journey();
			
			if(journeys != null && !journeys.isEmpty()){
				journey = journeys.get(journeys.size() - 1);
			} else {
				journey.setIdUser(user);
				journey.setJourneyName("Location History");
				journeyDAO.add(journey);
			}
			
			JourneyData journeyData = new JourneyData();
			journeyData.setJourneyId(journey);
			journeyData.setLatitude(location.getLatitude());
			journeyData.setLongitude(location.getLongitude());
			journeyData.setSpeed(location.getSpeed());
			journeyData.setTimestamp(new Date());
			journeyDataDAO.add(journeyData);
			
			return true;
		}

		return false;
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/location/saveHistory", method = RequestMethod.POST)
	public @ResponseBody boolean saveHistory(
			@RequestHeader("X-Auth-Token") String authToken,
			@RequestBody ArrayList<Location> locations) {
		User user = userDAO.get(authToken, 0);
		
		if (user != null) {
			for(Location location : locations){
				/* Add new location to journey_data */
				List<Journey> journeys = journeyDAO.getByUserId(user);
				Journey journey = new Journey();
				
				if(journeys != null && !journeys.isEmpty()){
					journey = journeys.get(journeys.size() - 1);
				} else {
					journey.setIdUser(user);
					journey.setJourneyName("Location History");
					journeyDAO.add(journey);
				}
				
				JourneyData journeyData = new JourneyData();
				journeyData.setJourneyId(journey);
				journeyData.setLatitude(location.getLatitude());
				journeyData.setLongitude(location.getLongitude());
				journeyData.setSpeed(location.getSpeed());
				journeyData.setTimestamp(new Date());
				journeyDataDAO.add(journeyData);
			}
			
			return true;
		}

		return false;
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

	@RequestMapping(value = "/social/getFriendsLocations", method = RequestMethod.GET)
	public @ResponseBody List<Location> getFriendsLocations(
			@RequestHeader("X-Auth-Token") String authToken) {
		User user = userDAO.get(authToken, 0);
		List<User> friends = new ArrayList<User>();
		List<Location> locations = new ArrayList<Location>();

		if (user != null) {
			friends = userContactDAO.getFriends(user);
			for (User friend : friends) {
				Location location = locationDAO
						.getLocation(friend);
				if (location != null) {
					location.setTimestamp(null);
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
			    exception.printStackTrace();
			}
		}

		return aPlace;
	}

	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/social/getRoute/{routeType}", method = RequestMethod.POST)
	public @ResponseBody List<Location> getRoute(
			@RequestHeader("X-Auth-Token") String authToken,
			@RequestBody ArrayList<Location> locations,
			@PathVariable String routeType) {
		ArrayList<Location> routePoints = new ArrayList<Location>();
		User user = userDAO.get(authToken, 0);
		int port = Constants.OSRM_CAR_PORT;
		
		if(routeType.equals(Constants.OSRM_BICYCLE_PARAMETER))
			port = Constants.OSRM_BICYCLE_PORT;
		if(routeType.equals(Constants.OSRM_FOOT_PARAMETER))
			port = Constants.OSRM_FOOT_PORT;
		
		try {
			HttpClient httpClient = new DefaultHttpClient();
			StringBuilder url = new StringBuilder();
			url.append(Constants.URL_OSRM_API);
			url.append(":" + port);
			url.append("/viaroute?loc=");
			url.append(locations.get(0).getLatitude()+","+locations.get(0).getLongitude()+"&loc=");
			url.append(locations.get(1).getLatitude()+","+locations.get(1).getLongitude()+"&instructions=true&compression=false");
		
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
		    exception.printStackTrace();
		}
		
		return routePoints;
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	@RequestMapping(value = "/social/getRouteFromMapzen/{routeType}", method = RequestMethod.POST)
	public @ResponseBody List<Location> getRouteFromMapzen(
			@RequestHeader("X-Auth-Token") String authToken,
			@RequestBody ArrayList<Location> locations,
			@PathVariable String routeType) {
		ArrayList<Location> routePoints = new ArrayList<Location>();
		User user = userDAO.get(authToken, 0);
		
		try {
			HttpClient httpClient = new DefaultHttpClient();
			StringBuilder url = new StringBuilder();
			url.append(Constants.URL_OSRM_MAPZEN_API);
			url.append("/" + routeType.toLowerCase());
			url.append("/viaroute?loc=");
			url.append(locations.get(0).getLatitude()+","+locations.get(0).getLongitude()+"&loc=");
			url.append(locations.get(1).getLatitude()+","+locations.get(1).getLongitude()+"&instructions=true&compression=false");
		
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

			    		String coord = viaPoints.getString(i);
			    		location.setLatitude(Float.parseFloat(coord.substring(0, coord.indexOf(",") - 1)));
			    		location.setLongitude(Float.parseFloat(coord.substring(coord.indexOf(",") + 1, coord.length())));
			    		location.setSpeed(0);
			    		
			    		routePoints.add(location);
		    		}
		    		
		    		routePoints.add(locations.get(1));
		    	}
		    	
		    }            
		} catch (Exception exception) {
		    exception.printStackTrace();
		}
		
		return routePoints;
	}
	
}