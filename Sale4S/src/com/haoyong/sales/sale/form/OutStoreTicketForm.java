package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.form.ClientForm;
import com.haoyong.sales.base.form.StorehouseForm;
import com.haoyong.sales.base.logic.BillTypeLogic;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OutstoreT;
import com.haoyong.sales.sale.domain.OutstoreTicket;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.OrderTypeLogic;
import com.haoyong.sales.sale.logic.OutstoreTicketLogic;
import com.haoyong.sales.sale.logic.SendTicketLogic;

public class OutStoreTicketForm extends AbstractForm<OrderDetail> implements FViewInitable {

	private void beforeOutstoreSale(IEditViewBuilder builder0) {
		OrderDetail domain = new OrderDetail();
		domain.getOrderTicket().setOrderType(new OrderTypeLogic().getSaleType());
		domain.getOrderTicket().setOrderDate(new Date());
		this.setDomain(domain);
		this.getDetailList().clear();
		this.getDetailList().add(domain);
	}
	private void beforeOutstoreSelf(IEditViewBuilder builder0) {
		OrderDetail domain = new OrderDetail();
		domain.getOrderTicket().setOrderType(new OrderTypeLogic().getBackType());
		domain.getOrderTicket().setOrderDate(new Date());
		this.setDomain(domain);
		this.getDetailList().clear();
		this.getDetailList().add(domain);
	}
	
	private void getClientSearchName(TextField input) {
		this.setIsDialogOpen(false);
		String name = input.getText();
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "ClientQuery", 1|2, "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	private void getCommoditySearchNumber(TextField input) {
		this.setIsDialogOpen(false);
		String number = input.getText();
		if (StringUtils.isNotEmpty(number)){
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "CommodityQuery", 2, "commNumber", number)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	public void validateOutStore4Sale() {
		StringBuffer sb=new StringBuffer();
		if (this.getDetailList().size()==0)
			sb.append("出库明细为空，");
		for (OrderDetail order: this.getDetailList()) {
			StringBuffer sitem = new StringBuffer();
			OrderDetail store = order.getVoparam(OrderDetail.class);
			if (store.getStPurchase()==0)
				sitem.append("请选择出库明细，");
			else if (store.getAmount()<order.getAmount())
				sitem.append("出库数量不能大于库存数量，");
			else if (order.getAmount()==0)
				sitem.append("销售数量不能为零，");
			if (order.getOrderTicket().getCprice()==0)
				sitem.append("销售价格不能为0，");
			if (sitem.length()>0)
				sb.append(new CommodityLogic().getPropertyChoosableLogic().toTrunk(order.getCommodity()).getCommName()).append(sitem).append("\t");
		}
		if (new ClientLogic().getPropertyChoosableLogic().isValid(this.getDomain().getClient(), sb)==false)
			sb.append("请补充客户信息，");
		if (new SendTicketLogic().getSendChoosableLogic().isValid(this.getDomain().getSendTicket(), sb)==false)
			sb.append("请补充发货信息，");
		if (sb.length()>0)			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateOutStore4Self() {
		StringBuffer sb = new StringBuffer();
		if (new OutstoreTicketLogic().getStoreChoosableLogic().isValid(getDomain().getOutstoreTicket(), sb)==false)
			sb.append("请补充出主库单信息，");
		if (this.getDetailList().size()==0)
			sb.append("出库明细为空！");
		for (OrderDetail order: this.getDetailList()) {
			StringBuffer sitem = new StringBuffer();
			OrderDetail store = order.getVoparam(OrderDetail.class);
			if (store.getStPurchase()==0)
				sitem.append("请选择出库明细，");
			else if (store.getAmount()<order.getAmount())
				sitem.append("出库数量不能大于库存数量，");
			if (sitem.length()>0)
				sb.append(new CommodityLogic().getPropertyChoosableLogic().toTrunk(order.getCommodity()).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	@Override
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true)
			new OrderTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new SendTicketLogic().getSendChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new OutstoreTicketLogic().getStoreChoosableLogic().trunkViewBuilder(viewBuilder);
	}
	
	public void addDetail() {
		OrderDetail d = new OrderDetail();
		this.getDetailList().add(d);
	}
	
	public List<OrderDetail> getDetailList() {
		String k="OrderDetailList";
		List<OrderDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}

	public OrderDetail getDomain() {
		String k = "OrderDetailDomain";
		OrderDetail domain = this.getAttr(k);
		if (domain==null) {
			domain = new OrderDetail();
			this.setAttr(k, domain);
		}
		return domain;
	}
	
	private void setDomain(OrderDetail domain) {
		String k = "OrderDetailDomain";
		this.setAttr(k, domain);
	}
	
	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	private List<OrderDetail> getSelectedList() {
		String k = "SelectedList";
		List<OrderDetail> list = getAttr(k);
		if (list==null) {
			list = new ArrayList<OrderDetail>();
			setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
		getSelectedList().clear();
		getSelectedList().addAll(selected);
	}
	
	private List<SupplyType> getSupplyTypeOptions(Object entity) {
		List<SupplyType> typeList = new SupplyTypeLogic().getTypeList();
		return typeList;
	}
	
	public boolean getIsDialogOpen() {
		String k = "IsDialogOpen";
		Boolean ok = this.getAttr(k);
		if (ok == null) {
			ok = Boolean.FALSE;
			this.setAttr(k, ok);
		}
		return ok;
	}
	
	private void setIsDialogOpen(boolean open) {
		String k = "IsDialogOpen";
		this.setAttr(k, open);
	}
	
	private double getTotalMoney() {
		double money = 0;
		for (OrderDetail detail: this.getDetailList()) {
			money += detail.getPrice() * detail.getAmount();
		}
		return money;
	}
	
	private void getStorehouseSearchName(TextField input) {
		this.setIsDialogOpen(false);
		String name = input.getText();
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "StorehouseQuery", 2, "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	private void setStorehouseSelect(List<Storehouse> clientList) {
		Storehouse client = clientList.size()==0? new Storehouse(): clientList.get(0);
		this.getDomain().getOutstoreTicket().setOutName(client.getName());
		this.getDomain().getLocationTicket().setOut(client);
		this.getDomain().getLocationTicket().setIn(null);
		this.getDetailList().clear();
	}
	
	private OutstoreTicket getOutstoreTicket() {
		OutstoreTicket ticket = this.getAttr(OutstoreTicket.class);
		if (ticket==null) {
			ticket = new OutstoreTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	private ChooseFormer getOutstoreChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new OutstoreTicketLogic().getStoreChoosableLogic();
		former.setViewBuilder(logic.getTicketBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getTicketBuilder()));
		return former;
	}
	private ChooseFormer getOutstore4DChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new OutstoreTicketLogic().getStoreChoosableLogic();
		former.setViewBuilder(logic.getDetailBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getDetailBuilder()));
		return former;
	}
	
	private void setOutstoreTicketNumber() {
		this.getDomain().getOutstoreTicket().genSerialNumber();
	}
	
	private void setSendNumberNew() {
		this.getDomain().getSendTicket().genSerialNumber();
	}
	
	private InStoreTicketForm getInStoreTicketForm() {
		InStoreTicketForm form = getAttr(InStoreTicketForm.class);
		if (form==null) {
			form = new InStoreTicketForm();
			setAttr(form);
		}
		return form;
	}
	
	private ClientForm getClientForm() {
		ClientForm form = getAttr(ClientForm.class);
		if (form==null) {
			form = new ClientForm();
			this.setAttr(form);
		}
		return form;
	}
	private StorehouseForm getStorehouseForm() {
		StorehouseForm form = getAttr(StorehouseForm.class);
		if (form==null) {
			form = new StorehouseForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public StoreTicketForm getOut4StoreTicketForm() {
		String k = "Out4StoreTicketForm";
		StoreTicketForm form = this.getAttr(k);
		if (form==null) {
			form = new StoreTicketForm();
			this.setAttr(k, form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<OutStoreTicketForm, OrderDetail> getSelectFormer4Sale() {
		String k="SelectFormer4Sale";
		SelectTicketFormer4Sql<OutStoreTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OutStoreTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<OutStoreTicketForm, OrderDetail> getSelectFormer4Purchase() {
		String k="SelectFormer4Purchase";
		SelectTicketFormer4Sql<OutStoreTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OutStoreTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private void setSendState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setSendId(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private void setOrderState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setStOrder(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	private void setArrangeState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setArrangeId(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private void setPurchaseState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setStPurchase(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private void setReceiptState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setReceiptId(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private void setBillState(State state, ViewData<BillDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (BillDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setStBill(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private void setSaleUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUorder(suser);
			d.setUarrange(suser);
			d.setUsend(suser);
		}
	}
	
	private void setSaleOrder4Service(ViewData<OrderDetail> viewData) {
		this.getDomain().getOrderTicket().genSerialNumber4Sale();
		this.getDomain().getSendTicket().genSerialNumber();
		PropertyChoosableLogic.TicketDetail<OutStoreTicketForm, OutstoreTicket, OutstoreT> logic = new OutstoreTicketLogic().getStoreChoosableLogic();
		PropertyChoosableLogic.TicketDetail ologic=new OrderTicketLogic().getTicketChoosableLogic();
		PropertyChoosableLogic.TicketDetail slogic=new SendTicketLogic().getSendChoosableLogic();
		for (OrderDetail order: this.getDetailList()) {
			OrderDetail store = order.getVoparam(OrderDetail.class);
			if (order.getAmount() < store.getAmount())
				order.setMonthnum(new OrderTicketLogic().getSplitMonthnum(store.getMonthnum()));
			logic.fromTrunk(order.getOutstoreTicket(), this.getDomain().getOutstoreTicket());
			order.setTOutstore(logic.toTrunk(order.getOutstoreTicket()));
			ologic.fromTrunk(ologic.getTicketBuilder(), order.getOrderTicket(), this.getDomain().getOrderTicket());
			slogic.fromTrunk(slogic.getTicketBuilder(), order.getSendTicket(), this.getDomain().getSendTicket());
			order.setClient(this.getDomain().getClient());
			order.getSendTicket().setSendDate(new Date());
		}
		viewData.setTicketDetails(this.getDetailList());
	}
	
	private void setOutstore4Service() {
		List<Long> puridList = new ArrayList<Long>();
		for (OrderDetail order: this.getDetailList()) {
			OrderDetail pur = order.getVoparam(OrderDetail.class);
			puridList.add(pur.getId());
			order.getLocationTicket().setIn(pur.getLocationTicket().getIn());
		}
		this.getOut4StoreTicketForm().setOutExtra4Service(this.getDetailList(), "id", puridList.toArray(new Long[0]));
		for (OrderDetail order: this.getDetailList()) {
			order.getLocationTicket().setOut(order.getLocationTicket().getIn());
		}
	}
	
	private void setOutstore4Service(ViewData<OrderDetail> viewData) {
		this.getOut4StoreTicketForm().setOutExtra4Service(viewData);
		viewData.setParam("WantoutList", this.getDetailList());
	}
	
	private void setSaleBill4Service(ViewData<BillDetail> viewData) {
		List<BillDetail> billList=new ArrayList<BillDetail>(), presentList=new ArrayList<BillDetail>();
		for (OrderDetail order: this.getDetailList()) {
			BillDetail bill = TicketPropertyUtil.copyProperties(order, new BillDetail());
			bill.setPrice(order.getOrderTicket().getCprice());
			bill.setMoney(bill.getPrice() * bill.getAmount());
			if (bill.getMoney()==0) {
				bill.getBillTicket().setBillDate(new Date());
				presentList.add(bill);
			}
			bill.getBillTicket().setTypeName(new BillTypeLogic().getSaleType());
			order.setVoparam(bill);
			billList.add(bill);
		}
		viewData.setTicketDetails(billList);
		viewData.setParam("PresentList", presentList);
	}
	
	private void setSaleStore4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getDetailList()) {
			OrderDetail pur=order.getVoparam(OrderDetail.class), spur=null;
			if (pur==null)
				continue;
			spur = pur.getSnapShot();
			if (spur.getStPurchase()>0)
				purList.add(pur);
		}
		viewData.setTicketDetails(purList);
	}
	
	public void setCommoditySelect(List<OrderDetail> storeList) {
		Iterator<OrderDetail> iter = this.getSelectedList().iterator();
		for (OrderDetail store: storeList) {
			OrderDetail detail = null;
			if (iter.hasNext()) {
				detail = iter.next();
			} else {
				detail = new OrderDetail();
				this.getDetailList().add(detail);
			}
			new CommodityLogic().fromTrunk(detail.getCommodity(), store.getCommodity());
			detail.setMonthnum(store.getMonthnum());
			detail.getReceiptTicket().setStorePrice(store.getReceiptTicket().getStorePrice());
			detail.setVoparam(store);
		}
		for (; iter.hasNext();) {
			OrderDetail detail = iter.next();
			OrderDetail store = new OrderDetail();
			new CommodityLogic().fromTrunk(detail.getCommodity(), store.getCommodity());
			detail.setMonthnum(store.getMonthnum());
			detail.getReceiptTicket().setStorePrice(store.getReceiptTicket().getStorePrice());
			detail.setVoparam(store);
		}
	}
	
	public void setClientSelect(List<Client> clientList) {
		Client client = clientList.size()==0? null: clientList.get(0);
		if (client!=null)
			new ClientLogic().fromTrunk(this.getDomain().getClient(), client);
	}
	
	private void setVersionDomains() {
		List<OrderDetail> storeList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getDetailList()) {
			OrderDetail store = order.getVoparam(OrderDetail.class);
			storeList.add(store);
		}
		this.getSelectFormer4Purchase().setIdDomains(storeList);
	}
	
	private List<OrderDetail> getPurchaseList4Sale() {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getDetailList()) {
			OrderDetail pur=order.getVoparam(OrderDetail.class), spur=null;
			if (pur==null)
				continue;
			spur = pur.getSnapShot();
			if (spur.getStPurchase()>0)
				list.add(pur);
		}
		return list;
	}
	
	public HashMap<String, String> getParam4Commodity() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (true) {
			StringBuffer sb = new StringBuffer();
			for (Iterator<OrderDetail> iter=getPurchaseList4Sale().iterator(); iter.hasNext();) {
				OrderDetail pur = iter.next();
				sb.append("c.id!=").append(pur.getId());
				sb.append(iter.hasNext()? " and ": "");
			}
			map.put("purchaseIdLimit", sb.length()>0? sb.toString(): "1=1");
		}
		if (true) {
			if (new UserLogic().isInstallRole(this.getUser())) {
				StringBuffer sb = new StringBuffer();
				sb.append(" and c.instore='").append(this.getUserName()).append("'");
				map.put("PInstore", sb.toString());
			} else if (StringUtils.isBlank(this.getDomain().getOutstoreTicket().getOutName())==false) {
				StringBuffer sb = new StringBuffer();
				sb.append(" and c.instore='").append(this.getDomain().getOutstoreTicket().getOutName()).append("'");
				map.put("PInstore", sb.toString());
			} else {
				map.put("PInstore", "");
			}
		}
		return map;
	}
}
