package com.haoyong.sales.sale.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

public class InstoreTicket implements PropertyChoosable<InstoreTicket> {
	
	private String number, number2;
	// 入库库存分账
	private String inName;
	// 入库类型
	private String type;
	// 入库时间
	private Date instoreDate;
	// 还料部门
	private String dept;
	private String remark;
//=============================================== 明细
	
	// 上月表读数，当月电表数
	private double fromAmount, toAmount;
	// 下月延续使用
	private boolean continu;
	
	
	/**
	 * 入库数量，出库数量，新台账数
	 */
	private double inAmount, outAmount, amount;
	
	private InstoreTicket getTicket() {
		return this;
	}
	private InstoreTicket getDetail() {
		return this;
	}
	private InstoreTicket getHandle() {
		return this;
	}
	
	public List<String> getTrunkDefault() {
		return new ArrayList<String>(0);
	}
	
	public String genSerialNumber() {
		if (this.number == null) {
			String serial = new SerialNumberFactory().serialLen2("RK", SerialNumberFactory.Date, 5).getNextSerial();
			this.number = serial;
		}
		return this.getNumber();
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getInName() {
		return inName;
	}
	public void setInName(String instore) {
		this.inName = instore;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getInstoreDate() {
		return instoreDate;
	}

	public void setInstoreDate(Date inDate) {
		this.instoreDate = inDate;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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

	public boolean isContinu() {
		return continu;
	}

	public void setContinu(boolean continu) {
		this.continu = continu;
	}

	public double getInAmount() {
		return inAmount;
	}

	public void setInAmount(double inAmount) {
		this.inAmount = inAmount;
	}

	public double getOutAmount() {
		return outAmount;
	}

	public void setOutAmount(double outAmount) {
		this.outAmount = outAmount;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}
	public String getNumber2() {
		return number2;
	}
	public void setNumber2(String number2) {
		this.number2 = number2;
	}
}
