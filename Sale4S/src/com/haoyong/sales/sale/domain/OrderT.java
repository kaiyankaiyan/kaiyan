package com.haoyong.sales.sale.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.TAlongable;

@Embeddable
public class OrderT implements TAlongable {

	private String orderLabel, orderName, orderOther;
	private String orderLabel2, orderName2, orderOther2;
	private String orderLabelH, orderNameH;
	
	private String number, proNumber, orderType, spnote;
	private Date hopeDate, orderDate;
	
	private double cprice, cmoney;
	
	@Transient
	public String[] getChooseValue() {
		return new String[]{orderLabel, orderName, orderOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.orderLabel = names;
		this.orderName = values;
		this.orderOther = others;
	}
	
	@Transient
	public String[] getChooseValue2() {
		return new String[]{orderLabel2, orderName2, orderOther2};
	}
	
	public void setChooseValue2(String names, String values, String others) {
		this.orderLabel2 = names;
		this.orderName2 = values;
		this.orderOther2 = others;
	}
	
	@Transient
	public String[] getHandleValue() {
		return new String[]{orderLabelH, orderNameH};
	}
	
	public void setHandleValue(String names, String values) {
		this.orderLabelH = names;
		this.orderNameH = values;
	}
	
	@Column(length=100)
	public String getOrderLabel() {
		return orderLabel;
	}

	public void setOrderLabel(String orderLabel) {
		this.orderLabel = orderLabel;
	}

	@Column(length=100)
	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	@Column(length=30)
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@Temporal(TemporalType.DATE)
	public Date getHopeDate() {
		return hopeDate;
	}

	public void setHopeDate(Date hopeDate) {
		this.hopeDate = hopeDate;
	}

	@Temporal(TemporalType.DATE)
	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	@Column(length=100)
	public String getOrderLabel2() {
		return orderLabel2;
	}

	public void setOrderLabel2(String orderLabel2) {
		this.orderLabel2 = orderLabel2;
	}

	@Column(length=100)
	public String getOrderName2() {
		return orderName2;
	}

	public void setOrderName2(String orderName2) {
		this.orderName2 = orderName2;
	}

	@Column(length=100)
	public String getOrderLabelH() {
		return orderLabelH;
	}

	public void setOrderLabelH(String orderLabelH) {
		this.orderLabelH = orderLabelH;
	}

	@Column(length=100)
	public String getOrderNameH() {
		return orderNameH;
	}

	public void setOrderNameH(String orderNameH) {
		this.orderNameH = orderNameH;
	}

	public String getOrderOther() {
		return orderOther;
	}

	public void setOrderOther(String orderOther) {
		this.orderOther = orderOther;
	}

	public String getOrderOther2() {
		return orderOther2;
	}

	public void setOrderOther2(String orderOther2) {
		this.orderOther2 = orderOther2;
	}
	
	@Column(length=30)
	public String getProNumber() {
		return proNumber;
	}

	public void setProNumber(String proNumber) {
		this.proNumber = proNumber;
	}

	@Column(length=20)
	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public double getCprice() {
		return cprice;
	}

	public void setCprice(double cprice) {
		this.cprice = cprice;
	}

	public double getCmoney() {
		return cmoney;
	}

	public void setCmoney(double cmoney) {
		this.cmoney = cmoney;
	}

	@Column(columnDefinition="TEXT")
	public String getSpnote() {
		return spnote;
	}

	public void setSpnote(String spnote) {
		this.spnote = spnote;
	}

	@Transient
	public String getTicket() {
		String name=this.orderName, other=this.orderOther;
		return new StringBuffer().append(number).append(name==null? ",": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getDetail() {
		String name=this.orderName2, other=this.orderOther2;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getHandle() {
		String name=this.orderNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}
