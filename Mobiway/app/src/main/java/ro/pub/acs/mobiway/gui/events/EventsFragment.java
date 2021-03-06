package ro.pub.acs.mobiway.gui.events;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.acra.ACRA;

import java.util.ArrayList;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.general.Constants;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.rest.RestClient;

public class EventsFragment extends PreferenceFragment{

    private final SharedPreferencesManagement spm = SharedPreferencesManagement.getInstance(null);
    private final String eventNameRoadBlocked = "Road Blocked";
    private final String eventNameCarAccident = "Car Accident";
    private static final String TAG = EventsFragment.class.getSimpleName();

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("EventsFragment.onCreate()", "method has been invoked");

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_events);

        final ArrayList<CheckBoxPreference> eventsCheckBoxes = new ArrayList<>();

        final CheckBoxPreference policyCheckbox = (CheckBoxPreference) findPreference("checkbox_preference_car_accident");
        final CheckBoxPreference trafficCheckbox = (CheckBoxPreference) findPreference("checkbox_preference_road_blocked");

        eventsCheckBoxes.add(policyCheckbox);
        eventsCheckBoxes.add(trafficCheckbox);

        uncheckEvents(eventsCheckBoxes);

        final EditTextPreference eventDetails = (EditTextPreference) findPreference("event_details");

        Preference button = findPreference("send_event_button");

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final String eventName = getEventName(eventsCheckBoxes);

                // get the details
                if(eventDetails.isEnabled() && eventDetails.getText() != null && eventDetails.getText().compareTo("") != 0 ) {
                    String details = eventDetails.getText();


                } else {

                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "No event details", Toast.LENGTH_SHORT);
                    toast.show();

                    return false;
                }

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {

                            Float distance = Float.valueOf("0");
                            Float timeSinceEvent = Float.valueOf("60");
                            Float spaceAccuracy = Float.valueOf("10");
                            Float timeAccuracy = Float.valueOf("0");

                            final ro.pub.acs.mobiway.rest.model.Location location = new ro.pub.acs.mobiway.rest.model.Location();
                            location.setIdUser(spm.getAuthUserId());
                            location.setLatitude((float)spm.getLatitude());
                            location.setLongitude((float)spm.getLongitude());

                            Log.v(TAG, "eventName" + eventName);
                            Log.v(TAG, "distance" + distance);
                            Log.v(TAG, "timeSinceEvent" + timeSinceEvent);
                            Log.v(TAG, "spaceAccuracy" + spaceAccuracy);
                            Log.v(TAG, "timeAccuracy" + timeAccuracy);

                            postEventOnFacebook((float)spm.getLatitude(), (float)spm.getLongitude(), eventName);

                            // send event to the server
                            RestClient restClient = new RestClient();
                            restClient.getApiService().postEvent(eventName, distance, timeSinceEvent, spaceAccuracy, timeAccuracy, location);

                        } catch (Exception e) {

                            //ACRA log
                            ACRA.getErrorReporter().putCustomData("MainActivity.navigateToLocation():errorSetLocation1", e.toString());

                            e.printStackTrace();
                        }
                    }
                });
                thread.start();

                return true;
            }
        });

    }

    private void postEventOnFacebook(float latitudine, float longitude, String eventName) {

        Bundle params = new Bundle();
        params.putString("link", "http://www.google.com/maps/place/" + latitudine + "," + longitude + "\n");
        params.putString("message", eventName);

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + Constants.GROUP_ID + "/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                    /* handle the result */
                        Log.d("post event to fb", response.toString());
                    }
                }
        ).executeAndWait();
    }

    private void uncheckEvents(ArrayList<CheckBoxPreference> eventsCheckboxes){

        final ArrayList<CheckBoxPreference> alViewMode = new ArrayList<>();

        Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                for (CheckBoxPreference cbp : alViewMode) {
                    if (!cbp.getKey().equals(preference.getKey()) && cbp.isChecked()) {
                        cbp.setChecked(false);
                    }
                    else if (cbp.getKey().equals(preference.getKey()) && !cbp.isChecked()) {
                        cbp.setChecked(true);
                    }
                }
                return false;
            }
        };

        for (CheckBoxPreference checkbox : eventsCheckboxes ) {

            checkbox.setOnPreferenceClickListener(listener);
            alViewMode.add(checkbox);
        }

    }

    private String getEventName(ArrayList<CheckBoxPreference> eventsCheckboxes){

        for (CheckBoxPreference checkbox : eventsCheckboxes) {
            if(checkbox.isChecked()) {
                if(checkbox.getKey().compareTo("checkbox_preference_car_accident") == 0) {
                    return eventNameCarAccident;
                }
                if(checkbox.getKey().compareTo("checkbox_preference_road_blocked") == 0) {
                    return eventNameRoadBlocked;
                }
            }
        }
        return "";
    }
}
