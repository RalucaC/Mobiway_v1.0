package ro.pub.acs.traffic.dao;

import java.util.List;
import ro.pub.acs.traffic.model.*;

public interface PolicyDAO {
	public List<Policy> list(String appId);
	public List<Policy> list(String appId, User user);
	public Policy get(String policyName, String appId);
	public Policy get(int id);
	public Integer updatePolicy(Policy policy);
	public Integer addPolicy(Policy policy);
}