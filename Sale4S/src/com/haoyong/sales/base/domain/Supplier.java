package com.haoyong.sales.base.domain;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

/**
 * 供应商
 */
@Entity
@DiscriminatorValue(value="1")
public class Supplier extends AbstractCompany implements PropertyChoosable<Supplier> {
	
	private String submitNumber;
	private String submitType;
	// 受理商家
	private String toSellerName;
	private long toSellerId;
	// 授受人，时间
	private String uaccept;
	
	// 人工编号
	private String number2;
	
	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("G", null, 1).getNextSerial();
			this.setNumber(serial);
		}
	}
	
	@Transient
	private Supplier getChoose() {
		return this;
	}
	@Transient
	private Supplier getHandle() {
		return this;
	}
	
	@Transient
	public List<String> getTrunkDefault() {
		return Arrays.asList(new String[]{"name"});
	}
	
	@Embedded
	private SupplierT getTSupplier() {
		SupplierT t = new SupplierLogic().getLinkChoosableLogic().toTrunk(this);
		this.setVoparam(t);
		return t;
	}
	private void setTSupplier(SupplierT t) throws Exception {
		if (t==null)
			t = new SupplierT();
		new SupplierLogic().getLinkChoosableLogic().fromTrunk(this, t);
		if (this.getVoparam(SupplierT.class)==null)
			this.setVoparam(t);
	}
	
	@Column(length=100)
	private String getSName() {
		SupplierT t = new SupplierLogic().getPropertyChoosableLogic().toTrunk(this);
		return t.getSupplierName();
	}
	private void setSName(String sname) {
	}

	@Transient
	public String getSubmitNumber() {
		return submitNumber;
	}

	public void setSubmitNumber(String submitNumber) {
		this.submitNumber = submitNumber;
	}

	@Transient
	public String getSubmitType() {
		return submitType;
	}

	public void setSubmitType(String submitType) {
		this.submitType = submitType;
	}

	@Transient
	public String getToSellerName() {
		return toSellerName;
	}

	public void setToSellerName(String toSellerName) {
		this.toSellerName = toSellerName;
	}

	@Transient
	public long getToSellerId() {
		return toSellerId;
	}

	public void setToSellerId(long toSellerId) {
		this.toSellerId = toSellerId;
	}

	@Transient
	public String getUaccept() {
		return uaccept;
	}

	public void setUaccept(String uaccept) {
		this.uaccept = uaccept;
	}

	@Column(length=100)
	public String getNumber2() {
		return number2;
	}

	public void setNumber2(String number2) {
		this.number2 = number2;
	}
}
