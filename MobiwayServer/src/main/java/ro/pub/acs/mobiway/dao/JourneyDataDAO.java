package ro.pub.acs.mobiway.dao;

import java.io.Serializable;
import java.util.*;
import ro.pub.acs.mobiway.model.*;

public interface JourneyDataDAO extends Serializable {
	public JourneyData get(int id);

	public int update(JourneyData journey);
	
	public List<JourneyData> getByJourneyId(Journey journeyId);

	public int add(JourneyData journey);
	
	public double maxSpeed(Journey journey);
	
	public double minSpeed(Journey journey);
}