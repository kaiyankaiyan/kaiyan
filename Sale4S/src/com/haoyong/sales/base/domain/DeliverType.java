package com.haoyong.sales.base.domain;

/**
 * 发货方式
 */
public class DeliverType implements TInfo {
	
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
