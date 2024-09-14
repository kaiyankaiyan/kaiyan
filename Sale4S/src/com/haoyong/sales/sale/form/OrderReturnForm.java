package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mily.http.Connection;
import net.sf.mily.support.form.SelectTicketFormer4Edit;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DoubleType;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.form.StorehouseForm;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.UserLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.ReceiptTicket;
import com.haoyong.sales.sale.domain.ReturnT;
import com.haoyong.sales.sale.domain.ReturnTicket;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.OrderDoptionLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.sale.logic.ReturnTicketLogic;
import com.haoyong.sales.test.base.ClientTest;

public class OrderReturnForm extends AbstractForm<OrderDetail> implements FViewInitable {
	
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
	
	private void beforeApply(IEditViewBuilder builder0) {
		TicketPropertyUtil.copyFieldsSkip(new OrderDetail(), this.getPurchaseDetail());
	}
	
	private void canEdit(List<List<Object>> valiRows) {
		// commName,uneditable
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (row.get(1) != null) {
				sb.append(row.get(0)).append(row.get(1)).append("\t");
			}
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}

	private void canApply(List<List<Object>> valiRows) {
		// commName,editable
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append(",");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void canAuditNo(List<List<Object>> valiRows) {
		// commName
		StringBuffer sb=new StringBuffer();
		if (StringUtils.isBlank(this.getPurchaseDetail().getReturnTicket().getRemark()))
			sb.append("请填写不通过原因，");
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
		// commName,editable
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
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
	
	private void prepareTicket() {
		setPurchaseDetailEmpty();
		TicketPropertyUtil.copyFieldsSkip(this.getSelectFormer4Purchase().getFirst(), this.getPurchaseDetail());
		this.getSelectEdit4Purchase().setSelectedList(new ArrayList<OrderDetail>(0));
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			new ReturnTicketLogic().getLocationChoosableLogic().fromTrunk(pur.getReturnTicket(), pur.getTReturn());
			OrderDetail spur = pur.getSnapShot();
			spur.setReturnTicket(pur.getReturnTicket());
		}
	}
	
	private void validateApply() throws Exception {
		StringBuffer sb=new StringBuffer(), sreturn=new StringBuffer();
		new ReturnTicketLogic().getLocationChoosableLogic().isValid(this.getPurchaseDetail().getReturnTicket(), sreturn);
		if ((sreturn = new StringBuffer(StringUtils.remove(sreturn.toString(), "货运单号不能为空，"))).length()>0)
			sb.append(sreturn).append("请补充退货单信息，");
		if (StringUtils.isBlank(this.getPurchaseDetail().getReturnTicket().getRemark())==true)
			sb.append("请填写退货原因，");
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private void validateOutstore() throws Exception {
		StringBuffer sb = new StringBuffer();
		if (new ReturnTicketLogic().getLocationChoosableLogic().isValid(this.getPurchaseDetail().getReturnTicket(), sb)==false)
			sb.append("请补充退货单信息，");
		if (StringUtils.isBlank(this.getPurchaseDetail().getReturnTicket().getRemark())==true)
			sb.append("请填写退货原因，");
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateReject4Apply() throws Exception {
		this.validateOutstore();
		if (StringUtils.isBlank(this.getPurchaseDetail().getLocationTicket().getIn().getName())==false && this.getPurchaseDetail().getLocationTicket().getIn().isStoreCheck()==false)
			throw new LogicException(2, "要验货的仓才需要申请");
	}
	private void validateReject4Receipt() throws Exception {
		this.validateOutstore();
		if (StringUtils.isBlank(this.getPurchaseDetail().getLocationTicket().getIn().getName())==false && this.getPurchaseDetail().getLocationTicket().getIn().isStoreCheck()==true)
			throw new LogicException(2, "要验货的仓需要去申请");
	}
	
	private void validateReceipt4Bad() {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getSelectEdit4Purchase().getSelectedList().size()==0)
			sb.append("请选择有次品数的到货明细，");
		for (OrderDetail detail: this.getSelectEdit4Purchase().getSelectedList()) {
			sitem = new StringBuffer();
			if (detail.getReceiptTicket().getBadAmount()==0)
				sitem.append("没有次品数，");
			if (detail.getReceiptTicket().getReceiptAmount()!=detail.getAmount())
				sitem.append("请先处理退货数差额，");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private void validateReceipt4Subtract() {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getSelectEdit4Purchase().getSelectedList().size()==0)
			sb.append("请选择有差额退货数的到货明细，");
		for (OrderDetail detail: this.getSelectEdit4Purchase().getSelectedList()) {
			sitem = new StringBuffer();
			if ((detail.getReceiptTicket().getReceiptAmount()<detail.getAmount())==false)
				sitem.append("没有退货数差额，");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	private void validateReceipt4Only() {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getSelectEdit4Purchase().getSelectedList().size()==0)
			sb.append("请选择全数退货的到货明细，");
		for (OrderDetail detail: this.getSelectEdit4Purchase().getSelectedList()) {
			sitem = new StringBuffer();
			if (detail.getReceiptTicket().getReceiptAmount()!=detail.getAmount())
				sitem.append("请先处理退货数差额，");
			if (detail.getReceiptTicket().getBadAmount()>0 && detail.getReceiptTicket().getBadAmount()!=detail.getAmount())
				sitem.append("请先拆分出次品数，");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void setApply4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	 	PropertyChoosableLogic.TicketDetail logic=new ReturnTicketLogic().getLocationChoosableLogic();
	 	for(OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
	 		OrderDetail spur = pur.getSnapShot();
	 		logic.fromTrunk(logic.getTicketBuilder(), pur.getReturnTicket(), this.getPurchaseDetail().getReturnTicket());
			pur.setTReturn((ReturnT)logic.toTrunk(pur.getReturnTicket()));
			pur.getLocationTicket().setOut(spur.getLocationTicket().getIn());
			pur.getLocationTicket().setIn(this.getPurchaseDetail().getLocationTicket().getIn());
			pur.getReceiptTicket().setReceiptAmount(0);
			pur.getReceiptTicket().setBadAmount(0);
	 	}
	}
	
	private void setOutstorePurchase4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	 	PropertyChoosableLogic.TicketDetail logic=new ReturnTicketLogic().getLocationChoosableLogic();
	 	for(OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
	 		logic.fromTrunk(logic.getTicketBuilder(), pur.getReturnTicket(), this.getPurchaseDetail().getReturnTicket());
			pur.setTReturn((ReturnT)logic.toTrunk(pur.getReturnTicket()));
			pur.setLocationTicket(this.getPurchaseDetail().getLocationTicket());
	 	}
	}
	
	private void setOutstoreOrder4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		for(OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail order = pur;
			viewData.getTicketDetails().add(order);
		}
	}
	
	private void setAuditNo4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	 	PropertyChoosableLogic.TicketDetail logic=new ReturnTicketLogic().getLocationChoosableLogic();
	 	for(OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
	 		pur.getReturnTicket().setRemark(new StringBuffer().append(pur.getReturnTicket().getRemark()).append(this.getPurchaseDetail().getReturnTicket().getRemark()).toString());
			pur.setTReturn((ReturnT)logic.toTrunk(pur.getReturnTicket()));
	 	}
	}
	
	private void setPurchase4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	}
	
	private void setReceiptOrder4Service(ViewData<OrderDetail> viewData) {
		PropertyChoosableLogic.TicketDetail logic=new ReturnTicketLogic().getLocationChoosableLogic();
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail spur = pur.getSnapShot();
			pur.setReturnTicket(pur.getReturnTicket());
			pur.getReturnTicket().setReturnDate(new Date());
			pur.getReturnTicket().setReturnAmount(pur.getAmount());
			pur.setTReturn((ReturnT)logic.toTrunk(pur.getReturnTicket()));
			list.add(pur);
		}
		viewData.setTicketDetails(list);
	}
	private void setReceiptBill4Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		List<BillDetail> disables=new ArrayList<BillDetail>(), rejects=new ArrayList<BillDetail>();
		for (OrderDetail purchase: this.getSelectFormer4Purchase().getSelectedList()) {
			BillDetail bill=purchase.getVoparam(BillDetail.class), sbill=bill==null? null: (BillDetail)bill.getSnapShot();
			if (bill==null) {
			} else if (sbill.getStBill()==20) {
				disables.add(bill);
				viewData.getTicketDetails().add(bill);
			} else if (sbill.getMoney()>0){// 新负数应收
				BillDetail reject = TicketPropertyUtil.copyProperties(bill, new BillDetail());
				reject.setMonthnum(purchase.getMonthnum());
				reject.setAmount(0 - sbill.getAmount());
				reject.getBillTicket().setBillDate(null);
				reject.setMoney(0 - sbill.getMoney());
				reject.getBillTicket().setDiffMoney(0 - sbill.getBillTicket().getDiffMoney());
				purchase.getVoParamMap().put("RejectBill", reject);
				rejects.add(reject);
				viewData.getTicketDetails().add(reject);
			}
		}
		viewData.setParam("DisableBills", disables);
		viewData.setParam("RejectBills", rejects);
	}
	
	private void setSplitNewService(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail nwPurchase = new PurchaseTicketLogic().genClonePurchase(pur);
			pur.getVoParamMap().put("NewPurchase", nwPurchase);
			nwPurchase.setMonthnum(new OrderTicketLogic().getSplitMonthnum(pur.getMonthnum()));
			nwPurchase.setAmount(this.getOrderDetail().getAmount());
			purList.add(nwPurchase);
		}
		viewData.setTicketDetails(purList);
	}
	
	private void setSplitRemainService(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			pur.setAmount(pur.getAmount() - this.getOrderDetail().getAmount());
			purList.add(pur);
		}
		viewData.setTicketDetails(purList);
	}
	
	private void getReceiptSplits() {
		List<String> monthnumList = new ArrayList<String>();
		this.setAttr("PreviousPurchaseList", this.getSelectEdit4Purchase().getSelectedList());
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			OrderDetail newPur = purchase.getVoparam("NewPurchase");
			monthnumList.add(purchase.getMonthnum());
			if (newPur!=null)
				monthnumList.add(newPur.getMonthnum());
		}
		ClientTest test = new ClientTest();
		test.loadFormView(this, "ReceiptList", "monthnum", monthnumList.toArray(new String[0]));
		test.setSqlAllSelect(test.getListViewValue().size());
		test.onMenu("确认到货");
	}
	
	private void setReceiptBadPurchase04Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			ReceiptTicket receipt = TicketPropertyUtil.copyProperties(purchase.getReceiptTicket(), new ReceiptTicket());
			OrderDetail bad = new PurchaseTicketLogic().genClonePurchase(purchase);
			new CommodityLogic().getPropertyChoosableLogic().trunkAppend(bad.getCommodity(), "次品");
			bad.setMonthnum(new OrderTicketLogic().getSplitMonthnum(purchase.getMonthnum()));
			bad.setAmount(receipt.getBadAmount());
			bad.getReceiptTicket().setReceiptAmount(bad.getAmount());
			bad.getReceiptTicket().setBadAmount(bad.getAmount());
			purchase.setAmount(purchase.getAmount() - bad.getAmount());
			purchase.getReceiptTicket().setReceiptAmount(receipt.getReceiptAmount() - bad.getAmount());
			purchase.getReceiptTicket().setBadAmount(0);
			purchase.getVoParamMap().put("NewPurchase", bad);
			purchase.getVoParamMap().put("NewBadMonthnum", bad.getMonthnum());
			purchase.getVoParamMap().put("NewBadCommodity", bad.getCommodity());
			viewData.getTicketDetails().add(purchase);
		}
	}
	private void setReceiptBadPurchase14Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			OrderDetail bad = purchase.getVoparam("NewPurchase");
			viewData.getTicketDetails().add(bad);
		}
	}
	private void setReceiptBadBill04Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		OrderDoptionLogic logic = new OrderDoptionLogic();
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			OrderDetail order=purchase, badOrder=purchase.getVoparam("NewPurchase");
			BillDetail bill = purchase.getVoparam(BillDetail.class);
			if (bill==null)
				continue;
			bill.setAmount(order.getAmount());
			bill.setMoney(order.getOrderTicket().getCmoney());
			if (logic.isPresent(order.getOrderTicket().getDoption())==true || bill.getMoney()==0) {
				bill.getBillTicket().setDiffMoney(bill.getMoney());
				bill.setMoney(0);
			}
			BillDetail sbill = bill.getSnapShot();
			BillDetail bad = TicketPropertyUtil.copyProperties(sbill, new BillDetail());
			bad.setAmount(badOrder.getAmount());
			bad.setMoney(badOrder.getOrderTicket().getCmoney());
			if (logic.isPresent(order.getOrderTicket().getDoption())==true || bad.getMoney()==0) {
				bad.getBillTicket().setDiffMoney(bad.getMoney());
				bad.setMoney(0);
			}
			bad.setMonthnum((String)purchase.getVoparam("NewBadMonthnum"));
			bad.setCommodity((Commodity)purchase.getVoparam("NewBadCommodity"));
			purchase.getVoParamMap().put("NewBill", bad);
			viewData.getTicketDetails().add(bill);
		}
	}
	private void setReceiptBadBill14Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			BillDetail bad = purchase.getVoparam("NewBill");
			if (bad==null)
				continue;
			bad.setCommodity((Commodity)purchase.getVoparam("NewBadCommodity"));
			viewData.getTicketDetails().add(bad);
		}
	}
	
	private void setReceiptSubtractPurchase04Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			OrderDetail spurchase = purchase.getSnapShot();
			ReceiptTicket receipt = TicketPropertyUtil.copyProperties(purchase.getReceiptTicket(), new ReceiptTicket());
			OrderDetail diff = new PurchaseTicketLogic().genClonePurchase(purchase);
			diff.setMonthnum(new OrderTicketLogic().getSplitMonthnum(purchase.getMonthnum()));
			diff.setAmount(spurchase.getAmount() - receipt.getReceiptAmount());
			diff.getReceiptTicket().setReceiptAmount(diff.getAmount());
			diff.getReceiptTicket().setBadAmount(0);
			purchase.setAmount(receipt.getReceiptAmount());
			purchase.getReceiptTicket().setReceiptAmount(purchase.getAmount());
			purchase.getVoParamMap().put("NewPurchase", diff);
			purchase.getVoParamMap().put("NewDiffMonthnum", diff.getMonthnum());
			viewData.getTicketDetails().add(purchase);
		}
	}
	private void setReceiptSubtractPurchase14Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			OrderDetail bad = purchase.getVoparam("NewPurchase");
			viewData.getTicketDetails().add(bad);
		}
	}
	private void setReceiptSubtractBill04Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		OrderDoptionLogic logic = new OrderDoptionLogic();
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			OrderDetail order=purchase, badOrder=purchase.getVoparam("NewPurchase");
			BillDetail bill = purchase.getVoparam(BillDetail.class);
			if (bill==null)
				continue;
			bill.setAmount(order.getAmount());
			bill.setMoney(order.getOrderTicket().getCmoney());
			if (logic.isPresent(order.getOrderTicket().getDoption())==true || bill.getMoney()==0) {
				bill.getBillTicket().setDiffMoney(bill.getMoney());
				bill.setMoney(0);
			}
			BillDetail sbill = bill.getSnapShot();
			BillDetail bad = TicketPropertyUtil.copyProperties(sbill, new BillDetail());
			bad.setAmount(badOrder.getAmount());
			bad.setMoney(badOrder.getOrderTicket().getCmoney());
			if (logic.isPresent(order.getOrderTicket().getDoption())==true || bad.getMoney()==0) {
				bad.getBillTicket().setDiffMoney(bad.getMoney());
				bad.setMoney(0);
			}
			bad.setMonthnum((String)purchase.getVoparam("NewDiffMonthnum"));
			purchase.getVoParamMap().put("NewBill", bad);
			viewData.getTicketDetails().add(bill);
		}
	}
	private void setReceiptSubtractBill14Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		for (OrderDetail purchase: this.getSelectEdit4Purchase().getSelectedList()) {
			BillDetail bad = purchase.getVoparam("NewBill");
			if (bad==null)
				continue;
			viewData.getTicketDetails().add(bad);
		}
	}
	
	private void setRejectApplyOrder4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
	}
	private void setRejectApplyPurchase4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		PropertyChoosableLogic.TicketDetail logic=new ReturnTicketLogic().getLocationChoosableLogic();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			order.getReturnTicket().setReturnAmount(order.getAmount());
	 		logic.fromTrunk(logic.getTicketBuilder(), order.getReturnTicket(), this.getPurchaseDetail().getReturnTicket());
			order.setTReturn((ReturnT)logic.toTrunk(order.getReturnTicket()));
			order.getLocationTicket().setOut(order.getLocationTicket().getIn());
			order.getLocationTicket().setIn(this.getPurchaseDetail().getLocationTicket().getIn());
			viewData.getTicketDetails().add(order);
		}
	}
	private void setRejectReceiptOrder4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
		PropertyChoosableLogic.TicketDetail logic=new ReturnTicketLogic().getLocationChoosableLogic();
		Date rturnDate = new Date();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail sorder = order.getSnapShot();
	 		logic.fromTrunk(logic.getTicketBuilder(), order.getReturnTicket(), this.getPurchaseDetail().getReturnTicket());
	 		order.getReturnTicket().setReturnAmount(sorder.getAmount());
	 		order.getReturnTicket().setReturnDate(rturnDate);
			order.setTReturn((ReturnT)logic.toTrunk(order.getReturnTicket()));
			order.getLocationTicket().setOut(order.getClient());
			order.getLocationTicket().setIn(this.getPurchaseDetail().getLocationTicket().getIn());
		}
	}
	private void setRejectReceiptPurchase4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		PropertyChoosableLogic.TicketDetail logic=new ReturnTicketLogic().getLocationChoosableLogic();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail sorder = order.getSnapShot();
			OrderDetail pur = order;
			pur.setReturnTicket(new ReturnTicket());
			pur.setTReturn((ReturnT)logic.toTrunk(pur.getReturnTicket()));
			pur.setAmount(sorder.getAmount());
			pur.getLocationTicket().setIn(this.getPurchaseDetail().getLocationTicket().getIn());
			viewData.getTicketDetails().add(pur);
		}
	}
	private void setRejectReceiptBill4Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		List<BillDetail> disables=new ArrayList<BillDetail>(), rejects=new ArrayList<BillDetail>();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			BillDetail bill=order.getVoparam(BillDetail.class);
			if (bill==null)
				continue;
			BillDetail sbill=bill.getSnapShot();
			if (sbill.getStBill()==20) {
				disables.add(bill);
				viewData.getTicketDetails().add(bill);
			} else if (sbill.getMoney()>0){// 新负数应收
				BillDetail reject = TicketPropertyUtil.copyProperties(bill, new BillDetail());
				reject.setMonthnum(order.getMonthnum());
				reject.setAmount(0 - sbill.getAmount());
				reject.getBillTicket().setBillDate(null);
				reject.setMoney(0 - sbill.getMoney());
				reject.getBillTicket().setDiffMoney(0 - sbill.getBillTicket().getDiffMoney());
				order.getVoParamMap().put("RejectBill", reject);
				rejects.add(reject);
				viewData.getTicketDetails().add(reject);
			}
		}
		viewData.setParam("DisableBills", disables);
		viewData.setParam("RejectBills", rejects);
	}
	private void setRejectSplitOrder04Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail sorder = order.getSnapShot();
			OrderDetail reject = new OrderTicketLogic().genCloneOrder(order);
			reject.setAmount(this.getOrderDetail().getAmount());
			reject.setMonthnum(new OrderTicketLogic().getSplitMonthnum(sorder.getMonthnum()));
			order.setAmount(sorder.getAmount() - reject.getAmount());
			order.getVoParamMap().put("NewOrder", reject);
		}
	}
	private void setRejectSplitOrder14Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail reject = order.getVoparam("NewOrder");
			viewData.getTicketDetails().add(reject);
		}
	}
	private void setRejectSplitBill04Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			BillDetail bill = order.getVoparam(BillDetail.class);
			bill.setAmount(order.getAmount());
			bill.setMoney(order.getOrderTicket().getCmoney());
			if (new OrderDoptionLogic().isPresent(order.getOrderTicket().getDoption())==true) {
				bill.getBillTicket().setDiffMoney(bill.getMoney());
				bill.setMoney(0);
			}
			viewData.getTicketDetails().add(bill);
		}
	}
	private void setRejectSplitBill14Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<BillDetail>());
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail rejectOrd = order.getVoparam("NewOrder");
			BillDetail bill=order.getVoparam(BillDetail.class), sbill=bill.getSnapShot();
			BillDetail reject = TicketPropertyUtil.copyProperties(bill, new BillDetail());
			reject.setAmount(rejectOrd.getAmount());
			reject.setMonthnum(rejectOrd.getMonthnum());
			reject.setMoney(sbill.getMoney() - bill.getMoney());
			reject.getBillTicket().setDiffMoney(sbill.getBillTicket().getDiffMoney() - bill.getBillTicket().getDiffMoney());
			order.getVoParamMap().put("NewBill", reject);
			viewData.getTicketDetails().add(reject);
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
		new ReturnTicketLogic().getLocationChoosableLogic().trunkViewBuilder(viewBuilder);
		new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
	}

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
	}
	
	private ReturnTicket getReturnTicket() {
		return new ReturnTicket();
	}
	
	private ChooseFormer getTicketChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new ReturnTicketLogic().getLocationChoosableLogic();
		former.setViewBuilder(logic.getTicketBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getTicketBuilder()));
		return former;
	}
	private StorehouseForm getStorehouseForm() {
		StorehouseForm form = this.getAttr(StorehouseForm.class);
		if (form == null) {
			form = new StorehouseForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<OrderReturnForm, OrderDetail> getSelectFormer4Order() {
		String k = "SelectFormer4Order";
		SelectTicketFormer4Sql<OrderReturnForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OrderReturnForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	public SelectTicketFormer4Sql<OrderReturnForm, OrderDetail> getSelectFormer4Purchase() {
		String k = "SelectFormer4Purchase";
		SelectTicketFormer4Sql<OrderReturnForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OrderReturnForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	public SelectTicketFormer4Edit<OrderReturnForm, OrderDetail> getSelectEdit4Purchase() {
		String k = "SelectEdit4Purchase";
		SelectTicketFormer4Edit<OrderReturnForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Edit<OrderReturnForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public OrderDetail getOrderDetail() {
		String k="OrderDetail";
		OrderDetail d = this.getAttr(k);
		if (d == null) {
			d = new OrderDetail();
			this.setAttr(k, d);
		}
		return d;
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
	private OrderReturnForm getTabOutstore() {
		return this;
	}
	private OrderReturnForm getTabReject() {
		return this;
	}
	
	private void setOrderDetailEmpty() {
		this.setAttr("OrderDetail", new OrderDetail());
	}
	
	private void setPurchaseDetailEmpty() {
		OrderDetail spur = this.getPurchaseDetail();
		this.setAttr("PurchaseDetail", new OrderDetail());
		this.getPurchaseDetail().getVoParamMap().put("SelfSnapShot", spur);
	}
	
	private void setReturnTicketNumber() {
		this.getPurchaseDetail().getReturnTicket().genSerialNumber();
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
	private HashMap<String, String> getParam4POutstore() {
		HashMap<String, String> map = new HashMap<String, String>();
		if (true) {
			StringBuffer sb = new StringBuffer();
			if (new UserLogic().isInstallRole(this.getUser()))
				sb.append("and c.outstore='").append(this.getUserName()).append("'");
			map.put("POutstore", sb.toString());
		}
		return map;
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
		this.getPurchaseDetail().getReturnTicket().setInName(client.getName());
		this.getPurchaseDetail().getLocationTicket().setIn(client);
	}
}
