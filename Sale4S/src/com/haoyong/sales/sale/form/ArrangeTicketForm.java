package com.haoyong.sales.sale.form;

import java.text.ParseException;
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
import java.util.Set;

import net.sf.mily.common.NoteAccessorFormer;
import net.sf.mily.http.Connection;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.support.form.SelectTicketFormer4Sql;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.ui.enumeration.ParameterName;
import net.sf.mily.util.LogUtil;
import net.sf.mily.webObject.AddNoteListener;
import net.sf.mily.webObject.IEditViewBuilder;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.SqlListBuilder;
import net.sf.mily.webObject.ViewBuilder;
import net.sf.mily.webObject.query.SqlListBuilderSetting;

import org.apache.commons.lang.StringUtils;
import org.hibernate.util.JoinedIterator;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.form.SubCompanyForm;
import com.haoyong.sales.base.logic.ClientLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.PurchaseAgentLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.AbstractForm;
import com.haoyong.sales.common.form.FSqlListRemindable;
import com.haoyong.sales.common.form.FViewInitable;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.listener.ActionService4LinkListener;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.ArrangeT;
import com.haoyong.sales.sale.domain.ArrangeTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.ReturnT;
import com.haoyong.sales.sale.domain.ReturnTicket;
import com.haoyong.sales.sale.domain.TicketUser;
import com.haoyong.sales.sale.logic.ArrangeTicketLogic;
import com.haoyong.sales.sale.logic.ArrangeTypeLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.OrderTypeLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.sale.logic.ReceiptTicketLogic;
import com.haoyong.sales.sale.logic.ReturnTicketLogic;

public class ArrangeTicketForm extends AbstractForm<OrderDetail> implements FViewInitable, FSqlListRemindable {
	
	private List<OrderDetail> detailList, selectedList;
	
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
	
	public void prepareEdit() {
		this.detailList = new ArrayList<OrderDetail>(getSelectFormer4Order().getSelectedList());
	}
	
	public void prepareChange() {
		prepareEdit();
		getDomain().setChangeRemark(null);
	}
	
	public void prepareRechange() {
		this.detailList = new ArrayList<OrderDetail>(getSelectFormer4Order().getSelectedList());
	}
	
	private void prepareRechange2Arrange() {
		OrderDetail order = new OrderTicketLogic().genCloneOrder(this.getSelectFormer4Order().getFirst());
		this.getNoteFormer4Order().setEntityChanges(order);
		order.setAmount(0);
		this.setOrderDetail(order);
		SqlListBuilder sqlBuilder = (SqlListBuilder)EntityClass.loadViewBuilder(this.getClass(), "ArrangeList.tabArrange").getFieldBuildersDeep(SqlListBuilder.class).get(0);
		HashMap<String, String> filters = new HashMap<String, String>();
		if (true) {
			StringBuffer monthnumList = new StringBuffer();
			for (OrderDetail item: this.getSelectFormer4Order().getSelectedList()) {
				monthnumList.append("=").append(item.getMonthnum()).append(" ");
			}
			filters.put("monthnum", monthnumList.toString());
		}
		SqlListBuilderSetting childSetting = getSearchSetting(sqlBuilder);
		childSetting.addFilters(filters);
		sqlBuilder.getSqlQuery().getFields().asSetting(childSetting);
	}
	
	public void prepareAdjust() {
		this.detailList = new ArrayList<OrderDetail>(getSelectFormer4Order().getSelectedList());
	}
	
	public void prepareDoadjust() {
		this.detailList = new ArrayList<OrderDetail>(getSelectFormer4Order().getSelectedList());
	}
	
	public void preparePurChange() {
		this.getPurchaseList().clear();
		this.getPurchaseList().addAll( getSelectFormer4Purchase().getSelectedList() );
		this.getNoteFormer4Purchase().getSourceRemark(this.getSelectFormer4Purchase().getFirst());
	}
	
	/**
	 * 用于可接单、可排单
	 */
	public void canArrange(List<List<Object>> valiRows) {
		// commName,uneditable
		StringBuffer sb = new StringBuffer();
		if (valiRows.size() == 0)
			sb.append("请选择一个待安排订单！");
		for (List<Object> row: valiRows) {
			if (row.get(1) != null) {
				sb.append(row.get(0)).append(row.get(1)).append("\t");
			}
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}
	
	public void canChangeConfirm(List<List<Object>> valiRows) {
		// commName,stateId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (!(Integer.parseInt(row.get(1)+"")==42)) {
				sb.append(row.get(0)).append("\t");
			}
		}
		if (sb.length() > 0) {
			sb.append("不用改单申请确认！");
			throw new LogicException(2, sb.toString());
		}
	}
	
	public void canAdjust(List<List<Object>> valiRows) {
		// commName,stateId
		StringBuffer sb = new StringBuffer();
		for (List<Object> row: valiRows) {
			if (!(Integer.parseInt(row.get(1)+"")==50 || Integer.parseInt(row.get(1)+"")==53))
				sb.append(row.get(0)).append("\t");
		}
		if (sb.length() > 0) {
			sb.append("不用红冲处理！");
			throw new LogicException(2, sb.toString());
		}
	}
	
	public void canRechange(List<List<Object>> valiRows) {
		// commName,arrangeId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			int ierror = 0;
			sitem = new StringBuffer().append(row.get(0));
			if (Integer.parseInt(row.get(1)+"") != 40) {
				sitem.append("不用改单申请处理");
				ierror++;
			}
			sitem.append("\t");
			if (ierror>0)		sb.append(sitem);
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canDoadjust(List<List<Object>> valiRows) {
		// commName,uneditable,arrangeId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (!(Integer.parseInt(row.get(2)+"")==30 || Integer.parseInt(row.get(2)+"")==50))
				sitem.append(row.get(1)).append("不能发起红冲；");
			if (sitem.length()>0)			sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canDoadjustNot(List<List<Object>> valiRows) {
		// commName,uneditable,arrangeId
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			int ierror = 0;
			sitem = new StringBuffer().append(row.get(0));
			if (Integer.parseInt(row.get(2)+"") != 52) {
				sitem.append(row.get(1)).append("不用红冲驳回确认；");
				ierror++;
			}
			if (ierror>0)		sb.append(sitem);
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canPurChange(List<List<Object>> valiRows) {
		// commName,arrangeId,stPurchase
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			int ierror = 0;
			sitem = new StringBuffer().append(row.get(0));
			if (Integer.parseInt(row.get(1)+"")!=43 || Integer.parseInt(row.get(2)+"")==0) {
				sitem.append("不用采购改单处理");
				ierror++;
			}
			sitem.append("\t");
			if (ierror>0)		sb.append(sitem);
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canUStoreBack(List<List<Object>> valiRows) {
		// commName,uneditable,stOrder
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (!(valiRows.size()>0 && getSelectFormer4Order().getSelectedList().size()>0))
			sb.append("请选择待排订单，备料库存！");
		int irow=-1;
		for (List<Object> row: valiRows) {
			irow++;
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if ((Integer)row.get(2)>0)
				sitem.append("客户订单请走挪用订单库存，");
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canUStoreOrder(List<List<Object>> valiRows) {
		// commName,uneditable,number,stOrder
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (!(valiRows.size()>0 && getSelectFormer4Order().getSelectedList().size()>0))
			sb.append("请选择待排订单，订单库存！");
		int irow=-1;
		for (List<Object> row: valiRows) {
			irow++;
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if (StringUtils.equals((String)row.get(2), getSelectFormer4Order().getFirst().getOrderTicket().getNumber()))
				sitem.append("订单相同不用挪料，");
			if ((Integer)row.get(3)==0)
				sitem.append("备料请走绑定备货库存，");
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canUPassageBack(List<List<Object>> valiRows) {
		// commName,uneditable,stOrder,orderType
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (!(valiRows.size()>0 && getSelectFormer4Order().getSelectedList().size()>0))
			sb.append("请选择待排订单，备料在途！");
		int irow=-1;
		for (List<Object> row: valiRows) {
			irow++;
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if ((Integer)row.get(2)>0 && new OrderTypeLogic().isClientType((String)row.get(3)))
				sitem.append("订单占用料请走挪用订单在途");
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canUPassageOrder(List<List<Object>> valiRows) {
		// commName,uneditable,number,stOrder,orderType
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (!(valiRows.size()>0 && getSelectFormer4Order().getSelectedList().size()>0))
			sb.append("请选择待排订单，订单在途！");
		int irow=-1;
		for (List<Object> row: valiRows) {
			irow++;
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if ((Integer)row.get(3)==0 || new OrderTypeLogic().isBackType((String)row.get(4)))
				sitem.append("备料请走绑定备货在途，");
			if (StringUtils.equals((String)row.get(2), this.getSelectFormer4Order().getFirst().getOrderTicket().getNumber()))
				sitem.append("同一订单不用挪料，");
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canTrans2Normal(List<List<Object>> valiRows) {
		// commName,uneditable,arrangeType,stPurchase,receiptId
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (valiRows.size()==0)
			sb.append("请选择订单！");
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if (new ArrangeTypeLogic().isNormal((String)row.get(2))==true)
				sitem.append("原安排已经是常规库存方式，");
			if ((Integer)row.get(3)>0 && (Integer)row.get(4)==0 && this.getTransNone()==true)
				sitem.append("请选择对原采购在途的处理方式，");
			if (row.get(4)!=null && (Integer)row.get(4)>=30 && this.getTransNone()==true)
				sitem.append("请选择对原采购在库的处理方式，");
			if ((Integer)row.get(3)==0 && this.getTransNone()==false)
				sitem.append("采购单未开时请选择未采购在途处理方式，");
			
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canTrans2Delete(List<List<Object>> valiRows) {
		// commName,stPurchase,receiptId
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (valiRows.size()==0)
			sb.append("请选择订单！");
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if ((Integer)row.get(1)>0 && (Integer)row.get(2)==0 && this.getTransNone()==true)
				sitem.append("请选择原采购在途处理方式，");
			if ((Integer)row.get(2)>=30 && this.getTransBack()==false)
				sitem.append("已经收货，不能选择采购取消");
			if ((Integer)row.get(1)==0 && this.getTransNone()==false)
				sitem.append("采购单未开时请选择未采购在途处理方式，");
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canTrans2Backup(List<List<Object>> valiRows) {
		// commName,stPurchase,stOrder,orderType
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (valiRows.size()==0)
			sb.append("请选择订单！");
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if ((Integer)row.get(1)==0)
				sitem.append("没有采购开单，请走终止订单，");
			if ((Integer)row.get(2)==0 || new OrderTypeLogic().isBackType((String)row.get(3)))
				sitem.append("已经是备货订单类型，");
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canTrans2UStoreBack(List<List<Object>> valiRows) {
		// commName,uneditable,stOrder,monthnum
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (!(valiRows.size()>0 && getSelectFormer4Order().getSelectedList().size()>0))
			sb.append("请选择已排订单，备料库存！");
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			if (order.getStPurchase()>0 && this.getTransNone()==true)
				sb.append("请选择原采购在途处理方式，");
			if (order.getStPurchase()>0 && this.getTransNone()==true)
				sb.append("采购单已开时请选择采购在途处理方式，");
			if (order.getStPurchase()==0 && this.getTransNone()==false)
				sb.append("采购单未开时请选择未采购在途处理方式，");
		}
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if ((Integer)row.get(2)>0)
				sitem.append("客户订单类型请走挪用订单库存，");
			for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
				if (row.get(3).equals(order.getMonthnum()))
					sitem.append("不能选择同一个月流水号订单！");
			}
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canTrans2UStoreOrder(List<List<Object>> valiRows) {
		// commName,uneditable,stOrder,monthnum
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (!(valiRows.size()>0 && getSelectFormer4Order().getSelectedList().size()>0))
			sb.append("请选择已排订单,备料库存！");
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			if (order.getStPurchase()>0 && this.getTransNone())
				; //两个订单交换采购单
			if (order.getStPurchase()>0 && order.getSendId()==20 && this.getTransCancel())
				sb.append("订单采购已经收货不能取消，");
			if (order.getStPurchase()==0 && this.getTransNone()==false)
				sb.append("采购单未开时请选择未采购在途处理方式，");
		}
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if ((Integer)row.get(2)==0)
				sitem.append("备货订单类型请走绑定库存备料，");
			for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
				if (row.get(3).equals(order.getMonthnum()))
					sitem.append("不能选择同一个月流水号订单！");
			}
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canTrans2UPassageBack(List<List<Object>> valiRows) {
		// commName,uneditable,monthnum,stOrder,orderType
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (!(valiRows.size()>0 && getSelectFormer4Order().getSelectedList().size()>0))
			sb.append("请选择已排订单,备料在途！");
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			if (order.getStPurchase()>0 && this.getTransNone())
				sb.append("请选择原采购在途处理方式，");
			if (order.getStPurchase()>0 && order.getSendId()==20 && this.getTransCancel())
				sb.append("订单采购已经收货不能取消，");
			if (order.getStPurchase()>0 && this.getTransNone()==true)
				sb.append("采购单已开时请选择采购在途处理方式，");
			if (order.getStPurchase()==0 && this.getTransNone()==false)
				sb.append("采购单未开时请选择未采购在途处理方式，");
		}
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if ((Integer)row.get(3)>0 && new OrderTypeLogic().isClientType((String)row.get(4)))
				sitem.append("客户订单类型请走挪用订单在途，");
			for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
				if (row.get(2).equals(order.getMonthnum()))
					sitem.append("不能选择同一个月流水号订单！");
			}
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void canTrans2UPassageOrder(List<List<Object>> valiRows) {
		// commName,uneditable,monthnum,stOrder,orderType
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (!(valiRows.size()>0 && getSelectFormer4Order().getSelectedList().size()>0))
			sb.append("请选择已排订单,备料在途！");
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			if (order.getStPurchase()>0 && this.getTransNone())
				; // 2订单交换采购单
			if (order.getStPurchase()>0 && order.getSendId()==20 && this.getTransCancel())
				sb.append("订单采购已经收货不能取消，");
			if (order.getStPurchase()==0 && this.getTransNone()==false)
				sb.append("采购单未开时请选择未采购在途处理方式，");
		}
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append("，");
			if ((Integer)row.get(3)==0 || new OrderTypeLogic().isBackType((String)row.get(4)))
				sitem.append("备货订单类型，请走绑定在途备料，");
			for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
				if (row.get(2).equals(order.getMonthnum()))
					sitem.append("不能选择同一个月流水号订单！");
			}
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	/**
	 * 用于可接单、可排单
	 */
	public void canTrans2Purchase(List<List<Object>> valiRows) throws Exception {
		// commName,uneditable,arrangeType,sendId,deliverNote,stPurchase,receiptId
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (valiRows.size() == 0)
			sb.append("请选择调整的订单！");
		if (getOrderDetail().getArrangeTicket().getArrangeType() == null)
			sb.append("请选择订单发货方式！");
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1) != null)
				sitem.append(row.get(1)).append("，");
			if (row.get(2).equals(getOrderDetail().getArrangeTicket().getArrangeType()))
				sitem.append("订单已经是").append(row.get(2)).append("安排方式，");
			if (new DeliverTypeLogic().getStringTypes().indexOf(row.get(2))==-1 && getArrangeChoosableLogic().isValid(getOrderDetail().getArrangeTicket(), sb)==false)
				sitem.append("请补充排单信息！");
			if (row.get(4)==null && new DeliverTypeLogic().isNeedDeliverNote(getOrderDetail().getArrangeTicket().getArrangeType()) && getOrderDetail().getArrangeTicket().getDeliverNote()==null)
				sitem.append("直发、当地购请填写发货备注，");
			if ((Integer)row.get(5)>0 && (Integer)row.get(6)==0 && this.getTransNone()==true)
				sitem.append("请选择原采购在途处理方式，");
			if (row.get(6)!=null && (Integer)row.get(6)>=30 && this.getTransNone()==true)
				sitem.append("已经收货，不能选择未采购处理，");
			if ((Integer)row.get(5)==0 && this.getTransNone()==false)
				sitem.append("采购单未开时请选择未采购在途处理方式，");
			if (sitem.length() > 0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.toString());
	}
	
	public void validateChangeApply() {
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.selectedList) {
			if (getSelectedFirst4Order().getStOrder()==30) {
				if (getNoteFormer4Order().isNoted(detail)==false)
					sb.append("请填写改单申请更改的内容！");
			} else {
				if (getNoteFormer4Order().isChangedNotesEX(detail)==false)
					sb.append("请填写改单申请更改的内容！");
			}
			if (getNoteFormer4Order().isChangedRemark(detail)==false)
				sb.append("改单申请备注不能为空！");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	public void validateRechangeYes() {
		StringBuffer sb = new StringBuffer();
		OrderTicketLogic logic = new OrderTicketLogic();
		for (OrderDetail detail: this.selectedList) {
			if (logic.getVoNoteMap(getNoteFormer4Order(), detail, ArrangeTicket.class).isEmpty()) {
				sb.append("请不要清空改单申请内容！");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
		this.getOrderLimitList().clear();
		this.getOrderLimitList().addAll(this.selectedList);
		OrderDetail detail = this.getSelectedList().get(0);
		String type = getNoteFormer4Order().getNoteString(detail, new StringBuffer("arrangeType"));
		this.getOrderDetail().getArrangeTicket().setArrangeType(type);
		LogUtil.info(new StringBuffer("\n******").append(this.getOrderDetail()).append(this.getOrderDetail().getArrangeTicket()).append(type).toString());
	}
	
	public void validateRechangeNo() {
		StringBuffer sb = new StringBuffer();
		OrderTicketLogic logic = new OrderTicketLogic();
		for (OrderDetail detail: this.selectedList) {
			if (logic.getVoNoteMap(getNoteFormer4Order(), detail, ArrangeTicket.class).isEmpty()) {
				sb.append("不要清空改单申请内容！");
			}
			if (getNoteFormer4Order().isChangedRemark(detail)==false) {
				sb.append("请补充改单备注！");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	public void validateDoadjust() {
		String arrangeType=getNoteFormer4Order().getNoteString(getDomain(), new StringBuffer("arrangeType"));
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.selectedList) {
			if (getNoteFormer4Order().isChangedNotesEX(detail)==false)
				sb.append("请填写红冲内容！");
			if (StringUtils.equals(detail.getArrangeTicket().getArrangeType(), arrangeType))
				sb.append("发货方式同为").append(arrangeType).append("不用红冲，");
			if (getNoteFormer4Order().isChangedRemark(detail)==false)
				sb.append("请补充红冲备注！");
			String deliverNote = getNoteFormer4Order().getNoteString(detail, new StringBuffer("deliverNote"));
			if (Arrays.asList(new String[]{"直发", "当地购"}).indexOf(arrangeType)>-1 && !(deliverNote!=null || detail.getArrangeTicket().getDeliverNote()!=null))
				sb.append("直当、当地购的发货方式时要填写发货备注！");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	public void validateAdjustYes() {
		StringBuffer sb = new StringBuffer();
		String cancelType = getNoteFormer4Order().getNoteString(getDomain(), new StringBuffer("cancelType"));
		for (OrderDetail detail: this.selectedList) {
			if (this.getNoteFormer4Order().isNoted(detail)==false)
				sb.append("无红冲更改内容！");
			if (detail.getStPurchase()==0) {
				if (StringUtils.isBlank(cancelType)==false)
					sb.append("未采购订单不用原采购处理方式，");
			} else if (StringUtils.equals("无影响", cancelType)==true) {
				if (this.getNoteFormer4Order().getNoteValue(detail, new StringBuffer("amount"))!=null)
					sb.append("有采购减少的订单要选择取消、转备料，走重新排单；");
			} else if (StringUtils.equals("无影响", cancelType)==false) {
				sb.append("有采购申请取消|转备料的订单请走重新排单，");
			}
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	public void validateAdjustNo() {
		StringBuffer sb = new StringBuffer();
		if (getNoteFormer4Order().isChangedRemark( getDomain() )==false)
			sb.append("请补充红冲备注！");
		String cancelType = getNoteFormer4Order().getNoteString(getDomain(), new StringBuffer("cancelType"));
		if (StringUtils.isBlank(cancelType)==false)
			sb.append("驳回时不用选择原采购处理方式，");
		for (OrderDetail detail: this.selectedList) {
			if (this.getNoteFormer4Order().isNoted(detail)==false)
				sb.append("不要清空红冲更改内容！");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	public void validateAdjustRearrange() {
		StringBuffer sb = new StringBuffer();
		String cancelType = getNoteFormer4Order().getNoteString(getDomain(), new StringBuffer("cancelType"));
		for (OrderDetail detail: this.selectedList) {
			if (detail.getStPurchase()==0) {
				if (StringUtils.isBlank(cancelType)==false)
					sb.append("未采购订单不用选择原采购处理方式，");
			} else if (StringUtils.equals("转备料", cancelType)==true) {
			} else if (detail.getReceiptId()==0) {
				if (StringUtils.equals("取消", cancelType)==false)
					sb.append("采购在途订单请选择取消、转备料，");
			} else if (detail.getReceiptId()>0) {
				if (StringUtils.equals("退货", cancelType)==false)
					sb.append("采购已收货订单请选择退货、转备料，");
			}
			if (this.getNoteFormer4Order().isNoted(detail)==false)
				sb.append("不要清空红冲更改内容！");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.toString());
		else
			getOrderDetail().getArrangeTicket().setCancelType(cancelType);
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
	
	public void validatePurchase() {
		StringBuffer sb = new StringBuffer();
		this.getOrderDetail().getArrangeTicket().setNumber("00");
		if (getOrderDetail().getArrangeTicket().getArrangeType() == null)
			sb.append("请选择订单发货方式！");
		if (getArrangeChoosableLogic().isValid(getOrderDetail().getArrangeTicket(), sb)==false)
			sb.append("请补充排单信息！");
		if (Arrays.asList(new String[]{"直发", "当地购"}).indexOf(getOrderDetail().getArrangeTicket().getArrangeType())>-1 && getOrderDetail().getArrangeTicket().getDeliverNote()==null)
			sb.append("直当、当地购的发货方式时要填写发货备注！");
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	// 同意收货改单申请的排单处理方式
	public void validatePurChangeYes() {
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.getPurchaseList()) {
			if (getNoteFormer4Purchase().isChangedNotesEX(detail))
				sb.append("改单申请内容有更改，请走不同意；");
			if (getNoteFormer4Purchase().isChangedRemark(detail))
				sb.append("改单申请备注不能更改，");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.insert(0, "同意改单申请处理方式，").deleteCharAt(sb.length()-1).toString());
	}
	
	// 调整收货改单申请的排单处理方式
	public void validatePurChangeNo() throws Exception {
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.getPurchaseList()) {
			getNoteFormer4Purchase().isChangedNotesEX(detail);
			if (getNoteFormer4Purchase().isChangedRemark(detail)==false)
				sb.append("请补充改单申请备注，");
		}
		sb.append(new PurchaseTicketForm().validatePurchaseChangeAmount(this.getPurchaseList(), this.getNoteFormer4Purchase()));
		if (sb.length()>0)		throw new LogicException(2, sb.insert(0, "不同意，").deleteCharAt(sb.length()-1).toString());
	}
	
	private void validateSubCompany() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isEmpty(this.getOrderDetail().getSubCompany().getName()))
			sb.append("请选择分公司查看库存,");
		for (OrderDetail detail: this.getSelectFormer4Order().getSelectedList()) {
			if (StringUtils.equals(detail.getSubCompany().getName(), this.getOrderDetail().getSubCompany().getName())==false)
				sb.append("请选择与订单相同的分公司,");
		}
		if(sb.length()>0)		throw new LogicException(2, sb.toString());
	}
	
	private void commitAdjust() throws ParseException {
		String remark=(String)getDomain().getVoParamMap().get("changeRemark"), change=getDomain().getChangeRemark();
		StringBuffer sb = new StringBuffer();
		sb.append(remark==null? "": remark).append(sb.length()==0? "": "。").append(change==null? "": change);
		getDomain().setChangeRemark(sb.length()==0? null: sb.toString());
	}
	
	private void clearArrange() {
		this.setAttr(new OrderDetail());
	}

	public void viewinit(IEditViewBuilder viewBuilder0) {
		ViewBuilder viewBuilder = (ViewBuilder)viewBuilder0;
		if (true)
			new ReceiptTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new PurchaseTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			getArrangeChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new OrderTicketLogic().getTicketChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new SupplierLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new ClientLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
		if (true)
			new CommodityLogic().getPropertyChoosableLogic().trunkViewBuilder(viewBuilder);
	}
	
	public List<OrderDetail> getDetailList() {
		return this.detailList;
	}
	
	public void setDetailList(List<OrderDetail> details) {
		this.detailList = details;
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
	
	private List<OrderDetail> getOrderLimitList() {
		String k = "OrderLimitList";
		List<OrderDetail> list = getAttr(k);
		if (list==null) {
			list = new ArrayList<OrderDetail>();
			setAttr(k, list);
		}
		return list;
	}
	
	public void clearOrderLimitList() {
		getOrderLimitList().clear();
	}
	
	private ChooseFormer getChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		former.setSellerViewSetting(getArrangeChoosableLogic().getChooseSetting(getArrangeChoosableLogic().getTicketBuilder()));
		former.setViewBuilder(getArrangeChoosableLogic().getTicketBuilder());
		return former;
	}

	public void setSelectedList(List<OrderDetail> selected) {
		this.selectedList = selected;
	}

	public List<OrderDetail> getSelectedList() {
		return this.selectedList;
	}

	public OrderDetail getSelectedFirst4Order() {
		return this.getSelectFormer4Order().getFirst().getSnapShot();
	}
	
	private List<SupplyType> getSupplyTypeOptions(Object entity) {
		List<SupplyType> typeList = new SupplyTypeLogic().getTypeList();
		return typeList;
	}
	
	public OrderDetail getDomain() {
		if (this.detailList==null || this.detailList.size()==0)
			return null;
		return this.detailList.get(0);
	}
	
	public Map<String, String> getDomainNotes() {
		Map<String, String> map = getNoteFormer4Order().getVoNoteMap(getDomain());
		return map;
	}
	
	private ArrangeTicketForm getTabArrange() {
		return this;
	}
	private ArrangeTicketForm getTabUPassage() {
		return this;
	}
	private ArrangeTicketForm getTabUStore() {
		return this;
	}
	private ArrangeTicketForm getTabUSubCompany() {
		return this;
	}
	
	public OrderDetail getPurchaseFirst() {
		return this.getPurchaseList().get(0);
	}
	
	public HashMap<String, String> getParam4Supply() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("supplyType", "'采购'");
		return map;
	}
	
	public HashMap<String, String> getParam4Arrange() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.putAll(getParam4Supply());
		StringBuffer sb = new StringBuffer();
		if (this.getOrderLimitList().isEmpty()) {
			sb.append("1=1");
		} else {
			sb.append("(");
			for (Iterator<OrderDetail> iter=this.getOrderLimitList().iterator(); iter.hasNext();) {
				OrderDetail d = iter.next();
				sb.append("c.id=").append(d.getId());
				if (iter.hasNext())			sb.append(" or ");
			}
			sb.append(")");
		}
		map.put("orderLimit", sb.toString());
		map.put("SubCompanySellerId", this.getOrderDetail().getSubCompany().getFromSellerId()+"");
		return map;
	}
	
	protected List<String> getAgentOptions(ArrangeTicket arrangeTicket) {
		List<String> list = new PurchaseAgentLogic().getStringTypes("采购");
		return list;
	}
	
	private List<String> getArrangeOptions(Object arrangeTicket) {
		List<String> list = new ArrangeTypeLogic().getArrangeTypes();
		list.add(0, "重新排单");
		return list;
	}
	
	protected List<String> getArrangeOptions4New(Object arrangeTicket) {
		List<String> list = new DeliverTypeLogic().getStringTypes();
		return list;
	}
	
	private List<String> getArrangeOptions4Doadjust(Object arrangeTicket) {
		DeliverTypeLogic logic = new DeliverTypeLogic();
		return logic.getStringTypes();
	}
	
	private List<Map.Entry<String, String>> getArrangeOptions4Trans(Object arrangeTicket) {
		Map<String, String> list = new LinkedHashMap<String, String>();
		list.put("取消", "取消");
		list.put("转备料", "转备料");
		list.put("未采购、交换采购", "无");
		return new ArrayList(list.entrySet());
	}
	
	private List<String> getArrangeOptions4Adjust(Object arrangeTicket) {
		List<String> list = new ArrayList<String>();
		list.add("无影响");
		list.add("取消");
		list.add("退货");
		list.add("转备料");
		return list;
	}
	
	protected List<Map.Entry<String, String>> getPurchaseAgreeOptions(Object detail0) {
		return new ReceiptTicketForm().getPurchaseAgreeOptions(detail0);
	}
	
	protected List<Map.Entry<String, String>> getOrderAgreeOptions(Object detail0) {
		return new ReceiptTicketForm().getOrderAgreeOptions(detail0);
	}
	
	public void setCType() {
		String ctype = getOrderDetail().getCommodity().getCommType();
		if (StringUtils.isEmpty(ctype)) {
			throw new LogicException(2, "请选择排单分类");
		}
		for (OrderDetail detail: this.detailList) {
			detail.getCommodity().setCommType(ctype);
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
	
	public void setChangeNotes(Component fcomp) {
		Window win = fcomp.searchParentByClass(Window.class);
		List<AddNoteListener> noteList = win.getInnerFormerList(AddNoteListener.class);
		Map<String, String> domainMap = new HashMap<String, String>();
		Map<String, String> domainAll = this.getNoteFormer4Order().getVoNoteMap(this.getDomain());
		for (AddNoteListener note: noteList) {
			String name=note.getPropertyName(), value=note.getNoteString();
			if (domainAll.containsKey(name)==false)
				continue;
			ListView listview = note.getComponent().searchFormerByClass(ListView.class);
			if (!(listview==null && note.getEntity()==getDomain()))			continue;
			domainMap.put(name, value);
		}
		for (OrderDetail detail: this.detailList) {
			this.getNoteFormer4Order().getVoNoteMap(detail).putAll(domainMap);
			this.getNoteFormer4Order().isChangedNotesEX(detail);
			detail.setChangeRemark(this.getDomain().getChangeRemark());
		}
	}
	
	public void setPurchaseChangeNotes(Component fcomp) {
		Window win = fcomp.searchParentByClass(Window.class);
		List<AddNoteListener> noteList = win.getInnerFormerList(AddNoteListener.class);
		Map<String, String> domainMap = new HashMap<String, String>();
		Map<String, String> domainAll = this.getNoteFormer4Purchase().getVoNoteMap(this.getPurchaseFirst());
		for (AddNoteListener note: noteList) {
			String name=note.getPropertyName(), value=note.getNoteString();
			if (domainAll.containsKey(name)==false)
				continue;
			ListView listview = note.getComponent().searchFormerByClass(ListView.class);
			if (listview!=null && listview.getViewBuilder().getName().equals("purchaseList"))
				continue;
			if (!(note.getEntity()==getPurchaseFirst()))			continue;
			domainMap.put(name, value);
		}
		for (OrderDetail detail: this.getPurchaseList()) {
			this.getNoteFormer4Purchase().getVoNoteMap(detail).putAll(domainMap);
			this.getNoteFormer4Purchase().isChangedNotesEX(detail);
			detail.setChangeRemark(this.getPurchaseFirst().getChangeRemark());
		}
	}
	
	public void setPurchaseChangeReceipt() {
		for (OrderDetail detail: this.getPurchaseList()) {
			detail.getPurchaseTicket().setBackupAmount(this.getPurchaseFirst().getPurchaseTicket().getBackupAmount());
			detail.getPurchaseTicket().setCancelAmount(this.getPurchaseFirst().getPurchaseTicket().getCancelAmount());
			detail.getPurchaseTicket().setRearrangeAmount(this.getPurchaseFirst().getPurchaseTicket().getRearrangeAmount());
			detail.getPurchaseTicket().setOverAmount(this.getPurchaseFirst().getPurchaseTicket().getOverAmount());
		}
	}
	
	public void setChangeUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUchange(suser);
		}
	}
	
	private void setArrangeUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail d: viewData.getTicketDetails()) {
			d.setUarrange(suser);
		}
	}
	
	private void setCancelUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail pur: viewData.getTicketDetails()) {
			pur.setUcancel(suser);
		}
	}
	
	private void setReturnUser(ViewData<OrderDetail> viewData) {
		String suser = genTicketUser().getUserDate();
		for (OrderDetail pur: viewData.getTicketDetails()) {
			pur.setUreturn(suser);
		}
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
	
	public void setChange4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.selectedList);
		viewData.setParam("ChangeList", Arrays.asList(new String[]{"订单"}));
		viewData.setParam(this.getNoteFormer4Order());
	}
	
	public void setRechange4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.selectedList);
		viewData.setParam(getNoteFormer4Order());
		viewData.setParam("ChangeType", ArrangeTicket.class);
	}
	
	public void setDoadjust4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.selectedList);
		viewData.setParam(getNoteFormer4Order());
		viewData.setParam("AdjustType", ArrangeTicket.class);
	}
	
	private void setPurChange4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getPurchaseList());
	}
	
	private void setArrange4Service(ViewData<OrderDetail> viewData) {
		this.getOrderDetail().getArrangeTicket().setNumber(null);
		this.getOrderDetail().getArrangeTicket().genSerialNumber();
		PropertyChoosableLogic.TicketDetail logic = this.getArrangeChoosableLogic();
		for (OrderDetail d: this.getSelectFormer4Order().getSelectedList()) {
			logic.fromTrunk(logic.getTicketBuilder(), d.getArrangeTicket(), this.getOrderDetail().getArrangeTicket());
		}
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
	}

	private void setArrangeLink4Service(ViewData<OrderDetail> viewData) {
		HashMap<Long, List<OrderDetail>> sellerOrders = new HashMap<Long, List<OrderDetail>>();
		for (OrderDetail d: this.getSelectFormer4Order().getSelectedList()) {
			long sellerId = d.getSubCompany().getFromSellerId();
			if (sellerId==0)
				continue;
			List<OrderDetail> orders = sellerOrders.get(sellerId);
			if (orders==null) {
				orders = new ArrayList<OrderDetail>();
				sellerOrders.put(sellerId, orders);
			}
			orders.add(d);
		}
		for (Map.Entry<Long, List<OrderDetail>> entry: sellerOrders.entrySet()) {
			long sellerId = entry.getKey();
			List<OrderDetail> orders = entry.getValue();
			ActionService4LinkListener listener = new ActionService4LinkListener();
			Seller toSeller = new Seller4lLogic().getSellerById(sellerId);
			listener.getOnceAttributes().put("seller", toSeller);
			User user = TicketPropertyUtil.copyProperties(this.getUser(), new User());
			user.setUserName(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
			listener.getOnceAttributes().put("user", user);
			this.setAttr(listener);
			
			listener.actionBefore();// Before
			this.getOrderDetail().getArrangeTicket().setNumber(null);
			this.getOrderDetail().getArrangeTicket().genSerialNumber();
			PropertyChoosableLogic.TicketDetail logic = this.getArrangeChoosableLogic();
			for (OrderDetail d: orders) {
				logic.fromTrunk(logic.getTicketBuilder(), d.getArrangeTicket(), this.getOrderDetail().getArrangeTicket());
			}
			viewData.setTicketDetails(orders);
			listener.actionAfter(true);// After
		}
	}
	
	private void setArrangeNormal4Service(ViewData<OrderDetail> viewData) {
		this.getOrderDetail().getArrangeTicket().setNumber(null);
		this.getOrderDetail().getArrangeTicket().genSerialNumber();
		this.getOrderDetail().getArrangeTicket().setArrangeType(new ArrangeTypeLogic().getNormal());
		PropertyChoosableLogic.TicketDetail logic = this.getArrangeChoosableLogic();
		for (OrderDetail d: this.getSelectFormer4Order().getSelectedList()) {
			logic.fromTrunk(logic.getTicketBuilder(), d.getArrangeTicket(), this.getOrderDetail().getArrangeTicket());
		}
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
		this.getOrderDetail().getArrangeTicket().setArrangeType(null);
	}
	
	private void setAdjust4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.selectedList);
		viewData.setParam(this.getNoteFormer4Order());
		viewData.setParam("订单");
	}
	
	private void setAdjustRearrange4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		getOrderDetail().getArrangeTicket().setCancelType((String)getNoteFormer4Order().getNoteValue(getDomain(), new StringBuffer("cancelType")));
		for (OrderDetail order: this.selectedList) {
			OrderDetail sorder = order.getSnapShot();
			OrderDetail rearrange = order;
			if (sorder.getStPurchase()>0) {
				rearrange = new OrderTicketLogic().genCloneOrder(sorder);
				new PurchaseTicketLogic().setPurchaseTicket(new OrderDetail(), rearrange);
				rearrange.setMonthnum(new OrderTicketLogic().genMonthnum());
			}
			LinkedHashMap<String, String> mall=this.getNoteFormer4Order().getVoNoteMap(order);
			this.getNoteFormer4Order().setEntityChanges(rearrange, mall);
			rearrange.setArrangeTicket(new ArrangeTicket());
			rearrange.setChangeRemark(null);
			rearrange.setNotes(null);
			order.getVoParamMap().put("RearrangeOrder", rearrange);
			if (rearrange.getAmount()>0)
				orderList.add(rearrange);
			order.setChangeRemark(null);
			order.setNotes(null);
		}
		viewData.setTicketDetails(orderList);
	}
	
	private void setAdjustBack4Service(ViewData<OrderDetail> viewData) {
		String cancelType = getNoteFormer4Order().getNoteString(getDomain(), new StringBuffer("cancelType"));
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getSelectedList()) {
			purList.add(order);
		}
		viewData.setTicketDetails(purList);
	}
	
	private void setAdjustCancel4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> purList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getSelectedList()) {
			OrderDetail sorder = order.getSnapShot();
			if (sorder.getStPurchase()>0)
				purList.add(order);
		}
		viewData.setTicketDetails(purList);
	}
	
	private void setAdjustEnough4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.selectedList);
		for (OrderDetail d: new ArrayList<OrderDetail>(this.selectedList)) {
			OrderDetail rearrange = (OrderDetail)d.getVoParamMap().get("RearrangeOrder");
			if (rearrange != null)
				viewData.getTicketDetails().add(rearrange);
		}
	}
	
	private void setDeliver4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.detailList);
		for (OrderDetail detail: this.detailList) {
			detail.getArrangeTicket().setDeliverNote(getOrderDetail().getArrangeTicket().getDeliverNote());
		}
		getOrderDetail().getArrangeTicket().setDeliverNote(null);
	}
	
	/**
	 * 订单Order>库存Purchase，订单拆有库存的，剩余无库存的用新月流水
	 * 订单<库存，库存拆给订单的用新分支月流水，剩余库存记原采购
	 * 订单.FromStore记用那个库存
	 */
	private void setExchangeSplit4Service() {
		LinkedHashSet<OrderDetail> orderSplitList = new LinkedHashSet<OrderDetail>();
		LinkedHashSet<OrderDetail> purSplitList = new LinkedHashSet<OrderDetail>();
		Iterator<OrderDetail> purIter=this.getSelectFormer4Purchase().getSelectedList().iterator();
		Iterator<OrderDetail> ordIter=this.getSelectFormer4Order().getSelectedList().iterator();
		String kOrderSplit="OrderSplitList", kOrderYes="OrderYes", kOrderNo="OrderNo";
		for (; ordIter.hasNext() && purIter.hasNext(); ) {
			OrderDetail purchase=purIter.next();
			OrderDetail order=ordIter.next(), sorder=order.getVoparam(OrderDetail.class);
			if (sorder==null) {
				sorder=order.getSnapShot();
				order.setVoparam(sorder);
			}
			orderSplitList.add(order);
			purSplitList.add(purchase);
			LinkedHashSet<OrderDetail> orderSplits = sorder.getVoparam(kOrderSplit);
			LinkedHashSet<Double> orderYes=null;
			if ("要占用的1订单分成了哪几个".length()>0) {
				if (orderSplits==null) {
					orderSplits = new LinkedHashSet<OrderDetail>();
					orderYes = new LinkedHashSet<Double>();
					sorder.getVoParamMap().put(kOrderSplit, orderSplits);
					sorder.getVoParamMap().put(kOrderYes, orderYes);
				} else
					orderYes = sorder.getVoparam(kOrderYes);
			}
			if (order.getAmount() > purchase.getAmount()) {
				String nwCurSplitMonthnum = new OrderTicketLogic().getSplitMonthnum(order.getMonthnum());
				OrderDetail nwRemainOrder = new OrderTicketLogic().genCloneOrder(order);
				nwRemainOrder.setAmount(order.getAmount() - purchase.getAmount());
				nwRemainOrder.setVoparam(sorder);
				order.setMonthnum(nwCurSplitMonthnum);
				order.setAmount(purchase.getAmount());
				orderYes.add(purchase.getAmount());
				// 剩余无库存订单加入待处理队列
				List<OrderDetail> list = new ArrayList<OrderDetail>(1);
				list.add(nwRemainOrder);
				ordIter = new JoinedIterator(list.iterator(), ordIter);
			} else if (order.getAmount() < purchase.getAmount()) {
				String nwRemainSplitMonthnum = new OrderTicketLogic().getSplitMonthnum(purchase.getMonthnum());
				OrderDetail nwRemainPur = new PurchaseTicketLogic().genClonePurchase(purchase);
				nwRemainPur.setAmount(purchase.getAmount() - order.getAmount());
				purchase.setAmount(order.getAmount());
				nwRemainPur.setMonthnum(nwRemainSplitMonthnum);
				orderYes.add(order.getAmount());
				// 剩余库存加入供用库存队列
				List<OrderDetail> list = new ArrayList<OrderDetail>(1);
				list.add(nwRemainPur);
				purIter = new JoinedIterator(list.iterator(), purIter);
			} else {
				// =
				orderYes.add(order.getAmount());
			}
			order.getVoParamMap().put("FromStore", purchase);
			orderSplits.add(order);
		}
		for (OrderDetail ord=null; ordIter.hasNext(); ) {
			ord = ordIter.next();
			if (ord.getId()==0)
				orderSplitList.add(ord);
			else
				break;
		}
		for (OrderDetail cur=null, next=null; purIter.hasNext(); cur=next, next=null) {
			next = purIter.next();
			purSplitList.add(next);
			if (cur==null || next==null)
				continue;
			if (cur.getId()!=next.getId())
				break;
		}
		this.detailList = new ArrayList<OrderDetail>(orderSplitList);
		this.getPurchaseList().clear();
		this.getPurchaseList().addAll(purSplitList);
		if ("Order原明细".length()>0) {
			LinkedHashSet<String> numbers=new LinkedHashSet<String>(), arranges=new LinkedHashSet<String>();
			double amount = 0;
			Iterator<OrderDetail> iter = orderSplitList.iterator();
			for (OrderDetail d=null,sd=null; iter.hasNext();) {
				d = iter.next();
				if (d.getId()==0)
					continue;
				sd = d.getSnapShot();
				numbers.add(d.getOrderTicket().getNumber());
				if (d.getVoparam(ArrangeT.class).getArrangeName()!=null)
					arranges.add(d.getVoparam(ArrangeT.class).getArrangeName());
				amount += sd.getAmount();
			}
			this.setAttr("SplitOrderNumbers", numbers.toArray(new String[0]));
			this.setAttr("SplitOrderArranges", arranges.toArray(new String[0]));
			this.setAttr("SplitOrderAmount", amount);
		}
		if ("Order原明细有库存数、无库存数".length()>0) {
			for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
				OrderDetail sorder = order.getVoparam(OrderDetail.class);
				LinkedHashSet<OrderDetail> orderSplit = sorder.getVoparam(kOrderSplit);
				LinkedHashSet<Double> orderYes = sorder.getVoparam(kOrderYes);
				if (orderSplit!=null) {
					double dyes=0d, dno=0d;
					for (Double d: orderYes) {
						dyes += d;
					}
					dno = sorder.getAmount() - dyes;
					sorder.getVoParamMap().put(kOrderYes, dyes);
					sorder.getVoParamMap().put(kOrderNo, dno);
					Assert.assertTrue("Order拆分，有库存的数量，无库存的数量", orderSplit.size()>=1 && orderYes!=null && dyes<=sorder.getAmount() && dyes+dno==sorder.getAmount());
				}
			}
		}
		if ("Purchase原明细".length()>0) {
			LinkedHashSet<String> numbers=new LinkedHashSet<String>(), arranges=new LinkedHashSet<String>();
			double amount = 0;
			Iterator<OrderDetail> iter = purSplitList.iterator();
			for (OrderDetail d=null,sd=null; iter.hasNext();) {
				d = iter.next();
				if (d.getId()==0)
					continue;
				sd = d.getSnapShot();
				numbers.add(d.getOrderTicket().getNumber());
				if (d.getVoparam(ArrangeT.class).getArrangeName()!=null)
					arranges.add(d.getVoparam(ArrangeT.class).getArrangeName());
				amount += sd.getAmount();
			}
			this.setAttr("SplitPurchaseNumbers", numbers.toArray(new String[0]));
			this.setAttr("SplitPurchaseArranges", arranges.toArray(new String[0]));
			this.setAttr("SplitPurchaseAmount", amount);
		}
	}
	
	private void setExchangeOrder4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.detailList);
		for (OrderDetail ord: viewData.getTicketDetails()) {
			ord.getVoParamMap().put("Monthnum", ord.getMonthnum());
		}
	}
	
	private void setExchangePurchase4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getPurchaseList());
		for (OrderDetail ord: viewData.getTicketDetails()) {
			ord.getVoParamMap().put("Monthnum", ord.getMonthnum());
		}
	}
	
	private void setExchangeStore4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>(this.getPurchaseList());
		for (OrderDetail order: this.detailList) {
			OrderDetail sorder = order.getSnapShot();
			if (sorder.getStPurchase()>0)
				list.add(order);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setExchangeBCgotAD4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		OrderDetail nwOrderArrange = null;
		PropertyChoosableLogic.TicketDetail logic = this.getArrangeChoosableLogic();
		for (OrderDetail order: this.detailList) {
			OrderDetail sorder=order.getSnapShot(), store=(OrderDetail)order.getVoParamMap().get("FromStore");
			if (store!=null) {
				OrderDetail sStore=store.getSnapShot();
				new OrderTicketLogic().setOrderTicket(sorder, store, (String)order.getVoparam("Monthnum"));
				list.add(store);
			}
		}
		viewData.setTicketDetails(list);
	}
	
	private void setExchangeDrop4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.detailList) {
			OrderDetail sorder=order.getSnapShot(), store=(OrderDetail)order.getVoParamMap().get("FromStore");
			if ((store!=null && sorder.getStPurchase()==0)==false)
				continue;
			OrderDetail sStore = store.getSnapShot();
			new OrderTicketLogic().setOrderTicket(sStore, order, (String)store.getVoparam("Monthnum"));
			list.add(order);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setExchangeRearrange4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.detailList) {
			OrderDetail sorder=order.getSnapShot(), store=(OrderDetail)order.getVoParamMap().get("FromStore");
			if ((store!=null && sorder.getStPurchase()==0)==false)
				continue;
			OrderDetail sStore = store.getSnapShot();
			new OrderTicketLogic().setOrderTicket(sStore, order, (String)store.getVoparam("Monthnum"));
			new PurchaseTicketLogic().setPurchaseTicket(new OrderDetail(), order);
			list.add(order);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setExchangeNewRearrange4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.detailList) {
			OrderDetail sorder=order.getSnapShot(), store=(OrderDetail)order.getVoParamMap().get("FromStore");
			if ((store!=null && sorder.getStPurchase()>0)==false)
				continue;
			OrderDetail sStore=store.getSnapShot(), nwRearrange=new OrderTicketLogic().genCloneOrder(sStore);
			new OrderTicketLogic().setOrderTicket(sStore, nwRearrange, new OrderTicketLogic().getSplitMonthnum((String)store.getVoparam("Monthnum")));
			new PurchaseTicketLogic().setPurchaseTicket(new OrderDetail(), nwRearrange);
			order.getVoParamMap().put("NewRearrange", nwRearrange);
			list.add(nwRearrange);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setExchangeBackup4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.detailList) {
			OrderDetail sorder = order.getSnapShot();
			OrderDetail store = (OrderDetail)order.getVoParamMap().get("FromStore");
			if ((store!=null && sorder.getStPurchase()>0)==false)
				continue;
			OrderDetail sStore = store.getSnapShot();
			new OrderTicketLogic().setOrderTicket(sStore, order, (String)store.getVoparam("Monthnum"));
			list.add(order);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setExchangeACgotBD4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> list = new ArrayList<OrderDetail>();
		PropertyChoosableLogic.TicketDetail logic = this.getArrangeChoosableLogic();
		for (OrderDetail order: this.detailList) {
			OrderDetail sorder = order.getSnapShot();
			OrderDetail store = (OrderDetail)order.getVoParamMap().get("FromStore");
			if ((store!=null)==false)
				continue;
			OrderDetail sStore = store.getSnapShot();
			new OrderTicketLogic().setOrderTicket(sStore, order, (String)store.getVoparam("Monthnum"));
			list.add(order);
		}
		viewData.setTicketDetails(list);
	}
	
	private void setTransPurchase4Service(ViewData<OrderDetail> viewData) {
		this.getOrderDetail().getArrangeTicket().setNumber(null);
		this.getOrderDetail().getArrangeTicket().genSerialNumber();
		PropertyChoosableLogic.TicketDetail logic = this.getArrangeChoosableLogic();
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail sorder = order.getSnapShot();
			OrderDetail nord = order;
			if (sorder.getStPurchase()>0) {
				nord = new OrderTicketLogic().genCloneOrder(sorder);
				new PurchaseTicketLogic().setPurchaseTicket(new OrderDetail(), nord);
				nord.setMonthnum(new OrderTicketLogic().genMonthnum());
			}
			order.getVoParamMap().put("NewTransTo", nord);
			logic.fromTrunk(logic.getTicketBuilder(), nord.getArrangeTicket(), getOrderDetail().getArrangeTicket());
			ordList.add(nord);
		}
		viewData.setTicketDetails(ordList);
	}
	
	private void setTransNormal4Service(ViewData<OrderDetail> viewData) {
		this.getOrderDetail().getArrangeTicket().setNumber(null);
		this.getOrderDetail().getArrangeTicket().genSerialNumber();
		this.getOrderDetail().getArrangeTicket().setArrangeType(new ArrangeTypeLogic().getNormal());
		PropertyChoosableLogic.TicketDetail logic = this.getArrangeChoosableLogic();
		List<OrderDetail> ordList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail sorder = order.getSnapShot();
			OrderDetail nord = order;
			if (sorder.getStPurchase()>0) {
				nord = new OrderTicketLogic().genCloneOrder(sorder);
				new PurchaseTicketLogic().setPurchaseTicket(new OrderDetail(), nord);
				nord.setMonthnum(new OrderTicketLogic().genMonthnum());
			}
			order.getVoParamMap().put("NewTransTo", nord);
			logic.fromTrunk(logic.getTicketBuilder(), nord.getArrangeTicket(), this.getOrderDetail().getArrangeTicket());
			ordList.add(nord);
		}
		viewData.setTicketDetails(ordList);
		this.getOrderDetail().getArrangeTicket().setArrangeType(null);
	}
	
	private void setTransBackCancel4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		if ("采购取消申请".length()>0) {
			List<OrderDetail> cancelList = new ArrayList<OrderDetail>();
			OrderDetail first=null;
			for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
				OrderDetail sorder = order.getSnapShot();
				if ((sorder.getStPurchase()>0 && sorder.getReceiptId()==0)==false)
					continue;
				if (cancelList.size()==0) {
					first = order;
				}
				cancelList.add(order);
			}
			viewData.getTicketDetails().addAll(cancelList);
		}
		if ("采购退货申请".length()>0) {
			List<OrderDetail> cancelList = new ArrayList<OrderDetail>();
			OrderDetail first=null;
			for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
				OrderDetail sorder = order.getSnapShot();
				if ((sorder.getStPurchase()>0 && sorder.getReceiptId()>0)==false)
					continue;
				if (cancelList.size()==0) {
					first = order;
					first.setReturnTicket(new ReturnTicket());
					first.getReturnTicket().genSerialNumber();
					first.getReturnTicket().setRemark("调整订单安排，原采购在库申请取消的退货申请");
				}
				order.setReturnTicket(first.getReturnTicket());
				cancelList.add(order);
			}
			PropertyChoosableLogic.TicketDetail<PurchaseReturnForm, ReturnTicket, ReturnT> logic = new ReturnTicketLogic().getPurchaseChoosableLogic();
			for (OrderDetail cancel: cancelList) {
				logic.fromTrunk(logic.getTicketBuilder(), cancel.getReturnTicket(), first.getReturnTicket());
				cancel.setTReturn( new ReturnTicketLogic().getPurchaseChoosableLogic().toTrunk(cancel.getReturnTicket()) );
			}
			viewData.getTicketDetails().addAll(cancelList);
		}
	}
	
	private void setTransBackup4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> backList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail sorder = order.getSnapShot();
			if (sorder.getStPurchase()>0) {
				backList.add(order);
			}
		}
		viewData.setTicketDetails(backList);
	}
	
	private void setOrderPurchaseCount4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
		viewData.getTicketDetails().addAll(this.getSelectFormer4Purchase().getSelectedList());
	}

	private ActionService4LinkListener getSubCompanyLink() {
		ActionService4LinkListener listener = new ActionService4LinkListener();
		Seller fromSeller = new Seller4lLogic().getSellerById(this.getOrderDetail().getSubCompany().getFromSellerId());
		listener.getOnceAttributes().put("seller", fromSeller);
		User user = TicketPropertyUtil.copyProperties(this.getUser(), new User());
		user.setUserName(new StringBuffer().append(this.getSeller().getName()).append(".").append(this.getUserName()).toString());
		listener.getOnceAttributes().put("user", user);
		this.setAttr(listener);
		return listener;
	}
	
	private void setSubCompanyLink4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> linkList = new ArrayList<OrderDetail>();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			OrderDetail link = new OrderTicketLogic().genCloneOrder(order);
			link.setMonthnum(new OrderTicketLogic().getLinkMonthnum(order.getMonthnum()));
			order.setMonthnum(link.getMonthnum());
			link.setSubCompany(new SubCompany());
			linkList.add(link);
		}
		viewData.setTicketDetails(linkList);
	}
	
	private void setArrangeSubCompany4Service(ViewData<OrderDetail> viewData) {
		this.getOrderDetail().getArrangeTicket().setNumber(null);
		this.getOrderDetail().getArrangeTicket().genSerialNumber();
		this.getOrderDetail().getArrangeTicket().setArrangeType(new ArrangeTypeLogic().getNormal());
		PropertyChoosableLogic.TicketDetail logic = this.getArrangeChoosableLogic();
		for (OrderDetail d: this.getSelectFormer4Order().getSelectedList()) {
			logic.fromTrunk(logic.getTicketBuilder(), d.getArrangeTicket(), this.getOrderDetail().getArrangeTicket());
			d.getArrangeTicket().setArrangeType(new ArrangeTypeLogic().getUseSubCompany());
			d.setMonthnum(new OrderTicketLogic().getSplitMonthnum(d.getMonthnum()));
		}
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
		this.getOrderDetail().getArrangeTicket().setArrangeType(null);
	}

	public boolean getTransCancel() {
		return "取消".equals(getOrderDetail().getArrangeTicket().getCancelType());
	}
	
	public boolean getTransBack() {
		return "转备料".equals(getOrderDetail().getArrangeTicket().getCancelType());
	}
	
	public boolean getTransNone() {
		String cancelType = getOrderDetail().getArrangeTicket().getCancelType();
		return cancelType==null || "无".equals(cancelType);
	}
	
	private String getFromArrangeType() {
		StringBuffer sb = new StringBuffer();
		sb.append(getDomain().getArrangeTicket().getArrangeType()).append(getDomain().getCommodity().getSupplyType());
		return sb.toString();
	}
	
	public String getNoteCancelType() {
		String cancelType = getNoteFormer4Order().getNoteString(getDomain(), new StringBuffer("cancelType"));
		return cancelType;
	}
	
	private TicketUser genTicketUser() {
		TicketUser user = new TicketUser();
		user.setUser(this.getUserName());
		user.setDate(new Date());
		return user;
	}
	
	private ArrangeTicket getArrangeTicket() {
		ArrangeTicket ticket = this.getAttr(ArrangeTicket.class);
		if (ticket==null) {
			ticket = new ArrangeTicket();
			this.setAttr(ticket);
		}
		return ticket;
	}
	
	public OrderDetail getOrderDetail() {
		OrderDetail detail = this.getAttr(OrderDetail.class);
		if (detail==null) {
			detail = new OrderDetail();
			this.setAttr(detail);
		}
		return detail;
	}
	
	protected void setOrderDetail(OrderDetail detail) {
		this.setAttr(detail);
	}
	
	private void setArrangeListFilterClear() {
		SqlListBuilder sqlBuilder = (SqlListBuilder)EntityClass.loadViewBuilder(this.getClass(), "ArrangeList.tabArrange").getFieldBuildersDeep(SqlListBuilder.class).get(0);
		this.getSearchSetting(sqlBuilder);
		sqlBuilder = (SqlListBuilder)EntityClass.loadViewBuilder(this.getClass(), "ArrangeList.selfForm.tabUStore").getFieldBuildersDeep(SqlListBuilder.class).get(0);
		this.getSearchSetting(sqlBuilder);
		sqlBuilder = (SqlListBuilder)EntityClass.loadViewBuilder(this.getClass(), "ArrangeList.selfForm.tabUPassage").getFieldBuildersDeep(SqlListBuilder.class).get(0);
		this.getSearchSetting(sqlBuilder);
	}
	
	public SelectTicketFormer4Sql<ArrangeTicketForm, OrderDetail> getSelectFormer4Order() {
		String k="SelectFormer4Order";
		SelectTicketFormer4Sql<ArrangeTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<ArrangeTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public SelectTicketFormer4Sql<ArrangeTicketForm, OrderDetail> getSelectFormer4Purchase() {
		String k = "SelectFormer4Purchase";
		SelectTicketFormer4Sql<ArrangeTicketForm, OrderDetail> form = getAttr(k);
		if (form == null) {
			form = new SelectTicketFormer4Sql<ArrangeTicketForm, OrderDetail>(this);
			this.setAttr(k, form);
		}
		return form;
	}
	
	public NoteAccessorFormer<OrderDetail> getNoteFormer4Order() {
		String k="NoteFormer4Order";
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
	
	private String getColumnOrder(LinkedHashMap<String, Object> params) {
		return new OrderTicketForm().getColumnOrder(params);
	}
	private String getColumnPurchase(LinkedHashMap<String, Object> params) {
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iter=params.keySet().iterator(); iter.hasNext();) {
			String purchase = (String)params.get(iter.next());
			int stPurchase = (Integer)params.get(iter.next());
			if (stPurchase>0)
				sb.append(purchase);
		}
		return sb.toString();
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
	private void getSubCompanySearchName(TextField input) {
		String name = input.getText();
		this.setIsDialogOpen(false);
		if(!StringUtils.isEmpty(name)) {
			if (this.getSqlListSearch(input.searchParentByClass(Window.class), this, "SubCompanyQuery", 1|2, "name", name)==false)
				this.setIsDialogOpen(true);
		}
	}
	private SubCompanyForm getSubCompanyForm() {
		return new SubCompanyForm();
	}
	private void setSubCompanySelect(List<SubCompany> subList) {
		SubCompany sub = subList.size()==0? new SubCompany(): subList.get(0);
		this.getOrderDetail().setSubCompany(sub);
	}
	
	public ActionService4LinkListener getActionService4LinkListener() {
		return this.getAttr(ActionService4LinkListener.class);
	}
	
	public PropertyChoosableLogic.TicketDetail<ArrangeTicketForm, ArrangeTicket, ArrangeT> getArrangeChoosableLogic() {
		return new ArrangeTicketLogic().getPropertyChoosableLogic("采购");
	}
}
