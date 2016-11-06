package ro.pub.acs.mobiway.gui.settings;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.*;

import com.google.android.gms.analytics.GoogleAnalytics;

import org.acra.ACRA;

import ro.pub.acs.mobiway.R;

public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("SettingsActivity.onCreate()", "method has been invoked");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        getSupportActionBar().setTitle(getResources().getString(R.string.action_settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("SettingsActivity.onCreateOptionsMenu()", "method has been invoked");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //ACRA log
        ACRA.getErrorReporter().putCustomData("SettingsActivity.onOptionsItemSelected()", "method has been invoked");

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("SettingsActivity.onStart()", "method has been invoked");

        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        super.onStop();
    }
}