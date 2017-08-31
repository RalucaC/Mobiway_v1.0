package ro.pub.acs.mobiway.service;

import javafx.util.Pair;
import org.springframework.scheduling.TaskScheduler;
import ro.pub.acs.mobiway.dao.StreetMeanSpeedDAO;
import ro.pub.acs.mobiway.model.StreetMeanSpeed;
import ro.pub.acs.mobiway.model.StreetSpeeds;

import java.util.Map;

public class PersistMeanSpeeds {

    private TaskScheduler taskScheduler;

    //@Autowired
    private StreetSpeeds streetSpeeds;

    //@Autowired
    private StreetMeanSpeedDAO streetMeanSpeedDAO;

    private class PersistMeanSpeedsTask implements Runnable {
        @Override
        public void run() {

            for (Map.Entry<String, Pair<Float, Integer>> streetMeanSpeedEntry : streetSpeeds.getStreetToSpeed().entrySet()) {
                String streetName = streetMeanSpeedEntry.getKey();

                StreetMeanSpeed streetMeanSpeedRow = streetMeanSpeedDAO.getStreetMeanSpeed(streetName);
                if (streetMeanSpeedRow == null) {
                    streetMeanSpeedRow = new StreetMeanSpeed();
                    streetMeanSpeedRow.setStreetName(streetName);
                }

                streetMeanSpeedRow.setMeanSpeed(streetMeanSpeedEntry.getValue().getKey());
                streetMeanSpeedRow.setNumberOfMeasures(streetMeanSpeedEntry.getValue().getValue());

                /*else {
                    Float oldMean = streetMeanSpeedRow.getMeanSpeed();
                    Float cacheMean = streetMeanSpeedEntry.getValue().getKey();
                    Integer oldNumberOfMeasures = streetMeanSpeedRow.getNumberOfMeasures();
                    Integer cacheNumberOfMeasures = streetMeanSpeedEntry.getValue().getValue();

                    Float newMean = (oldMean * oldNumberOfMeasures + cacheMean * cacheNumberOfMeasures) /
                            (oldNumberOfMeasures + cacheNumberOfMeasures);

                    streetMeanSpeedRow.setMeanSpeed(newMean);
                    streetMeanSpeedRow.setNumberOfMeasures(oldNumberOfMeasures + cacheNumberOfMeasures);
                } */
                System.out.println(streetMeanSpeedRow);
                streetMeanSpeedDAO.saveOrUpdateStreetMeanSpeed(streetMeanSpeedRow);
            }
        }
    }

    public PersistMeanSpeeds(TaskScheduler taskScheduler, StreetSpeeds streetSpeeds, StreetMeanSpeedDAO streetMeanSpeedDAO) {
        this.taskScheduler = taskScheduler;
        this.streetSpeeds = streetSpeeds;
        this.streetMeanSpeedDAO = streetMeanSpeedDAO;

        taskScheduler.scheduleAtFixedRate(new PersistMeanSpeedsTask(), 5 * 60 * 1000);
    }
}
