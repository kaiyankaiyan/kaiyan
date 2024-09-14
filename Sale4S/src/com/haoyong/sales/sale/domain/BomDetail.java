package com.haoyong.sales.sale.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.sf.mily.common.Notely;
import net.sf.mily.support.tools.TicketPropertyUtil;

import org.hibernate.annotations.Index;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.ClientT;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.SubCompanyT;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.SupplierT;
import com.haoyong.sales.base.logic.BomTicketLogic;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.common.domain.AbstractCommodityItem;
import com.haoyong.sales.sale.logic.InstoreTicketLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.sale.logic.ReceiptTicketLogic;

@Entity
@Table(name="sa_bomdetail")
public class BomDetail extends AbstractCommodityItem implements Notely {

	// 月流水号, 出库用的库存物月流水号
	private String monthnum, outMonthnum;
	private int stBom;
	// 安排方式
	private String arrange;
	// 顺序号，层级
	private int sn, level;
	// 流程状态
	private String stateName;
	// 生产收货，原物料出库日期
	private Date sendDate;
	// 备注
	private String remark;
	// 单据过程备注
	private StringBuffer stateNotes = new StringBuffer();
//====================================单据
	// 原料Bom
	private BomTicket bomTicket = new BomTicket();
	// 订单
	private OrderTicket orderTicket = new OrderTicket();
	// 分公司
	private SubCompany subCompany=new SubCompany();
	// 客户
	private Client client=new Client();
	// 供应商，生产方
	private Supplier supplier = new Supplier();
	// 采购单
	private PurchaseTicket purchaseTicket = new PurchaseTicket();
	// 收货单
	private ReceiptTicket receiptTicket = new ReceiptTicket();
	// 入库单
	private InstoreTicket instoreTicket = new InstoreTicket();
	// 出库单
	private OutstoreTicket outstoreTicket = new OutstoreTicket();
//====================================流程状态
	private int stPurchase;
	private int receiptId;
	// 采购人
	private String upurchase;

	@Transient
	public BomTicket getBomTicket() {
		return bomTicket;
	}

	public void setBomTicket(BomTicket bomTicket) {
		TicketPropertyUtil.copyFieldsSkip(bomTicket, this.bomTicket);
	}
	
	@Embedded
	private BomT getTBom() {
		BomT t = new BomTicketLogic().getTicketChoosableLogic().toTrunk(bomTicket);
		this.setVoparam(t);
		return t;
	}
	private void setTBom(BomT tbom) throws Exception {
		if (tbom==null)			tbom=new BomT();
		new BomTicketLogic().getTicketChoosableLogic().fromTrunk(bomTicket, tbom);
		if (this.getVoparam(BomT.class)==null)
			this.setVoparam(tbom);
	}

	@Transient
	public OrderTicket getOrderTicket() {
		return orderTicket;
	}

	public void setOrderTicket(OrderTicket orderTicket) {
		TicketPropertyUtil.copyFieldsSkip(orderTicket, this.orderTicket);
	}
	
	@Embedded
	private OrderT getTOrder() {
		orderTicket.setCmoney(orderTicket.getCprice() * this.getAmount());
		OrderT t = new OrderTicketLogic().getTicketChoosableLogic().toTrunk(orderTicket);
		this.setVoparam(t);
		return t;
	}
	private void setTOrder(OrderT torder) throws Exception {
		if (torder==null)			torder=new OrderT();
		new OrderTicketLogic().getTicketChoosableLogic().fromTrunk(orderTicket, torder);
		if (this.getVoparam(OrderT.class)==null)
			this.setVoparam(torder);
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
	public Client getClient() {
		return this.client;
	}
	public void setClient(Client client) {
		this.client = client;
	}
	@Embedded
	private ClientT getTClient() {
		ClientT tclient =  new ClientLogic().getPropertyChoosableLogic().toTrunk(client);
		new ClientLogic().getLinkChoosableLogic().setTAlong(tclient).toTrunk(client);
		this.setVoparam(tclient);
		return tclient;
	}
	private void setTClient(ClientT tclient) throws Exception {
		if (tclient==null)
			tclient = new ClientT();
		new ClientLogic().getPropertyChoosableLogic().fromTrunk(client, tclient);
		new ClientLogic().getLinkChoosableLogic().fromTrunk(client, tclient);
		if (this.getVoparam(ClientT.class)==null)
			this.setVoparam(tclient);
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
	public PurchaseTicket getPurchaseTicket() {
		return purchaseTicket;
	}
	public void setPurchaseTicket(PurchaseTicket purchaseTicket) {
		TicketPropertyUtil.copyFieldsSkip(purchaseTicket, this.purchaseTicket);
	}
	@Embedded
	private PurchaseT getTPurchase() {
		purchaseTicket.setPmoney(this.getPrice() * this.getAmount());
		PurchaseT t = new PurchaseTicketLogic().getTicketChoosableLogic().toTrunk(purchaseTicket);
		this.setVoparam(t);
		return t;
	}
	private void setTPurchase(PurchaseT tpurchase) throws Exception {
		if (tpurchase==null)			tpurchase=new PurchaseT();
		new PurchaseTicketLogic().getTicketChoosableLogic().fromTrunk(purchaseTicket, tpurchase);
		if (this.getVoparam(PurchaseT.class)==null)
			this.setVoparam(tpurchase);
	}

	@Transient
	public ReceiptTicket getReceiptTicket() {
		return receiptTicket;
	}
	public void setReceiptTicket(ReceiptTicket receiptTicket) {
		TicketPropertyUtil.copyProperties(receiptTicket, this.receiptTicket);
	}
	@Embedded
	private ReceiptT getTReceipt() throws Throwable {
		receiptTicket.setStoreMoney(receiptTicket.getStorePrice() * this.getAmount());
		ReceiptT treceipt = new ReceiptTicketLogic().getTicketChoosableLogic().toTrunk(receiptTicket);
		this.setVoparam(treceipt);
		return treceipt;
	}
	private void setTReceipt(ReceiptT treceipt) throws Throwable {
		if (treceipt==null)			treceipt=new ReceiptT();
		new ReceiptTicketLogic().getTicketChoosableLogic().fromTrunk(receiptTicket, treceipt);
		if (this.getVoparam(ReceiptT.class)==null)
			this.setVoparam(treceipt);
	}

	@Transient
	public InstoreTicket getInstoreTicket() {
		return instoreTicket;
	}
	public void setInstoreTicket(InstoreTicket ticket) {
		new InstoreTicketLogic().getAgentChoosableLogic().fromTrunk(this.instoreTicket, ticket);
	}
	@Embedded
	private InstoreT getTInstore() {
		InstoreT t = new InstoreTicketLogic().getAgentChoosableLogic().toTrunk(instoreTicket);
		this.setVoparam(t);
		return t;
	}
	private void setTInstore(InstoreT torder) throws Exception {
		if (torder==null)			torder=new InstoreT();
		new InstoreTicketLogic().getAgentChoosableLogic().fromTrunk(instoreTicket, torder);
		if (this.getVoparam(InstoreT.class)==null)
			this.setVoparam(torder);
	}

	@Transient
	public OutstoreTicket getOutstoreTicket() {
		return outstoreTicket;
	}
	private void setOutstoreTicket(OutstoreTicket outTicket) {
		this.outstoreTicket = outTicket;
	}
	@Embedded
	public OutstoreT getTOutstore() {
		OutstoreT t = this.getVoparam(OutstoreT.class);
		if (t==null) {
			t = new OutstoreT();
			this.setVoparam(t);
		}
		return t;
	}
	public void setTOutstore(OutstoreT tout) {
		if (tout==null)			tout=new OutstoreT();
		TicketPropertyUtil.copyFieldsSkip(tout, this.getTOutstore());
	}
	
	public String getArrange() {
		return arrange;
	}
	public void setArrange(String arrange) {
		this.arrange = arrange;
	}

	public double getMoney() {
		return this.getPrice() * this.getAmount();
	}

	private void setMoney(double storeMoney) {
	}

	public int getSn() {
		return sn;
	}
	public void setSn(int sn) {
		this.sn = sn;
	}

	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}

	public String getStateName() {
		return stateName;
	}
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
	
	@Column(length=30)
	@Index(name="ibMonthnum")
	public String getMonthnum() {
		return monthnum;
	}
	public void setMonthnum(String monthnum) {
		this.monthnum = monthnum;
	}
	
	@Column(length=30)
	public String getOutMonthnum() {
		return outMonthnum;
	}
	public void setOutMonthnum(String outMonthnum) {
		this.outMonthnum = outMonthnum;
	}
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Temporal(TemporalType.DATE)
	public Date getSendDate() {
		return sendDate;
	}
	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
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

	public int getStBom() {
		return stBom;
	}
	public void setStBom(int stBom) {
		this.stBom = stBom;
	}

	public int getReceiptId() {
		return receiptId;
	}
	public void setReceiptId(int receiptId) {
		this.receiptId = receiptId;
	}

	public String getUpurchase() {
		return upurchase;
	}
	public void setUpurchase(String upurchase) {
		this.upurchase = upurchase;
	}

	public int getStPurchase() {
		return stPurchase;
	}
	public void setStPurchase(int stPurchase) {
		this.stPurchase = stPurchase;
	}
}
