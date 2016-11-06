package ro.pub.acs.mobiway.core;

import android.app.Activity;
import com.google.android.gms.maps.model.*;

import org.acra.ACRA;

public class RoutingHelper {

    private boolean getSrcFromGps;
    private Activity parentActivity;

    private LatLng srcLocation;
    private Marker srcMarker;

    private LatLng dstLocation;
    private Marker dstMarker;

    private LatLng userSelectedLocation;
    private Marker userSelecteMarker;

    public RoutingHelper(Activity parentActivity) {
        this.parentActivity = parentActivity;
        this.getSrcFromGps = true;
    }

    public boolean getUseGpsForSrc() {
        return getSrcFromGps;
    }

    public void setUseGpsForSrc(boolean getSrcFromGps) {
        this.getSrcFromGps = getSrcFromGps;
    }

    public void selectPoint(LatLng selectedPoint, Marker selectedMarker) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("RoutingHelper.selectPoint()", "method has been invoked");

        if (userSelecteMarker != null) {
            userSelecteMarker.setVisible(false);
        }

        userSelectedLocation = selectedPoint;
        userSelecteMarker = selectedMarker;
    }

    public void selectSrc() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("RoutingHelper.selectSrc()", "method has been invoked");

        if (userSelectedLocation != null) {

            if (srcMarker != null) {
                srcMarker.setVisible(false);
            }

            userSelecteMarker.setTitle("Source");
            srcMarker = userSelecteMarker;
            userSelecteMarker = null;
            srcLocation = userSelectedLocation;
        }

        userSelecteMarker = null;
    }

    public void selectDst() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("RoutingHelper.selectDst()", "method has been invoked");

        if (userSelectedLocation != null) {

            if (dstMarker != null) {
                // Make existing marker invisible first
                dstMarker.setVisible(false);
            }

            userSelecteMarker.setTitle("Destination");
            dstMarker = userSelecteMarker;
            userSelecteMarker = null;
            dstLocation = userSelectedLocation;
        }
    }

    public LatLng getSrcLocation() {
        return srcLocation;
    }

    public LatLng getDstLocation() {
        return dstLocation;
    }

    public void clear() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("RoutingHelper.clear()", "method has been invoked");

        srcLocation = null;
        if (srcMarker != null) {
            srcMarker.setVisible(false);
            srcMarker = null;
        }

        dstLocation = null;
        if (dstMarker != null) {
            dstMarker.setVisible(false);
            dstMarker = null;
        }

        userSelectedLocation = null;
        if (userSelecteMarker != null) {
            userSelecteMarker.setVisible(false);
            userSelecteMarker = null;
        }
    }

}


