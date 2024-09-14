package com.haoyong.sales.sale.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.mily.attributes.AttributeName;
import net.sf.mily.attributes.ClientEventName;
import net.sf.mily.attributes.StyleName;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DoubleType;
import net.sf.mily.types.TimeType;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.BlockGrid.BlockCell;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.Text;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.enumeration.BlockGridMode;
import net.sf.mily.ui.enumeration.EventListenerType;
import net.sf.mily.ui.event.ActionListener;
import net.sf.mily.ui.event.EventObject;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.Field;
import net.sf.mily.webObject.ListView;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.hibernate.util.JoinedIterator;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.SupplierT;
import com.haoyong.sales.base.domain.SupplyType;
import com.haoyong.sales.base.form.BOMForm;
import com.haoyong.sales.base.form.ChooseFormer;
import com.haoyong.sales.base.form.SupplyTypeForm;
import com.haoyong.sales.base.logic.BomTicketLogic;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.domain.AbstractCommodityItem;
import com.haoyong.sales.common.domain.State;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.ArrangeT;
import com.haoyong.sales.sale.domain.ArrangeTicket;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.BomTicket;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.PurchaseT;
import com.haoyong.sales.sale.domain.PurchaseTicket;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.logic.ArrangeTicketLogic;
import com.haoyong.sales.sale.logic.ArrangeTypeLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.test.base.ClientTest;

public class PPurchaseTicketForm extends PurchaseTicketForm {
	
	private void canProduct(List<List<Object>> valiRows) {
		// commName,uneditable,stateId,arrangeId,Complete
		StringBuffer sb=new StringBuffer(), sitem=null;
		if (valiRows.size()>1)
			sb.append("请只选择一个明细，");
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (!(Integer.parseInt(row.get(2)+"")==30 && Integer.parseInt(row.get(3)+"")==30))
				sitem.append(row.get(1)).append("\t");
			if ("齐".equals(row.get(4))==false)
				sitem.append("子物料不齐，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem);
		}
		if (sb.length() > 0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void canBomArrange(List<List<Object>> valiRows) {
		// commName,uneditable,monthnum1,BDetails
		StringBuffer sb=new StringBuffer(), sitem=null;
		for (List<Object> row: valiRows) {
			sitem = new StringBuffer();
			if (row.get(1)!=null)
				sitem.append(row.get(1)).append(",");
			if (this.getBomDetails((String)row.get(2)).size()>0) {
			} else if (row.get(2)!=null) {
			} else
				sitem.append("请先配置BOM，");
			if (sitem.length()>0)
				sb.append(row.get(0)).append(sitem).append("\t");
		}
		if (sb.length() > 0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	private void clear() {
		this.getOrderDetail().setAmount(0);
		this.getMonthnumLinkList().clear();
	}
	
	public void prepareProduct() {
		List<OrderDetail> purchaseList = new ArrayList<OrderDetail>();
		CommodityLogic commLogic = new CommodityLogic();
		OrderDetail order = this.getSelectFormer4Order().getFirst();
		OrderDetail purchase = new OrderDetail();
		commLogic.fromTrunk(purchase.getCommodity(), order.getCommodity());
		purchase.setAmount(order.getAmount());
		purchase.setMonthnum(order.getMonthnum());
		purchase.getArrangeTicket().setArrangeType(order.getArrangeTicket().getArrangeType());
		purchase.setVoparam(order);
		purchase.getSupplier().setName(order.getPurchaseTicket().getAgent());
		List<OrderDetail> materialList = order.getVoparam(ArrayList.class);
		for (Iterator<OrderDetail> iter=materialList.iterator(); iter.hasNext();) {
			if (iter.next()==null)
				iter.remove();
		}
		this.getOrderList().clear();
		this.getOrderList().addAll(materialList);
		purchaseList.add(purchase);
		this.setDetailList(purchaseList);
		this.setDomain(purchase);
	}
	
	public void prepareBom() {
		OrderDetail order = getSelectFormer4Order().getFirst();
		Commodity comm = order.getVoparam(Commodity.class);
		List<BomDetail> olist=this.getOnPageLoadedListener().getBomDetails(order.getMonthnum()), clist=comm==null? null: comm.getBomDetails(), list=null;
		order.getVoParamMap().put("BomDetails", olist);
		if (olist.size()>0)
			list = olist;
		else if (clist!=null && clist.size()>0)
			list = clist;
		else
			list = new ArrayList<BomDetail>();
		BOMForm bomForm = BOMForm.getForm4List(list, this);
		bomForm.getBomCopy().clear();
		bomForm.getBomCopy().addAll(this.getBomCopy());
		this.setAttr(bomForm);
	}
	
	public void validateSubtract() {
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.getDetailList()) {
			if (!(detail.getAmount()>0 && ((OrderDetail)detail.getSnapShot()).getAmount() > detail.getAmount()))
				sb.append("请合理填写缩减生产数量，");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateProduct() {
		super.validatePurchase();
		StringBuffer sb = new StringBuffer();
		for (OrderDetail detail: this.getDetailList()) {
			if (detail.getAmount()>0 && ((OrderDetail)detail.getSnapShot()).getAmount() > detail.getAmount())
				sb.append("请先处理缩减生产数量，");
		}
		if (sb.length()>0)
			throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void validateSplitAmount() {
		StringBuffer sb=new StringBuffer();
		if (this.getSelectFormer4Order().getSelectedList().isEmpty())
			sb.append("请选择要拆分的订单，");
		for (OrderDetail detail: this.getSelectFormer4Order().getSelectedList()) {
			if ( !(0<getOrderDetail().getAmount() && getOrderDetail().getAmount()<detail.getAmount()) )
				sb.append("拆分数量应小于订单数量，");
		}
		if (sb.length()>0)		throw new LogicException(2, sb.deleteCharAt(sb.length()-1).toString());
	}
	
	public void setBomOut4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getOrderList());
	}
	
	public void setSubtract4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		for (OrderDetail p: this.getDetailList()) {
			OrderDetail d = p;
			d.setAmount(p.getAmount());
			orderList.add(d);
		}
		viewData.setTicketDetails(orderList);
	}
	
	public void setStoreEffect4Service(ViewData<AbstractCommodityItem> viewData) {
		viewData.setTicketDetails(new ArrayList());
		viewData.getTicketDetails().addAll(this.getDetailList());
		viewData.getTicketDetails().addAll(this.getOrderList());
	}
	
	private List<String> getParam4Enough(List<Object> columns) {
		// agent,commNumber,commName,commOther
		List<String> paramList = new ArrayList<String>();
		int ci=0;
		String agent=(String)columns.get(ci++), commNumber=(String)columns.get(ci++), commName=(String)columns.get(ci++), commOther=(String)columns.get(ci++), s=null;
		StringBuffer sb = new StringBuffer();
		if ((s=commNumber)!=null)
			sb.append(s).append(",");
		if ((s=commName)!=null)
			sb.append(s).append(",");
		if ((s=commOther)!=null)
			sb.append(s).append(",");
		paramList.add(agent);
		paramList.add(sb.length()>0? sb.deleteCharAt(sb.length()-1).toString(): null);
		return paramList;
	}
	private List<String> getParam4Keep(List<Object> columns) {
		// supplierName,commNumber,commName,commOther
		List<String> paramList = new ArrayList<String>();
		int ci=0;
		String supplierName=(String)columns.get(ci++), commNumber=(String)columns.get(ci++), commName=(String)columns.get(ci++), commOther=(String)columns.get(ci++), s=null;
		StringBuffer sb = new StringBuffer();
		if ((s=commNumber)!=null)
			sb.append(s).append(",");
		if ((s=commName)!=null)
			sb.append(s).append(",");
		if ((s=commOther)!=null)
			sb.append(s).append(",");
		paramList.add(supplierName);
		paramList.add(sb.length()>0? sb.deleteCharAt(sb.length()-1).toString(): null);
		return paramList;
	}
	private HashMap<String, String> getParam4Supply() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("supplyType", "'生产'");
		return map;
	}
	private HashMap<String, String> getParam4Commodity() {
		String commNumber = this.getAttr(BomDetail.class).getCommodity().getCommNumber();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("commNumber", new StringBuffer().append("'").append(commNumber).append("'").toString());
		return map;
	}
	
	protected PropertyChoosableLogic.TicketDetail<ArrangeTicketForm, ArrangeTicket, ArrangeT> getArrangeChoosableLogic() {
		return new ArrangeTicketLogic().getPropertyChoosableLogic("生产");
	}
	
	private void setBDetailsLink(Hyperlink link, String monthnum, OrderDetail row, int rowi) {
		if (rowi==1)
			this.getMonthnumLinkList().clear();
		this.getMonthnumLinkList().add(link);
		link.setText((String)monthnum);
		if (rowi==1) {
			link.getEventListenerList().addActionListener(this.getOnPageLoadedListener());
			link.addAttribute(ClientEventName.InitScript0, "L_action(this);");
		}
	}
	
	private void setBDetailsLinkMaterial(Hyperlink link, Object monthnum, ArrayList<Object> row, int rowi) {
		if (rowi==1)
			this.getMonthnumLinkList().clear();
		this.getMonthnumLinkList().add(link);
		link.setText((String)monthnum);
		if (rowi==1) {
			link.getEventListenerList().addActionListener(this.getOnPageLoadedListener());
			link.addAttribute(ClientEventName.InitScript0, "L_action(this);");
		}
	}
	
	private void setBDetailsLinkFold(Hyperlink link, Object monthnum, ArrayList<Object> row, int rowi) {
		if (rowi==1)
			this.getMonthnumLinkList().clear();
		this.getMonthnumLinkList().add(link);
		link.setText((String)monthnum);
		if (rowi==1) {
			link.getEventListenerList().addActionListener(this.getOnPageLoadedListener());
			link.addAttribute(ClientEventName.InitScript0, "L_action(this);");
		}
	}
	
	private void setBDetailsLinkRecord(Hyperlink link, Object monthnum, ArrayList<Object> row, int rowi) {
		if (rowi==1)
			this.getMonthnumLinkList().clear();
		this.getMonthnumLinkList().add(link);
		link.setText((String)monthnum);
		if (rowi==1) {
			link.getEventListenerList().addActionListener(this.getOnPageLoadedListener());
			link.addAttribute(ClientEventName.InitScript0, "L_action(this);");
		}
	}
	
	private void setMaterialAdd() {
		for (OrderDetail detail: this.getSelectEdit4Order().getSelectedList()) {
			OrderDetail material = new OrderTicketLogic().genCloneOrder(detail);
			material.getArrangeTicket().setArrangeType(new ArrangeTypeLogic().getNormal());
			material.setMonthnum(new OrderTicketLogic().getBomMonthnum(this.getDomain().getMonthnum()));
			material.setAmount(0.0);
			this.getOrderList().add(this.getOrderList().indexOf(detail)+1, material);
		}
	}
	
	private PurchaseTicketForm getSuperForm() {
		String k = "PurchaseTicketForm";
		PurchaseTicketForm form = this.getAttr(k);
		if (form==null) {
			form = new PPurchaseTicketForm();
			this.setAttr(k, form);
		}
		return form;
	}
	
	protected OnPageLoadedListener getOnPageLoadedListener() {
		String k = "OnPageLoadedListener";
		OnPageLoadedListener listener = this.getAttr(k);
		if (listener == null) {
			listener = new OnPageLoadedListener(this);
			this.setAttr(k, listener);
		}
		return listener;
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
	
	private void setPurchaseEffect4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(new ArrayList<OrderDetail>());
		viewData.getTicketDetails().addAll(this.getDetailList());
		for (OrderDetail detail: this.getDetailList()) {
			List blist = this.getBomDetails(detail.getMonthnum());
			viewData.getTicketDetails().addAll(blist);
		}
	}
	
	private void setSplitNew4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail d = new OrderTicketLogic().genCloneOrder(detail);
			d.setMonthnum(new OrderTicketLogic().getSplitMonthnum(detail.getMonthnum()));
			d.setAmount(this.getOrderDetail().getAmount());
			detail.getVoParamMap().put("NewPurchase", d);
			orderList.add(d);
		}
		viewData.setTicketDetails(orderList);
	}
	
	private void setSplitRemain4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
			detail.setAmount(detail.getAmount() - this.getOrderDetail().getAmount());
			orderList.add(detail);
		}
		viewData.setTicketDetails(orderList);
	}
	
	private void setSplitNew4BomService(ViewData<BomDetail> viewData) {
		List<BomDetail> bomList = new ArrayList<BomDetail>();
		BomTicketLogic bomLogic = new BomTicketLogic();
		for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail nwOrder = (OrderDetail)detail.getVoParamMap().get("NewPurchase");
			List<BomDetail> sourceList = this.getBomDetails(detail.getMonthnum());
			List<BomDetail> nwsourceList = new ArrayList<BomDetail>();
			for (BomDetail b: sourceList) {
				BomDetail nwb = TicketPropertyUtil.copyProperties(b, new BomDetail());
				nwb.setMonthnum(nwOrder.getMonthnum());
				nwb.setAmount(nwOrder.getAmount() * b.getBomTicket().getAunit());
				if (b.getBomTicket().getGotAmount() > 0) {
					double got=b.getBomTicket().getGotAmount()>=nwb.getAmount()? nwb.getAmount(): b.getBomTicket().getGotAmount();
					nwb.getBomTicket().setGotAmount(got);
					b.getBomTicket().setGotAmount(b.getBomTicket().getGotAmount() - got);
				}
				if (b.getBomTicket().getOccupyAmount() > 0) {
					double occupy=b.getBomTicket().getOccupyAmount()>=nwb.getAmount()? nwb.getAmount(): b.getBomTicket().getOccupyAmount();
					nwb.getBomTicket().setOccupyAmount(occupy);
					b.getBomTicket().setOccupyAmount(b.getBomTicket().getOccupyAmount() - occupy);
				}
				if (b.getBomTicket().getNotAmount() > 0) {
					if (b.getBomTicket().getAunit()*((OrderDetail)detail.getSnapShot()).getAmount()- b.getBomTicket().getNotAmount() >= nwOrder.getAmount()*b.getBomTicket().getAunit())
						nwb.getBomTicket().setNotAmount(0);
					else
						nwb.getBomTicket().setNotAmount(nwOrder.getAmount()*b.getBomTicket().getAunit() - (b.getBomTicket().getAunit()*((OrderDetail)detail.getSnapShot()).getAmount()- b.getBomTicket().getNotAmount()));
					b.getBomTicket().setNotAmount(b.getBomTicket().getNotAmount() - nwb.getBomTicket().getNotAmount());
				}
				b.setAmount(detail.getAmount() * b.getBomTicket().getAunit());
				nwsourceList.add(nwb);
			}
			bomList.addAll(nwsourceList);
			if ("物料齐".length()>0) {
				this.setCalcGotAmount(nwOrder, bomLogic.getChildrenRoot(nwsourceList), nwsourceList);
				this.setCalcGotAmount(detail, bomLogic.getChildrenRoot(sourceList), sourceList);
			}
		}
		viewData.setTicketDetails(bomList);
	}
	
	private void setSplitRemain4BomService(ViewData<BomDetail> viewData) {
		List<BomDetail> bomList = new ArrayList<BomDetail>();
		for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
			bomList.addAll(this.getBomDetails(detail.getMonthnum()));
		}
		viewData.setTicketDetails(bomList);
	}
	
	private void setSplitOrder4Service(ViewData<OrderDetail> viewData) {
		List<OrderDetail> orderList = new ArrayList<OrderDetail>();
		for (OrderDetail detail: this.getSelectFormer4Purchase().getSelectedList()) {
			OrderDetail nwOrder = (OrderDetail)detail.getVoParamMap().get("NewPurchase");
			orderList.add(nwOrder);
		}
		orderList.addAll(this.getSelectFormer4Order().getSelectedList());
		viewData.setTicketDetails(orderList);
	}
	
	private void setCalc4Service(ViewData<BomDetail> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Bom().getSelectedList());
	}
	
	private void setCalcOrder4Service(ViewData<OrderDetail> viewData) {
		viewData.setTicketDetails(this.getSelectFormer4Order().getSelectedList());
	}
	
	private void setBom4Service(ViewData<BomDetail> viewData) {
		OrderDetail order = super.getSelectFormer4Order().getFirst();
		List<BomDetail> bomList = getBomForm().getDetailList();
		viewData.setTicketDetails(bomList);
		int sn=0;
		for (BomDetail b: this.getBomForm().getDetailList()) {
			b.setMonthnum(order.getMonthnum());
			b.setSubCompany(order.getSubCompany());
			b.setClient(order.getClient());
			b.setOrderTicket(order.getOrderTicket());
			b.setSn(++sn);
			b.setAmount(b.getBomTicket().getAunit() * order.getAmount());
		}
		List<BomDetail> bomDeletes = new ArrayList<BomDetail>((List)order.getVoParamMap().get("BomDetails"));
		bomDeletes.removeAll(bomList);
		this.setAttr("BomDeleteList", bomDeletes);
	}
	
	private void setBomCreate4Service(ViewData<BomDetail> viewData) {
		List<BomDetail> bomList = getBomForm().getDetailList();
		viewData.setTicketDetails(bomList);
	}
	
	private void setBomOccupy4Service(ViewData<BomDetail> viewData) {
		viewData.setTicketDetails(this.getAttr(BomDetail.class));
	}
	
	private void setBomRecord4Service(ViewData<BomDetail> viewData) {
		LinkedHashSet<BomDetail> blist = new LinkedHashSet<BomDetail>();
		List<Long> brepeat = new ArrayList<Long>();
		for (TextField[] row: this.getTextFieldMatrix()) {
			TextField f = null;
			for (int ci=0,csize=row.length; ci<csize && f==null; ci++, f=row[ci-1]==null? null: row[ci-1]);
			BomDetail b = this.getBomDetailRecord(f);
			if (b == null)
				continue;
			if (blist.add(b)==false)
				brepeat.add(b.getId());
		}
		Assert.assertTrue(new StringBuffer("没有重复保存的记录").append(brepeat).toString(), brepeat.size()==0);
		viewData.setTicketDetails(new ArrayList<BomDetail>(blist));
		this.getOrderDetail().getOrderTicket().setRemark(new StringBuffer().append("最近一次保存录入时间").append(new TimeType().format(new Date())).toString());
		if ("刷新BOM界面".length()>0) {
			Set<String> monthnums = new HashSet<String>();
			List<OrderDetail> orderList = new ArrayList<OrderDetail>();
			for (BomDetail b: blist) {
				if (monthnums.add(b.getMonthnum())==true) {
					OrderDetail order = b.getVoparam(OrderDetail.class);
					Assert.assertTrue("BOM上有订单", order!=null && order.getId()>0);
					Hyperlink link = order.getVoparam(Hyperlink.class);
					List<BomDetail> dlist = this.getBomDetails(order.getMonthnum());
					Assert.assertTrue("订单上有Hyperlink，BomDetailList", link!=null && dlist!=null);
					this.getOnPageLoadedListener().setBDetailsLinkRecord(link, dlist);
					orderList.add(order);
				}
			}
			
		}
	}
	
	private void setPurchaseBom4Service(ViewData<BomDetail> viewData) {
		List<BomDetail> bomList = new ArrayList<BomDetail>();
		for (OrderDetail detail: this.getDetailList()) {
			OrderDetail order=detail, sorder=order.getSnapShot();
			List<BomDetail> boms=this.getBomDetails(sorder.getMonthnum()), rootBoms=new BomTicketLogic().getChildrenRoot(boms);
			for (BomDetail bom: boms) {
				bom.getBomTicket().setOccupyAmount(0);
				bom.setMonthnum(order.getMonthnum());
				bomList.add(bom);
			}
			for (Iterator<BomDetail> iter=rootBoms.iterator(); iter.hasNext();) {
				BomDetail bom = iter.next();
				bom.setSupplier(detail.getSupplier());
				if (new ArrangeTypeLogic().isNormal(bom.getArrange())) {
					List<BomDetail> subs = new BomTicketLogic().getChildrenBrother(boms, bom);
					iter = new JoinedIterator(iter, subs.iterator());
				}
			}
			this.getBomDetailsMap().remove(sorder.getMonthnum());
			this.getBomDetailsMap().put(order.getMonthnum(), boms);
		}
		viewData.setTicketDetails(bomList);
	}
	
	private BomTicket getBomTicket() {
		BomTicket t = this.getAttr(BomTicket.class);
		if (t==null) {
			t = new BomTicket();
			this.setAttr(t);
		}
		return t;
	}
	
	private void setBomCopy() {
		List<BomDetail> list = this.getSelectFormer4Order().getFirst().getCommodity().getBomDetails();
		if (list == null)
			try {
				list = this.getSelectFormer4Order().getFirst().getVoparam(Commodity.class).getBomDetails();
			}catch(Exception e){
				//
			}
		getBomCopy().clear();
		getBomCopy().addAll(list==null? new ArrayList<BomDetail>(0): list);
		getBomCopy().addAll(this.getBomDetails(this.getSelectFormer4Order().getFirst().getMonthnum()));
	}
	
	public BOMForm getBomForm() {
		return this.getAttr(BOMForm.class);
	}
	
	private SupplyTypeForm getSupplyTypeForm() {
		SupplyTypeForm form = this.getAttr(SupplyTypeForm.class);
		if (form == null) {
			form = new SupplyTypeForm();
			SupplyType supply = new SupplyType();
			supply.setName(new SupplyTypeLogic().getProductType());
			form.setSupply(supply);
			this.setAttr(form);
		}
		return form;
	}
	
	private List<BomDetail> getBomCopy() {
		String k="BomCopyList";
		List<BomDetail> list = this.getAttr(k);
		if (list==null) {
			list = new ArrayList<BomDetail>();
			this.setAttr(k, list);
		}
		return list;
	}

	public List<Hyperlink> getMonthnumLinkList() {
		String k = "MonthnumList";
		List<Hyperlink> list = this.getAttr(k);
		if (list == null) {
			list = new ArrayList<Hyperlink>();
			this.setAttr(k, list);
		}
		return list;
	}
	
	private BomDetail getABomDetail() {
		String k = "ABomDetail";
		BomDetail bom = this.getAttr(k);
		if (bom == null) {
			bom = new BomDetail();
			this.setAttr(k, bom);
		}
		return bom;
	}
	
	private void setABomDetail(BomDetail bom) {
		String k = "ABomDetail";
		this.setAttr(k, bom);
	}
	
	private void setCalculateDo() {
		ClientTest test = new ClientTest();
		test.loadFormView(new PPurchaseTicketForm(), "BomDetailList");
		test.onMenu("计算物料配给");
	}
	
	private void getStoreEnoughNew() {
		ClientTest test = new ClientTest();
		test.loadFormView(new StoreTicketForm(), "EnoughList");
		test.onMenu("计算够用数");
	}
	
	private void setCalcGotAmount() {
		OnPageLoadedListener onpage = this.getOnPageLoadedListener();
		StringBuffer error = new StringBuffer();
		onpage.setBomDetailsMap(this.getSelectFormer4Bom().getSelectedList());
		for (BomDetail bom: this.getSelectFormer4Bom().getSelectedList()) {
			StoreEnough store = bom.getVoparam(StoreEnough.class);
			if (store==null || store.getStoreAmount()==0 || bom.getBomTicket().getOccupyAmount()==0)
				continue;
			store.setFreeAmount(store.getStoreAmount() - bom.getBomTicket().getOccupyAmount());
		}
		HashSet<StoreEnough> storeList = new HashSet<StoreEnough>();
		for (BomDetail bom: this.getSelectFormer4Bom().getSelectedList()) {
			StoreEnough store = bom.getVoparam(StoreEnough.class);
			if (store==null)
				continue;
			if (storeList.add(store) && store.getStoreAmount()<0) {
				error.append(store.getVoparam(CommodityT.class).getCommName()).append("不够指派数量").append(store.getStoreAmount()).append("\t");
			}
		}
		if (error.length()==0)
			error.append("计算成功").append(new TimeType().format(new Date()));
		this.getOrderDetail().getOrderTicket().setRemark(error.toString());
		BomTicketLogic bomLogic = new BomTicketLogic();
		for (OrderDetail order: this.getSelectFormer4Order().getSelectedList()) {
			List<BomDetail> sourceList = this.getBomDetails(order.getMonthnum());
			List<BomDetail> rootList = bomLogic.getChildrenRoot(sourceList);
			this.setCalcGotAmount(order, rootList, sourceList);
		}
	}
	
	private void setCalcGotAmount(AbstractCommodityItem parent, List<BomDetail> brotherList, List<BomDetail> sourceList) {
		BomTicketLogic bomLogic = new BomTicketLogic();
		for (BomDetail brother: brotherList) {
			List<StoreEnough> storeList = brother.getVoparam("StoreEnoughList");
			for (StoreEnough store: storeList) { 
				double maxGiveOccupyCommit=brother.getBomTicket().getGiveAmount();
				if (brother.getBomTicket().getOccupyAmount()>maxGiveOccupyCommit)
					maxGiveOccupyCommit = brother.getBomTicket().getOccupyAmount();
				if (brother.getBomTicket().getCommitAmount()>maxGiveOccupyCommit)
					maxGiveOccupyCommit = brother.getBomTicket().getCommitAmount();
				brother.getBomTicket().setGotAmount(maxGiveOccupyCommit);
				brother.getBomTicket().setNotAmount(brother.getAmount()>maxGiveOccupyCommit? brother.getAmount()-maxGiveOccupyCommit: 0);
				if ("没库存".length()>0 && store.getStoreAmount()<=0) {
				} else if ("够库存".length()>0 && store.getStoreAmount()>=brother.getBomTicket().getNotAmount()) {
					store.setStoreAmount(store.getStoreAmount() - brother.getBomTicket().getNotAmount());
					brother.getBomTicket().setGotAmount(brother.getBomTicket().getGotAmount()+brother.getBomTicket().getNotAmount());
					brother.getBomTicket().setNotAmount(0);
				} else if ("部分有库存".length()>0) {
					brother.getBomTicket().setGotAmount(brother.getBomTicket().getGotAmount()+store.getStoreAmount());
					brother.getBomTicket().setNotAmount(brother.getBomTicket().getNotAmount()-store.getStoreAmount());
					store.setStoreAmount(0);
				}
			}
			if ("下一级物料计算".length()>0) {
				List<BomDetail> childList = bomLogic.getChildrenBrother(sourceList, brother);
				if (childList.size()==0)
					continue;
				this.setCalcGotAmount(brother, childList, sourceList);
			}
		}
		boolean ok = false;
		if ("齐料率，齐料数".length()>0) {
			double minCan=0, can=0;
			for (BomDetail brother: brotherList) {
				can = brother.getBomTicket().getGotAmount() / brother.getBomTicket().getAunit();
				can = ((Double)can).intValue();
				if (minCan>can || minCan==0)
					minCan = can;
			}
			DoubleType type = new DoubleType();
			StringBuffer sb = new StringBuffer();
			if (StringUtils.equals(type.format(minCan), type.format(parent.getAmount()))) {
				sb.append("物料备齐100%");
				ok = true;
			} else if (StringUtils.equals(type.format(minCan), "")) {
			} else
				sb.append("可生产数量").append(type.format(minCan));
			PurchaseTicket pur = (PurchaseTicket)ReflectHelper.getPropertyValue(parent, "PurchaseTicket");
			pur.setMaterial(sb.toString());
		}
		if (parent.getClass()==OrderDetail.class)
			((OrderDetail)parent).setBomId(ok? 30: 0);
	}
	
	public LinkedHashMap<String, List<BomDetail>> getBomDetailsMap() {
		String k = "BomDetailsMap";
		LinkedHashMap<String, List<BomDetail>> map = this.getAttr(k);
		if (map==null) {
			map = new LinkedHashMap<String, List<BomDetail>>();
			this.setAttr(k, map);
		}
		return map;
	}
	
	public List<BomDetail> getBomDetails(String monthnum) {
		List<BomDetail> list = this.getBomDetailsMap().get(monthnum);
		if (list==null)
			list = new ArrayList<BomDetail>(0);
		return list;
	}
	
	private List<TextField[]> getTextFieldMatrix() {
		String k = "TextFieldMatrix";
		List<TextField[]> matrix = this.getAttr(k);
		if (matrix == null) {
			matrix = new ArrayList<TextField[]>();
			this.setAttr(k, matrix);
		}
		return matrix;
	}
	
	private void setTextFieldMatrix() {
		List<TextField[]> matrix = this.getTextFieldMatrix();
		for (int rowSize=matrix.size(), row=0; row<rowSize; row++) {
			for (int colSize=matrix.get(row).length, col=0; col<colSize; col++) {
				TextField textfield = matrix.get(row)[col];
				if (textfield==null)
					continue;
				StringBuffer sb = new StringBuffer();
				TextField t = null; // top
				for (int ri=row; ri>0 && t==null; ri--, t=matrix.get(ri)[col]);
				sb.append(t==null? "": t.getIdentifier()).append(",");
				t = null; // right
				for (int ci=col; ci<colSize-1 && t==null; ci++, t=matrix.get(row)[ci]);
				for (int ri=row; ri<rowSize-1 && col<colSize-1 && t==null; ri++, t=matrix.get(ri)[col+1]);
				sb.append(t==null? "": t.getIdentifier()).append(",");
				t = null; // bottom
				for (int ri=row; ri<rowSize-1 && t==null; ri++, t=matrix.get(ri)[col]);
				sb.append(t==null? "": t.getIdentifier()).append(",");
				t = null; // left
				for (int ci=col; ci>0 && t==null; ci--, t=matrix.get(row)[ci]);
				for (int ri=row; ri>0 && col>0 && t==null; ri--, t=matrix.get(ri)[col-1]);
				sb.append(t==null? "": t.getIdentifier()).append(",");
				sb.append(",");
				sb.append(",");
				textfield.addAttribute(AttributeName.Neighbor, sb.toString());
				textfield.addAttribute(ClientEventName.ONKEYDOWN, "L_propertychange_neighbor(this, event);");
			}
		}
	}
	
	private void setBomDetailMaterial(Component fcomp) {
		List<Field> flist = null;
		for (Component cur=fcomp, prt=fcomp.getParent(); cur!=null; cur=prt, prt=cur==null? null: cur.getParent()) {
			if (cur instanceof BlockGrid) {
				BlockGrid g = (BlockGrid)cur;
				flist = g.getInnerFormerList(Field.class);
				if (flist.size()==2)
					break;
			}
		}
		BomDetail d_temp = (BomDetail)flist.get(0).getEntityBean().getBean(), d=d_temp.getVoparam(BomDetail.class);
		Assert.assertTrue("找到原物料明细", d!=null && d.getId()>0);
		this.setAttr(d);
		this.setAttr(fcomp.searchParentByClass(Hyperlink.class));
		this.getOrderDetail().setAmount(d.getAmount());
		this.getOrderDetail().getCommodity().setRemark("指派数量后，要重新计算，出全部生产明细的齐料状态！");
	}
	
	private BomDetail getBomDetailRecord(Component fcomp) {
		List<Field> flist = null;
		fcomp.searchFormerLinkByClass(Field.class);
		for (Component cur=fcomp, prt=fcomp.getParent(); cur!=null; cur=prt, prt=cur==null? null: cur.getParent()) {
			if (cur instanceof BlockGrid) {
				BlockGrid g = (BlockGrid)cur;
				flist = g.getInnerFormerList(Field.class);
				if (flist.size()==7)
					break;
			}
		}
		BomDetail d_temp = (BomDetail)fcomp.searchFormerByClass(Field.class).getEntityBean().getBean(), d=d_temp.getVoparam(BomDetail.class);
		Assert.assertTrue("找到原物料明细", d!=null && d.getId()>0);
		double commt=(Double)flist.get(2).getValue(), occupy=(Double)flist.get(3).getValue(), back=(Double)flist.get(4).getValue(), koccupy=(Double)flist.get(5).getValue(), kback=(Double)flist.get(6).getValue();
		if (commt + occupy + back + koccupy + kback > 0) {
			d.getBomTicket().setCommitAmount(d.getBomTicket().getCommitAmount() + commt);
			d.getBomTicket().setOccupy1(d.getBomTicket().getOccupy1() + occupy-back);
			d.getBomTicket().setKeepAmount(d.getBomTicket().getKeepAmount()-d.getBomTicket().getOccupy2() + kback-koccupy);
			if (d.getBomTicket().getKeepAmount()>=0)
				d.getBomTicket().setOccupy2(0);
			else {
				d.getBomTicket().setOccupy2(0-d.getBomTicket().getKeepAmount());
				d.getBomTicket().setKeepAmount(0);
			}
			if (d.getBomTicket().getNotAmount()!=0 && d.getBomTicket().getCommitAmount()>0)
				d.getBomTicket().setNotAmount(d.getAmount()-d.getBomTicket().getCommitAmount());
			if (d.getBomTicket().getNotAmount()!=0 && d.getBomTicket().getOccupyAmount()>0)
				d.getBomTicket().setNotAmount(d.getAmount()-d.getBomTicket().getOccupyAmount());
			StringBuffer sb = new StringBuffer();
			sb = new StringBuffer(this.genTicketUser().getUserDate()).append("，");
			if (commt>0)	sb.append("加生产数").append(commt);
			if (occupy>0)	sb.append("加领料数").append(occupy);
			if (back>0)		sb.append("加还料数").append(back);
			if (koccupy>0)	sb.append("加留用.领料数").append(koccupy);
			if (kback>0)	sb.append("加留用.还料数").append(kback);
			d.getStateBuffer().append("\n").append(sb);
			return d;
		}
		return null;
	}
	
	private void setStoreEnoughSelect(List<StoreEnough> storeList) {
		BomDetail detail=this.getAttr(BomDetail.class), source=(BomDetail)detail.getSnapShot();
		Hyperlink link = this.getAttr(Hyperlink.class);
		detail.setCommodity(storeList.get(0).getCommodity());
		detail.setVoparam(storeList.get(0).getVoparam(CommodityT.class));
		detail.setVoparam(storeList.get(0));
		detail.getBomTicket().setGiveAmount(this.getOrderDetail().getAmount());
		detail.getBomTicket().setGotAmount(detail.getBomTicket().getGotAmount());
		detail.getBomTicket().setNotAmount(detail.getAmount() - detail.getBomTicket().getGotAmount());
		this.getOnPageLoadedListener().setBDetailsLinkMaterial(link, this.getBomDetails(detail.getMonthnum()));
		this.getOrderDetail().setAmount(0);
	}
	
	protected static class OnPageLoadedListener implements ActionListener {
		
		private PPurchaseTicketForm form;
		
		private OnPageLoadedListener(PPurchaseTicketForm form) {
			this.form = form;
		}
		
		public void perform(EventObject e) {
			String listName = e.getSource().searchFormerByClass(ListView.class).getFieldBuilder().getFullName();
			if ("查看".length()>0 && listName.startsWith("Show.")) {
				List<String> monthnumList = new ArrayList<String>();
				for (Hyperlink link: form.getMonthnumLinkList()) {
					monthnumList.add(link.getText());
				}
				this.getBomDetails(monthnumList.toArray(new String[0]));
				for (Hyperlink link: form.getMonthnumLinkList()) {
					List<BomDetail> dlist = form.getBomDetails(link.getText());
					link.setText(null);
					this.setBDetailsLinkShow(link, dlist);
				}
				e.getSource().getEventListenerList().removeListener(EventListenerType.Action, this);
			} else if ("订单成品生产录入".length()>0 && listName.startsWith("RecordList.")) {
				List<String> monthnumList = new ArrayList<String>();
				for (Hyperlink link: form.getMonthnumLinkList()) {
					monthnumList.add(link.getText());
				}
				this.getBomDetails(monthnumList.toArray(new String[0]));
				for (Hyperlink link: form.getMonthnumLinkList()) {
					List<BomDetail> dlist = form.getBomDetails(link.getText());
					link.setText(null);
					this.setBDetailsLinkRecord(link, dlist);
				}
				e.getSource().getEventListenerList().removeListener(EventListenerType.Action, this);
				form.setTextFieldMatrix();
			} else if ("订单成品生产开单".length()>0 && listName.startsWith("CommonList.")) {
				List<String> monthnumList = new ArrayList<String>();
				for (Hyperlink link: form.getMonthnumLinkList()) {
					monthnumList.add(link.getText());
				}
				this.getBomDetails(monthnumList.toArray(new String[0]));
				for (Hyperlink link: form.getMonthnumLinkList()) {
					List<BomDetail> dlist = form.getBomDetails(link.getText());
					link.setText(null);
					this.setBDetailsLinkMaterial(link, dlist);
				}
				e.getSource().getEventListenerList().removeListener(EventListenerType.Action, this);
			}
			TextField twidth = e.getSource().searchParentByClass(BlockCell.class).getBlockCol().getCellIterator().next().getInnerComponentList(TextField.class).get(0);
			TextField tfirst = e.getSource().searchFormerByClass(ListView.class).getComponent().getInnerComponentList(TextField.class).get(0);
			StringBuffer swidth = new StringBuffer("L_propertychange_resize($('").append(twidth.getIdentifier()).append("'));");
			tfirst.addAttribute(ClientEventName.InitScript0, swidth.toString());
			if ("同步左右高度".length()>0) {
				StringBuffer sb = new StringBuffer();
				List<BlockGrid> listviews = e.getSource().searchFormerByClass(ListView.class).getComponent().getInnerComponentList("ListViewBody");
				if (listviews.size()>0) {
					sb.append("var c=$('").append(listviews.get(0).getIdentifier()).append("');");
					sb.append("C_View.sameTrHeight(c.ids0Left, c.ids0Right);");
					tfirst.addAttribute(ClientEventName.InitScript1, sb.toString());
					LogUtil.info(new StringBuffer().append(tfirst).append(sb).toString());
				}
			}
		}
		
		protected List<BomDetail> getBomDetails(String... monthnumList) {
			ClientTest test = new ClientTest();
			test.loadSqlView(form, "BomDetailList", "selectFormer4Bom.selectedList", "monthnum", monthnumList);
			test.onMenu("全选");
			List<BomDetail> bomList = form.getSelectFormer4Bom().getSelectedList();
			this.setBomDetailsMap(bomList);
			form.getTextFieldMatrix().clear();
			return bomList;
		}
		
		private void setBDetailsLinkShow(Hyperlink link, List<BomDetail> dlist) {
			if (dlist==null || dlist.size()==0)
				return ;
			ViewBuilder selectBuilder = EntityClass.loadViewBuilder(form.getClass(), "BomDetailShow");
			form.setAttr("BomDetail4Order", form.getBomDetails(dlist.get(0).getMonthnum()));
			Component grid = selectBuilder.build(form).getComponent().getInnerFormerComponentList(ListView.class).get(0);
			Component pregrid = link.getComponent();
			if (pregrid!=null)
				pregrid.fireComponentReplace(grid);
			else
				link.add(grid);
		}
		
		private void setBDetailsLinkMaterial(Hyperlink link, List<BomDetail> dlist) {
			if (dlist==null || dlist.size()==0)
				return ;
			BlockGrid grid = new BlockGrid().createGrid(5, BlockGridMode.NotOccupySizable);
			grid.append(new Text("需求")).setStyleClass("ListCellTL");
			grid.append(new Text("配给")).setStyleClass("ListCellTL");
			grid.append(new Text("指派")).setStyleClass("ListCellTL");
			grid.append(new Text("差额")).setStyleClass("ListCellTL");
			grid.append(new Text("物料")).setStyleClass("ListCellTL").addStyle(StyleName.BORDER_WIDTH, "1 0 1 0");
			DoubleType type = new DoubleType();
			for (Iterator<BomDetail> biter=dlist.iterator(); biter.hasNext();) {
				BomDetail b=biter.next();
				boolean HasChild = (new BomTicketLogic().getChildrenFold(dlist, b).size()>0);
				grid.append(new Text(type.format(b.getAmount()))).setStyleClass("ListCellTL");
				grid.append(new Text(type.format(b.getBomTicket().getGotAmount()))).setStyleClass("ListCellTL");
				if (b.getAmount() > b.getBomTicket().getGotAmount()) {
					form.setABomDetail(TicketPropertyUtil.copyFieldsSkip(b, new BomDetail()));
					form.getABomDetail().setVoparam(b);
					BlockGrid g = (BlockGrid)EntityClass.loadViewBuilder(form.getClass(), "ABomMaterial").build(form).getComponent().getInnerComponentList("ViewButton").get(0).setStyleClass(null);
					g.getCell(0, 0).setComponent(new Text(type.format(b.getBomTicket().getOccupyAmount())));
					g.getCell(1, 0).addStyle(StyleName.BACKGROUND, "#9DCFFE");
					grid.append(g).setStyleClass("ListCellTL");
				} else {
					grid.append(new Text(type.format(b.getBomTicket().getOccupyAmount()))).setStyleClass("ListCellTL");
				}
				grid.append(new Text(type.format(b.getBomTicket().getNotAmount()))).setStyleClass("ListCellTL");
				if ("商品名称".length()>0) {
					StringBuffer sb = new StringBuffer();
					for (int i=b.getLevel(); i-->1; sb.append("\t\t"));
					sb.append(b.getVoparam(CommodityT.class).getCommName()).append("，");
					sb.append(b.getArrange()).append("，");
					if (b.getStPurchase()>0)
						sb.deleteCharAt(sb.length()-1).append(b.getPurchaseTicket().getNumber()).append(",").append(b.getVoparam(SupplierT.class).getSupplierName()).append("，");
					if (HasChild && b.getPurchaseTicket().getMaterial()!=null)
						sb.append(b.getPurchaseTicket().getMaterial()).append("，");
					grid.append(new Hyperlink(null, sb.deleteCharAt(sb.length()-1).toString()));
				}
			}
			Component pregrid = link.getComponent();
			if (pregrid!=null)
				pregrid.fireComponentReplace(grid);
			else
				link.add(grid);
		}
		
		private void setBDetailsLinkRecord(Hyperlink link, List<BomDetail> dlist) {
			if (dlist==null || dlist.size()==0)
				return ;
			BlockGrid grid = new BlockGrid().createGrid(9, BlockGridMode.NotOccupySizable);
			if ("订单记住Link，BomDetailList".length()>0) {
				OrderDetail order = dlist.get(0).getVoparam(OrderDetail.class);
				if (order!=null) {
					grid.setFormer(order);
					order.setVoparam(link);
					order.getVoParamMap().put("BomDetailList", dlist);
				}
			}
			for (int i=0, isize=grid.getColSize(), sizeList[]={4, 4, 4, 10, 10, 10, 10, 10, 15}; i<isize; i++) {
				TextField twidth = new TextField();
				twidth.addAttribute(AttributeName.Height, "1px").getAttributes().removeAttribute(AttributeName.CLASS);
				twidth.setSize(sizeList[i]);
				twidth.addAttribute(AttributeName.READONLY, true);
				BlockCell cell = grid.append(twidth);
				if (i==0)
					cell.getBlockRow().setStyleClass("ListViewHeaderTwidth");
			}
			grid.append(new Text("需求")).setStyleClass("ListCellTL");
			grid.append(new Text("配给")).setStyleClass("ListCellTL");
			grid.append(new Text("差额")).setStyleClass("ListCellTL");
			grid.append(new Text("+生产数")).setStyleClass("ListCellTL");
			grid.append(new Text("+仓库.领料数")).setStyleClass("ListCellTL");
			grid.append(new Text("+仓库.还料数")).setStyleClass("ListCellTL");
			grid.append(new Text("+留用.领料数")).setStyleClass("ListCellTL");
			grid.append(new Text("+留用.还料数")).setStyleClass("ListCellTL");
			grid.append(new Text("物料")).setStyleClass("ListCellTL").addStyle(StyleName.BORDER_WIDTH, "1 0 1 0");
			DoubleType type = new DoubleType();
			BomTicketLogic bomLogic = new BomTicketLogic();
			for (BomDetail bomDetail: dlist) {
				grid.append(new Text(type.format(bomDetail.getAmount()))).setStyleClass("ListCellTL");
				grid.append(new Text(type.format(bomDetail.getBomTicket().getGotAmount()))).setStyleClass("ListCellTL");
				grid.append(new Text(type.format(bomDetail.getBomTicket().getNotAmount()))).setStyleClass("ListCellTL");
				form.setABomDetail(TicketPropertyUtil.copyProperties(bomDetail, new BomDetail()));
				form.getABomDetail().setVoparam(bomDetail);
				form.getABomDetail().setId(bomDetail.getId());
				form.getABomDetail().getBomTicket().setCommitAmount(0);
				form.getABomDetail().getBomTicket().setOccupy1(0);
				form.getABomDetail().getBomTicket().setBack1(0);
				form.getABomDetail().getBomTicket().setOccupy2(0);
				form.getABomDetail().getBomTicket().setKeepAmount(0);
				if ("已领数、加领料数，已还数，加还料数".length()>0) {
					List<TextField> flist = EntityClass.loadViewBuilder(form.getClass(), "ABomRecord").build(form).getComponent().getInnerComponentList(TextField.class);
					TextField[] textRow = bomDetail.getVoparam("TextFieldRow");
					if (textRow==null) {
						textRow = new TextField[5];
						bomDetail.getVoParamMap().put("TextFieldRow", textRow);
						form.getTextFieldMatrix().add(textRow);
					} else if ("保持Neighbor切换".length()>0) {
						for (int ti=0; ti<textRow.length; ti++) {
							TextField f0=textRow[ti], f1=flist.get(ti);
							if (f0!=null) {
								f1.setIdentifier(f0.getIdentifier());
								f1.addAttribute(AttributeName.Neighbor, f0.getAttributes().getAttribute(AttributeName.Neighbor));
								f1.addAttribute(ClientEventName.ONKEYDOWN, "L_propertychange_neighbor(this, event);");
							}
						}
					}
					if ("生产数".length()>0 && new SupplyTypeLogic().isProductType(bomDetail.getCommodity().getSupplyType())) {
						BlockGrid g = new BlockGrid().createGrid(2, BlockGridMode.Independent);
						g.append(new Text(type.format(bomDetail.getBomTicket().getCommitAmount())));
						g.append(flist.get(0));
						textRow[0] = flist.get(0);
						grid.append(g).setStyleClass("ListCellTL");
						g.addStyle(StyleName.WIDTH, "100%");
					} else {
						grid.append(null).setStyleClass("ListCellTL");
					}
					if ("仓库领料数".length()>0) {
						BlockGrid g = new BlockGrid().createGrid(2, BlockGridMode.Independent);
						g.append(new Text(type.format(bomDetail.getBomTicket().getOccupy1())));
						g.append(flist.get(1));
						textRow[1] = flist.get(1);
						grid.append(g).setStyleClass("ListCellTL");
						g.addStyle(StyleName.WIDTH, "100%");
					}
					if ("仓库还料数".length()>0) {
						BlockGrid g = new BlockGrid().createGrid(2, BlockGridMode.Independent);
						g.append(new Text(type.format(bomDetail.getBomTicket().getBack1())));
						g.append(flist.get(2));
						textRow[2] = flist.get(2);
						grid.append(g).setStyleClass("ListCellTL");
						g.addStyle(StyleName.WIDTH, "100%");
					}
					if ("留用领料数".length()>0) {
						BlockGrid g = new BlockGrid().createGrid(2, BlockGridMode.Independent);
						g.append(new Text(type.format(bomDetail.getBomTicket().getOccupy2())));
						g.append(flist.get(3));
						textRow[3] = flist.get(3);
						grid.append(g).setStyleClass("ListCellTL");
						g.addStyle(StyleName.WIDTH, "100%");
					}
					if ("留用还料数".length()>0) {
						BlockGrid g = new BlockGrid().createGrid(2, BlockGridMode.Independent);
						g.append(new Text(type.format(bomDetail.getBomTicket().getKeepAmount())));
						g.append(flist.get(4));
						textRow[4] = flist.get(4);
						grid.append(g).setStyleClass("ListCellTL");
						g.addStyle(StyleName.WIDTH, "100%");
					}
				}
				if ("Bom商品名称".length()>0) {
					StringBuffer sb = new StringBuffer();
					for (int i=bomDetail.getLevel(); i-->1; sb.append("\t\t"));
					sb.append(bomDetail.getVoparam(CommodityT.class).getCommName()).append("，");
					sb.append(bomDetail.getArrange()).append("，");
					if (bomDetail.getStPurchase()>0)
						sb.deleteCharAt(sb.length()-1).append(bomDetail.getPurchaseTicket().getNumber()).append(",").append(bomDetail.getVoparam(SupplierT.class).getSupplierName()).append("，");
					if (bomLogic.getChildrenFold(dlist, bomDetail).size()>0 && bomDetail.getPurchaseTicket().getMaterial()!=null)
						sb.append(bomDetail.getPurchaseTicket().getMaterial()).append("，");
					grid.append(new Hyperlink(null, sb.deleteCharAt(sb.length()-1).toString()));
				}
			}
			Component pregrid = link.getComponent();
			if (pregrid!=null) {
				pregrid.fireComponentReplace(grid);
			} else
				link.add(grid);
		}
		
		private void setBomDetailsMap(List<BomDetail> bomList) {
			LinkedHashMap<String, List<BomDetail>> map = new LinkedHashMap<String, List<BomDetail>>();
			for (BomDetail b: bomList) {
				List<BomDetail> details = map.get(b.getMonthnum());
				if (details==null) {
					details = new ArrayList<BomDetail>();
					map.put(b.getMonthnum(), details);
				}
				details.add(b);
			}
			form.getBomDetailsMap().clear();
			form.getBomDetailsMap().putAll(map);
		}
	}
	
	protected <F extends PurchaseTicketForm> PropertyChoosableLogic.TicketDetail<F, PurchaseTicket, PurchaseT> getPurchaseChoosable() {
		return (PropertyChoosableLogic.TicketDetail<F, PurchaseTicket, PurchaseT>)new PurchaseTicketLogic().getPTicketChoosableLogic();
	}
	
	private ChooseFormer getMaterialChooseFormer() {
		ChooseFormer former = new ChooseFormer();
		PropertyChoosableLogic.Choose12<BOMForm, Commodity, CommodityT> plogic = new CommodityLogic().getMaterialChoosableLogic();
		former.setViewBuilder(plogic.getChooseBuilder());
		former.setSellerViewSetting(plogic.getChooseSetting(plogic.getChooseBuilder()));
		return former;
	}
}
