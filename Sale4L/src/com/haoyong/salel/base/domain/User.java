package com.haoyong.salel.base.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.haoyong.salel.common.domain.AbstractDomain;

/**
 * 用户
 */
@Entity
@Table(name = "bs_user")
public class User extends AbstractDomain {

	private static final long serialVersionUID = 4942035927489392260L;

	private long sellerId;
	
	private int linkType;
	
	/** 登陆名称 */
	private String userId;
	
	/** 部门名 */
	private String deptName;
	/** 用户名 */
	private String userName;
	
	/** 商家加密密码 */
	private String password;
	
	@Transient
	public long getSellerId() {
		return sellerId;
	}

	public void setSellerId(long sellerId) {
		this.sellerId = sellerId;
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
		setUserName(userId);
		this.userId = userId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	@Column(length = 50)
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Column(length = 50)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}