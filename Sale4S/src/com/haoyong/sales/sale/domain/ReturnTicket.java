package com.haoyong.sales.sale.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

public class ReturnTicket implements PropertyChoosable<ReturnTicket> {
	
	// 退货单号, 人工编号
	private String number, number2;
	// 退入仓库
	private String inName;
	// 货运单号
	private String deliverNum;
	// 退货备注
	private String remark;

//********************************************************** 处理属性
	// 退货数量
	private double returnAmount;
	// 退货生效日期
	private Date returnDate;
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("TH", SerialNumberFactory.Month, 5).getNextSerial36();
			this.setNumber(serial);
		}
	}
	
	private ReturnTicket getTicket() {
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public double getReturnAmount() {
		return returnAmount;
	}

	public void setReturnAmount(double returnAmount) {
		this.returnAmount = returnAmount;
	}
	
	public Date getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(Date returnDate) {
		this.returnDate = returnDate;
	}

	public String getInName() {
		return inName;
	}

	public void setInName(String instore) {
		this.inName = instore;
	}

	public String getNumber2() {
		return number2;
	}

	public void setNumber2(String number2) {
		this.number2 = number2;
	}
}
