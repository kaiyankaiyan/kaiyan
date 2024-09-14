package com.haoyong.sales.common.derby;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
/**
 * 用户
 */
@Entity
@Table(name = "bs_user")
public class User4d extends AbstractDerby {

	private static final long serialVersionUID = 4942035927489392260L;
	
	/**
	 * 0全局商家待处理
	 * 1领班师傅待处理
	 * 10商品deptName、userName各安装师傅安全库存数gson
	 */
	private int linkType;
	
	// 用户名
	private String userName;
	// 部门名称
	private String deptName;
	// 功能点待处理记录数
	private int rcount;

	private boolean changed=false;
	
	public int getLinkType() {
		return linkType;
	}

	public void setLinkType(int linkType) {
		if (this.linkType != linkType)
			this.changed = true;
		this.linkType = linkType;
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		if (StringUtils.equals(this.userName, userName)==false)
			this.changed = true;
		this.userName = userName;
	}

	@Column(length=50)
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		if (StringUtils.equals(this.deptName, deptName)==false)
			this.changed = true;
		this.deptName = deptName;
	}

	public int getRcount() {
		return rcount;
	}
	public void setRcount(int rcount) {
		if (this.rcount != rcount)
			this.changed = true;
		this.rcount = rcount;
	}

	@Transient
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
}