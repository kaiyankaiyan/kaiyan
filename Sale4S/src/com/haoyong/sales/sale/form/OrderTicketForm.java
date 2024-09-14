package com.haoyong.sales.sale.form;

import java.text.ParseException;
import java.util.ArrayList;
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
import net.sf.mily.support.form.SelectTicketFormer4Cross;
import net.sf.mily.support.form.SelectTicketFormer4Edit;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DateType;
import net.sf.mily.types.DoubleType;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.HtmlImage;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.SimpleDialog;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.AddNoteListener;
import net.sf.mily.webObject.AuditViewBuilder;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.EditViewBuilder;
import net.sf.mily.webObject.EntityField;
import net.sf.mily.webObject.Field;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ListViewBuilder;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.CType;
import com.haoyong.sales.base.domain.CType2;
import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.form.ClientForm;
import com.haoyong.sales.base.form.CommodityForm;
import com.haoyong.sales.base.form.SubCompanyForm;
import com.haoyong.sales.base.logic.CType2Logic;
import com.haoyong.sales.base.logic.CTypeLogic;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SellerViewInputsLogic;
import com.haoyong.sales.base.logic.SubCompanyLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FSqlListRemindable;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.listener.ActionService4LinkListener;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.LocationTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderDoption;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.ArrangeTicketLogic;
import com.haoyong.sales.sale.logic.OrderDoptionLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.OrderTypeLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.sale.logic.ReceiptTicketLogic;
import com.haoyong.sales.sale.logic.SendTicketLogic;
import com.haoyong.sales.test.base.ClientTest;
import com.haoyong.sales.test.sale.OrderTicketTest;

public class OrderTicketForm extends AbstractForm<OrderDetail> implements FViewInitable, FSqlListRemindable {
	
	private List<OrderDetail> selectedList;
	
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
	
	public void beforeCreate4Client(IEditViewBuilder builder0) {
		OrderDetail domain = new OrderDetail();
		this.setDomain(domain);
		this.getDetailList().clear();
		getDomain().getOrderTicket().setOrderType(new OrderTypeLogic().getClientType());
		getDrawList().delete(0, getDrawList().length()).append("Client,");
	}

	public void beforeCreate4Back(IEditViewBuilder builder0) {
		OrderDetail domain = new OrderDetail();
		this.setDomain(domain);
		this.getDetailList().clear();
		getDomain().getOrderTicket().setOrderType(new OrderTypeLogic().getBackType());
		getDrawList().delete(0, getDrawList().length());
	}

	public void canDoadjust(List<List<Object>> valiRows) {
		// commName,uneditable,stateId,arrangeId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (!(Integer.parseInt(row.get(2)+"")==30 || Integer.parseInt(row.get(2)+"")>=50)) {
				sitem.append("不可红冲，");
			} else if (Integer.parseInt(row.get(3)+"")==0) {
				sitem.append("请走未安排编辑，");
			}
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canReedit(List<List<Object>> valiRows) {
		// commName,uneditable,stateId,arrangeId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (!(Integer.parseInt(row.get(2)+"")==30 || Integer.parseInt(row.get(2)+"")>=50)) {
				sitem.append("不可红冲，");
			} else if (Integer.parseInt(row.get(3)+"")>0) {
				sitem.append("请走已排单红冲，");
			}
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canDoadjustConfirm(List<List<Object>> valiRows) {
		// commName,stateId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (Integer.parseInt(row.get(1)+"")!=52) {
				sb.append(row.get(0)).append("\t");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length()-1).append("不用红冲驳回确认！");
			throw new LogicException(2, sb.toString());
		}
	}
	
	public void canOrderChange(List<List<Object>> valiRows) {
		// commName,stateId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (Integer.parseInt(row.get(1)+"")!=40) {
				sb.append(row.get(0)).append("\t");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length()-1).append("不用订单改单处理！");
			throw new LogicException(2, sb.toString());
		}
	}
	
	public void canPurchaseChange(List<List<Object>> valiRows) {
		// commName,stateId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (Integer.parseInt(row.get(1)+"")!=43) {
				sb.append(row.get(0)).append("\t");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length()-1).append("不用采购改单处理！");
			throw new LogicException(2, sb.toString());
		}
	}
	
	public void preparePrintModel() {
		this.getDetailList().clear();
		OrderDetail detail = new OrderDetail();
		this.getDetailList().add(detail);
		this.setDomain(detail);
	}
	
	public void preparePrintOne() {
		this.getDetailList().clear();
		OrderTicketTest test = new OrderTicketTest();
		test.loadFormView(this, "ShowQuery", "DetailForm.selectedList", "clientName", "notnull");
		if (test.getListViewValue().size()==0)
			throw new LogicException(2, "订单查询中没有记录，无法打印预览!");
		test.setSqlAllSelect(test.getListViewValue().size());
		test.onMenu("选择OrderDetail");
		this.getDetailList().addAll(this.getSelectFormer4Order().getSelectedList());
		this.setDomain(this.getDetailList().get(0));
		this.setDrawListInit(this.getDetailList().get(0));
	}
	
	public void preparePrint(Component fcomp) {
		this.setDomain(this.getSelectFormer4Order().getFirst());
		this.getDetailList().clear();
		this.getDetailList().addAll(this.getSelectFormer4Order().getSelectedList());
		this.setDrawListInit(getSelectFormer4Order().getFirst());
		this.getPrintModelForm().showPrintOne(fcomp);
	}
	
	private void prepareDoadjust() {
		this.getCreateList4Simple().clear();
		this.setDomain(this.getSelectFormer4Order().getFirst());
		this.getDetailList().clear();
		this.getDetailList().addAll(getSelectFormer4Order().getSelectedList());
		this.setDrawListInit(getSelectFormer4Order().getFirst());
	}
	
	private void prepareShow() {
		this.setDomain(this.getSelectFormer4Order().getFirst());
		this.getDetailList().clear();
		this.getDetailList().addAll(getSelectFormer4Order().getSelectedList());
		this.setDrawListInit(getSelectFormer4Order().getFirst());
	}
	
	private void prepareAudit() throws Exception {
		this.getCreateList4Simple().clear();
		this.setDomain(this.getSelectFormer4Order().getFirst());
		this.getDetailList().clear();
		this.getDetailList().addAll(getSelectFormer4Order().getSelectedList());
		this.setDrawListInit(getSelectFormer4Order().getFirst());
	}
	
	public void prepareChange() {
		this.getCreateList4Simple().clear();
		this.setDomain(this.getSelectFormer4Order().getFirst());
		this.getDetailList().clear();
		this.getDetailList().addAll(getSelectFormer4Order().getSelectedList());
		this.setDrawListInit(getSelectFormer4Order().getFirst());
	}
	
	public void prepareDoadjustAdd() {
		this.getCreateList4Simple().clear();
		this.setDomain(this.getSelectFormer4Order().getFirst());
		for (OrderDetail from: getSelectFormer4Order().getSelectedList()) {
			OrderDetail d = new OrderDetail();
			d.setCommodity(from.getCommodity());
			d.setPrice(from.getPrice());
			getCreateList().add(d);
		}
		this.getDetailList().clear();
		this.setDrawListInit(getSelectFormer4Order().getFirst());
	}
	
	public void prepareDelete() {
		this.getDetailList().clear();
		this.getDetailList().addAll(selectedList);
	}
	
	public void preparePurChange() {
		getPurchaseList().clear();
		getPurchaseList().addAll(getSelectFormer4Purchase().getSelectedList());
		this.setDrawListInit(getSelectFormer4Purchase().getFirst());
	}
	
	public void prepareImport() {
		OrderDetail preDomain = this.getDomain();
		this.setDomain(new OrderDetail());
		this.getDomain().setVoparam(preDomain);
		this.getDomain().getVoParamMap().put("note", "对应列序号");
		this.getImportList().clear();
	}
	
	private void validateCreate(Component fcomp) {
		StringBuffer sb = new StringBuffer();
		sb.append(this.validateDetailList(this.getDetailList()));
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateReedit(Component fcomp) {
		StringBuffer sb = new StringBuffer();
		List<OrderDetail> dlist = new ArrayList<OrderDetail>();
		for (OrderDetail detail: dlist) {
			if (detail.getAmount()>0)
				dlist.add(detail);
		}
		sb.append(this.validateDetailList(dlist));
		if (new ClientTest().setWindow(fcomp.searchParentByClass(Window.class)).hasField("domain.voParamMap.ClientDoadjust"))
			sb.append("请先处理客户商家红冲，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private StringBuffer validateDetailList(List<OrderDetail> detailList) {
		StringBuffer sb = new StringBuffer();
		if (new OrderTicketLogic().getTicketChoosableLogic().isValid(getDomain().getOrderTicket(), sb)==false)
			sb.append("请补充订单信息，");
		if (getDrawList().indexOf("Client")>-1 && new ClientLogic().getPropertyChoosableLogic().isValid(getDomain().getClient(), sb) == false)
			sb.append("请填写客户信息，");
		if (getDrawList().indexOf("SubCompany")>-1 && new SubCompanyLogic().getPropertyChoosableLogic().isValid(getDomain().getSubCompany(), sb) == false)
			sb.append("请填写分公司信息，");
		for (OrderDetail detail: detailList) {
			StringBuffer sitem = new StringBuffer();
			if (new CommodityLogic().getPropertyChoosableLogic().isValid(detail.getCommodity(), sitem) == false)
				sitem.append("请填写商品信息，");
			if (detail.getAmount()==0)
				sitem.append("下单数量为0，");
			if (sitem.length() > 0)
				sb.append(sitem).append("\t");
		}
		return sb;
	}
	
	private void validateAuditYes() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isBlank(this.getDomain().getOrderTicket().getNumber())==true)
			sb.append("请填写订单号，");
		for (OrderDetail detail: this.getDetailList()) {
			StringBuffer sitem = new StringBuffer();
			if (this.getNoteFormer4Order().isNoted(detail))
				sitem.append("有申请改单内容请选择不同意！");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		sb.append(this.validateDetailList(this.getDetailList()));
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateAuditNo() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isBlank(this.getDomain().getOrderTicket().getNumber())==true)
			sb.append("请填写订单号，");
		boolean changeRemark = false;
		if (getNoteFormer4Order().isChangedRemark(this.getDomain())==false)
			sb.append("请补充原因到改单备注，");
		else
			changeRemark = true;
		List<OrderDetail> changedOrders = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getDetailList()) {
			StringBuffer sitem = new StringBuffer();
			if (!changeRemark && this.getNoteFormer4Order().isNoted(detail)==false)
				sitem.append("无改单内容！");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
			else {
				OrderDetail changed = new OrderTicketLogic().genCloneOrder(detail);
				this.getNoteFormer4Order().setEntityChanges(changed, this.getNoteFormer4Order().getVoNoteMap(detail));
			}
		}
		sb.append(this.validateDetailList(changedOrders));
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateChangeYes() {
		if (getNoteFormer4Order().isNoted(getDomain())==false)
			throw new LogicException(2, "清空申请改单内容，请选择不同意！");
	}
	
	private void validateChangeNo() {
		if (getNoteFormer4Order().isChangedRemark(getDomain())==false)
			throw new LogicException(2, "请补充原因到改单备注");
	}
	
	private void validateClientLinkReedit(Component fcomp) {
		StringBuffer sb=new StringBuffer(), sitem=null;
		ListView lview = fcomp.searchParentByClass(Window.class).getInnerFormerList(ListView.class).get(0);
		"".toCharArray();
		for (OrderDetail detail: this.getSelectEdit4Order().getSelectedList()) {
			EntityField efield = lview.getListBuilder().getEntityField((BlockGrid)lview.getComponent(), this.getSelectFormer4Order().getSelectedList().indexOf(detail));
			sitem = new StringBuffer();
			if (this.getNoteFormer4Order().getVoNoteMapIN((OrderDetail)detail.getSnapShot(), "commodity", "amount", "OrderTicket.cprice").size()==0)
				sitem.append("要有客户商家改单内容，");
			ListViewBuilder vbuilder = (ListViewBuilder)efield.getFieldBuilder();
			if (this.getNoteFormer4Order().getVoNoteMapIN(detail, "commodity").size()>0) {
				StringBuffer sb1=new StringBuffer(), sb0=new StringBuffer();
				for (Iterator<FieldBuilder> citer=((ViewBuilder)vbuilder.getFieldBuilder("commodity")).getFieldBuilderLeafs().iterator(); citer.hasNext();) {
					FieldBuilder fbuilder=citer.next();
					Object v1=fbuilder.getEntityPropertyValue(detail), v0=fbuilder.getEntityPropertyValue(detail.getSnapShot());
					sb0.append(v0).append(",");
					sb1.append(v1).append(",");
				}
				if (StringUtils.equals(sb0.toString(), sb1.toString()))
					sitem.append("商品有红冲请调整，");
			}
			if (this.getNoteFormer4Order().getVoNoteMapIN(detail, "amount").size()>0) {
				StringBuffer sb1=new StringBuffer(), sb0=new StringBuffer();
				for (Iterator<FieldBuilder> citer=vbuilder.getFieldBuilders("amount").values().iterator(); citer.hasNext();) {
					FieldBuilder fbuilder=citer.next();
					Object v1=fbuilder.getEntityPropertyValue(detail), v0=fbuilder.getEntityPropertyValue(detail.getSnapShot());
					sb0.append(v0).append(",");
					sb1.append(v1).append(",");
				}
				if (StringUtils.equals(sb0.toString(), sb1.toString()))
					sitem.append("数量有红冲请调整，");
			}
			if (this.getNoteFormer4Order().getVoNoteMapIN(detail, "cprice").size()>0) {
				StringBuffer sb1=new StringBuffer(), sb0=new StringBuffer();
				for (Iterator<FieldBuilder> citer=vbuilder.getFieldBuilders("cprice").values().iterator(); citer.hasNext();) {
					FieldBuilder fbuilder=citer.next();
					Object v1=fbuilder.getEntityPropertyValue(detail), v0=fbuilder.getEntityPropertyValue(detail.getSnapShot());
					sb0.append(v0).append(",");
					sb1.append(v1).append(",");
				}
				if (StringUtils.equals(sb0.toString(), sb1.toString()))
					sitem.append("价格有红冲请调整，");
			}
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem.toString()).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		else {
			for (OrderDetail detail: this.getSelectEdit4Order().getSelectedList()) {
				detail.setStOrder(30);
				this.getNoteFormer4Order().removeVoNoteMap(this.getNoteFormer4Order().getVoNoteMap(detail), "commodity", "amount", "OrderTicket.cprice");
			}
		}
	}
	private void validateClientLinkDoadjust(Component fcomp) {
		StringBuffer sb=new StringBuffer(), sitem=null;
		ListView lview = fcomp.searchParentByClass(Window.class).getInnerFormerList(ListView.class).get(0);
		"".toCharArray();
		for (OrderDetail detail: this.getSelectEdit4Order().getSelectedList()) {
			EntityField efield = lview.getListBuilder().getEntityField((BlockGrid)lview.getComponent(), this.getSelectFormer4Order().getSelectedList().indexOf(detail));
			sitem = new StringBuffer();
			if (this.getNoteFormer4Order().getVoNoteMapIN((OrderDetail)detail, "commodity", "amount", "OrderTicket.cprice").size()==0)
				sitem.append("要有客户商家改单内容，");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem.toString()).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		else {
			for (OrderDetail detail: this.getSelectEdit4Order().getSelectedList())
				detail.setStOrder(30);
		}
	}
	
	private void validateDoadjust(Component fcomp) throws ParseException {
		StringBuffer sb = new StringBuffer();
		if (this.getCreateList().size()>0)
			sb.append("有新增明细请先提交新增，");
		else if (this.getDetailList().size()==0)
			sb.append("没有红冲的明细");
		else {
			if (this.getNoteFormer4Order().isChangedRemark(getDomain())==false)
				sb.append("请补充红冲申请原因，");
			if (this.getDetailList().size()==0)
				sb.append("没有红冲明细，");
			if (new ClientTest().setWindow(fcomp.searchParentByClass(Window.class)).hasField("domain.voParamMap.ClientDoadjust"))
				sb.append("请先处理客户商家红冲，");
			for (OrderDetail detail: this.getDetailList()) {
				Double toAmount = (Double)getNoteFormer4Order().getNoteValue(detail, new StringBuffer("amount"));
				if (toAmount!=null && toAmount>detail.getAmount()) {
					throw new LogicException(2, new StringBuffer().append(toAmount).append("大于原订单数量").append(detail.getAmount()).append(", 请复制新增明细").toString());
				}
				if (this.getNoteFormer4Order().isNoted(detail)==false)
					sb.append("请填写红冲更改内容");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	private void validateDoadjustAdd() {
		StringBuffer sb = new StringBuffer();
		if (this.getCreateList().size()==0)
			sb.append("无新增明细不可提交新增，");
		else {
			if (getNoteFormer4Order().isChangedNotesIN(this.getDomain(), "client", "subCompany", "OrderTicket"))
				sb.append("单头有更改不能复制新增明细");
			sb.append(this.validateDetailList(this.getCreateList()));
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	// 同意更改订单信息项
	private void validatePurchaseChangeYes() {
		StringBuffer sb = new StringBuffer();
		if (this.getOrderAgreeList().isEmpty())
			sb.append("请选择同意的订单内容，");
		for (OrderDetail detail: this.getPurchaseList()) {
			if (getNoteFormer4Purchase().isNoted(detail)==false)
				sb.append("改单申请内容不能为空，");
			if (this.isOrderAgreeAll()==false && getNoteFormer4Purchase().isChangedRemark(detail)==false)
				sb.append("有不同意项，请补充改单申请备注，");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.insert(0, "订单同意，").deleteCharAt(sb.length()-1).toString());
	}
	
	// 拒绝采购改单
	private void validatePurchaseChangeNo() {
		StringBuffer sb = new StringBuffer();
		if (this.getOrderAgreeList().size()>0) {
			sb.append("有同意项请先订单同意处理！");
		} else {
			for (OrderDetail detail: this.getPurchaseList()) {
				if (getNoteFormer4Purchase().isChangedNotesEX(detail, "OrderAgree"))
					sb.append("改单申请内容不能更改！");
				if (getNoteFormer4Purchase().isChangedRemark(detail)==false)
					sb.append("请补充改单申请备注！");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.insert(0, "拒绝采购改单，").deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateSplitAmount() {
		StringBuffer sb=new StringBuffer();
		if (this.getSelectFormer4Order().getSelectedList().isEmpty())
			sb.append("请选择要拆分的订单，");
		for (OrderDetail detail: this.getSelectFormer4Order().getSelectedList()) {
			if ( !(0<getOrderDetail().getAmount() && getOrderDetail().getAmount()<detail.getAmount()) )
				sb.append("拆分数量应小于订单数量，");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateToImport(Component fcomp) throws Exception {
		this.getBaseImportForm().validateIndexes(fcomp);
		if ("保存列序号配置到Derby".length()>0) {
			ListView listview = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0);
			SellerViewInputs inputs = this.getBaseImportForm().getSellerViewInputs();
			this.getBaseImportForm().setSellerIndexes(listview, inputs);
			new SellerViewInputsLogic().saveOrUpdate(inputs);
		}
	}
	
	private void validateImport() throws Exception {
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
	
	public void setVersionDomains() {
		this.getSelectFormer4Order().setIdDomains(this.getDetailList());
	}
	
	public void addDetail() {
		OrderDetail d = new OrderDetail();
		this.getDetailList().add(d);
	}
	
	public void addDetail4Commodity() {
		for (Commodity comm: this.getSelectFormer4Commodity().getSelectedList()) {
			StoreEnough store = comm.getVoparam(StoreEnough.class);
			OrderDetail d = new OrderDetail();
			d.setVoparam(comm);
			d.setCommodity(store.getCommodity());
			d.setAmount(comm.getMinInventory()-(store.getStoreAmount()+store.getRequestAmount()+store.getOnroadAmount()));
			this.getDetailList().add(d);
		}
	}
	
	public void addDetail4Create() {
		OrderDetail d = new OrderDetail();
		getCreateList().add(d);
	}
	
	public void addDetail4CreateCopy() {
		for (OrderDetail from: this.selectedList) {
			OrderDetail d = new OrderDetail();
			d.setCommodity(from.getCommodity());
			d.setPrice(from.getPrice());
			getCreateList().add(d);
		}
	}
	
	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true)
			new OrderTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new PurchaseTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new ReceiptTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new SendTicketLogic().getSendChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new SupplierLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (getDrawList().indexOf("Client")>-1)
			new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (getDrawList().indexOf("SubCompany")>-1)
			new SubCompanyLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (viewBuilder.getName().equals("Import"))
			this.getBaseImportForm().setImportBuilderInit(viewBuilder);
	}
	
	public void setHyperlink(Component fromComp) {
		String winId=fromComp.searchParentByClass(Window.class).getIdentifier();
		HtmlImage image = (HtmlImage)fromComp.getInnerComponentList(HtmlImage.class).get(0);
		StringBuffer openStr=new StringBuffer();
		for (int isize=getSelectFormer4Order().getSelectedList().size(),i=0; i<isize; i++) {
			String url="actionform.jsp?action=sale.OrderTicketForm&view=Print&preparemethod=getPrint&window="+winId+"&iprint="+i;
			openStr.append("window.open('").append(url).append("','_blank');");
		}
		image.addAttribute(ClientEventName.InitScript0, openStr.toString());
	}
	
	private HashMap<String, String> getParam4Order() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("SubCompany", "c.subName is null");
		return map;
	}
	
	/**
	 * 低于安全库存的记录，过滤已经添加备货的
	 */
	private HashMap<String, String> getParam4Enough(){
		Set<String> commnameList = new LinkedHashSet<String>();
		HashMap<String, String> params = new HashMap<String, String>();
		String k="limitCommName", c="c.commNumber";
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		boolean hasCommodity = false;
		for (Iterator<OrderDetail> iter=this.getDetailList().iterator(); iter.hasNext();) {
			Commodity item = iter.next().getCommodity();
			if (item.getCommNumber()!=null) {
				sb.append(c).append("!='").append(item.getCommNumber()).append("'").append(" and ");
				hasCommodity = true;
			}
		}
		if (hasCommodity)
			sb.delete(sb.length()-5, sb.length()).append(")");
		else
			sb.append("1=1)");
		params.put(k, sb.toString());
		return params;
	}

	private void getCommoditySearchNumber(TextField input) {
		this.setIsDialogOpen(false);
		String number = input.getText();
		if (StringUtils.isNotEmpty(number)){
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "CommodityQuery", 1|2, "commNumber", number)==false)
				this.setIsDialogOpen(true);
		}
	}

	private void getProjectSearchNumber(TextField input) {
		this.setIsDialogOpen(false);
		String number = input.getText();
		if (StringUtils.isNotEmpty(number)){
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "ProjectQuery", 1|2|4, "proNumber", number)==false)
				this.setIsDialogOpen(true);
		}
	}
	private void getStorehouseSearchName(TextField input) {
		this.setIsDialogOpen(false);
		if (StringUtils.isNotEmpty(input.getText())){
			String name = new StringBuffer().append("like %").append(input.getText()).append("%").toString();
			ClientTest test = new ClientTest();
			OrderTicketForm form = new OrderTicketForm();
			test.loadFormView(form, "Create");
			boolean isOut = ((Field)input.getFormer()).getFieldBuilder().getName().contains("out.");
			if (isOut)
				test.onMenu("选择cc");
			else
				test.onMenu("选择cr");
			for (boolean once=true,got=false; once; once=false) {
				test.setFilters("ShowQuery.selectedList", "name", name);
				if (test.getListViewValue().size()==1) {
					test.setSqlAllSelect(1);
					test.onMenu("确定分公司");
					got = true;
				}
				if (got == false) {
					test.setFilters("selectFormer4User.selectedList", "userName", name);
					if (test.getListViewValue().size()==1) {
						test.setSqlAllSelect(1);
						test.onMenu("确定人员");
						got = true;
					}
				}
				if (got==true) {
					if (isOut)
						this.getDomain().getLocationTicket().setOut(form.getDomain().getLocationTicket().getOut());
					else
						this.getDomain().getLocationTicket().setTo(form.getDomain().getLocationTicket().getTo());
					return;
				}
			}
		}
		this.setIsDialogOpen(true);
	}

	private double getTotalAmount() {
		double amount = 0;
		for (OrderDetail detail: this.getDetailList()) {
			amount += detail.getAmount();
		}
		return amount;
	}
	private void getDetailsOfTicket(Component fcomp) {
		OrderTicket ticket = this.getSelectFormer4OrderTicket().getFirst();
		ClientTest test = new ClientTest().setWindow(fcomp.searchParentByClass(Window.class));
		int cnt = 0;
		if (new OrderTypeLogic().isClientType(ticket.getOrderType())) {
			test.loadSqlView(this, "ShowQuery", "DetailForm.selectedList", "number", ticket.getNumber(), "stOrder", ">=30");
			cnt = test.getListViewValue().size();
		}
		if (cnt==0) {
			test.loadSqlView(this, "ShowQuery", "DetailForm.selectedList", "number", ticket.getNumber(), "arrangeId", ">0");
			cnt = test.getListViewValue().size();
		}
		if (cnt > 0) {
			test.setSqlAllSelect(test.getListViewValue().size());
			test.onMenu("查看");
		}
		fcomp.searchParentByClass(Window.class).getSubComponents().setChanged();
	}
	
	public void setChangeNotes(Component fcomp) {
		Window win = fcomp.searchParentByClass(Window.class);
		List<AddNoteListener> noteList = win.getInnerFormerList(AddNoteListener.class);
		Map<String, String> domainMap = new HashMap<String, String>();
		Map<String, String> domainAll = this.getNoteFormer4Order().getVoNoteMap(this.getDomain());
		for (Iterator<AddNoteListener> iter=noteList.iterator(); iter.hasNext();) {
			AddNoteListener note = iter.next();
			String name=note.getPropertyName(), value=note.getNoteString();
			if (domainAll.containsKey(name)==false)
				continue;
			ListView listview = note.getComponent().searchFormerByClass(ListView.class);
			if (!(listview==null && note.getEntity()==getDomain())) {
				continue;
			}
			domainMap.put(name, value);
		}
		for (OrderDetail detail: this.getDetailList()) {
			this.getNoteFormer4Order().getVoNoteMap(detail).putAll(domainMap);
			this.getNoteFormer4Order().isChangedNotesEX(detail);
			detail.setChangeRemark(this.getDomain().getChangeRemark());
		}
	}
	
	private void setOrderChangeNotes(Component fcomp) {
		Window win = fcomp.searchParentByClass(Window.class);
		List<AddNoteListener> noteList = win.getInnerFormerList(AddNoteListener.class);
		Map<String, String> domainMap = new HashMap<String, String>();
		Map<String, String> domainAll = this.getNoteFormer4Order().getVoNoteMap(this.getDomain());
		for (Iterator<AddNoteListener> iter=noteList.iterator(); iter.hasNext();) {
			AddNoteListener note = iter.next();
			String name=note.getPropertyName(), value=note.getNoteString();
			if (domainAll.containsKey(name)==false)
				continue;
			ListView listview = note.getComponent().searchFormerByClass(ListView.class);
			if (!(listview==null && note.getEntity()==this.getDomain())) {
				continue;
			}
			domainMap.put(name, value);
		}
		for (OrderDetail detail: this.getDetailList()) {
			this.getNoteFormer4Order().getVoNoteMap(detail).putAll(domainMap);
			this.getNoteFormer4Order().isChangedNotesEX(detail);
			detail.setChangeRemark(this.getDomain().getChangeRemark());
		}
	}
	
	private void setPurchaseChangeNotes(Component fcomp) {
		Window win = fcomp.searchParentByClass(Window.class);
		List<AddNoteListener> noteList = win.getInnerFormerList(AddNoteListener.class);
		Map<String, String> domainMap = new HashMap<String, String>();
		Map<String, String> domainAll = this.getNoteFormer4Purchase().getVoNoteMap(this.getPurchaseFirst());
		for (Iterator<AddNoteListener> iter=noteList.iterator(); iter.hasNext();) {
			AddNoteListener note = iter.next();
			String name=note.getPropertyName(), value=note.getNoteString();
			if (domainAll.containsKey(name)==false)
				continue;
			ListView listview = note.getComponent().searchFormerByClass(ListView.class);
			if (!(listview==null && note.getEntity()==getPurchaseFirst())) {
				continue;
			}
			domainMap.put(name, value);
		}
		for (OrderDetail detail: this.getPurchaseList()) {
			this.getNoteFormer4Purchase().getVoNoteMap(detail).putAll(domainMap);
			this.getNoteFormer4Purchase().isChangedNotesEX(detail);
			detail.setChangeRemark(this.getPurchaseFirst().getChangeRemark());
		}
	}
	
	private void setDetailSupplyType(Component fcomp) {
		ClientTest test = new ClientTest().setWindow(fcomp.searchParentByClass(Window.class));
		if (test.hasField("commodity.supplyType")==false)
		for (OrderDetail detail: this.getDetailList()) {
			if (detail.getCommodity().getSupplyType()==null)
				detail.getCommodity().setSupplyType(new SupplyTypeLogic().getPurchaseType());
		}
	}
	
	private OrderTicket getOrderTicket() {
		OrderTicket ticket = this.getAttr(OrderTicket.class);
		if (ticket==null) {
			ticket = new OrderTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	private LocationTicket getLocationTicket() {
		LocationTicket ticket = this.getAttr(LocationTicket.class);
		if (ticket==null) {
			ticket = new LocationTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	private void setAmountByWeight() {
		OrderDetail detail = this.getSelectedList().get(0);
		double aweight=detail.getCommodity().getAweight(), weight=detail.getOrderTicket().getWeight();
		if (aweight>0 && weight>0) {
			detail.setAmount(weight / aweight);
		}
	}
	
	private void setTaxPrice() {
		OrderDetail detail = this.getSelectedList().get(0);
		double rate=detail.getOrderTicket().getTaxRate(), untax=detail.getOrderTicket().getUntaxPrice(), price=detail.getPrice();
		if (untax>0) {
			detail.setPrice(untax * (1+rate/100));
		} else if (untax==0) {
			detail.getOrderTicket().setUntaxPrice(price / (1+rate/100));
		}
	}
	
	private ChooseFormer getTicketChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new OrderTicketLogic().getTicketChoosableLogic();
		former.setViewBuilder(logic.getTicketBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getTicketBuilder()));
		return former;
	}
	
	private ChooseFormer getDetailChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new OrderTicketLogic().getTicketChoosableLogic();
		former.setViewBuilder(logic.getDetailBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getDetailBuilder()));
		return former;
	}
	
	public OrderDetail getDomain() {
		String k = "OrderTicketForm.Domain";
		return this.getAttr(k);
	}
	
	protected void setDomain(OrderDetail domain) {
		String k = "OrderTicketForm.Domain";
		this.setAttr(k, domain);
	}
	
	private OrderDetail getOrderDetail() {
		String k = "OrderDetail";
		OrderDetail detail = this.getAttr(k);
		if (detail==null) {
			detail = new OrderDetail();
			this.setAttr(k, detail);
		}
		return detail;
	}
	
	private void setOrderNumberNew() {
		this.getDomain().getOrderTicket().genSerialNumber();
	}
	
	private void setProjectNumberNew() {
		this.getDomain().getOrderTicket().genSerialNumber4Project();
	}
	
	private void setOrderDetailClear() {
		this.setAttr(new OrderDetail());
	}
	
	private OrderTicketForm getOrderTicketForm() {
		return this;
	}
	
	private BOrderTicketForm getBOrderTicketForm() {
		BOrderTicketForm form = getAttr(BOrderTicketForm.class);
		if (form == null) {
			form = new BOrderTicketForm();
			setAttr(form);
		}
		TicketPropertyUtil.copyFieldsSkip(this, form);
		return form;
	}
	
	private OrderTypeForm getOrderTypeForm() {
		OrderTypeForm form = getAttr(OrderTypeForm.class);
		if (form == null) {
			form = new OrderTypeForm();
			form.beforeList(null);
			setAttr(form);
		}
		return form;
	}

	private OrderDoptionForm getOrderDoptionForm() {
		OrderDoptionForm form = getAttr(OrderDoptionForm.class);
		if (form == null) {
			form = new OrderDoptionForm();
			form.beforeList(null);
			setAttr(form);
		}
		return form;
	}
	private SubCompanyForm getSubCompanyForm() {
		SubCompanyForm form = getAttr(SubCompanyForm.class);
		if (form == null) {
			form = new SubCompanyForm();
			setAttr(form);
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

	public OrderDetail getPurchaseFirstOrder() {
		return this.getPurchaseFirst();
	}
	
	public OrderDetail getPurchaseFirst() {
		if (this.getPurchaseList().size()>0) {
			OrderDetail p = this.getPurchaseList().get(0);
			this.setAttr(p);
			return p;
		}
		OrderDetail p = this.getAttr(OrderDetail.class);
		return p;
	}
	
	public List<OrderDetail> getDetailList() {
		String k = "OrderDetailList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<OrderDetail> getPurchaseList() {
		String k = "PurchaseList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	protected StringBuffer getDrawList() {
		String k = "DrawList";
		StringBuffer list = this.getAttr(k);
		if (list == null) {
			list = new StringBuffer();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private void setDrawListInit(OrderDetail detail) {
		this.getDrawList().delete(0, this.getDrawList().length());
		if (new OrderTypeLogic().isClientType(detail.getOrderTicket().getOrderType()))
			this.getDrawList().append("Client,");
		if (detail.getSubCompany().getName()!=null)
			this.getDrawList().append("SubCompany,");
	}
	
	private List<OrderDetail> getCreateList4Simple() {
		List<OrderDetail> list = this.getAttr("createList");
		return list==null? new ArrayList<OrderDetail>(0): list;
	}
	
	/**
	 * 红冲新增的订单明细
	 */
	public List<OrderDetail> getCreateList() {
		String k = "createList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private void setCreateListClear() {
		this.getCreateList().clear();
	}
	
	/**
	 * 红冲取消订单，明细数量为0
	 */
	public void setZeroAmount() {
		for (OrderDetail detail: this.selectedList) {
			this.getNoteFormer4Order().getVoNoteMap(detail).put("amount", "0");
		}
	}
	
	private void setTicket4Service(ViewData<OrderTicket> viewData) {
		OrderTicket ticket=this.getDomain().getOrderTicket(), tload=new OrderTicketLogic().getTicket(ticket.getNumber());
		if (tload==null)		tload=new OrderTicket();
		if ("写入单头".length()>0) {
			new OrderTicketLogic().getTicketChoosableLogic().fromTrunk(tload, ticket);
			tload.setClientName((String)ReflectHelper.getPropertyValue(this.getDomain(), "TClient.clientName"));
			tload.setSubName((String)ReflectHelper.getPropertyValue(this.getDomain(), "TSubCompany.subName"));
		}
		viewData.setTicketDetails(tload);
		this.getDomain().setVoparam(tload);
	}
	private void setTicketDoadjust4Service(ViewData<OrderTicket> viewData) {
		OrderDetail tdomain = TicketPropertyUtil.deepClone(this.getDomain());
		OrderTicket tload=new OrderTicketLogic().getTicket(tdomain.getOrderTicket().getNumber());
		if ("写入更改".length()>0) {
			Assert.assertTrue("已有订单单头", tload.getSnapShot().getId()>0);
			LinkedHashMap<String, String> map = new OrderTicketLogic().getVoNoteMap(this.getNoteFormer4Order(), this.getDomain(), OrderTicket.class);
			this.getNoteFormer4Order().setEntityChanges(tdomain, map);
			TicketPropertyUtil.copyFieldsSkip(tdomain, tload);
			tload.setClientName((String)ReflectHelper.getPropertyValue(tdomain, "TClient.clientName"));
			tload.setSubName((String)ReflectHelper.getPropertyValue(tdomain, "TSubCompany.subName"));
		}
		viewData.setTicketDetails(tload);
		this.getDomain().setVoparam(tload);
	}
	
	private void setOrderCount4Service(ViewData<OrderTicket> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4OrderTicket().getSelectedList());
	}
	
	private void setCreate4Service(ViewData<OrderDetail> viewData) {
		OrderDetail domain = this.getDomain();
		viewData.setTicketDetails(this.getDetailList());
		PropertyChoosableLogic.TicketDetail logic = new OrderTicketLogic().getTicketChoosableLogic();
		for (OrderDetail detail: viewData.getTicketDetails()) {
			detail.setMonthnum(new OrderTicketLogic().genMonthnum());
			logic.fromTrunk(logic.getTicketBuilder(), detail.getOrderTicket(), domain.getOrderTicket());
			detail.setClient(domain.getClient());
			detail.setSubCompany(domain.getSubCompany());
		}
	}
	
	private void setEffect4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getDetailList());
	}
	
	private ActionService4LinkListener getPurchaseLink() {
		ActionService4LinkListener listener = new ActionService4LinkListener();
		long clientId = this.getDomain().getClient().getFromSellerId();
		long companyId = this.getDomain().getSubCompany().getFromSellerId();
		Seller toSeller = new Seller4lLogic().getSellerById(clientId>0? clientId: companyId);
		listener.getOnceAttributes().put("seller", toSeller);
		User user = TicketPropertyUtil.copyProperties(this.getUser(), new User());
		user.setUserName(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
		listener.getOnceAttributes().put("user", user);
		this.setAttr(listener);
		return listener;
	}
	
	private void setPurchaseLink4Service(ViewData<OrderDetail> viewData) {
		List<String> monthnumList = new ArrayList<String>();
		for (OrderDetail order: this.getDetailList()) {
			int idx=order.getMonthnum().indexOf("_");
			monthnumList.add(idx==-1? order.getMonthnum(): order.getMonthnum().substring(0, idx));
		}
		List<OrderDetail> purList = new PurchaseTicketLogic().getDetails(monthnumList);
		for (OrderDetail pur: purList) {
			for (OrderDetail ord: this.getDetailList()) {
				if (ord.getMonthnum().startsWith(pur.getMonthnum())) {
					this.getNoteFormer4Purchase().getVoNoteMap(pur).putAll(this.getNoteFormer4Order().getVoNoteMap(ord));
					this.getNoteFormer4Purchase().isChangedNotesEX(pur);
					pur.setChangeRemark(ord.getChangeRemark());
					break;
				}
			}
		}
		viewData.setTicketDetails(purList);
	}
	
	private void setReeditSave4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		PropertyChoosableLogic.TicketDetail logic=new OrderTicketLogic().getTicketChoosableLogic();
		for (OrderDetail detail: this.getDetailList()) {
			if (detail.getAmount()==0)
				continue;
			logic.fromTrunk(logic.getTicketBuilder(), detail.getOrderTicket(), this.getDomain().getOrderTicket());
			detail.setClient(this.getDomain().getClient());
			detail.setSubCompany(this.getDomain().getSubCompany());
			list.add(detail);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setReeditDelete4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getDetailList()) {
			if (detail.getAmount()==0 && detail.getId()>0)
				list.add(detail);
		}
		viewData.setTicketDetails(list);
	}

	private void setArrange4Service(ViewData<OrderDetail> viewData) {
		OrderDetail domain = getDomain();
		domain.getArrangeTicket().genSerialNumber();
		domain.getArrangeTicket().setArrangeType(new DeliverTypeLogic().getCommonType());
		viewData.setTicketDetails(this.getDetailList());
		for (OrderDetail detail: viewData.getTicketDetails()) {
			PropertyChoosableLogic.TicketDetail logic=new ArrangeTicketLogic().getPropertyChoosableLogic(detail.getCommodity().getSupplyType());
			logic.fromTrunk(logic.getTicketBuilder(), detail.getArrangeTicket(), domain.getArrangeTicket());
		}
	}
	
	private void setAdd4Service(ViewData<OrderDetail> viewData) {
		this.getDomain().getOrderTicket().genSerialNumber();
		PropertyChoosableLogic.TicketDetail logic=new OrderTicketLogic().getTicketChoosableLogic();
		for (OrderDetail detail: this.getCreateList()) {
			detail.setMonthnum(new OrderTicketLogic().genMonthnum());
			logic.fromTrunk(logic.getTicketBuilder(), detail.getOrderTicket(), this.getDomain().getOrderTicket());
			detail.setClient(this.getDomain().getClient());
			detail.setSubCompany(this.getDomain().getSubCompany());
		}
		viewData.setTicketDetails(this.getCreateList());
	}
	
	private void setChange4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getDetailList());
		viewData.setParam(this.getNoteFormer4Order());
		viewData.setParam("ChangeType", OrderTicket.class);
	}
	
	private void setChangeNo4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getDetailList());
	}
	
	private void setDoadjust4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getDetailList());
		viewData.setParam(this.getNoteFormer4Order());
		viewData.setParam("AdjustType", OrderTicket.class);
	}
	
	private void setPurChange4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getPurchaseList());
	}
	
	private void setSplitNew4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail d = new OrderTicketLogic().genCloneOrder(detail);
			d.setMonthnum(new OrderTicketLogic().getSplitMonthnum(detail.getMonthnum()));
			d.setAmount(this.getOrderDetail().getAmount());
			detail.getVoParamMap().put("NewOrder", d);
			orderList.add(d);
		}
		viewData.setTicketDetails(orderList);
	}
	
	private void setSplitRemain4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectFormer4Order().getSelectedList()) {
			detail.setAmount(detail.getAmount() - this.getOrderDetail().getAmount());
			orderList.add(detail);
		}
		viewData.setTicketDetails(orderList);
	}
	
	private List<String> getOrderAgreeList() {
		Object v = getNoteFormer4Purchase().getNoteValue(getPurchaseFirst(), new StringBuffer("OrderAgree"));
		return v instanceof List? (List<String>)v: new ArrayList<String>(0);
	}
	
	public boolean isOrderAgreeAll() {
		List options = this.getOrderAgreeOptions(null);
		return getOrderAgreeList().size()>0 && getOrderAgreeList().size()==options.size();
	}
	
	public List<OrderDetail> getSelectedList() {
		return this.selectedList;
	}

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
		this.selectedList = selected;
	}
	
	private List<SupplyType> getSupplyTypeOptions(Object entity) {
		List<SupplyType> typeList = new SupplyTypeLogic().getTypeList();
		return typeList;
	}
	
	private List<OrderDoption> getOrderDOptions(Object t) {
		return new OrderDoptionLogic().getTypeList();
	}
	
	private List<CType> getCTypeList(Commodity entity) {
		List<CType> typeList = new CTypeLogic().getTypeList();
		return typeList;
	}
	
	public List<CType2> getCType2List(Commodity entity) {
		List<CType2> type2List = new CType2Logic().getDomain().getInfoList();
		return type2List;
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
	
	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	private OrderTicketForm getTicketForm() {
		return this;
	}
	private OrderTicketForm getDetailForm() {
		return this;
	}
	
	private List<Map.Entry<String, String>> getOrderAgreeOptions(Object adetail) {
		LinkedHashMap<String, String> mapAll = new LinkedHashMap<String, String>();
		mapAll.put("OrderTicket", "订单");
		mapAll.put("client", "客户");
		mapAll.put("commodity", "更改商品");
		mapAll.put("amount", "数量");
		LinkedHashMap<String, String> mapCur = new LinkedHashMap<String, String>();
		for (OrderDetail detail: getPurchaseList()) {
			List<String> sagree = (List)this.getNoteFormer4Purchase().getSourceNoteValue(detail, new StringBuffer("OrderAgree"));
			for (String s: sagree) {
				mapCur.put(s, mapAll.get(s));
			}
		}
		return new ArrayList<Entry<String, String>>(mapCur.entrySet());
	}

	public NoteAccessorFormer<OrderDetail> getNoteFormer4Order() {
		NoteAccessorFormer accessor = this.getAttr(NoteAccessorFormer.class);
		if (accessor==null) {
			accessor = new NoteAccessorFormer(OrderDetail.class);
			this.setAttr(accessor);
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
	
	private PrintModelForm getPrintModelForm() {
		PrintModelForm form = this.getAttr(PrintModelForm.class);
		if (form == null) {
			AuditViewBuilder builder = (AuditViewBuilder)EntityClass.loadViewBuilder(OrderTicketForm.class, "Print");
			if (true)
				new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(builder);
			if (true)
				new SubCompanyLogic().getPropertyChoosableLogic().trunkViewBuilder(builder);
 			form = PrintModelForm.getForm(this, builder);
			this.setAttr(form);
		}
		return form;
	}
	
	public ClientForm getClientForm() {
		ClientForm form = this.getAttr(ClientForm.class);
		if (form == null) {
			form = new ClientForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public CommodityForm getCommodityForm() {
		CommodityForm form = this.getAttr(CommodityForm.class);
		if (form == null) {
			form = new CommodityForm();
			this.setAttr(form);
		}
		return form;
	}
	
	private BaseImportForm getBaseImportForm() {
		BaseImportForm form = this.getAttr(BaseImportForm.class);
		if (form==null) {
			form = new BaseImportForm();
			this.setAttr(form);
		}
		return form;
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
	
	protected void setIsDialogOpen(boolean open) {
		String k = "IsDialogOpen";
		this.setAttr(k, open);
	}
	
	public SelectTicketFormer4Sql<OrderTicketForm, OrderDetail> getSelectFormer4Purchase() {
		String k="SelectFormer4Purchase";
		SelectTicketFormer4Sql<OrderTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OrderTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<OrderTicketForm, OrderDetail> getSelectFormer4Order() {
		String k="SelectFormer4Order";
		SelectTicketFormer4Sql<OrderTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OrderTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	public SelectTicketFormer4Sql<OrderTicketForm, OrderTicket> getSelectFormer4OrderTicket() {
		String k="SelectFormer4OrderTicket";
		SelectTicketFormer4Sql<OrderTicketForm, OrderTicket> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OrderTicketForm, OrderTicket>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	private SelectTicketFormer4Cross<OrderTicketForm> getSelectCross4OrderTicket() {
		String k="SelectCross4OrderTicket";
		SelectTicketFormer4Cross<OrderTicketForm> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Cross<OrderTicketForm>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	private SelectTicketFormer4Sql<OrderTicketForm, SubCompany> getSelectFormer4SubCompany() {
		String k="SelectFormer4SubCompany";
		SelectTicketFormer4Sql<OrderTicketForm, SubCompany> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OrderTicketForm, SubCompany>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	private SelectTicketFormer4Sql<OrderTicketForm, User> getSelectFormer4User() {
		String k="SelectFormer4User";
		SelectTicketFormer4Sql<OrderTicketForm, User> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OrderTicketForm, User>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public SelectTicketFormer4Edit<OrderTicketForm, OrderDetail> getSelectEdit4Order() {
		String k="SelectEdit4Order";
		SelectTicketFormer4Edit<OrderTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Edit<OrderTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<OrderTicketForm, Commodity> getSelectFormer4Commodity() {
		String k="SelectFormer4Commodity";
		SelectTicketFormer4Sql<OrderTicketForm, Commodity> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<OrderTicketForm, Commodity>(this);
			this.setAttr(k, form);
		}
		return form;
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
	private void setChangeState(State state, ViewData<OrderDetail> viewData) {
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
	private void setOrderUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUorder(suser);
		}
	}
	
	private void setArrangeUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUarrange(suser);
		}
	}
	
	private void setChangeUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUchange(suser);
		}
	}
	
	private void setPurChangeUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUchange(suser);
		}
	}
	
	private void getClientSearchName(TextField input) {
		this.setIsDialogOpen(false);
		String name = input.getText();
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "ClientQuery", 1|2, "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	
	private void setClientSelect(List<Client> clientList) {
		Client client = clientList.size()==0? new Client(): clientList.get(0);
		getDomain().getClient().setName(client.getName());
		new ClientLogic().fromTrunk(getDomain().getClient(), client);
	}
	
	private void setProjectSelect(List<OrderTicket> projectList) {
		OrderTicket project = projectList.size()==0? new OrderTicket(): projectList.get(0);
		PropertyChoosableLogic.TicketDetail logic = new OrderTicketLogic().getTicketChoosableLogic();
		logic.fromTrunk((ViewBuilder)logic.getTicketBuilder().getFieldBuilder("Project"), this.getDomain().getOrderTicket(), project);
	}
	
	private void setStorehouseSub(Component fcomp) {
		SubCompany sub = this.getSelectFormer4SubCompany().getFirst();
		Field field = (Field)fcomp.searchParentByClass(SimpleDialog.class).getFirer();
		if (field.getFieldBuilder().getName().contains("out"))
			this.getDomain().getLocationTicket().setOut(sub);
		else
			this.getDomain().getLocationTicket().setTo(sub);
	}
	private void setStorehouseUser(Component fcomp) {
		User sub = this.getSelectFormer4User().getFirst();
		Field field = (Field)fcomp.searchParentByClass(SimpleDialog.class).getFirer();
		if (field.getFieldBuilder().getName().contains("out"))
			this.getDomain().getLocationTicket().setOut(sub);
		else
			this.getDomain().getLocationTicket().setTo(sub);
	}
	
	private void getOrderTicketCount() {
		SelectTicketFormer4Cross cross = this.getSelectCross4OrderTicket();
		this.getSelectFormer4OrderTicket().getASelects().clear();
		for (Iterator<List<Object>> riter=cross.getBodyRows().iterator(), rcntIter=cross.getRowCounts().iterator(); riter.hasNext();) {
			List<Object> row = riter.next();
			List<Object> rcount=rcntIter.next();
			String number = (String)row.get(0);
			OrderTicket ticket = null;
			for (OrderTicket t: this.getSelectFormer4OrderTicket().getSelectedList()) {
				if (StringUtils.equals(number, t.getNumber())) {
					ticket = t;
					break;
				}
			}
			if (ticket==null)
				continue;
			int ci=0;
			double cnt=0,amount=0,cmoney=0,arrange=0,arrangeC=0,wait=0,waitC=0,receipt=0,receiptC=0,send=0,sendC=0,paid=0,paidC=0,paidMoney=0,waitMoney=0;
			String arrangeDate=null,waitDate=null,receiptDate=null,sendDate=null,paidDate=null;
			if ("id0|count".length()>0)
				cnt = Double.valueOf(rcount.get(ci++).toString());
			if ("amount|sum".length()>0)
				amount = Double.valueOf(rcount.get(ci++).toString());
			if ("cmoney|sum".length()>0)
				cmoney = Double.valueOf(rcount.get(ci++).toString());
			if ("Arrange|sum".length()>0)
				arrange = Double.valueOf(rcount.get(ci++).toString());
			if ("ArrangeC|sum".length()>0)
				arrangeC = Double.valueOf(rcount.get(ci++).toString());
			if ("ArrangeDate|max".length()>0)
				arrangeDate = (String)rcount.get(ci++);
			if ("Wait|sum".length()>0)
				wait = Double.valueOf(rcount.get(ci++).toString());
			if ("WaitC|sum".length()>0)
				waitC = Double.valueOf(rcount.get(ci++).toString());
			if ("WaitDate|max".length()>0)
				waitDate = (String)rcount.get(ci++);
			if ("Receipt|sum".length()>0)
				receipt = Double.valueOf(rcount.get(ci++).toString());
			if ("ReceiptC|sum".length()>0)
				receiptC = Double.valueOf(rcount.get(ci++).toString());
			if ("ReceiptDate|max".length()>0)
				receiptDate = (String)rcount.get(ci++);
			if ("Send|sum".length()>0)
				send = Double.valueOf(rcount.get(ci++).toString());
			if ("SendC|sum".length()>0)
				sendC = Double.valueOf(rcount.get(ci++).toString());
			if ("SendDate|max".length()>0)
				sendDate = (String)rcount.get(ci++);
			if ("Paid|sum".length()>0)
				paid = Double.valueOf(rcount.get(ci++).toString());
			if ("PaidC|sum".length()>0)
				paidC = Double.valueOf(rcount.get(ci++).toString());
			if ("PaidMoney|sum".length()>0)
				paidMoney = Double.valueOf(rcount.get(ci++).toString());
			if ("WaitMoney|sum".length()>0)
				waitMoney = Double.valueOf(rcount.get(ci++).toString());
			if ("PaidDate|max".length()>0)
				paidDate = new DateType().format((Date)rcount.get(ci++));
			DoubleType dtype = new DoubleType();
			if ("订单合计".length()>0) {
				StringBuffer sb=new StringBuffer().append("有明细").append(dtype.format(cnt)).append("项，数量").append(dtype.format(amount)).append("，金额").append(dtype.format(cmoney));
				ticket.setOrderT(sb.toString());
			}
			if ("排单合计".length()>0) {
				StringBuffer sb=new StringBuffer();
				if (arrange==amount)
					sb.append("安排率100%，");
				else {
					if (cnt-arrangeC>0)
						sb.append("未排项").append(dtype.format(cnt-arrangeC)).append("，");
					if (arrangeC>0)
						sb.append("已排项").append(dtype.format(arrangeC)).append("，");
				}
				if (waitC>0)
					sb.append("在途项").append(dtype.format(waitC)).append("，").append("在途数").append(dtype.format(wait)).append("，").append("在途日期").append(waitDate).append("，");
				else if (arrangeDate!=null)
					sb.append("安排日期").append(arrangeDate).append("，");
				ticket.setArrange(sb.length()==0? null: sb.deleteCharAt(sb.length()-1).toString());
			} 
			if ("待发合计".length()>0) {
				StringBuffer sb=new StringBuffer();
				if (receipt+send==amount)
					sb.append("可发率100%，");
				else {
					if (receiptC>0)
						sb.append("待发项").append(dtype.format(receiptC)).append("，").append("待发数").append(dtype.format(receipt)).append("，");
					if (sendC>0)
						sb.append("已发项").append(dtype.format(sendC)).append("，").append("已发数").append(dtype.format(send)).append("，");
				}
				if (receiptDate!=null)
					sb.append("待发日期").append(receiptDate).append("，");
				ticket.setReceipt(sb.length()==0? null: sb.deleteCharAt(sb.length()-1).toString());
			}
			if ("发货合计".length()>0) {
				StringBuffer sb=new StringBuffer();
				if (send==amount)
					sb.append("发货率100%，");
				else {
					if (receiptC>0)
						sb.append("未发项").append(dtype.format(receiptC)).append("，").append("未发数").append(dtype.format(receipt)).append("，");
					if (sendC>0)
						sb.append("已发项").append(dtype.format(sendC)).append("，").append("已发数").append(dtype.format(send)).append("，");
				}
				if (sendDate!=null)
					sb.append("发货日期").append(sendDate).append("，");
				ticket.setSend(sb.length()==0? null: sb.deleteCharAt(sb.length()-1).toString());
			}
			if ("付款合计".length()>0) {
				StringBuffer sb=new StringBuffer();
				if (paid==amount)
					sb.append("付款率100%，");
				else {
					if (sendC-paidC>0)
						sb.append("未付项").append(dtype.format(sendC-paidC)).append("，").append("未付数").append(dtype.format(send-paid)).append("，").append("未付额").append(dtype.format(waitMoney)).append("，");
					if (paidC>0)
						sb.append("已付项").append(dtype.format(paidC)).append("，").append("已付数").append(dtype.format(paid)).append("，").append("已付额").append(dtype.format(paidMoney)).append("，");
				}
				if (paidDate!=null)
					sb.append("付款日期").append(paidDate).append("，");
				ticket.setPaid(sb.length()==0? null: sb.deleteCharAt(sb.length()-1).toString());
			}
			this.getSelectFormer4OrderTicket().getASelects().add(ticket);
		}
		if ("订单没有明细了".length()>0) {
			List<OrderTicket> emptyTickets = (List)this.getSelectFormer4OrderTicket().getSelectedList();
			emptyTickets.removeAll(this.getSelectFormer4OrderTicket().getASelects());
			for (OrderTicket t: emptyTickets) {
				if (t.getOrderT()!=null && t.getOrderT().startsWith("----")==false)
					t.setOrderT("----" + t.getOrderT());
			}
		}
	}
	
	private void setCommoditySelect(List<Commodity> commodityList) {
		Iterator<OrderDetail> iter = this.getSelectedList().iterator();
		for (Commodity comm: commodityList) {
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
	
	private void setBDetailsLink(Hyperlink link, String monthnum, OrderDetail row, int rowi) {
		if (rowi==1)
			this.getPPurchaseTicketForm().getMonthnumLinkList().clear();
		this.getPPurchaseTicketForm().getMonthnumLinkList().add(link);
		link.setText((String)monthnum);
		if (rowi==1) {
			link.getEventListenerList().addActionListener(this.getPPurchaseTicketForm().getOnPageLoadedListener());
			link.addAttribute(ClientEventName.InitScript0, "L_action(this);");
		}
	}
	
	private PPurchaseTicketForm getPPurchaseTicketForm() {
		PPurchaseTicketForm form = this.getAttr(PPurchaseTicketForm.class);
		if (form == null) {
			form = new PPurchaseTicketForm();
			this.setAttr(form);
		}
		return form;
	}
	
	public ActionService4LinkListener getActionService4LinkListener() {
		return this.getAttr(ActionService4LinkListener.class);
	}
	
	protected String getColumnOrder(LinkedHashMap<String, Object> params) {
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iter=params.keySet().iterator(); iter.hasNext();) {
			String order = (String)params.get(iter.next());
			int stOrder = (Integer)params.get(iter.next());
			if (StringUtils.isBlank(order)==false)
				sb.append(order);
			if (stOrder==0 && sb.length()>0)
				sb.insert(0, "__");
		}
		return sb.toString();
	}
	
	private void getImportFromBuilder(Component fcomp) {
		List<EditView> vlist = fcomp.searchFormerLinkByClass(EditView.class);
		EditView vtop = vlist.get(vlist.size()-1);
		EditViewBuilder vbuilder = (EditViewBuilder)vtop.getViewBuilder();
		this.setAttr("ImportFromBuilder", vbuilder);
	}
	
	private void setImportBackBuilder(Component fcomp) {
		EditViewBuilder vbuilder = (EditViewBuilder)this.getAttr("ImportFromBuilder");
		List<EditView> vlist = fcomp.searchFormerLinkByClass(EditView.class);
		EditView vtop = vlist.get(vlist.size()-1);
		this.setDomain(this.getDomain().getVoparam(OrderDetail.class));
		vtop.getComponent().fireComponentReplace(vbuilder.build(this).getComponent());
	}
	
	private void setImportLabelLoad(Component fcomp) {
		ListView listview = fcomp.searchFormerByClass(EditView.class).getComponent().getInnerFormerList(ListView.class).get(0);
		String builderName=listview.getViewBuilder().getFullViewName();
		SellerViewInputs inputs = new SellerViewInputsLogic().get(builderName, this.getUserName());
		if (inputs == null) {
			inputs = new SellerViewInputs();
			inputs.setBuilderName(builderName);
			inputs.setUserName(this.getUserName());
		}
		this.getBaseImportForm().setFormProperty("attrMap.SellerViewInputs", inputs);
	}
	
	private void setImportFormated(Component fcomp) {
		List<OrderDetail> importList = this.getBaseImportForm().setImportFormated(fcomp, this.getDomain());
		this.getImportList().addAll(importList);
	}
	
	private void setImportListAdd() {
		List<OrderDetail> importList = this.getImportList();
		for (OrderDetail d: importList) {
			d.getOrderTicket().setCmoney(d.getOrderTicket().getCprice() * d.getAmount());
		}
		this.getDetailList().addAll(importList);
	}
}
