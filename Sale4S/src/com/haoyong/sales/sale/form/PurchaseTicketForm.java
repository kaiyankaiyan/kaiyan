package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.mily.attributes.ClientEventName;
import net.sf.mily.common.NoteAccessorFormer;
import net.sf.mily.http.Connection;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.support.form.SelectTicketFormer4Edit;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DateType;
import net.sf.mily.types.DoubleType;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Container;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.Text;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.webObject.AddNoteListener;
import net.sf.mily.webObject.AuditViewBuilder;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.EditViewBuilder;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.View;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.query.SqlListBuilderSetting;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.hibernate.util.JoinedIterator;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.form.CommodityForm;
import com.haoyong.sales.base.form.SupplierForm;
import com.haoyong.sales.base.form.SupplyTypeForm;
import com.haoyong.sales.base.logic.BomTicketLogic;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SellerViewInputsLogic;
import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.base.logic.SubmitTypeLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FSqlListRemindable;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.listener.ActionService4LinkListener;
import com.haoyong.sales.common.listener.SelectDomainListener;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.ArrangeT;
import com.haoyong.sales.sale.domain.ArrangeTicket;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.haoyong.sales.sale.domain.PurchaseT;
import com.haoyong.sales.sale.domain.PurchaseTicket;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.ArrangeTicketLogic;
import com.haoyong.sales.sale.logic.ArrangeTypeLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.OrderTypeLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.sale.logic.ReceiptTicketLogic;
import com.haoyong.sales.test.base.AbstractTest.TestMode;
import com.haoyong.sales.test.base.ClientTest;

public class PurchaseTicketForm extends AbstractForm<OrderDetail> implements FViewInitable, FSqlListRemindable {

	private List<OrderDetail> detailList;
	
	protected void beforeWindow(Window window) {
		super.beforeWindow(window);
		window.addJS("js/PrintModel.js");
	}
	
	protected void beforeWaiting(IEditViewBuilder builder0) {
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
	
	private void prepareChange() {
		this.getOrderList().clear();
		this.getOrderList().addAll( getSelectFormer4Order().getSelectedList());
		this.getOrderFirst().setChangeRemark(null);
		this.setAttr("ChangeList", null);
	}
	
	private void prepareRechange() {
		this.detailList = getSelectFormer4Purchase().getSelectedList();
		this.setDomain(getSelectFormer4Purchase().getFirst());
	}
	
	private void prepareRepurchase() {
		this.detailList = getSelectFormer4Purchase().getSelectedList();
		this.setDomain(getSelectFormer4Purchase().getFirst());
	}
	
	private void prepareDoadjust() {
		this.detailList = getSelectFormer4Purchase().getSelectedList();
		this.setDomain(getSelectFormer4Purchase().getFirst());
	}
	
	private void prepareShowQuery() {
		Set<String> monthnumList = new LinkedHashSet<String>();
		for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
			monthnumList.add(detail.getMonthnum().split("\\-")[0]);
		}
		StringBuffer sb = new StringBuffer();
		for (String m: monthnumList) {
			sb.append(m).append(" ");
		}
		HashMap<String, String> filters = new HashMap<String, String>();
		filters.put("monthnum", sb.toString());
		SqlListBuilderSetting setting = this.getSearchSetting((SqlListBuilder)EntityClass.loadViewBuilder(this.getClass(), "ShowQuery").getFieldBuildersDeep(SqlListBuilder.class).get(0));
		setting.addFilters(filters);
	}
	
	private void prepareShow() {
		this.detailList = getSelectFormer4Purchase().getSelectedList();
		this.setDomain(getSelectFormer4Purchase().getFirst());
		this.getSearchSetting((SqlListBuilder)EntityClass.loadViewBuilder(this.getClass(), "ShowQuery").getFieldBuildersDeep(SqlListBuilder.class).get(0));
	}
	
	private void prepareAdjust() {
		this.getOrderList().clear();
		this.getOrderList().addAll( getSelectFormer4Order().getSelectedList());
	}
	
	private void prepareTicket() {
		this.getSelectFormer4Purchase().setSelectedList(null);
		this.detailList = this.getSelectFormer4Order().getSelectedList();
		this.setDomain(this.getDetailList().get(0));
	}
	
	private void prepareLocalAudit() {
		this.getSelectFormer4Order().setSelectedList(null);
		this.detailList = this.getSelectFormer4Purchase().getSelectedList();
		this.setDomain(this.getSelectFormer4Purchase().getFirst());
	}
	
	private void prepareTicket4Bom() {
		this.detailList = new ArrayList<OrderDetail>();
		PropertyChoosableLogic.TicketDetail logic=new OrderTicketLogic().getTicketChoosableLogic();
		for (BomDetail bom: this.getSelectFormer4Bom().getSelectedList()) {
			OrderDetail order = bom.getVoparam(OrderDetail.class);
			OrderDetail detail = TicketPropertyUtil.copyProperties(bom, new OrderDetail());
			detail.setOrderTicket(order.getOrderTicket());
			detail.getOrderTicket().setOrderType(new OrderTypeLogic().getBomType());
			detail.getArrangeTicket().setArrangeType(new DeliverTypeLogic().getCommonType());
			detail.setMonthnum(new OrderTicketLogic().getBomMonthnum(order.getMonthnum()));
			detail.setVoparam(order);
			detail.setVoparam(bom);
			this.getDetailList().add(detail);
		}
		this.setDomain(this.getDetailList().get(0));
	}
	
	public void prepareImport() {
		this.setDomain(new OrderDetail());
		this.getDomain().getVoParamMap().put("note", "对应列序号");
		this.getImportList().clear();
	}
	
	private void preparePrintModel() {
		this.detailList = new ArrayList<OrderDetail>();
		OrderDetail detail = new OrderDetail();
		this.detailList.add(detail);
		this.setDomain(detail);
	}
	
	private void preparePrintOne() {
		this.detailList = new ArrayList<OrderDetail>();
		View view = (View)EntityClass.loadViewBuilder(this.getClass(), "ShowQuery").build(this);
		ListView listview = view.getComponent().getInnerFormerList(ListView.class).get(0);
		List<List<Object>> listvalue = (List<List<Object>>)listview.getValue();
		if (listvalue.size()==0)
			throw new LogicException(2, "查询中没有记录，无法打印预览!");
		this.detailList.addAll(new SelectDomainListener().toDomains(listvalue.subList(0, listvalue.size()>1? 2: 1), OrderDetail.class));
		this.setDomain(this.detailList.get(0));
	}
	
	private void preparePrint(Component fcomp) {
		this.setDomain(this.getSelectFormer4Purchase().getFirst());
		this.detailList = this.getSelectFormer4Purchase().getSelectedList();
		this.getPrintModelForm().showPrintOne(fcomp);
	}
	
	private void canTicket(List<List<Object>> valiRows) {
		// commName,uneditable,stateId,arrangeId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void canLocalConfirm(List<List<Object>> valiRows) {
		// commName,purStateId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (Integer.parseInt(row.get(1)+"")!=10)
				sitem.append("非审核不通过返回的单，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void canChange(List<List<Object>> valiRows) {
		// commName,uneditable,stateId,arrangeId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (!(Integer.parseInt(row.get(2)+"")==30 && Integer.parseInt(row.get(3)+"")==30))
				sb.append(row.get(0)).append(row.get(1)).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}
	
	private void canChangeConfirm(List<List<Object>> valiRows) {
		// commName,stateId,arrangeId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (!(Integer.parseInt(row.get(1)+"")==42 || Integer.parseInt(row.get(2)+"")==42)) {
				sb.append(row.get(0)).append("\t");
			}
		}
		if (sb.length() > 0) {
			sb.append("不用改单申请确认！");
			throw new LogicException(2, sb.toString());
		}
	}
	
	private void canAdjust(List<List<Object>> valiRows) {
		// commName,ordStateId,arrangeId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (!(Integer.parseInt(row.get(1)+"")==50 || Integer.parseInt(row.get(2)+"")==50))
				sitem.append("不用红冲处理，");
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0) {
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		}
	}
	
	private void canRechange(List<List<Object>> valiRows) {
		// commName,stateId,ordStateId,arrangeId
		StringBuffer sb=new StringBuffer();
		for (List<Object> row: valiRows) {
			if (Integer.parseInt(row.get(1)+"")==40 || Integer.parseInt(row.get(1)+"")==45) {
			} else if (Integer.parseInt(row.get(2)+"")==45) {
			} else if (Integer.parseInt(row.get(3)+"")==45) {
			} else {
				sb.append(row.get(0)).append("不用改单申请处理\t");
			}
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void canRepurchase(List<List<Object>> valiRows) {
		// commName,stateId
		StringBuffer sb=new StringBuffer();
		for (List<Object> row: valiRows) {
			if (Integer.parseInt(row.get(1)+"")==20) {
			} else {
				sb.append(row.get(0)).append("不用供应商返单处理\t");
			}
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void canReedit(List<List<Object>> valiRows) {
		// commName,stateId,receiptId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (Integer.parseInt(row.get(1)+"")>=50)
				sitem.append("有红冲，");
			else if (Integer.parseInt(row.get(1)+"")>=40)
				sitem.append("有改单，");
			if (Integer.parseInt(row.get(2)+"")>0)
				sitem.append("已收货请走红冲，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.insert(0, "不能未收货编辑").deleteCharAt(sb.length()-1).toString());
	}

	private void canDoadjust(List<List<Object>> valiRows) {
		// commName,stateId,receiptId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (Integer.parseInt(row.get(1)+"")>=40)
				sitem.append("有改单，");
			if (Integer.parseInt(row.get(2)+"")==0)
				sitem.append("未收货请走编辑，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.insert(0, "不能红冲开单").deleteCharAt(sb.length()-1).toString());
	}

	private void canDoadjustConfirm(List<List<Object>> valiRows) {
		// commName,stateId
		StringBuffer sb=new StringBuffer();
		for (List<Object> row: valiRows) {
			if (Integer.parseInt(row.get(1)+"")!=52) {
				sb.append(row.get(0)).append("不用红冲不通过确认\t");
			}
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

	private void canCancelYes(List<List<Object>> valiRows) {
		// commName,stOrder
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (valiRows.size()==0)
			sb.append("请选择采购单！");
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (((Integer)row.get(1))>0)
				sitem.append("取消审核的采购不应有订单，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void canReturnYes(List<List<Object>> valiRows) {
		// commName,stOrder
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (valiRows.size()==0)
			sb.append("请选择采购单！");
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (((Integer)row.get(1))>0)
				sitem.append("退货审核的采购不应有客户订单，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem.deleteCharAt(sitem.length()-1)).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}

	private void validateChangeApply() {
		StringBuffer sb = new StringBuffer();
		List<String> changeList = getChangeList();
		if (changeList.isEmpty()) {
			sb.append("请选择更改单据类型！");
		}
		OrderTicketLogic logic = new OrderTicketLogic();
		for (OrderDetail detail: this.getOrderList()) {
			if (changeList.contains("排单") && logic.getVoNoteMap(getNoteFormer4Order(), detail, ArrangeTicket.class).isEmpty()) {
				sb.append("请填写排单更改的内容！");
			}
			if (changeList.contains("订单") && logic.getVoNoteMap(getNoteFormer4Order(), detail, OrderTicket.class).isEmpty()) {
				sb.append("请填写订单更改的内容！");
			}
			if (!getNoteFormer4Order().isChangedRemark(getOrderFirst())) {
				sb.append("请补充改单申请备注！");
			}
			getNoteFormer4Order().isChangedNotesEX(detail);
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	protected void validatePurchase() {
		StringBuffer sb = new StringBuffer();
		if (this.getClass()==PurchaseTicketForm.class && new SupplierLogic().getPropertyChoosableLogic().isValid(this.getDomain().getSupplier(), sb)==false)
			sb.append("请填写供应商信息，");
		if (getPurchaseChoosable().isValid(this.getDomain().getPurchaseTicket(), sb)==false)
			sb.append("请补充单头，");
		for (OrderDetail detail: this.getDetailList()) {
			if (detail.getPrice()==0)
				sb.append("请填写价格，");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateLocalYes() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isBlank(this.getDomain().getChangeRemark())==false)
			sb.append("不通过原因，");
		boolean change = false;
		for (OrderDetail detail: this.getDetailList()) {
			change = change || this.getNoteFormer4Purchase().getVoNoteMap(detail).size()>0;
		}
		if (change)
			sb.append("采购更改内容，");
		if (sb.length()>0)
			throw new LogicException(2, sb.insert(0, "当地购通过不应有：").deleteCharAt(sb.length()-1).toString());
		validatePurchase();
	}
	
	private void validateLocalNo() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isBlank(this.getDomain().getChangeRemark())==true)
			sb.append("请补充不通过原因，");
		boolean change = false;
		for (OrderDetail detail: this.getDetailList()) {
			change = change || this.getNoteFormer4Purchase().isNoted(detail);
		}
		if (!change)
			sb.append("请填写采购更改内容，");
		if (sb.length()>0)
			throw new LogicException(2, sb.insert(0, "当地购不通过，").deleteCharAt(sb.length()-1).toString());
	}

	private void validateAdjustYes() {
		StringBuffer sb = new StringBuffer();
		OrderTicketLogic logic = new OrderTicketLogic();
		String adjustType = this.getAdjustType();
		String cancelType = (String)getNoteFormer4Order().getNoteValue(getOrderFirst(), new StringBuffer("cancelType"));
		if (StringUtils.isBlank(cancelType)) {
			sb.append("请选择原采购处理方式为无影响，");
		} else if (StringUtils.equals(cancelType, "无影响") == false) {
			sb.append("选择对原采购处理为").append(cancelType).append("时，请转排单处理，");
		}
		if (StringUtils.isEmpty(adjustType))
			sb.append("请选择红冲单据，");
		for (OrderDetail detail: this.getOrderList()) {
			if ("排单".equals(adjustType) && logic.getVoNoteMap(getNoteFormer4Order(), detail, ArrangeTicket.class).isEmpty()) {
				sb.append("请勿清空排单红冲内容！");
			} else if ("订单".equals(adjustType) && logic.getVoNoteMap(getNoteFormer4Order(), detail, OrderTicket.class).isEmpty()) {
				sb.append("请勿清空订单红冲内容！");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}

	private void validateAdjustNo() {
		StringBuffer sb = new StringBuffer();
		String adjustType = this.getAdjustType();
		if (StringUtils.isEmpty(adjustType))
			sb.append("请选择红冲单据，");
		String cancelType = (String)getNoteFormer4Order().getNoteValue(getOrderFirst(), new StringBuffer("cancelType"));
		if (StringUtils.isBlank(cancelType)==false)
			sb.append("驳回时不用选择原采购处理方式，");
		List<OrderDetail> list = new ArrayList(this.getSelectedList());
		for (OrderDetail detail: list) {
			if (!this.getNoteFormer4Order().isNoted(detail))
				sb.append("请勿清空红冲内容！");
			if (!getNoteFormer4Order().isChangedRemark(detail))
				sb.append("请补充红冲备注！");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}

	private void validateAdjust2Arrange() {
		StringBuffer sb = new StringBuffer();
		String adjustType = this.getAdjustType();
		if (StringUtils.isEmpty(adjustType))
			sb.append("请选择红冲单据，");
		String cancelType = (String)getNoteFormer4Order().getNoteValue(getOrderFirst(), new StringBuffer("cancelType"));
		if (StringUtils.equals(cancelType, "无影响")==true || StringUtils.isBlank(cancelType))
			sb.append("请选择原采购有影响的处理方式，");
		for (OrderDetail detail: this.getOrderList()) {
			if (!this.getNoteFormer4Order().isNoted(detail))
				sb.append("请勿清空红冲内容！");
			if (!getNoteFormer4Order().isChangedRemark(detail))
				sb.append("请补充红冲备注！");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}

	private void validateDoadjust() {
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.getDetailList()) {
			if (!this.getNoteFormer4Purchase().isNoted(detail)) {
				sb.append("请勿清空红冲内容！");
			}
			if (!getNoteFormer4Purchase().isChangedRemark(detail)) {
				sb.append("请补充红冲备注！");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	private void validateRepurchaseOrder() {
		StringBuffer sb = new StringBuffer();
		if (this.getSelectedList().size()==0)
			sb.append("请选择采购明细，");
		for (OrderDetail pur: this.getSelectedList()) {
			StringBuffer sitem = new StringBuffer();
			if (this.getNoteFormer4Purchase().getVoNoteMapIN(pur, "commodity").size()==0)
				sitem.append("没有订单更改内容不用订单改单，");
			if (sitem.length()>0)
				sb.append(pur.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateRepurchaseArrange() {
		StringBuffer sb = new StringBuffer();
		if (this.getSelectedList().size()==0)
			sb.append("请选择采购明细");
		for (OrderDetail pur: this.getSelectedList()) {
			StringBuffer sitem = new StringBuffer();
			if (this.getNoteFormer4Purchase().getVoNoteMapIN(pur, "ArrangeTicket").size()==0)
				sitem.append("没有排单更改内容不用排单改单，");
			if (sitem.length()>0)
				sb.append(pur.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateRepurchasePurchase() {
		StringBuffer sb = new StringBuffer();
		if (this.getSelectedList().size()==0)
			sb.append("请选择采购明细");
		for (OrderDetail pur: this.getSelectedList()) {
			StringBuffer sitem = new StringBuffer();
			if (this.getNoteFormer4Purchase().getVoNoteMap(pur).size()>0)
				sitem.append("有更改内容不用继续采购，");
			if (sitem.length()>0)
				sb.append(pur.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	// 收货改单申请交与各方处理
	private void validateRechangeYes() throws Exception {
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.getDetailList()) {
			if (this.getOrderAgreeList().contains("amount") && getNoteFormer4Purchase().getNoteString(detail, new StringBuffer("amount"))==null)
				sb.append("订单改单申请项有数量，请填写更改的采购数量，或取消数量勾选；");
			else if (this.getOrderAgreeList().contains("amount")==false && this.isRechange2Arrange()==false && getNoteFormer4Purchase().getNoteString(detail, new StringBuffer("amount"))!=null)
				sb.append("变更订单采购数量未转排单处理，请勾选订单改单申请项的数量项，");
			List<String> exlist = new ArrayList<String>();
			if (detail.getPurchaseTicket().getRearrangeAmount()==detail.getAmount())
				exlist.add("commodity");
			exlist.add( "amount");
			exlist.add( "voParamMap");
			exlist.add( "ReceiptTicket");
			exlist.add( "PurchaseTicket");
			exlist.addAll( getPurchaseAgreeList());
			exlist.addAll( getOrderAgreeList());
			List<String> mall=new ArrayList<String>(getNoteFormer4Purchase().getVoNoteMap(detail).keySet());
			List<String> mpart=new ArrayList<String>(getNoteFormer4Purchase().getVoNoteMapIN(detail, exlist.toArray(new String[0])).keySet());
			mall.removeAll(mpart);
			if (mall.isEmpty()==false)
				sb.append("请处理完改单申请内容，");
		}
		if (getNoteFormer4Purchase().isChangedRemark(getDomain())==false)
			sb.append("请补充改单申请备注，");
		sb.append(validatePurchaseChangeAmount(this.detailList, this.getNoteFormer4Purchase()));
		if (sb.length()>0)		throw new LogicException(2, sb.insert(0, "提交改单，").deleteCharAt(sb.length()-1).toString());
	}
	
	// 拒绝收货改单
	private void validateRechangeNo() throws Exception {
		StringBuffer sb = new StringBuffer();
		if (getDomain().getPurchaseTicket().getBackupAmount()>0)
			sb.append("备料数应为0，");
		if (getDomain().getPurchaseTicket().getCancelAmount()>0)
			sb.append("取消数应为0，");
		if (getDomain().getPurchaseTicket().getRearrangeAmount()>0)
			sb.append("重排单数应为0，");
		if (getDomain().getPurchaseTicket().getOverAmount()>0)
			sb.append("多收货数应为0，");
		for (OrderDetail detail: this.getDetailList()) {
			if (getNoteFormer4Purchase().isChangedNotesEX(detail, "voParamMap", "ReceiptTicket"))
				sb.append("改单申请内容不能更改，");
			if (getNoteFormer4Purchase().isChangedRemark(detail)==false)
				sb.append("请补充改单申请备注，");
			double receipt0=detail.getReceiptTicket().getReceiptAmount(), bad0=detail.getReceiptTicket().getBadAmount();
			Double receipt1 = (Double)getNoteFormer4Purchase().getNoteValue(detail, new StringBuffer("receiptAmount"));
			Double bad1 = (Double)getNoteFormer4Purchase().getNoteValue(detail, new StringBuffer("badAmount"));
			if (receipt0==0) {
			} else if (receipt1!=null && receipt1<detail.getReceiptTicket().getReceiptAmount()) {
			} else {
				sb.append("请填写收货数为0，");
			}
			if (detail.getReceiptTicket().getBadAmount()==0) {
			} else if (bad1!=null && bad1<detail.getReceiptTicket().getBadAmount()) {
				if (receipt1!=null && (receipt0-receipt1)>=(bad0-bad1)) {
				} else {
					sb.append("次品减少数要计入收货减少数，");
				}
			} else {
				sb.append("请填写次品收货数为0，");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.insert(0, "拒绝收货，").deleteCharAt(sb.length()-1).toString());
	}
	
	// 验证收货改单申请数量的正确
	protected StringBuffer validatePurchaseChangeAmount(List<OrderDetail> detailList, NoteAccessorFormer<OrderDetail> accessor) throws Exception {
		StringBuffer sb = new StringBuffer();
		DoubleType type = new DoubleType();
		for (OrderDetail detail: detailList) {
			Double purchase1 = (Double)accessor.getNoteValue(detail, new StringBuffer("amount"));
			Double receipt1 = (Double)accessor.getNoteValue(detail, new StringBuffer("receiptAmount"));
			Double bad1 = (Double)accessor.getNoteValue(detail, new StringBuffer("badAmount"));
			double purchase0=detail.getAmount(), purchase=purchase1==null? purchase0: purchase1;
			double receipt0=detail.getReceiptTicket().getReceiptAmount(), receipt=receipt1==null? receipt0: receipt1;
			double bad0=detail.getReceiptTicket().getBadAmount(), bad=bad1==null? bad0: bad1;
			if (bad>0 && bad>receipt)
				sb.append("其中次品数应小于收货数，");
			if (receipt1!= null && !(0<=receipt1 && receipt1!=receipt0))
				sb.append("请填写正确的应收货数量，");
			if (bad1!=null && !(0<=bad1 && bad1<bad0))
//				sb.append("请填写正确的应收货次品数量，");
			if (receipt==0 && bad==0)
				sb.append("0收货数量，请点拒绝收货，");
			if (purchase1!=null && purchase0>purchase1) {
				if (StringUtils.equals(type.format(purchase0-purchase1), type.format(detail.getPurchaseTicket().getCancelAmount()+detail.getPurchaseTicket().getBackupAmount()))==false)
					sb.append("减少的采购数量 要= 取消数量+转备料数量，");
				if (StringUtils.equals(type.format(purchase0-purchase1), type.format(detail.getPurchaseTicket().getRearrangeAmount()))==false)
					sb.append("减少的采购数量 要= 重排单数量，");
			}
			if (StringUtils.equals(type.format(purchase-detail.getPurchaseTicket().getBackupAmount()-detail.getPurchaseTicket().getCancelAmount()
				+detail.getPurchaseTicket().getRearrangeAmount()+detail.getPurchaseTicket().getOverAmount()), type.format(receipt-bad))==false)
				sb.append("采购数量-转备料数量-取消数量+重排单数量+多收货数量 要= 收货数量-次品数量，");
		}
		return sb;
	}
	
	private void validateToImport(Component fcomp) throws Exception {
		this.getBaseImportForm().validateIndexes(fcomp);
		StringBuffer sb = new StringBuffer();
		StringBuffer input = new StringBuffer().append(getDomain().getVoParamMap().get("Remark"));
		if ("null".equals(input.toString()))
			sb.append("请粘贴入单元格内容，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		else {
			// 保存列序号配置到Derby
			ListView listview = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0);
			SellerViewInputs inputs = this.getBaseImportForm().getSellerViewInputs();
			this.getBaseImportForm().setSellerIndexes(listview, inputs, "commodity.supplyType", this.getDomain().getCommodity().getSupplyType());
			new SellerViewInputsLogic().saveOrUpdate(inputs);
		}
	}
	
	private void validateImport(Component fcomp) throws Exception {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (getImportList().size()==0)
			sb.append("导入明细为空，");
		int ri=1;
		for (Iterator<OrderDetail> iter=getImportList().iterator(); iter.hasNext(); ri++) {
			OrderDetail d = iter.next();
			sitem = new StringBuffer();
			new CommodityLogic().getPropertyChoosableLogic().isValid(d.getCommodity(), sitem);
			if (d.getAmount()<=0)
				sitem.append("导入数量应大于0，");
			if (sitem.length() > 0)
				sb.append("第").append(ri).append("行").append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void resetChangeList(Component fcomp) {
		EditView view = fcomp.searchParentByClass(Window.class).getInnerFormerList(EditView.class).get(0);
		EditViewBuilder sourceBuilder = (EditViewBuilder)view.getViewBuilder();
		List<ViewBuilder> viewList = sourceBuilder.getFieldBuildersDeep(ViewBuilder.class);
		List<String> changeList = getChangeList();
		for (ViewBuilder vb: viewList) {
			if (Arrays.asList(vb.getFullName().split("[\\.]")).contains("ArrangeTicket")) {
				vb.setAttribute(changeList.contains("排单")? "show": "hidden", ParameterName.Note, ParameterName.Type);
			} else {
				vb.setAttribute(changeList.contains("订单")? "show": "hidden", ParameterName.Note, ParameterName.Type);
			}
		}
		Component from = view.getComponent();
		from.fireComponentReplace(sourceBuilder.build(view.getValue()).getComponent());
	}
	
	public void resetAdjusting(Component fcomp) {
		EditView view = fcomp.searchParentByClass(Window.class).getInnerFormerList(EditView.class).get(0);
		EditViewBuilder sourceBuilder = (EditViewBuilder)view.getViewBuilder();
		List<ViewBuilder> viewList = sourceBuilder.getFieldBuildersDeep(ViewBuilder.class);
		String adjustType = getAdjustType();
		for (ViewBuilder vb: viewList) {
			if ("only-note".equals(vb.getParameter(ParameterName.Note).getString(ParameterName.Type)))			continue;
			if (Arrays.asList(vb.getFullName().split("[\\.]")).contains("ArrangeTicket")) {
				vb.setAttribute("排单".equals(adjustType)? "has-note": "hidden", ParameterName.Note, ParameterName.Type);
			} else {
				vb.setAttribute("订单".equals(adjustType)? "has-note": "hidden", ParameterName.Note, ParameterName.Type);
			}
		}
		Component from = view.getComponent();
		from.fireComponentReplace(sourceBuilder.build(view.getValue()).getComponent());
	}
	
	public void clearOrder() {
		this.setAttr(new OrderDetail());
	}
	
	private void getCommoditySearchNumber(TextField input) {
		this.setIsDialogOpen(false);
		String number = input.getText();
		if (StringUtils.isNotEmpty(number)){
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "CommodityQuery", 1|2, "commNumber", number)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	private void setChangeState(State state, ViewData<OrderDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (getChangeList().contains("排单")) {
				if (stateId>-1)		d.setArrangeId(stateId);
			}
			if (getChangeList().contains("订单")) {
				if (stateId>-1)		d.setStOrder(stateId);
			}
			if (stateName != null) {
				d.setStateName(getChangeList()+stateName);
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
	private void setBomPurchaseState(State state, ViewData<BomDetail> viewData) {
		int stateId = state.getId();
		String stateName = state.getName();
		for (BomDetail d: viewData.getTicketDetails()) {
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
	
	public void setChangeNotes(Component fcomp) {
		Window win = fcomp.searchParentByClass(Window.class);
		List<AddNoteListener> noteList = win.getInnerFormerList(AddNoteListener.class);
		Map<String, String> domainMap = new HashMap<String, String>();
		Map<String, String> domainAll = this.getNoteFormer4Order().getVoNoteMap(this.getOrderFirst());
		for (AddNoteListener note: noteList) {
			String name=note.getPropertyName(), value=note.getNoteString();
			if (domainAll.containsKey(name)==false)
				continue;
			ListView listview = note.getComponent().searchFormerByClass(ListView.class);
			if (!(listview==null && note.getEntity()==getOrderFirst()))			continue;
			domainMap.put(name, value);
		}
		for (OrderDetail detail: this.getOrderList()) {
			this.getNoteFormer4Order().getVoNoteMap(detail).putAll(domainMap);
			this.getNoteFormer4Order().isChangedNotesEX(detail);
			detail.setChangeRemark(this.getOrderFirst().getChangeRemark());
		}
	}
	
	private void setPurchaseNotesClear() {
		this.getDomain().setChangeRemark(null);
		for (OrderDetail pur: this.getDetailList()) {
			this.getNoteFormer4Purchase().getVoNoteMap(pur).clear();
			pur.setChangeRemark(null);
		}
	}
	
	public void setPurchaseChangeNotes(Component fcomp) {
		Window win = fcomp.searchParentByClass(Window.class);
		List<AddNoteListener> noteList = win.getInnerFormerList(AddNoteListener.class);
		Map<String, String> domainMap = new HashMap<String, String>();
		Map<String, String> domainAll = this.getNoteFormer4Purchase().getVoNoteMap(this.getDomain());
		for (AddNoteListener note: noteList) {
			String name=note.getPropertyName(), value=note.getNoteString();
			if (domainAll.containsKey(name)==false)
				continue;
			ListView listview = note.getComponent().searchFormerByClass(ListView.class);
			if (listview!=null && listview.getViewBuilder().getName().equals("detailList"))
				continue;
			if (!(note.getEntity()==getDomain()))			continue;
			domainMap.put(name, value);
		}
		for (OrderDetail detail: this.getDetailList()) {
			this.getNoteFormer4Purchase().getVoNoteMap(detail).putAll(domainMap);
			this.getNoteFormer4Purchase().isChangedNotesEX(detail);
			detail.setChangeRemark(this.getDomain().getChangeRemark());
		}
	}
	
	public void setPurchaseChangeReceipt() {
		for (OrderDetail detail: this.getDetailList()) {
			detail.getPurchaseTicket().setBackupAmount(this.getDomain().getPurchaseTicket().getBackupAmount());
			detail.getPurchaseTicket().setCancelAmount(this.getDomain().getPurchaseTicket().getCancelAmount());
			detail.getPurchaseTicket().setRearrangeAmount(this.getDomain().getPurchaseTicket().getRearrangeAmount());
			detail.getPurchaseTicket().setOverAmount(this.getDomain().getPurchaseTicket().getOverAmount());
		}
	}
	
	private void setRemoveSelected() {
		this.getDetailList().removeAll(this.getSelectedList());
	}
	
	private void setOrderUser(ViewData<OrderDetail> viewData) {
		String suser = this.genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUorder(suser);
		}
	}
	
	protected void setPurchaseUser(ViewData<OrderDetail> viewData) {
		Date hope=this.getDomain().getPurchaseTicket().getHopeDate();
		StringBuffer suser = new StringBuffer(hope==null? "          ": new DateType().format(hope)).append(this.genTicketUser().getUserDate());
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUpurchase(suser.toString());
		}
	}
	private void setBomPurchaseUser(ViewData<BomDetail> viewData) {
		Date hope=this.getDomain().getPurchaseTicket().getHopeDate();
		StringBuffer suser = new StringBuffer(hope==null? "          ": new DateType().format(hope)).append(this.genTicketUser().getUserDate());
		for (BomDetail d: viewData.getTicketDetails()) {
			d.setUpurchase(suser.toString());
		}
	}
	
	private void setChangeUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUchange(suser);
		}
	}
	
	private void setChangeUser4Pur(ViewData<OrderDetail> viewData) {
		TicketUser suser = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUchange(suser.addUserDate(d.getUchange()) );
		}
	}
	
	private void addCancelUser(ViewData<OrderDetail> viewData) {
		TicketUser user = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUcancel(user.addUserDate(d.getUcancel()));
		}
	}
	
	private void addReturnUser(ViewData<OrderDetail> viewData) {
		TicketUser user = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreturn(user.addUserDate(d.getUreturn()));
		}
	}
	
	private void setReceiptUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreceipt(suser);
		}
	}
	
	private void setPurchase4Service(ViewData<OrderDetail> viewData) {
		PropertyChoosableLogic.TicketDetail logic=this.getPurchaseChoosable();
		for (OrderDetail d: this.detailList) {
			d.setSupplier(this.getDomain().getSupplier());
			logic.fromTrunk(logic.getTicketBuilder(), d.getPurchaseTicket(), this.getDomain().getPurchaseTicket());
		}
		viewData.setTicketDetails(this.detailList);
	}
	
	private void setBomPurchase4Service(ViewData<BomDetail> viewData) {
		List<BomDetail> bomList = new ArrayList<BomDetail>();
		PropertyChoosableLogic.TicketDetail logic=this.getPurchaseChoosable();
		for (OrderDetail d: this.detailList) {
			BomDetail bom = d.getVoparam(BomDetail.class);
			logic.fromTrunk(logic.getTicketBuilder(), bom.getPurchaseTicket(), this.getDomain().getPurchaseTicket());
			bom.setPrice(d.getPrice());
			bom.getPurchaseTicket().setPmoney(d.getPurchaseTicket().getPmoney());
			bomList.add(bom);
		}
		viewData.setTicketDetails(bomList);
	}
	private void setBomSupplier4Service(ViewData<BomDetail> viewData) {
		List<BomDetail> bomList = new ArrayList<BomDetail>();
		for (OrderDetail d: this.detailList) {
			BomDetail bom = d.getVoparam(BomDetail.class);
			if (bom==null)
				continue;
			PPurchaseTicketForm pform = new PPurchaseTicketForm();
			List<BomDetail> sourceList = pform.getOnPageLoadedListener().getBomDetails(bom.getMonthnum());
			List<BomDetail> childList = new BomTicketLogic().getChildrenBrother(sourceList, bom);
			for (Iterator<BomDetail> iter=childList.iterator(); iter.hasNext();) {
				BomDetail b = iter.next();
				b.setSupplier(d.getSupplier());
				bomList.add(b);
				if (new ArrangeTypeLogic().isNormal(b.getArrange())) {
					List<BomDetail> subs = new BomTicketLogic().getChildrenBrother(sourceList, b);
					iter = new JoinedIterator(iter, subs.iterator());
				}
			}
		}
		viewData.setTicketDetails(bomList);
	}
	
	private void setPurchaseEffect4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getDetailList());
	}
	
	public void setReedit4Service(ViewData<OrderDetail> viewData) {
		PropertyChoosableLogic.TicketDetail logic=this.getPurchaseChoosable();
		for (OrderDetail d: this.detailList) {
			d.setSupplier(this.getDomain().getSupplier());
			logic.fromTrunk(logic.getTicketBuilder(), d.getPurchaseTicket(), this.getDomain().getPurchaseTicket());
		}
		viewData.setTicketDetails(this.detailList);
	}
	
	private void setOrder4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(getSelectEdit4Order().getSelectedList());
	}
	
	private ActionService4LinkListener getOrderLink() {
		ActionService4LinkListener listener = new ActionService4LinkListener();
		Seller toSeller = new Seller4lLogic().getSellerById(this.getDomain().getSupplier().getToSellerId());
		listener.getOnceAttributes().put("seller", toSeller);
		User user = TicketPropertyUtil.copyProperties(this.getUser(), new User());
		user.setUserName(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
		listener.getOnceAttributes().put("user", user);
		this.setAttr(listener);
		return listener;
	}
	private ActionService4LinkListener getSelectOrderLink() {
		ActionService4LinkListener listener = new ActionService4LinkListener();
		Seller toSeller = new Seller4lLogic().getSellerById(this.getSelectFormer4Purchase().getFirst().getSupplier().getToSellerId());
		listener.getOnceAttributes().put("seller", toSeller);
		User user = TicketPropertyUtil.copyProperties(this.getUser(), new User());
		user.setUserName(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
		listener.getOnceAttributes().put("user", user);
		this.setAttr(listener);
		return listener;
	}
	private ActionService4LinkListener getDetailOrderLink() {
		ActionService4LinkListener listener = new ActionService4LinkListener();
		OrderDetail sorder = this.getDetailFirstOrder().getSnapShot();
		Assert.assertTrue("当地分公司的上级公司", sorder.getSellerId()!=this.getSellerId());
		Seller toSeller = new Seller4lLogic().getSellerById(sorder.getSellerId());
		listener.getOnceAttributes().put("seller", toSeller);
		User user = TicketPropertyUtil.copyProperties(this.getUser(), new User());
		user.setUserName(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
		listener.getOnceAttributes().put("user", user);
		this.setAttr(listener);
		return listener;
	}
	
	private void setOrderLink4Service(ViewData<OrderDetail> viewData) {
		Supplier link = this.getDomain().getSupplier();
		Client client = null;
		SubCompany subCompany = null;
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		viewData.setTicketDetails(orderList);
		if (new SubmitTypeLogic().isSubCompanyType(link.getSubmitType()) && (subCompany=new SubCompanyLogic().getSubCompany(link))!=null) {
			subCompany.getSubmitNumber();
		} else if (new SubmitTypeLogic().isClientType(link.getSubmitType()) && (client=new ClientLogic().getClient(link))!=null) {
			client.getSubmitNumber();
		} else {
			return ;
		}
		OrderDetail first = null;
		PropertyChoosableLogic.TicketDetail ologic=new OrderTicketLogic().getTicketChoosableLogic();
		for (OrderDetail detail: this.getDetailList()) {
			OrderDetail order=new OrderDetail(), purOrder=detail;
			if (first==null) {
				first = order;
				String number = new StringBuffer().append(this.getDomain().getPurchaseTicket().getNumber()).append(".").append(first.getOrderTicket().genSerialNumber()).toString();
				if (subCompany != null)
					first.setOrderTicket(detail.getOrderTicket());
				first.getOrderTicket().setNumber(number);
				this.getDomain().getVoParamMap().put("UpOrder", first);
			}
			detail.getVoParamMap().put("UpOrder", order);
			if (client != null)
				order.setClient((Client)client.getSnapShot());
			if (subCompany != null) {
				order.setSubCompany(subCompany);
				order.setClient((Client)purOrder.getClient().getSnapShot());
				if (order.getClient().getName()==null)
					TicketPropertyUtil.copyProperties(order.getSubCompany(), order.getClient());
			}
			order.getClient().setFromSellerId(purOrder.getSellerId());
			order.getClient().setUaccept(new StringBuffer(this.getDomain().getSupplier().getToSellerName()).toString());
			order.getOrderTicket().setOrderType(new OrderTypeLogic().getClientType());
			order.setMonthnum(new OrderTicketLogic().getLinkMonthnum(detail.getMonthnum()));
			detail.setMonthnum(order.getMonthnum());
			purOrder.setMonthnum(order.getMonthnum());
			order.setCommodity(detail.getCommodity());
			order.setAmount(detail.getAmount());
			order.getOrderTicket().setCprice(detail.getPrice());
			ologic.fromTrunk(ologic.getTicketBuilder(), order.getOrderTicket(), first.getOrderTicket());
			orderList.add(order);
		}
	}
	
	private void setOrderReLink4Service(ViewData<OrderDetail> viewData) {
		Supplier link = this.getDomain().getSupplier();
		Client client = null;
		SubCompany subCompany = null;
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		viewData.setTicketDetails(orderList);
		if (new SubmitTypeLogic().isClientType(link.getSubmitType()) && (client=new ClientLogic().getClient(link))!=null) {
			client.getSubmitNumber();
		} else if (new SubmitTypeLogic().isSubCompanyType(link.getSubmitType()) && (subCompany=new SubCompanyLogic().getSubCompany(link))!=null) {
			subCompany.getSubmitNumber();
		} else {
			return ;
		}
		OrderDetail first = null;
		PropertyChoosableLogic.TicketDetail logic=new OrderTicketLogic().getTicketChoosableLogic();
		for (OrderDetail pur: this.getSelectedList()) {
			OrderDetail spur=pur.getSnapShot(), order=new OrderDetail();
			if (orderList.size()==0) {
				first = order;
				String number = new StringBuffer().append(this.getDomain().getPurchaseTicket().getNumber()).append(".").append(first.getOrderTicket().genSerialNumber()).toString();
				if (subCompany != null)
					first.setOrderTicket(pur.getOrderTicket());
				first.getOrderTicket().setNumber(number);
				this.getDomain().getVoParamMap().put("UpOrder", first);
			}
			pur.getVoParamMap().put("UpOrder", order);
			if (client != null)
				order.setClient((Client)client.getSnapShot());
			if (subCompany != null) {
				order.setSubCompany(subCompany);
				if (spur.getStOrder()>0)
					order.setClient((Client)spur.getClient().getSnapShot());
				if (order.getClient().getName()==null)
					TicketPropertyUtil.copyProperties(order.getSubCompany(), order.getClient());
			}
			order.getClient().setFromSellerId(spur.getSellerId());
			order.getClient().setUaccept(new StringBuffer(this.getDomain().getSupplier().getToSellerName()).toString());
			order.getOrderTicket().setOrderType(new OrderTypeLogic().getClientType());
			order.setMonthnum(pur.getMonthnum());
			order.setCommodity(pur.getCommodity());
			order.setAmount(pur.getAmount());
			order.getOrderTicket().setCprice(pur.getPrice());
			logic.fromTrunk(logic.getTicketBuilder(), order.getOrderTicket(), first.getOrderTicket());
			orderList.add(order);
		}
	}
	
	private void setRechangeLink4Service_validate(ViewData<OrderDetail> viewData) {
		if ("有更改".length()>0) {
			String[] names=new String[]{"commodity", "price"};
			int cnt=0;
			for (String name: names) { 
				if (this.getPurchaseAgreeList().contains(name)==false)
					continue;
				cnt++;
			}
			if (cnt == 0)
				return;
			for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
				StringBuffer pname = new StringBuffer("price");
				this.getNoteFormer4Purchase().getNoteString(detail, pname);
				HashMap<String, String> map = this.getNoteFormer4Purchase().getVoNoteMapIN(detail, names);
				if (map.keySet().contains(pname.toString())) {
					String pval = map.get(pname.toString());
					map.remove(pname.toString());
					String cname = StringUtils.replace(pname.toString(), "price", "OrderTicket.cprice");
					map.put(cname, pval);
				}
				detail.getVoParamMap().put("DownCommodityRechanges", map);
			}
		}
		TestMode[] fromModes = new ClientTest().getModeList().getModeList();
		HashMap<Set<TestMode>, List<OrderDetail>> modeList = new ClientTest().getSplitPurchases(this.getSelectFormer4Purchase().getSelectedList());
		for (Set<TestMode> kmode: modeList.keySet()) {
			new ClientTest().getModeList().setMode(kmode.toArray(new TestMode[0]));
			new ClientTest().getModeList().getSelfOrderTest().linkSupplier开红冲__1拆单准备_2红冲草稿('1', modeList.get(kmode));
		}
		new ClientTest().getModeList().setMode(fromModes);
		viewData.setTicketDetails();
	}
	private void setRechangeLink4Service(ViewData<OrderDetail> viewData) {
		if ("有更改".length()>0) {
			String[] names=new String[]{"commodity", "price"};
			int cnt=0;
			for (String name: names) { 
				if (this.getPurchaseAgreeList().contains(name))
					cnt++;
			}
			if (cnt == 0)
				return;
			for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
				HashMap<String, String> map = this.getNoteFormer4Purchase().getVoNoteMapIN(detail, names);
				detail.getVoParamMap().put("DownCommodityRechanges", map);
			}
		}
		TestMode[] fromModes = new ClientTest().getModeList().getModeList();
		HashMap<Set<TestMode>, List<OrderDetail>> modeList = new ClientTest().getSplitPurchases(this.getSelectFormer4Purchase().getSelectedList());
		for (Set<TestMode> kmode: modeList.keySet()) {
			new ClientTest().getModeList().setMode(kmode.toArray(new TestMode[0]));
			new ClientTest().getModeList().getSelfOrderTest().linkSupplier开红冲__1拆单准备_2红冲草稿('2', modeList.get(kmode));
		}
		new ClientTest().getModeList().setMode(fromModes);
		viewData.setTicketDetails();
	}

	private void setCancelLink4Service_validate(ViewData<OrderDetail> viewData) {
		if ("有更改".length()>0) {
			for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
				HashMap<String, String> map = this.getNoteFormer4Purchase().getVoNoteMap(detail);
				map.put("amount", "0");
				detail.getVoParamMap().put("DownCommodityRechanges", map);
			}
		}
		TestMode[] fromModes = new ClientTest().getModeList().getModeList();
		HashMap<Set<TestMode>, List<OrderDetail>> modeList = new ClientTest().getSplitPurchases(this.getSelectFormer4Purchase().getSelectedList());
		for (Set<TestMode> kmode: modeList.keySet()) {
			new ClientTest().getModeList().setMode(kmode.toArray(new TestMode[0]));
			new ClientTest().getModeList().getSelfOrderTest().linkSupplier开红冲__1拆单准备_2红冲草稿('1', modeList.get(kmode));
		}
		new ClientTest().getModeList().setMode(fromModes);
		viewData.setTicketDetails();
	}
	private void setCancelLink4Service(ViewData<OrderDetail> viewData) {
		TestMode[] fromModes = new ClientTest().getModeList().getModeList();
		HashMap<Set<TestMode>, List<OrderDetail>> modeList = new ClientTest().getSplitPurchases(this.getSelectFormer4Purchase().getSelectedList());
		for (Set<TestMode> kmode: modeList.keySet()) {
			new ClientTest().getModeList().setMode(kmode.toArray(new TestMode[0]));
			new ClientTest().getModeList().getSelfOrderTest().linkSupplier开红冲__1拆单准备_2红冲草稿('2', modeList.get(kmode));
		}
		new ClientTest().getModeList().setMode(fromModes);
		viewData.setTicketDetails();
	}

	public void setChangeClear4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(getSelectEdit4Order().getSelectedList());
		viewData.setParam("ChangeList", this.getChangeList());
		viewData.setParam(this.getNoteFormer4Order());
	}
	
	private void setRepurchaseChangeOrder4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		String start="commodity";
		for (OrderDetail pur: this.getSelectedList()) {
			OrderDetail ord = pur;
			ord.setChangeRemark(pur.getChangeRemark());
			LinkedHashMap<String, String> map = this.getNoteFormer4Purchase().getVoNoteMapIN(pur, start);
			for (Map.Entry<String, String> entry: map.entrySet()) {
				this.getNoteFormer4Order().getVoNoteMap(ord).put(entry.getKey().substring(entry.getKey().indexOf(start)), entry.getValue());
			}
			this.getNoteFormer4Order().isNoted(ord);
			ordList.add(ord);
		}
		viewData.setTicketDetails(ordList);
	}
	
	private void setRepurchaseChangeArrange4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		String start="ArrangeTicket";
		for (OrderDetail pur: this.getSelectedList()) {
			OrderDetail ord = pur;
			ord.setChangeRemark(pur.getChangeRemark());
			LinkedHashMap<String, String> map = this.getNoteFormer4Purchase().getVoNoteMapIN(pur, start);
			for (Map.Entry<String, String> entry: map.entrySet()) {
				this.getNoteFormer4Order().getVoNoteMap(ord).put(entry.getKey().substring(entry.getKey().indexOf(start)), entry.getValue());
			}
			this.getNoteFormer4Order().isNoted(ord);
			ordList.add(ord);
		}
		viewData.setTicketDetails(ordList);
	}
	
	private void setRepurchasePurchase4Service(ViewData<OrderDetail> viewData) {
		this.getDomain().getPurchaseTicket().genSerialNumber();
		for (OrderDetail detail: this.getSelectedList()) {
			detail.getPurchaseTicket().setNumber(this.getDomain().getPurchaseTicket().getNumber());
		}
		viewData.setTicketDetails(this.getSelectedList());
	}
	
	private void setRepurchaseDelete4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectedList());
	}
	
	public void setAdjust4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList(this.getSelectedList());
		viewData.setTicketDetails(list);
		viewData.setParam(this.getNoteFormer4Order());
		viewData.setParam(getAdjustType());
	}
	
	public void setRechangePurchase4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getDetailList());
		viewData.setParam(this.getNoteFormer4Purchase());
		viewData.setParam("ChangeType", ArrayList.class);
		List<String> propList = getPurchaseAgreeList();
		propList.remove("commodity");
		viewData.setParam(propList);
	}
	
	public void setRechangeOrder4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		for (OrderDetail p: this.detailList) {
			OrderDetail d = p;
			orderList.add(d);
		}
		viewData.setTicketDetails(orderList);
	}
	
	public void setRechangeReceipt4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getDetailList());
	}
	
	public void setDoadjust4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectedList());
		viewData.setParam(this.getNoteFormer4Purchase());
	}
	
	private void setSplitNew4Service(ViewData<OrderDetail> viewData) {
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
	
	private void setSplitRemain4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail pur: this.getSelectFormer4Purchase().getSelectedList()) {
			pur.setAmount(pur.getAmount() - this.getOrderDetail().getAmount());
			purList.add(pur);
		}
		viewData.setTicketDetails(purList);
	}
	
	public void setCancelYes4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	}
	
	public void setCancel4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	}
	
	public void setReturn4Service(ViewData<OrderDetail> viewData) {
	 	viewData.setTicketDetails(this.getSelectFormer4Purchase().getSelectedList());
	}
	
	private void setInExtra4Service() {
		StoreTicketForm form = new StoreTicketForm();
		for (OrderDetail pur: this.getImportList()) {
			pur.getReceiptTicket().setStorePrice(pur.getPrice());
		}
		form.setInExtra4Service(this.getImportList());
		this.setAttr(form);
	}
	
	public void setImportInstore4Service(ViewData<OrderDetail> viewData) {
	 	this.getAttr(StoreTicketForm.class).setInExtra4Service(viewData);
	}
	
	public void setImportCount4Service(ViewData<OrderDetail> viewData) {
	 	this.getAttr(StoreTicketForm.class).setExtraCount4Service(viewData);
	}
	
	protected TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}

	public List<OrderDetail> getDetailList() {
		return this.detailList;
	}
	protected void setDetailList(List<OrderDetail> list) {
		this.detailList = list;
	}
	
	public List<OrderDetail> getImportList() {
		String k = "ImportList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private BaseImportForm getBaseImportForm() {
		BaseImportForm form = this.getAttr(BaseImportForm.class);
		if (form==null) {
			form = new BaseImportForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private List<SupplyType> getSupplyTypeOptions(Object entity) {
		List<SupplyType> typeList = new SupplyTypeLogic().getTypeList();
		return typeList;
	}
	
	private void setImportLabelLoad(Component fcomp) {
		ListView listview = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0);
		String builderName=listview.getViewBuilder().getFullViewName();
		SellerViewInputs inputs = new SellerViewInputsLogic().get(builderName);
		if (inputs == null) {
			inputs = new SellerViewInputs();
			inputs.setBuilderName(builderName);
		}
		this.getBaseImportForm().setFormProperty("attrMap.SellerViewInputs", inputs);
		String supplyType = this.getBaseImportForm().getSellerViewInputs().getInputs().get("commodity.supplyType");
		this.getDomain().getCommodity().setSupplyType(supplyType);
		this.getDomain().getArrangeTicket().setArrangeType(new DeliverTypeLogic().getCommonType());
	}
	
	private void setImportFormated(Component fcomp) throws Exception {
		List<OrderDetail> importList = this.getBaseImportForm().setImportFormated(fcomp, this.getDomain());
		this.getImportList().addAll(importList);
	}
	
	private boolean isNeedPurchaseAgreeOptions() {
		List<Entry<String, String>> list = getPurchaseAgreeOptions(this.getDomain());
		List<String> exnames = Arrays.asList(new String[]{"commodity"});
		for (Iterator<Entry<String, String>> iter=list.iterator(); iter.hasNext();) {
			String name = iter.next().getKey();
			if (exnames.contains(name)) {
				iter.remove();
			}
		}
		return list.size()>getPurchaseAgreeList().size();
	}
	
	public List<Map.Entry<String, String>> getPurchaseAgreeOptions(Object detail0) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("supplier", "供应商");
		map.put("commodity", "更改商品");
		map.put("price", "价格");
		for (String agree: getOrderAgreeList()) {
			map.remove(agree);
		}
		for (OrderDetail detail: detailList) {
			for (String pname: new ArrayList<String>(map.keySet())) {
				Map<String,String> pnotes=getNoteFormer4Purchase().getVoNoteMapIN(detail, pname);
				if (pnotes.isEmpty())		map.remove(pname);
			}
		}
		return new ArrayList<Entry<String, String>>(map.entrySet());
	}
	
	protected List<String> getPurchaseAgreeList() {
		Object v = getNoteFormer4Purchase().getNoteValue(getDomain(), new StringBuffer("PurchaseAgree"));
		return v instanceof List? (List<String>)v: new ArrayList<String>(0);
	}
	
	public List<Map.Entry<String, String>> getOrderAgreeOptions(Object detail0) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("commodity", "更改商品");
		for (String agree: getPurchaseAgreeList()) {
			map.remove(agree);
		}
		for (OrderDetail detail: detailList) {
			for (String pname: new ArrayList<String>(map.keySet())) {
				Map<String,String> pnotes=getNoteFormer4Purchase().getVoNoteMapIN(detail, pname);
				if (pnotes.isEmpty())		map.remove(pname);
			}
		}
		double receipt1 = this.getDomain().getReceiptTicket().getReceiptAmount()-this.getDomain().getReceiptTicket().getBadAmount();
		if (receipt1!=getDomain().getAmount()) {
			map.put("amount", "数量");
		}
		return new ArrayList<Entry<String, String>>(map.entrySet());
	}
	
	protected List<String> getOrderAgreeList() {
		Object v = getNoteFormer4Purchase().getNoteValue(getDomain(), new StringBuffer("OrderAgree"));
		return v instanceof List? (List<String>)v: new ArrayList<String>(0);
	}
	
	private List<String> getChangeOptions(Object form) {
		String k = "ChangeOptions";
		List<String> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<String>();
			list.add("排单");
			list.add("订单");
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<String> getArrangeOptions(Object arrangeTicket) {
		List<String> list = new ArrangeTypeLogic().getArrangeTypes();
		list.add(0, "重新排单");
		return list;
	}

	private List<String> getArrangeOptions4Cancel(Object arrangeTicket) {
		List<String> list = new ArrayList<String>();
		list.add("无影响");
		list.add("取消");
		list.add("转备料");
		return list;
	}
	
	private List<String> getAdjustOptions(Object form) {
		List<String> list = new ArrayList<String>();
		String adjustType = getAdjustType();
		OrderTicketLogic logic = new OrderTicketLogic();
		for (OrderDetail detail: this.getOrderList()) {
			if (logic.getVoNoteMap(getNoteFormer4Order(), detail, OrderTicket.class).size()>0) {
				list.add("订单");
			}
			if (logic.getVoNoteMap(getNoteFormer4Order(), detail, ArrangeTicket.class).size()>0) {
				list.add("排单");
			}
		}
		this.setAttr("AdjustType", list.get(0));
		return list;
	}
	
	public String getAdjustType() {
		return (String)getAttr("AdjustType");
	}
	
	public boolean isRechange2PurchaseAgree() {
		return getPurchaseAgreeList().size()>0;
	}
	
	public boolean isRechange2Order() {
		StringBuffer sname = new StringBuffer("OrderAgree");
		List<String> curList = (List<String>)getNoteFormer4Purchase().getNoteValue(getDomain(), sname);
		List<String> fromList = (List<String>)getNoteFormer4Purchase().getSourceNoteValue(getDomain(), sname);
		if (curList==null)		curList = new ArrayList<String>();
		if (fromList==null)		fromList = new ArrayList<String>();
		curList.removeAll(fromList);
		return curList.size()>0;
	}
	
	public boolean isRechange2Arrange() {
		PurchaseTicket t=getDomain().getPurchaseTicket(), st=((OrderDetail)getDomain().getSnapShot()).getPurchaseTicket();
		if (t.getBackupAmount()!=st.getBackupAmount()) {
		} else if (t.getCancelAmount()!=st.getCancelAmount()) {
		} else if (t.getOverAmount()!=st.getOverAmount()) {
		} else if (t.getRearrangeAmount()!=st.getRearrangeAmount()) {
		} else {
			return false;
		}
		return t.getBackupAmount()+t.getCancelAmount()+t.getOverAmount()+t.getRearrangeAmount()>0;
	}
	
	public boolean isRechange2Receipt() {
		return getNoteFormer4Purchase().getVoNoteMapIN(getDomain(), "ReceiptTicket").size()>0;
	}
	
	public OrderDetail getOrderDetail() {
		OrderDetail d = this.getAttr(OrderDetail.class);
		if (d == null) {
			d = new OrderDetail();
			this.setAttr(d);
		}
		return d;
	}
	
	public OrderDetail getOrderFirst() {
		return this.getOrderList().get(0);
	}
	
	public List<OrderDetail> getOrderList() {
		String k = "orderList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<String> getChangeList() {
		String k = "changeList";
		List<String> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<String>();
			OrderTicketLogic logic = new OrderTicketLogic();
			if (this.getOrderList().size()>0) {
				OrderDetail order=getOrderFirst(), sorder=order.getSnapShot();
				if (logic.getVoNoteMap(getNoteFormer4Order(), order, ArrangeTicket.class).size()>0 && order.getArrangeId()==42) {
					list.add("排单");
				}
				if (logic.getVoNoteMap(getNoteFormer4Order(), order, OrderTicket.class).size()>0 && sorder.getStOrder()==42) {
					list.add("订单");
				}
			}
			this.setAttr(k, list);
		}
		return list;
	}
	
	public void setChangeList(List<String> list) {
		list = list==null? new ArrayList<String>(0): new ArrayList<String>(list);
		getChangeList().clear();
		getChangeList().addAll(list);
		OrderTicketLogic logic = new OrderTicketLogic();
		for (OrderDetail detail: getOrderList()) {
			Map<String, String> mall=getNoteFormer4Order().getVoNoteMap(detail);
			if (getChangeList().contains("排单")==false) {
				for (String k: logic.getVoNoteMap(getNoteFormer4Order(), detail, ArrangeTicket.class).keySet()) {
					mall.remove(k);
				}
			}
			if (getChangeList().contains("订单")==false) {
				for (String k: logic.getVoNoteMap(getNoteFormer4Order(), detail, OrderTicket.class).keySet()) {
					mall.remove(k);
				}
			}
		}
	}
	
	public void setChangeOptionsDisable(Component fcomp) {
		Hyperlink link = fcomp.searchParentByClass(Window.class).getInnerComponentList(Hyperlink.class).get(0);
		Container container = link.searchParentByClass(Container.class);
		container.fireComponentReplace(new Text(getChangeList()+""));
	}
	
	public void setAdjustOptionsDisable(Component fcomp) {
		Hyperlink link = fcomp.searchParentByClass(Window.class).getInnerComponentList(Hyperlink.class).get(0);
		Container container = link.searchParentByClass(Container.class);
		container.fireComponentReplace(new Text(getAdjustType()));
	}
	
	public SelectTicketFormer4Sql<PurchaseTicketForm, OrderDetail> getSelectFormer4Purchase() {
		String k = "SelectFormer4Purchase";
		SelectTicketFormer4Sql<PurchaseTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<PurchaseTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<PurchaseTicketForm, OrderDetail> getSelectFormer4Order() {
		String k = "SelectFormer4Order";
		SelectTicketFormer4Sql<PurchaseTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<PurchaseTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<PurchaseTicketForm, BomDetail> getSelectFormer4Bom() {
		String k = "SelectFormer4Bom";
		SelectTicketFormer4Sql<PurchaseTicketForm, BomDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<PurchaseTicketForm, BomDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	protected SelectTicketFormer4Edit<PurchaseTicketForm, OrderDetail> getSelectEdit4Order() {
		String k = "SelectEdit4Order";
		SelectTicketFormer4Edit<PurchaseTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Edit<PurchaseTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public OrderDetail getDetailFirstOrder() {
		return this.getDetailList().get(0);
	}

	public OrderDetail getDomain() {
		String k = "FormDomain";
		OrderDetail d = this.getAttr(k);
		if (d == null) {
			d = new OrderDetail();
			this.setAttr(k, d);
		}
		return d;
	}
	
	protected void setDomain(OrderDetail d) {
		String k = "FormDomain";
		this.setAttr(k, d);
	}
	
	public OrderDetail getSelectedFirst4Purchase() {
		return this.getSelectedList().get(0);
	}

	@Override
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		new ReceiptTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
		getArrangeChoosableLogic().trunkViewBuilder(viewBuilder);
		getPurchaseChoosable().trunkViewBuilder(viewBuilder);
		new OrderTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
		new SupplierLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (viewBuilder.getName().equals("Import"))
			this.getBaseImportForm().setImportBuilderInit(viewBuilder);
	}
	
	public List<OrderDetail> getSelectedList() {
		String k = "SelectedList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
	}
	
	private void getSupplierSearchName(TextField input) {
		String name = input.getText();
		this.setIsDialogOpen(false);
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "SupplierQuery", 1|2, "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	public List<Supplier> getSupplierSearchNumber(String number) {
		this.setIsDialogOpen(false);
		if(!StringUtils.isEmpty(number)) {
			SqlListBuilder sqlBuilder = (SqlListBuilder)EntityClass.loadViewBuilder(this.getClass(), "SupplierQuery").getFieldBuildersDeep(SqlListBuilder.class).get(0);
			HashMap<String, String> filters = new HashMap<String, String>();
			filters.put("number", number);
			SqlListBuilderSetting childSetting = getSearchSetting(sqlBuilder);
			childSetting.addFilters(filters);
			sqlBuilder.getSqlQuery().getFields().asSetting(childSetting);
			List<List<Object>> rows = sqlBuilder.getSqlQuery().loadResultAll(sqlBuilder.getSqlQuery().getFields().sqlSelectSource());
			if(rows.size()==0){//找到零条
			} else if(rows.size()==1){//找到一条
				getSearchSetting(sqlBuilder);
				return (List)new SelectDomainListener().toDomains(rows, Supplier.class);
			} else {//找到多条
				this.setIsDialogOpen(true);
			}
		}
		return new ArrayList<Supplier>(0);
	}
	
	public void setSupplierSelect(List<Supplier> supplierList) {
		Supplier supplier = supplierList.size()==0? new Supplier(): supplierList.get(0);
		getDomain().setSupplier(supplier);
	}
	
	public void setCommoditySelect(List<Commodity> commodityList) {
		Commodity commodity = commodityList.size()==0? new Commodity(): commodityList.get(0);
		OrderDetail detail = this.getSelectedList().get(0);
		new CommodityLogic().fromTrunk(detail.getCommodity(), commodity);
	}
	
	private PurchaseTicket getPurchaseTicket() {
		PurchaseTicket ticket = getAttr(PurchaseTicket.class);
		if (ticket==null) {
			ticket = new PurchaseTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	private void setTaxPrice() {
		OrderDetail detail = this.getSelectedList().get(0);
		double rate=detail.getPurchaseTicket().getTaxRate(), untax=detail.getPurchaseTicket().getUntaxPrice(), price=detail.getPrice();
		if (untax>0) {
			detail.setPrice(untax * (1+rate/100));
		} else if (untax==0) {
			detail.getPurchaseTicket().setUntaxPrice(price / (1+rate/100));
		}
	}
	
	private ChooseFormer getTicketChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		former.setViewBuilder(getPurchaseChoosable().getTicketBuilder());
		former.setSellerViewSetting(getPurchaseChoosable().getChooseSetting( getPurchaseChoosable().getTicketBuilder() ));
		return former;
	}
	
	private ChooseFormer getDetailChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		former.setViewBuilder(getPurchaseChoosable().getDetailBuilder());
		former.setSellerViewSetting(getPurchaseChoosable().getChooseSetting( getPurchaseChoosable().getDetailBuilder() ));
		return former;
	}
	
	private PrintModelForm getPrintModelForm() {
		PrintModelForm form = this.getAttr(PrintModelForm.class);
		if (form == null) {
			AuditViewBuilder builder = (AuditViewBuilder)EntityClass.loadViewBuilder(PurchaseTicketForm.class, "Print");
			form = PrintModelForm.getForm(this, builder);
			this.setAttr(form);
		}
		return form;
	}
	private PurchaseTicketForm getTicketForm() {
		// 成品Form
		return this;
	}
	
	public NoteAccessorFormer<OrderDetail> getNoteFormer4Order() {
		String k = "NoteFormer4Order";
		NoteAccessorFormer accessor = this.getAttr(k);
		if (accessor==null) {
			accessor = new NoteAccessorFormer(OrderDetail.class);
			this.setAttr(k, accessor);
		}
		return accessor;
	}
	
	public NoteAccessorFormer<OrderDetail> getNoteFormer4Purchase() {
		String k = "NoteFormer4Purchase";
		NoteAccessorFormer accessor = this.getAttr(k);
		if (accessor==null) {
			accessor = new NoteAccessorFormer(OrderDetail.class);
			this.setAttr(k, accessor);
		}
		return accessor;
	}
	
	private SupplierForm getSupplierForm() {
		SupplierForm form = this.getAttr(SupplierForm.class);
		if (form == null) {
			form = new SupplierForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private CommodityForm getCommodityForm() {
		CommodityForm form = this.getAttr(CommodityForm.class);
		if (form == null) {
			form = new CommodityForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private SupplyTypeForm getSupplyTypeForm() {
		SupplyTypeForm form = this.getAttr(SupplyTypeForm.class);
		if (form == null) {
			form = new SupplyTypeForm();
			SupplyType supply = new SupplyType();
			supply.setName(new SupplyTypeLogic().getPurchaseType());
			form.setSupply(supply);
			this.setAttr(form);
		}
		return form;
	}
	
	private void setPurchaseTicketNumber() {
		this.getDomain().getPurchaseTicket().genSerialNumber();
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
	
	private HashMap<String, String> getParam4Supply() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("supplyType", "'采购'");
		return map;
	}
	
	public ActionService4LinkListener getActionService4LinkListener() {
		return this.getAttr(ActionService4LinkListener.class);
	}
	
	protected PropertyChoosableLogic.TicketDetail<ArrangeTicketForm, ArrangeTicket, ArrangeT> getArrangeChoosableLogic() {
		return new ArrangeTicketLogic().getPropertyChoosableLogic("采购");
	}
	
	protected <F extends PurchaseTicketForm> PropertyChoosableLogic.TicketDetail<F, PurchaseTicket, PurchaseT> getPurchaseChoosable() {
		return (PropertyChoosableLogic.TicketDetail<F, PurchaseTicket, PurchaseT>)new PurchaseTicketLogic().getTicketChoosableLogic();
	}
	
	private String getColumnOrder(LinkedHashMap<String, Object> params) {
		return new OrderTicketForm().getColumnOrder(params);
	}
}
