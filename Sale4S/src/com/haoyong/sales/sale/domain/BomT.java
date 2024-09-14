package com.haoyong.sales.sale.domain;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

@Embeddable
public class BomT implements TAlongable {

	private String bomLabel, bomName, bomOther;
	
	private double gotAmount, notAmount, occupyAmount, keepAmount, instore, outstore;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{bomLabel, bomName, bomOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.bomLabel = names;
		this.bomName = values;
		this.bomOther = others;
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
	
	public String getBomLabel() {
		return bomLabel;
	}

	public void setBomLabel(String bomLabel) {
		this.bomLabel = bomLabel;
	}

	public String getBomName() {
		return bomName;
	}

	public void setBomName(String bomName) {
		this.bomName = bomName;
	}

	public String getBomOther() {
		return bomOther;
	}

	public void setBomOther(String bomOther) {
		this.bomOther = bomOther;
	}

	public double getGotAmount() {
		return gotAmount;
	}

	public void setGotAmount(double gotAmount) {
		this.gotAmount = gotAmount;
	}

	public double getNotAmount() {
		return notAmount;
	}

	public void setNotAmount(double notAmount) {
		this.notAmount = notAmount;
	}

	public double getOccupyAmount() {
		return occupyAmount;
	}

	public void setOccupyAmount(double occupyAmount) {
		this.occupyAmount = occupyAmount;
	}

	public double getKeepAmount() {
		return keepAmount;
	}

	public void setKeepAmount(double keepAmount) {
		this.keepAmount = keepAmount;
	}

	public double getInstore() {
		return instore;
	}

	public void setInstore(double instore) {
		this.instore = instore;
	}

	public double getOutstore() {
		return outstore;
	}

	public void setOutstore(double outstore) {
		this.outstore = outstore;
	}

	@Transient
	protected String getTicket() {
		String name=this.bomName, other=this.bomOther;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
}
