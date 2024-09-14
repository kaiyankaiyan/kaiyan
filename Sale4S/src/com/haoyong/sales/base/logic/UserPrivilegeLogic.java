package com.haoyong.sales.base.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import net.sf.mily.ui.WindowMonitor;

import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.common.dao.BaseDAO;

/**
 * 部门、岗位的权限
 */
public class UserPrivilegeLogic {
	
	/**
	 * 是否已经存在此岗位的用户
	 */
	public boolean isSingleRoleUser(User user) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=1 and t.deptName=? and t.userName=? and t.id!=? and t.sellerId=?");
		List<User> userList = new BaseDAO().nativeQuery(sql.toString(), User.class, user.getDeptName(), user.getUserName(), user.getId());
		return userList.isEmpty();
	}
	
	public boolean isContainRight(List<String> rightList, String right) {
		User user = (User)WindowMonitor.getMonitor().getAttribute("user");
		if ("admin".equals(user.getUserId())) {
			return true;
		}
		return rightList.contains(right);
	}

	public List<User> getRoleLinkRights(String deptName) {
		String sql = "select t.* from bs_user t join bs_user r on r.sellerId=t.sellerId and r.linkType=2 and r.deptName=t.userName where t.linkType=11 and t.deptName=? and t.sellerId=?";
		List<User> linkList = new BaseDAO().nativeSqlQuery(sql, User.class, deptName);
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
		String sql = "select t.* from bs_user t where t.linkType=21 and t.deptName=? and t.sellerId=?";
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
		String sql = "select t.* from bs_user t where t.linkType=0 and t.deptName is not null and t.sellerId=?";
		List<User> linkList = new BaseDAO().nativeQuery(sql, User.class);
		LinkedHashSet<String> deptList = new LinkedHashSet<String>();
		for (User item: linkList) {
			deptList.add(item.getDeptName());
		}
		return new ArrayList<String>(deptList);
	}

	public List<User> getRightAll() {
		StringBuffer sql = new StringBuffer();
		sql.append("select t.* from bs_user t where t.linkType=2 and t.sellerId=? order by t.id");
		return new BaseDAO().nativeQuery(sql.toString(), User.class);
	}
	
	/**
	 * 取用户权限列表
	 */
	public List<String> getRightsOfUser(User user) {
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
		LinkedHashSet<String> rightList = new LinkedHashSet<String>();
		if ("上面岗位、部门的权限列表".length()>0) {
			StringBuffer sql = new StringBuffer();
			sql.append("select t.* from bs_user t where t.linkType=11 and t.sellerId=? and (");
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
	
	/**
	 * 取角色用户列表
	 */
	public List<User> getUsersOfRole(String roleName) {
		List<String> deptList = new ArrayList<String>();
		if ("岗位分享给了哪些部门".length()>0) {
			StringBuffer sql = new StringBuffer();
			sql.append("select t.* from bs_user t where t.linkType=21 and t.deptName=? and t.sellerId=?");
			for (User roleLinkActor: new BaseDAO().nativeQuery(sql.toString(), User.class, roleName)) {
				deptList.add(roleLinkActor.getUserName());
			}
		}
		if ("上面部门的用户列表".length()>0 && deptList.size()>0) {
			StringBuffer sql = new StringBuffer();
			sql.append("select t.* from bs_user t where t.linkType=0 and t.sellerId=? and (");
			for (Iterator<String> iter=deptList.iterator(); iter.hasNext();) {
				String dept = iter.next();
				sql.append("t.deptName='").append(dept).append("'").append(iter.hasNext()? " or ": ")");
			}
			return new BaseDAO().nativeQuery(sql.append(" order by t.id").toString(), User.class);
		}
		return new ArrayList<User>();
	}
}
