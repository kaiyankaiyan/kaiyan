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
public class ReceiptT implements TAlongable {

	private String receiptLabel, receiptName, receiptOther;
	private String receiptLabel2, receiptName2, receiptOther2;
	private String receiptLabelH, receiptNameH;

	private double storePrice, storeMoney;
	
	private Date receiptDate;

	@Transient
	public String[] getChooseValue() {
		return new String[]{receiptLabel, receiptName, receiptOther};
	}
	
	public void setChooseValue(String names, String values, String others) {
		this.receiptLabel = names;
		this.receiptName = values;
		this.receiptOther = others;
	}

	@Transient
	public String[] getChooseValue2() {
		return new String[]{receiptLabel2, receiptName2, receiptOther2};
	}
	
	public void setChooseValue2(String names, String values, String others) {
		this.receiptLabel2 = names;
		this.receiptName2 = values;
		this.receiptOther2 = others;
	}

	@Transient
	public String[] getHandleValue() {
		return new String[]{receiptLabelH, receiptNameH};
	}
	
	public void setHandleValue(String names, String values) {
		this.receiptLabelH = names;
		this.receiptNameH = values;
	}

	@Column(length=100)
	public String getReceiptLabel() {
		return receiptLabel;
	}

	public void setReceiptLabel(String receiptLabel) {
		this.receiptLabel = receiptLabel;
	}

	@Column(length=100)
	public String getReceiptName() {
		return receiptName;
	}

	public void setReceiptName(String receiptName) {
		this.receiptName = receiptName;
	}

	@Column(length=100)
	public String getReceiptLabel2() {
		return receiptLabel2;
	}

	public void setReceiptLabel2(String receiptLabel2) {
		this.receiptLabel2 = receiptLabel2;
	}

	@Column(length=100)
	public String getReceiptName2() {
		return receiptName2;
	}

	public void setReceiptName2(String receiptName2) {
		this.receiptName2 = receiptName2;
	}

	public double getStorePrice() {
		return storePrice;
	}

	public void setStorePrice(double storePrice) {
		this.storePrice = storePrice;
	}

	public double getStoreMoney() {
		return storeMoney;
	}

	public void setStoreMoney(double storeMoney) {
		this.storeMoney = storeMoney;
	}

	@Temporal(TemporalType.DATE)
	public Date getReceiptDate() {
		return receiptDate;
	}

	public void setReceiptDate(Date receiptDate) {
		this.receiptDate = receiptDate;
	}

	@Column(length=100)
	public String getReceiptLabelH() {
		return receiptLabelH;
	}

	public void setReceiptLabelH(String receiptLabelH) {
		this.receiptLabelH = receiptLabelH;
	}

	@Column(length=100)
	public String getReceiptNameH() {
		return receiptNameH;
	}

	public void setReceiptNameH(String receiptNameH) {
		this.receiptNameH = receiptNameH;
	}

	public String getReceiptOther() {
		return receiptOther;
	}

	public void setReceiptOther(String receiptOther) {
		this.receiptOther = receiptOther;
	}

	public String getReceiptOther2() {
		return receiptOther2;
	}

	public void setReceiptOther2(String receiptOther2) {
		this.receiptOther2 = receiptOther2;
	}
	
	@Transient
	protected String getTicket() {
		String name=this.receiptName, other=this.receiptOther, date=new DateType().format(this.receiptDate);
		return new StringBuffer().append(name==null? "": name).append(date==null? "": date).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getDetail() {
		String name=this.receiptName2, other=this.receiptOther2;
		return new StringBuffer().append(name==null? "": name).append(other==null? "": other.substring(other.indexOf(":\"")+2, other.length()-2)).toString();
	}
	@Transient
	protected String getHandle() {
		String name=this.receiptNameH;
		return new StringBuffer().append(name==null? "": name).toString();
	}
}
