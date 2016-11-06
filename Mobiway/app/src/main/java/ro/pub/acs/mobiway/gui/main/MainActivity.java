package ro.pub.acs.mobiway.gui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.*;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.core.RoutingHelper;
import ro.pub.acs.mobiway.general.Constants;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.general.Util;
import ro.pub.acs.mobiway.gui.settings.SettingsActivity;
import ro.pub.acs.mobiway.gui.statistics.StatisticsActivity;
import ro.pub.acs.mobiway.rest.RestClient;
import ro.pub.acs.mobiway.rest.model.Place;
import ro.pub.acs.mobiway.rest.model.Policy;
import ro.pub.acs.mobiway.rest.model.User;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static List<User> friendsNames = null;
    private static List<ro.pub.acs.mobiway.rest.model.Location> friendsLocations = null;
    private static boolean firstLocation = true;

    private SharedPreferencesManagement spm;
    private GoogleMap googleMap = null; /* Might be null if Google Play services APK is not available. */
    private GoogleApiClient googleApiClient = null;
    private Location oldLocation = null;
    private Location lastLocation = null;
    private LocationRequest locationRequest = null;
    private Marker marker;
    private String destinationTitle;
    private LatLng latLngMarker;


    private AutoCompleteTextView autoCompleteTextView;
    private ArrayList<String> places;
    private PlacesAdapter placesAdapter;

    private RoutingHelper routingHelper;

    private ArrayList<Polyline> aPolyline = new ArrayList<>();

    private void showRoute(final String routingEngine) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RestClient restClient = new RestClient();

                    ArrayList<ro.pub.acs.mobiway.rest.model.Location> locations = new ArrayList<>();
                    ro.pub.acs.mobiway.rest.model.Location location1 = new ro.pub.acs.mobiway.rest.model.Location();
                    ro.pub.acs.mobiway.rest.model.Location location2 = new ro.pub.acs.mobiway.rest.model.Location();

                    if (routingHelper.getUseGpsForSrc()) {
                        location1.setLatitude((float) lastLocation.getLatitude());
                        location1.setLongitude((float) lastLocation.getLongitude());
                    } else {
                        LatLng srcLoc = routingHelper.getSrcLocation();
                        location1.setLatitude((float) srcLoc.latitude);
                        location1.setLongitude((float) srcLoc.longitude);
                    }

                    LatLng dstLoc = routingHelper.getDstLocation();
                    location2.setLatitude((float) dstLoc.latitude);
                    location2.setLongitude((float) dstLoc.longitude);

                    locations.add(location1);
                    locations.add(location2);

                    /* getRoute -> OSRM getRoutePG -> PGRouting */

                    if (routingEngine.equalsIgnoreCase("osrm")) {

                        Log.d("ro.pub.acs.mobiway", location1.toString() + "---" + location2.toString());

                        List<ro.pub.acs.mobiway.rest.model.Location> result = restClient.getApiService().getRoute(locations);
                        showRouteOnMap(result);
                    } else if (routingEngine.equalsIgnoreCase("pgrouting")) {


                        List<ro.pub.acs.mobiway.rest.model.Location> result = restClient.getApiService().getRoutePG(locations);

                        Log.d("---------------00000", result.toString());
                        showRouteOnMap(result);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Logging config
        Log.i(TAG, "onCreate() callback method was invoked");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spm = new SharedPreferencesManagement(getApplicationContext());
        routingHelper = new RoutingHelper(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (spm.isFirstTimeUse()) {

            loadDefaultPolicyValues();
            getContacts();
            spm.setFirstTimeUse(true);
        } else {

            getFriends();
        }

        getNearbyLocations();

        places = new ArrayList<>();
        placesAdapter = new PlacesAdapter(this, places);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.placesAutoCompleteTextView);
        autoCompleteTextView.setAdapter(placesAdapter);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 5) {
                    getPlacesAutocomplete(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 5) {
                    getPlacesAutocomplete(s.toString());
                }
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getLocationsFromAddress(autoCompleteTextView.getText().toString());
            }
        });
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart() callback method was invoked");

        super.onStart();

        checkServerStatus();
        googleApiClient.connect();
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            googleMap.setOnMarkerClickListener(this);
            googleMap.setOnMapClickListener(this);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);
            googleMap.setMapType(Util.getMapType(spm.getMapType()));
        }

        if (googleApiClient != null && googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {

        Log.i(TAG, "onStop() callback method was invoked");

        stopLocationUpdates();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        Log.i(TAG, "onDestroy() callback method was invoked");

        googleApiClient = null;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        Log.i(TAG, "onSaveInstanceState() callback method was invoked");

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {

        Log.i(TAG, "onResume() callback method was invoked");

        super.onResume();
    }

    private Menu currentMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        currentMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.gps_as_src: {

                // Toggle selected state
                boolean useGpsAsSrc = !item.isChecked();

                MenuItem m1 = currentMenu.findItem(R.id.src_loc);
                MenuItem m2 = currentMenu.findItem(R.id.dst_loc);
                if (!useGpsAsSrc) {
                    m1.setVisible(true);
                    m2.setVisible(true);
                } else {
                    m1.setVisible(false);
                    m2.setVisible(false);
                }
                item.setChecked(useGpsAsSrc);

                routingHelper.setUseGpsForSrc(useGpsAsSrc);
                routingHelper.clear();

                return true;
            }

            case R.id.src_loc: {
                routingHelper.selectSrc();
                return true;
            }

            case R.id.dst_loc: {
                routingHelper.selectDst();
                return true;
            }

            case R.id.show_route_pgrouting: {
                showRoute("pgrouting");
                return true;
            }

            case R.id.show_route_osrm: {
                showRoute("osrm");
                return true;
            }

            case R.id.clear_routes: {
                for (Polyline polyline : aPolyline) {
                    polyline.remove();
                }
                aPolyline.clear();
                return true;
            }

            case R.id.action_settings: {
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
                return true;
            }

            case R.id.action_statistics: {
                Intent i = new Intent(getApplicationContext(), StatisticsActivity.class);
                startActivity(i);
                return true;
            }

            case R.id.action_logout: {
                new AlertDialog.Builder(this)
                        .setMessage(getResources().getString(R.string.logout_confirmation))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                LoginManager.getInstance().logOut();

                                spm.logoutUser();
                                finish();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.no), null)
                        .show();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToLocation(final double latitude, final double longitude, final float speed) {
        if (firstLocation) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude, longitude)
            ).zoom(Constants.CAMERA_ZOOM)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            firstLocation = false;
        }

        final ro.pub.acs.mobiway.rest.model.Location location = new ro.pub.acs.mobiway.rest.model.Location();
        location.setLatitude((float) latitude);
        location.setLongitude((float) longitude);
        location.setSpeed((int) speed);
        location.setIdUser(spm.getAuthUserId());

        if (spm.getNotificationsEnabled() &&
                lastLocation != null && oldLocation != null &&
                (lastLocation.getLatitude() != oldLocation.getLatitude() ||
                        lastLocation.getLongitude() != oldLocation.getLatitude())) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        RestClient restClient = new RestClient();

                        if (!spm.getShareLocationEnabled()) {
                            location.setLatitude(null);
                            location.setLongitude(null);
                        }

                        if (!spm.getShareSpeedEnabled()) {
                            location.setSpeed(null);
                        }

                        restClient.getApiService().updateLocation(location);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

            try {
                final JSONArray jsonArray = new JSONArray(spm.getLocationHistory());
                if(jsonArray.length() > 0){
                    Thread thread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ArrayList<ro.pub.acs.mobiway.rest.model.Location> aLocation = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++){
                                    JSONArray savedLocation = jsonArray.getJSONArray(i);
                                    location.setLatitude(Float.parseFloat(savedLocation.get(0) + ""));
                                    location.setLongitude(Float.parseFloat(savedLocation.get(1) + ""));
                                    location.setSpeed((int) savedLocation.get(2));
                                    location.setIdUser(spm.getAuthUserId());

                                    aLocation.add(location);
                                }

                                RestClient restClient = new RestClient();
                                restClient.getApiService().updateLocations(aLocation);

                                spm.setLocationHistory(Constants.EMPTY_ARRAY);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread2.start();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONArray jsonArray = new JSONArray(spm.getLocationHistory());

                JSONArray aLocation = new JSONArray();
                aLocation.put(latitude);
                aLocation.put(longitude);
                aLocation.put(speed);
                aLocation.put(new Date().toString());
                jsonArray.put(aLocation);

                spm.setLocationHistory(jsonArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void navigateToLocation(Location location) {
        navigateToLocation(location.getLatitude(), location.getLongitude(), location.getSpeed());
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        Log.i(TAG, "onConnected() callback method has been invoked");

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        oldLocation = lastLocation;
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "onConnectionSuspended() callback method has been invoked");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed() callback method has been invoked");
    }

    protected void startLocationUpdates() {

        // Notify Server that we started a new Journey
        if (spm.getNotificationsEnabled()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        RestClient restClient = new RestClient();
                        restClient.getApiService().newJourney(spm.getAuthUserId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } else {
            //locationArrayList.add(location);
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                this
        );

        googleMap.setMyLocationEnabled(true);

        if (lastLocation != null) {
            navigateToLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), lastLocation.getSpeed());
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient,
                this
        );
        firstLocation = true;
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.i(TAG, "onLocationChanged() callback method has been invoked");

        oldLocation = lastLocation;
        lastLocation = location;
        navigateToLocation(lastLocation);
    }

    private void getContacts() {
        final Context context = getApplicationContext();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<String> phones = new ArrayList<>();
                    RestClient restClient = new RestClient();
                    List<User> users = restClient.getApiService().getUsersWithPhone();
                    HashMap<String, User> usersWithPhones = new HashMap<>();

                    for (User user : users) {
                        phones.add(user.getPhone());
                        usersWithPhones.put(user.getPhone(), user);
                    }

                    if (!phones.isEmpty()) {
                        ArrayList<String> contacts = Util.readContacts(context, phones);
                        users = new ArrayList<>();

                        if (!contacts.isEmpty()) {
                            for (String number : contacts) {
                                users.add(usersWithPhones.get(number));
                            }

                            restClient.getApiService().addFriends(users);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                getFriends();
            }
        }).start();
    }

    private void getFriends() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RestClient restClient = new RestClient();
                    friendsNames = restClient.getApiService().getFriendsNames();
                    friendsLocations = restClient.getApiService().getFriendsLocations();

                    Log.i(TAG, "Friends: " + friendsNames);
                    Log.i(TAG, "Friends locations: " + friendsLocations);

                    showFriendsOnMap();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void showFriendsOnMap() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                googleMap.clear();
                for (User friend : friendsNames) {
                    for (ro.pub.acs.mobiway.rest.model.Location location : friendsLocations) {
                        if (location.getIdUser() == friend.getId()) {
                            googleMap.addMarker(new MarkerOptions()
                                    .title(friend.getFirstname() + " " + friend.getLastname())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_face_black_24dp))
                                    .alpha(0.8f)
                                    .position(new LatLng(location.getLatitude(), location.getLongitude())));
                            break;
                        }
                    }
                }
            }
        });
    }

    private void getNearbyLocations() {

        Set<String> locPref = spm.getUserLocPreferences();
        final ArrayList<String> prefList = new ArrayList<>();
        prefList.addAll(locPref);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RestClient restClient = new RestClient();
                    List<Place> result = restClient.getApiService().getNearbyLocations(prefList);

                    showPlacesOnMap(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void showPlacesOnMap(final List<Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Place place : places) {
                    googleMap.addMarker(new MarkerOptions()
                            .title(place.getName())
                            .alpha(0.8f)
                            .position(new LatLng(place.getLatitude(), place.getLongitude())));
                }
            }
        });
    }

    private void showRouteOnMap(final List<ro.pub.acs.mobiway.rest.model.Location> points) {

        Log.d("---------------", points.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PolylineOptions polylineOptions = new PolylineOptions();
                for (ro.pub.acs.mobiway.rest.model.Location point : points) {
                    polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
                }
                aPolyline.add(googleMap.addPolyline(polylineOptions));
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        this.marker = marker;
        latLngMarker = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        destinationTitle = marker.getTitle();
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {

        Marker selMarker = googleMap.addMarker(new MarkerOptions()
                .title("Marker")
                .alpha(0.8f)
                .position(latLng));

        routingHelper.selectPoint(latLng, selMarker);

        if (routingHelper.getUseGpsForSrc()) {
            routingHelper.selectDst();
        }
    }

    private void getPlacesAutocomplete(final String query) {
       /* latLngBounds for Romania - southwest, northeast */
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(44.153834, 22.895508), new LatLng(47.715675, 27.254333));
        final PendingResult<AutocompletePredictionBuffer> result =
                Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query,
                        latLngBounds, null);
        places.clear();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AutocompletePredictionBuffer autocompletePredictionBuffer = result.await(30, TimeUnit.SECONDS);

                for (AutocompletePrediction autocompletePrediction : autocompletePredictionBuffer) {
                    places.add(autocompletePrediction.getDescription());
                }
                autocompletePredictionBuffer.release();

                Log.e(TAG, "places " + places);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        placesAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        thread.start();
    }

    public void getLocationsFromAddress(final String address) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LatLng latLng = null;
                String uri = null;

                try {
                    uri = Constants.MAPS_API_GEOCODE + URLEncoder.encode(address, Constants.ENCODING) + "&sensor=false";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                HttpGet httpGet = new HttpGet(uri);
                HttpClient client = new DefaultHttpClient();
                HttpResponse response;

                try {
                    response = client.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    JSONObject jsonObject = new JSONObject(EntityUtils.toString(entity));

                    double lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lng");

                    double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lat");

                    latLng = new LatLng(lat, lng);
                    if (latLng != null) {
                        ArrayList<Place> aPlaces = new ArrayList<Place>();
                        Place place = new Place();
                        place.setName(autoCompleteTextView.getText().toString());
                        place.setLatitude((float) latLng.latitude);
                        place.setLongitude((float) latLng.longitude);
                        aPlaces.add(place);

                        showPlacesOnMap(aPlaces);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void loadDefaultPolicyValues() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferencesManagement spm = SharedPreferencesManagement.getInstance(null);

                    RestClient restClient = new RestClient();
                    List<Policy> policyList =
                            restClient.getApiService().getPolicyListApp(Constants.APP_NAME);

                    List<String> acceptedPolicyList = new ArrayList<>();
                    for (Policy policy : policyList) {
                        acceptedPolicyList.add(policy.getPolicyName());
                    }

                    restClient.getApiService().acceptUserPolicyListForApp(
                            spm.getAuthUserId(), Constants.APP_NAME, acceptedPolicyList);

                    Set<String> acceptedPolicySet = new HashSet<>();
                    acceptedPolicySet.addAll(acceptedPolicyList);
                    spm.setUserPolicy(acceptedPolicySet);
                } catch (Exception ex) {
                    Log.d(TAG, "Error loading default policy values");
                    //ex.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private boolean checkNetworkConnectivity() {
        if (!Util.isNetworkAvailable(this)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                    dlgAlert.setMessage("\nNo network connectivity" +
                            "\n\nPlease enable WiFi or" +
                            "\nMobile Data" +
                            "\n\n\nGoing to Exit now !");
                    dlgAlert.setTitle("Network Error");
                    dlgAlert.setPositiveButton("Exit Application", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dlgAlert.setCancelable(false);
                    dlgAlert.create().show();
                }
            });
            return false;
        }
        return true;
    }

    private boolean checkServerConnectivity() {
        boolean canConnect = false;
        try {
            RestClient restClient = new RestClient();
            canConnect = restClient.getApiService().checkServerConnectivity();
        } catch (Exception ex) {
            canConnect = false;
        }

        if (!canConnect) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                    dlgAlert.setMessage("\nNo server connectivity" +
                            "\n\n\nGoing to Exit now !");
                    dlgAlert.setTitle("Server Connectivity Error");
                    dlgAlert.setPositiveButton("Exit Application", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dlgAlert.setCancelable(false);
                    dlgAlert.create().show();
                }
            });
        }

        return canConnect;
    }

    private void checkServerStatus() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // See if we have WiFi or 4G connectivity
                if (!checkNetworkConnectivity())
                    return;

                // See if we can connect to the Server
                if (!checkServerConnectivity())
                    return;
            }
        });
        thread.start();
    }


}