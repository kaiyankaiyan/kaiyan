package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.form.CommodityForm;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.StorehouseLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.InstoreT;
import com.haoyong.sales.sale.domain.InstoreTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.InstoreTicketLogic;
import com.haoyong.sales.sale.logic.ReceiptTicketLogic;
import com.haoyong.sales.sale.logic.SendTicketLogic;

public class InStoreTicketForm extends AbstractForm<OrderDetail> implements FViewInitable {

	public void beforeInStore(IEditViewBuilder buuilder0) {
		this.setDomain(new OrderDetail());
		this.getDetailList().clear();
		this.getDetailList().add(new OrderDetail());
	}

	public void validateInStore() {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getDetailList().size()==0)
			sb.append("进仓明细为空！");
		if (new InstoreTicketLogic().getStoreChoosableLogic().isValid(this.getDomain().getInstoreTicket(), sb)==false)
			sb.append("请补充入库信息，");
		for (OrderDetail d: this.getDetailList()) {
			sitem = new StringBuffer();
			if (new CommodityLogic().getPropertyChoosableLogic().isValid(d.getCommodity(), sitem)==false)
				sitem.append("请补充商品信息，");
			if (d.getAmount()<=0)
				sitem.append("进仓数量要大于0，");
			if (sitem.length()>0)
				sb.append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		else
			for (OrderDetail detail: this.getDetailList()) {
				detail.setInstoreTicket(this.getDomain().getInstoreTicket());
				detail.getLocationTicket().setIn(new StorehouseLogic().getStorehouseByName(this.getDomain().getInstoreTicket().getInName()));
			}
	}

	@Override
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true) {
			new SendTicketLogic().getSendChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (true) {
			new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (true) {
			new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (true) {
			new InstoreTicketLogic().getStoreChoosableLogic().trunkViewBuilder(viewBuilder);
		}
	}
	
	public void addDetail() {
		OrderDetail d = new OrderDetail();
		this.getDetailList().add(d);
	}
	
	public OrderDetail getDomain() {
		String k = "PurchaseDetail";
		OrderDetail d = this.getAttr(k);
		if (d==null) {
			d = new OrderDetail();
			this.setAttr(k, d);
		}
		return d;
	}
	
	private void setDomain(OrderDetail detail) {
		String k = "PurchaseDetail";
		this.setAttr(k, detail);
	}
	
	public List<OrderDetail> getDetailList() {
		String k = "PurchaseList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
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
	
	private CommodityForm getCommodityForm() {
		CommodityForm form = getAttr(CommodityForm.class);
		if (form==null) {
			form = new CommodityForm();
			setAttr(form);
		}
		return form;
	}
	
	private InstoreTicket getInstoreTicket() {
		InstoreTicket ticket = this.getAttr(InstoreTicket.class);
		if (ticket==null) {
			ticket = new InstoreTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	public void setInstoreTicketNumber() {
		this.getDomain().getInstoreTicket().genSerialNumber();
	}
	
	private ChooseFormer getInstoreChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new InstoreTicketLogic().getStoreChoosableLogic();
		former.setViewBuilder(logic.getTicketBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getTicketBuilder()));
		return former;
	}
	private ChooseFormer getInstore4DChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new InstoreTicketLogic().getStoreChoosableLogic();
		former.setViewBuilder(logic.getDetailBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getDetailBuilder()));
		return former;
	}
	
	public SelectTicketFormer4Sql<InStoreTicketForm, OrderDetail> getSelectFormer4Purchase() {
		String k="SelectFormer4Purchase";
		SelectTicketFormer4Sql<InStoreTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<InStoreTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private void setInExtra4Service() {
		StoreTicketForm form = new StoreTicketForm();
		for (OrderDetail pur: this.getDetailList()) {
			pur.getReceiptTicket().setStorePrice(pur.getPrice());
		}
		form.setInExtra4Service(this.getDetailList());
		this.setAttr(form);
	}
	
	public void setInstore4Service(ViewData<OrderDetail> viewData) {
	 	this.getAttr(StoreTicketForm.class).setInExtra4Service(viewData);
	}
	
	private void setPurchase4Service(ViewData<OrderDetail> viewData) {
		this.getDomain().getReceiptTicket().genSerialNumber();
		PropertyChoosableLogic.TicketDetail inlogic=new InstoreTicketLogic().getStoreChoosableLogic();
		PropertyChoosableLogic.TicketDetail logic=new ReceiptTicketLogic().getTicketChoosableLogic();
		for (OrderDetail detail: this.getDetailList()) {
			logic.fromTrunk(logic.getTicketBuilder(), detail.getReceiptTicket(), this.getDomain().getReceiptTicket());
			inlogic.fromTrunk(detail.getInstoreTicket(), this.getDomain().getInstoreTicket());
			detail.setTInstore((InstoreT)inlogic.toTrunk(detail.getInstoreTicket()));
		}
		viewData.setTicketDetails(this.getDetailList());
	}
	
	public void setInstoreCount4Service(ViewData<OrderDetail> viewData) {
	 	this.getAttr(StoreTicketForm.class).setExtraCount4Service(viewData);
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
	
	private void setReceiptUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreceipt(suser);
		}
	}
	
	private void getCommoditySearchNumber(TextField input) {
		this.setIsDialogOpen(false);
		String number = input.getText();
		if (StringUtils.isNotEmpty(number)){
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "CommodityQuery", 1|2, "commNumber", number)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	public void setCommoditySelect(List<Commodity> commList) {
		Iterator<OrderDetail> iter = this.getSelectedList().iterator();
		for (Commodity comm: commList) {
			OrderDetail detail = null;
			if (iter.hasNext()) {
				detail = iter.next();
			} else {
				detail = new OrderDetail();
				this.getDetailList().add(detail);
			}
			detail.setCommodity(comm);
		}
		for (; iter.hasNext();) {
			OrderDetail detail = iter.next();
			Commodity comm = new Commodity();
			detail.setCommodity(comm);
		}
	}
}
