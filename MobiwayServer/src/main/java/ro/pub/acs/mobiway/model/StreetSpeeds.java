package ro.pub.acs.mobiway.model;

import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import ro.pub.acs.mobiway.dao.StreetMeanSpeedDAO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreetSpeeds {

    Map<String, Pair<Float, Integer>> streetToSpeedAndNumberOfMeasures;

    //@Autowired
    private StreetMeanSpeedDAO streetMeanSpeedDao;

    public StreetSpeeds(StreetMeanSpeedDAO streetMeanSpeedDao) {
        this.streetMeanSpeedDao = streetMeanSpeedDao;

        streetToSpeedAndNumberOfMeasures = new ConcurrentHashMap<>();

        List<StreetMeanSpeed> streetMeanSpeeds = streetMeanSpeedDao.getAllStreetMeanSpeeds();

        for (StreetMeanSpeed streetMeanSpeed : streetMeanSpeeds) {
            streetToSpeedAndNumberOfMeasures.put(streetMeanSpeed.getStreetName(),
                    new Pair<>(streetMeanSpeed.getMeanSpeed(), streetMeanSpeed.getNumberOfMeasures()));
        }
    }

    public Map<String, Pair<Float, Integer>> getStreetToSpeed() {
        return streetToSpeedAndNumberOfMeasures;
    }

    public void resetStreetSpeeds() {
        streetToSpeedAndNumberOfMeasures.clear();
    }
}
