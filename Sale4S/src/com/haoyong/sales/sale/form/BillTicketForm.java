package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.BillType;
import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.form.BillTypeForm;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.form.ClientForm;
import com.haoyong.sales.base.form.SupplierForm;
import com.haoyong.sales.base.logic.BillTypeLogic;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.BillTicket;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.BillTicketLogic;

public class BillTicketForm extends AbstractForm<BillDetail> implements FViewInitable {

	protected void beforeInBill(IEditViewBuilder buuilder0) {
		this.setDomain(new BillDetail());
		this.getDomain().getBillTicket().setBillDate(new Date());
		this.getDetailList().clear();
		this.getDetailList().add(new BillDetail());
	}

	protected void beforeOutBill(IEditViewBuilder buuilder0) {
		this.setDomain(new BillDetail());
		this.getDomain().getBillTicket().setBillDate(new Date());
		this.getDetailList().clear();
		this.getDetailList().add(new BillDetail());
	}

	public void validateInBill() {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getDetailList().size()==0)
			sb.append("收款明细为空，");
		if (new ClientLogic().getPropertyChoosableLogic().isValid(this.getDomain().getClient(), sb)==false)
			sb.append("请补充客户信息，");
		if (new BillTicketLogic().getPropertyChoosableLogic().isValid(this.getDomain().getBillTicket(), sb)==false)
			sb.append("请补充单头内容，");
		for (BillDetail d: this.getDetailList()) {
			sitem = new StringBuffer();
			if (StringUtils.isEmpty(d.getCommodity().getName()))
				sitem.append("请补充收款名称，");
			if (d.getMoney()<=0)
				sitem.append("金额要大于0，");
			if (sitem.length()>0)
				sb.append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length()>0)			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	public void validateOutBill() {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getDetailList().size()==0)
			sb.append("付款明细为空，");
		if (new SupplierLogic().getPropertyChoosableLogic().isValid(this.getDomain().getSupplier(), sb)==false)
			sb.append("请补充供应商信息，");
		if (new BillTicketLogic().getPropertyChoosableLogic().isValid(this.getDomain().getBillTicket(), sb)==false)
			sb.append("请补充单头内容，");
		for (BillDetail d: this.getDetailList()) {
			sitem = new StringBuffer();
			if (StringUtils.isEmpty(d.getCommodity().getName()))
				sitem.append("请补充付款名称，");
			if (d.getMoney()<=0)
				sitem.append("金额要大于0，");
			if (sitem.length()>0)
				sb.append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length()>0)			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void addDetail() {
		BillDetail d = new BillDetail();
		this.getDetailList().add(d);
	}
	
	@Override
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true) {
			new BillTicketLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (true) {
			new SupplierLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (true) {
			new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
	}

	public List<BillDetail> getDetailList() {
		String k="DetailList";
		List<BillDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<BillDetail>();
			this.setAttr(k, list);
		}
		return list;
	}

	public BillDetail getDomain() {
		String k = "Domain";
		BillDetail detail = this.getAttr(k);
		if (detail == null) {
			detail = new BillDetail();
			this.setAttr(k, detail);
		}
		return detail;
	}
	
	protected void setDomain(BillDetail detail) {
		this.setAttr("Domain", detail);
	}
	
	private void setInBill4Service(ViewData<BillDetail> viewData) {
		this.getDomain().getBillTicket().genSerialNumber();
		PropertyChoosableLogic.TicketDetail logic = new BillTicketLogic().getPropertyChoosableLogic();
		for (BillDetail d: this.getDetailList()) {
			logic.fromTrunk(logic.getTicketBuilder(), d.getBillTicket(), this.getDomain().getBillTicket());
			d.setClient(this.getDomain().getClient());
		}
		viewData.setTicketDetails(this.getDetailList());
	}
	
	private void setOutBill4Service(ViewData<BillDetail> viewData) {
		this.getDomain().getBillTicket().genSerialNumber();
		PropertyChoosableLogic.TicketDetail logic = new BillTicketLogic().getPropertyChoosableLogic();
		for (BillDetail d: this.getDetailList()) {
			logic.fromTrunk(logic.getTicketBuilder(), d.getBillTicket(), this.getDomain().getBillTicket());
			d.setSupplier(this.getDomain().getSupplier());
			d.setMoney(d.getMoney() * -1);
		}
		viewData.setTicketDetails(this.getDetailList());
	}
	
	public void setBillState(State state, ViewData<BillDetail> viewData) {
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
	
	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	public void setBillUser(ViewData<BillDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (BillDetail d: viewData.getTicketDetails()) {
			d.setUcreate(suser);
		}
	}
	
	private List<BillDetail> getSelectedList() {
		String k = "SelectedList";
		List<BillDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<BillDetail>();
			this.setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<BillDetail> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}
	
	private ChooseFormer getChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new BillTicketLogic().getPropertyChoosableLogic();
		former.setViewBuilder(logic.getTicketBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getTicketBuilder()));
		return former;
	}
	
	private BillTypeForm getBillTypeForm() {
		BillTypeForm form = this.getAttr(BillTypeForm.class);
		if (form == null) {
			form = new BillTypeForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private BillTicket getBillTicket() {
		BillTicket ticket = getAttr(BillTicket.class);
		if (ticket==null) {
			ticket = new BillTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	private List<BillType> getBillTypeOptions(Object ticket) {
		return new BillTypeLogic().getTypeList();
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
	
	private void getClientSearchName(TextField input) {
		this.setIsDialogOpen(false);
		String name = input.getText();
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "ClientQuery", 1|2, "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	protected void setClientSelect(List<Client> clientList) {
		Client client = clientList.size()==0? new Client(): clientList.get(0);
		getDomain().getClient().setName(client.getName());
		new ClientLogic().fromTrunk(getDomain().getClient(), client);
	}
	
	private void getSupplierSearchName(TextField input) {
		String name = input.getText();
		this.setIsDialogOpen(false);
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "SupplierQuery", 1|2, "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	protected void setSupplierSelect(List<Supplier> supplierList) {
		Supplier supplier = supplierList.size()==0? new Supplier(): supplierList.get(0);
		getDomain().setSupplier(supplier);
	}
	
	private void setBillTicketNumber() {
		this.getDomain().getBillTicket().genSerialNumber();
	}
	
	private ClientForm getClientForm() {
		ClientForm form = this.getAttr(ClientForm.class);
		if (form == null) {
			form = new ClientForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private SupplierForm getSupplierForm() {
		SupplierForm form = this.getAttr(SupplierForm.class);
		if (form == null) {
			form = new SupplierForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private WBillTicketForm getWBillTicketForm() {
		WBillTicketForm form = this.getAttr(WBillTicketForm.class);
		if (form == null) {
			form = new WBillTicketForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<BillTicketForm, BillDetail> getSelectFormer4Bill() {
		String k = "SelectFormer4Bill";
		SelectTicketFormer4Sql<BillTicketForm, BillDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<BillTicketForm, BillDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
}
