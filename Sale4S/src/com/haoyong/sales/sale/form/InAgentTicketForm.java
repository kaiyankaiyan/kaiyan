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

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.form.CommodityForm;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.InstoreTicket;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.InstoreTicketLogic;
import com.haoyong.sales.sale.logic.SendTicketLogic;

public class InAgentTicketForm extends AbstractForm<BomDetail> implements FViewInitable {

	public void beforeInstore(IEditViewBuilder buuilder0) {
		this.getDomain().setInstoreTicket(new InstoreTicket());
		this.getDetailList().clear();
	}
	
	public void validateInstore() {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getDetailList().size()==0)
			sb.append("进仓明细为空！");
		if (new InstoreTicketLogic().getAgentChoosableLogic().isValid(this.getDomain().getInstoreTicket(), sb)==false)
			sb.append("请补充入账单信息，");
		for (BomDetail d: this.getDetailList()) {
			sitem = new StringBuffer();
			d.setAmount(d.getInstoreTicket().getInAmount() - d.getInstoreTicket().getOutAmount());
			if (new CommodityLogic().getPropertyChoosableLogic().isValid(d.getCommodity(), sitem)==false)
				sitem.append("请补充商品信息，");
			if (sitem.length()>0)
				sb.append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
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
			new InstoreTicketLogic().getAgentChoosableLogic().trunkViewBuilder(viewBuilder);
		}
	}
	
	public BomDetail getDomain() {
		String k = "BomDetail";
		BomDetail d = this.getAttr(k);
		if (d==null) {
			d = new BomDetail();
			this.setAttr(k, d);
		}
		return d;
	}
	
	private void setDomain(BomDetail detail) {
		String k = "BomDetail";
		this.setAttr(k, detail);
	}
	
	public List<BomDetail> getDetailList() {
		String k = "BomDetailList";
		List<BomDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<BomDetail>();
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
	
	private List<BomDetail> getSelectedList() {
		String k = "SelectedList";
		List<BomDetail> list = getAttr(k);
		if (list==null) {
			list = new ArrayList<BomDetail>();
			setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<BomDetail> selected) {
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
	
	private ChooseFormer getInstoreChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		InstoreTicketLogic logic = new InstoreTicketLogic();
		former.setViewBuilder(logic.getAgentChoosableLogic().getTicketBuilder());
		former.setSellerViewSetting(logic.getAgentChoosableLogic().getChooseSetting( logic.getAgentChoosableLogic().getTicketBuilder() ));
		return former;
	}
	
	public HashMap<String, String> getParam4Commodity() {
		HashMap<String, String> map = new HashMap<String, String>();
		StringBuffer sb = new StringBuffer();
		for (Iterator<BomDetail> iter=this.getDetailList().iterator(); iter.hasNext();) {
			BomDetail agent = iter.next();
			if (agent!=null) {
				CommodityT tcomm = agent.getVoparam(CommodityT.class);
				sb.append("s.commName!='").append(tcomm.getCommName()).append("' and ");
			}
		}
		map.put("NotCommodityList", sb.length()>0? sb.insert(0, "(").delete(sb.length()-5, sb.length()).append(")").toString(): "1=1");
		return map;
	}
	
	private double getTotalMoney() {
		double money = 0;
		for (BomDetail detail: this.getDetailList()) {
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
	
	private StoreTicketForm getInstoreAgentForm() {
		String k = "InstoreAgentForm";
		StoreTicketForm form = this.getAttr(k);
		if (form == null) {
			form = new StoreTicketForm();
			this.setAttr(k, form);
		}
		return form;
	}
	
	private InAgentTicketForm getSelfForm() {
		return this;
	}
	
	private InstoreTicket getInstoreTicket() {
		InstoreTicket ticket = this.getAttr(InstoreTicket.class);
		if (ticket == null) {
			ticket = new InstoreTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	private void setInstoreTicketNumber() {
		this.getDomain().getInstoreTicket().genSerialNumber();
	}
	
	public SelectTicketFormer4Sql<InAgentTicketForm, BomDetail> getSelectFormer4Store() {
		String k="SelectFormer4Store";
		SelectTicketFormer4Sql<InAgentTicketForm, BomDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<InAgentTicketForm, BomDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<InAgentTicketForm, Commodity> getSelectFormer4Commodity() {
		String k="SelectFormer4Commodity";
		SelectTicketFormer4Sql<InAgentTicketForm, Commodity> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<InAgentTicketForm, Commodity>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private void setInstore4Service() {
		List<BomDetail> bomList = new ArrayList<BomDetail>();
		for (BomDetail bom: this.getDetailList()) {
			if (bom.getAmount()!=0)
				bomList.add(bom);
		}
		this.getInstoreAgentForm().setInAgent4Service(this.getDomain(), bomList);
	}
	
	public void setInstore4Service(ViewData<BomDetail> viewData) {
	 	this.getInstoreAgentForm().setAgentAdd4Service(viewData);
	}
	
	public void setBom4Service(ViewData<BomDetail> viewData) {
		List<BomDetail> bomList = new ArrayList<BomDetail>();
		PropertyChoosableLogic.TicketDetail logic = new InstoreTicketLogic().getAgentChoosableLogic();
		for (BomDetail bom: this.getDetailList()) {
			if (bom.getAmount()!=0)
				bomList.add(bom);
			logic.fromTrunk(logic.getTicketBuilder(), bom.getInstoreTicket(), this.getDomain().getInstoreTicket());
		}
	 	viewData.setTicketDetails(bomList);
	}
	
	private void setStoreState(State state, ViewData<BomDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (BomDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setStBom(stateId);
			}
			if (stateName != null) {
//				d.setStateName(stateName);
			}
		}
	}
	
	private void setBomState(State state, ViewData<BomDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (BomDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setStBom(stateId);
			}
			if (stateName != null) {
				d.setStateName(stateName);
			}
		}
	}
	
	private void getSupplierSearchName(TextField input) {
		String name = input.getText();
		this.setIsDialogOpen(false);
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "SupplierQuery", 2, "agent", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	public void setSupplierSelect(List<BomDetail> storeList) {
		BomDetail agent = storeList.size()==0? new BomDetail(): storeList.get(0);
		this.getDomain().setSupplier(agent.getSupplier());
		this.getDetailList().clear();
		List<BomDetail> list = new ArrayList<BomDetail>();
		for (BomDetail store: storeList) {
			BomDetail detail = TicketPropertyUtil.copyProperties(store, new BomDetail());
			detail.setVoparam(store.getVoparam(CommodityT.class));
			detail.setVoparam(store);
			detail.getInstoreTicket().setAmount(store.getAmount());
			list.add(detail);
		}
		this.getDetailList().addAll(list);
	}
	
	private void getCommoditySearchNumber(TextField input) {
		this.setIsDialogOpen(false);
		String number = input.getText();
		if (StringUtils.isNotEmpty(number)){
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "CommodityQuery", 1|2, "commNumber", number)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	public void setCommoditySelect(List<BomDetail> storeList) {
		BomDetail store = storeList.size()==0? new BomDetail(): storeList.get(0);
		BomDetail bom = this.getSelectedList().size()==0? new BomDetail(): this.getSelectedList().get(0);
		bom.setCommodity(store.getCommodity());
		bom.setVoparam(store);
		bom.setInstoreTicket(new InstoreTicket());
		bom.getInstoreTicket().setAmount(store.getAmount());
		bom.setVoparam(store.getVoparam(CommodityT.class));
	}
	
	public void setStoreSelect(List<StoreEnough> storeList) {
		List<BomDetail> list = new ArrayList<BomDetail>();
		for (StoreEnough store: storeList) {
			BomDetail detail = TicketPropertyUtil.copyProperties(store, new BomDetail());
			detail.setVoparam(store.getVoparam(CommodityT.class));
			detail.setAmount(0);
			list.add(detail);
		}
		this.getDetailList().addAll(list);
	}
}
