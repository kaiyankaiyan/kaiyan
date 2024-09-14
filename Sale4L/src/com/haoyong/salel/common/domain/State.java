package com.haoyong.salel.common.domain;

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
	
	public static State valueOf(int stateId) {
		switch(stateId) {
		case 10:
			return New();
		case 20:
			return Save();
		case 30:
			return Effect();
		}
		return null;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
