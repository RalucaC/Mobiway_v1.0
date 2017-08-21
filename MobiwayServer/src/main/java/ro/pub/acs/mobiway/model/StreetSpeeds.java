package ro.pub.acs.mobiway.model;

import javafx.util.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreetSpeeds {

    Map<String, Pair<Float, Integer>> streetToSpeedAndNumberOfMeasures;

    public StreetSpeeds() {
        streetToSpeedAndNumberOfMeasures = new ConcurrentHashMap<>();
    }

    public Map<String, Pair<Float, Integer>> getStreetToSpeed() {
        return streetToSpeedAndNumberOfMeasures;
    }
}
