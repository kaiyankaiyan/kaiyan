package com.haoyong.sales.sale.domain;

import com.haoyong.sales.base.domain.TInfo;

public class OrderType implements TInfo {
	
	private int id;

	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
