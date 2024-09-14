package com.haoyong.sales.sale.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

public class WBillTicket implements PropertyChoosable<WBillTicket> {

	/**
	 * 开票单号
	 */
	private String number;
	
	/**
	 * 票据类型
	 */
	private String typeName;
	
	/**
	 * 开票单位
	 */
	private String billUnit;
	
	/**
	 * 开票日期
	 */
	private Date wbillDate;
	
	/**
	 * 到款金额，待付金额
	 */
	private double reachMoney, waitMoney;
	
	/**
	 * 备注
	 */
	private String remark;
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("DZ", SerialNumberFactory.Month, 5).getNextSerial36();
			this.setNumber(serial);
		}
	}
	
	private WBillTicket getTicket() {
		return this;
	}
	
	public List<String> getTrunkDefault() {
		return new ArrayList<String>(0);
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getBillUnit() {
		return billUnit;
	}

	public void setBillUnit(String billUnit) {
		this.billUnit = billUnit;
	}

	public Date getWbillDate() {
		return wbillDate;
	}

	public void setWbillDate(Date wbillDate) {
		this.wbillDate = wbillDate;
	}

	public double getReachMoney() {
		return reachMoney;
	}

	public void setReachMoney(double reachMoney) {
		this.reachMoney = reachMoney;
	}

	public double getWaitMoney() {
		return waitMoney;
	}

	public void setWaitMoney(double waitMoney) {
		this.waitMoney = waitMoney;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
