package com.haoyong.sales.base.domain;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.domain.PropertyChoosable;
/**
 * 用户
 */
@Entity
@Table(name = "bs_user")
public class User extends AbstractDomain implements PropertyChoosable<User> {

	private static final long serialVersionUID = 4942035927489392260L;
	
	/** RoleLinkRight类型
	 * 0 部门人员，部门
	 * 1 岗位人员，岗位
	 * 2 功能点Right 索引名deptName,显示名userName
	 * 11 RoleLinkRight 角色拥有的权限 deptName,userName
	 * 21 RoleLinkActor 角色共享给部门 deptName,userName
	 */
	private int linkType;
	
	// 登陆名称, 用户名, 部门名称
	private String userId, userName, deptName;
	
	/** 商家加密密码 */
	private String password;
	private String linkerCall, address;
	
	@Transient
	private User getChoose() {
		return this;
	}
	
	@Transient
	public List<String> getTrunkDefault() {
		return Arrays.asList(new String[]{"deptName", "userId", "userName", "password"});
	}
	
	public int getLinkType() {
		return linkType;
	}

	public void setLinkType(int linkType) {
		this.linkType = linkType;
	}

	@Column(length = 50)
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(length = 50)
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Transient
	public String getName() {
		return this.getUserName();
	}

	public void setName(String linker) {
		this.setUserName(linker);
	}

	@Column(length = 50)
	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	@Column(length = 30)
	public String getLinkerCall() {
		return linkerCall;
	}

	public void setLinkerCall(String linkerCall) {
		this.linkerCall = linkerCall;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}