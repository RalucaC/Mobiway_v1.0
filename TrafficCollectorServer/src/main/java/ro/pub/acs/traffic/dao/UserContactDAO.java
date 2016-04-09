package ro.pub.acs.traffic.dao;

import java.util.List;

import ro.pub.acs.traffic.model.User;
import ro.pub.acs.traffic.model.UserContact;

public interface UserContactDAO {
	public UserContact get(int id);
	public int update(UserContact userContact);
	public int add(UserContact userContact);
	public List<User> getFriends(User user);
	public List<String> getFriendsEmails(User user);
	public boolean addFriend(UserContact userContact);
}
