package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mily.http.Connection;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DoubleType;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.listener.ActionService4LinkListener;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.LocationTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.haoyong.sales.sale.domain.ReturnT;
import com.haoyong.sales.sale.domain.ReturnTicket;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.sale.logic.ReturnTicketLogic;
import com.haoyong.sales.test.sale.OrderReturnTest;

public class PurchaseReturnForm extends AbstractForm<OrderDetail> implements FViewInitable {
	
	private void beforeWaiting(IEditViewBuilder builder0) {
		ViewBuilder builder = (ViewBuilder)builder0;
		for (boolean one="待处理提醒的记录".length()>0; one; one=false) {
			List<SqlListBuilder> sqlList = builder.getFieldBuildersDeep(SqlListBuilder.class);
			Connection conn = (Connection)WindowMonitor.getMonitor().getAttribute("conn");
			if (conn==null || sqlList.size()==0)
				break;
			SqlListBuilder sqlBuilder = sqlList.get(0);
			String rid=sqlBuilder.getAttribute(ParameterName.Remind, ParameterName.ID), sid=sqlBuilder.getAttribute(ParameterName.Select, ParameterName.ID);
			Set<String> klist = new HashSet<String>();
			if (rid!=null) {
				klist.add(rid);
			} else if (sid!=null) {
				klist.add(sid);
			} else {
				break;
			}
			if ("1".equals(conn.getParameterMap().get("wait"))) {
				String value=sqlBuilder.getAttribute(ParameterName.Remind, ParameterName.Value);
				HashMap<String, String> filters = new HashMap<String, String>();
				filters.put(rid, value);
				this.getSearchSetting(sqlBuilder).addFilters(filters);
				klist.addAll(filters.keySet());
			} else if ("1".equals(conn.getParameterMap().get("global"))) {
				String value=sqlBuilder.getAttribute(ParameterName.Select, ParameterName.Value);
				HashMap<String, String> filters = new HashMap<String, String>();
				filters.put(sid, value);
				for (Map.Entry<String, String> entry: new GlobalSearchForm().getDomain().getInputs().entrySet()) {
					if (StringUtils.isBlank(entry.getValue()) == false) {
						filters.put(entry.getKey(), entry.getValue());
					}
				}
				this.getSearchSetting(sqlBuilder).addFilters(filters);
				klist.addAll(filters.keySet());
			}
		}
	}

	private void canApply(List<List<Object>> valiRows) {
		// commName,uneditable,stOrder
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if (((Integer)row.get(2))>0)
				sitem.append("退货库存不应有客户订单占用，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void canAuditNoConfirm(List<List<Object>> valiRows) {
		// commName,stateId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if ((Integer)row.get(1)!=72)
				sitem.append("非审核不通过订单不用确认，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void canOutstoreYes(List<List<Object>> valiRows) {
		// commName,uneditable,stOrder
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if (((Integer)row.get(2))>0)
				sitem.append("退货库存不应有客户订单占用，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void canSplit(List<List<Object>> valiRows) throws Exception {
		// commName,amount
		StringBuffer sb=new StringBuffer();
		if (valiRows.size()==0)
			sb.append("请选择采购单！");
		for (List<Object> row: valiRows) {
			double amount = new DoubleType().parse(row.get(1).toString());
			if (!(0<getOrderDetail().getAmount() && getOrderDetail().getAmount()<amount))
				sb.append("请填写合理的拆分数量，");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void prepareTicket() throws Exception {
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			new ReturnTicketLogic().getPurchaseChoosableLogic().fromTrunk(pur.getReturnTicket(), pur.getTReturn());
		}
	}
	
	private void validateApply() throws Exception {
		StringBuffer sb = new StringBuffer();
		if (new ReturnTicketLogic().getPurchaseChoosableLogic().isValid(this.getPurchaseDetail().getReturnTicket(), sb)==false)
			sb.append("请补充退货信息，");
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateOutstore() throws Exception {
		StringBuffer sb = new StringBuffer();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			pur.getReturnTicket().setDeliverNum(this.getReturnTicket().getDeliverNum());
			if (new ReturnTicketLogic().getPurchaseChoosableLogic().isValid(pur.getReturnTicket(), sb)==false)
				sb.append("请补充退货单信息，");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void setApplyPurchase4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	 	PropertyChoosableLogic.TicketDetail logic = new ReturnTicketLogic().getPurchaseChoosableLogic();
	 	for(OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
	 		logic.fromTrunk(logic.getTicketBuilder(), pur.getReturnTicket(), this.getPurchaseDetail().getReturnTicket());
	 		pur.setTReturn(new ReturnTicketLogic().getPurchaseChoosableLogic().toTrunk(pur.getReturnTicket()));
	 	}
	}
	private void setApplyOrder4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		LocationTicket tlocation = new LocationTicket();
		tlocation.genSerialNumber();
		OrderTicket torder = new OrderTicket();
		torder.genSerialNumber();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			if (pur.getLocationTicket().getIn().isStoreCheck()==false)
				continue;
			OrderDetail order = pur;
			order.setLocationTicket(tlocation);
			order.getLocationTicket().setOut(pur.getLocationTicket().getIn());
			order.getLocationTicket().setIn(pur.getSupplier());
			viewData.getTicketDetails().add(order);
		}
	}
	
	private void setPurchase4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	}
	
	private void setBill4Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		List<BillDetail> diffBills = new ArrayList<BillDetail>();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			BillDetail bill=pur.getVoparam(BillDetail.class);
			if (bill.getStBill()==30) {
				BillDetail diffbill=TicketPropertyUtil.copyProperties(bill, new BillDetail());
				diffbill.setAmount(pur.getAmount());
				diffbill.setMoney(0-pur.getPurchaseTicket().getPmoney());
				viewData.getTicketDetails().add(diffbill);
				diffBills.add(diffbill);
			}
			viewData.getTicketDetails().add(bill);
		}
		viewData.setParam("DiffBillList", diffBills);
	}
	
	private void setOutstoreYes4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	 	this.getPurchaseDetail().getReturnTicket().genSerialNumber();
	 	PropertyChoosableLogic.TicketDetail logic=new ReturnTicketLogic().getPurchaseChoosableLogic();
	 	for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
	 		logic.fromTrunk(logic.getTicketBuilder(), pur.getReturnTicket(), this.getPurchaseDetail().getReturnTicket());
			pur.setTReturn((ReturnT)logic.toTrunk(pur.getReturnTicket()));
			pur.getReturnTicket().setReturnAmount(pur.getAmount());
			pur.setAmount(0.0);
			pur.getReturnTicket().setReturnDate(new Date());
			pur.setTReturn(new ReturnTicketLogic().getPurchaseChoosableLogic().toTrunk(pur.getReturnTicket()));
	 	}
	}
	
	private void setOutstore4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	}
	
	private ActionService4LinkListener getOrderLink() {
		ActionService4LinkListener listener = new ActionService4LinkListener();
		Seller toSeller = new Seller4lLogic().getSellerById(this.getSelectFormer4Purchase().getFirst().getSupplier().getToSellerId());
		listener.getOnceAttributes().put("seller", toSeller);
		User user = TicketPropertyUtil.copyProperties(this.getUser(), new User());
		user.setUserName(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
		listener.getOnceAttributes().put("user", user);
		this.setAttr(listener);
		return listener;
	}
	
	private void setOrderLink4Service_validate(ViewData<OrderDetail> viewData) {
		List<OrderDetail> rejectList = new ArrayList<OrderDetail>();
		OrderDetail first=this.getSelectFormer4Purchase().getFirst(), sfirst=first.getSnapShot();
		for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail sdetail = detail.getSnapShot();
			rejectList.add(sdetail);
		}
		new OrderReturnTest().linkSupplier拒收开退货入库申请_1拆分出退数_2退货申请无事务('1', rejectList);
		viewData.setTicketDetails();
	}
	private void setOrderLink4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> rejectList = new ArrayList<OrderDetail>();
		OrderDetail first=this.getSelectFormer4Purchase().getFirst(), sfirst=first.getSnapShot();
		for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail sdetail = detail.getSnapShot();
			rejectList.add(sdetail);
		}
		new OrderReturnTest().linkSupplier拒收开退货入库申请_1拆分出退数_2退货申请无事务('2', rejectList);
		viewData.setTicketDetails();
	}
	
	private void setSplitNew4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail nwPur = new PurchaseTicketLogic().genClonePurchase(pur);
			pur.getVoParamMap().put("NewPurchase", nwPur);
			nwPur.setMonthnum(new OrderTicketLogic().getSplitMonthnum(pur.getMonthnum()));
			nwPur.setAmount(this.getOrderDetail().getAmount());
			purList.add(nwPur);
		}
		viewData.setTicketDetails(purList);
	}
	
	private void setSplitRemain4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			pur.setAmount(pur.getAmount() - this.getOrderDetail().getAmount());
			purList.add(pur);
		}
		viewData.setTicketDetails(purList);
	}
	
	private void setSplitNew4BillService(ViewData<BillDetail> viewData) {
		List<BillDetail> billList = new ArrayList<BillDetail>();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail nwPur = (OrderDetail)pur.getVoParamMap().get("NewPurchase");
			BillDetail bill=pur.getVoparam(BillDetail.class), nwBill=TicketPropertyUtil.copyProperties(bill, new BillDetail());
			pur.getVoParamMap().put("NewBill", nwBill);
			nwBill.setMonthnum(nwPur.getMonthnum());
			nwBill.setAmount(this.getOrderDetail().getAmount());
			nwBill.setMoney(nwPur.getPurchaseTicket().getPmoney());
			billList.add(nwBill);
		}
		viewData.setTicketDetails(billList);
	}
	
	private void setSplitRemain4BillService(ViewData<BillDetail> viewData) {
		List<BillDetail> billList = new ArrayList<BillDetail>();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			BillDetail bill=pur.getVoparam(BillDetail.class);
			bill.setAmount(pur.getAmount());
			bill.setMoney(pur.getPurchaseTicket().getPmoney());
			billList.add(bill);
		}
		viewData.setTicketDetails(billList);
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
	private void setOutsfState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setOutsfId(stateId);
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
	
	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	private void setReturnUser(ViewData<OrderDetail> viewData) {
		TicketUser user = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreturn(user.getUserDate());
		}
	}
	
	private void addReturnUser(ViewData<OrderDetail> viewData) {
		TicketUser user = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreturn(user.addUserDate(d.getUreturn()));
		}
	}

	@Override
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true) {
			new ReturnTicketLogic().getPurchaseChoosableLogic().trunkViewBuilder(viewBuilder);
		}
	}

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
	}
	
	private ReturnTicket getReturnTicket() {
		return new ReturnTicket();
	}
	
	private ChooseFormer getTicketChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new ReturnTicketLogic().getPurchaseChoosableLogic();
		former.setViewBuilder(logic.getTicketBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getTicketBuilder()));
		return former;
	}
	
	public SelectTicketFormer4Sql<PurchaseReturnForm, OrderDetail> getSelectFormer4Purchase() {
		String k = "SelectFormer4Purchase";
		SelectTicketFormer4Sql<PurchaseReturnForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<PurchaseReturnForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public OrderDetail getOrderDetail() {
		String k = "OrderDetail";
		OrderDetail d = this.getAttr(k);
		if (d == null) {
			d = new OrderDetail();
			this.setAttr(k, d);
		}
		return d;
	}
	
	private void setOrderDetailEmpty() {
		this.setAttr("OrderDetail", new OrderDetail());
	}
	
	public OrderDetail getPurchaseDetail() {
		String k = "PurchaseDetail";
		OrderDetail d = this.getAttr(k);
		if (d == null) {
			d = new OrderDetail();
			this.setAttr(k, d);
		}
		return d;
	}
	
	private void setPurchaseDetailEmpty() {
		OrderDetail pre = this.getPurchaseDetail();
		this.setAttr("PurchaseDetail", new OrderDetail());
		this.getPurchaseDetail().getVoParamMap().put("SelfSnapShot", pre);
	}
	
	private void setReturnTicketNumber() {
		this.getPurchaseDetail().getReturnTicket().genSerialNumber();
	}
	
	public ActionService4LinkListener getActionService4LinkListener() {
		return this.getAttr(ActionService4LinkListener.class);
	}
}
