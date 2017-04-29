package ro.pub.acs.mobiway.core;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import ro.pub.acs.mobiway.R;

@ReportsCrashes(
//        formUri = "http://www.backendofyourchoice.com/reportpath",//todo
        mailTo = "raluca.constanda@gmail.com, radu.ciobanu@cs.pub.ro",
//        customReportContent = { APP_VERSION, ANDROID_VERSION, PHONE_MODEL, CUSTOM_DATA, STACK_TRACE, LOGCAT }
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
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

    public static void trackBreadcrumb(String event) {
        ACRA.getErrorReporter().putCustomData("Event at " + System.currentTimeMillis(), event);
    }
}
