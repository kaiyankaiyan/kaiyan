package com.haoyong.sales.sale.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

public class ReceiptTicket implements PropertyChoosable<ReceiptTicket> {

	// 收货单号
	private String number;
	// 货运单号
	private String deliverNum;
	// 收货时间
	private Date receiptDate;
	// 收货备注
	private String remark;
	
//********************************************************** 处理属性
	// 收货数量
	private double receiptAmount;
	// 次品数量
	private double badAmount;
	// 一条明细数量的合重量
	private double weight;
	// 库存价格、库存金额
	private double storePrice, storeMoney;
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("SH", SerialNumberFactory.Month, 5).getNextSerial36();
			this.setNumber(serial);
		}
	}
	
	private ReceiptTicket getTicket() {
		return this;
	}
	private ReceiptTicket getDetail() {
		return this;
	}
	private ReceiptTicket getHandle() {
		return this;
	}
	
	public List<String> getTrunkDefault() {
		return Arrays.asList(new String[]{"number"});
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getDeliverNum() {
		return deliverNum;
	}

	public void setDeliverNum(String deliverNum) {
		this.deliverNum = deliverNum;
	}

	public double getReceiptAmount() {
		return receiptAmount;
	}

	public void setReceiptAmount(double receiptAmount) {
		this.receiptAmount = receiptAmount;
	}

	public double getBadAmount() {
		return badAmount;
	}

	public void setBadAmount(double badAmount) {
		this.badAmount = badAmount;
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

	public Date getReceiptDate() {
		return receiptDate;
	}

	public void setReceiptDate(Date receiptDate) {
		this.receiptDate = receiptDate;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
}
