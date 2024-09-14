package com.haoyong.salel.base.logic;

import java.util.List;

import com.haoyong.salel.base.domain.User;
import com.haoyong.salel.common.dao.BaseDAO;

public class UserLogic {
	
	public User getUser(String userId) {
		BaseDAO dao = new BaseDAO();
		return dao.nativeQuerySingleResult("select t.* from bs_user t where t.linkType=0 and t.userId=?", User.class, userId);
	}
	
	public boolean isSingleUserName(User user) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=0 and t.userName=? and t.id!=?");
		List<User> userList = new BaseDAO().nativeQuery(sql.toString(), User.class, user.getUserName(), user.getId());
		return userList.isEmpty();
	}
	
	public boolean isContainUserName(String userName) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=0 and t.userName=?");
		List<User> userList = new BaseDAO().nativeQuery(sql.toString(), User.class, userName);
		return userList.isEmpty()==false;
	}
}