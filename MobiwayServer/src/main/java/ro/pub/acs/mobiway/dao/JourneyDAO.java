package ro.pub.acs.mobiway.dao;

import java.io.Serializable;
import java.util.*;

import ro.pub.acs.mobiway.model.*;

public interface JourneyDAO extends Serializable {
	public Journey get(int id);

	public Journey get(User user);

	public List<Journey> getByUserId(User userId);

	public int update(Journey journey);

	public int add(Journey journey);
	public void add2(JourneyData journey);
	
	public Journey getCurrentJourney(User userId);
	
}