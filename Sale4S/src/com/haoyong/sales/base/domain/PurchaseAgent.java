package com.haoyong.sales.base.domain;

/**
 * 采购|生产的部门、或人员
 */
public class PurchaseAgent implements TInfo {
	
	private int id;
	
	private String supply;

	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSupply() {
		return supply;
	}

	public void setSupply(String supply) {
		this.supply = supply;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
