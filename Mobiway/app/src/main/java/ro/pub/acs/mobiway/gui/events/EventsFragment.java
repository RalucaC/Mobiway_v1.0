package ro.pub.acs.mobiway.gui.events;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import org.acra.ACRA;

import ro.pub.acs.mobiway.R;

public class EventsFragment extends PreferenceFragment{

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("EventsFragment.onCreate()", "method has been invoked");

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_events);


        CheckBoxPreference pref = (CheckBoxPreference) findPreference("checkbox_preference");
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                boolean checked = Boolean.valueOf(newValue.toString());

                //set your shared preference value equal to checked
Log.d("Events", "checked value: " + checked);
                return true;
            }
        });



    }

}
