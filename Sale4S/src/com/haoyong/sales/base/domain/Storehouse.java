package com.haoyong.sales.base.domain;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.haoyong.sales.base.logic.StorehouseLogic;
import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

/**
 * 仓
 */
@Entity
@DiscriminatorValue(value="4")
public class Storehouse extends AbstractCompany implements PropertyChoosable<Storehouse> {
	
	// 需要仓库验货
	private boolean storeCheck;
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("C", null, 1).getNextSerial();
			this.setNumber(serial);
		}
	}
	
	@Transient
	private Storehouse getChoose() {
		return this;
	}
	
	@Transient
	public List<String> getTrunkDefault() {
		return Arrays.asList(new String[]{"name"});
	}
	
	@Column(length=100)
	private String getSName() {
		StorehouseT t = new StorehouseLogic().getPropertyChoosableLogic().toTrunk(this);
		return t.getHouseName();
	}
	private void setSName(String sname) {
	}

	public boolean isStoreCheck() {
		return storeCheck;
	}

	public void setStoreCheck(boolean storeCheck) {
		this.storeCheck = storeCheck;
	}
}
