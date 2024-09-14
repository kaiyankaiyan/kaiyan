package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mily.common.NoteAccessorFormer;
import net.sf.mily.http.Connection;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.ListType;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.webObject.AuditViewBuilder;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.View;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.logic.BillTypeLogic;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.listener.ActionService4LinkListener;
import com.haoyong.sales.common.listener.SelectDomainListener;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.SendTicket;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.ArrangeTypeLogic;
import com.haoyong.sales.sale.logic.BillTicketLogic;
import com.haoyong.sales.sale.logic.OrderDoptionLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.SendTicketLogic;
import com.haoyong.sales.test.base.AbstractTest.TestMode;
import com.haoyong.sales.test.base.ClientTest;
import com.haoyong.sales.test.sale.OrderTicketTest;
import com.haoyong.sales.test.sale.ReceiptTicketTest;


public class SendTicketForm extends AbstractForm<OrderDetail> implements FViewInitable {

	private List<OrderDetail> detailList;
	
	protected void beforeWindow(Window window) {
		super.beforeWindow(window);
		window.addJS("js/PrintModel.js");
	}
	
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
	
	private void prepareSend() {
		for (OrderDetail order: getSelectFormer4Order().getSelectedList()) {
			if (StringUtils.isBlank(order.getLocationTicket().getTo().getName())==false)
				order.getLocationTicket().setIn(order.getLocationTicket().getTo());
		}
		this.detailList = getSelectFormer4Order().getSelectedList();
	}
	
	private void preparePaid() {
		getSelectFormer4Order().getFirst().getVoparam(BillDetail.class);
		this.detailList = getSelectFormer4Order().getSelectedList();
		double money = 0;
		for (OrderDetail detail: this.getSelectFormer4Order().getSelectedList()) {
			BillDetail bill = detail.getVoparam(BillDetail.class);
			detail.setAmount(bill.getAmount());
			money += detail.getOrderTicket().getCmoney();
			detail.getVoparam(BillDetail.class).setMoney(0);
		}
		this.getDomain().getVoParamMap().put("waitMoney", money);
	}
	
	private void prepareRollback() {
		this.detailList = getSelectFormer4Order().getSelectedList();
	}
	
	private void preparePrintModel() {
		this.detailList = new ArrayList<OrderDetail>();
		OrderDetail detail = new OrderDetail();
		this.detailList.add(detail);
	}
	
	private void preparePrintOne() {
		this.detailList = new ArrayList<OrderDetail>();
		View view = (View)EntityClass.loadViewBuilder(this.getClass(), "ShowQuery").build(this);
		ListView listview = view.getComponent().getInnerFormerList(ListView.class).get(0);
		List<List<Object>> listvalue = (List<List<Object>>)listview.getValue();
		if (listvalue.size()==0)
			throw new LogicException(2, "查询中没有记录，无法打印预览!");
		this.detailList.addAll(new SelectDomainListener().toDomains(listvalue.subList(0, listvalue.size()>1? 2: 1), OrderDetail.class));
	}
	
	private void preparePrint(Component fcomp) {
		this.detailList = this.getSelectFormer4Order().getSelectedList();
		this.getPrintModelForm().showPrintOne(fcomp);
	}
	
	private void canSend(List<List<Object>> valiRows) {
		// commName,uneditable
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (row.get(1) != null) {
				sb.append(row.get(0)).append(row.get(1)).append("\t");
			}
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}
	
	private void canPaid(List<List<Object>> valiRows) {
		// commName,uneditable
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (row.get(1) != null) {
				sb.append(row.get(0)).append(row.get(1)).append("\t");
			}
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}
	
	private void canRollback(List<List<Object>> valiRows) {
		// commName,uneditable
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (row.get(1) != null) {
				sb.append(row.get(0)).append(row.get(1)).append("\t");
			}
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}
	
	public void validateSend4Full() {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getSelectedList().size()==0)
			sb.append("请选择全部发货的明细，");
		if (new SendTicketLogic().getSendChoosableLogic().isValid(this.getDomain().getSendTicket(), sb)==false)
			sb.append("请补充发货单信息，");
		for (OrderDetail detail: this.getSelectedList()) {
			sitem = new StringBuffer();
			if (detail.getSendTicket().getSendAmount()==0)
				sitem.append("请填写发货数量，");
			else if (detail.getSendTicket().getSendAmount() / detail.getAmount() < 1)
				sitem.append("请走部分发货提交，");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateSend4Part() {
		// 没有改单申请，有收货数量
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getSelectedList().size()==0)
			sb.append("请选择部分发货的明细，");
		for (OrderDetail detail: this.getSelectedList()) {
			sitem = new StringBuffer();
			if (detail.getSendTicket().getSendAmount()==0)
				sitem.append("请填写发货数量，");
			if (detail.getSendTicket().getSendAmount() / detail.getAmount() == 1)
				sitem.append("完整发货请按全部发货提交，");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validatePaid() {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (new BillTicketLogic().getPropertyChoosableLogic().isValid(this.getDomain().getVoparam(BillDetail.class).getBillTicket(), sb)==false)
			sb.append("请补充支付单信息，");
		for (OrderDetail detail: this.detailList) {
			sitem = new StringBuffer();
			BillDetail bill = detail.getVoparam(BillDetail.class);
			if (bill.getMoney()==0)
				sb.append("请填写支付金额，");
			else if (bill.getMoney()+bill.getBillTicket().getDiffMoney()!=detail.getOrderTicket().getCmoney())
				sb.append("支付金额+差异金额 要= 订单金额，");
			else if (bill.getBillTicket().getDiffMoney()<0)
				sb.append("差异金额不能<0，");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void setSendLocal4Service(ViewData<OrderDetail> viewData) {
		PropertyChoosableLogic.TicketDetail logic=new SendTicketLogic().getSendChoosableLogic();
		if (StringUtils.isBlank(this.getDomain().getLocationTicket().getIn().getName())==false)
			this.getDomain().getLocationTicket().genSerialNumber();
		for (OrderDetail send: this.getSelectedList()) {
			logic.fromTrunk(logic.getTicketBuilder(), send.getSendTicket(), this.getDomain().getSendTicket());
			send.getSendTicket().setSendDate(new Date());
			send.getLocationTicket().setNumber(this.getDomain().getLocationTicket().getNumber());
			send.getLocationTicket().setIn(this.getDomain().getLocationTicket().getIn());
		}
		viewData.setTicketDetails(this.getSelectedList());
	}
	private void setSendDown4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> downList = new ArrayList<OrderDetail>();
		OrderTicketLogic logic = new OrderTicketLogic();
		for (OrderDetail send: this.getSelectedList()) {
			OrderDetail downPur = send.getVoparam("DownPurchase");
			if ("下推生成的采购单".length()>0 && downPur!=null) {
				downList.add(downPur);
				downPur.setLocationTicket(send.getLocationTicket());
			} else if ("link分公司的采购单".length()>0) {
				List<OrderDetail> list = new OrderTicketTest().getOrderList(send.getClient().getFromSellerId(), "monthnum", new StringBuffer("like ").append(logic.getPrtMonthnum(send.getMonthnum())).append("%").toString());
				Assert.assertTrue("能找到下级采购订单", list.size()>0);
				for (OrderDetail d: list) {
					d.setLocationTicket(send.getLocationTicket());
				}
				downList.addAll(list);
				send.getVoParamMap().put("DownPurchase", list.get(0));
			}
		}
		viewData.setTicketDetails(downList);
	}
	
	private void setSendPurchase4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getSelectedList()) {
			OrderDetail pur=order, spur=pur.getSnapShot();
			if (spur.getStPurchase()==0)
				continue;
			order.getLocationTicket().setOut(pur.getLocationTicket().getIn());
			purList.add(pur);
		}
		viewData.setTicketDetails(purList);
	}
	
	private void setSendBill4Service(ViewData<BillDetail> viewData) {
		List<BillDetail> billList = new ArrayList<BillDetail>();
		OrderDoptionLogic logic = new OrderDoptionLogic();
		List<BillDetail> paidList=new ArrayList<BillDetail>(), presentList=new ArrayList<BillDetail>();
		for (OrderDetail order: this.getSelectedList()) {
			BillDetail bill = TicketPropertyUtil.copyProperties(order, new BillDetail());
			bill.setPrice(order.getOrderTicket().getCprice());
			bill.setMoney(order.getOrderTicket().getCmoney());
			bill.getBillTicket().setTypeName(new BillTypeLogic().getSaleType());
			order.setVoparam(bill);
			billList.add(bill);
			if (logic.isPaid(order.getOrderTicket().getDoption())==true) {
				bill.getBillTicket().setBillDate(new Date());
				paidList.add(bill);
			}
			if (logic.isPresent(order.getOrderTicket().getDoption())==true || bill.getMoney()==0) {
				bill.getBillTicket().setDiffMoney(bill.getMoney());
				bill.setMoney(0);
				bill.getBillTicket().setBillDate(new Date());
				presentList.add(bill);
			}
		}
		viewData.setParam("PaidList", paidList);
		viewData.setParam("PresentList", presentList);
		viewData.setTicketDetails(billList);
	}
	
	public void setSendStore4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getSelectedList()) {
			OrderDetail pur=order, spur=pur.getSnapShot();
			if (spur.getStPurchase()>0)
				list.add(pur);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setOutExtra4Service() {
		StoreTicketForm form = new StoreTicketForm();
		List<OrderDetail> itemList = new ArrayList<OrderDetail>();
		for (OrderDetail order: new ArrayList<OrderDetail>((List)this.getSelectedList())) {
			OrderDetail sorder = order.getSnapShot();
			if (sorder.getStPurchase()==0)
				itemList.add(order);
		}
		form.setOutExtra4Service(itemList, "instore", "null");
		this.setAttr(form);
		for (OrderDetail order: itemList) {
			order.getLocationTicket().setOut(order.getLocationTicket().getIn());
		}
	}
	
	private void setOutExtra4Service(ViewData<OrderDetail> viewData) {
	 	this.getAttr(StoreTicketForm.class).setOutExtra4Service(viewData);
	 	List<OrderDetail> wantoutList = this.getAttr(StoreTicketForm.class).getExtraOutstoreList();
	 	viewData.setParam("WantoutList", wantoutList);
	}
	
	private void setCompanyNormal4Service(ViewData<OrderDetail> viewData) {
		for (OrderDetail detail: this.getSelectFormer4Order().getSelectedList()) {
			detail.getArrangeTicket().setArrangeType(new ArrangeTypeLogic().getNormal());
		}
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
	}
	
	private ActionService4LinkListener getSubCompanyLink() {
		ActionService4LinkListener listener = new ActionService4LinkListener();
		this.setAttr(listener);
		Seller fromSeller = new Seller4lLogic().getSellerById(this.getDomain().getSubCompany().getFromSellerId());
		listener.getOnceAttributes().put("seller", fromSeller);
		User user = TicketPropertyUtil.copyProperties(this.getUser(), new User());
		user.setUserName(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
		listener.getOnceAttributes().put("user", user);
		return listener;
	}
	
	private void setPurchaseLink4Service(ViewData<OrderDetail> viewData) {
		Supplier supplier = new SupplierLogic().getSupplierByLink(this.getDomain().getSubCompany().getSubmitNumber());
		if (supplier==null)
			throw new LogicException(2, "在分公司没有本上级供应商!");
		TestMode[] fromModes = new ClientTest().getModeList().getModeList();
		HashMap<Set<TestMode>, List<OrderDetail>> modeList = new ClientTest().getSplitOrders(this.getSelectedList());
		for (Set<TestMode> kmode: modeList.keySet()) {
			new ClientTest().getModeList().setMode(kmode.toArray(new TestMode[0]));
			new ClientTest().getModeList().getSelfReceiptTest().linkClient待收货__1生成_2失效验证_3失效('1', modeList.get(kmode));
		}
		new ClientTest().getModeList().setMode(fromModes);
		viewData.setTicketDetails();
	}

	private void setSplitOrderCurSubtract4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		for (OrderDetail curOrdSubtract: this.getSelectedList()) {
			curOrdSubtract.getVoParamMap().put("SourceAmount", curOrdSubtract.getAmount());
			double subtractAmount = curOrdSubtract.getSendTicket().getSendAmount();
			double remainAmount = curOrdSubtract.getAmount() - subtractAmount;
			curOrdSubtract.getVoParamMap().put("NewRemainAmount", remainAmount);
			String curMonthnum = curOrdSubtract.getMonthnum();
			String subtractMonthnum = new OrderTicketLogic().getSplitMonthnum(curOrdSubtract.getMonthnum());
			OrderDetail nwOrdRemain = new OrderTicketLogic().genCloneOrder(curOrdSubtract);
			nwOrdRemain.setAmount(remainAmount);
			nwOrdRemain.setMonthnum(curMonthnum);
			nwOrdRemain.setSendTicket(new SendTicket());
			curOrdSubtract.getVoParamMap().put("NewRemainOrder", nwOrdRemain);
			curOrdSubtract.setAmount(subtractAmount);
			curOrdSubtract.setMonthnum(subtractMonthnum);
			ordList.add(curOrdSubtract);
		}
		viewData.setTicketDetails(ordList);
	}
	private void setSplitOrderNewRemain4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		for (OrderDetail curOrdSubtract: this.getSelectedList()) {
			OrderDetail nwOrdRemain = (OrderDetail)curOrdSubtract.getVoParamMap().get("NewRemainOrder");
			if (nwOrdRemain!=null)
				ordList.add(nwOrdRemain);
		}
		viewData.setTicketDetails(ordList);
	}
	
	private void setPaid4Service(ViewData<BillDetail> viewData) {
		PropertyChoosableLogic.TicketDetail logic=new BillTicketLogic().getPropertyChoosableLogic();
		List<BillDetail> list = new ArrayList<BillDetail>();
		BillDetail billDomain = this.getDomain().getVoparam(BillDetail.class);
		for (OrderDetail ordCur: this.getDetailList()) {
			BillDetail bill = ordCur.getVoparam(BillDetail.class);
			logic.fromTrunk(logic.getTicketBuilder(), bill.getBillTicket(), billDomain.getBillTicket());
			list.add(bill);
		}
		viewData.setTicketDetails(list);
	}

	private void setRejectSplitOrderCurSubtract4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		for (OrderDetail curOrdSubtract: this.getSelectFormer4Order().getASelects()) {
			double sourceAmount = curOrdSubtract.getAmount();
			double remainAmount = this.getOrderDetail().getReturnTicket().getReturnAmount();
			double subtractAmount = sourceAmount - remainAmount;
			curOrdSubtract.getVoParamMap().put("SourceAmount", sourceAmount);
			curOrdSubtract.getVoParamMap().put("NewRemainAmount", remainAmount);
			String curMonthnum = curOrdSubtract.getMonthnum();
			String remainMonthnum = new OrderTicketLogic().getSplitMonthnum(curOrdSubtract.getMonthnum());
			OrderDetail nwOrdRemain = new OrderTicketLogic().genCloneOrder(curOrdSubtract);
			nwOrdRemain.setAmount(remainAmount);
			nwOrdRemain.setMonthnum(remainMonthnum);
			curOrdSubtract.getVoParamMap().put("NewRemainOrder", nwOrdRemain);
			curOrdSubtract.getVoParamMap().put("NewRemainMonthnum", remainMonthnum);
			curOrdSubtract.setAmount(subtractAmount);
			curOrdSubtract.setMonthnum(curMonthnum);
			ordList.add(curOrdSubtract);
		}
		viewData.setTicketDetails(ordList);
	}
	private void setRejectSplitOrderNewRemain4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		for (OrderDetail curOrdSubtract: this.getSelectFormer4Order().getASelects()) {
			OrderDetail nwOrdRemain = (OrderDetail)curOrdSubtract.getVoParamMap().get("NewRemainOrder");
			if (nwOrdRemain!=null) {
				nwOrdRemain.setVoparam(curOrdSubtract);
				ordList.add(nwOrdRemain);
			}
		}
		viewData.setTicketDetails(ordList);
	}
	
	private void setRollbackPurchaseValidate4Service(ViewData<OrderDetail> viewData) {
		Supplier supplier = new SupplierLogic().getSupplierByLink(this.getDomain().getSubCompany().getSubmitNumber());
		if (supplier==null)
			throw new LogicException(2, "在分公司没有本上级供应商!");
		new ReceiptTicketTest().linkClient待收货__1生成_2失效验证_3失效('2', this.getSelectFormer4Order().getSelectedList());
		viewData.setTicketDetails();
	}
	private void setRollbackPurchaseLink4Service(ViewData<OrderDetail> viewData) {
		Supplier supplier = new SupplierLogic().getSupplierByLink(this.getDomain().getSubCompany().getSubmitNumber());
		if (supplier==null)
			throw new LogicException(2, "在分公司没有本上级供应商!");
		new ReceiptTicketTest().linkClient待收货__1生成_2失效验证_3失效('3', this.getSelectFormer4Order().getSelectedList());
		viewData.setTicketDetails();
	}
	private void setRollbackSend4Service(ViewData<OrderDetail> viewData) throws Exception {
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			order.getSendTicket().setSendDate(null);
			ArrayList<String> list = new ListType().parse(order.getOrderTicket().getDoption());
			if (list==null)
				continue;
			list.remove(new OrderDoptionLogic().getPaid());
			order.getOrderTicket().setDoption(new ListType().format(list));
		}
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
	}
	private void setRollbackPur4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			if (new ArrangeTypeLogic().isNormal(order.getArrangeTicket().getArrangeType())) {
				order.getArrangeTicket().setArrangeType(new DeliverTypeLogic().getCommonType());
				order.getLocationTicket().setIn(order.getLocationTicket().getOut());
				purList.add(order);
			}
		}
		viewData.setTicketDetails(purList);
	}
	private void setRollbackSendBill4Service(ViewData<BillDetail> viewData) {
		List<BillDetail> dieBills=new ArrayList<BillDetail>(), nwBills=new ArrayList<BillDetail>(), editBills=new ArrayList<BillDetail>();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			BillDetail bill=order.getVoparam(BillDetail.class), sbill=bill.getSnapShot();
			if (bill.getStBill()==20 || bill.getMoney()==0) {
				dieBills.add(bill);
			} else {// 应收减少，新负数应收，必走
				BillDetail nwbill = TicketPropertyUtil.copyProperties(bill, new BillDetail());
				nwbill.setAmount(0-order.getAmount());
				nwbill.getBillTicket().setBillDate(null);
				nwbill.setMoney(0-sbill.getMoney());
				nwBills.add(nwbill);
				bill.setMonthnum(new OrderTicketLogic().getSplitMonthnum(bill.getMonthnum()));
				editBills.add(bill);
				order.getVoParamMap().put("DiffBill", nwbill);
			}
		}
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		viewData.getTicketDetails().addAll(dieBills);
		viewData.getTicketDetails().addAll(nwBills);
		viewData.getTicketDetails().addAll(editBills);
		viewData.setParam("DieBillList", dieBills);
		viewData.setParam("DiffBillList", nwBills);
		viewData.setParam("EditBillList", editBills);
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
	private void setInsfState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (stateId > -1) {
				d.setInsfId(stateId);
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
	
	private void setSendUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUsend(suser);
		}
	}
	private void setReturnUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreturn(suser);
		}
	}
	
	private void setBillUser(ViewData<BillDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (BillDetail d: viewData.getTicketDetails()) {
			d.setUcreate(suser);
		}
	}
	private void addBillUser4Rollback(ViewData<BillDetail> viewData) {
		TicketUser user = genTicketUser();
		if (this.getDomain().getVoparam(BillDetail.class)==null)
			return;
		for (BillDetail d: viewData.getTicketDetails()) {
			user.setRemark(new StringBuffer("撤销发货_").append(((BillDetail)this.getDomain().getVoparam(BillDetail.class).getSnapShot()).getStBill()==30? "已收款": "未收款").toString());
			d.setUcreate(user.addUserDate(d.getUcreate()));
		}
	}
	private void addSendUser4Rollback(ViewData<OrderDetail> viewData) {
		TicketUser user = genTicketUser();
		if (this.getDomain().getVoparam(BillDetail.class)==null)
			return;
		for (OrderDetail d: viewData.getTicketDetails()) {
			user.setRemark(new StringBuffer("撤销发货_").append(((BillDetail)this.getDomain().getVoparam(BillDetail.class).getSnapShot()).getStBill()==30? "已收款": "未收款").toString());
			d.setUsend(user.addUserDate(d.getUsend()));
		}
	}

	private void setArrangeUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUarrange(suser);
		}
	}
	
	private void setReceiptUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreceipt(suser);
		}
	}

	public List<OrderDetail> getDetailList() {
		return this.detailList;
	}

	public OrderDetail getDomain() {
		return detailList.get(0);
	}
	
	public OrderDetail getOrderDetail() {
		String k="FormOrderDetail";
		OrderDetail d = this.getAttr(k);
		if (d==null) {
			d = new OrderDetail();
			this.setAttr(k, d);
		}
		return d;
	}
	
	@Override
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		new SendTicketLogic().getSendChoosableLogic().trunkViewBuilder(viewBuilder);
		new BillTicketLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		new OrderTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
	}
	
	private void setSendNumberNew() {
		this.getDomain().getSendTicket().genSerialNumber();
	}
	
	private void setBillTicketNumber() {
		this.getDomain().getVoparam(BillDetail.class).getBillTicket().genSerialNumber();
	}
	
	private void setDetailPaid() {
		for (OrderDetail d: this.getSelectedList()) {
			d.getVoparam(BillDetail.class).setMoney(d.getOrderTicket().getCmoney());
			d.getVoparam(BillDetail.class).getBillTicket().setDiffMoney(0);
		}
	}
	
	private List<OrderDetail> getSelectedList() {
		String k = "SelectedOrderList";
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
	
	public List<OrderDetail> getPurchaseList() {
		String k = "PurchaseList";
		List<OrderDetail> list = getAttr(k);
		if (list==null) {
			list = new ArrayList<OrderDetail>();
			setAttr(k, list);
		}
		return list;
	}
	
	public SelectTicketFormer4Sql<SendTicketForm, OrderDetail> getSelectFormer4Order() {
		String k = "SelectFormer4Order";
		SelectTicketFormer4Sql<SendTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<SendTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	private SelectTicketFormer4Sql<SendTicketForm, OrderDetail> getSelectFormer4Purchase() {
		String k = "SelectFormer4Purchase";
		SelectTicketFormer4Sql<SendTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<SendTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	private SelectTicketFormer4Sql<SendTicketForm, SubCompany> getSelectFormer4SubCompany() {
		String k="SelectFormer4SubCompany";
		SelectTicketFormer4Sql<SendTicketForm, SubCompany> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<SendTicketForm, SubCompany>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	private SelectTicketFormer4Sql<SendTicketForm, User> getSelectFormer4User() {
		String k="SelectFormer4User";
		SelectTicketFormer4Sql<SendTicketForm, User> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<SendTicketForm, User>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	private HashMap<String, String> getParam4PInstore() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (true) {
			StringBuffer sb = new StringBuffer();
			if (new UserLogic().isInstallRole(this.getUser()))
				sb.append("and c.instore='").append(this.getUserName()).append("'");
			map.put("PInstore", sb.toString());
		}
		return map;
	}
	private HashMap<String, String> getParam4COutstore() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (true) {
			StringBuffer sb = new StringBuffer();
			if (new UserLogic().isInstallRole(this.getUser()))
				sb.append("and c.outstore='").append(this.getUserName()).append("'");
			map.put("COutstore", sb.toString());
		}
		return map;
	}
	
	private void getStorehouseSearchName(TextField input) {
		this.setIsDialogOpen(false);
		if (StringUtils.isNotEmpty(input.getText())){
			String name = new StringBuffer().append("like %").append(input.getText()).append("%").toString();
			ClientTest test = new ClientTest();
			test.loadFormView(this, "StorehouseQuery", "userName", name);
			if (test.getListViewValue().size()==1) {
				test.setSqlAllSelect(1);
				test.onMenu("确定人员");
				return ;
			}
		}
		this.setIsDialogOpen(true);
	}

	private void setStorehouseUser(Component fcomp) {
		User sub = this.getSelectFormer4User().getFirst();
		this.getDomain().getLocationTicket().setIn(sub);
		if (this.hasLocationToName())
			this.getDomain().getLocationTicket().setTo(sub);
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
		for (OrderDetail detail: this.detailList) {
			money += detail.getPrice() * detail.getSendTicket().getSendAmount();
		}
		return money;
	}
	
	private boolean hasLocationToName() {
		ClientTest test = new ClientTest();
		test.loadFormView(new OrderTicketForm(), "Create");
		return test.hasField("locationTicket.to.name");
	}
	
	public void setCreateSelected(List<OrderDetail> orderList) {
		this.detailList.addAll(orderList);
		for (OrderDetail detail: orderList) {
			detail.setAmount(0.0);
		}
	}
	
	private SendTicket getSendTicket() {
		SendTicket ticket = this.getAttr(SendTicket.class);
		if (ticket == null) {
			ticket = new SendTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	private ChooseFormer getSendChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		SendTicketLogic logic = new SendTicketLogic();
		ViewBuilder standardBuilder = logic.getSendChoosableLogic().getTicketBuilder();
		SellerViewSetting setting = logic.getSendChoosableLogic().getChooseSetting( standardBuilder );
		former.setViewBuilder(standardBuilder);
		former.setSellerViewSetting(setting);
		return former;
	}
	
	private PrintModelForm getPrintModelForm() {
		PrintModelForm form = this.getAttr(PrintModelForm.class);
		if (form == null) {
			AuditViewBuilder builder = (AuditViewBuilder)EntityClass.loadViewBuilder(this.getClass(), "Print");
			if (true) {
				new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(builder);
			}
			if (true) {
				new SubCompanyLogic().getPropertyChoosableLogic().trunkViewBuilder(builder);
			}
			form = PrintModelForm.getForm(this, builder);
			this.setAttr(form);
		}
		return form;
	}
	
	private LocationTicketForm getLocationTicketForm() {
		LocationTicketForm form = getAttr(LocationTicketForm.class);
		if (form == null) {
			form = new LocationTicketForm();
			setAttr(form);
		}
		return form;
	}
	
	private NoteAccessorFormer<OrderDetail> getNoteFormer4Order() {
		String k = "NoteFormer4Order";
		NoteAccessorFormer accessor = this.getAttr(k);
		if (accessor==null) {
			accessor = new NoteAccessorFormer(OrderDetail.class);
			this.setAttr(k, accessor);
		}
		return accessor;
	}
	
	private void setDetailSnapShot() {
		for (OrderDetail curOrdSubtract: this.getSelectedList()) {
			curOrdSubtract.setSnapShot1();
		}
	}
	
	public ActionService4LinkListener getActionService4LinkListener() {
		return this.getAttr(ActionService4LinkListener.class);
	}
	
	private SendTicketForm getTabReject4Input() {
		return this;
	}
	
	private SendTicketForm getTabReject4Link() {
		return this;
	}
}
