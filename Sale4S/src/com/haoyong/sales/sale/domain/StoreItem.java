package com.haoyong.sales.sale.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.haoyong.sales.common.domain.AbstractCommodityItem;

@Entity
@Table(name = "sa_storeitem")
public class StoreItem extends AbstractCommodityItem {
	// 库存分账
	private String instore;
	// 交叉统计列名
	private String c01,c02,c03,c04,c05,c06,c07,c08,c09,c10,c11,c12;
	// 交叉统计数量
	private double a01,a02,a03,a04,a05,a06,a07,a08,a09,a10,a11,a12;
	// 来源采购明细月流水号，以，分隔
	private String monthnums;
	// amount为库存数
	// 备货数
	private double backAmount;
	private double money;

	@Column(length=100)
	public String getInstore() {
		return instore;
	}

	public void setInstore(String instore) {
		this.instore = instore;
	}

	@Column(length=50)
	private String getC01() {
		return c01;
	}
	private void setC01(String c01) {
		this.c01 = c01;
	}
	@Column(length=50)
	private String getC02() {
		return c02;
	}
	private void setC02(String c02) {
		this.c02 = c02;
	}
	@Column(length=50)
	private String getC03() {
		return c03;
	}
	private void setC03(String c03) {
		this.c03 = c03;
	}
	@Column(length=50)
	private String getC04() {
		return c04;
	}
	private void setC04(String c04) {
		this.c04 = c04;
	}
	@Column(length=50)
	private String getC05() {
		return c05;
	}
	private void setC05(String c05) {
		this.c05 = c05;
	}
	@Column(length=50)
	private String getC06() {
		return c06;
	}
	private void setC06(String c06) {
		this.c06 = c06;
	}
	@Column(length=50)
	private String getC07() {
		return c07;
	}
	private void setC07(String c07) {
		this.c07 = c07;
	}
	@Column(length=50)
	private String getC08() {
		return c08;
	}
	private void setC08(String c08) {
		this.c08 = c08;
	}
	@Column(length=50)
	private String getC09() {
		return c09;
	}
	private void setC09(String c09) {
		this.c09 = c09;
	}
	@Column(length=50)
	private String getC10() {
		return c10;
	}
	private void setC10(String c10) {
		this.c10 = c10;
	}
	@Column(length=50)
	private String getC11() {
		return c11;
	}
	private void setC11(String c11) {
		this.c11 = c11;
	}
	@Column(length=50)
	private String getC12() {
		return c12;
	}
	private void setC12(String c12) {
		this.c12 = c12;
	}

	private double getA01() {
		return a01;
	}

	private void setA01(double a01) {
		this.a01 = a01;
	}

	private double getA02() {
		return a02;
	}

	private void setA02(double a02) {
		this.a02 = a02;
	}

	private double getA03() {
		return a03;
	}

	private void setA03(double a03) {
		this.a03 = a03;
	}

	private double getA04() {
		return a04;
	}

	private void setA04(double a04) {
		this.a04 = a04;
	}

	private double getA05() {
		return a05;
	}

	private void setA05(double a05) {
		this.a05 = a05;
	}

	private double getA06() {
		return a06;
	}

	private void setA06(double a06) {
		this.a06 = a06;
	}

	private double getA07() {
		return a07;
	}

	private void setA07(double a07) {
		this.a07 = a07;
	}

	private double getA08() {
		return a08;
	}

	private void setA08(double a08) {
		this.a08 = a08;
	}

	private double getA09() {
		return a09;
	}

	private void setA09(double a09) {
		this.a09 = a09;
	}

	private double getA10() {
		return a10;
	}

	private void setA10(double a10) {
		this.a10 = a10;
	}

	private double getA11() {
		return a11;
	}

	private void setA11(double a11) {
		this.a11 = a11;
	}

	private double getA12() {
		return a12;
	}

	private void setA12(double a12) {
		this.a12 = a12;
	}
	
	@Transient
	public List<String> getColNamesF9() {
		List<String> list = new ArrayList<String>();
		if (c09!=null)		list.add(c09);
		if (c10!=null)		list.add(c10);
		if (c11!=null)		list.add(c11);
		if (c12!=null)		list.add(c12);
		return list;
	}
	
	public void setColAmount(int col, String cname, double cvalue) {
		switch(col) {
		case 1:
			this.c01 = cname;
			this.a01 = cvalue;
			break;
		case 2:
			this.c02 = cname;
			this.a02 = cvalue;
			break;
		case 3:
			this.c03 = cname;
			this.a03 = cvalue;
			break;
		case 4:
			this.c04 = cname;
			this.a04 = cvalue;
			break;
		case 5:
			this.c05 = cname;
			this.a05 = cvalue;
			break;
		case 6:
			this.c06 = cname;
			this.a06 = cvalue;
			break;
		case 7:
			this.c07 = cname;
			this.a07 = cvalue;
			break;
		case 8:
			this.c08 = cname;
			this.a08 = cvalue;
			break;
		case 9:
			this.c09 = cname;
			this.a09 = cvalue;
			break;
		case 10:
			this.c10 = cname;
			this.a10 = cvalue;
			break;
		case 11:
			this.c11 = cname;
			this.a11 = cvalue;
			break;
		case 12:
			this.c12 = cname;
			this.a12 = cvalue;
			break;
		}
	}

	@Transient
	public String getMonthnums() {
		return monthnums;
	}

	public void setMonthnums(String monthnums) {
		this.monthnums = monthnums;
	}

	public double getBackAmount() {
		return backAmount;
	}

	public void setBackAmount(double backAmount) {
		this.backAmount = backAmount;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}
}
