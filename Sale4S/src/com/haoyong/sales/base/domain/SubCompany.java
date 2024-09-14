package com.haoyong.sales.base.domain;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

/**
 * 分公司、子公司、工程分公司
 */
@Entity
@DiscriminatorValue(value="3")
public class SubCompany extends AbstractCompany implements PropertyChoosable<SubCompany> {

	private String submitNumber;
	private String submitType;
	// 申请商家
	private String fromSellerName;
	private long fromSellerId;
	// 接受人、时间
	private String uaccept;

	public void genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("F", null, 1).getNextSerial();
			this.setNumber(serial);
		}
	}
	
	@Transient
	private SubCompany getChoose() {
		return this;
	}
	@Transient
	private SubCompany getHandle() {
		return this;
	}
	
	@Transient
	public List<String> getTrunkDefault() {
		return Arrays.asList(new String[]{"name"});
	}

	@Embedded
	private SubCompanyT getTSubCompany() {
		SubCompanyT t = new SubCompanyLogic().getLinkChoosableLogic().toTrunk(this);
		this.setVoparam(t);
		return t;
	}
	private void setTSubCompany(SubCompanyT t) throws Exception {
		if (t==null)
			t = new SubCompanyT();
		new SubCompanyLogic().getLinkChoosableLogic().fromTrunk(this, t);
		if (this.getVoparam(SubCompanyT.class)==null)
			this.setVoparam(t);
	}
	
	@Column(length=100)
	private String getSName() {
		SubCompanyT t = new SubCompanyLogic().getPropertyChoosableLogic().toTrunk(this);
		return t.getSubName();
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
	public String getFromSellerName() {
		return fromSellerName;
	}

	public void setFromSellerName(String fromSellerName) {
		this.fromSellerName = fromSellerName;
	}

	public long getFromSellerId() {
		return fromSellerId;
	}

	public void setFromSellerId(long fromSellerId) {
		this.fromSellerId = fromSellerId;
	}

	@Transient
	public String getUaccept() {
		return uaccept;
	}

	public void setUaccept(String uaccept) {
		this.uaccept = uaccept;
	}
}
