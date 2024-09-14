package com.haoyong.sales.sale.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.sf.mily.support.tools.TicketPropertyUtil;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.common.domain.AbstractDomain;

@Entity
@Table(name="sa_ordercount")
public class OrderCount extends AbstractDomain {

	// 统计日期
	private Date countDate;
	// 统计周期，按天，按月
	private String countMode;
	// 商品
	private Commodity commodity=new Commodity();
	// 统计名称,比如安装师傅
	private String countName;
	
	// 发货数量，赠送数量，回款金额，退货数量，撤货退回数量
	private double sendAmount, presentAmount, paidMoney, returnAmount, rollbackAmount;
	// 备货安装数量
	private double backAmount;
	
	@Temporal(TemporalType.DATE)
	public Date getCountDate() {
		return countDate;
	}
	public void setCountDate(Date countDate) {
		this.countDate = countDate;
	}
	public String getCountName() {
		return countName;
	}
	public void setCountName(String countName) {
		this.countName = countName;
	}
	
	@Column(length=10)
	public String getCountMode() {
		return countMode;
	}
	public void setCountMode(String countMode) {
		this.countMode = countMode;
	}

	@Embedded
	public CommodityT getTCommodity() {
		CommodityT t = new CommodityLogic().getPropertyChoosableLogic().toTrunk(commodity);
		this.setVoparam(t);
		return t;
	}
	private void setTCommodity(CommodityT tcommodity) throws Exception {
		new CommodityLogic().getPropertyChoosableLogic().fromTrunk(commodity, tcommodity);
		if (this.getVoparam(CommodityT.class)==null)
			this.setVoparam(tcommodity);
	}
	@Transient
	public Commodity getCommodity() {
		return this.commodity;
	}
	public void setCommodity(Commodity commodity) {
		TicketPropertyUtil.copyFieldsSkip(commodity, this.commodity);
	}
	
	public double getSendAmount() {
		return sendAmount;
	}
	public void setSendAmount(double sendAmount) {
		this.sendAmount = sendAmount;
	}
	public double getPresentAmount() {
		return presentAmount;
	}
	public void setPresentAmount(double presentAmount) {
		this.presentAmount = presentAmount;
	}
	public double getPaidMoney() {
		return paidMoney;
	}
	public void setPaidMoney(double paidMoney) {
		this.paidMoney = paidMoney;
	}
	public double getReturnAmount() {
		return returnAmount;
	}
	public void setReturnAmount(double returnAmount) {
		this.returnAmount = returnAmount;
	}
	public double getRollbackAmount() {
		return rollbackAmount;
	}
	public void setRollbackAmount(double rollbackAmount) {
		this.rollbackAmount = rollbackAmount;
	}
	public double getBackAmount() {
		return backAmount;
	}
	public void setBackAmount(double backAmount) {
		this.backAmount = backAmount;
	}
}
