package ro.pub.acs.mobiway.gui.main;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.acra.ACRA;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.core.RoutingHelper;
import ro.pub.acs.mobiway.general.Constants;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.general.Util;
import ro.pub.acs.mobiway.gui.events.EventsActivity;
import ro.pub.acs.mobiway.gui.settings.SettingsActivity;
import ro.pub.acs.mobiway.gui.statistics.StatisticsActivity;
import ro.pub.acs.mobiway.lib.SQLiteDatabaseHelper;
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
    private Marker marker = null;
    private String destinationTitle;
    private LatLng latLngMarker;

    private SQLiteDatabaseHelper sqlDbHelper = null;

    private SimpleDateFormat sdf;

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

                        //ACRA log
                        ACRA.getErrorReporter().putCustomData("MainActivity.showRoute():routeByOSRM", "osrm");

                        Log.d("ro.pub.acs.mobiway", location1.toString() + "---" + location2.toString());

                        List<ro.pub.acs.mobiway.rest.model.Location> result = restClient.getApiService().getRoute(locations);
                        showRouteOnMap(result);

                    } else if (routingEngine.equalsIgnoreCase("pgrouting")) {

                        //ACRA log
                        ACRA.getErrorReporter().putCustomData("MainActivity.showRoute():routeByPgRouting", "pgrouting");

                        List<ro.pub.acs.mobiway.rest.model.Location> result = restClient.getApiService().getRoutePG(locations);

                        showRouteOnMap(result);
                    }

                } catch (Exception e) {

                    //ACRA log
                    ACRA.getErrorReporter().putCustomData("MainActivity.showRoute():error", e.toString());
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onCreate()", "callback method was invoked");

        // Logging config
        Log.i(TAG, "onCreate() callback method was invoked");

        super.onCreate(savedInstanceState);
        sqlDbHelper = new SQLiteDatabaseHelper(getApplicationContext());

        setContentView(R.layout.activity_main);
        spm = new SharedPreferencesManagement(getApplicationContext());
        routingHelper = new RoutingHelper(this);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        getTrafficEvents();

        places = new ArrayList<>();
        placesAdapter = new PlacesAdapter(this, places);
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.placesAutoCompleteTextView);
        autoCompleteTextView.setAdapter(placesAdapter);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                places.clear();

                if (s.toString().length() > 3) {
                    getPlacesAutocomplete(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                places.clear();

                if (s.toString().length() > 3) {
                    getPlacesAutocomplete(s.toString());
                }
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                marker = null;
                hideKeyboard();
                getLocationsFromAddress(autoCompleteTextView.getText().toString());
            }
        });

        googleApiClient.connect();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStart() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onStart()", "callback method was invoked");

        Log.i(TAG, "onStart() callback method was invoked");

        super.onStart();

        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.setOnMarkerClickListener(this);
            googleMap.setOnMapClickListener(this);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setCompassEnabled(false);
            googleMap.setMapType(Util.getMapType(spm.getMapType()));
        }
    }

    @Override
    protected void onStop() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onStop()", "callback method was invoked");

        Log.i(TAG, "onStop() callback method was invoked");

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onDestroy()", "callback method was invoked");

        Log.i(TAG, "onDestroy() callback method was invoked");

        if (googleApiClient != null && googleApiClient.isConnected()) {
            stopLocationUpdates();
            googleApiClient.disconnect();
        }

        // close the database
        sqlDbHelper.close();

        googleApiClient = null;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onSaveInstanceState()", "callback method was invoked");

        Log.i(TAG, "onSaveInstanceState() callback method was invoked");

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onResume()", "callback method was invoked");

        Log.i(TAG, "onResume() callback method was invoked");

        super.onResume();
    }

    private Menu currentMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onCreateOptionsMenu()", "callback method was invoked");

        Log.i(TAG, "onCreateOptionsMenu() callback method was invoked");

        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        currentMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onOptionsItemSelected()", "callback method was invoked");

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

            case R.id.action_about: {
                new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage(R.string.licenses)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
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

            case R.id.post_event: {
                //ACRA log
                ACRA.getErrorReporter().putCustomData("MainActivity.onOptionsItemSelected()", "method has been invoked");

                Intent i = new Intent(getApplicationContext(), EventsActivity.class);
                startActivity(i);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateToLocation(final double latitude, final double longitude, final float speed) {
        Log.v(TAG, "Navigate to location " + latitude + " " + longitude);

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.navigateToLocation()", "method was invoked");

        if (firstLocation) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude, longitude)
            ).zoom(Constants.CAMERA_ZOOM)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            firstLocation = false;
        }

        final ro.pub.acs.mobiway.rest.model.Location location = new ro.pub.acs.mobiway.rest.model.Location();
        location.setLatitude(spm.getShareLocationEnabled() ? (float) latitude : null);
        location.setLongitude(spm.getShareLocationEnabled() ? (float) longitude : null);
        location.setSpeed(spm.getShareSpeedEnabled() ? (int) speed : null);
        location.setIdUser(spm.getAuthUserId());
        //location.setTimestamp(new Date());

        if (spm.getNotificationsEnabled() &&
                lastLocation != null && oldLocation != null &&
                (lastLocation.getLatitude() != oldLocation.getLatitude() ||
                        lastLocation.getLongitude() != oldLocation.getLatitude())) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.v(TAG, "Uploading location " + location.getLatitude() + " " + location.getLongitude() + " " + location.getSpeed());
                        saveLocationInLocalStorage(location);
                        RestClient restClient = new RestClient();
                        restClient.getApiService().updateLocation(location);
                    } catch (Exception e) {
                        //ACRA log
                        ACRA.getErrorReporter().putCustomData("MainActivity.navigateToLocation():errorSetLocation1", e.toString());
                        e.printStackTrace();
                        saveLocationOffline(location);
                    }
                }
            });
            thread.start();

            readLocationsFromLocalStorage();

            if (!Constants.EMPTY_ARRAY.equals(spm.getLocationHistory())) {
                Thread thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Integer> uploaded = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(spm.getLocationHistory());

                            try {
                                ArrayList<ro.pub.acs.mobiway.rest.model.Location> aLocation = new ArrayList<>();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONArray savedLocation = jsonArray.getJSONArray(i);
                                    ro.pub.acs.mobiway.rest.model.Location savedRestLocation = new ro.pub.acs.mobiway.rest.model.Location();

                                    Float latitude = Float.parseFloat(savedLocation.get(0) + "");
                                    Float longitude = Float.parseFloat(savedLocation.get(1) + "");
                                    Integer speed = (int) savedLocation.get(2);

                                    savedRestLocation.setLatitude(latitude == Float.MIN_VALUE ? null : latitude);
                                    savedRestLocation.setLongitude(longitude == Float.MIN_VALUE ? null : longitude);
                                    savedRestLocation.setSpeed(speed == Integer.MIN_VALUE ? null : speed);
                                    savedRestLocation.setTimestamp(sdf.parse(savedLocation.getString(3)));
                                    savedRestLocation.setIdUser(spm.getAuthUserId());

                                    Log.v(TAG, "Uploading saved location " + savedRestLocation.getLatitude() + " " + savedRestLocation.getLongitude() + " " +
                                            savedRestLocation.getSpeed() + " " + savedRestLocation.getIdUser() + savedRestLocation.getTimestamp());

                                    aLocation.add(savedRestLocation);
                                }

                                RestClient restClient = new RestClient();
                                restClient.getApiService().updateLocations(aLocation);
                                spm.setLocationHistory(Constants.EMPTY_ARRAY);
                            } catch (Exception e) {

                                //ACRA log
                                ACRA.getErrorReporter().putCustomData("MainActivity.navigateToLocation():errorSetLocation2", e.toString());

                                e.printStackTrace();
                            }
                        } catch (JSONException e) {

                            //ACRA log
                            ACRA.getErrorReporter().putCustomData("MainActivity.navigateToLocation():errorJsonEx1", e.toString());

                            e.printStackTrace();
                        }
                    }
                });
                thread2.start();
            }
        } else {
            saveLocationOffline(location);
        }
    }

    private void saveLocationOffline(ro.pub.acs.mobiway.rest.model.Location location) {
        Log.v(TAG, "Saving location " + location.getLatitude() + " " + location.getLongitude() + " " + location.getSpeed());

        try {
            JSONArray jsonArray = new JSONArray(spm.getLocationHistory());

            JSONArray aLocation = new JSONArray();
            aLocation.put(location.getLatitude() == null ? Float.MIN_VALUE : location.getLatitude());
            aLocation.put(location.getLongitude() == null ? Float.MIN_VALUE : location.getLongitude());
            aLocation.put(location.getSpeed() == null ? Integer.MIN_VALUE : location.getSpeed());
            aLocation.put(sdf.format(new Date()));
            jsonArray.put(aLocation);

            spm.setLocationHistory(jsonArray.toString());
        } catch (JSONException e) {

            //ACRA log
            ACRA.getErrorReporter().putCustomData("MainActivity.saveLocationOffline():errorJsonEx2", e.toString());

            e.printStackTrace();
        }
    }

    private void saveLocationInLocalStorage(ro.pub.acs.mobiway.rest.model.Location location) {

        try{

            // Gets the data repository in write mode
            SQLiteDatabase db = sqlDbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(SQLiteDatabaseHelper.USER_ID, location.getIdUser());
            values.put(SQLiteDatabaseHelper.LONGITUDE, location.getLongitude());
            values.put(SQLiteDatabaseHelper.LATITUDE, location.getLatitude());
            values.put(SQLiteDatabaseHelper.SPEED, location.getSpeed());
            values.put(SQLiteDatabaseHelper.DATE, sdf.format(new Date()));

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(SQLiteDatabaseHelper.LOCATION_TABLE_NAME, null, values);

        } catch (Exception e) {
            //ACRA log
            ACRA.getErrorReporter().putCustomData("MainActivity.saveLocationInLocalStorage():error", e.toString());

            e.printStackTrace();
        }
    }

    private ArrayList readLocationsFromLocalStorage(){
        ArrayList<ro.pub.acs.mobiway.rest.model.Location> aLocation = new ArrayList<>();
        SQLiteDatabase db = sqlDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                SQLiteDatabaseHelper.USER_ID,
                SQLiteDatabaseHelper.LONGITUDE,
                SQLiteDatabaseHelper.LATITUDE,
                SQLiteDatabaseHelper.SPEED,
                SQLiteDatabaseHelper.DATE
        };

// Filter results WHERE "title" = 'My Title'
        String selection = SQLiteDatabaseHelper.USER_ID + " = ?";
        String[] selectionArgs = {spm.getAuthUserId() + ""};

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                SQLiteDatabaseHelper.DATE + " DESC";

        Cursor cursor = db.query(
                SQLiteDatabaseHelper.LOCATION_TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // The Cursor is now set to the right position
            ro.pub.acs.mobiway.rest.model.Location savedDbLocation = new ro.pub.acs.mobiway.rest.model.Location();
            savedDbLocation.setIdUser(cursor.getInt(cursor.getColumnIndex(SQLiteDatabaseHelper.USER_ID)));
            savedDbLocation.setLatitude(cursor.getFloat(cursor.getColumnIndex(SQLiteDatabaseHelper.LATITUDE)));
            savedDbLocation.setLongitude(cursor.getFloat(cursor.getColumnIndex(SQLiteDatabaseHelper.LONGITUDE)));
            savedDbLocation.setSpeed(cursor.getInt(cursor.getColumnIndex(SQLiteDatabaseHelper.SPEED)));
//            savedDbLocation.setTimestamp(cursor.get(cursor.getColumnIndex(SQLiteDatabaseHelper.DATE)));


            aLocation.add(savedDbLocation);
        }

        Log.d(TAG, aLocation + "");
        return aLocation;
    }

    private void navigateToLocation(Location location) {
        navigateToLocation(location.getLatitude(), location.getLongitude(), location.getSpeed());
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onConnected()", "method has been invoked");

        Log.i(TAG, "onConnected() callback method has been invoked");

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        oldLocation = lastLocation;
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onConnectionSuspended()", "method has been invoked");

        Log.i(TAG, "onConnectionSuspended() callback method has been invoked");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onConnectionFailed()", "method has been invoked");

        Log.i(TAG, "onConnectionFailed() callback method has been invoked");
    }

    protected void startLocationUpdates() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.startLocationUpdates()", "method has been invoked");

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


        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.onLocationChanged()", "method has been invoked");

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

                    //ACRA log
                    ACRA.getErrorReporter().putCustomData("MainActivity.getContacts():error", e.toString());

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

                    //ACRA log
                    ACRA.getErrorReporter().putCustomData("MainActivity.getFriends():error", e.toString());

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
                                    .alpha(0.9f)
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

                    //ACRA log
                    ACRA.getErrorReporter().putCustomData("MainActivity.getNearbyLocations:error", e.toString());

                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void getTrafficEvents() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("MainActivity.getTrafficEvents:error", "called function");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RestClient restClient = new RestClient();
                    List<Place> result = restClient.getApiService().getEvent(spm.getAuthUserId(), spm.getLatitude(), spm.getLongitude());

                    Place p = new Place();
                    p.setType("road_blocked");
                    p.setName("Road blocked");
                    p.setLatitude((float)(spm.getLatitude() - 0.00200));
                    p.setLongitude((float)(spm.getLongitude() - 0.00010));
                    result.add(p);

                    Place p2 = new Place();
                    p2.setType("car_accident");
                    p2.setName("Car accident");
                    p2.setLatitude((float)(spm.getLatitude() + 0.00000));
                    p2.setLongitude((float)(spm.getLongitude() + 0.00310));
                    result.add(p2);

                    showPlacesOnMap(result);
                } catch (Exception e) {

                    //ACRA log
                    ACRA.getErrorReporter().putCustomData("MainActivity.getTrafficEvents:error", e.toString());
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
                            .alpha(1f)
                            .position(new LatLng(place.getLatitude(), place.getLongitude()))
                            .flat(true)
                            .icon(getIconFromPlace(place)));
                }
            }
        });
    }

    private void showPlaceOnMap(final Place place) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng pos = new LatLng(place.getLatitude(), place.getLongitude());

                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .title("Marker")
                        .alpha(0.9f)
                        .position(pos));

                Log.v(TAG, "Selecting place " + place.getName());
                routingHelper.selectPoint(pos, marker);
                if (routingHelper.getUseGpsForSrc()) {
                    routingHelper.selectDst();
                }
            }
        });
    }

    private void showRouteOnMap(final List<ro.pub.acs.mobiway.rest.model.Location> points) {

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
        hideKeyboard();

        this.marker = marker;
        latLngMarker = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        destinationTitle = marker.getTitle();
        marker.showInfoWindow();

        Log.v(TAG, "Selecting marker " + marker.getTitle());

        routingHelper.selectPoint(latLngMarker, marker);
        if (routingHelper.getUseGpsForSrc()) {
            routingHelper.selectDst();
        }

        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        hideKeyboard();
        marker = null;

        Marker selMarker = googleMap.addMarker(new MarkerOptions()
                .title("Marker")
                .alpha(0.9f)
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

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final AutocompletePredictionBuffer autocompletePredictionBuffer = result.await(30, TimeUnit.SECONDS);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        places.clear();
                        for (AutocompletePrediction autocompletePrediction : autocompletePredictionBuffer) {
                            places.add(autocompletePrediction.getDescription());
                        }
                        autocompletePredictionBuffer.release();

                        Log.e(TAG, "places " + places);
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
                        Place place = new Place();
                        place.setName(autoCompleteTextView.getText().toString());
                        place.setLatitude((float) latLng.latitude);
                        place.setLongitude((float) latLng.longitude);

                        showPlaceOnMap(place);
                    }
                } catch (JSONException e) {

                    //ACRA log
                    ACRA.getErrorReporter().putCustomData("MainActivity.getLocationsFromAddress:JSONException", e.toString());

                    e.printStackTrace();
                } catch (ClientProtocolException e) {

                    //ACRA log
                    ACRA.getErrorReporter().putCustomData("MainActivity.getLocationsFromAddress:ClientProtocolException", e.toString());

                    e.printStackTrace();
                } catch (IOException e) {

                    //ACRA log
                    ACRA.getErrorReporter().putCustomData("MainActivity.getLocationsFromAddress:IOException", e.toString());

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
                } catch (Exception e) {

                    //ACRA log
                    ACRA.getErrorReporter().putCustomData("MainActivity.loadDefaultPolicyValues:IOException", e.toString());

                    Log.d(TAG, "Error loading default policy values");
                }
            }
        });
        thread.start();
    }

    private boolean checkNetworkConnectivity() {
        if (!Util.isNetworkAvailable(this)) {

            //ACRA log
            ACRA.getErrorReporter().putCustomData("MainActivity.checkNetworkConnectivity()", "No network connectivity");

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

            //ACRA log
            ACRA.getErrorReporter().putCustomData("MainActivity.checkServerConnectivity()", "No server connectivity");

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

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private static BitmapDescriptor getIconFromPlace(Place place) {
        String type = place.getType();

        if ("atm".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.atm);
        } else if ("bureau_de_change".equals(type) || "bank".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.bank);
        } else if ("bar".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.bar);
        } else if ("cafe".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.cafe);
        } else if ("car_wash".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.carwash);
        } else if ("cinema".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.cinema);
        } else if ("clinic".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.clinic);
        } else if ("college".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.college);
        } else if ("dentist".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.dentist);
        } else if ("bakery".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.fastfood);
        } else if ("fuel".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.fuel);
        } else if ("sports_centre".equals(type) || "fitness_centre".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.gym);
        } else if ("hospital".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.hospital);
        } else if ("library".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.library);
        } else if ("marketplace".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.marketplace);
        } else if ("nightclub".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.club);
        } else if ("pharmacy".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.pharmacy);
        } else if ("police".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.police);
        } else if ("pub".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.pub);
        } else if ("restaurant".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.restaurant);
        } else if ("school".equals(type) || "kindergarten".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.school);
        } else if ("taxi".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.taxi);
        } else if ("theatre".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.theater);
        } else if ("university".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.university);
        } else if ("road_blocked".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.traffic_jam);
        } else if ("car_accident".equals(type)) {
            return BitmapDescriptorFactory.fromResource(R.drawable.car_accident);
        }

        return (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
    }
}