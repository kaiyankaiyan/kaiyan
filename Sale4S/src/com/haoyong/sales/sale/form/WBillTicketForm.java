package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.mily.support.form.SelectTicketFormer4Edit;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.hibernate.util.JoinedIterator;

import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.WBillTicket;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.WBillTicketLogic;

public class WBillTicketForm extends AbstractForm<BillDetail> implements FViewInitable {

	private void beforeInBill(IEditViewBuilder buuilder0) {
		this.setDomain(new BillDetail());
		this.getDomain().getWBillTicket().setWbillDate(new Date());
	}

	private void beforeOutBill(IEditViewBuilder buuilder0) {
		this.setDomain(new BillDetail());
		this.getDomain().getWBillTicket().setWbillDate(new Date());
	}
	
	public void canReceiptAdd() {
		if (this.getDomain().getClient().getName()==null)
			throw new LogicException(2, "请先选择客户");
	}
	
	public void canPaidAdd() {
		if (this.getDomain().getSupplier().getName()==null)
			throw new LogicException(2, "请先选择供应商");
	}

	public void validateInBill() {
		StringBuffer sb=new StringBuffer();
		if (this.getSelectEdit4Reach().getSelectedList().size()==0)
			sb.append("请选择已收款明细，");
		if (this.getDetailList().size()==0)
			sb.append("待收明细为空！");
		if (this.getSelectEdit4Bill().getSelectedList().size()==0)
			sb.append("请选择应收明细，");
		if (new WBillTicketLogic().getPropertyChoosableLogic().isValid(this.getDomain().getWBillTicket(), sb)==false)
			sb.append("请补充单头内容，");
		if (sb.length()>0)			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	public void validateOutBill() {
		StringBuffer sb=new StringBuffer();
		if (this.getSelectEdit4Reach().getSelectedList().size()==0)
			sb.append("请选择已付款明细，");
		if (this.getDetailList().size()==0)
			sb.append("待付明细为空！");
		if (this.getSelectEdit4Bill().getSelectedList().size()==0)
			sb.append("请选择待付明细，");
		if (new WBillTicketLogic().getPropertyChoosableLogic().isValid(this.getDomain().getWBillTicket(), sb)==false)
			sb.append("请补充单头内容，");
		if (sb.length()>0)			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	@Override
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true) {
			new WBillTicketLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (true) {
			new SupplierLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (true) {
			new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
		if (true) {
			new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		}
	}
	
	public void setMoney() {
		if (true) {
			double m = 0;
			for (BillDetail d: this.getReachList()) {
				m += d.getMoney();
			}
			this.getDomain().getWBillTicket().setReachMoney(m);
		}
		if (true) {
			double m = 0;
			for (BillDetail d: this.getDetailList()) {
				m += d.getMoney();
			}
			this.getDomain().getWBillTicket().setWaitMoney(m);
		}
	}
	
	public void setReachDetail() {
		this.getReachList().removeAll(this.getSelectEdit4Reach().getSelectedList());
		this.getDetailList().removeAll(this.getSelectEdit4Bill().getSelectedList());
		this.beforeInBill(null);
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

	public List<BillDetail> getDetailList() {
		String k="DetailList";
		List<BillDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<BillDetail>();
			this.setAttr(k, list);
		}
		return list;
	}

	public List<BillDetail> getReachList() {
		String k="ReachList";
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
	
	private void setSplit4Service(ViewData<BillDetail> viewData) {
		double mreceipt = 0;
		for (Iterator<BillDetail> iter=this.getSelectEdit4Reach().getSelectedList().iterator(); iter.hasNext(); mreceipt+=iter.next().getMoney());
		Set<BillDetail> receiptList = new LinkedHashSet<BillDetail>();
		Set<BillDetail> waitList = new LinkedHashSet<BillDetail>();
		Iterator<BillDetail> receiptIter=this.getSelectEdit4Reach().getSelectedList().iterator(), waitIter=this.getSelectEdit4Bill().getSelectedList().iterator();
		for (; receiptIter.hasNext() && waitIter.hasNext();) { 
			BillDetail receipt=receiptIter.next(), wait=waitIter.next();
			receiptList.add(receipt);
			waitList.add(wait);
			if (Math.abs(receipt.getMoney()) > Math.abs(wait.getMoney())) {
				BillDetail remainReceipt = TicketPropertyUtil.copyProperties(receipt, new BillDetail());
				remainReceipt.setMoney(receipt.getMoney() - wait.getMoney());
				receipt.setMoney(wait.getMoney());
				List<BillDetail> remains = new ArrayList<BillDetail>();
				remains.add(remainReceipt);
				receiptIter = new JoinedIterator(remains.iterator(), receiptIter);
			} else if (Math.abs(receipt.getMoney()) < Math.abs(wait.getMoney())) {
				BillDetail remainWait = TicketPropertyUtil.copyProperties(wait, new BillDetail());
				remainWait.setMoney(wait.getMoney() - receipt.getMoney());
				wait.setMoney(receipt.getMoney());
				wait.setMonthnum(new OrderTicketLogic().getSplitMonthnum(wait.getMonthnum()));
				List<BillDetail> remains = new ArrayList<BillDetail>();
				remains.add(remainWait);
				waitIter = new JoinedIterator(remains.iterator(), waitIter);
			} else {
				receipt.setMoney(0);
			}
			wait.getBillTicket().setBillDate(receipt.getBillTicket().getBillDate());
			wait.getVoParamMap().put("Receipt", receipt);
		}
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		viewData.getTicketDetails().addAll(receiptList);
		for (; receiptIter.hasNext();) {
			viewData.getTicketDetails().add(receiptIter.next());
		}
		viewData.getTicketDetails().addAll(waitList);
		for (; waitIter.hasNext();) {
			viewData.getTicketDetails().add(waitIter.next());
		}
		double mwait = 0;
		for (Iterator<BillDetail> iter=waitList.iterator(); iter.hasNext(); mwait+=iter.next().getMoney());
		this.getDomain().getWBillTicket().setReachMoney(mreceipt);
		this.getDomain().getWBillTicket().setWaitMoney(mwait);
		this.getDomain().getWBillTicket().genSerialNumber();
		this.setAttr("ReceiptList", new ArrayList<BillDetail>(receiptList));
		this.setAttr("WaitList", new ArrayList<BillDetail>(waitList));
	}
	
	private void setReach4Service(ViewData<BillDetail> viewData) {
		List<BillDetail> receiptList = (List<BillDetail>)this.getAttr("ReceiptList");
		PropertyChoosableLogic.TicketDetail logic=new WBillTicketLogic().getPropertyChoosableLogic();
		for (BillDetail d: receiptList) {
			logic.fromTrunk(logic.getTicketBuilder(), d.getWBillTicket(), this.getDomain().getWBillTicket());
			d.setMoney(0);
		}
		viewData.setTicketDetails(receiptList);
	}
	
	private void setWait4Service(ViewData<BillDetail> viewData) {
		List<BillDetail> waitList = (List<BillDetail>)this.getAttr("WaitList");
		PropertyChoosableLogic.TicketDetail logic=new WBillTicketLogic().getPropertyChoosableLogic();
		for (BillDetail d: waitList) {
			logic.fromTrunk(logic.getTicketBuilder(), d.getWBillTicket(), this.getDomain().getWBillTicket());
		}
		viewData.setTicketDetails(waitList);
	}
	
	private WBillTicket getWBillTicket() {
		WBillTicket ticket = this.getAttr(WBillTicket.class);
		if (ticket==null) {
			ticket = new WBillTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	private void getClientSearchName(TextField input) {
		this.setIsDialogOpen(false);
		String name = input.getText();
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "ClientQuery", 0, "clientName", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	private void setClientSelect(List<BillDetail> clientList) {
		if (clientList.isEmpty())
			return ;
		BillDetail first = (BillDetail)clientList.get(0);
		this.getDomain().setClient(first.getClient());
		this.getDomain().getWBillTicket().setTypeName(first.getBillTicket().getTypeName());
		this.getReachList().clear();
		this.getDetailList().clear();
		this.getDetailList().addAll(clientList);
	}
	
	private void setReachSelected(List<BillDetail> selected) {
		List<BillDetail> selectedList = new ArrayList<BillDetail>(selected);
		selectedList.removeAll(this.getReachList());
		this.getReachList().addAll(selectedList);
	}
	
	private void getSupplierSearchName(TextField input) {
		String name = input.getText();
		this.setIsDialogOpen(false);
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "SupplierQuery", 0, "supplierName", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	private void setSupplierSelect(List<BillDetail> supplierList) {
		if (supplierList.isEmpty())
			return ;
		BillDetail first = (BillDetail)supplierList.get(0);
		this.getDomain().setSupplier(first.getSupplier());
		this.getDomain().getWBillTicket().setTypeName(first.getBillTicket().getTypeName());
		this.getReachList().clear();
		this.getDetailList().clear();
		this.getDetailList().addAll(supplierList);
	}
	
	private SelectTicketFormer4Sql<WBillTicketForm, BillDetail> getSelectFormer4Bill() {
		String k="SelectFormer4Bill";
		SelectTicketFormer4Sql<WBillTicketForm, BillDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<WBillTicketForm, BillDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Sql<WBillTicketForm, BillDetail> getSelectFormer4Reach() {
		String k="SelectFormer4Reach";
		SelectTicketFormer4Sql<WBillTicketForm, BillDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<WBillTicketForm, BillDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	public SelectTicketFormer4Edit<WBillTicketForm, BillDetail> getSelectEdit4Bill() {
		String k = "SelectEdit4Bill";
		SelectTicketFormer4Edit<WBillTicketForm, BillDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Edit<WBillTicketForm, BillDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	public SelectTicketFormer4Edit<WBillTicketForm, BillDetail> getSelectEdit4Reach() {
		String k = "SelectEdit4Reach";
		SelectTicketFormer4Edit<WBillTicketForm, BillDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Edit<WBillTicketForm, BillDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private ChooseFormer getChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new WBillTicketLogic().getPropertyChoosableLogic();
		former.setViewBuilder(logic.getTicketBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getTicketBuilder()));
		return former;
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
	
	private void setWBillTicketNumber() {
		this.getDomain().getWBillTicket().genSerialNumber();
	}
	
	private HashMap<String, String> getParam4Client() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("ClientName", new StringBuffer().append("'").append(new ClientLogic().getPropertyChoosableLogic().toTrunk(this.getDomain().getClient()).getClientName()).append("'").toString());
		return map;
	}
	
	private HashMap<String, String> getParam4Supplier() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("SupplierName", new StringBuffer().append("'").append(new SupplierLogic().getPropertyChoosableLogic().toTrunk(this.getDomain().getSupplier()).getSupplierName()).append("'").toString());
		return map;
	}
}
