package com.haoyong.sales.base.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

/**
 * 单据客户
 */
@Embeddable
public class ClientT implements TAlongable {

	private String clientLabel, clientName, clientOther;
	private String clientLabelH, clientNameH;
	
	private long fromSellerId;
	private String uaccept;

	@Transient
	public String[] getChooseValue() {
		return new String[]{clientLabel, clientName, clientOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.clientLabel = names;
		this.clientName = values;
		this.clientOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return null;
	}
	
	public void setChooseValue2(String names, String values, String others) {
	}
	
	@Transient
	public String[] getHandleValue() {
		return new String[]{clientLabelH, clientNameH};
	}
	
	public void setHandleValue(String names, String values) {
		this.clientLabelH = names;
		this.clientNameH = values;
	}
	
	@Column(length=100)
	public String getClientName() {
		return clientName;
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	@Column(length=100)
	public String getClientLabel() {
		return clientLabel;
	}

	public void setClientLabel(String clientLabel) {
		this.clientLabel = clientLabel;
	}

	public String getClientOther() {
		return clientOther;
	}

	public void setClientOther(String clientOther) {
		this.clientOther = clientOther;
	}
	
	@Column(length=100)
	public String getClientLabelH() {
		return clientLabelH;
	}

	public void setClientLabelH(String clientLabelH) {
		this.clientLabelH = clientLabelH;
	}

	@Column(length=100)
	public String getClientNameH() {
		return clientNameH;
	}

	public void setClientNameH(String clientNameH) {
		this.clientNameH = clientNameH;
	}

	public long getFromSellerId() {
		return fromSellerId;
	}

	public void setFromSellerId(long fromSellerId) {
		this.fromSellerId = fromSellerId;
	}

	@Column(length=100)
	public String getUaccept() {
		return uaccept;
	}

	public void setUaccept(String uaccept) {
		this.uaccept = uaccept;
	}

	@Transient
	public String getTrunk() {
		String name=this.clientName, other=this.clientOther;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	private String getHandle() {
		String name=this.clientNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}
