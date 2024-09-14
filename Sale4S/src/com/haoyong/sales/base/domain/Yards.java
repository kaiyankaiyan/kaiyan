package com.haoyong.sales.base.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

/**
 * 一种商品大类的码谱
 */
public class Yards implements TInfo {
	
	private int id;
	/**
	 * 商品大类
	 */
	private String commType;

	/**
	 * 交叉统计列名
	 */
	private String c01,c02,c03,c04,c05,c06,c07,c08;
	
	@Transient
	public List<String> getColList() {
		List<String> list = new ArrayList<String>();
		list.add(c01);
		list.add(c02);
		list.add(c03);
		list.add(c04);
		list.add(c05);
		list.add(c06);
		list.add(c07);
		list.add(c08);
		return list;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCommType() {
		return commType;
	}

	public void setCommType(String ctype) {
		this.commType = ctype;
	}

	public String getC01() {
		return c01;
	}

	public void setC01(String c01) {
		this.c01 = c01;
	}

	public String getC02() {
		return c02;
	}

	public void setC02(String c02) {
		this.c02 = c02;
	}

	public String getC03() {
		return c03;
	}

	public void setC03(String c03) {
		this.c03 = c03;
	}

	public String getC04() {
		return c04;
	}

	public void setC04(String c04) {
		this.c04 = c04;
	}

	public String getC05() {
		return c05;
	}

	public void setC05(String c05) {
		this.c05 = c05;
	}

	public String getC06() {
		return c06;
	}

	public void setC06(String c06) {
		this.c06 = c06;
	}

	public String getC07() {
		return c07;
	}

	public void setC07(String c07) {
		this.c07 = c07;
	}

	public String getC08() {
		return c08;
	}

	public void setC08(String c08) {
		this.c08 = c08;
	}
}
