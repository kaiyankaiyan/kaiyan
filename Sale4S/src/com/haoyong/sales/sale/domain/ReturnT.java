package com.haoyong.sales.sale.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.sf.mily.types.DateType;
import net.sf.mily.types.DoubleType;

import com.haoyong.sales.common.domain.TAlongable;

@Embeddable
public class ReturnT implements TAlongable {

	private String returnLabel, returnName, returnOther;
	private String returnLabelH, returnNameH;
	
	private double returnAmount;
	private Date returnDate;

	@Transient
	public String[] getChooseValue() {
		return new String[]{returnLabel, returnName, returnOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.returnLabel = names;
		this.returnName = values;
		this.returnOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return null;
	}
	
	public void setChooseValue2(String names, String values, String others) {
	}
	
	@Transient
	public String[] getHandleValue() {
		return new String[]{returnLabelH, returnNameH};
	}
	
	public void setHandleValue(String names, String values) {
		this.returnLabel = names;
		this.returnName = values;
	}

	@Column(length=100)
	public String getReturnLabel() {
		return returnLabel;
	}

	public void setReturnLabel(String rtnLabel) {
		this.returnLabel = rtnLabel;
	}

	public String getReturnName() {
		return returnName;
	}

	public void setReturnName(String rtnName) {
		this.returnName = rtnName;
	}

	public String getReturnOther() {
		return returnOther;
	}

	public void setReturnOther(String returnOther) {
		this.returnOther = returnOther;
	}

	@Column(length=100)
	public String getReturnLabelH() {
		return returnLabelH;
	}

	public void setReturnLabelH(String returnLabelH) {
		this.returnLabelH = returnLabelH;
	}

	@Column(length=100)
	public String getReturnNameH() {
		return returnNameH;
	}

	public void setReturnNameH(String returnNameH) {
		this.returnNameH = returnNameH;
	}

	public double getReturnAmount() {
		return returnAmount;
	}

	public void setReturnAmount(double returnAmount) {
		this.returnAmount = returnAmount;
	}

	@Temporal(TemporalType.DATE)
	public Date getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(Date returnDate) {
		this.returnDate = returnDate;
	}

	@Transient
	public String getTicket() {
		String name=this.returnName, other=this.returnOther, amount=new DoubleType().format(this.getReturnAmount()), date=new DateType().format(this.returnDate);
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).append(amount).append(" ").append(date==null? "": date).toString();
	}
	@Transient
	protected String getHandle() {
		String name=this.returnNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}
