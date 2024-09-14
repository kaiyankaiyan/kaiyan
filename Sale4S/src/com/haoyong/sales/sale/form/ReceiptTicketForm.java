package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.mily.common.NoteAccessorFormer;
import net.sf.mily.http.Connection;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DoubleType;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Menu;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.webObject.AddNoteListener;
import net.sf.mily.webObject.AuditViewBuilder;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.View;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.logic.BillTypeLogic;
import com.haoyong.sales.base.logic.BomTicketLogic;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.domain.AbstractCommodityItem;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.listener.ActionService4LinkListener;
import com.haoyong.sales.common.listener.SelectDomainListener;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.PurchaseTicket;
import com.haoyong.sales.sale.domain.ReceiptTicket;
import com.haoyong.sales.sale.domain.ReturnT;
import com.haoyong.sales.sale.domain.ReturnTicket;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.sale.logic.ReceiptTicketLogic;
import com.haoyong.sales.sale.logic.ReturnTicketLogic;
import com.haoyong.sales.test.sale.OrderReturnTest;

public class ReceiptTicketForm extends AbstractForm<OrderDetail> implements FViewInitable {
	
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
	
	private void prepareReceipt() {
		this.detailList = getSelectFormer4Purchase().getSelectedList();
		for (OrderDetail detail: this.detailList) {
			detail.setNotes(null);
		}
		TicketPropertyUtil.copyProperties(this.detailList.get(0), this.getDomain());
		this.getDomain().setChangeRemark(null);
		this.getNoteFormer4Purchase().getVoNoteMap(this.getDomain()).clear();
		for (OrderDetail detail: this.getDetailList()) {
			detail.getReceiptTicket().setReceiptAmount(0);
			detail.getReceiptTicket().setBadAmount(0);
		}
		this.prepareBomDetails();
	}
	
	private void prepareBomDetails() {
		// 生产原物料
		List<String> monthnumList = new ArrayList<String>();
		for (OrderDetail pur: this.getDetailList()) {
			List<BomDetail> bomReceipt=pur.getVoparam("BomDetailReceipt");
			if (bomReceipt.size()==0)
				monthnumList.add(pur.getMonthnum());
			else {
				Assert.assertTrue("一个加工原物料的收货", bomReceipt.size()==1);
				BomDetail bom = bomReceipt.get(0);
				monthnumList.add(bom.getMonthnum());
			}
		}
		PPurchaseTicketForm pform = new PPurchaseTicketForm();
		pform.getOnPageLoadedListener().getBomDetails(monthnumList.toArray(new String[0]));
		List<BomDetail> blist = new ArrayList<BomDetail>();
		for (OrderDetail pur: this.getDetailList()) {
			List<BomDetail> boms = pur.getVoparam("BomDetailReceipt");
			if ("车间生产成品".length()>0 && boms.size()==0) {
				List<BomDetail> bomAll=pform.getBomDetails(pur.getMonthnum()), bomShow=new ArrayList<BomDetail>(bomAll);
				for (BomDetail b: bomAll) {
					if (false && "去请购".equals(b.getArrange())) {
						List<BomDetail> childBrother = new BomTicketLogic().getChildrenBrother(bomAll, b);
						bomShow.removeAll(childBrother);
					}
				}
				pur.getVoParamMap().put("BomDetails", bomShow);
				blist.addAll(bomShow);
			} else if ("委外加工料".length()>0) {
				BomDetail bom = boms.get(0);
				List<BomDetail> bomAll = pform.getBomDetails(bom.getMonthnum());
				List<BomDetail> childBrother = new BomTicketLogic().getChildrenBrother(bomAll, bom);
				pur.getVoParamMap().put("BomDetails", childBrother);
				blist.addAll(childBrother);
			}
		}
		if ("剩余领料数出库，生产未领料数入库".length()>0) {
			for (BomDetail b: blist) {
				OrderDetail order=b.getVoparam(OrderDetail.class);
				Assert.assertTrue("原物料有生产订单", order!=null && order.getId()>0);
				double out = new DoubleType().getFormated(-b.getBomTicket().getCommitAmount()+b.getBomTicket().getOccupy1());
				b.getBomTicket().setInstore(out>=0? 0d: -out);
				b.getBomTicket().setOutstore(out>0? out: 0d);
				b.setSendDate(new Date());
			}
		}
		this.getSelectFormer4Bom().setSelectedList(blist);
	}
	
	public void prepareAdjust() {
		this.detailList = getSelectFormer4Purchase().getSelectedList();
		TicketPropertyUtil.copyProperties(this.detailList.get(0), this.getDomain());
	}
	
	public void prepareChangeConfirm() {
		this.detailList = getSelectFormer4Purchase().getSelectedList();
		TicketPropertyUtil.copyProperties(this.detailList.get(0), this.getDomain());
		this.prepareBomDetails();
	}
	
	public void prepareRollback() {
		this.detailList = getSelectFormer4Purchase().getSelectedList();
		TicketPropertyUtil.copyProperties(this.detailList.get(0), this.getDomain());
		StringBuffer monthnums = new StringBuffer();
		for (OrderDetail detail: this.detailList) {
			monthnums.append("=").append(detail.getMonthnum()).append(" ");
		}
		HashMap<String, String> filters = new HashMap<String, String>();
		filters.put("monthnum", monthnums.toString());
		for (SqlListBuilder builder: (List<SqlListBuilder>)EntityClass.loadViewBuilder(this.getClass(), "Rollback").getFieldBuildersDeep(SqlListBuilder.class)) {
			this.getSearchSetting(builder).addFilters(filters);
		}
	}
	
	public void preparePrintModel() {
		this.detailList = new ArrayList<OrderDetail>();
		OrderDetail detail = new OrderDetail();
		this.detailList.add(detail);
		TicketPropertyUtil.copyProperties(this.detailList.get(0), this.getDomain());
	}
	
	public void preparePrintOne() {
		this.detailList = new ArrayList<OrderDetail>();
		View view = (View)EntityClass.loadViewBuilder(this.getClass(), "ShowQuery").build(this);
		ListView listview = view.getComponent().getInnerFormerList(ListView.class).get(0);
		List<List<Object>> listvalue = (List<List<Object>>)listview.getValue();
		if (listvalue.size()==0)
			throw new LogicException(2, "查询中没有记录，无法打印预览!");
		this.detailList.addAll(new SelectDomainListener().toDomains(listvalue.subList(0, listvalue.size()>1? 2: 1), OrderDetail.class));
		TicketPropertyUtil.copyProperties(this.detailList.get(0), this.getDomain());
	}
	
	public void preparePrint(Component fcomp) {
		this.detailList = this.getSelectFormer4Purchase().getSelectedList();
		TicketPropertyUtil.copyProperties(this.detailList.get(0), this.getDomain());
		this.getPrintModelForm().showPrintOne(fcomp);
	}
	
	public void canReceipt(List<List<Object>> valiRows) {
		// commName,uneditable
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (row.get(1) != null) {
				sb.append(row.get(0)).append(row.get(1)).append("\t");
			}
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}
	
	public void canAdjust(List<List<Object>> valiRows) {
		// commName,stateId,receiptId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (!((Integer)row.get(1)==50 && (Integer)row.get(2)==30))
				sitem.append("无采购红冲，");
			if (sitem.length()>0)		sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canChangeConfirm(List<List<Object>> valiRows) {
		// commName,stateId,receiptId
		StringBuffer sb=new StringBuffer();
		for (List<Object> row: valiRows) {
			if (Integer.parseInt(row.get(2)+"")!=20) {
				sb.append(row.get(0)).append("不用改单申请确认,");
			}
		}
		if (sb.length() > 0) {
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		}
	}
	
	public void canRollback(List<List<Object>> valiRows) {
		// monthnum,commName,sendId,amount,stateId,arrangeType
		StringBuffer sb=new StringBuffer(), sitem;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(2)!=null && Integer.parseInt(row.get(2)+"")==30 && "普通".equals(row.get(5)))
				sitem.append("请先撤销发货，");
			if (Double.parseDouble(row.get(3)+"")==0)
				sitem.append("收货已消耗完，");
			if (Integer.parseInt(row.get(4)+"")==0 && "普通".equals(row.get(5)))
				sitem.append("收货已消耗完，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(row.get(1)).append(sitem).append("\t");
		}
		if (sb.length() > 0) {
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		}
	}
	
	public void validateReceipt4Full() {
		StringBuffer sb=new StringBuffer(), schange=new StringBuffer(), sitem=null;
		if (this.getSelectedList().size()==0)
			sb.append("请选择全数收货的明细，");
		if (getNoteFormer4Purchase().isNoted(this.getDomain()))
			schange.append("单头有更改，");
		for (OrderDetail detail: this.getSelectedList()) {
			sitem = new StringBuffer();
			if (getNoteFormer4Purchase().isNoted(detail))
				schange.append("明细有更改，");
			if (detail.getReceiptTicket().getReceiptAmount() / detail.getAmount() > 1)
				schange.append("多收货，");
			if (detail.getReceiptTicket().getBadAmount()>0)
				schange.append("有次品数，");
			if (sitem.length()==0 && detail.getReceiptTicket().getReceiptAmount() / detail.getAmount() < 1)
				sitem.append("少收货请按部分收货提交，");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (schange.length()>0)
			sb.append(schange.deleteCharAt(schange.length()-1)).append("请走改单申请，");
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		else if (new SupplyTypeLogic().isProductType(this.getSelectFormer4Purchase().getFirst().getCommodity().getSupplyType()))
			this.validateBomDetails();
	}
	
	public void validateReceipt4Part() {
		// 没有改单申请，有收货数量
		StringBuffer sb=new StringBuffer(), schange=new StringBuffer(), sitem=null;
		boolean part = true;
		if (this.getSelectedList().size()==0)
			sb.append("请选择收货的明细，");
		if (getNoteFormer4Purchase().isNoted(this.getDomain()))
			schange.append("单头有更改，");
		for (OrderDetail detail: this.getSelectedList()) {
			sitem = new StringBuffer();
			part = part && (detail.getReceiptTicket().getReceiptAmount() / detail.getAmount() < 1);
			if (getNoteFormer4Purchase().isNoted(detail))
				schange.append("明细有更改，");
			if (detail.getReceiptTicket().getReceiptAmount() / detail.getAmount() > 1)
				schange.append("多收货，");
			if (detail.getReceiptTicket().getBadAmount()>0)
				schange.append("有次品数，");
			if (sitem.length()==0 && detail.getReceiptTicket().getReceiptAmount() / detail.getAmount() == 1)
				sitem.append("完整收货请按全部收货提交，");
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (part==false && schange.length()>0)
			sb.append(schange.deleteCharAt(schange.length()-1)).append("请走改单申请，");
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateChange() {
		// 有改单申请
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getSelectedList().size()==0)
			sb.append("请选择改单申请的明细，");
		for (OrderDetail detail: this.getSelectedList()) {
			sitem = new StringBuffer();
			if (detail.getReceiptTicket().getReceiptAmount() / detail.getAmount() < 1)
				sitem.append("少收货请按部分收货提交，");
			if (getNoteFormer4Purchase().isNoted(this.getDomain())) {
			} else if (getNoteFormer4Purchase().isNoted(detail)) {
			} else if (detail.getReceiptTicket().getBadAmount()>0) {
			} else if (detail.getReceiptTicket().getReceiptAmount()>detail.getAmount()) {
			} else {
				sitem.append("请填写改单申请更改的内容，");
			}
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		if (getNoteFormer4Purchase().isChangedRemark(getDomain())==false)
			sb.append("请补充改单申请备注，");
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	// 确认少收货，退回部分货物
	public void validateChangeReturn() throws Exception {
		StringBuffer sb = new StringBuffer();
		if (this.getSelectedList().size()==0)
			sb.append("请选择减少收货数的明细，");
		for (OrderDetail detail: this.getSelectedList()) {
			Double receipt1 = (Double)getNoteFormer4Purchase().getNoteValue(detail, new StringBuffer("receiptAmount"));
			Double bad1 = (Double)getNoteFormer4Purchase().getNoteValue(detail, new StringBuffer("badAmount"));
			if (receipt1!=null && receipt1<detail.getReceiptTicket().getReceiptAmount()) {
				if (receipt1==null)		receipt1 = 0d;
				if (bad1==null)			bad1 = 0d;
				detail.getVoParamMap().put("NewRemainAmount", detail.getReceiptTicket().getReceiptAmount() - receipt1 - bad1);
			} else {
				sb.append("没有差异收货数量变更，请走收货确认，");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	// 确认收货，的各种安排数量
	public void validateChangeEffect() throws Exception {
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (this.getSelectedList().size()==0)
			sb.append("请选择确认收货明细，");
		for (OrderDetail detail: this.getSelectedList()) {
			sitem = new StringBuffer();
			String sreceipt = getNoteFormer4Purchase().getNoteString(detail, new StringBuffer("receiptAmount"));
			String sbad = getNoteFormer4Purchase().getNoteString(detail, new StringBuffer("badAmount"));
			double receipt0=detail.getReceiptTicket().getReceiptAmount(), receipt1=receipt0; 
			double bad0=detail.getReceiptTicket().getBadAmount(), bad1=bad0;
			if (StringUtils.isBlank(sreceipt)==false) {
				receipt1 = new DoubleType().parse(sreceipt);
			}
			if (StringUtils.isBlank(sbad)==false) {
				bad1 = new DoubleType().parse(sbad);
			}
			if (receipt1 < receipt0) {
				sitem.append("有差异收货数量变更，请走减少收货数，");
			}
			if (sitem.length()>0)
				sb.append(detail.getVoparam(CommodityT.class).getCommName()).append(sitem).append("\t");
		}
		sb.append(new PurchaseTicketForm().validatePurchaseChangeAmount(this.getDetailList(), this.getNoteFormer4Purchase()));
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
		else if (new SupplyTypeLogic().isProductType(this.getSelectFormer4Purchase().getFirst().getCommodity().getSupplyType()))
			this.validateBomDetails();
	}
	
	public void validateAdjustYes() {
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.getDetailList()) {
			if (this.getNoteFormer4Purchase().isChangedNotesEX(detail)) {
				sb.append("请勿更改红冲内容！");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}

	public void validateAdjustNo() {
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.getDetailList()) {
			if (this.getNoteFormer4Purchase().isNoted(detail)==false) {
				sb.append("请勿清空红冲内容！");
			}
			if (!getNoteFormer4Purchase().isChangedRemark(detail)) {
				sb.append("请补充红冲备注！");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	private void validateBomDetails() {
		List<BomDetail> bomDetails = new ArrayList<BomDetail>();
		for (OrderDetail detail: this.getSelectedList()) {
			List<BomDetail> list = (List<BomDetail>)detail.getVoParamMap().get("BomDetails");
			bomDetails.addAll(list);
		}
		StringBuffer sb = new StringBuffer();
		int bi=0;
		for (BomDetail bom: bomDetails) {
			bi++;
			BomDetail sbom = bom.getSnapShot();
			StringBuffer sitem = new StringBuffer();
			Double in1=bom.getBomTicket().getInstore(), in2=(Double)bom.getVoParamMap().get("ReInstore");
			Double out1=bom.getBomTicket().getOutstore(), out2=(Double)bom.getVoParamMap().get("ReOutstore");
			Double kin1=(Double)bom.getBomTicket().getKeepAmount(), kin2=(Double)bom.getVoParamMap().get("ReKeepIn");
			Double kout1=(Double)bom.getBomTicket().getOccupy2(), kout2=(Double)bom.getVoParamMap().get("ReKeepOut");
			if (in1!=null && in1>0 && !(in2!=null && in2.doubleValue()==in1.doubleValue()))
				sitem.append("请确认原物料入库数，");
			if (out1!=null && out1>0 && !(out2!=null && out2.doubleValue()==out1.doubleValue()))
				sitem.append("请确认原物料出库数，");
			if (kin1!=null && kin1>0 && !(kin2!=null && kin2.doubleValue()==kin1.doubleValue()))
				sitem.append("请确认原物料留用的添加数，");
			if (kout1!=null && kout1>0 && !(kout2!=null && kout2.doubleValue()==kout1.doubleValue()))
				sitem.append("请确认原物料留用的领用数，");
			if (in1>0 && out1>0)
				sitem.append("不能同时有出库数、入库数，");
			if (kin1>0 && kout1>0)
				sitem.append("不能同时有留用添加数、留用领用数，");
			if (out1>0 && kin1>bom.getBomTicket().getOccupyAmount())
				sitem.append("不能留用数大于出库数，");
			if (sitem.length()>0)
				sb.append("原物料行").append(bi).append(sitem).append("\t");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.toString());
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
			new SupplierLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
	}
	
	public void setChangeNotes(Component fcomp) {
		Window win = fcomp.searchParentByClass(Window.class);
		List<AddNoteListener> noteList = win.getInnerFormerList(AddNoteListener.class);
		Map<String, String> domainMap = new HashMap<String, String>();
		Map<String, String> domainAll = this.getNoteFormer4Purchase().getVoNoteMap(this.getDomain());
		for (AddNoteListener note: noteList) {
			String name=note.getPropertyName(), value=note.getNoteString();
			if (domainAll.containsKey(name)==false)
				continue;
			ListView listview = note.getComponent().searchFormerByClass(ListView.class);
			if (!(listview==null && note.getEntity()==getDomain()))			continue;
			domainMap.put(name, value);
		}
		for (OrderDetail detail: this.getSelectedList()) {
			this.getNoteFormer4Purchase().getVoNoteMap(detail).putAll(domainMap);
			this.getNoteFormer4Purchase().isChangedNotesEX(detail);
			detail.setChangeRemark(this.getDomain().getChangeRemark());
		}
	}

	public SelectTicketFormer4Sql<ReceiptTicketForm, OrderDetail> getSelectFormer4Purchase() {
		String k = "SelectFormer4Purchase";
		SelectTicketFormer4Sql<ReceiptTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<ReceiptTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	private SelectTicketFormer4Sql<ReceiptTicketForm, OrderDetail> getSelectFormer4Order() {
		String k = "SelectFormer4Order";
		SelectTicketFormer4Sql<ReceiptTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<ReceiptTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	public SelectTicketFormer4Sql<ReceiptTicketForm, BomDetail> getSelectFormer4Bom() {
		String k = "SelectFormer4Bom";
		SelectTicketFormer4Sql<ReceiptTicketForm, BomDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<ReceiptTicketForm, BomDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}

	public SelectTicketFormer4Sql<ReceiptTicketForm, BillDetail> getSelectFormer4Bill() {
		String k = "SelectFormer4Bill";
		SelectTicketFormer4Sql<ReceiptTicketForm, BillDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<ReceiptTicketForm, BillDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public StoreTicketForm getBomInstoreForm() {
		String k = "BomInstoreForm";
		StoreTicketForm form = this.getAttr(k);
		if (form == null) {
			form = new StoreTicketForm();
			this.setAttr(k, form);
		}
		return form;
	}
	
	public StoreTicketForm getBomOutstoreForm() {
		String k = "BomOutstoreForm";
		StoreTicketForm form = this.getAttr(k);
		if (form == null) {
			form = new StoreTicketForm();
			this.setAttr(k, form);
		}
		return form;
	}
	
	public StoreTicketForm getBomKeepInstoreForm() {
		String k = "BomKeepInstoreForm";
		StoreTicketForm form = this.getAttr(k);
		if (form == null) {
			form = new StoreTicketForm();
			this.setAttr(k, form);
		}
		return form;
	}
	public StoreTicketForm getBomKeepOutstoreForm() {
		String k = "BomKeepOutstoreForm";
		StoreTicketForm form = this.getAttr(k);
		if (form == null) {
			form = new StoreTicketForm();
			this.setAttr(k, form);
		}
		return form;
	}
	
	public void setBomInstoreConfirm() {
		for (BomDetail bom: this.getSelectFormer4Bom().getSelectedList()) {
			Double instore = bom.getBomTicket().getInstore();
			if (instore>0)
				bom.getVoParamMap().put("ReInstore", instore);
			Double outstore = bom.getBomTicket().getOutstore();
			if (outstore>0)
				bom.getVoParamMap().put("ReOutstore", outstore);
		}
	}
	
	public void setBomOutstoreConfirm() {
		for (BomDetail bom: this.getSelectFormer4Bom().getSelectedList()) {
			Double outstore = bom.getBomTicket().getOutstore();
			if (outstore>0)
				bom.getVoParamMap().put("ReOutstore", outstore);
		}
	}
	
	public void setBomKeepInConfirm() {
		for (BomDetail bom: this.getSelectFormer4Bom().getSelectedList()) {
			Double keep = (Double)bom.getBomTicket().getKeepAmount();
			if (keep!=null)
				bom.getVoParamMap().put("ReKeepIn", keep);
		}
	}
	public void setBomKeepOutConfirm() {
		for (BomDetail bom: this.getSelectFormer4Bom().getSelectedList()) {
			Double keep = (Double)bom.getBomTicket().getOccupy2();
			if (keep!=null)
				bom.getVoParamMap().put("ReKeepOut", keep);
		}
	}
	
	private void getRollbackBomBill(Component fcomp) {
		ListView instoreList = null;
		for (ListView view: fcomp.searchParentByClass(Window.class).getInnerFormerList(ListView.class)) {
			if (view.getListBuilder() instanceof SqlListBuilder)
				view.getComponent().getInnerComponentList(Menu.class).get(0).getEventListenerList().fireListener();
			if (view.getListBuilder().getName().equals("selectFormer4Bom.CSelects"))
				instoreList = view;
		}
		"".toCharArray();
		for (BomDetail bom: this.getSelectFormer4Bom().getSelectedList()) {
			OrderDetail order = bom.getVoparam(OrderDetail.class);
			Assert.assertTrue("原物料有生产订单", order!=null && order.getId()>0);
			OrderDetail pur = TicketPropertyUtil.copyProperties(order, new OrderDetail());
			if ("订单的分支".length()>0) {
				for (String k: new String[]{"NumberSplit", "MonthnumSplit", "PurNumberSplit"}) {
					Map<String, String> map = this.getAttr(k);
					if (map==null) {
						map = new HashMap<String, String>();
						this.setAttr(k, map);
					}
				}
				for (String k="NumberSplit", v=((Map<String, String>)this.getAttr(k)).get(order.getOrderTicket().getNumber()); k!=null; k=null,pur.getOrderTicket().setNumber(v)) {
					if (v!=null)	continue;
					v = new OrderTicketLogic().getBomMonthnum(order.getOrderTicket().getNumber());
					((Map<String, String>)this.getAttr(k)).put(order.getOrderTicket().getNumber(), v);
				}
				for (String k="MonthnumSplit", v=((Map<String, String>)this.getAttr(k)).get(order.getMonthnum()); k!=null; k=null,pur.setMonthnum(v)) {
					if (v!=null)	continue;
					v = new OrderTicketLogic().getBomMonthnum(order.getMonthnum());
					((Map<String, String>)this.getAttr(k)).put(order.getMonthnum(), v);
				}
				for (String k="PurNumberSplit", v=((Map<String, String>)this.getAttr(k)).get(order.getReceiptTicket().getNumber()); k!=null; k=null,pur.getPurchaseTicket().setNumber(v)) {
					if (v!=null)	continue;
					v = new OrderTicketLogic().getBomMonthnum(order.getPurchaseTicket().getNumber());
					((Map<String, String>)this.getAttr(k)).put(order.getReceiptTicket().getNumber(), v);
				}
			}
			pur.getArrangeTicket().setArrangeType(new DeliverTypeLogic().getCommonType());
			pur.setAmount(bom.getBomTicket().getOccupyAmount());
			pur.setCommodity(bom.getCommodity());
			pur.getReceiptTicket().setStorePrice(bom.getPrice());
			if (pur.getAmount()>0) {
				this.getSelectFormer4Bom().getCSelects().add(pur);
				bom.getVoParamMap().put("InstorePurchase", pur);
			}
		}
		instoreList.update();
	}
	
	private double getFullyReceipt() {
		double full=1;
		for (OrderDetail detail: this.getDetailList()) {
			full = full * detail.getReceiptTicket().getReceiptAmount() / detail.getAmount();
		}
		return full;
	}
	
	public OrderDetail getSFirstOrder() {
		return this.getDetailList().get(0).getSnapShot();
	}
	
	public OrderDetail getDomain() {
		String k="PurchaseDomain";
		OrderDetail detail = this.getAttr(k);
		if (detail==null) {
			detail = new OrderDetail();
			this.setAttr(k, detail);
		}
		return detail;
	}
	
	public List<OrderDetail> getDetailList() {
		return this.detailList;
	}
	
	private ChooseFormer getTicketChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new ReceiptTicketLogic().getTicketChoosableLogic();
		former.setViewBuilder(logic.getTicketBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getTicketBuilder()));
		return former;
	}
	
	private ChooseFormer getDetailChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.TicketDetail logic = new ReceiptTicketLogic().getTicketChoosableLogic();
		former.setViewBuilder(logic.getDetailBuilder());
		former.setSellerViewSetting(logic.getChooseSetting(logic.getDetailBuilder()));
		return former;
	}
	
	private PrintModelForm getPrintModelForm() {
		PrintModelForm form = this.getAttr(PrintModelForm.class);
		if (form == null) {
			AuditViewBuilder builder = (AuditViewBuilder)EntityClass.loadViewBuilder(this.getClass(), "Print");
			form = PrintModelForm.getForm(this, builder);
			this.setAttr(form);
		}
		return form;
	}
	
	private ReceiptTicket getReceiptTicket() {
		ReceiptTicket ticket = getAttr(ReceiptTicket.class);
		if (ticket==null) {
			ticket = new ReceiptTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	private void setAmountByWeight() {
		OrderDetail detail = this.getSelectedList().get(0);
		double aweight=detail.getCommodity().getAweight(), weight=detail.getReceiptTicket().getWeight();
		if (aweight>0 && weight>0) {
			detail.getReceiptTicket().setReceiptAmount(weight / aweight);
		}
	}
	
	private OrderDetail getOrderDetail() {
		OrderDetail detail = getAttr(OrderDetail.class);
		if (detail==null) {
			detail = new OrderDetail();
			this.setAttr(detail);
		}
		return detail;
	}
	
	private OrderDetail getOrderFirst() {
		return this.getOrderList().get(0);
	}
	
	private List<OrderDetail> getReceiptBillList() {
		String k = "ReceiptEffectList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public List<OrderDetail> getOrderList() {
		String k = "OrderList";
		List<OrderDetail> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<OrderDetail>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	public void setOrderList(List<OrderDetail> orderList) {
		orderList = new ArrayList<OrderDetail>(orderList);
		getOrderList().clear();
		getOrderList().addAll(orderList);
	}
	
	public boolean getZeroReceipt() {
		boolean zero = true;
		for (OrderDetail detail: this.getSelectedList()) {
			Double receipt1 = (Double)getNoteFormer4Purchase().getNoteValue(detail, new StringBuffer("receiptAmout"));
			Double bad1 = (Double)getNoteFormer4Purchase().getNoteValue(detail, new StringBuffer("badAmout"));
			double receipt = receipt1!=null? receipt1: detail.getReceiptTicket().getReceiptAmount();
			double bad = bad1!=null? bad1: detail.getReceiptTicket().getBadAmount();
			zero = zero && receipt==0 && bad==0;
		}
		return zero;
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
	
	private List<OrderDetail> getSelectedList() {
		String k = "PurchaseSelectedReceipts";
		List list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private List<SupplyType> getSupplyTypeOptions(Object entity) {
		List<SupplyType> typeList = new SupplyTypeLogic().getTypeList();
		return typeList;
	}

	protected List<Map.Entry<String, String>> getPurchaseAgreeOptions(Object detail0) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("supplier", "供应商");
		map.put("commodity", "更改商品");
		map.put("price", "价格");
		return new ArrayList<Entry<String, String>>(map.entrySet());
	}
	
	protected List<Map.Entry<String, String>> getOrderAgreeOptions(Object detail0) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("commodity", "更改商品");
		map.put("amount", "数量");
		return new ArrayList<Entry<String, String>>(map.entrySet());
	}

	@Override
	public void setSelectedList(List<OrderDetail> selected) {
		this.getSelectedList().clear();
		this.getSelectedList().addAll(selected);
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
	
	private void setPurchaseUser(ViewData<OrderDetail> viewData) {
		new PurchaseTicketForm().setPurchaseUser(viewData);
	}
	
	private void setChangeUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUchange(suser);
		}
	}
	
	private void setCancelUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUcancel(suser);
		}
	}
	
	private void setReturnUser(ViewData<OrderDetail> viewData) {
		TicketUser suser = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreturn(suser.getUserDate());
		}
	}
	
	private void setOrderUser(ViewData<OrderDetail> viewData) {
		TicketUser suser = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUorder(suser.getUserDate());
		}
	}
	
	private void setRejectUser(ViewData<OrderDetail> viewData) {
		TicketUser suser = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreturn(suser.getUserDate());
		}
	}
	
	private void setOrderCancelUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUcancel(suser);
		}
	}
	
	private void setReceiptUser(ViewData<OrderDetail> viewData) {
		TicketUser suser = genTicketUser();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUreceipt(suser.getUserDate());
		}
	}
	
	private void setReceipt4Service(ViewData<OrderDetail> viewData) {
		this.getDomain().getReceiptTicket().genSerialNumber();
		PropertyChoosableLogic.TicketDetail logic=new ReceiptTicketLogic().getTicketChoosableLogic();
		for (OrderDetail detail: this.getSelectedList()) {
			logic.fromTrunk(logic.getTicketBuilder(), detail.getReceiptTicket(), this.getDomain().getReceiptTicket());
		}
		viewData.setTicketDetails(this.getSelectedList());
	}
	
	private void setReceiptOrder4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail p: this.getSelectedList()) {
			OrderDetail order=p, sorder=order.getSnapShot();
			if (sorder.getStOrder()>0)
				list.add(order);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setBomReceipt4Service(ViewData<BomDetail> viewData) {
		List<BomDetail> bomList = new ArrayList<BomDetail>();
		for (OrderDetail pur: this.getSelectedList()) {
			List<BomDetail> boms = pur.getVoparam("BomDetailReceipt");
			if (boms.size()==0)
				continue;
			BomDetail bom = boms.get(0);
			if ("累加生产数量".length()>0 && new SupplyTypeLogic().isProductType(bom.getCommodity().getSupplyType()))
				bom.getBomTicket().setCommitAmount(bom.getBomTicket().getCommitAmount() + pur.getAmount());
			bom.getBomTicket().setGiveAmount(bom.getBomTicket().getGiveAmount() + pur.getAmount());
			bomList.add(bom);
		}
		viewData.setTicketDetails(bomList);
	}
	
	private void setReceiptBill4Service(ViewData<BillDetail> viewData) {
		List<BillDetail> billList = new ArrayList<BillDetail>();
		for (OrderDetail pur: this.getSelectedList()) {
			BillDetail bill = TicketPropertyUtil.copyProperties(pur, new BillDetail());
			bill.setMoney(pur.getPrice() * pur.getAmount() * -1);
			bill.getBillTicket().setTypeName(new BillTypeLogic().getPurchaseType());
			billList.add(bill);
		}
		viewData.setTicketDetails(billList);
	}
	
	private void setReceiptSendBill4Service(ViewData<BillDetail> viewData) {
		List<BillDetail> billList = new ArrayList<BillDetail>();
		for (OrderDetail detail: this.getSelectedList()) {
			OrderDetail order = detail;
			BillDetail bill = TicketPropertyUtil.copyProperties(order, new BillDetail());
			bill.setMoney(order.getPrice() * order.getAmount());
			bill.getBillTicket().setTypeName(new BillTypeLogic().getSaleType());
			billList.add(bill);
		}
		viewData.setTicketDetails(billList);
	}
	
	private void setReceiptBom4Service(ViewData<BomDetail> viewData) {
		List<BomDetail> bomList = new ArrayList<BomDetail>();
		for (OrderDetail pur: this.getSelectedList()) {
			List<BomDetail> bomDetails = (List<BomDetail>)pur.getVoParamMap().get("BomDetails");
			if (bomDetails!=null)
				for (BomDetail bom: bomDetails) {
					bomList.add(bom);
					if (bom.getBomTicket().getKeepAmount()>0)
						bom.setSupplier(pur.getSupplier());
				}
		}
		viewData.setTicketDetails(bomList);
	}
	
	private void setReceiptStore4Service(ViewData<AbstractCommodityItem> viewData) {
		viewData.setTicketDetails(new ArrayList<AbstractCommodityItem>());
		viewData.getTicketDetails().addAll(this.getSelectedList());
		viewData.getTicketDetails().addAll(this.getBomInstoreForm().getExtraInstoreList());
		viewData.getTicketDetails().addAll(this.getBomOutstoreForm().getExtraOutstoreList());
	}
	
	private void setChange4Service(ViewData<OrderDetail> viewData) {
		Map<String, String> mapTicket = getNoteFormer4Purchase().getVoNoteMapIN(this.getDomain(), "PurchaseTicket","supplier");
		getDomain().getReceiptTicket().genSerialNumber();
		for (OrderDetail d: this.getSelectedList()) {
			d.getReceiptTicket().setNumber(this.getDomain().getReceiptTicket().getNumber());
			getNoteFormer4Purchase().getVoNoteMap(d).putAll(mapTicket);
			d.setChangeRemark(this.getDomain().getChangeRemark());
			getNoteFormer4Purchase().isChangedNotesEX(d);
		}
		viewData.setTicketDetails(this.getSelectedList());
	}
	
	private void setChangeOrder4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		for (OrderDetail purchase: this.getSelectedList()) {
			OrderDetail ord=purchase, sord=ord.getSnapShot();
			if (sord.getStOrder()==0)
				continue;
		}
	}

	private void setSplitPurchaseCurSubtract4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail curPurSubtract: this.getSelectedList()) {
			curPurSubtract.getVoParamMap().put("SourceAmount", curPurSubtract.getAmount());
			double remainAmount = curPurSubtract.getAmount() - curPurSubtract.getReceiptTicket().getReceiptAmount();
			if (remainAmount<=0)
				continue;
			String remainMonthnum = null;
			remainMonthnum = new OrderTicketLogic().getSplitMonthnum(curPurSubtract.getMonthnum());
			OrderDetail nwPurRemain = new OrderTicketLogic().genCloneOrder(curPurSubtract);
			nwPurRemain.setAmount(remainAmount);
			nwPurRemain.setMonthnum(remainMonthnum);
			nwPurRemain.getReceiptTicket().setReceiptAmount(0);
			nwPurRemain.getReceiptTicket().setBadAmount(0);
			nwPurRemain.getPurchaseTicket().setBackupAmount(0);
			PropertyChoosableLogic.TicketDetail logic=new PurchaseTicketLogic().getTicketChoosableLogic();
			logic.fromTrunk(logic.getHandleBuilder(), nwPurRemain.getPurchaseTicket(), new PurchaseTicket());
			curPurSubtract.getVoParamMap().put("NewRemainPurchase", nwPurRemain);
			curPurSubtract.setAmount(curPurSubtract.getReceiptTicket().getReceiptAmount());
			purList.add(curPurSubtract);
		}
		viewData.setTicketDetails(purList);
	}

	private void setSplitPurchaseNewRemain4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail curPurSubtract: this.getSelectedList()) {
			OrderDetail nwPurRemain = (OrderDetail)curPurSubtract.getVoParamMap().get("NewRemainPurchase");
			if (nwPurRemain!=null)
				purList.add(nwPurRemain);
		}
		viewData.setTicketDetails(purList);
	}
	
	private void setChangePart4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectedList());
		viewData.setParam(this.getNoteFormer4Purchase());
		viewData.setParam("ChangeType", ReceiptTicket.class);
	}
	
	private void setChangeClear4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectedList());
		viewData.setParam(this.getNoteFormer4Purchase());
		viewData.setParam("ChangeType", OrderDetail.class);
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
	
	private void setOrderLink4Service_validate(ViewData<OrderDetail> viewData) {
		List<OrderDetail> rejectList = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectedList()) {
			OrderDetail reject = TicketPropertyUtil.deepClone(detail);
			Double receiptAmount = (Double)this.getNoteFormer4Purchase().getNoteValue(detail, new StringBuffer("receiptAmount"));
			reject.setAmount(detail.getAmount() - receiptAmount);
			detail.getVoParamMap().put("RejectPurchase", reject);
			rejectList.add(reject);
		}
		new OrderReturnTest().linkSupplier拒收开退货入库申请_1拆分出退数_2退货申请无事务('1', rejectList);
		viewData.setTicketDetails();
	}
	
	private void setOrderLink4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> rejectList = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectedList()) {
			OrderDetail reject = detail.getVoparam("RejectPurchase");
			rejectList.add(reject);
		}
		new OrderReturnTest().linkSupplier拒收开退货入库申请_1拆分出退数_2退货申请无事务('2', rejectList);
		viewData.setTicketDetails();
	}
	
	private void setOutBomList4Service() {
		if ("多生产，入库".length()>0) {
			List<OrderDetail> itemList = new ArrayList<OrderDetail>();
			for (OrderDetail order: this.getSelectedList()) {
				List<BomDetail> bomDetails = (List<BomDetail>)order.getVoParamMap().get("BomDetails");
				if (bomDetails == null)
					continue;
				for (BomDetail bom: bomDetails) {
					OrderDetail pur = TicketPropertyUtil.copyProperties(order, new OrderDetail());
					Double instore = bom.getBomTicket().getInstore();
					if (instore==null || instore.doubleValue()==0)
						continue;
					if ("订单的分支".length()>0) {
						for (String k: new String[]{"NumberSplit", "MonthnumSplit", "PurNumberSplit"}) {
							Map<String, String> map = this.getAttr(k);
							if (map==null) {
								map = new HashMap<String, String>();
								this.setAttr(k, map);
							}
						}
						for (String k="NumberSplit", v=((Map<String, String>)this.getAttr(k)).get(order.getOrderTicket().getNumber()); k!=null; k=null,pur.getOrderTicket().setNumber(v)) {
							if (v!=null)	continue;
							v = new OrderTicketLogic().getBomMonthnum(order.getOrderTicket().getNumber());
							((Map<String, String>)this.getAttr(k)).put(order.getOrderTicket().getNumber(), v);
						}
						for (String k="MonthnumSplit", v=((Map<String, String>)this.getAttr(k)).get(order.getMonthnum()); k!=null; k=null,pur.setMonthnum(v)) {
							if (v!=null)	continue;
							v = new OrderTicketLogic().getBomMonthnum(order.getMonthnum());
							((Map<String, String>)this.getAttr(k)).put(order.getMonthnum(), v);
						}
						for (String k="PurNumberSplit", v=((Map<String, String>)this.getAttr(k)).get(order.getReceiptTicket().getNumber()); k!=null; k=null,pur.getPurchaseTicket().setNumber(v)) {
							if (v!=null)	continue;
							v = new OrderTicketLogic().getBomMonthnum(order.getPurchaseTicket().getNumber());
							((Map<String, String>)this.getAttr(k)).put(order.getReceiptTicket().getNumber(), v);
						}
					}
					pur.setCommodity(bom.getCommodity());
					pur.setAmount(instore);
					pur.setReceiptTicket(this.getDomain().getReceiptTicket());
					pur.getReceiptTicket().setStorePrice(bom.getPrice());
					itemList.add(pur);
				}
			}
			this.getBomInstoreForm().setInExtra4Service(itemList, "instore", "null");
		}
		if ("多领料，出库".length()>0) {
			List<OrderDetail> itemList = new ArrayList<OrderDetail>();
			for (OrderDetail pur: this.getSelectedList()) {
				List<BomDetail> bomDetails = (List<BomDetail>)pur.getVoParamMap().get("BomDetails");
				for (BomDetail bom: bomDetails) {
					OrderDetail item = TicketPropertyUtil.copyProperties(bom, new OrderDetail());
					item.setVoparam(bom);
					Double outstore = bom.getBomTicket().getOutstore();
					if (outstore==null || outstore.doubleValue()==0)
						continue;
					item.setAmount(outstore);
					itemList.add(item);
				}
			}
			this.getBomOutstoreForm().setOutExtra4Service(itemList, "instore", "null");
			for (OrderDetail item: itemList) {
				BomDetail bom = item.getVoparam(BomDetail.class);
				bom.setPurchaseTicket(item.getPurchaseTicket());
				bom.setPrice(item.getReceiptTicket().getStorePrice());
			}
		}
		if ("从车间留用数领料，出库".length()>0) {
			List<BomDetail> itemList = new ArrayList<BomDetail>();
			for (OrderDetail pur: this.getSelectedList()) {
				List<BomDetail> bomDetails = (List<BomDetail>)pur.getVoParamMap().get("BomDetails");
				for (BomDetail bom: bomDetails) {
					BomDetail item = TicketPropertyUtil.copyProperties(bom, new BomDetail());
					item.setVoparam(bom);
					Double outstore = bom.getBomTicket().getOccupy2();
					if (outstore==null || outstore.doubleValue()==0)
						continue;
					item.getBomTicket().setKeepAmount(outstore);
					itemList.add(item);
				}
			}
			if (itemList.size()>0)
				this.getBomKeepOutstoreForm().setOutAgent4Service(itemList.get(0), itemList);
		}
	}

	private void setBomInstore4Service(ViewData<OrderDetail> viewData) {
		this.getBomInstoreForm().setInExtra4Service(viewData);
	}

	private void setBomOutstore4Service(ViewData<OrderDetail> viewData) {
		this.getBomOutstoreForm().setOutExtra4Service(viewData);
	}

	private void setBomKeepOutstore4Service(ViewData<BomDetail> viewData) {
		this.getBomKeepOutstoreForm().setAgentAdd4Service(viewData);
	}
	
	public void setConfirm4Service() throws Exception {
		for (OrderDetail pur: this.getSelectedList()) {
			OrderDetail spur = pur.getSnapShot();
			Commodity oldCommodity = TicketPropertyUtil.copyProperties(pur.getCommodity(), new Commodity());
			OrderDetail d = TicketPropertyUtil.deepClone(pur);
			getNoteFormer4Purchase().getVoNoteMap(d).putAll(getNoteFormer4Purchase().getVoNoteMap(pur));
			getNoteFormer4Purchase().setEntityChanges(d, "commodity");
			getNoteFormer4Purchase().setEntityChanges(pur, "ReceiptTicket.Handle");
			getNoteFormer4Purchase().getVoNoteMap(spur);
			getNoteFormer4Purchase().getVoNoteMap(pur);
			Commodity newCommodity = d.getCommodity();
			pur.getVoParamMap().put("OldCommodity", oldCommodity);
			pur.getVoParamMap().put("NewCommodity", newCommodity);
			if (pur.getReceiptTicket().getBadAmount()>0) {
				Commodity badCommodity = TicketPropertyUtil.copyProperties(newCommodity, new Commodity());
				new CommodityLogic().getPropertyChoosableLogic().trunkAppend(badCommodity, "次品");
				pur.getVoParamMap().put("BadCommodity", badCommodity);
			}
		}
		this.getReceiptBillList().clear();
	}
	
	private void setConfirmBad4Service(ViewData<OrderDetail> viewData) throws Exception {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		this.getDomain().getReceiptTicket().genSerialNumber();
		for (OrderDetail purchase: this.getSelectedList()) {
			for (double m=purchase.getReceiptTicket().getBadAmount(); m>0; m=0) {
				OrderDetail d = new PurchaseTicketLogic().genClonePurchase(purchase);
				d.setReceiptTicket(new ReceiptTicket());
				d.getReceiptTicket().setNumber(this.getDomain().getReceiptTicket().getNumber());
				purchase.getVoParamMap().put("NewBad", d);
				d.setMonthnum(new OrderTicketLogic().getSplitMonthnum(d.getMonthnum()));
				d.setCommodity((Commodity)purchase.getVoParamMap().get("BadCommodity"));
				d.setAmount(m);
				d.setNotes(null);
				purList.add(d);
			}
		}
		viewData.setTicketDetails(purList);
		this.getReceiptBillList().addAll(viewData.getTicketDetails());
	}
	
	private void setConfirmPurchase4Service(ViewData<OrderDetail> viewData) throws Exception {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		this.getDomain().getReceiptTicket().genSerialNumber();
		for (OrderDetail d: this.getSelectedList()) {
			Double amount1 = (Double)getNoteFormer4Purchase().getNoteValue(d, new StringBuffer("amount"));
			double amount = amount1!=null? amount1: ((OrderDetail)d.getSnapShot()).getAmount();
			amount = amount - d.getPurchaseTicket().getBackupAmount() - d.getPurchaseTicket().getCancelAmount();
			for (double m=amount; m>-1; m=-1) {
				d.setCommodity((Commodity)d.getVoParamMap().get("NewCommodity"));;
				d.setAmount(m);
				d.setNotes(null);
				purList.add(d);
			}
			d.setNotes(null);
			d.setChangeRemark(null);
		}
		viewData.setTicketDetails(purList);
		for (OrderDetail d: viewData.getTicketDetails()) {
			if (d.getAmount()>0)
				this.getReceiptBillList().add(d);
			d.getReceiptTicket().setNumber(this.getDomain().getReceiptTicket().getNumber());
		}
	}
	
	private void setConfirmOrder4Service(ViewData<OrderDetail> viewData) throws Exception {
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectedList()) {
			Double amount1 = (Double)getNoteFormer4Purchase().getNoteValue(detail, new StringBuffer("amount"));
			double amount = amount1!=null? amount1: ((OrderDetail)detail.getSnapShot()).getAmount(); 
			amount = amount - detail.getPurchaseTicket().getBackupAmount() - detail.getPurchaseTicket().getCancelAmount();
			for (double m=amount; m>-1; m=-1) {
				OrderDetail d = detail;
				d.setCommodity((Commodity)detail.getVoParamMap().get("NewCommodity"));
				d.setAmount(m);
				d.setNotes(null);
				ordList.add(d);
			}
			detail.getReceiptTicket().setBadAmount(0);
			detail.getPurchaseTicket().setBackupAmount(0);
			detail.getPurchaseTicket().setCancelAmount(0);
			detail.getPurchaseTicket().setRearrangeAmount(0);
			detail.getPurchaseTicket().setOverAmount(0);
		}
		viewData.setTicketDetails(ordList);
	}
	
	private void setConfirmBackup4Service(ViewData<OrderDetail> viewData) throws Exception {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		this.getDomain().getReceiptTicket().genSerialNumber();
		for (OrderDetail purchase: this.getSelectedList()) {
			for (double m=purchase.getPurchaseTicket().getBackupAmount()-purchase.getReceiptTicket().getBadAmount(); m>0; m=0) {
				OrderDetail d = new PurchaseTicketLogic().genClonePurchase(purchase);
				d.setReceiptTicket(new ReceiptTicket());
				d.getReceiptTicket().setNumber(this.getDomain().getReceiptTicket().getNumber());
				purchase.getVoParamMap().put("NewBackup", d);
				d.setMonthnum(new OrderTicketLogic().getSplitMonthnum(d.getMonthnum()));
				d.setCommodity((Commodity)purchase.getVoParamMap().get("NewCommodity"));
				d.setAmount(m);
				d.setNotes(null);
				purList.add(d);
			}
		}
		viewData.setTicketDetails(purList);
		this.getReceiptBillList().addAll(viewData.getTicketDetails());
	}
	
	private void setConfirmCancel4Service(ViewData<OrderDetail> viewData) throws Exception {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		OrderDetail first = null;
		for (OrderDetail purchase: this.getSelectedList()) {
			for (double m=purchase.getPurchaseTicket().getCancelAmount(); m>0; m=0) {
				OrderDetail d = new PurchaseTicketLogic().genClonePurchase(purchase);
				purchase.getVoParamMap().put("NewCancel", d);
				d.setMonthnum(new OrderTicketLogic().getSplitMonthnum(d.getMonthnum()));
				d.setCommodity((Commodity)purchase.getVoParamMap().get("OldCommodity"));
				d.setAmount(m);
				d.setReceiptId(0);
				d.setNotes(null);
				d.setChangeRemark("收货改单申请处理为取消采购");
				purList.add(d);
				if (purList.size()==1) {
					first = purList.get(0);
					first.getReturnTicket().genSerialNumber();
					first.getReturnTicket().setRemark("收货改单申请处理为取消采购，发起退货申请");
				}
			}
		}
		PropertyChoosableLogic.TicketDetail<PurchaseReturnForm, ReturnTicket, ReturnT> logic = new ReturnTicketLogic().getPurchaseChoosableLogic();
		for (OrderDetail pur: purList) {
			logic.fromTrunk(logic.getTicketBuilder(), pur.getReturnTicket(), first.getReturnTicket());
			pur.setTReturn(logic.toTrunk(pur.getReturnTicket()));
		}
		viewData.setTicketDetails(purList);
		this.getReceiptBillList().addAll(viewData.getTicketDetails());
	}
	
	private void setConfirmRearrange4Service(ViewData<OrderDetail> viewData) throws Exception {
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectedList()) {
			OrderDetail order = detail;
			for (double m=detail.getPurchaseTicket().getRearrangeAmount(); order.getStOrder()>0 && m>0; m=0) {
				OrderDetail d = new OrderTicketLogic().genCloneOrder(order);
				new PurchaseTicketLogic().setPurchaseTicket(new OrderDetail(), d);
				detail.getVoParamMap().put("NewRearrange", d);
				d.setMonthnum(new OrderTicketLogic().genMonthnum());
				d.setCommodity((Commodity)detail.getVoParamMap().get("OldCommodity"));
				d.setAmount(m);
				d.setNotes(null);
				ordList.add(d);
			}
		}
		viewData.setTicketDetails(ordList);
	}
	
	private void setConfirmOver4Service(ViewData<OrderDetail> viewData) throws Exception {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		this.getDomain().getReceiptTicket().genSerialNumber();
		for (OrderDetail purchase: this.getSelectedList()) {
			for (double m=purchase.getPurchaseTicket().getOverAmount(); m>0; m=0) {
				OrderDetail d = new PurchaseTicketLogic().genClonePurchase(purchase);
				d.setReceiptTicket(new ReceiptTicket());
				d.getReceiptTicket().setNumber(this.getDomain().getReceiptTicket().getNumber());
				purchase.getVoParamMap().put("NewOver", d);
				d.setMonthnum(new OrderTicketLogic().getSplitMonthnum(d.getMonthnum()));
				d.setCommodity((Commodity)purchase.getVoParamMap().get("NewCommodity"));
				d.setAmount(m);
				d.setNotes(null);
				purList.add(d);
			}
		}
		viewData.setTicketDetails(purList);
		this.getReceiptBillList().addAll(viewData.getTicketDetails());
	}
	
	private void setConfirmBill4Service(ViewData<BillDetail> viewData) throws Exception {
		List<BillDetail> billList = new ArrayList<BillDetail>();
		for (OrderDetail pur: this.getReceiptBillList()) {
			BillDetail d = TicketPropertyUtil.copyProperties(pur, new BillDetail());
			d.setMoney(pur.getPrice() * pur.getAmount() * -1);
			d.getBillTicket().setTypeName(new BillTypeLogic().getPurchaseType());
			billList.add(d);
		}
		viewData.setTicketDetails(billList);
	}
	
	private void setConfirmSendBill4Service(ViewData<BillDetail> viewData) throws Exception {
		List<BillDetail> billList = new ArrayList<BillDetail>();
		for (OrderDetail detail: this.getReceiptBillList()) {
			OrderDetail order = detail;
			BillDetail d = TicketPropertyUtil.copyProperties(order, new BillDetail());
			d.setMoney(order.getPrice() * order.getAmount());
			d.getBillTicket().setTypeName(new BillTypeLogic().getSaleType());
			billList.add(d);
		}
		viewData.setTicketDetails(billList);
	}
	
	private void setAdjust4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectedList());
		viewData.setParam(this.getNoteFormer4Order());
	}
	
	private void setAdjustBill4Service(ViewData<BillDetail> viewData) {
		List<BillDetail> billList = new ArrayList<BillDetail>();
		for (OrderDetail detail: this.getSelectedList()) {
			OrderDetail sd = detail.getSnapShot();
			BillDetail bill = TicketPropertyUtil.copyProperties(detail, new BillDetail());
			bill.setPrice(detail.getPrice() - sd.getPrice());
			bill.setMoney(bill.getPrice() * detail.getAmount() * -1);
			bill.getBillTicket().setTypeName(new BillTypeLogic().getPurchaseType());
			if (bill.getPrice() != 0)
				billList.add(bill);
			detail.setVoparam(bill);
		}
		viewData.setTicketDetails(billList);
	}
	
	private void setRollbackReceipt4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getDetailList());
	}
	
	private void setRollbackOrder4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail purchase: this.getDetailList()) {
			OrderDetail order=purchase, sorder=order.getSnapShot();
			if (sorder.getStOrder()>0)
				list.add(order);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setRollbackBill4Service(ViewData<BillDetail> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Bill().getSelectedList());
	}
	
	private void setRollbackBom4Service(ViewData<BomDetail> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Bom().getSelectedList());
	}
	
	private void setRollbackStore4Service(ViewData<AbstractCommodityItem> viewData) {
		viewData.setTicketDetails(new ArrayList<AbstractCommodityItem>());
		viewData.getTicketDetails().addAll(this.getDetailList());
		viewData.getTicketDetails().addAll(this.getSelectFormer4Bom().getSelectedList());
	}
	
	private void setInExtra4Service() {
		StoreTicketForm form = new StoreTicketForm();
		List<OrderDetail> inList = new ArrayList<OrderDetail>();
		List<OrderDetail> bomTranstos = this.getSelectFormer4Bom().getCSelects();
		for (OrderDetail pur: bomTranstos) {
			inList.add(pur);
		}
		form.setInExtra4Service(inList, "instore", "null");
		this.setAttr(form);
	}
	
	private void setRollbackInstoreBom4Service(ViewData<OrderDetail> viewData) {
		this.getAttr(StoreTicketForm.class).setInExtra4Service(viewData);
	}
	
	private void setDetailSnapShot() {
		for (OrderDetail curPurSubtract: this.getSelectedList()) {
			curPurSubtract.setSnapShot1();
		}
	}

	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	public ActionService4LinkListener getActionService4LinkListener() {
		return this.getAttr(ActionService4LinkListener.class);
	}
	
	private List<String> getParam4BomReceipt(List<Object> columns) {
		List<String> paramList = new ArrayList<String>();
		String purMonthnum=(String)columns.get(0), purName=(String)columns.get(1);
		String prtMonthnum = new OrderTicketLogic().getPrtBomMonthnum(purMonthnum);
		paramList.add(prtMonthnum);
		paramList.add(purName);
		return paramList;
	}
}
