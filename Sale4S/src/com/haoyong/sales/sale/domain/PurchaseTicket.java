package com.haoyong.sales.sale.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

public class PurchaseTicket implements PropertyChoosable<PurchaseTicket> {
	
	// 采购单号，人工编号
	private String number, number2;
	// 采购人、生产组、车间
	private String agent;
	// 采购、生产日期
	private Date purDate;
	// 预计到货日期
	private Date hopeDate;
	// 采购取消数量
	private double cancelAmount;
	// 采购转备料数量
	private double backupAmount;
	// 重排单数量
	private double rearrangeAmount;
	// 多收货数量
	private double overAmount;
	// 生产原物料齐料状态
	private String Material;
	private String remark;
	
//********************************************************** 明细属性
	// 税率, 不含税价格
	private double taxRate, untaxPrice;
	// 合价=price*amount
	private double pmoney;
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("CG", SerialNumberFactory.Month, 5).getNextSerial36();
			this.setNumber(serial);
		}
	}
	
	private PurchaseTicket getTicket() {
		return this;
	}
	private PurchaseTicket getDetail() {
		return this;
	}
	private PurchaseTicket getHandle() {
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

	public String getNumber2() {
		return number2;
	}
	public void setNumber2(String number2) {
		this.number2 = number2;
	}

	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}

	public Date getPurDate() {
		return purDate;
	}
	public void setPurDate(Date purDate) {
		this.purDate = purDate;
	}

	public Date getHopeDate() {
		return hopeDate;
	}
	public void setHopeDate(Date wreachDate) {
		this.hopeDate = wreachDate;
	}

	public double getCancelAmount() {
		return cancelAmount;
	}
	public void setCancelAmount(double cancelAmount) {
		this.cancelAmount = cancelAmount;
	}

	public double getBackupAmount() {
		return backupAmount;
	}
	public void setBackupAmount(double backupAmount) {
		this.backupAmount = backupAmount;
	}

	public double getRearrangeAmount() {
		return rearrangeAmount;
	}
	public void setRearrangeAmount(double rearrangeAmount) {
		this.rearrangeAmount = rearrangeAmount;
	}

	public double getOverAmount() {
		return overAmount;
	}
	public void setOverAmount(double overAmount) {
		this.overAmount = overAmount;
	}

	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public double getTaxRate() {
		return taxRate;
	}
	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}

	public double getUntaxPrice() {
		return untaxPrice;
	}
	public void setUntaxPrice(double untaxPrice) {
		this.untaxPrice = untaxPrice;
	}

	public double getPmoney() {
		return pmoney;
	}
	public void setPmoney(double pmoney) {
		this.pmoney = pmoney;
	}

	public String getMaterial() {
		return Material;
	}

	public void setMaterial(String material) {
		Material = material;
	}
}
