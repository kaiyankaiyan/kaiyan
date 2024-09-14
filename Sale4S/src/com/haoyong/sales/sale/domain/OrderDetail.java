package com.haoyong.sales.sale.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.sf.mily.common.Notely;
import net.sf.mily.common.Tickety;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.util.LogUtil;

import org.hibernate.annotations.Index;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.ClientT;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.SubCompanyT;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.SupplierT;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.common.domain.AbstractCommodityItem;
import com.haoyong.sales.sale.form.PurchaseTicketForm;
import com.haoyong.sales.sale.logic.ArrangeTicketLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.sale.logic.ReceiptTicketLogic;
import com.haoyong.sales.sale.logic.SendTicketLogic;

/**
 * 订单明细（下单——排单）
 */
@Entity
@Table(name="sa_orderdetail")
public class OrderDetail extends AbstractCommodityItem implements Notely, Tickety {
	
	// 月流水号
	private String monthnum;
	
	// 客户
	private Client client=new Client();
	// 分公司
	private SubCompany subCompany=new SubCompany();
	// 供应商
	private Supplier supplier = new Supplier();
	// 物流运送，OrderTicket.LocationTicket
	// 订单
	private OrderTicket orderTicket = new OrderTicket();
	// 排单
	private ArrangeTicket arrangeTicket = new ArrangeTicket();
	// 采购单
	private PurchaseTicket purchaseTicket = new PurchaseTicket();
	// 收货单
	private ReceiptTicket receiptTicket = new ReceiptTicket();
	// 入库单
	private InstoreTicket instoreTicket = new InstoreTicket();
	// 出库单
	private OutstoreTicket outstoreTicket = new OutstoreTicket();
	// 销售发货单
	private SendTicket sendTicket = new SendTicket();
	// 发货退回单
	private ReturnTicket returnTicket = new ReturnTicket();
	
	// 备注
	private String remark;
	// 改单备注
	private String changeRemark;
	// 审核各属性批注，以,分隔
	private String notes;
	// 生效状态.名称
	private String stateName;
	// 单据过程备注
	private StringBuffer stateNotes = new StringBuffer();
	
	// 订单人
	private String uorder;
	// 采购人
	private String upurchase;
	// 收货人
	private String ureceipt;
	// 改单申请人
	private String uchange;
	// 终止订单人
	private String ucancel;
	// 退货人，流程多以,相隔
	private String ureturn;
	// 排单人
	private String uarrange;
	// 销售出货人
	private String usend;
	// 库存分账迁出开单人员日期from分账to分账
	private String ulocation;
	
	private int stOrder;
	private int stPurchase;
	// 排单状态.Id
	private int arrangeId;
	// 收货状态ID
	private int receiptId;
	// 物料状态.Id
	private int bomId;
	// 发料状态.Id, 采购开单在途10，收货20，发料30
	private int sendId;
	// 顺丰出库ID，入库ID
	private int outsfId, insfId;
	
	@Column(length=50)
	@Index(name="ioMonthnum")
	public String getMonthnum() {
		return monthnum;
	}

	public void setMonthnum(String monthnum) {
		this.monthnum = monthnum;
	}
	
	public int getStOrder() {
		return stOrder;
	}

	public void setStOrder(int stOrder) {
		this.stOrder = stOrder;
	}

	@Column(length=4000)
	@Deprecated
	public String getBDetails() {
		return this.getCommodity().getBDetails();
	}

	public void setBDetails(String json) {
		this.getCommodity().setBDetails(json);
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
	public LocationTicket getLocationTicket() {
		return this.getOrderTicket().getLocationTicket();
	}

	public void setLocationTicket(LocationTicket storehouse) {
		TicketPropertyUtil.copyFieldsSkip(storehouse, this.getOrderTicket().getLocationTicket());
	}

	@Embedded
	private LocationT getTLocation() {
		LocationT tlocation = this.getLocationTicket().getTLocation();
		this.setVoparam(tlocation);
		return tlocation;
	}
	private void setTLocation(LocationT tlocation) throws Exception {
		if (tlocation==null)
			tlocation = new LocationT();
		this.getLocationTicket().setTLocation(tlocation);
		if (this.getVoparam(LocationT.class)==null)
			this.setVoparam(tlocation);
	}
	
	@Column(length=30)
	public String getOutstore() {
		return this.getLocationTicket().getOut().getName();
	}
	private void setOutstore(String n) {
	}
	@Column(length=100)
	public String getInstore() {
		return this.getLocationTicket().getIn().getName();
	}
	private void setInstore(String n) {
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

	@Column(length=30)
	public String getUorder() {
		return this.uorder;
	}
	
	public void setUorder(String tcreate) {
		this.uorder = tcreate;
	}

	public String getChangeRemark() {
		return changeRemark;
	}

	public void setChangeRemark(String changeRemark) {
		this.changeRemark = changeRemark;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(length=30)
	public String getUchange() {
		return uchange;
	}

	public void setUchange(String tchange) {
		this.uchange = tchange;
	}

	@Column(length=30)
	public String getUpurchase() {
		return upurchase;
	}

	public void setUpurchase(String tcreate) {
		this.upurchase = tcreate;
	}

	@Column(length=30)
	public String getUreceipt() {
		return ureceipt;
	}

	public void setUreceipt(String treceipt) {
		this.ureceipt = treceipt;
	}

	@Column(length=40)
	public String getUcancel() {
		return ucancel;
	}

	public void setUcancel(String ucancel) {
		this.ucancel = ucancel;
	}

	@Column(length=40)
	public String getUreturn() {
		return ureturn;
	}

	public void setUreturn(String ureturn) {
		this.ureturn = ureturn;
	}

	@Column(length=30)
	public String getUarrange() {
		return this.uarrange;
	}
	
	public void setUarrange(String tuser) {
		this.uarrange = tuser;
	}
	
	@Column(length=50)
	public String getUsend() {
		return usend;
	}

	public void setUsend(String usend) {
		this.usend = usend;
	}

	@Column(length=50)
	public String getUlocation() {
		return ulocation;
	}

	public void setUlocation(String ulocation) {
		this.ulocation = ulocation;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getStPurchase() {
		return stPurchase;
	}

	public void setStPurchase(int stPurchase) {
		this.stPurchase = stPurchase;
	}
	
	public int getArrangeId() {
		return arrangeId;
	}

	public void setArrangeId(int arrangeId) {
		this.arrangeId = arrangeId;
	}

	public int getReceiptId() {
		return receiptId;
	}

	public void setReceiptId(int receiptId) {
		this.receiptId = receiptId;
	}

	public int getBomId() {
		return bomId;
	}

	public void setBomId(int bomId) {
		this.bomId = bomId;
	}

	public int getSendId() {
		return sendId;
	}

	public void setSendId(int sendId) {
		this.sendId = sendId;
	}

	public int getOutsfId() {
		return outsfId;
	}

	public void setOutsfId(int outsfId) {
		this.outsfId = outsfId;
	}

	public int getInsfId() {
		return insfId;
	}

	public void setInsfId(int insfId) {
		this.insfId = insfId;
	}

	@Column(length=20)
	public String getStateName() {
		return this.stateName;
	}
	
	public void setStateName(String name) {
		this.stateName = name;
	}
	
	@Transient
	public StringBuffer getStateBuffer() {
		return this.stateNotes;
	}
	
	@Column(length=2000)
	private String getStateNotes() {
		int len=2000;
		if (this.stateNotes.length()>len)
			this.stateNotes.delete(0, this.stateNotes.length()-len);
		return this.stateNotes.toString();
	}
	
	public void setStateNotes(String notes) {
		this.stateNotes.delete(0, this.stateNotes.length()).append(notes);
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
	private String getSOrder() {
		OrderT t = this.getVoparam(OrderT.class);
		StringBuffer sb = new StringBuffer().append(t.getTicket()).append("\n").append(t.getDetail()).append("\n").append(t.getHandle());
		return sb.length()<5? "": sb.append("\n").append(this.uorder).append("，").append(this.getStOrder()).toString();
	}
	
	@Transient
	public ArrangeTicket getArrangeTicket() {
		List<Object> list = this.getTVoParamMap(this.arrangeTicket);
		if (list.size()>0) {
			ArrangeT tarrange = (ArrangeT)list.get(0);
			this.setTArrange(tarrange);
			list.clear();
		}
		return arrangeTicket;
	}
	
	public void setArrangeTicket(ArrangeTicket arrangeTicket) {
		TicketPropertyUtil.copyFieldsSkip(arrangeTicket, this.arrangeTicket);
	}
	
	@Embedded
	private ArrangeT getTArrange() {
		ArrangeT t = new ArrangeTicketLogic().getPropertyChoosableLogic(this.getCommodity().getSupplyType()).toTrunk(arrangeTicket);
		this.setVoparam(t);
		return t;
	}
	private void setTArrange(ArrangeT tarrange) {
		if (tarrange==null)				tarrange=new ArrangeT();
		if (this.getVoparam(CommodityT.class)==null) {
			this.getTVoParamMap(this.arrangeTicket).add(tarrange);
		} else {
			try {
				new ArrangeTicketLogic().getPropertyChoosableLogic(this.getCommodity().getSupplyType()).fromTrunk(arrangeTicket, tarrange);
			} catch (Exception e) {
				throw LogUtil.getRuntimeException(e);
			}
			if (this.getVoparam(ArrangeT.class)==null)
				this.setVoparam(tarrange);
		}
	}
	@Transient
	private String getSArrange() {
		ArrangeT t = this.getVoparam(ArrangeT.class);
		StringBuffer sb = new StringBuffer().append(t.getTicket()).append("\n").append("\n").append(t.getHandle());
		return sb.length()<5? "": sb.append("\n").append(this.uarrange).append("，").append(this.arrangeId).toString();
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
	private String getSReceipt() {
		ReceiptT t = this.getVoparam(ReceiptT.class);
		StringBuffer sb = new StringBuffer().append(t.getTicket()).append("\n").append(t.getDetail()).append("\n").append(t.getHandle());
		return sb.length()<5? "": sb.append("\n").append(this.ureceipt).append("，").append(this.receiptId).toString();
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
	private String getSPurchase() {
		PurchaseT t = this.getVoparam(PurchaseT.class);
		StringBuffer sb = new StringBuffer().append(t.getTicket()).append("\n").append(t.getDetail()).append("\n").append(t.getHandle());
		return sb.length()<5? "": sb.append("\n").append(this.upurchase).append("，").append(this.getStPurchase()).toString();
	}

	@Transient
	public InstoreTicket getInstoreTicket() {
		return instoreTicket;
	}

	public void setInstoreTicket(InstoreTicket instoreTicket) {
		TicketPropertyUtil.copyFieldsSkip(instoreTicket, this.instoreTicket);
	}
	
	@Embedded
	public InstoreT getTInstore() {
		InstoreT t = this.getVoparam(InstoreT.class);
		if (t==null) {
			t = new InstoreT();
			this.setVoparam(t);
		}
		return t;
	}
	public void setTInstore(InstoreT tin) {
		if (tin==null)			tin=new InstoreT();
		TicketPropertyUtil.copyFieldsSkip(tin, this.getTInstore());
	}
	@Transient
	private String getSInstore() {
		InstoreT t = this.getVoparam(InstoreT.class);
		StringBuffer sb = new StringBuffer().append(t.getTicket()).append("\n").append("\n").append(t.getHandle());
		return sb.toString();
	}
	
	@Transient
	public SendTicket getSendTicket() {
		return sendTicket;
	}
	
	public void setSendTicket(SendTicket saleSendTicket) {
		TicketPropertyUtil.copyFieldsSkip(saleSendTicket, this.sendTicket);
	}
	
	@Embedded
	private SendT getTSend() {
		SendT t = new SendTicketLogic().getSendChoosableLogic().toTrunk(sendTicket);
		this.setVoparam(t);
		return t;
	}
	private void setTSend(SendT tsaleSend) throws Exception {
		if (tsaleSend==null)				tsaleSend=new SendT();
		new SendTicketLogic().getSendChoosableLogic().fromTrunk(sendTicket, tsaleSend);
		if (this.getVoparam(SendT.class)==null)
			this.setVoparam(tsaleSend);
	}
	@Transient
	private String getSSend() {
		SendT t = this.getVoparam(SendT.class);
		StringBuffer sb = new StringBuffer().append(t.getTicket()).append("\n").append("\n");
		return sb.length()<5? "": sb.append("\n").append(this.usend).append("，").append(this.sendId).toString();
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
	@Transient
	private String getSOutstore() {
		OutstoreT t = this.getVoparam(OutstoreT.class);
		StringBuffer sb = new StringBuffer().append(t.getTicket()).append("\n").append("\n").append(t.getHandle());
		return sb.length()<5? "": sb.append("\n").toString();
	}

	@Transient
	public ReturnTicket getReturnTicket() {
		return returnTicket;
	}

	public void setReturnTicket(ReturnTicket returnTicket) {
		TicketPropertyUtil.copyFieldsSkip(returnTicket, this.returnTicket);
	}
	
	@Embedded
	public ReturnT getTReturn() {
		ReturnT t = this.getVoparam(ReturnT.class);
		if (t==null) {
			t = new ReturnT();
			this.setVoparam(t);
		}
		return t;
	}
	public void setTReturn(ReturnT tin) {
		if (tin==null)			tin=new ReturnT();
		TicketPropertyUtil.copyFieldsSkip(tin, this.getTReturn());
	}
	@Transient
	private String getSReturn() {
		ReturnT t = this.getVoparam(ReturnT.class);
		return new StringBuffer().append(t.getTicket()).append("\n").append("\n").append(t.getHandle()).toString();
	}
}
