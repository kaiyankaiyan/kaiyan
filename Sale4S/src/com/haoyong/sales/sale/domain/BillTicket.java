package com.haoyong.sales.sale.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

public class BillTicket implements PropertyChoosable<BillTicket> {

	// 开票单号
	private String number;
	// 开票单位
	private String billUnit;
	// 票据类型
	private String typeName;
	
	// 开票日期
	private Date billDate;
	// 备注
	private String remark;
//********************************************************** 处理属性
	// 差异金额
	private double diffMoney;
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("KP", SerialNumberFactory.Month, 5).getNextSerial36();
			this.setNumber(serial);
		}
	}
	
	private BillTicket getTicket() {
		return this;
	}
	private BillTicket getHandle() {
		return this;
	}
	
	public List<String> getTrunkDefault() {
		return new ArrayList<String>(0);
	}

	public String getBillUnit() {
		return billUnit;
	}

	public void setBillUnit(String billUnit) {
		this.billUnit = billUnit;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Date getBillDate() {
		return billDate;
	}

	public void setBillDate(Date billDate) {
		this.billDate = billDate;
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
	
	public double getDiffMoney() {
		return this.diffMoney;
	}
	
	public void setDiffMoney(double money) {
		this.diffMoney = money;
	}
}
