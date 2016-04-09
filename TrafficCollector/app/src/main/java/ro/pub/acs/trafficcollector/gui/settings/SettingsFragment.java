package ro.pub.acs.trafficcollector.gui.settings;


import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import java.util.ArrayList;

import ro.pub.acs.trafficcollector.R;
import ro.pub.acs.trafficcollector.general.SharedPreferencesManagement;
import ro.pub.acs.trafficcollector.gui.main.MainActivity;

public class SettingsFragment extends PreferenceFragment {

    private ArrayList<String> categories =  new ArrayList<>();

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);

        final SharedPreferencesManagement spm = SharedPreferencesManagement.getInstance(null);
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

    private void getPreferences(){
        if(MainActivity.prefList == null)
            MainActivity.prefList = new ArrayList();
        else
            MainActivity.prefList.clear();

        Log.e("prefList cleared", MainActivity.prefList.toString());

        for (String category : categories){
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(category);
            if(checkBoxPreference.isChecked()){
                MainActivity.prefList.add(category);
            }
        }

        Log.e("prefList", MainActivity.prefList.toString());
    }

    private void setListeners(){
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

        getPreferences();

        for (final String category : categories){
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(category);
            checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(new Boolean(newValue.toString())){
                        MainActivity.prefList.add(category);
                    } else {
                        MainActivity.prefList.remove(category);
                    }

                    Log.e("prefList changed", MainActivity.prefList.toString());

                    return true;
                }
            });
        }
    }
}