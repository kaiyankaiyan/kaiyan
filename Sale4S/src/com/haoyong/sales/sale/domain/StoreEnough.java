package com.haoyong.sales.sale.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.haoyong.sales.common.domain.AbstractCommodityItem;

@Entity
@Table(name = "sa_storeenough")
public class StoreEnough extends AbstractCommodityItem {
	
	// 入库库存分账
	private String instore;
	// amount 为理论库存数，够用数
	/**
	 * 未发货订单数量
	 */
	private double orderAmount;
	
	/**
	 * 订单请购数量
	 */
	private double requestAmount;
	
	/**
	 * 采购在途数量
	 */
	private double onroadAmount;
	
	/**
	 * 库存数量
	 */
	private double storeAmount;
	
	/**
	 * 占用库存锁定数量，可用数量
	 */
	private double lockAmount, freeAmount;

	@Column(length=100)
	public String getInstore() {
		return instore;
	}

	public void setInstore(String instore) {
		this.instore = instore;
	}

	public double getOrderAmount() {
		return orderAmount;
	}

	public void setOrderAmount(double orderAmount) {
		this.orderAmount = orderAmount;
	}

	public double getRequestAmount() {
		return requestAmount;
	}

	public void setRequestAmount(double requestAmount) {
		this.requestAmount = requestAmount;
	}

	public double getOnroadAmount() {
		return onroadAmount;
	}

	public void setOnroadAmount(double onroadAmount) {
		this.onroadAmount = onroadAmount;
	}

	public double getStoreAmount() {
		return storeAmount;
	}

	public void setStoreAmount(double storeAmount) {
		this.storeAmount = storeAmount;
	}

	public double getLockAmount() {
		return lockAmount;
	}

	public void setLockAmount(double lockAmount) {
		this.lockAmount = lockAmount;
	}

	public double getFreeAmount() {
		return freeAmount;
	}

	public void setFreeAmount(double freeAmount) {
		this.freeAmount = freeAmount;
	}
}
