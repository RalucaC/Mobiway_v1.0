package ro.pub.acs.mobiway.dao;

import java.io.Serializable;
import java.util.*;

import ro.pub.acs.mobiway.model.*;

public interface UserEventDAO extends Serializable {

	public List<UserEvent> get(Integer id);

	public List<UserEvent> get(String osmId);

	public int update(UserEvent event);

	public int add(UserEvent event);
}
