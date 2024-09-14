package com.haoyong.sales.sale.domain;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Transient;

import com.haoyong.sales.common.domain.PropertyChoosable;

public class BomTicket implements PropertyChoosable<BomTicket> {
	
	// 单位用量
	private double aunit;
	// amount需求数量，配给数量，差额数量，占用数量
	private double gotAmount, notAmount, giveAmount;
	// 领用数量，生产还料数量，生产数量，留用数量
	private double occupyAmount, commitAmount, keepAmount;
	// 仓库领用数，仓库还料数，留用领用数，留用还料数
	private double occupy1, back1, occupy2;
	// 收货时确定出库、入库数量
	private double instore, outstore;
	
	private BomTicket getTicket() {
		return this;
	}
	
	public List<String> getTrunkDefault() {
		return Arrays.asList(new String[]{"number"});
	}

	public double getAunit() {
		return aunit;
	}
	public void setAunit(double amount) {
		this.aunit = amount;
	}
	
	public double getGotAmount() {
		return gotAmount;
	}
	public void setGotAmount(double gotAmount) {
		this.gotAmount = gotAmount;
	}
	
	public double getNotAmount() {
		return notAmount;
	}
	public void setNotAmount(double notAmount) {
		this.notAmount = notAmount;
	}
	
	public double getGiveAmount() {
		return giveAmount;
	}
	public void setGiveAmount(double giveAmount) {
		this.giveAmount = giveAmount;
	}

	public double getOccupyAmount() {
		this.occupyAmount = occupy1+occupy2;
		return occupyAmount;
	}
	public void setOccupyAmount(double occupyAmount) {
		this.occupyAmount = occupyAmount;
	}
	
	public double getCommitAmount() {
		return commitAmount;
	}
	public void setCommitAmount(double commitAmount) {
		this.commitAmount = commitAmount;
	}

	@Transient
	public double getOccupy1() {
		return occupy1;
	}
	public void setOccupy1(double occupy1) {
		this.occupy1 = occupy1;
	}
	
	@Transient
	public double getBack1() {
		return back1;
	}
	public void setBack1(double back1) {
		this.back1 = back1;
	}
	
	@Transient
	public double getOccupy2() {
		return occupy2;
	}
	public void setOccupy2(double occupy2) {
		this.occupy2 = occupy2;
	}

	public double getKeepAmount() {
		return keepAmount;
	}
	public void setKeepAmount(double keepAmount) {
		this.keepAmount = keepAmount;
	}

	public double getInstore() {
		return instore;
	}
	public void setInstore(double instore) {
		this.instore = instore;
	}

	public double getOutstore() {
		return outstore;
	}
	public void setOutstore(double outstore) {
		this.outstore = outstore;
	}
}
