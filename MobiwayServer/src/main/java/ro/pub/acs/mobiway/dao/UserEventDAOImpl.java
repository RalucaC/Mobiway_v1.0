package ro.pub.acs.mobiway.dao;

import java.util.*;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;
import ro.pub.acs.mobiway.model.*;

public class UserEventDAOImpl implements UserEventDAO {
	private SessionFactory sessionFactory;

	public UserEventDAOImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@Transactional
	public List<UserEvent> get(Integer id) {
		 Criteria criteria = sessionFactory.getCurrentSession()
				 .createCriteria(UserEvent.class)
				 .add(Restrictions.eq("id", id));

		 List<Object> result = criteria.list();
		 List<UserEvent> listEvents = new ArrayList<UserEvent>();

		 for (Object event : result) {
			 UserEvent userEvent = (UserEvent) event;
			 listEvents.add(userEvent);
		 }
		

		 return listEvents;
	 }

	@Override
	@Transactional
	public List<UserEvent> get(String osmId) {
		 Criteria criteria = sessionFactory.getCurrentSession()
				 .createCriteria(UserEvent.class)
				 .add(Restrictions.eq("osmWayId", osmId));

		List<Object> result = criteria.list();
		List<UserEvent> listEvents = new ArrayList<UserEvent>();

		 for (Object event : result) {
			UserEvent userEvent = (UserEvent) event;
			listEvents.add(userEvent);
		 }
		 
		 return listEvents;
	}

	@Override
	@Transactional
	public int update(UserEvent event) {
		Session session = sessionFactory.getCurrentSession();
		session.update(event);

		return event.getId();
	}

	@Override
	@Transactional
	public int add(UserEvent event) {
		Session session = sessionFactory.getCurrentSession();
		session.save(event);

		return event.getId();
	}
}
