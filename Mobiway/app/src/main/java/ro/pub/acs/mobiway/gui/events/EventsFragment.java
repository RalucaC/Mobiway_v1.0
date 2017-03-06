package ro.pub.acs.mobiway.gui.events;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import org.acra.ACRA;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.rest.RestClient;

public class EventsFragment extends PreferenceFragment{

    private final SharedPreferencesManagement spm = SharedPreferencesManagement.getInstance(null);

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("EventsFragment.onCreate()", "method has been invoked");

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_events);


        final CheckBoxPreference policyCheckbox = (CheckBoxPreference) findPreference("checkbox_preference");
        final EditTextPreference eventDetails = (EditTextPreference) findPreference("event_details");

        policyCheckbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) {

                    eventDetails.setEnabled(true);
                } else {

                    eventDetails.setEnabled(false);
                }

                Log.d("Events", "checked value: " + checked);

                return true;
            }
        });

        Preference button = findPreference("send_event_button");

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if(policyCheckbox.isChecked()) {
                    String details = eventDetails.getText();
                } else {
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
                            restClient.getApiService().postEvent("police", distance, distance, distance, distance, spm.getLatitude(), spm.getLongitude(), "", location);


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

}
