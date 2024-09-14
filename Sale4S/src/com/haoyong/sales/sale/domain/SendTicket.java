package com.haoyong.sales.sale.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

/**
 * 销售出货单
 */
public class SendTicket implements PropertyChoosable<SendTicket> {

	// 发货单号
	private String number;
	// 发运单号
	private String deliverNum;
	// 出货日期
	private Date sendDate;
	// 发货备注
	private String remark;
	// 发货数量
	private double sendAmount;
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("SC", SerialNumberFactory.Month, 5).getNextSerial36();
			this.setNumber(serial);
		}
	}
	
	private SendTicket getTicket() {
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

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public double getSendAmount() {
		return this.sendAmount;
	}
	
	public void setSendAmount(double amount) {
		this.sendAmount = amount;
	}
}
