package ro.pub.acs.mobiway.dao;


import ro.pub.acs.mobiway.model.StreetMeanSpeed;

import java.util.List;

public interface StreetMeanSpeedDAO {

    List<StreetMeanSpeed> getAllStreetMeanSpeeds();

    StreetMeanSpeed getStreetMeanSpeed(String street);

    void saveOrUpdateStreetMeanSpeed(StreetMeanSpeed streetMeanSpeed);

    void deleteAllRecords();

}
