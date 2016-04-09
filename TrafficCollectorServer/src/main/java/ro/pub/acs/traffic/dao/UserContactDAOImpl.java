package ro.pub.acs.traffic.dao;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.*;
import org.hibernate.criterion.Restrictions;

import ro.pub.acs.traffic.model.*;

public class UserContactDAOImpl implements UserContactDAO {
	private SessionFactory sessionFactory;

	public UserContactDAOImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public UserContact get(int id) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(User.class).add(Restrictions.eq("id", id));

		Object result = criteria.uniqueResult();
		UserContact userContact = null;
		if (result != null)
			userContact = (UserContact) result;

		return userContact;
	}

	@Override
	public int update(UserContact userContact) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(userContact);

		return userContact.getId().intValue();
	}

	@Override
	public int add(UserContact userContact) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(userContact);

		return userContact.getId().intValue();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public List<User> getFriends(User user) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(UserContact.class)
				.add(Restrictions.eq("idUser", user));

		List<Object> result = criteria.list();
		List<User> listUser = new ArrayList<User>();

		for (Object usr : result) {
			UserContact userContact = (UserContact) usr;
			User friend = new UserDAOImpl(sessionFactory).get(userContact
					.getIdFriendUser().getId());
			listUser.add(friend);
		}

		return listUser;
	}

	@Override
	@Transactional
	public boolean addFriend(UserContact userContact) {
		Session session = sessionFactory.getCurrentSession();
		session.save(userContact);

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public List<String> getFriendsEmails(User user) {
		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(UserContact.class)
				.add(Restrictions.eq("idUser", user));

		List<Object> result = criteria.list();
		List<String> listUser = new ArrayList<String>();

		for (Object usr : result) {
			UserContact userContact = (UserContact) usr;
			User friend = new UserDAOImpl(sessionFactory).get(userContact
					.getIdFriendUser().getId());
			listUser.add(friend.getUsername());
		}

		return listUser;
	}
	
}
