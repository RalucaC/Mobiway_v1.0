package ro.pub.acs.mobiway.dao;

import java.util.List;

import ro.pub.acs.mobiway.model.Location;
import ro.pub.acs.mobiway.model.User;

public interface LocationDAO {
	public List<Location> list();
	public Location getLocation(User user);
	public Integer updateLocation(Location location);
	public Integer addLocation(Location location);
}