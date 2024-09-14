package com.haoyong.sales.base.domain;

import javax.persistence.Column;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

public class StorehouseT implements TAlongable {
	
	private String houseLabel, houseName, houseOther;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{houseLabel, houseName, houseOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.houseLabel = names;
		this.houseName = values;
		this.houseOther = others;
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

	public String getHouseLabel() {
		return houseLabel;
	}

	public void setHouseLabel(String houseLabel) {
		this.houseLabel = houseLabel;
	}

	@Column(length=100)
	public String getHouseName() {
		return houseName;
	}

	public void setHouseName(String houseName) {
		this.houseName = houseName;
	}

	public String getHouseOther() {
		return houseOther;
	}

	public void setHouseOther(String houseOther) {
		this.houseOther = houseOther;
	}
	
	@Transient
	public String getTrunk() {
		String name=this.houseName, other=this.houseOther;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
}
