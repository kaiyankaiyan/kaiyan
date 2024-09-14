package com.haoyong.salel.base.logic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.haoyong.salel.base.domain.User;
import com.haoyong.salel.common.dao.BaseDAO;

/**
 * 部门、岗位的权限
 */
public class PrivilegeLogic {
	
	public boolean isSingleRoleUser(User user) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=1 and t.deptName=? and t.userName=? and t.id!=?");
		List<User> userList = new BaseDAO().nativeQuery(sql.toString(), User.class, user.getDeptName(), user.getUserName(), user.getId());
		return userList.isEmpty();
	}

	public List<User> getRoleLinkRights(String deptName) {
		String sql = "select t.* from bs_user t where t.linkType=11 and t.deptName=?";
		List<User> linkList = new BaseDAO().nativeQuery(sql, User.class, deptName);
		return linkList;
	}
	
	public List<String> getRoleLinkRightNames(String deptName) {
		List<String> rightList = new ArrayList<String>();
		for (User link: this.getRoleLinkRights(deptName)) {
			rightList.add(link.getUserName());
		}
		return rightList;
	}
	
	public List<User> getRoleLinkActors(String roleName) {
		String sql = "select t.* from bs_user t where t.linkType=21 and t.deptName=?";
		List<User> linkList = new BaseDAO().nativeQuery(sql, User.class, roleName);
		return linkList;
	}
	
	public List<String> getRoleLinkActorNames(String roleName) {
		List<String> actorList = new ArrayList<String>();
		for (User link: this.getRoleLinkActors(roleName)) {
			actorList.add(link.getUserName());
		}
		return actorList;
	}
	
	public List<String> getActorDepts() {
		String sql = "select t.* from bs_user t where t.linkType=0 and t.deptName is not null";
		List<User> linkList = new BaseDAO().nativeQuery(sql, User.class);
		LinkedHashSet<String> deptList = new LinkedHashSet<String>();
		for (User item: linkList) {
			deptList.add(item.getDeptName());
		}
		return new ArrayList<String>(deptList);
	}

	public List<User> getRightAll() {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=2 order by t.id");
		return new BaseDAO().nativeQuery(sql.toString(), User.class);
	}
	
	/**
	 * 取用户权限列表
	 */
	public List<String> getRightsOfUser(User user) {
		List<String> roleList = new ArrayList<String>();
		if (true) {
			StringBuffer sql = new StringBuffer();
			sql.append("select t.* from bs_user t where t.linkType=1 and t.userName=?");
			for (User roleLinkUser: new BaseDAO().nativeQuery(sql.toString(), User.class, user.getUserName())) {
				roleList.add(roleLinkUser.getDeptName());
			}
		}
		if (true) {
			StringBuffer sql = new StringBuffer();
			sql.append("select t.* from bs_user t where t.linkType=21 and t.userName=?");
			roleList.add(user.getDeptName());
			for (User roleLinkActor: new BaseDAO().nativeQuery(sql.toString(), User.class, user.getDeptName())) {
				roleList.add(roleLinkActor.getDeptName());
			}
		}
		LinkedHashSet<String> rightList = new LinkedHashSet<String>();
		if (true) {
			StringBuffer sql = new StringBuffer();
			sql.append("select t.* from bs_user t where t.linkType=11 and (");
			for (String role: roleList) {
				sql.append("t.deptName='").append(role).append("' or ");
			}
			sql.delete(sql.length()-4, sql.length()).append(")");
			for (User roleLinkRight: new BaseDAO().nativeQuery(sql.toString(), User.class)) {
				rightList.add(roleLinkRight.getUserName());
			}
		}
		return new ArrayList<String>(rightList);
	}
}
