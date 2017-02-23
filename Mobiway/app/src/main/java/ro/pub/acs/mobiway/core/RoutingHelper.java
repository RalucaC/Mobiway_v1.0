package ro.pub.acs.mobiway.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

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
    private Marker userSelectedMarker;

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

        if (userSelectedMarker != null && !isPOIMarker(userSelectedMarker) && !userSelectedMarker.equals(selectedMarker)) {
            userSelectedMarker.setVisible(false);
        }

        userSelectedLocation = selectedPoint;
        userSelectedMarker = selectedMarker;
    }

    public void selectSrc() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("RoutingHelper.selectSrc()", "method has been invoked");

        if (userSelectedLocation != null) {

            if (userSelectedMarker != null && userSelectedMarker.equals(dstMarker)) {
                userSelectedMarker = null;
                showAlertDialog();
                return;
            }

            if (srcMarker != null && !isPOIMarker(srcMarker) && !srcMarker.equals(userSelectedMarker)) {
                srcMarker.setVisible(false);
            }

            if (userSelectedMarker != null && !isPOIMarker(userSelectedMarker)) {
                userSelectedMarker.setTitle("Source");
            }
            srcMarker = userSelectedMarker;
            userSelectedMarker = null;
            srcLocation = userSelectedLocation;
        }

        userSelectedMarker = null;
    }

    public void selectDst() {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("RoutingHelper.selectDst()", "method has been invoked");

        if (userSelectedLocation != null) {

            if (userSelectedMarker != null && userSelectedMarker.equals(srcMarker)) {
                userSelectedMarker = null;
                showAlertDialog();
                return;
            }

            if (dstMarker != null && !isPOIMarker(dstMarker) && !dstMarker.equals(userSelectedMarker)) {
                // Make existing marker invisible first
                dstMarker.setVisible(false);
            }

            if (userSelectedMarker != null && !isPOIMarker(userSelectedMarker)) {
                userSelectedMarker.setTitle("Destination");
            }
            dstMarker = userSelectedMarker;
            userSelectedMarker = null;
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
        if (userSelectedMarker != null) {
            userSelectedMarker.setVisible(false);
            userSelectedMarker = null;
        }
    }

    private static boolean isPOIMarker(Marker marker) {
        return !marker.getTitle().equals("Marker") && !marker.getTitle().equals("Destination") && !marker.getTitle().equals("Source");
    }

    private void showAlertDialog() {
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(parentActivity)
                        .setTitle("Invalid source or destination")
                        .setMessage("The source cannot be the same as the destination. Please try again.")
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }
}
