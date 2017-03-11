package ro.pub.acs.mobiway.gui.events;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import org.acra.ACRA;

import java.util.ArrayList;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.rest.RestClient;

public class EventsFragment extends PreferenceFragment{

    private final SharedPreferencesManagement spm = SharedPreferencesManagement.getInstance(null);
    private final String eventNamePolice = "police";
    private final String eventNameTrafficJam = "traffic_jam";

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

        final CheckBoxPreference policyCheckbox = (CheckBoxPreference) findPreference("checkbox_preference_police");
        final CheckBoxPreference trafficCheckbox = (CheckBoxPreference) findPreference("checkbox_preference_traffic_jam");

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
                if(eventDetails.isEnabled() && eventDetails.getText().compareTo("") != 0 ) {
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
                            final ro.pub.acs.mobiway.rest.model.Location location = new ro.pub.acs.mobiway.rest.model.Location();
//                @Path ("eventName") String eventName,
//                @Path ("distance") Float distance,
//                @Path("timeSinceEvent") Float timeSinceEvent,
//                @Path ("spaceAccuracy") Float spaceAccuracy,
//                @Path ("timeAccuracy") Float timeAccuracy,
//                @Path ("latitude") Float latitude,
//                @Path ("longitude") Float longitude,
//                @Path ("osmWayId") String osmWayId

                            // send event to the server
                            RestClient restClient = new RestClient();
                            restClient.getApiService().postEvent(eventName, distance, distance, distance, distance, spm.getLatitude(), spm.getLongitude(), "", location);

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
                if(checkbox.getKey().compareTo("checkbox_preference_police") == 0) {
                    return eventNamePolice;
                }
                if(checkbox.getKey().compareTo("checkbox_preference_traffic_jam") == 0) {
                    return eventNameTrafficJam;
                }
            }
        }
        return "";
    }
}
