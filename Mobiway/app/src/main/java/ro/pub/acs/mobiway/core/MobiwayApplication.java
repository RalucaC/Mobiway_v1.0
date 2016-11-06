package ro.pub.acs.mobiway.core;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        formUri = "http://www.backendofyourchoice.com/reportpath"
)
public class MobiwayApplication extends Application{

    private static MobiwayApplication application ;


    public MobiwayApplication getInstance(){
        return application;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
