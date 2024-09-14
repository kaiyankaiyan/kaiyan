package com.haoyong.sales.sale.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

public class OutstoreTicket implements PropertyChoosable<OutstoreTicket> {
	
	private String number, number2;
	// 出库类型
	private String type;
	// 出库库存分账，出库状态
	private String outName, status;
	// 出库时间
	private Date outstoreDate;
	// 出库部门
	private String dept;
	private String remark;
//=============================================== 明细
	
	// 上月表读数，当月电表数
	private double fromAmount, toAmount;
	// 下月延续使用
	private boolean continu;

	private OutstoreTicket getTicket() {
		return this;
	}
	private OutstoreTicket getDetail() {
		return this;
	}
	private OutstoreTicket getHandle() {
		return this;
	}
	
	public List<String> getTrunkDefault() {
		return new ArrayList<String>(0);
	}
	
	public String genSerialNumber() {
		if (this.number == null) {
			String serial = new SerialNumberFactory().serialLen2("CK", SerialNumberFactory.Date, 5).getNextSerial();
			this.number = serial;
		}
		return this.number;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOutName() {
		return outName;
	}
	public void setOutName(String outstore) {
		this.outName = outstore;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getOutstoreDate() {
		return outstoreDate;
	}

	public void setOutstoreDate(Date outDate) {
		this.outstoreDate = outDate;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public boolean isContinu() {
		return continu;
	}

	public void setContinu(boolean continu) {
		this.continu = continu;
	}

	public double getFromAmount() {
		return fromAmount;
	}

	public void setFromAmount(double fromAmount) {
		this.fromAmount = fromAmount;
	}

	public double getToAmount() {
		return toAmount;
	}

	public void setToAmount(double toAmount) {
		this.toAmount = toAmount;
	}
	public String getNumber2() {
		return number2;
	}
	public void setNumber2(String number2) {
		this.number2 = number2;
	}
}
