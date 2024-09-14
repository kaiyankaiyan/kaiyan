package com.haoyong.sales.base.domain;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.domain.PropertyChoosable;

/**
 * 商家的客户
 */
@Entity
@DiscriminatorValue(value="2")
public class Client extends AbstractCompany implements PropertyChoosable<Client> {

	// 客户分级，购买商品
	private String level, useCommodity;
	// 付款金额
	private double billReceipt;
	
	private String submitNumber;
	private String submitType;
	// 申请商家
	private String fromSellerName;
	private long fromSellerId;
	// 接受人、时间
	private String uaccept;
	
	public String genSerialNumber() {
		if (this.getNumber() == null) {
			String serial = new SerialNumberFactory().serialLen3("K", null, 1).getNextSerial();
			this.setNumber(serial);
		}
		return this.getNumber();
	}
	
	@Transient
	private Client getChoose() {
		return this;
	}
	@Transient
	private Client getHandle() {
		return this;
	}
	
	@Transient
	public List<String> getTrunkDefault() {
		return Arrays.asList(new String[]{"name"});
	}
	
	@Embedded
	private ClientT getTClient() {
		ClientT t = new ClientLogic().getLinkChoosableLogic().toTrunk(this);
		this.setVoparam(t);
		return t;
	}
	private void setTClient(ClientT t) throws Exception {
		if (t==null)
			t = new ClientT();
		new ClientLogic().getLinkChoosableLogic().fromTrunk(this, t);
		if (this.getVoparam(ClientT.class)==null)
			this.setVoparam(t);
	}
	
	@Column(length=100)
	private String getSName() {
		ClientT t = new ClientLogic().getLinkChoosableLogic().toTrunk(this);
		return t.getClientName();
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
	public String getUaccept() {
		return uaccept;
	}

	public void setUaccept(String uaccept) {
		this.uaccept = uaccept;
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

	@Transient
	public long getFromSellerId() {
		return fromSellerId;
	}

	public void setFromSellerId(long fromSellerId) {
		this.fromSellerId = fromSellerId;
	}

	@Column(length=30)
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public double getBillReceipt() {
		return billReceipt;
	}

	public void setBillReceipt(double billReceipt) {
		this.billReceipt = billReceipt;
	}

	@Column(length=50)
	public String getUseCommodity() {
		return useCommodity;
	}

	public void setUseCommodity(String useCommodity) {
		this.useCommodity = useCommodity;
	}
}
