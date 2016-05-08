package ro.pub.acs.mobiway.gui.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.general.Constants;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.rest.RestClient;
import ro.pub.acs.mobiway.rest.model.Policy;

public class SettingsFragment extends PreferenceFragment {

    private final SharedPreferencesManagement spm = SharedPreferencesManagement.getInstance(null);

    public SettingsFragment() {
        // Required empty public constructor
    }

    private void setAcceptedPolicies() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferencesManagement spm = SharedPreferencesManagement.getInstance(null);
                    Set<String> policyPreferences = spm.getUserPolicies();
                    List<String> policyList = new ArrayList<String>();
                    policyList.addAll(policyPreferences);

                    RestClient restClient = new RestClient();
                    restClient.getApiService().acceptUserPolicyListForApp(
                            spm.getAuthUserId(), Constants.APP_NAME, policyList);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        thread.start();
    }


    private void populatePolicyList(List<Policy> appPolicies) {
        PreferenceScreen screen = this.getPreferenceScreen();
        PreferenceCategory policyCat = (PreferenceCategory) findPreference(getString(R.string.policy_settings));
        Set<String> acceptedPolicies = spm.getUserPolicies();

        for (Policy policy : appPolicies) {
            CheckBoxPreference checkBoxPref = new CheckBoxPreference(screen.getContext());
            checkBoxPref.setKey(policy.getPolicyName());
            checkBoxPref.setTitle(policy.getPolicyName());
            checkBoxPref.setSummary(policy.getPolicyDescription());
            checkBoxPref.setChecked(acceptedPolicies.contains(policy.getPolicyName()));
            policyCat.addPreference(checkBoxPref);
        }
    }

    private void updatePolicyList() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<Policy> policies = new ArrayList<>();
                try {
                    RestClient restClient = new RestClient();
                    policies = restClient.getApiService().getPolicyListApp(Constants.APP_NAME);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                populatePolicyList(policies);
            }
        });
        thread.start();
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

        updatePolicyList();
    }

    @Override
    public void onStop() {
        getPolicyPreferences();
        getPreferences();
        super.onStop();
    }

    private void getPolicyPreferences() {
        Set<String> acceptedPolicies = new HashSet<>();

        PreferenceCategory policyCat = (PreferenceCategory) findPreference(getString(R.string.policy_settings));
        int count = policyCat.getPreferenceCount();
        for (int i = 0; i < count; ++i) {
            CheckBoxPreference ck = (CheckBoxPreference) policyCat.getPreference(i);
            if (ck != null && ck.isChecked()) {
                acceptedPolicies.add(ck.getTitle().toString());
            }
        }
        spm.setUserPolicy(acceptedPolicies);
        setAcceptedPolicies();
    }

    private Set<String> getPreferences(){

        Set<String> userLocPreferences = new HashSet<>();
        String[] categories = getResources().getStringArray(R.array.user_loc);

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
}