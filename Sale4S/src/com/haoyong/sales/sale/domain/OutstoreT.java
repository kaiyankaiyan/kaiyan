package com.haoyong.sales.sale.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

public class OutstoreT implements TAlongable {

	private String outstoreLabel, outstoreName, outstoreOther;
	private String outstoreLabel2, outstoreName2, outstoreOther2;
	private String outstoreLabelH, outstoreNameH;
	private Date outstoreDate;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{outstoreLabel, outstoreName, outstoreOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.outstoreLabel = names;
		this.outstoreName = values;
		this.outstoreOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return new String[]{outstoreLabel2, outstoreName2, outstoreOther2};
	}
	
	public void setChooseValue2(String names, String values, String others) {
		this.outstoreLabel2 = names;
		this.outstoreName2 = values;
		this.outstoreOther2 = others;
	}
	
	@Transient
	public String[] getHandleValue() {
		return new String[]{outstoreLabelH, outstoreNameH};
	}
	
	public void setHandleValue(String names, String values) {
		this.outstoreLabelH = names;
		this.outstoreNameH = values;
	}

	@Column(length=100)
	public String getOutstoreLabel() {
		return outstoreLabel;
	}

	public void setOutstoreLabel(String outstoreLabel) {
		this.outstoreLabel = outstoreLabel;
	}

	@Column(length=100)
	public String getOutstoreName() {
		return outstoreName;
	}

	public void setOutstoreName(String outstoreName) {
		this.outstoreName = outstoreName;
	}

	@Temporal(TemporalType.DATE)
	public Date getOutstoreDate() {
		return outstoreDate;
	}

	public void setOutstoreDate(Date outDate) {
		this.outstoreDate = outDate;
	}

	@Column(length=100)
	public String getOutstoreLabelH() {
		return outstoreLabelH;
	}

	public void setOutstoreLabelH(String outstoreLabelH) {
		this.outstoreLabelH = outstoreLabelH;
	}

	@Column(length=100)
	public String getOutstoreNameH() {
		return outstoreNameH;
	}

	public void setOutstoreNameH(String outstoreNameH) {
		this.outstoreNameH = outstoreNameH;
	}

	public String getOutstoreOther() {
		return outstoreOther;
	}

	public void setOutstoreOther(String outstoreOther) {
		this.outstoreOther = outstoreOther;
	}
	
	@Column(length=100)
	public String getOutstoreLabel2() {
		return outstoreLabel2;
	}

	public void setOutstoreLabel2(String outstoreLabel2) {
		this.outstoreLabel2 = outstoreLabel2;
	}

	@Column(length=100)
	public String getOutstoreName2() {
		return outstoreName2;
	}

	public void setOutstoreName2(String outstoreName2) {
		this.outstoreName2 = outstoreName2;
	}

	public String getOutstoreOther2() {
		return outstoreOther2;
	}

	public void setOutstoreOther2(String outstoreOther2) {
		this.outstoreOther2 = outstoreOther2;
	}

	@Transient
	protected String getTicket() {
		String name=this.outstoreName, other=this.outstoreOther;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getHandle() {
		String name=this.outstoreNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}
