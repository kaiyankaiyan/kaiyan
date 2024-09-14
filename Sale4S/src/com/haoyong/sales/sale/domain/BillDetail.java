package com.haoyong.sales.sale.domain;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import net.sf.mily.common.Notely;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.ClientT;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.SubCompanyT;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.SupplierT;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.common.domain.AbstractCommodityItem;
import com.haoyong.sales.sale.logic.BillTicketLogic;
import com.haoyong.sales.sale.logic.WBillTicketLogic;

/**
 * 应收(正数)，应付（负数）
 */
@Entity
@Table(name="sa_billdetail")
public class BillDetail extends AbstractCommodityItem implements Notely {
	
	// 月流水号
	private String monthnum;
	private int stBill;
	
	// 金额
	private double money;
	// 月结流程状态Id
	private int monthId;
	// 生效状态.名称
	private String stateName;
	// 开单人日期
	private String ucreate;
	// 单据过程备注
	private StringBuffer stateNotes = new StringBuffer();
	
	// 客户
	private Client client=new Client();
	// 分公司
	private SubCompany subCompany = new SubCompany();
	// 供应商
	private Supplier supplier = new Supplier();
	// 收款单
	private BillTicket billTicket = new BillTicket();
	// 对账单
	private WBillTicket wbillTicket = new WBillTicket();

	@Column(length=50)
	@Index(name="ibiMonthnum")
	public String getMonthnum() {
		return monthnum;
	}

	public void setMonthnum(String monthnum) {
		this.monthnum = monthnum;
	}

	public int getStBill() {
		return stBill;
	}

	public void setStBill(int stBill) {
		this.stBill = stBill;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	@Transient
	public Client getClient() {
		return this.client;
	}
	
	public void setClient(Client client) {
		this.client = client;
	}

	@Embedded
	private ClientT getTClient() {
		ClientT tclient = new ClientLogic().getPropertyChoosableLogic().toTrunk(client);
		new ClientLogic().getLinkChoosableLogic().setTAlong(tclient).toTrunk(client);
		this.setVoparam(tclient);
		return tclient;
	}

	private void setTClient(ClientT tclient) throws Exception {
		if (tclient==null)			tclient = new ClientT();
		new ClientLogic().getPropertyChoosableLogic().fromTrunk(client, tclient);
		new ClientLogic().getLinkChoosableLogic().fromTrunk(client, tclient);
		if (this.getVoparam(ClientT.class)==null)
			this.setVoparam(tclient);
	}

	@Transient
	public SubCompany getSubCompany() {
		return subCompany;
	}

	public void setSubCompany(SubCompany subCompany) {
		this.subCompany = subCompany;
	}
	
	@Embedded
	private SubCompanyT getTSubCompany() {
		SubCompanyT t = new SubCompanyLogic().getPropertyChoosableLogic().toTrunk(subCompany);
		new SubCompanyLogic().getLinkChoosableLogic().setTAlong(t).toTrunk(subCompany);
		this.setVoparam(t);
		return t;
	}
	
	private void setTSubCompany(SubCompanyT tsub) throws Exception {
		if (tsub==null)				tsub = new SubCompanyT();
		new SubCompanyLogic().getPropertyChoosableLogic().fromTrunk(subCompany, tsub);
		new SubCompanyLogic().getLinkChoosableLogic().fromTrunk(subCompany, tsub);
		if (this.getVoparam(SubCompanyT.class)==null)
			this.setVoparam(tsub);
	}

	@Transient
	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}
	
	@Embedded
	private SupplierT getTSupplier() {
		SupplierT tsupplier = new SupplierLogic().getPropertyChoosableLogic().toTrunk(supplier);
		new SupplierLogic().getLinkChoosableLogic().setTAlong(tsupplier).toTrunk(supplier);
		this.setVoparam(tsupplier);
		return tsupplier;
	}
	
	private void setTSupplier(SupplierT tsupplier) throws Exception {
		if (tsupplier==null)			tsupplier = new SupplierT();
		new SupplierLogic().getPropertyChoosableLogic().fromTrunk(supplier, tsupplier);
		new SupplierLogic().getLinkChoosableLogic().fromTrunk(supplier, tsupplier);
		if (this.getVoparam(SupplierT.class)==null)
			this.setVoparam(tsupplier);
	}

	@Transient
	public BillTicket getBillTicket() {
		return billTicket;
	}

	public void setBillTicket(BillTicket billTicket) {
		new BillTicketLogic().getPropertyChoosableLogic().fromTrunk(this.billTicket, billTicket);
	}
	
	@Embedded
	private BillT getTBill() throws Throwable {
		BillT tbill = new BillTicketLogic().getPropertyChoosableLogic().toTrunk(billTicket);
		this.setVoparam(tbill);
		return tbill;
	}
	private void setTBill(BillT tbill) throws Throwable {
		if (tbill==null)			tbill=new BillT();
		new BillTicketLogic().getPropertyChoosableLogic().fromTrunk(billTicket, tbill);
		if (this.getVoparam(BillT.class)==null)
			this.setVoparam(tbill);
	}
	@Transient
	private String getSBill() {
		BillT t = this.getVoparam(BillT.class);
		StringBuffer sb = new StringBuffer().append(t.getTicket()).append("\n").append("\n").append(t.getHandle());
		return sb.length()<5? "": sb.append("\n").append(this.ucreate).append("，").append(this.getStBill()).toString();
	}

	@Transient
	public WBillTicket getWBillTicket() {
		return wbillTicket;
	}

	public void setWBillTicket(WBillTicket wbillTicket) {
		new WBillTicketLogic().getPropertyChoosableLogic().fromTrunk(this.wbillTicket, wbillTicket);
	}
	
	@Embedded
	private WBillT getTWBill() throws Throwable {
		WBillT twbill = new WBillTicketLogic().getPropertyChoosableLogic().toTrunk(wbillTicket);
		this.setVoparam(twbill);
		return twbill;
	}
	private void setTWBill(WBillT twbill) throws Throwable {
		if (twbill==null)			twbill=new WBillT();
		new WBillTicketLogic().getPropertyChoosableLogic().fromTrunk(wbillTicket, twbill);
		if (this.getVoparam(WBillT.class)==null)
			this.setVoparam(twbill);
	}
	@Transient
	private String getSWBill() {
		WBillT t = this.getVoparam(WBillT.class);
		StringBuffer sb = new StringBuffer().append(t.getTicket()).append("\n").append("\n");
		return sb.toString();
	}

	public int getMonthId() {
		return monthId;
	}

	public void setMonthId(int monthId) {
		this.monthId = monthId;
	}

	@Column(length=20)
	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	@Column(length=50)
	public String getUcreate() {
		return ucreate;
	}

	public void setUcreate(String ucreate) {
		this.ucreate = ucreate;
	}
	
	@Transient
	public StringBuffer getStateBuffer() {
		return this.stateNotes;
	}
	
	@Column(length=1000)
	private String getStateNotes() {
		int len=1000;
		if (this.stateNotes.length()>len)
			this.stateNotes.delete(0, this.stateNotes.length()-len);
		return this.stateNotes.toString();
	}
	
	public void setStateNotes(String notes) {
		this.stateNotes.delete(0, this.stateNotes.length()).append(notes);
	}
}
