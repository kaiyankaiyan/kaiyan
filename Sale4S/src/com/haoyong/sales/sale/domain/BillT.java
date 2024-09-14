package com.haoyong.sales.sale.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.sf.mily.types.DateType;

import com.haoyong.sales.common.domain.TAlongable;

@Embeddable
public class BillT implements TAlongable {

	private String billLabel, billName, billOther;
	private String billLabelH, billNameH;
	
	private String typeName;
	private Date billDate;

	@Transient
	public String[] getChooseValue() {
		return new String[]{billLabel, billName, billOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.billLabel = names;
		this.billName = values;
		this.billOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return null;
	}
	
	public void setChooseValue2(String names, String values, String others) {
	}

	@Transient
	public String[] getHandleValue() {
		return new String[]{billLabelH, billNameH, null};
	}
	
	public void setHandleValue(String names, String values) {
		this.billLabelH = names;
		this.billNameH = values;
	}

	@Column(length=100)
	public String getBillLabel() {
		return billLabel;
	}

	public void setBillLabel(String billLabel) {
		this.billLabel = billLabel;
	}

	@Column(length=100)
	public String getBillName() {
		return billName;
	}

	public void setBillName(String billName) {
		this.billName = billName;
	}

	@Column(length=10)
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@Temporal(TemporalType.DATE)
	public Date getBillDate() {
		return billDate;
	}

	public void setBillDate(Date billDate) {
		this.billDate = billDate;
	}

	public String getBillOther() {
		return billOther;
	}

	public void setBillOther(String billOther) {
		this.billOther = billOther;
	}
	
	@Column(length=100)
	public String getBillLabelH() {
		return billLabelH;
	}

	public void setBillLabelH(String billLabelH) {
		this.billLabelH = billLabelH;
	}

	@Column(length=100)
	public String getBillNameH() {
		return billNameH;
	}

	public void setBillNameH(String billNameH) {
		this.billNameH = billNameH;
	}

	@Transient
	protected String getTicket() {
		String name=this.billName, other=this.billOther, date=new DateType().format(this.billDate);
		return new StringBuffer().append(this.typeName).append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).append(date==null? "": date).toString();
	}

	@Transient
	protected String getHandle() {
		String name=this.billNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}
