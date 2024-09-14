package com.haoyong.sales.common.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import net.sf.mily.support.tools.TicketPropertyUtil;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.logic.CommodityLogic;

@MappedSuperclass
public abstract class AbstractCommodityItem extends AbstractDomain {
	// 单价，数量
	private double price=0, amount=0.0;
	// 商品
	private Commodity commodity=new Commodity();
	
	public double getAmount() {
		return this.amount;
	}

	public void setAmount(double amount) {
		this.amount=amount;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
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
	
	protected List<Object> getTVoParamMap(Object key) {
		String k = "TVoParamMap";
		HashMap<Object, List<Object>> map = (HashMap)this.getVoParamMap().get(k);
		if (map == null) {
			map = new HashMap<Object, List<Object>>();
			this.getVoParamMap().put(k, map);
		}
		List<Object> vlist = map.get(key);
		if (vlist == null) {
			vlist = new ArrayList<Object>();
			map.put(key, vlist);
		}
		return vlist;
	}
}
