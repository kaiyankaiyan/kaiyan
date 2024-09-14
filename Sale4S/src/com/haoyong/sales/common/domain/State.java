package com.haoyong.sales.common.domain;


/**
 * 状态枚举
 */
public class State {
	
	/**
	 * 名称
	 */
	private String name;

	private int id;
	
	private State() {
	}

	private State(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static State New() {
		return new State(10, "草稿");
	}
	
	public static State Save() {
		return new State(20, "保存");
	}
	
	public static State Effect() {
		return new State(30, "生效");
	}
	
	public static State Change() {
		return new State(40, "改单申请");
	}
	
	public static State Adjust() {
		return new State(50, "红冲申请");
	}
	
	public static State Disable() {
		return new State(100, "停用");
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
