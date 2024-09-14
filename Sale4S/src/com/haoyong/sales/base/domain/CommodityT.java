package com.haoyong.sales.base.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;


@Embeddable
public class CommodityT implements TAlongable {
	
	private String commName, commLabel, commOther;

	private	String commNumber;
	private String supplyType;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{commLabel, commName, commOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.commLabel = names;
		this.commName = values;
		this.commOther = others;
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

	@Column(length=20)
	public String getCommNumber() {
		return this.commNumber;
	}

	public void setCommNumber(String commodityNumber) {
		this.commNumber = commodityNumber;
	}

	@Column(length=100)
	public String getCommName() {
		return commName;
	}

	public void setCommName(String commodityName) {
		this.commName = commodityName;
	}

	@Column(length=100)
	public String getCommLabel() {
		return commLabel;
	}

	public void setCommLabel(String commodityLabel) {
		this.commLabel = commodityLabel;
	}

	@Column(length=10)
	public String getSupplyType() {
		return supplyType;
	}

	public void setSupplyType(String supplyType) {
		this.supplyType = supplyType;
	}

	public String getCommOther() {
		return commOther;
	}

	public void setCommOther(String commOther) {
		this.commOther = commOther;
	}
	
	@Transient
	public String getTrunk() {
		String name=this.commName, other=this.commOther;
		return new StringBuffer().append(this.commNumber).append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
}
