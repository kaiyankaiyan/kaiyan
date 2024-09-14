package com.haoyong.sales.base.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

@Embeddable
public class UserT implements TAlongable {

	private String userName1, userLabel, userOther;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{userLabel, userName1, userOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.userLabel = names;
		this.userName1 = values;
		this.userOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return null;
	}
	
	public void setChooseValue2(String names, String values, String others) {
	}
	
	@Transient
	public String[] getHandleValue() {
		return null;
	}
	
	public void setHandleValue(String names, String values) {
	}

	@Column(length=100)
	private String getUserName1() {
		return userName1;
	}

	private void setUserName1(String userName1) {
		this.userName1 = userName1;
	}

	@Column(length=100)
	public String getUserLabel() {
		return userLabel;
	}

	public void setUserLabel(String userLabel) {
		this.userLabel = userLabel;
	}

	public String getUserOther() {
		return userOther;
	}

	public void setUserOther(String userOther) {
		this.userOther = userOther;
	}
}
