package com.haoyong.sales.sale.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

public class ArrangeTicket implements PropertyChoosable<ArrangeTicket> {
	
	private String number;
	/**
	 * 排单发起红冲的处理
	 * 原订单安排取消、转备料
	 */
	private String cancelType;
	// 安排类型
	private String arrangeType;
	// 直发、当地发货方式的备注
	private String deliverNote;
	// 生产日期
	private Date productDate;
	
	private ArrangeTicket getTicket() {
		return this;
	}
	private ArrangeTicket getDetail() {
		return this;
	}
	private ArrangeTicket getHandle() {
		return this;
	}
	
	public List<String> getTrunkDefault() {
		return Arrays.asList(new String[]{"number"});
	}
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("PD", SerialNumberFactory.Month, 5).getNextSerial36();
			this.setNumber(serial);
		}
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getCancelType() {
		return cancelType;
	}

	public void setCancelType(String cancelType) {
		this.cancelType = cancelType;
	}

	public String getArrangeType() {
		return arrangeType;
	}

	public void setArrangeType(String arrangeType) {
		this.arrangeType = arrangeType;
	}

	public String getDeliverNote() {
		return deliverNote;
	}

	public void setDeliverNote(String deliverNote) {
		this.deliverNote = deliverNote;
	}

	public Date getProductDate() {
		return productDate;
	}

	public void setProductDate(Date productDate) {
		this.productDate = productDate;
	}
}
