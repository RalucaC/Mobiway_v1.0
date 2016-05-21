package ro.pub.acs.mobiway.dao;

import java.util.*;

import org.hibernate.*;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import ro.pub.acs.mobiway.model.*;

public class JourneyDataDAOImpl implements JourneyDataDAO {
	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;

	public JourneyDataDAOImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@Transactional
	public JourneyData get(int id) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(User.class).add(Restrictions.eq("id", id));

		Object result = criteria.uniqueResult();
		JourneyData journeyData = null;
		if (result != null)
			journeyData = (JourneyData) result;

		return journeyData;
	}
	
	@Override
	@Transactional
	public List<JourneyData> getByJourneyId(Journey journeyId) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(JourneyData.class).add(Restrictions.eq("journeyId", journeyId));

		// @SuppressWarnings("unchecked")
		return criteria.list();
	}


	@Override
	@Transactional
	public int add(JourneyData journeyData) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(journeyData);

		return journeyData.getId().intValue();
	}

	@Override
	@Transactional
	public int update(JourneyData journeyData) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(journeyData);

		return journeyData.getId().intValue();
	}

	@Override
	@Transactional
	public double maxSpeed(Journey journey) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(JourneyData.class);
		criteria.add(Restrictions.eq("journeyId", journey));
		criteria.setProjection(Projections.max("speed"));
		Integer maxSpeed = (Integer)criteria.uniqueResult();
		
		return maxSpeed;
	}

	@Override
	@Transactional
	public double minSpeed(Journey journey) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(JourneyData.class);
		criteria.add(Restrictions.eq("journeyId", journey));
		criteria.setProjection(Projections.min("speed"));
		Integer minSpeed = (Integer)criteria.uniqueResult();
		
		return minSpeed;
	}
}