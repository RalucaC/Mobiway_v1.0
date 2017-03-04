package ro.pub.acs.mobiway.gui.events;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.acra.ACRA;

import ro.pub.acs.mobiway.R;

public class EventsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("EventsActivity.onCreate()", "method has been invoked");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_events);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new EventsFragment())
                .commit();

        getSupportActionBar().setTitle(getResources().getString(R.string.action_post_event));
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}
