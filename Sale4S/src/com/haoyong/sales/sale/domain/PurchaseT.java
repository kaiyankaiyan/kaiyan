package com.haoyong.sales.sale.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

@Embeddable
public class PurchaseT implements TAlongable {

	private String purLabel, purName, purOther;
	private String purLabel2, purName2, purOther2;
	private String purLabelH, purNameH;
	
	// 采购人、生产组、车间
	private String agent;
	private Date purDate;
	private double pmoney=0;

	@Transient
	public String[] getChooseValue() {
		return new String[]{purLabel, purName, purOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.purLabel = names;
		this.purName = values;
		this.purOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return new String[]{purLabel2, purName2, purOther2};
	}
	
	public void setChooseValue2(String names, String values, String others) {
		this.purLabel2 = names;
		this.purName2 = values;
		this.purOther2 = others;
	}

	@Transient
	public String[] getHandleValue() {
		return new String[]{purLabelH, purNameH};
	}
	
	public void setHandleValue(String names, String values) {
		this.purLabelH = names;
		this.purNameH = values;
	}
	
	@Column(length=100)
	public String getPurLabel() {
		return purLabel;
	}

	public String getPurName() {
		return purName;
	}

	@Column(length=100)
	public void setPurName(String purName) {
		this.purName = purName;
	}

	public void setPurLabel(String purLabel) {
		this.purLabel = purLabel;
	}

	@Column(length=100)
	public String getPurLabel2() {
		return purLabel2;
	}

	public void setPurLabel2(String purLabel2) {
		this.purLabel2 = purLabel2;
	}

	@Column(length=100)
	public String getPurName2() {
		return purName2;
	}

	public void setPurName2(String purName2) {
		this.purName2 = purName2;
	}

	@Column(length=100)
	public String getPurLabelH() {
		return purLabelH;
	}

	public void setPurLabelH(String purLabelH) {
		this.purLabelH = purLabelH;
	}

	@Column(length=100)
	public String getPurNameH() {
		return purNameH;
	}

	public void setPurNameH(String purNameH) {
		this.purNameH = purNameH;
	}

	public String getPurOther() {
		return purOther;
	}

	public void setPurOther(String purOther) {
		this.purOther = purOther;
	}

	public String getPurOther2() {
		return purOther2;
	}

	public void setPurOther2(String purOther2) {
		this.purOther2 = purOther2;
	}

	@Temporal(TemporalType.DATE)
	public Date getPurDate() {
		return purDate;
	}

	public void setPurDate(Date purDate) {
		this.purDate = purDate;
	}

	public double getPmoney() {
		return pmoney;
	}

	public void setPmoney(double pmoney) {
		this.pmoney = pmoney;
	}

	@Column(length=100)
	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	@Transient
	public String getTicket() {
		String name=this.purName, other=this.purOther;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getDetail() {
		String name=this.purName2, other=this.purOther2;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getHandle() {
		String name=this.purNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}
