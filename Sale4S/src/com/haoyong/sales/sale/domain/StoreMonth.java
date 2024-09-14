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
@Table(name = "sa_storemonth")
public class StoreMonth extends AbstractDomain {

	private String instore;
	private Commodity commodity=new Commodity();
	private Date monthi;
	
	private double startAmount, startMoney, startPrice; // 月初库存数
	private double inAmount, inMoney, inPrice; // 月内入库数量
	private double outAmount, outMoney, outPrice; // 月内出库数量
	private double endAmount, endMoney, endPrice; // 月终库存数

	@Transient
	public Commodity getCommodity() {
		return commodity;
	}
	public void setCommodity(Commodity commodity) {
		new CommodityLogic().getPropertyChoosableLogic().fromTrunk(this.commodity, commodity);
	}

	@Embedded
	private CommodityT getTCommodity() {
		CommodityT t = new CommodityLogic().getPropertyChoosableLogic().toTrunk(commodity);
		this.setVoparam(t);
		return t;
	}

	private void setTCommodity(CommodityT tcommodity) throws Exception {
		new CommodityLogic().getPropertyChoosableLogic().fromTrunk(commodity, tcommodity);
		this.setVoparam(tcommodity);
		Commodity comm = TicketPropertyUtil.copyProperties(this.commodity, new Commodity());
		if (this.getVoparam(CommodityT.class)==null)
			this.setVoparam(comm);
	}
	
	@Column(length=100)
	public String getInstore() {
		return instore;
	}
	public void setInstore(String instore) {
		this.instore = instore;
	}
	
	@Temporal(TemporalType.DATE)
	public Date getMonthi() {
		return monthi;
	}
	public void setMonthi(Date monthi) {
		this.monthi = monthi;
	}
	public double getStartAmount() {
		return startAmount;
	}
	public void setStartAmount(double startAmount) {
		this.startAmount = startAmount;
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
	
	public double getEndAmount() {
		return endAmount;
	}
	public void setEndAmount(double endAmount) {
		this.endAmount = endAmount;
	}
	
	public double getStartMoney() {
		return startMoney;
	}
	public void setStartMoney(double startMoney) {
		this.startMoney = startMoney;
	}
	
	public double getInMoney() {
		return inMoney;
	}
	public void setInMoney(double inMoney) {
		this.inMoney = inMoney;
	}
	
	public double getOutMoney() {
		return outMoney;
	}
	public void setOutMoney(double outMoney) {
		this.outMoney = outMoney;
	}
	
	public double getEndMoney() {
		return endMoney;
	}
	public void setEndMoney(double endMoney) {
		this.endMoney = endMoney;
	}
	
	public double getStartPrice() {
		return startPrice;
	}
	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
	}
	
	public double getInPrice() {
		return inPrice;
	}
	public void setInPrice(double inPrice) {
		this.inPrice = inPrice;
	}
	
	public double getOutPrice() {
		return outPrice;
	}
	public void setOutPrice(double outPrice) {
		this.outPrice = outPrice;
	}
	
	public double getEndPrice() {
		return endPrice;
	}
	public void setEndPrice(double endPrice) {
		this.endPrice = endPrice;
	}
}
