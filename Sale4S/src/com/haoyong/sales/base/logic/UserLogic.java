package com.haoyong.sales.base.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.domain.UserT;
import com.haoyong.sales.base.form.UserForm;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;


public class UserLogic {
	
	public PropertyChoosableLogic.Choose12<UserForm, User, UserT> getChoosableLogic() {
		return new PropertyChoosableLogic.Choose12<UserForm, User, UserT>(new UserForm(), new User(), new UserT());
	}
	
	public User getUser(long uid) {
		List<User> userList = new BaseDAO().nativeSqlQuery("select t.* from bs_user t where t.linkType=0 and t.id=? and t.sellerId=?", User.class, uid);
		return userList.size()==0? null: userList.get(0);
	}
	
	public User getUser(String userId) {
		User user = new BaseDAO().nativeQuerySingleResult("select t.* from bs_user t where t.linkType=0 and t.userId=? and t.sellerId=?", User.class, userId);
		return user;
	}
	public User getUserByName(String userName) {
		User user = new BaseDAO().nativeQuerySingleResult("select t.* from bs_user t where t.linkType=0 and t.userName=? and t.sellerId=?", User.class, userName);
		return user;
	}
	
	public boolean isSingleUserName(User user) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=0 and t.userName=? and t.id!=? and t.sellerId=?");
		List<User> userList = new BaseDAO().nativeQuery(sql.toString(), User.class, user.getUserName(), user.getId());
		return userList.isEmpty();
	}
	
	public boolean isContainUserName(String userName) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=0 and t.userName=? and t.sellerId=?");
		List<User> userList = new BaseDAO().nativeQuery(sql.toString(), User.class, userName);
		return userList.isEmpty()==false;
	}
	
	public String getInstallRoleName() {
		return "领班人员";
	}
	
	/**
	 * 是否工程安装领班人员
	 */
	public boolean isInstallRole(User user) {
		List<String> roleList = new ArrayList<String>();
		if ("岗位用户的岗位".length()>0) {
			StringBuffer sql = new StringBuffer();
			sql.append("select t.* from bs_user t where t.linkType=1 and t.userName=? and t.sellerId=?");
			for (User roleLinkUser: new BaseDAO().nativeQuery(sql.toString(), User.class, user.getUserName())) {
				roleList.add(roleLinkUser.getDeptName());
			}
		}
		if ("本部门获得了哪些岗位分享".length()>0) {
			StringBuffer sql = new StringBuffer();
			sql.append("select t.* from bs_user t where t.linkType=21 and t.userName=? and t.sellerId=?");
			roleList.add(user.getDeptName()); // 用户本部门
			for (User roleLinkActor: new BaseDAO().nativeQuery(sql.toString(), User.class, user.getDeptName())) {
				roleList.add(roleLinkActor.getDeptName());
			}
		}
		String rname = this.getInstallRoleName();
		for (String roleName: roleList) {
			if (StringUtils.equals(roleName, rname)) {
				return true;
			}
		}
		return false;
	}
}