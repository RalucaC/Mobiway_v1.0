package ro.pub.acs.mobiway.gui.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

import org.acra.ACRA;

import java.util.Locale;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.core.LocationService;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.gui.main.MainActivity;

public class AuthenticationActivity extends ActionBarActivity implements FragmentManager.OnBackStackChangedListener {
    CallbackManager callbackManager;

    // Log tag
    private static final String TAG = AuthenticationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "===  onCreate === ");

        //ACRA log
        ACRA.getErrorReporter().putCustomData("AuthenticationActivity.onCreate()", "method has been invoked");

        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        SharedPreferencesManagement sharedPreferencesManagement = SharedPreferencesManagement.getInstance(getApplicationContext());
        Double latitude = sharedPreferencesManagement.getLatitude();
        Double longitude = sharedPreferencesManagement.getLongitude();

        if(latitude == 0 && longitude == 0){
            LocationService ls = new LocationService(getApplicationContext());
            sharedPreferencesManagement.setLatitude(ls.getLatitude());
            sharedPreferencesManagement.setLongitude(ls.getLongitude());
        }

        /* set selected language */
        Locale myLocale = new Locale(sharedPreferencesManagement.getLanguage());
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        sharedPreferencesManagement.setFirstTimeUse(false);
        /* verify if user is logged in */
        if (sharedPreferencesManagement.isLoggedIn()) {

            //ACRA log
            ACRA.getErrorReporter().putCustomData("AuthenticationActivity.onCreate():alreadyLoggedIn", "user is already logged in");

            Log.d(TAG, "user is already logged in");
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {

            //ACRA log
            ACRA.getErrorReporter().putCustomData("AuthenticationActivity.onCreate():logIn", "going to log in");

            Log.d(TAG, "going to log in");
            setContentView(R.layout.activity_authentication);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.container, new LoginFragment());
            fragmentTransaction.commit();

            fragmentManager.addOnBackStackChangedListener(this);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

                requestPermissions(permissions, 1);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_authentication, menu);
        return true;
    }

    @Override
    public void onBackStackChanged() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("AuthenticationActivity.onBackStackChanged()", "method has been invoked");

        boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("AuthenticationActivity.onOptionsItemSelected()", "method has been invoked");

        switch (item.getItemId()) {
            case android.R.id.home: {
                FragmentManager fm = getSupportFragmentManager();

                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                }

                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
