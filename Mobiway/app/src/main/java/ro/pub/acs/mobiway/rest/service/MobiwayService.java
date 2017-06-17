package ro.pub.acs.mobiway.rest.service;

import java.util.*;

import retrofit.http.*;

import ro.pub.acs.mobiway.general.Constants;
import ro.pub.acs.mobiway.rest.TypedJsonString;
import ro.pub.acs.mobiway.rest.model.*;

public interface MobiwayService {

    @GET(Constants.URL_CHECK_SERVER_CONN)
    boolean checkServerConnectivity(
    );

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })

    @GET(Constants.URL_GET_POLICY_APP + "/{appId}")
    List<Policy> getPolicyListApp(
            @Path("appId") String appId
    );

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET(Constants.URL_GET_POLICY_USER_APP + "/{userId}/{appId}")
    List<Policy> getUserPolicyListApp(
            @Path("userId") Integer userId,
            @Path("appId") String appId
    );

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST(Constants.URL_ACCEPT_POLICY_APP + "/{userId}/{appId}")
    boolean acceptUserPolicyListForApp(
            @Path("userId") Integer userId,
            @Path("appId") String appId,
            @Body List<String> policyList
    );

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })

    @POST(Constants.URL_CREATE_ACCOUNT)
    User createAccount(@Body User body);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST(Constants.URL_LOG_IN)
    User loginUserPass(@Body User body);

    @POST(Constants.URL_RESET_PASSWORD)
    User resetUserPass(@Body User body);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST(Constants.URL_AUTH_FACEBOOK)
    User loginFacebook(@Body User body);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST(Constants.URL_UPDATE_USER)
    String updateUSer(@Body TypedJsonString body);


    @GET(Constants.URL_GET_USER + "/{userId}")
    User getUser(@Path("userId") int userId);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @PUT(Constants.URL_UPDATE_LOCATION)
    boolean updateLocation(@Body Location location);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST(Constants.URL_UPDATE_LOCATIONS)
    boolean updateLocations(@Body ArrayList<Location> location);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET(Constants.URL_GET_SPEED_STATISTICS)
    List<Double> getSpeedStatistics();

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET(Constants.URL_GET_FRIENDS_NAMES)
    List<User> getFriendsNames();

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET(Constants.URL_GET_FRIENDS_LOCATIONS)
    List<Location> getFriendsLocations();

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET(Constants.URL_GET_USERS_WITH_PHONE)
    List<User> getUsersWithPhone();

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @PUT(Constants.URL_ADD_FRIENDS)
    boolean addFriends(@Body List<User> contacts);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST(Constants.URL_GET_NEARBY_LOCATIONS)
    List<Place> getNearbyLocations(@Body ArrayList<String> types);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST(Constants.URL_NEW_JOURNEY)
    boolean newJourney(@Body Integer userId);

//    @Headers({
//            "Content-Type: application/json",
//            "Accept: application/json"
//    })
//    @POST(Constants.URL_GET_ROUTE + "/{routeType}")
//    List<Location> getRoute(@Body ArrayList<Location> locations, @Path("routeType") String routeType);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST(Constants.URL_GET_ROUTE)
    List<Location> getRoute(@Body ArrayList<Location> locations);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST(Constants.URL_GET_ROUTE_PG)
    List<Location> getRoutePG(@Body ArrayList<Location> locations);


    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @PUT(Constants.URL_POST_EVENT + "/{eventName}/{distance}/{timeSinceEvent}/{spaceAccuracy}/{timeAccuracy}")
    boolean postEvent(
          @Path ("eventName") String eventName,
          @Path ("distance") Float distance,
          @Path ("timeSinceEvent") Float timeSinceEvent,
          @Path ("spaceAccuracy") Float spaceAccuracy,
          @Path ("timeAccuracy") Float timeAccuracy,
          @Body Location location
    );

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @PUT(Constants.URL_GET_EVENT)
    List<Place> getEvent(@Body Location location);

}
