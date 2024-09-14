package com.haoyong.sales.sale.domain;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.support.tools.TicketPropertyUtil;

import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.domain.StorehouseT;
import com.haoyong.sales.base.logic.StorehouseLogic;
import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.domain.PropertyChoosable;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;

/**
 * 库存分账单
 */
public class LocationTicket implements PropertyChoosable<LocationTicket> {
	
	// 单号
	private String number;
	// 备注
	private String remark;
	
	private double locAmount;
	
	// 迁出分账名称，迁入名称
	private Storehouse out = new Storehouse();
	private Storehouse in = new Storehouse();
	// 订单目标给安装师傅
	private Storehouse to = new Storehouse();
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("FZ", SerialNumberFactory.Month, 5).getNextSerial36();
			this.setNumber(serial);
		}
	}

	private LocationTicket getTicket() {
		return this;
	}
	private LocationTicket getHandle() {
		return this;
	}

	@Override
	public List<String> getTrunkDefault() {
		return new ArrayList<String>();
	}

	public Storehouse getOut() {
		return out;
	}
	public void setOut(AbstractDomain outstore) {
		if (outstore==null)
			outstore = new Storehouse();
		TicketPropertyUtil.copyProperties(outstore, this.out);
	}

	public Storehouse getIn() {
		return in;
	}
	public void setIn(AbstractDomain instore) {
		if (instore==null)
			instore = new Storehouse();
		TicketPropertyUtil.copyProperties(instore, this.in);
	}

	public Storehouse getTo() {
		return to;
	}
	public void setTo(AbstractDomain instore) {
		if (instore==null)
			instore = new Storehouse();
		TicketPropertyUtil.copyProperties(instore, this.to);
	}
	
	protected LocationT getTLocation() {
		LocationT t = new LocationT();
		LocationT t1 = new PurchaseTicketLogic().getLocationChoosableLogic().toTrunk(this);
		t.setChooseValue(t1.getLocationLabel(), t1.getLocationName(), t1.getLocationOther());
		StorehouseT tout =  new StorehouseLogic().getPropertyChoosableLogic().toTrunk(this.out);
		t.setOutValue(tout.getHouseLabel(), tout.getHouseName(), tout.getHouseOther());
		StorehouseT tin = new StorehouseLogic().getPropertyChoosableLogic().toTrunk(this.in);
		t.setInValue(tin.getHouseLabel(), tin.getHouseName(), tin.getHouseOther());
		StorehouseT tTo = new StorehouseLogic().getPropertyChoosableLogic().toTrunk(this.to);
		t.setToValue(tTo.getHouseLabel(), tTo.getHouseName(), tTo.getHouseOther());
		return t;
	}
	
	protected void setTLocation(LocationT t) throws Exception {
		if (t==null)
			t = new LocationT();
		new PurchaseTicketLogic().getLocationChoosableLogic().fromTrunk(this, t);
		StorehouseT tout = new StorehouseT();
		tout.setChooseValue(t.getOutLabel(), t.getOutName(), t.getOutOther());
		new StorehouseLogic().getPropertyChoosableLogic().fromTrunk(this.out, tout);
		StorehouseT tin = new StorehouseT();
		tin.setChooseValue(t.getInLabel(), t.getInName(), t.getInOther());
		new StorehouseLogic().getPropertyChoosableLogic().fromTrunk(this.in, tin);
		StorehouseT tTo = new StorehouseT();
		tTo.setChooseValue(t.getToLabel(), t.getToName(), t.getToOther());
		new StorehouseLogic().getPropertyChoosableLogic().fromTrunk(this.to, tTo);
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public double getLocAmount() {
		return this.locAmount;
	}
	
	public void setLocAmount(double amount) {
		this.locAmount = amount;
	}
}
