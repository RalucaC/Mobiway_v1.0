package ro.pub.acs.mobiway.gui.settings;


import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;

public class SettingsFragment extends PreferenceFragment {

    private ArrayList<String> categories =  new ArrayList<>();

    private final SharedPreferencesManagement spm = SharedPreferencesManagement.getInstance(null);
    private ArrayList<String> checkedCategories = new ArrayList<>();

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);

        final EditTextPreference etp = (EditTextPreference) findPreference(getResources().getString(R.string.key_account_preference));
        final SwitchPreference sp = (SwitchPreference) findPreference(getResources().getString(R.string.key_location_preference));
        final ListPreference mapListPreference = (ListPreference) findPreference(getResources().getString(R.string.key_map_type_list_preference));
        final ListPreference routeListPreference = (ListPreference) findPreference(getResources().getString(R.string.key_route_type_list_preference));

        etp.setSummary(spm.getAuthUserFirstName() + " " + spm.getAuthUserLastName());
        mapListPreference.setSummary(spm.getMapType());
        routeListPreference.setSummary(spm.getRouteType());

        sp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                spm.setNotificationsEnabled(new Boolean(newValue.toString()));
                return true;
            }
        });

        mapListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                spm.setMapType(newValue.toString());
                mapListPreference.setSummary(spm.getMapType());
                return true;
            }
        });

        routeListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                spm.setRouteType(newValue.toString());
                routeListPreference.setSummary(spm.getRouteType());
                return true;
            }
        });

        setListeners();
    }

    private Set<String> getPreferences(){

        Set<String> userLocPreferences = new HashSet<>();

        for (String category : categories) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(category);

            if(checkBoxPreference.isChecked()) {
                userLocPreferences.add(category);
            }
        }

        Log.e("prefList", userLocPreferences.toString());
        spm.setUserLocPreferences(userLocPreferences);

        return userLocPreferences;

    }

    private void setListeners() {
        categories.add("bar");
        categories.add("cafe");
        categories.add("fast_food");
        categories.add("restaurant");
        categories.add("pub");
        categories.add("school");
        categories.add("college");
        categories.add("library");
        categories.add("university");
        categories.add("fuel");
        categories.add("taxi");
        categories.add("car_wash");
        categories.add("atm");
        categories.add("bank");
        categories.add("clinic");
        categories.add("dentist");
        categories.add("hospital");
        categories.add("pharmacy");
        categories.add("cinema");
        categories.add("night_club");
        categories.add("theatre");
        categories.add("gym");
        categories.add("marketplace");
        categories.add("police");

        final Set<String> userLocPreferences = getPreferences();

        for (final String category : categories) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(category);
            checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(new Boolean(newValue.toString())){
                        userLocPreferences.add(category);
                    } else {
                        userLocPreferences.remove(category);
                    }

                    Log.e("prefList changed", userLocPreferences.toString());

                    spm.setUserLocPreferences(userLocPreferences);
                    return true;
                }
            });
        }
    }
}