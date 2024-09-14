package com.haoyong.sales.sale.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

@Embeddable
public class WBillT implements TAlongable {

	private String wbillLabel, wbillName, wbillOther;
	
	private Date wbillDate;

	@Transient
	public String[] getChooseValue() {
		return new String[]{wbillLabel, wbillName, wbillOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.wbillLabel = names;
		this.wbillName = values;
		this.wbillOther = others;
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
	public String getWbillLabel() {
		return wbillLabel;
	}

	public void setWbillLabel(String wbillLabel) {
		this.wbillLabel = wbillLabel;
	}

	@Column(length=100)
	public String getWbillName() {
		return wbillName;
	}

	public void setWbillName(String wbillName) {
		this.wbillName = wbillName;
	}

	@Temporal(TemporalType.DATE)
	public Date getWbillDate() {
		return wbillDate;
	}

	public void setWbillDate(Date wbillDate) {
		this.wbillDate = wbillDate;
	}

	public String getWbillOther() {
		return wbillOther;
	}

	public void setWbillOther(String wbillOther) {
		this.wbillOther = wbillOther;
	}
	
	@Transient
	protected String getTicket() {
		String name=this.wbillName, other=this.wbillOther;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
}
