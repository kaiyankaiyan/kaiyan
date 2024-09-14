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
public class SendT implements TAlongable {

	private String sendLabel, sendName, sendOther;
	private Date sendDate;

	@Transient
	public String[] getChooseValue() {
		return new String[]{sendLabel, sendName, sendOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.sendLabel = names;
		this.sendName = values;
		this.sendOther = others;
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
	public String getSendLabel() {
		return sendLabel;
	}

	public void setSendLabel(String receiptLabel) {
		this.sendLabel = receiptLabel;
	}

	@Column(length=100)
	public String getSendName() {
		return sendName;
	}

	public void setSendName(String receiptName) {
		this.sendName = receiptName;
	}

	@Temporal(TemporalType.DATE)
	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public String getSendOther() {
		return sendOther;
	}

	public void setSendOther(String sendOther) {
		this.sendOther = sendOther;
	}

	@Transient
	public String getTicket() {
		String name=this.sendName, other=this.sendOther, date=new DateType().format(this.sendDate);
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).append(date==null? "": date).toString();
	}
}
