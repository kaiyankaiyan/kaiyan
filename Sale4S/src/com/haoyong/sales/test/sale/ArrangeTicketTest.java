package com.haoyong.sales.test.sale;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.mily.webObject.RadioButtonGroupBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.PurchaseT;
import com.haoyong.sales.sale.form.ArrangeTicketForm;
import com.haoyong.sales.sale.form.OrderTicketForm;
import com.haoyong.sales.sale.form.ReceiptTicketForm;
import com.haoyong.sales.sale.form.SaleQueryForm;
import com.haoyong.sales.sale.form.SendTicketForm;
import com.haoyong.sales.sale.logic.ArrangeTypeLogic;
import com.haoyong.sales.test.base.AbstractTest;

public class ArrangeTicketTest extends AbstractTest<ArrangeTicketForm> implements TRemind {
	
	public ArrangeTicketTest() {
		this.setForm(this.getModeList().getSelfArrangeForm());
	}
	
	public void check订单安排__1用常规库存_2请购_3直发_4当地购(char type, String number) {
		ArrangeTicketForm form = this.getForm();
		this.loadView("ArrangeList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if ("1用常规库存".length()>0 && type=='1') {
			this.onMenu("用常规库存");
			OrderDetail order=form.getSelectFormer4Order().getLast();
			Assert.assertTrue("有安排", order.getArrangeId()==30 && order.getUarrange()!=null);
			this.loadView("TransList", "number", number);
			Assert.assertTrue("安排用常规库存未出货前应可调整", this.getListViewValue().size()==detailCount);
			this.loadFormView(new SendTicketForm(), "SendList", "number", number);
			Assert.assertTrue("安排用常规库存未出货前应可订单发货", this.getListViewValue().size()==detailCount);
		}
		if ("2请购".length()>0 && type=='2') {
			form.getOrderDetail().getArrangeTicket().setArrangeType(new DeliverTypeLogic().getCommonType());
			if (this.getModeList().contain(TestMode.Product))
				form.getOrderDetail().getPurchaseTicket().setAgent("生产车间一");
			this.onButton("请购");
			OrderDetail order=form.getSelectFormer4Order().getLast();
			Assert.assertTrue("有安排", order.getArrangeId()==30 && order.getUarrange()!=null);
			this.setFilters("number", number);
			Assert.assertTrue("请购失败", this.getListViewValue().size()==0);
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CommonList", 
					"number", number);
			Assert.assertTrue("采购商品请购要到采购开单", this.getListViewValue().size()==detailCount);
		}
		if ("3直发".length()>0 && type=='3') {
			form.getOrderDetail().getArrangeTicket().setArrangeType(new DeliverTypeLogic().getDirectType());
			if (this.getModeList().contain(TestMode.Product))
				form.getOrderDetail().getPurchaseTicket().setAgent("生产车间一");
			form.getOrderDetail().getArrangeTicket().setDeliverNote("直发到加工厂地址");
			this.onButton("请购");
			OrderDetail order=form.getSelectFormer4Order().getLast();
			Assert.assertTrue("有安排", order.getArrangeId()==30 && order.getUarrange()!=null);
			this.setFilters("number", number);
			Assert.assertTrue("请购失败", this.getListViewValue().size()==0);
			for (OrderDetail detail: form.getSelectFormer4Order().getSelectedList()) {
				Assert.assertEquals("明细是应直发安排", detail.getArrangeTicket().getArrangeType(), new DeliverTypeLogic().getDirectType());
				Assert.assertTrue("明细要有直发备注", detail.getArrangeTicket().getDeliverNote()!=null);
			}
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "DirectList", 
					"number", number);
			Assert.assertTrue("采购商品直发请购要到直发开单", this.getListViewValue().size()==detailCount);
		}
		if ("4当地购".length()>0 && type=='4') {
			form.getOrderDetail().getArrangeTicket().setArrangeType(new DeliverTypeLogic().getLocalType());
			if (this.getModeList().contain(TestMode.Product))
				form.getOrderDetail().getPurchaseTicket().setAgent("生产车间一");
			form.getOrderDetail().getArrangeTicket().setDeliverNote("在加工厂当地采购，指定采购人");
			this.onButton("请购");
			OrderDetail order=form.getSelectFormer4Order().getLast();
			Assert.assertTrue("有安排", order.getArrangeId()==30 && order.getUarrange()!=null);
			this.setFilters("number", number);
			Assert.assertTrue("请购失败", this.getListViewValue().size()==0);
			OrderDetail detail = form.getSelectFormer4Order().getLast();
			Assert.assertEquals("明细是应当地购安排", detail.getArrangeTicket().getArrangeType(), new DeliverTypeLogic().getLocalType());
			Assert.assertTrue("明细要有当地购备注", detail.getArrangeTicket().getDeliverNote()!=null);
			if (detail.getClient().getFromSellerId()==0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "LocalList", "number", number);
				Assert.assertTrue("采购商品申请当地购要到当地购开单", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check订单安排改单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0(char type, String number) {
		ArrangeTicketForm form = this.getForm();
		if ("1在采购排单发起改单申请".length()>0 && type=='1') {
			this.loadView("ArrangeList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单申请");
			if ("改客户人，改商品".length()>0) {
				this.setEditAllSelect(detailCount);
				try {
					this.onMenu("删除申请改单");
					Assert.fail("开申请页面不能删除改单申请");
				} catch(Exception e) {
				}
				StringBuffer fname = new StringBuffer("client.linkerCall");
				form.getNoteFormer4Order().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Order().getVoNoteMap(form.getDomain()).put(fname.toString(), "0512-111111-Call");
				fname = new StringBuffer("commodity.model");
				form.getNoteFormer4Order().getNoteString(form.getDetailList().get(0), fname);
				for (OrderDetail detail: form.getDetailList()) {
					form.getNoteFormer4Order().getVoNoteMap(detail).put(fname.toString(), "2.0*8");
				}
				form.getDomain().setChangeRemark("改客户人，改商品");
			}
			this.setEditAllSelect(detailCount);
			List<OrderDetail> detailList = new ArrayList<OrderDetail>(form.getDetailList());
			this.onMenu("提交申请改单");
			if (true) {
				for (OrderDetail detail: detailList) {
					Assert.assertTrue("每个改单申请明细都要改单内容", detail.getNotes()!=null);
					Assert.assertTrue("每个改单申请明细都要有申请原因", detail.getChangeRemark()!=null);
				}
				this.loadFormView(this.getModeList().getSelfOrderForm(), "RechangeList", 
						"number", number);
				Assert.assertTrue("改单申请要到订单返订单处理", this.getListViewValue().size()==detailCount);
			}
		}
		if ("2重发起改单申请，调整改单".length()>0 && type=='2') {
			this.loadView("ArrangeList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单不通过确认");
			form.getDomain().setChangeRemark("改客户人，改商品2");
			this.setEditAllSelect(detailCount);
			List<OrderDetail> detailList = new ArrayList<OrderDetail>(form.getDetailList());
			this.onMenu("提交申请改单");
			if (true) {
				for (int i=0; i<detailCount; i++,
					Assert.assertTrue("每个改单申请明细都要改单内容", detailList.get(i-1).getNotes()!=null),
					Assert.assertTrue("每个改单申请明细都要有申请原因", detailList.get(i-1).getChangeRemark()!=null));
				this.loadFormView(this.getModeList().getSelfOrderForm(), "RechangeList", 
						"number", number);
				Assert.assertTrue("改单申请要到订单返订单处理", this.getListViewValue().size()==detailCount);
			}
		}
		if ("3订单改单不通过，删除改单申请".length()>0 && type=='3') {
			this.loadView("ArrangeList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单不通过确认");
			this.setEditAllSelect(detailCount);
			this.onMenu("删除申请改单");
			if (true) {
				for (int i=0; i<detailCount; i++,
					Assert.assertTrue("每个明细都要删除改单内容", form.getDetailList().get(i-1).getNotes()==null),
					Assert.assertTrue("每个明细都要删除改单备注", form.getDetailList().get(i-1).getChangeRemark()==null));
			}
		}
		if ("4改单0数量返订单".length()>0 && type=='4') {
			this.loadView("ArrangeList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单申请");
			if (true) {
				StringBuffer fname = new StringBuffer("amount");
				form.getNoteFormer4Order().getNoteString(form.getDetailList().get(0), fname);
				for (int i=0; i<detailCount; i++, form.getNoteFormer4Order().getVoNoteMap(form.getDetailList().get(i-1)).put(fname.toString(), "0"));
				fname = new StringBuffer("commodity.model");
				form.getNoteFormer4Order().getNoteString(form.getDetailList().get(0), fname);
				for (int i=0; i<detailCount; i++, form.getNoteFormer4Order().getVoNoteMap(form.getDetailList().get(i-1)).put(fname.toString(), "12UZ"));
				form.getDomain().setChangeRemark("改数量0，改商品3");
			}
			this.setEditAllSelect(detailCount);
			List<OrderDetail> detailList = new ArrayList<OrderDetail>(form.getDetailList());
			this.onMenu("提交申请改单");
			if (true) {
				for (int i=0; i<detailCount; i++,
					Assert.assertTrue("每个改单申请明细都要改单内容", detailList.get(i-1).getNotes()!=null),
					Assert.assertTrue("每个改单申请明细都要有申请原因", detailList.get(i-1).getChangeRemark()!=null));
				this.loadFormView(this.getModeList().getSelfOrderForm(), "RechangeList", 
						"number", number);
				Assert.assertTrue("改单申请要到订单返订单处理", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单(char type, Object[] Afilters0, Object[] Bfilters0) {
		ArrangeTicketForm form = this.getForm();
		Object[] Afilters = this.genFiltersStart(Afilters0, "tabArrange.selectedList");
		this.loadView("ArrangeList", Afilters);
		this.setSqlAllSelect(this.getListViewValue().size());
		String sArrange = new StringBuffer().append(this.getListViewValue()).toString();
		if (type=='1' && "绑定备货库存".length()>0) {
			Object[] Bfilters = this.genFiltersStart(Bfilters0, "tabUStore.selectedList");
			this.setFilters(Bfilters);
			Assert.assertTrue("要有备货库存", this.getListViewValue().size()>0);
			this.setSqlAllSelect(this.getListViewValue().size());
			String sUStore = new StringBuffer().append(this.getListViewValue()).toString();
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("绑定备货库存");
			OrderDetail order=form.getSelectFormer4Purchase().getLast();
			Assert.assertTrue("有安排", order.getArrangeId()>=30 && order.getUarrange()!=null);
			Assert.assertTrue("排单有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabArrange.selectedList").getValue()).toString(), sArrange)==false);
			Assert.assertTrue("库存有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabUStore.selectedList").getValue()).toString(), sUStore)==false);
			double storeAmount=form.getFormProperty("attrMap.SplitPurchaseAmount"), orderAmount=form.getFormProperty("attrMap.SplitOrderAmount");
			String[] storeNumbers=form.getFormProperty("attrMap.SplitPurchaseNumbers"), orderNumbers=form.getFormProperty("attrMap.SplitOrderNumbers");
			if ("总库存数不变".length()>0) {
				if (storeAmount >= orderAmount) {
					this.setFilters(Afilters);
					Assert.assertTrue("订单够库存应无未安排", (Double)this.getListFootColumn("amount").get(0)==null);
					this.setFilters("tabUStore.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单占用库存数量=未安排时的数量", (Double)this.getListFootColumn("amount").get(0)==orderAmount);
					this.setFilters("tabUStore.selectedList", "number", storeNumbers, "modifytime", timeDo);
					if (storeAmount-orderAmount>0)
						Assert.assertTrue("未被占用的剩余数量应在库存列表中", (Double)this.getListFootColumn("amount").get(0)==storeAmount-orderAmount);
				} else if (storeAmount < orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单不够库存应有剩余未安排", (Double)this.getListFootColumn("amount").get(0)==orderAmount-storeAmount);
					this.setFilters("tabUStore.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单占用库存数量=库存数量", (Double)this.getListFootColumn("amount").get(0)==storeAmount);
					this.setFilters("tabUStore.selectedList", "number", storeNumbers, "modifytime", timeDo);
					Assert.assertTrue("未被占用的剩余数量应为0", (Double)this.getListFootColumn("amount").get(0)==null);
				}
			}
			if (true) {
				this.setFilters("tabUStore.selectedList", "number", storeNumbers, "modifytime", timeDo);
				Assert.assertTrue("被挪用采购拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
				this.setFilters("tabUStore.selectedList", "number", orderNumbers, "modifytime", timeDo);
				Assert.assertTrue("订单拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
			}
			if (true) {
				for (OrderDetail ord: form.getDetailList()) {
					OrderDetail sord = ord.getSnapShot();
					OrderDetail pur=(OrderDetail)ord.getVoParamMap().get("FromStore"), spur=null;
					if (pur != null) {
						OrderDetail ordA=pur, purB=ord;
						spur=pur.getSnapShot();
						Assert.assertTrue("订单为有安排，可发货状态", ordA.getArrangeTicket().getNumber()!=null && ordA.getSendId()==20 && ordA.getReceiptId()>=30);
						Assert.assertTrue("订单挪用库存的用订单月流水号，有原客户分公司", ordA.getMonthnum().startsWith(sord.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(ordA.getClient().getName(),sord.getClient().getName()) && StringUtils.equals(ordA.getSubCompany().getName(),sord.getSubCompany().getName()));
						Assert.assertTrue("被挪用备货采购库存删除", purB.getId()<0);
					} else {
						Assert.assertTrue("不够库存为未安排", ord.getId()>0 && ord.getSendId()==0 && ord.getArrangeId()==0);
						Assert.assertTrue("订单不库存的继续原月流水号", StringUtils.equals(ord.getMonthnum(), sord.getMonthnum()));
					}
				}
				for (OrderDetail pur: form.getPurchaseList()) {
					Assert.assertTrue("分后的明细都有保存", pur.getId()>0);
				}
				this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
				Assert.assertTrue("挪用后要重计算库存数", this.getListViewValue().size()>0);
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("挪用后要重计算够用数", this.getListViewValue().size()>0);
			}
		}
		if (type=='2' && "挪用订单库存".length()>0) {
			Object[] Bfilters = this.genFiltersStart(Bfilters0, "tabUStore.selectedList");
			this.setFilters(Bfilters);
			this.setSqlAllSelect(this.getListViewValue().size());
			String sUStore = new StringBuffer().append(this.getListViewValue()).toString();
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("挪用订单库存");
			OrderDetail order=form.getSelectFormer4Purchase().getLast();
			Assert.assertTrue("有安排", order.getArrangeId()>=30 && order.getUarrange()!=null);
			Assert.assertTrue("排单有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabArrange.selectedList").getValue()).toString(), sArrange)==false);
			Assert.assertTrue("库存有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabUStore.selectedList").getValue()).toString(), sUStore)==false);
			double storeAmount=form.getFormProperty("attrMap.SplitPurchaseAmount"), orderAmount=form.getFormProperty("attrMap.SplitOrderAmount");
			String[] storeNumbers=form.getFormProperty("attrMap.SplitPurchaseNumbers"), orderNumbers=form.getFormProperty("attrMap.SplitOrderNumbers");
			if ("总库存数不变".length()>0) {
				if (storeAmount >= orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单够库存应无未安排", (Double)this.getListFootColumn("amount").get(0)==null);
					this.setFilters("tabUStore.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单占用库存数量=未安排时的数量", (Double)this.getListFootColumn("amount").get(0)==orderAmount);
					this.setFilters("tabUStore.selectedList", "number", storeNumbers, "modifytime", timeDo);
					if (storeAmount-orderAmount>0)
						Assert.assertTrue("未被占用的剩余数量应在库存列表中", (Double)this.getListFootColumn("amount").get(0)==storeAmount-orderAmount);
				} else if (storeAmount < orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单不够库存应有剩余未安排", (Double)this.getListFootColumn("amount").get(0)==orderAmount-storeAmount);
					this.setFilters("tabUStore.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单占用库存数量=库存数量", (Double)this.getListFootColumn("amount").get(0)==storeAmount);
					this.setFilters("tabUStore.selectedList", "number", storeNumbers, "modifytime", timeDo);
					Assert.assertTrue("未被占用的剩余数量应为0", (Double)this.getListFootColumn("amount").get(0)==null);
				}
			}
			if (true) {
				this.setFilters("tabUStore.selectedList", "number", storeNumbers, "modifytime", timeDo);
				Assert.assertTrue("被挪用采购拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
				this.setFilters("tabUStore.selectedList", "number", orderNumbers, "modifytime", timeDo);
				Assert.assertTrue("订单拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
			}
			if (true) {
				for (OrderDetail ord: form.getDetailList()) {
					OrderDetail sord = ord.getSnapShot();
					OrderDetail pur=(OrderDetail)ord.getVoParamMap().get("FromStore"), spur=null;
					Assert.assertTrue("分后的明细都有保存", ord.getId()>0);
					if (pur != null) {
						OrderDetail ordA=pur, purB=ord;
						spur=pur.getSnapShot();
						Assert.assertTrue("订单为有安排，可发货状态", ordA.getArrangeTicket().getNumber()!=null && ordA.getSendId()==20 && ordA.getReceiptId()>=30);
						Assert.assertTrue("订单挪用库存的用订单月流水号，有原客户分公司", ordA.getMonthnum().startsWith(sord.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(ordA.getClient().getName(),sord.getClient().getName()) && StringUtils.equals(ordA.getSubCompany().getName(),sord.getSubCompany().getName()));
						Assert.assertTrue("被挪用采购，未排单，用采购月流水号，有原客户分公司", purB.getArrangeId()==sord.getArrangeId() && purB.getSendId()==sord.getSendId() && purB.getStPurchase()==sord.getStPurchase()
								&& purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(purB.getClient().getName(),spur.getClient().getName()) && StringUtils.equals(purB.getSubCompany().getName(),spur.getSubCompany().getName()));
					} else {
						Assert.assertTrue("不够库存为未安排", ord.getSendId()==0 && ord.getArrangeId()==0);
						Assert.assertTrue("订单不库存的继续原月流水号", StringUtils.equals(ord.getMonthnum(), sord.getMonthnum()));
					}
				}
				for (OrderDetail pur: form.getPurchaseList()) {
					Assert.assertTrue("分后的明细都有保存", pur.getId()>0);
				}
				this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
				Assert.assertTrue("挪用后要重计算库存数", this.getListViewValue().size()>0);
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("挪用后要重计算够用数", this.getListViewValue().size()>0);
			}
		}
		if (type=='3' && "绑定备货在途".length()>0) {
			Object[] Bfilters = this.genFiltersStart(Bfilters0, "tabUPassage.selectedList");
			this.setFilters(Bfilters);
			this.setSqlAllSelect(this.getListViewValue().size());
			String sUPassage = new StringBuffer().append(this.getListViewValue()).toString();
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("绑定备货在途");
			OrderDetail order=form.getSelectFormer4Purchase().getLast();
			Assert.assertTrue("有安排", order.getArrangeId()>=30 && order.getUarrange()!=null);
			Assert.assertTrue("排单有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabArrange.selectedList").getValue()).toString(), sArrange)==false);
			Assert.assertTrue("在途有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabUPassage.selectedList").getValue()).toString(), sUPassage)==false);
			double storeAmount=form.getFormProperty("attrMap.SplitPurchaseAmount"), orderAmount=form.getFormProperty("attrMap.SplitOrderAmount");
			String[] storeNumbers=form.getFormProperty("attrMap.SplitPurchaseNumbers"), orderNumbers=form.getFormProperty("attrMap.SplitOrderNumbers");
			if ("总在途数不变".length()>0) {
				if (storeAmount >= orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单够在途应无未安排", (Double)this.getListFootColumn("amount").get(0)==null);
					this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单占用在途数量=未安排时的数量", (Double)this.getListFootColumn("amount").get(0)==orderAmount);
					this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "modifytime", timeDo);
					if (storeAmount-orderAmount>0)
						Assert.assertTrue("未被占用的剩余数量应在在途列表中", (Double)this.getListFootColumn("amount").get(0)==storeAmount-orderAmount);
				} else if (storeAmount < orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单不够在途应有剩余未安排", (Double)this.getListFootColumn("amount").get(0)==orderAmount-storeAmount);
					this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单占用在途数量=在途数量", (Double)this.getListFootColumn("amount").get(0)==storeAmount);
					this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "modifytime", timeDo);
					Assert.assertTrue("未被占用的剩余数量应为0", (Double)this.getListFootColumn("amount").get(0)==null);
				}
			}
			if (true) {
				this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "modifytime", timeDo);
				Assert.assertTrue("被挪用采购拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
				this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "modifytime", timeDo);
				Assert.assertTrue("订单拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
			}
			if (true) {
				for (OrderDetail ord: form.getDetailList()) {
					OrderDetail sord = ord.getSnapShot();
					OrderDetail pur=(OrderDetail)ord.getVoParamMap().get("FromStore"), spur=null;
					Assert.assertTrue("分后的明细都有保存", ord.getId()>0);
					if (pur != null) {
						OrderDetail ordA=pur, purB=ord;
						spur=pur.getSnapShot();
						Assert.assertTrue("订单为有安排，采购在途状态", ordA.getArrangeTicket().getNumber()!=null && ordA.getSendId()==10);
						Assert.assertTrue("订单挪用库存的用订单月流水号，有原客户分公司", ordA.getMonthnum().startsWith(sord.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(ordA.getClient().getName(),sord.getClient().getName()) && StringUtils.equals(ordA.getSubCompany().getName(),sord.getSubCompany().getName()));
						Assert.assertTrue("被挪用采购，失效，用采购月流水号，有原客户分公司", purB.getStPurchase()==0 && purB.getStOrder()==0
								&& purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(purB.getClient().getName(),spur.getClient().getName()) && StringUtils.equals(purB.getSubCompany().getName(),spur.getSubCompany().getName()));
					} else {
						Assert.assertTrue("不够库存为未安排", ord.getSendId()==0 && ord.getArrangeId()==0);
						Assert.assertTrue("订单不库存的继续原月流水号", StringUtils.equals(ord.getMonthnum(), sord.getMonthnum()));
					}
				}
				for (OrderDetail pur: form.getPurchaseList()) {
					Assert.assertTrue("分后的明细都有保存", pur.getId()>0);
				}
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("挪用后要重计算在途数", (Double)this.getListFootColumn("onroadAmount").get(0)>0);
				Assert.assertTrue("挪用后要重计算够用数", this.getListViewValue().size()>0);
			}
		}
		if (type=='4' && "挪用订单在途".length()>0) {
			Object[] Bfilters = this.genFiltersStart(Bfilters0, "tabUPassage.selectedList");
			this.setFilters(Bfilters);
			this.setSqlAllSelect(this.getListViewValue().size());
			String sUPassage = new StringBuffer().append(this.getListViewValue()).toString();
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("挪用订单在途");
			OrderDetail order=form.getSelectFormer4Purchase().getLast();
			Assert.assertTrue("有安排", order.getArrangeId()>=30 && order.getUarrange()!=null);
			Assert.assertTrue("排单有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabArrange.selectedList").getValue()).toString(), sArrange)==false);
			Assert.assertTrue("在途有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabUPassage.selectedList").getValue()).toString(), sUPassage)==false);
			double storeAmount=form.getFormProperty("attrMap.SplitPurchaseAmount"), orderAmount=form.getFormProperty("attrMap.SplitOrderAmount");
			String[] storeNumbers=form.getFormProperty("attrMap.SplitPurchaseNumbers"), orderNumbers=form.getFormProperty("attrMap.SplitOrderNumbers");
			if ("总在途数不变".length()>0) {
				if (storeAmount >= orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单够在途应无未安排", (Double)this.getListFootColumn("amount").get(0)==null);
					this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单占用在途数量=未安排时的数量", (Double)this.getListFootColumn("amount").get(0)==orderAmount);
					this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "modifytime", timeDo);
					if (storeAmount-orderAmount>0)
						Assert.assertTrue("未被占用的剩余数量应在在途列表中", (Double)this.getListFootColumn("amount").get(0)==storeAmount-orderAmount);
				} else if (storeAmount < orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单不够在途应有剩余未安排", (Double)this.getListFootColumn("amount").get(0)==orderAmount-storeAmount);
					this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "modifytime", timeDo);
					Assert.assertTrue("订单占用在途数量=在途数量", (Double)this.getListFootColumn("amount").get(0)==storeAmount);
					this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "modifytime", timeDo);
					Assert.assertTrue("未被占用的剩余数量应为0", (Double)this.getListFootColumn("amount").get(0)==null);
				}
			}
			if (true) {
				this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "modifytime", timeDo);
				Assert.assertTrue("被挪用采购拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
				this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "modifytime", timeDo);
				Assert.assertTrue("订单拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
			}
			if (true) {
				for (OrderDetail ord: form.getDetailList()) {
					OrderDetail sord = ord.getSnapShot();
					OrderDetail pur=(OrderDetail)ord.getVoParamMap().get("FromStore"), spur=null;
					Assert.assertTrue("分后的明细都有保存", ord.getId()>0);
					if (pur != null) {
						OrderDetail ordA=pur, purB=ord;
						spur=pur.getSnapShot();
						Assert.assertTrue("订单为有安排，采购在途状态", ordA.getArrangeTicket().getNumber()!=null && ordA.getSendId()==10);
						Assert.assertTrue("订单挪用库存的用订单月流水号，有原客户分公司", ordA.getMonthnum().startsWith(sord.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(ordA.getClient().getName(),sord.getClient().getName()) && StringUtils.equals(ordA.getSubCompany().getName(),sord.getSubCompany().getName()));
						Assert.assertTrue("被挪用订单，未安排，用采购月流水号，有原客户分公司", purB.getArrangeId()==0 && purB.getSendId()==0 && purB.getStPurchase()==0
								&& purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(purB.getClient().getName(),spur.getClient().getName()) && StringUtils.equals(purB.getSubCompany().getName(),spur.getSubCompany().getName()));
					} else {
						Assert.assertTrue("不够库存为未安排", ord.getSendId()==0 && ord.getArrangeId()==0);
						Assert.assertTrue("订单不库存的继续原月流水号", StringUtils.equals(ord.getMonthnum(), sord.getMonthnum()));
					}
				}
				for (OrderDetail pur: form.getPurchaseList()) {
					Assert.assertTrue("分后的明细都有保存", pur.getId()>0);
				}
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("挪用后要重计算在途数", (Double)this.getListFootColumn("onroadAmount").get(0)>0);
				Assert.assertTrue("挪用后要重计算够用数", this.getListViewValue().size()>0);
			}
		}
	}
	
	public void check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲(char type, String number) {
		ArrangeTicketForm form = this.getForm();
		if ("1订单安排".length()>0 && type=='1') {
			this.loadView("ArrangeList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			form.getOrderDetail().setAmount(1);
			double fromAmount=(Double)this.getListFootColumn("amount").get(0);
			this.onButton("拆分订单");
			this.setFilters("number", number);
			Assert.assertTrue("拆分出订单数量1失败", this.getListViewValue().size()==detailCount*2);
			Assert.assertTrue("拆分出的订单要用新月流水号", new HashSet<String>((List)this.getListViewColumn("monthnum")).size()==detailCount*2);
			Assert.assertTrue("订单总数量不变", (Double)this.getListFootColumn("amount").get(0)==fromAmount);
		}
		if ("2调整安排".length()>0 && type=='2') {
			this.loadView("TransList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			form.getOrderDetail().setAmount(1);
			double fromAmount=(Double)this.getListFootColumn("amount").get(0);
			this.onButton("拆分订单");
			this.setFilters("number", number);
			Assert.assertTrue("拆分出订单数量1失败", this.getListViewValue().size()==detailCount*2);
			Assert.assertTrue("拆分出的订单要用新月流水号", new HashSet<String>((List)this.getListViewColumn("monthnum")).size()==detailCount*2);
			Assert.assertTrue("订单总数量不变", (Double)this.getListFootColumn("amount").get(0)==fromAmount);
			for (OrderDetail order: form.getSelectFormer4Order().getSelectedList()) {
				OrderDetail sorder = order.getSnapShot();
				OrderDetail orderPur=order, sorderPur=orderPur.getSnapShot();
				OrderDetail nwOrder = (OrderDetail)order.getVoParamMap().get("NewOrder");
				Assert.assertTrue("拆分出来的订单不为空", nwOrder!=null && nwOrder.getId()>0);
				OrderDetail nwOrderPur = nwOrder;
				Assert.assertTrue("原订单用原月流水号", StringUtils.equals(order.getMonthnum(), sorder.getMonthnum()));
				Assert.assertTrue("拆分出的新订单用新月流水号", StringUtils.equals(nwOrder.getMonthnum(), sorder.getMonthnum())==false);
				if (sorderPur.getStPurchase()>0) {
					Assert.assertTrue("拆分出的订单也要有拆分出的采购", nwOrderPur!=null);
					Assert.assertTrue("原订单采购用原月流水号", StringUtils.equals(orderPur.getMonthnum(), sorder.getMonthnum()));
					Assert.assertTrue("拆分出的新订单用新分支月流水号", nwOrderPur.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0].concat("-")));
					Assert.assertTrue("拆分出新订单的采购用新分支月流水号", StringUtils.equals(nwOrderPur.getMonthnum(), nwOrder.getMonthnum()));
				}
			}
		}
		if ("3红冲申请处理".length()>0 && type=='3') {
			this.loadView("AdjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			form.getOrderDetail().setAmount(1);
			double fromAmount=(Double)this.getListFootColumn("amount").get(0);
			this.onButton("拆分订单");
			this.setFilters("number", number);
			Assert.assertTrue("拆分出订单数量1失败", this.getListViewValue().size()==detailCount*2);
			Assert.assertTrue("拆分出的订单要用新月流水号", new HashSet<String>((List)this.getListViewColumn("monthnum")).size()==detailCount*2);
			Assert.assertTrue("订单总数量不变", (Double)this.getListFootColumn("amount").get(0)==fromAmount);
			for (OrderDetail order: form.getSelectFormer4Order().getSelectedList()) {
				OrderDetail sorder = order.getSnapShot();
				OrderDetail orderPur=order, sorderPur=orderPur.getSnapShot();
				OrderDetail nwOrder = (OrderDetail)order.getVoParamMap().get("NewOrder");
				Assert.assertTrue("拆分出来的订单不为空", nwOrder!=null && nwOrder.getId()>0);
				OrderDetail nwOrderPur = nwOrder;
				Assert.assertTrue("原订单用原月流水号", StringUtils.equals(order.getMonthnum(), sorder.getMonthnum()));
				Assert.assertTrue("拆分出的新订单用新月流水号", StringUtils.equals(nwOrder.getMonthnum(), sorder.getMonthnum())==false);
				if (sorderPur.getStPurchase()>0) {
					Assert.assertTrue("拆分出的订单也要有拆分出的采购", nwOrderPur!=null);
					Assert.assertTrue("原订单采购用原月流水号", StringUtils.equals(orderPur.getMonthnum(), sorder.getMonthnum()));
					Assert.assertTrue("拆分出的新订单用新分支月流水号", nwOrderPur.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0].concat("-")));
					Assert.assertTrue("拆分出新订单的采购用新分支月流水号", StringUtils.equals(nwOrderPur.getMonthnum(), nwOrder.getMonthnum()));
				}
			}
		}
		if ("4排单红冲开单".length()>0 && type=='4') {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			form.getOrderDetail().setAmount(1);
			double fromAmount=(Double)this.getListFootColumn("amount").get(0);
			this.onButton("拆分订单");
			this.setFilters("number", number);
			Assert.assertTrue("拆分出订单数量1失败", this.getListViewValue().size()==detailCount*2);
			Assert.assertTrue("拆分出的订单要用新月流水号", new HashSet<String>((List)this.getListViewColumn("monthnum")).size()==detailCount*2);
			Assert.assertTrue("订单总数量不变", (Double)this.getListFootColumn("amount").get(0)==fromAmount);
			for (OrderDetail order: form.getSelectFormer4Order().getSelectedList()) {
				OrderDetail sorder = order.getSnapShot();
				OrderDetail orderPur=order, sorderPur=orderPur.getSnapShot();
				OrderDetail nwOrder = (OrderDetail)order.getVoParamMap().get("NewOrder");
				Assert.assertTrue("拆分出来的订单不为空", nwOrder!=null && nwOrder.getId()>0);
				OrderDetail nwOrderPur = nwOrder;
				Assert.assertTrue("原订单用原月流水号", StringUtils.equals(order.getMonthnum(), sorder.getMonthnum()));
				Assert.assertTrue("拆分出的新订单用新月流水号", StringUtils.equals(nwOrder.getMonthnum(), sorder.getMonthnum())==false);
				if (sorderPur.getStPurchase()>0) {
					Assert.assertTrue("拆分出的订单也要有拆分出的采购", nwOrderPur!=null);
					Assert.assertTrue("原订单采购用原月流水号", StringUtils.equals(orderPur.getMonthnum(), sorder.getMonthnum()));
					Assert.assertTrue("拆分出的新订单用新分支月流水号", nwOrderPur.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0].concat("-")));
					Assert.assertTrue("拆分出新订单的采购用新分支月流水号", StringUtils.equals(nwOrderPur.getMonthnum(), nwOrder.getMonthnum()));
				}
			}
		}
	}
	
	public void check调整安排1__1常规_2普通_3直发_4当地购(char type, String number, String cancelType) {
		ArrangeTicketForm form = this.getForm();
		this.loadView("TransList", "number", number, "canState", true);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.setRadioGroup(cancelType);
		boolean transBack=form.getTransBack(), transCancel=form.getTransCancel(), transNone=form.getTransNone();
		double sumAmount = (Double)this.getListFootColumn("amount").get(0);
		if (type=='1' && "1常规库存".length()>0) {
			String timeDo = this.getTimeDo();
			this.onMenu("改用常规库存");
			OrderDetail order=form.getSelectFormer4Order().getLast(), sorder=order.getSnapShot();
			OrderDetail nord=order.getVoparam("NewTransTo");
			Assert.assertTrue("改用常规库存失败", StringUtils.equals(nord.getArrangeTicket().getArrangeType(), new ArrangeTypeLogic().getNormal()));
			if (sorder.getStPurchase()>0)
				Assert.assertTrue("新订单，无采购，月流水号应改变，无采购，常规可发", nord.getId()!=order.getId()
						&& StringUtils.equals(nord.getArrangeTicket().getNumber(), sorder.getArrangeTicket().getNumber())==false
						&& nord.getPurchaseTicket().getNumber()==null && nord.getReceiptTicket().getNumber()==null
						&& StringUtils.equals(sorder.getMonthnum(), nord.getMonthnum())==false
						&& nord.getPurchaseTicket().getNumber()==null && nord.getStPurchase()==0
						&& StringUtils.equals(nord.getArrangeTicket().getArrangeType(), new ArrangeTypeLogic().getNormal()) && nord.getSendId()==20);
			else
				Assert.assertTrue("原订单，月流水号不改变，常规可发", nord.getId()==order.getId()
						&& StringUtils.equals(sorder.getMonthnum(), nord.getMonthnum())
						&& order.getPurchaseTicket().getNumber()==null
						&& StringUtils.equals(order.getArrangeTicket().getArrangeType(), new ArrangeTypeLogic().getNormal()) && order.getSendId()==20);
			if (transCancel && sorder.getReceiptId()==0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CancelList", "purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo);
				Assert.assertTrue("原采购在途申请取消要到采购取消审核列表", this.getListViewValue().size()==detailCount);
				Assert.assertTrue("有取消人，为备货", order.getUcancel()!=null);
			}
			if (transCancel && sorder.getReceiptId()>0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ReturnList", "purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo);
				Assert.assertTrue("原采购在库申请取消要到采购退货审核列表", this.getListViewValue().size()==detailCount);
				Assert.assertTrue("有退货申请，为备货", order.getReturnTicket().getNumber()!=null && order.getUreturn()!=null);
			}
			if (transBack) {
				Assert.assertTrue("原采购转备料", StringUtils.equals(order.getPurchaseTicket().getNumber(), sorder.getPurchaseTicket().getNumber()));
			}
			if (true) {
				this.loadFormView(new SendTicketForm(), "SendList", 
						"number", number, "arrangeType", new ArrangeTypeLogic().getNormal());
				Assert.assertTrue("常规要到发货开单", this.getListViewValue().size()==detailCount);
			}
			if (sorder.getStPurchase()>0) {
				OrderDetail forder = new PurchaseTicketTest().getPurchaseList("number", sorder.getOrderTicket().getNumber(), "purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo).get(0);
				Assert.assertTrue("原采购,无订单,数量不变", StringUtils.equals(forder.getVoparam(PurchaseT.class).getPurName(), sorder.getVoparam(PurchaseT.class).getPurName())
						&& forder.getStOrder()==0
						&& StringUtils.equals(forder.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber())
						&& StringUtils.equals(forder.getSubCompany().getName(), sorder.getSubCompany().getName())
						&& forder.getAmount()==sorder.getAmount());
			}
		}
		if (type=='2' && "2普通请购".length()>0) {
			form.getOrderDetail().getArrangeTicket().setArrangeType(new DeliverTypeLogic().getCommonType());
			if (this.getModeList().contain(TestMode.Product))
				form.getOrderDetail().getPurchaseTicket().setAgent("生产车间一");
			String timeDo = this.getTimeDo();
			this.onButton("请购");
			OrderDetail order=form.getSelectFormer4Order().getLast(), sorder=order.getSnapShot();
			OrderDetail nord=order.getVoparam("NewTransTo");
			if (sorder.getStPurchase()>0)
				Assert.assertTrue("新订单，月流水号应改变，无采购可发", nord.getId()!=order.getId()
						&& StringUtils.equals(nord.getArrangeTicket().getNumber(), sorder.getArrangeTicket().getNumber())==false
						&& StringUtils.equals(sorder.getMonthnum(), nord.getMonthnum())==false
						&& nord.getPurchaseTicket().getNumber()==null
						&& StringUtils.equals(nord.getArrangeTicket().getArrangeType(), new DeliverTypeLogic().getCommonType()) && nord.getStPurchase()==0 && nord.getSendId()==0);
			else
				Assert.assertTrue("原订单，月流水号不改变，可发", nord.getId()==order.getId()
						&& StringUtils.equals(sorder.getMonthnum(), nord.getMonthnum())
						&& order.getPurchaseTicket().getNumber()==null
						&& StringUtils.equals(order.getArrangeTicket().getArrangeType(), new DeliverTypeLogic().getCommonType()) && order.getSendId()==0);
			if (transCancel && sorder.getReceiptId()==0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CancelList", "purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo);
				Assert.assertTrue("原采购在途申请取消要到采购取消审核列表", this.getListViewValue().size()==detailCount);
				Assert.assertTrue("有取消人", order.getUcancel()!=null);
		}
			if (transCancel && sorder.getReceiptId()>0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ReturnList", "purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo);
				Assert.assertTrue("原采购在库申请取消要到采购退货审核列表", this.getListViewValue().size()==detailCount);
				Assert.assertTrue("有退货申请", order.getReturnTicket().getNumber()!=null && order.getUreturn()!=null);
			}
			if (transBack) {
				Assert.assertTrue("原采购转备料", order.getStOrder()==0 && order.getSendId()==0 && StringUtils.equals(order.getPurchaseTicket().getNumber(), sorder.getPurchaseTicket().getNumber()));
			}
			if (true) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CommonList",
						"number", number);
				Assert.assertTrue("普通请购要到采购开单", this.getListViewValue().size()==detailCount);
			}
			if (sorder.getStPurchase()>0) {
				OrderDetail forder = new PurchaseTicketTest().getPurchaseList("purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo).get(0);
				Assert.assertTrue("原采购,没有订单,数量不变", StringUtils.equals(forder.getVoparam(PurchaseT.class).getPurName(), sorder.getVoparam(PurchaseT.class).getPurName())
						&& forder.getStOrder()==0
						&& StringUtils.equals(forder.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber())
						&& StringUtils.equals(forder.getSubCompany().getName(), sorder.getSubCompany().getName())
						&& forder.getAmount()==sorder.getAmount());
			}
		}
		if ("3直发请购".length()>0 && type=='3') {
			form.getOrderDetail().getArrangeTicket().setArrangeType(new DeliverTypeLogic().getDirectType());
			form.getOrderDetail().getArrangeTicket().setDeliverNote("直接发到加工厂地址");
			if (this.getModeList().contain(TestMode.Product))
				form.getOrderDetail().getPurchaseTicket().setAgent("生产车间一");
			String timeDo = this.getTimeDo();
			this.onButton("请购");
			OrderDetail order=form.getSelectFormer4Order().getLast(), sorder=order.getSnapShot();
			OrderDetail nord=order.getVoparam("NewTransTo");
			Assert.assertTrue("明细要有直发备注，直发请购", nord.getArrangeTicket().getDeliverNote()!=null
					&& StringUtils.equals(nord.getArrangeTicket().getArrangeType(), new DeliverTypeLogic().getDirectType()));
			if (sorder.getStPurchase()>0)
				Assert.assertTrue("新订单，月流水号应改变，无采购不可发", nord.getId()!=order.getId()
						&& StringUtils.equals(nord.getArrangeTicket().getNumber(), sorder.getArrangeTicket().getNumber())==false
						&& StringUtils.equals(sorder.getMonthnum(), nord.getMonthnum())==false
						&& nord.getPurchaseTicket().getNumber()==null && nord.getStPurchase()==0 && nord.getSendId()==0);
			else
				Assert.assertTrue("原订单，月流水号不改变，不可发", nord.getId()==order.getId()
						&& StringUtils.equals(nord.getArrangeTicket().getNumber(), sorder.getArrangeTicket().getNumber())==false
						&& StringUtils.equals(sorder.getMonthnum(), nord.getMonthnum())
						&& order.getPurchaseTicket().getNumber()==null && order.getSendId()==0);
			if (transCancel && sorder.getReceiptId()==0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CancelList", "purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo);
				Assert.assertTrue("原采购在途申请取消要到采购取消审核列表", this.getListViewValue().size()==detailCount);
				Assert.assertTrue("有取消人", order.getUcancel()!=null);
			}
			if (transCancel && sorder.getReceiptId()>0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ReturnList", "purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo);
				Assert.assertTrue("原采购在库申请取消要到采购退货审核列表", this.getListViewValue().size()==detailCount);
				Assert.assertTrue("有退货申请", order.getReturnTicket().getNumber()!=null && order.getUreturn()!=null);
			}
			if (transBack) {
				Assert.assertTrue("原采购转备料", order.getStOrder()==0 && order.getSendId()==0 && StringUtils.equals(order.getPurchaseTicket().getNumber(), sorder.getPurchaseTicket().getNumber()));
			}
			if (true) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "DirectList",
						"number", number);
				Assert.assertTrue("普通请购要到直发开单", this.getListViewValue().size()==detailCount);
			}
			if (sorder.getStPurchase()>0) {
				OrderDetail forder = new PurchaseTicketTest().getPurchaseList("purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo).get(0);
				Assert.assertTrue("原采购,没有订单,数量不变", StringUtils.equals(forder.getVoparam(PurchaseT.class).getPurName(), sorder.getVoparam(PurchaseT.class).getPurName())
						&& forder.getStOrder()==0
						&& StringUtils.equals(forder.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber())
						&& StringUtils.equals(forder.getSubCompany().getName(), sorder.getSubCompany().getName())
						&& forder.getAmount()==sorder.getAmount());
			}
		}
		if ("4当地购请购".length()>0 && type=='4') {
			form.getOrderDetail().getArrangeTicket().setArrangeType(new DeliverTypeLogic().getLocalType());
			form.getOrderDetail().getArrangeTicket().setDeliverNote("在加工厂当地购买");
			if (this.getModeList().contain(TestMode.Product))
				form.getOrderDetail().getPurchaseTicket().setAgent("生产车间一");
			String timeDo = this.getTimeDo();
			this.onButton("请购");
			OrderDetail order=form.getSelectFormer4Order().getLast(), sorder=order.getSnapShot();
			OrderDetail nord=order.getVoparam("NewTransTo");
			Assert.assertTrue("明细要有当地购备注，当地购请购", nord.getArrangeTicket().getDeliverNote()!=null
					&& StringUtils.equals(nord.getArrangeTicket().getArrangeType(), new DeliverTypeLogic().getLocalType()));
			if (sorder.getStPurchase()>0)
				Assert.assertTrue("新订单，月流水号应改变，无采购不可发", nord.getId()!=order.getId()
						&& StringUtils.equals(nord.getArrangeTicket().getNumber(), sorder.getArrangeTicket().getNumber())==false
						&& StringUtils.equals(sorder.getMonthnum(), nord.getMonthnum())==false
						&& nord.getPurchaseTicket().getNumber()==null && nord.getStPurchase()==0 && nord.getSendId()==0);
			else
				Assert.assertTrue("原订单，月流水号不改变，不可发", nord.getId()==order.getId()
						&& StringUtils.equals(sorder.getMonthnum(), nord.getMonthnum())
						&& order.getPurchaseTicket().getNumber()==null && order.getSendId()==0);
			if (transCancel && sorder.getReceiptId()==0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CancelList", "purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo);
				Assert.assertTrue("原采购在途申请取消要到采购取消审核列表", this.getListViewValue().size()==detailCount);
				Assert.assertTrue("有取消人", order.getUcancel()!=null);
			}
			if (transCancel && sorder.getReceiptId()>0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ReturnList", "purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo);
				Assert.assertTrue("原采购在库申请取消要到采购退货审核列表", this.getListViewValue().size()==detailCount);
				Assert.assertTrue("有退货申请", order.getReturnTicket().getNumber()!=null && order.getUreturn()!=null);
			}
			if (transBack) {
				Assert.assertTrue("原采购转备料", order.getStOrder()==0 && order.getSendId()==0 && StringUtils.equals(order.getPurchaseTicket().getNumber(), sorder.getPurchaseTicket().getNumber()));
			}
			if (true) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "LocalList",
						"number", number);
				Assert.assertTrue("普通请购要到当地购开单", this.getListViewValue().size()==detailCount);
			}
			if (sorder.getStPurchase()>0) {
				OrderDetail forder = new PurchaseTicketTest().getPurchaseList("purName", sorder.getVoparam(PurchaseT.class).getPurName(), "modifytime", timeDo).get(0);
				Assert.assertTrue("原采购,没有订单,数量不变", StringUtils.equals(forder.getVoparam(PurchaseT.class).getPurName(), sorder.getVoparam(PurchaseT.class).getPurName())
						&& forder.getStOrder()==0
						&& StringUtils.equals(forder.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber())
						&& StringUtils.equals(forder.getSubCompany().getName(), sorder.getSubCompany().getName())
						&& forder.getAmount()==sorder.getAmount());
			}
		}
	}
	
	public void check调整安排2__1终止订单_2转为备料(char type, Object[] filters0, String cancelType) {
		ArrangeTicketForm form = this.getForm();
		this.loadView("TransList", this.genFiltersStart(filters0, "canState", true));
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if (cancelType!=null)
			this.setRadioGroup(cancelType);
		boolean transBack=form.getTransBack(), transCancel=form.getTransCancel(), transNone=form.getTransNone();
		double sumAmount = (Double)this.getListFootColumn("amount").get(0);
		if ("1终止订单".length()>0 && type=='1') {
			String timeDo = this.getTimeDo();
			this.onMenu("终止订单");
			OrderDetail order=form.getSelectFormer4Order().getLast(), sorder=order.getSnapShot();
			Assert.assertTrue("订单终止", order.getStOrder()==0 && order.getSendId()==0);
			Assert.assertEquals("采购月流水号应不变", order.getMonthnum(), sorder.getMonthnum());
			Assert.assertTrue("订单月流水号前缀应不变", order.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0]));
			if (transCancel) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CancelList",
						"purName", form.getSelectFormer4Order().getFirst().getVoparam(PurchaseT.class).getPurName());
				Assert.assertTrue("原采购申请取消要到采购取消审核列表", this.getListViewValue().size()==detailCount);
			}
			if (true) {
				this.loadSqlView(new OrderTicketForm(), "ShowQuery", "DetailForm.selectedList",
						"number", order.getOrderTicket().getNumber(), "stOrder", "0");
				Assert.assertTrue("终止订单状态为0只能查看", this.getListViewValue().size()==detailCount);
			}
			if (sorder.getStPurchase()>0) {
				Assert.assertTrue("原采购,没有订单,数量不变", StringUtils.equals(order.getVoparam(PurchaseT.class).getPurName(), sorder.getVoparam(PurchaseT.class).getPurName())
						&& order.getStOrder()==0 && order.getSendId()==0
						&& StringUtils.equals(order.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber())
						&& StringUtils.equals(order.getSubCompany().getName(), sorder.getSubCompany().getName())
						&& order.getAmount()==sorder.getAmount());
			}
		}
		if ("2转为备料".length()>0 && type=='2') {
			String timeDo = this.getTimeDo();
			this.onMenu("转为备料");
			OrderDetail order=form.getSelectFormer4Order().getLast(), sorder=order.getSnapShot();
			Assert.assertTrue("订单转备料", order.getStOrder()==0 && order.getSendId()==0);
			Assert.assertEquals("采购月流水号应不变", order.getMonthnum(), sorder.getMonthnum());
			Assert.assertTrue("订单月流水号前缀应不变", order.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0]));
			if (sorder.getStPurchase()>0) {
				Assert.assertTrue("原采购,没有订单,数量不变", StringUtils.equals(order.getVoparam(PurchaseT.class).getPurName(), sorder.getVoparam(PurchaseT.class).getPurName())
						&& order.getStOrder()==0 && order.getSendId()==0
						&& StringUtils.equals(order.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber())
						&& StringUtils.equals(order.getSubCompany().getName(), sorder.getSubCompany().getName())
						&& order.getAmount()==sorder.getAmount());
			}
		}
	}
	
	public void check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单(char type, Object[] Afilters0, Object[] Bfilters0, String cancelType) {
		ArrangeTicketForm form = this.getForm();
		Object[] Afilters = this.genFiltersStart(Afilters0, "tabArrange.selectedList", "canState", true);
		this.loadView("TransList", Afilters);
		String sArrange = new StringBuffer().append(this.getListViewValue()).toString();
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.setRadioGroup(cancelType);
		boolean transBack=form.getTransBack(), transCancel=form.getTransCancel(), transNone=form.getTransNone();
		if (type=='1' && "绑定备货库存".length()>0) {
			Object[] Bfilters = this.genFiltersStart(Bfilters0, "tabUStore.selectedList");
			this.setFilters(Bfilters);
			this.setSqlAllSelect(this.getListViewValue().size());
			String sUStore = new StringBuffer().append(this.getListViewValue()).toString();
			try {
				this.onMenu("挪用订单库存");
				Assert.fail("备货不能按订单处理");
			}catch(Exception e) {
			}
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("绑定备货库存");
			Assert.assertTrue("排单有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabArrange.selectedList").getValue()).toString(), sArrange)==false);
			Assert.assertTrue("库存有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabUStore.selectedList").getValue()).toString(), sUStore)==false);
			double storeAmount=form.getFormProperty("attrMap.SplitPurchaseAmount"), orderAmount=form.getFormProperty("attrMap.SplitOrderAmount");
			String[] storeNumbers=form.getFormProperty("attrMap.SplitPurchaseNumbers"), orderNumbers=form.getFormProperty("attrMap.SplitOrderNumbers");
			String[] storeArranges=form.getFormProperty("attrMap.SplitPurchaseArranges"), orderArranges=form.getFormProperty("attrMap.SplitOrderArranges");
			if (true) {
				if (storeAmount >= orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("订单够库存应无未安排", (Double)this.getListFootColumn("amount").get(0)==null);
					this.setFilters("tabUStore.selectedList", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("订单占用库存数量=未安排时的数量", (Double)this.getListFootColumn("amount").get(0)==orderAmount);
					this.setFilters("tabUStore.selectedList", "number", storeNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					if (storeAmount-orderAmount>0)
						Assert.assertTrue("未被占用的剩余数量应在库存列表中", (Double)this.getListFootColumn("amount").get(0)==storeAmount-orderAmount);
				} else if (storeAmount < orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("订单不够库存应有剩余未安排", (Double)this.getListFootColumn("amount").get(0)==orderAmount-storeAmount);
					this.setFilters("tabUStore.selectedList", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("订单占用库存数量=库存数量", (Double)this.getListFootColumn("amount").get(0)==storeAmount);
					this.setFilters("tabUStore.selectedList", "number", storeNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("未被占用的剩余数量应为0", (Double)this.getListFootColumn("amount").get(0)==null);
				}
				if (true) {
					this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("被挪用方备货库存现为占用方订单库存", (Double)this.getListFootColumn("amount").get(0)==(storeAmount<orderAmount? storeAmount: orderAmount));
				}
				if (form.getSelectedFirst4Order().getStPurchase()>0) {
					this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", storeNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("占用方订单A采购现为订单B的备料", (Double)this.getListFootColumn("amount").get(0)==(storeAmount<orderAmount? storeAmount: orderAmount));
				}
			}
			if (true) {
				for (OrderDetail order: form.getDetailList()) {
					OrderDetail sorder = order.getSnapShot();
					OrderDetail pur=(OrderDetail)order.getVoParamMap().get("FromStore"), spur=null;
					if (pur != null) {
						OrderDetail ordA=pur, purB=order;
						spur=pur.getSnapShot();
						Assert.assertTrue("订单A挪用库存B，有安排可发货状态，用订单月流水号，用库存订单B的排单、采购，有原客户分公司", ordA.getArrangeTicket().getNumber()!=null && ordA.getSendId()==20 && ordA.getReceiptId()>=30
								&& ordA.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(ordA.getArrangeTicket().getNumber(), spur.getArrangeTicket().getNumber())
								&& StringUtils.equals(ordA.getPurchaseTicket().getNumber(), spur.getPurchaseTicket().getNumber())
								&& StringUtils.equals(ordA.getClient().getName(),sorder.getClient().getName()) && StringUtils.equals(ordA.getSubCompany().getName(),sorder.getSubCompany().getName()));
						if (sorder.getStPurchase()==0) {
							Assert.assertTrue("被挪用采购B在订单A，订单删除", purB.getId()<0);
						} else {
							Assert.assertTrue("被挪用采购B在订单A，订单失效，记订单B月流水号，有订单A在途，有原客户分公司", purB.getStPurchase()>0
									&& StringUtils.equals(purB.getOrderTicket().getNumber(), spur.getOrderTicket().getNumber()) && purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
									&& StringUtils.equals(purB.getPurchaseTicket().getNumber(), sorder.getPurchaseTicket().getNumber()) && purB.getReceiptId()==sorder.getReceiptId()
									&& StringUtils.equals(purB.getClient().getName(),spur.getClient().getName()) && StringUtils.equals(purB.getSubCompany().getName(),spur.getSubCompany().getName()));
							if (transCancel)
								Assert.assertTrue("占用方采购取消，重记为订单B", purB.getStOrder()==0 && purB.getSendId()==0
										&& (sorder.getReceiptId()<30? purB.getStPurchase()==60: purB.getStPurchase()==70));
							else if (transBack)
								Assert.assertTrue("占用方采购转备料，重记为订单B", purB.getStOrder()==0 && purB.getSendId()==0 && purB.getStPurchase()<60);
							else
								Assert.assertTrue("原采购给订单B用", purB.getStOrder()>0 && purB.getSendId()>0);
						}
					} else {
						Assert.assertTrue("不够库存订单A，为原安排，继续原月流水号", order.getId()>0 && StringUtils.equals(order.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber())
								&& StringUtils.equals(order.getArrangeTicket().getArrangeType(), sorder.getArrangeTicket().getArrangeType())
								&& StringUtils.equals(order.getMonthnum(), sorder.getMonthnum()));
					}
				}
				for (OrderDetail pur: form.getPurchaseList()) {
					Assert.assertTrue("分后的明细都有保存", pur.getId()>0);
				}
				this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
				Assert.assertTrue("挪用后要重计算库存数", this.getListViewValue().size()>0);
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("挪用后要重计算够用数", this.getListViewValue().size()>0);
			}
		}
		if (type=='2' && "挪用订单库存".length()>0) {
			Object[] Bfilters = this.genFiltersStart(Bfilters0, "tabUStore.selectedList");
			this.setFilters(Bfilters);
			this.setSqlAllSelect(this.getListViewValue().size());
			String sUStore = new StringBuffer().append(this.getListViewValue()).toString();
			try {
				this.onMenu("绑定备货库存");
				Assert.fail("订单不能按备货处理");
			}catch(Exception e) {
			}
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("挪用订单库存");
			Assert.assertTrue("排单有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabArrange.selectedList").getValue()).toString(), sArrange)==false);
			Assert.assertTrue("库存有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabUStore.selectedList").getValue()).toString(), sUStore)==false);
			double storeAmount=form.getFormProperty("attrMap.SplitPurchaseAmount"), orderAmount=form.getFormProperty("attrMap.SplitOrderAmount");
			String[] storeNumbers=form.getFormProperty("attrMap.SplitPurchaseNumbers"), orderNumbers=form.getFormProperty("attrMap.SplitOrderNumbers");
			String[] storeArranges=form.getFormProperty("attrMap.SplitPurchaseArranges"), orderArranges=form.getFormProperty("attrMap.SplitOrderArranges");
			if (true) {
				if (storeAmount >= orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("订单够库存应无未安排", (Double)this.getListFootColumn("amount").get(0)==null);
					this.setFilters("tabUStore.selectedList", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("订单占用库存数量=未安排时的数量", (Double)this.getListFootColumn("amount").get(0)==orderAmount);
					this.setFilters("tabUStore.selectedList", "number", storeNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					if (storeAmount-orderAmount>0)
						Assert.assertTrue("未被占用的剩余数量应在库存列表中", (Double)this.getListFootColumn("amount").get(0)==storeAmount-orderAmount);
				} else if (storeAmount < orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("订单不够库存应有剩余未安排", (Double)this.getListFootColumn("amount").get(0)==orderAmount-storeAmount);
					this.setFilters("tabUStore.selectedList", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("订单占用库存数量=库存数量", (Double)this.getListFootColumn("amount").get(0)==storeAmount);
					this.setFilters("tabUStore.selectedList", "number", storeNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("未被占用的剩余数量应为0", (Double)this.getListFootColumn("amount").get(0)==null);
				}
				if (true) {
					this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("被挪用方备货库存现为占用方订单库存", (Double)this.getListFootColumn("amount").get(0)==(storeAmount<orderAmount? storeAmount: orderAmount));
				}
				if (form.getSelectedFirst4Order().getStPurchase()>0) {
					this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", storeNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("占用方订单采购现为被挪用方订单的采购", (Double)this.getListFootColumn("amount").get(0)==(storeAmount<orderAmount? storeAmount: orderAmount));
				}
			}
			if (true) {
				this.loadView("TransList", "tabUStore.selectedList", "number", storeNumbers, "modifytime", timeDo);
				Assert.assertTrue("被挪用采购拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
				this.setFilters("tabUStore.selectedList", "number", orderNumbers, "modifytime", timeDo);
				Assert.assertTrue("订单拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
			}
			if (true) {
				for (OrderDetail order: form.getDetailList()) {
					OrderDetail sorder = order.getSnapShot();
					OrderDetail pur=(OrderDetail)order.getVoParamMap().get("FromStore"), spur=null;
					Assert.assertTrue("分后的明细都有保存", order.getId()>0);
					if (pur != null) {
						OrderDetail ordA=pur, purB=order;
						spur=pur.getSnapShot();
						OrderDetail nwRearrange = order.getVoparam("NewRearrange");
						Assert.assertTrue("订单A挪用库存B，有安排可发货状态，用订单A月流水号，用库存订单B的排单、采购，有原客户分公司", ordA.getArrangeTicket().getNumber()!=null && ordA.getSendId()==20 && ordA.getReceiptId()>=30
								&& StringUtils.equals(ordA.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber()) && ordA.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(ordA.getArrangeTicket().getNumber(), spur.getArrangeTicket().getNumber())
								&& StringUtils.equals(ordA.getPurchaseTicket().getNumber(), spur.getPurchaseTicket().getNumber())
								&& StringUtils.equals(ordA.getClient().getName(),sorder.getClient().getName()) && StringUtils.equals(ordA.getSubCompany().getName(),sorder.getSubCompany().getName()));
						if (sorder.getStPurchase()==0) {
							Assert.assertTrue("被挪用采购B写在订单A，订单有效无采购，记订单B月流水号，无收货", purB.getStPurchase()==0
									&& StringUtils.equals(purB.getOrderTicket().getNumber(), spur.getOrderTicket().getNumber()) && purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
									&& StringUtils.equals(purB.getReceiptTicket().getNumber(), spur.getReceiptTicket().getNumber())==false
									);
							Assert.assertTrue("订单B记在订单A，没有重排单", nwRearrange==null);
						} else {
							Assert.assertTrue("被挪用采购B写在订单A，订单有效有采购，记订单B月流水号，有订单A在途安排，有原客户分公司", purB.getStPurchase()>0
									&& StringUtils.equals(purB.getOrderTicket().getNumber(), spur.getOrderTicket().getNumber()) && purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
									&& StringUtils.equals(purB.getPurchaseTicket().getNumber(), sorder.getPurchaseTicket().getNumber()) && purB.getReceiptId()==sorder.getReceiptId() && StringUtils.equals(purB.getArrangeTicket().getNumber(), sorder.getArrangeTicket().getNumber())
									&& StringUtils.equals(purB.getClient().getName(),spur.getClient().getName()) && StringUtils.equals(purB.getSubCompany().getName(),spur.getSubCompany().getName()));
							if (transCancel)
								Assert.assertTrue("占用方采购取消，重记为订单B，订单B重排单", purB.getStOrder()==0 && purB.getSendId()==0 && (sorder.getReceiptId()<30? purB.getStPurchase()==60: purB.getStPurchase()==70)
										&& nwRearrange.getId()>0 && StringUtils.equals(nwRearrange.getOrderTicket().getNumber(), purB.getOrderTicket().getNumber()) && nwRearrange.getArrangeId()==0 && nwRearrange.getStPurchase()==0 && nwRearrange.getStOrder()==30
										);
							else if (transBack)
								Assert.assertTrue("占用方采购转备料，重记为订单B，订单B重排单", purB.getStOrder()==0 && purB.getSendId()==0 && purB.getStPurchase()<60
										&& nwRearrange.getId()>0 && StringUtils.equals(nwRearrange.getOrderTicket().getNumber(), purB.getOrderTicket().getNumber()) && nwRearrange.getArrangeId()==0 && nwRearrange.getStPurchase()==0 && nwRearrange.getStOrder()==30
										);
							else
								Assert.assertTrue("订单B用订单A采购，没有重排单", purB.getStOrder()>0 && purB.getSendId()>0 && nwRearrange==null);
						}
					} else {
						Assert.assertTrue("不够库存订单A，为原安排，继续原月流水号", StringUtils.equals(order.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber())
								&& StringUtils.equals(order.getArrangeTicket().getArrangeType(), sorder.getArrangeTicket().getArrangeType())
								&& StringUtils.equals(order.getMonthnum(), sorder.getMonthnum()));
					}
				}
			}
			if (true) {
				double purAmount = 0;
				for (OrderDetail pur: form.getPurchaseList()) {
					purAmount += pur.getAmount();
					Assert.assertTrue("分后的明细都有保存", pur.getId()>0);
				}
				this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
				Assert.assertTrue("挪用后要重计算库存数", this.getListViewValue().size()>0);
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("挪用后要重计算够用数", this.getListViewValue().size()>0);
			}
		}
		if (type=='3' && "绑定备货在途".length()>0) {
			Object[] Bfilters = this.genFiltersStart(Bfilters0, "tabUPassage.selectedList");
			this.setFilters(Bfilters);
			this.setSqlAllSelect(this.getListViewValue().size());
			String sUPassage = new StringBuffer().append(this.getListViewValue()).toString();
			try {
				this.onMenu("挪用订单在途");
				Assert.fail("备货不能按订单处理");
			}catch(Exception e) {
			}
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("绑定备货在途");
			Assert.assertTrue("排单有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabArrange.selectedList").getValue()).toString(), sArrange)==false);
			Assert.assertTrue("在途有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabUPassage.selectedList").getValue()).toString(), sUPassage)==false);
			double storeAmount=form.getFormProperty("attrMap.SplitPurchaseAmount"), orderAmount=form.getFormProperty("attrMap.SplitOrderAmount");
			String[] storeNumbers=form.getFormProperty("attrMap.SplitPurchaseNumbers"), orderNumbers=form.getFormProperty("attrMap.SplitOrderNumbers");
			String[] storeArranges=form.getFormProperty("attrMap.SplitPurchaseArranges"), orderArranges=form.getFormProperty("attrMap.SplitOrderArranges");
			if (true) {
				if (storeAmount >= orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("订单够在途应无未安排", (Double)this.getListFootColumn("amount").get(0)==null);
					this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("订单占用在途数量=未安排时的数量", (Double)this.getListFootColumn("amount").get(0)==orderAmount);
					this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					if (storeAmount-orderAmount>0)
						Assert.assertTrue("未被占用的剩余数量应在在途列表中", (Double)this.getListFootColumn("amount").get(0)==storeAmount-orderAmount);
				} else if (storeAmount < orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("订单不够在途应有剩余未安排", (Double)this.getListFootColumn("amount").get(0)==orderAmount-storeAmount);
					this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("订单占用在途数量=在途数量", (Double)this.getListFootColumn("amount").get(0)==storeAmount);
					this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("未被占用的剩余数量应为0", (Double)this.getListFootColumn("amount").get(0)==null);
				}
				if (true) {
					this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("被挪用方备货库存现为占用方订单库存", (Double)this.getListFootColumn("amount").get(0)==(storeAmount<orderAmount? storeAmount: orderAmount));
				}
				if (form.getSelectedFirst4Order().getStPurchase()>0) {
					this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", storeNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("占用方订单采购现为订单B的备料", (Double)this.getListFootColumn("amount").get(0)==(storeAmount<orderAmount? storeAmount: orderAmount));
				}
			}
			if (true) {
				for (OrderDetail order: form.getDetailList()) {
					OrderDetail sorder = order.getSnapShot();
					OrderDetail pur=(OrderDetail)order.getVoParamMap().get("FromStore"), spur=null;
					Assert.assertTrue("分后的明细都有保存", order.getId()>0);
					if (pur != null) {
						OrderDetail ordA=pur, purB=order;
						spur=pur.getSnapShot();
						Assert.assertTrue("订单A挪用库存B，有安排在途状态，用订单月流水号，用库存订单B的排单、采购，有原客户分公司", ordA.getArrangeTicket().getNumber()!=null && ordA.getStPurchase()>0 && ordA.getSendId()==10
								&& ordA.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(ordA.getArrangeTicket().getNumber(), spur.getArrangeTicket().getNumber())
								&& StringUtils.equals(ordA.getPurchaseTicket().getNumber(), spur.getPurchaseTicket().getNumber())
								&& StringUtils.equals(ordA.getClient().getName(),sorder.getClient().getName()) && StringUtils.equals(ordA.getSubCompany().getName(),sorder.getSubCompany().getName()));
						if (sorder.getStPurchase()==0) {
							Assert.assertTrue("被挪用采购B在订单A，订单失效，记订单B月流水号", purB.getStOrder()==0 && purB.getStPurchase()==0 && purB.getSendId()==0
									&& StringUtils.equals(purB.getOrderTicket().getNumber(), spur.getOrderTicket().getNumber()) && purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
									);
						} else {
							Assert.assertTrue("被挪用采购B在订单A，订单失效，记订单B月流水号，有订单A在途，有原客户分公司", purB.getStPurchase()>0
									&& StringUtils.equals(purB.getOrderTicket().getNumber(), spur.getOrderTicket().getNumber()) && purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
									&& StringUtils.equals(purB.getPurchaseTicket().getNumber(), sorder.getPurchaseTicket().getNumber()) && purB.getReceiptId()==sorder.getReceiptId()
									&& StringUtils.equals(purB.getClient().getName(),spur.getClient().getName()) && StringUtils.equals(purB.getSubCompany().getName(),spur.getSubCompany().getName()));
							if (transCancel)
								Assert.assertTrue("占用方采购取消，重记为订单B", purB.getStOrder()==0 && purB.getSendId()==0
										&& (sorder.getReceiptId()<30? purB.getStPurchase()==60: purB.getStPurchase()==70));
							else if (transBack)
								Assert.assertTrue("占用方采购转备料，重记为订单B", purB.getStOrder()==0 && purB.getSendId()==0 && purB.getStPurchase()<60);
						}
					} else {
						Assert.assertEquals("不够在途为原安排", order.getArrangeTicket().getArrangeType(), sorder.getArrangeTicket().getArrangeType());
						Assert.assertTrue("订单不库存的继续原月流水号", StringUtils.equals(order.getMonthnum(), sorder.getMonthnum()));
					}
				}
				double purAmount = 0;
				for (OrderDetail pur: form.getPurchaseList()) {
					purAmount += pur.getAmount();
					Assert.assertTrue("分后的明细都有保存", pur.getId()>0);
				}
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("挪用备货在途后要重计算够用数", this.getListViewValue().size()>0);
			}
		}
		if (type=='4' && "挪用订单在途".length()>0) {
			Object[] Bfilters = this.genFiltersStart(Bfilters0, "tabUPassage.selectedList");
			this.setFilters(Bfilters);
			this.setSqlAllSelect(this.getListViewValue().size());
			String sUPassage = new StringBuffer().append(this.getListViewValue()).toString();
			try {
				this.onMenu("绑定备货在途");
				Assert.fail("订单不能按备货处理");
			}catch(Exception e) {
			}
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("挪用订单在途");
			Assert.assertTrue("排单有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabArrange.selectedList").getValue()).toString(), sArrange)==false);
			Assert.assertTrue("在途有刷新", StringUtils.equals(new StringBuffer().append(this.getSqlListView("tabUPassage.selectedList").getValue()).toString(), sUPassage)==false);
			double storeAmount=form.getFormProperty("attrMap.SplitPurchaseAmount"), orderAmount=form.getFormProperty("attrMap.SplitOrderAmount");
			String[] storeNumbers=form.getFormProperty("attrMap.SplitPurchaseNumbers"), orderNumbers=form.getFormProperty("attrMap.SplitOrderNumbers");
			String[] storeArranges=form.getFormProperty("attrMap.SplitPurchaseArranges"), orderArranges=form.getFormProperty("attrMap.SplitOrderArranges");
			if (true) {
				if (storeAmount >= orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("订单够在途应无未安排", (Double)this.getListFootColumn("amount").get(0)==null);
					this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("订单占用在途数量=未安排时的数量", (Double)this.getListFootColumn("amount").get(0)==orderAmount);
					this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					if (storeAmount-orderAmount>0)
						Assert.assertTrue("未被占用的剩余数量应在在途列表中", (Double)this.getListFootColumn("amount").get(0)==storeAmount-orderAmount);
				} else if (storeAmount < orderAmount) {
					this.setFilters("tabArrange.selectedList", "number", orderNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("订单不够在途应有剩余未安排", (Double)this.getListFootColumn("amount").get(0)==orderAmount-storeAmount);
					this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("订单占用在途数量=在途数量", (Double)this.getListFootColumn("amount").get(0)==storeAmount);
					this.setFilters("tabUPassage.selectedList", "number", storeNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("未被占用的剩余数量应为0", (Double)this.getListFootColumn("amount").get(0)==null);
				}
				if (true) {
					this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", orderNumbers, "arrangeName", storeArranges, "modifytime", timeDo);
					Assert.assertTrue("被挪用方备货库存现为占用方订单库存", (Double)this.getListFootColumn("amount").get(0)==(storeAmount<orderAmount? storeAmount: orderAmount));
				}
				if (form.getSelectedFirst4Order().getStPurchase()>0) {
					this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", storeNumbers, "arrangeName", orderArranges, "modifytime", timeDo);
					Assert.assertTrue("占用方订单采购现为被挪用方订单的采购", (Double)this.getListFootColumn("amount").get(0)==(storeAmount<orderAmount? storeAmount: orderAmount));
				}
			}
			if (true) {
				this.loadView("TransList", "tabUPassage.selectedList", "number", storeNumbers, "modifytime", timeDo);
				Assert.assertTrue("被挪用采购拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
				this.setFilters("tabUPassage.selectedList", "number", orderNumbers, "modifytime", timeDo);
				Assert.assertTrue("订单拆分后月流水号应不重复", new HashSet<String>((List)this.getListViewColumn("monthnum")).size() == this.getListViewValue().size());
			}
			if (true) {
				for (OrderDetail order: form.getDetailList()) {
					OrderDetail sorder = order.getSnapShot();
					OrderDetail pur=(OrderDetail)order.getVoParamMap().get("FromStore"), spur=null;
					Assert.assertTrue("分后的明细都有保存", order.getId()>0);
					if (pur != null) {
						OrderDetail ordA=pur, purB=order;
						spur=pur.getSnapShot();
						OrderDetail nwRearrange = order.getVoparam("NewRearrange");
						Assert.assertTrue("订单A挪用在途B，有安排在途状态，用订单A月流水号，用库存订单B的排单、采购，有原客户分公司", ordA.getArrangeTicket().getNumber()!=null && ordA.getStPurchase()>0 && ordA.getSendId()==10
								&& StringUtils.equals(ordA.getOrderTicket().getNumber(), sorder.getOrderTicket().getNumber()) && ordA.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0])
								&& StringUtils.equals(ordA.getArrangeTicket().getNumber(), spur.getArrangeTicket().getNumber())
								&& StringUtils.equals(ordA.getPurchaseTicket().getNumber(), spur.getPurchaseTicket().getNumber())
								&& StringUtils.equals(ordA.getClient().getName(),sorder.getClient().getName()) && StringUtils.equals(ordA.getSubCompany().getName(),sorder.getSubCompany().getName()));
						if (sorder.getStPurchase()==0) {
							Assert.assertTrue("被挪用采购B写在订单A，订单有效无采购，记订单B月流水号，不用重排单", purB.getStOrder()>0 && purB.getStPurchase()==0
									&& StringUtils.equals(purB.getOrderTicket().getNumber(), spur.getOrderTicket().getNumber()) && purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
									&& nwRearrange==null
									);
						} else {
							Assert.assertTrue("被挪用采购B写在订单A，订单有效有采购，记订单B月流水号，有订单A在途安排，有原客户分公司", purB.getStPurchase()>0
									&& StringUtils.equals(purB.getOrderTicket().getNumber(), spur.getOrderTicket().getNumber()) && purB.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0])
									&& StringUtils.equals(purB.getPurchaseTicket().getNumber(), sorder.getPurchaseTicket().getNumber()) && purB.getReceiptId()==sorder.getReceiptId() && StringUtils.equals(purB.getArrangeTicket().getNumber(), sorder.getArrangeTicket().getNumber())
									&& StringUtils.equals(purB.getClient().getName(),spur.getClient().getName()) && StringUtils.equals(purB.getSubCompany().getName(),spur.getSubCompany().getName()));
							if (transCancel)
								Assert.assertTrue("占用方采购取消，重记为订单B，订单B重排单", purB.getStOrder()==0 && purB.getSendId()==0
										&& (sorder.getReceiptId()<30? purB.getStPurchase()==60: purB.getStPurchase()==70)
										&& nwRearrange.getId()>0 && StringUtils.equals(nwRearrange.getOrderTicket().getNumber(), purB.getOrderTicket().getNumber()) && nwRearrange.getArrangeId()==0 && nwRearrange.getStPurchase()==0 && nwRearrange.getStOrder()==30
										);
							else if (transBack)
								Assert.assertTrue("占用方采购转备料，重记为订单B，订单B重排单", purB.getStOrder()==0 && purB.getSendId()==0 && purB.getStPurchase()<60
										&& nwRearrange.getId()>0 && StringUtils.equals(nwRearrange.getOrderTicket().getNumber(), purB.getOrderTicket().getNumber()) && nwRearrange.getArrangeId()==0 && nwRearrange.getStPurchase()==0 && nwRearrange.getStOrder()==30
										);
							else
								Assert.assertTrue("订单B用订单A采购，不用重排单", purB.getStOrder()>0 && purB.getSendId()>0 && nwRearrange==null);
						}
					} else {
						Assert.assertEquals("不够在途为原安排", order.getArrangeTicket().getArrangeType(), sorder.getArrangeTicket().getArrangeType());
						Assert.assertTrue("订单不库存的继续原月流水号", StringUtils.equals(order.getMonthnum(), sorder.getMonthnum()));
					}
				}
			}
			if (true) {
				for (OrderDetail pur: form.getPurchaseList()) {
					Assert.assertTrue(pur.getId()>0);
				}
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("挪用订单在途后要重计算够用数", this.getListViewValue().size()>0);
			}
		}
	}
	
	public void check红冲申请处理__1通过_2不通过_3重排单(char type, String number, String cancelType) {
		ArrangeTicketForm form = this.getForm();
		this.loadView("AdjustList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.onMenu("红冲处理");
		this.setEditAllSelect(detailCount);
		if (cancelType!=null)
			this.setRadioGroup(cancelType);
		if ("1红冲通过".length()>0 && type=='1') {
			new StoreTicketTest().setQ清空();
			OrderDetail detail = form.getDetailList().get(0);
			this.onMenu("红冲通过");
			if (detail.getAmount()==0) {
				this.loadFormView(new OrderTicketForm(), "ShowQuery","DetailForm.selectedList",
						"number", number, "amount", "0");
				Assert.assertTrue("红冲通过0数量应为0", this.getListViewValue().size()==detailCount);
				this.loadFormView(this.getModeList().getSelfOrderForm(), "DoadjustList",
						"number", number, "amount", "0");
				Assert.assertTrue("红冲通过0数量应不可再红冲", this.getListViewValue().size()==0);
			} else {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("红冲通过改单后，要重计算库存够用数", this.getListViewValue().size()>0);
			}
			if (true) {
				this.loadView("AdjustList", "number", number);
				Assert.assertTrue("红冲通过失败", this.getListViewValue().size()==0);
			}
		}
		if ("2不通过驳回红冲".length()>0 && type=='2') {
			form.getDomain().setChangeRemark("不通过");
			this.onMenu("不通过驳回红冲");
			this.setFilters("number", number);
			Assert.assertTrue("红冲不通过失败", this.getListViewValue().size()==0);
			if (true) {
				this.loadFormView(this.getModeList().getSelfOrderForm(), "DoadjustList",
						"number", number);
				Assert.assertTrue("红冲排单不通过应返回红冲不通过处理", this.getListViewValue().size()==detailCount);
			}
		}
		if ("3重新排单".length()>0 && type=='3') {
			this.onMenu("重新排单");
			this.setFilters("number", number);
			Assert.assertTrue("重新排单失败", this.getListViewValue().size()==0);
			if (true) {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("重新排单应重计算库存够用数", this.getListViewValue().size()>0);
			}
			OrderDetail order=form.getDetailList().get(form.getDetailList().size()-1), sorder=order.getSnapShot();
			OrderDetail rearrange=((OrderDetail)order.getVoParamMap().get("RearrangeOrder"));
			if (StringUtils.equals("取消", cancelType)) {
				Assert.assertTrue("采购单要申请取消", order.getStPurchase()==60 && order.getStOrder()==0 && order.getSendId()==0);
				Assert.assertEquals("采购单取消申请要用原商品",
					new CommodityLogic().getPropertyChoosableLogic().toTrunk(((OrderDetail)order.getSnapShot()).getCommodity()).getTrunk(),
					new CommodityLogic().getPropertyChoosableLogic().toTrunk(order.getCommodity()).getTrunk());
				Assert.assertEquals("采购取消申请要用原月流水号", order.getMonthnum(), order.getMonthnum());
			}
			if (StringUtils.equals("转备料", cancelType)) {
				Assert.assertTrue("采购单要转备料要保持采购", order.getStPurchase()==30 && order.getStOrder()==0 && order.getSendId()==0);
				Assert.assertEquals("采购单转备料要用原商品",
						new CommodityLogic().getPropertyChoosableLogic().toTrunk(((OrderDetail)order.getSnapShot()).getCommodity()).getTrunk(),
						new CommodityLogic().getPropertyChoosableLogic().toTrunk(order.getCommodity()).getTrunk());
				Assert.assertEquals("采购转备料要用原月流水号", order.getMonthnum(), order.getMonthnum());
			}
			if (form.getNoteFormer4Order().isChangedNotesIN(order, "commodity"))
				Assert.assertTrue("重新排单订单要用新商品", StringUtils.equals(
						new CommodityLogic().getPropertyChoosableLogic().toTrunk(((OrderDetail)order.getSnapShot()).getCommodity()).getTrunk(),
						new CommodityLogic().getPropertyChoosableLogic().toTrunk(rearrange.getCommodity()).getTrunk())==false);
			if (rearrange.getAmount()==0)
				Assert.assertTrue("订单改数量为0，不用生成新订单", rearrange.getId()==0);
			else if (sorder.getStPurchase()>0 )
				Assert.assertTrue("重新排单的订单应为未安排，有采购在新订单，用新月流水号，无采购", rearrange.getArrangeId()==0
						&& rearrange!=order && StringUtils.equals(rearrange.getMonthnum(), sorder.getMonthnum())==false
						&& rearrange.getPurchaseTicket().getNumber()==null && rearrange.getReceiptTicket().getNumber()==null);
			else if (sorder.getStPurchase()==0)
				Assert.assertTrue("重新排单的订单应为未安排，无采购在原订单，用原月流水号", rearrange.getArrangeId()==0
						&& rearrange==order && StringUtils.equals(rearrange.getMonthnum(), sorder.getMonthnum()));
		}
	}
	
	public void check开红冲__1转直发_2转当地购_3转普通(char type, String number) {
		ArrangeTicketForm form = this.getForm();
		if ("1转为直发".length()>0 && type=='1') {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			try {
				this.onMenu("红冲驳回确认");
				Assert.fail("可开排单红冲，不应能驳回确认");
			}catch(Exception e) {
			}
			this.onMenu("红冲申请");
			this.setRadioGroup("直发");
			if (true) {
				Assert.assertTrue("应能看到发货备注", this.hasField("deliverNote"));
				StringBuffer fname = new StringBuffer("deliverNote");
				form.getNoteFormer4Order().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Order().getVoNoteMap(form.getDomain()).put(fname.toString(), "发到广州加工商工厂地址");
				form.getDomain().setChangeRemark("转为直发");
			}
			this.setEditAllSelect(detailCount);
			this.onMenu("提交红冲");
			for (int i=0; i<detailCount; i++, Assert.assertTrue("排单开红冲失败", form.getDetailList().get(i-1).getArrangeId()==50));
		}
		if ("2转为当地购".length()>0 && type=='2') {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			try {
				this.onMenu("红冲驳回确认");
				Assert.fail("可开排单红冲，不应能驳回确认");
			}catch(Exception e) {
			}
			this.onMenu("红冲申请");
			this.setRadioGroup("当地购");
			if (true) {
				Assert.assertTrue("应能看到发货备注", this.hasField("deliverNote"));
				StringBuffer fname = new StringBuffer("deliverNote");
				form.getNoteFormer4Order().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Order().getVoNoteMap(form.getDomain()).put(fname.toString(), "在广州加工商工厂当地购买");
				form.getDomain().setChangeRemark("转为当地购");
			}
			this.setEditAllSelect(detailCount);
			this.onMenu("提交红冲");
			for (int i=0; i<detailCount; i++, Assert.assertTrue("排单开红冲失败", form.getDetailList().get(i-1).getArrangeId()==50));
		}
		if ("3转为普通".length()>0 && type=='3') {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			try {
				this.onMenu("红冲驳回确认");
				Assert.fail("可开排单红冲，不应能驳回确认");
			}catch(Exception e) {
			}
			this.onMenu("红冲申请");
			this.setRadioGroup("普通");
			if (true) {
				Assert.assertTrue("应能看到发货备注", this.hasField("deliverNote"));
				form.getDomain().setChangeRemark("转为普通");
			}
			this.setEditAllSelect(detailCount);
			this.onMenu("提交红冲");
			for (int i=0; i<detailCount; i++, Assert.assertTrue("排单开红冲失败", form.getDetailList().get(i-1).getArrangeId()==50));
		}
	}
	
	public void check返改单申请__1排单通过_2排单不通过_3采购通过(char type, String number) {
		ArrangeTicketForm form = this.getForm();
		if ("1采购开单返排单改单，通过".length()>0 && type=='1') {
			this.loadView("RechangeList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			try {
				this.onMenu("采购改单处理");
				Assert.fail("返排单的改单申请，不应能采购改单处理");
			}catch(Exception e) {
			}
			this.onMenu("排单改单处理");
			Assert.assertTrue("要显示排单改单内容", this.hasBuilder(RadioButtonGroupBuilder.class));
			this.setEditAllSelect(detailCount);
			this.onMenu("排单改单通过");
			Assert.assertTrue("跳转到订单安排", this.hasMenu("用常规库存") && this.getListViewValue().size()==detailCount);
			if (true) {
				this.setFilters("number", number);
				Assert.assertTrue("改单申请重新排单通过跳到订单安排", this.getListViewValue().size()==detailCount);
				this.loadView("ArrangeList", "number", number);
				Assert.assertTrue("改单申请重新排单通过，应在待排单中看到", this.getListViewValue().size()==detailCount);
			}
		}
		if ("2采购开单返排单改单，不通过".length()>0 && type=='2') {
			this.loadView("RechangeList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			try {
				this.onMenu("采购改单处理");
				Assert.fail("返排单的改单申请，不应能采购改单处理");
			}catch(Exception e) {
			}
			this.onMenu("排单改单处理");
			Assert.assertTrue("要显示排单改单内容", this.hasBuilder(RadioButtonGroupBuilder.class));
			this.setEditAllSelect(detailCount);
			form.getDomain().setChangeRemark("返排单改单，不通过");
			this.onMenu("不通过驳回改单");
			if (true) {
				this.setFilters("number", number);
				Assert.assertTrue("改单申请重新排单不通过失败", this.getListViewValue().size()==0);
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CommonList",
						"number", number);
				Assert.assertTrue("改单申请重新排单不通过失败", this.getListViewValue().size()==detailCount);
			}
		}
		if ("3收货改单采购转返排单处理，通过".length()>0 && type=='3') {
			this.loadView("RechangeList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			try {
				this.onMenu("排单改单处理");
				Assert.fail("返排单的收货改单申请，不应能排单改单处理");
			}catch(Exception e) {
			}
			this.onMenu("采购改单处理");
			this.onMenu("同意处理");
			if (true) {
				this.setFilters("number", number);
				Assert.assertTrue("收货改单申请转排单通过失败", this.getListViewValue().size()==0);
				this.loadFormView(new ReceiptTicketForm(), "AuditList",
						"number", number);
				Assert.assertTrue("收货改单申请转排单通过，应在收货确认中看到", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check返改单申请采购__2不通过(String number, Map<String, Double> checkMap) {
		ArrangeTicketForm form = this.getForm();
		this.loadView("RechangeList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		try {
			this.onMenu("排单改单处理");
			Assert.fail("收货改单申请，应不能排单改单处理");
		}catch(Exception e) {
		}
		this.onMenu("采购改单处理");
		boolean change=false;
		if (true) {
			Double checkValue = null;
			if ((checkValue=checkMap.get("ReceiptAmount"))!=null) {
				StringBuffer fname = new StringBuffer("receiptAmount");
				form.getNoteFormer4Purchase().getNoteString(form.getPurchaseFirst(), fname);
				form.getNoteFormer4Purchase().getVoNoteMap(form.getPurchaseFirst()).put(fname.toString(), checkValue+"");
				change = true;
			}
			if ((checkValue=checkMap.get("BadAmount"))!=null) {
				StringBuffer fname = new StringBuffer("badAmount");
				form.getNoteFormer4Purchase().getNoteString(form.getPurchaseFirst(), fname);
				form.getNoteFormer4Purchase().getVoNoteMap(form.getPurchaseFirst()).put(fname.toString(), checkValue+"");
				change = true;
			}
		}
		if (true) {
			Double checkValue = null;
			if ((checkValue=checkMap.get("Amount"))!=null) {
				StringBuffer fname = new StringBuffer("amount");
				form.getNoteFormer4Purchase().getNoteString(form.getPurchaseFirst(), fname);
				form.getNoteFormer4Purchase().getVoNoteMap(form.getPurchaseFirst()).put(fname.toString(), checkValue+"");
				change = true;
			}
			if ((checkValue=checkMap.get("BackupAmount"))!=null) {
				form.getPurchaseFirst().getPurchaseTicket().setBackupAmount(checkValue);
			}
			if ((checkValue=checkMap.get("CancelAmount"))!=null) {
				form.getPurchaseFirst().getPurchaseTicket().setCancelAmount(checkValue);
			}
			if ((checkValue=checkMap.get("RearrangeAmount"))!=null) {
				form.getPurchaseFirst().getPurchaseTicket().setRearrangeAmount(checkValue);
			}
			if ((checkValue=checkMap.get("OverAmount"))!=null) {
				form.getPurchaseFirst().getPurchaseTicket().setOverAmount(checkValue);
			}
		}
		form.getPurchaseFirst().setChangeRemark(new StringBuffer().append("收货改单申请采购处理结果排单不同意").toString());
		this.onMenu("不同意");
		if (true) {
			Assert.assertTrue("在排单采购改单处理不同意失败", this.hasMenu("不同意")==false);
			this.setFilters("number", number);
			Assert.assertTrue("在排单采购处理收货改单申请不同意失败", this.getListViewValue().size()==0);
			for (OrderDetail pur: form.getPurchaseList()) {
				OrderDetail spur = (OrderDetail)pur.getSnapShot();
				Assert.assertTrue("订单明细应是处理完排单的状态", pur.getArrangeId()==45);
				if (change)
					Assert.assertTrue("每个改单申请明细都要有改单内容", StringUtils.equals(pur.getNotes(), spur.getNotes())==false);
				Assert.assertTrue("每个改单申请明细都要有申请原因", StringUtils.equals(pur.getChangeRemark(), spur.getChangeRemark())==false);
			}
		}
		if (true) {
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "RechangeList",
					"number", number);
			Assert.assertTrue("应返采购改单处理", this.getListViewValue().size()==detailCount);
		}
	}
	
	public void check排单红冲__1开红冲_2重开红冲_3删除红冲(char type, String number, String deliverType) {
		ArrangeTicketForm form = this.getForm();
		if (type=='1' && "1发起红冲".length()>0) {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			try {
				this.onMenu("红冲驳回确认");
				Assert.fail("未开红冲应不可驳回确认");
			}catch(Exception e) {
			}
			this.onMenu("红冲申请");
			this.setRadioGroup(deliverType);
			if ("改客户人，改商品".length()>0) {
				this.setEditAllSelect(detailCount);
				try {
					this.onMenu("删除红冲申请");
					Assert.fail("开申请页面不能删除改单申请");
				} catch(Exception e) {
				}
				StringBuffer fname = new StringBuffer("deliverNote");
				form.getNoteFormer4Order().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Order().getVoNoteMap(form.getDomain()).put(fname.toString(), "红冲转发货方式为"+deliverType);
				form.getDomain().setChangeRemark("改客户人，改商品");
			}
			this.setEditAllSelect(detailCount);
			this.onMenu("提交红冲");
			if (true) {
				this.setFilters("number", number);
				Assert.assertTrue("提交红冲失败", this.getListViewValue().size()==0);
				for (OrderDetail detail: form.getSelectFormer4Order().getSelectedList()) {
					Assert.assertTrue("明细要有红冲内容", detail.getNotes()!=null);
					Assert.assertTrue("明细要有红冲备注", detail.getChangeRemark()!=null);
				}
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "AdjustList",
						"number", number);
				Assert.assertTrue("提交红冲要到采购红冲处理", this.getListViewValue().size()==detailCount);
			}
		}
		if (type=='2' && "2重开红冲".length()>0) {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			try {
				this.onMenu("红冲申请");
				Assert.fail("驳回的红冲应不可开红冲申请");
			}catch(Exception e) {
			}
			this.onMenu("红冲驳回确认");
			if (true) {
				StringBuffer fname = new StringBuffer("deliverNote");
				form.getNoteFormer4Order().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Order().getVoNoteMap(form.getDomain()).put(fname.toString(), "1红冲转发货方式为"+deliverType);
				form.getDomain().setChangeRemark("改客户人，改商品1");
			}
			this.setEditAllSelect(detailCount);
			this.onMenu("提交红冲");
			if (true) {
				this.setFilters("number", number);
				Assert.assertTrue("提交红冲失败", this.getListViewValue().size()==0);
				for (OrderDetail detail: form.getSelectFormer4Order().getSelectedList()) {
					Assert.assertTrue("明细要有红冲内容", detail.getNotes()!=null);
					Assert.assertTrue("明细要有红冲备注", detail.getChangeRemark()!=null);
				}
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "AdjustList",
						"number", number);
				Assert.assertTrue("提交红冲要到采购红冲处理", this.getListViewValue().size()==detailCount);
			}
		}
		if (type=='3' && "3删除红冲".length()>0) {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("红冲驳回确认");
			this.setEditAllSelect(detailCount);
			this.onMenu("删除红冲申请");
			this.setFilters("number", number);
			Assert.assertTrue("删除红冲失败", this.getListViewValue().size()==detailCount);
			for (OrderDetail detail: form.getSelectFormer4Order().getSelectedList()) {
				Assert.assertTrue("明细要清空红冲内容", detail.getNotes()==null);
				Assert.assertTrue("明细要清空红冲备注", detail.getChangeRemark()==null);
			}
		}
	}
	
	public void test订单安排() {
if (1==1) {
		if ("1普通请购".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfOrderTest().get客户订单(1, 2, 3);
			this.check订单安排__1用常规库存_2请购_3直发_4当地购('2', number);
		}
		if ("2常规".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfOrderTest().get客户订单(1, 2, 3);
			this.check订单安排__1用常规库存_2请购_3直发_4当地购('1', number);
		}
		if ("3改单申请".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfOrderTest().get客户订单(1, 2, 3);
			this.check订单安排改单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
		}
		if ("11发起改单，不通过，删除改单，安排用常规库存".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfOrderTest().get客户订单(11);
			this.check订单安排改单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('1', number);
			this.check订单安排改单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('3', number);
			this.check订单安排__1用常规库存_2请购_3直发_4当地购('1', number);
		}
		if ("12发起改单，通过".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfOrderTest().get客户订单(12);
			this.check订单安排改单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('2', number);
		}
		if ("13发起改单数量0，通过".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfOrderTest().get客户订单(13);
			this.check订单安排改单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('4', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('3', number);
		}
		if ("绑定备货库存".length()>0) {
			if ("不能click挪用订单库存".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(1);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(1);
				try {
					this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
					Assert.fail("备货不能按订单处理");
				}catch(Exception e) {
				}
			}
			if ("未安排订单10，挪用备货库存订单9，剩余未安排1".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(10);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(9);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单7*3，挪用备货库存订单23，剩余备货库存2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(7, 7, 7);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(23);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单20，挪用备货库存订单6*3，剩余未安排2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(20);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(6, 6, 6);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单6*2，挪用备货库存订单5*3，剩余备货库存3".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(6, 6);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(5, 5, 5);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
		}
}
		if ("绑定订单库存".length()>0) {
			if ("不能click绑定备货库存".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(1);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(1);
				try {
					this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
					Assert.fail("订单不能按备货处理");
				}catch(Exception e) {
				}
			}
			if ("未安排订单10，挪用客户库存订单9，剩余未安排1".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(10);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(9);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单7*3，挪用客户库存订单23，剩余备货库存2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(7, 7, 7);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(23);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单20，挪用客户库存订单6*3，剩余未安排2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(20);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(6, 6, 6);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单6*2，挪用客户库存订单5*3，剩余备货库存3".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(6, 6);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(5, 5, 5);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
		}
		if ("绑定备货在途".length()>0) {
			if ("不能click挪用订单在途".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(1);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(1);
				try {
					this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
					Assert.fail("备货不能按订单处理");
				}catch(Exception e) {
				}
			}
			if ("未安排订单10，挪用备货在途订单9，剩余未安排1".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(10);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(9);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单7*3，挪用备货在途订单23，剩余备货库存2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(7, 7, 7);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(23);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单20，挪用备货在途订单6*3，剩余未安排2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(20);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(6, 6, 6);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单6*2，挪用备货在途订单5*3，剩余备货库存3".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(6, 6);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(5, 5, 5);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
		}
		if ("绑定订单在途".length()>0) {
			if ("不能click绑定备货在途".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(1);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(1);
				try {
					this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
					Assert.fail("订单不能按备货处理");
				}catch(Exception e) {
				}
			}
			if ("未安排订单10，挪用客户在途订单9，剩余未安排1".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(10);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(9);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单7*3，挪用客户在途订单23，剩余备货库存2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(7, 7, 7);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(23);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单20，挪用客户在途订单6*3，剩余未安排2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(20);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(6, 6, 6);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
			if ("未安排订单6*2，挪用客户在途订单5*3，剩余备货库存3".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfOrderTest().get客户订单(6, 6);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(5, 5, 5);
				this.check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber});
			}
		}
	}
	
	public void test拆分订单() {
		if ("在订单安排拆分订单".length()>0) {
			if ("1拆分出1，不合法".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfOrderTest().get客户订单(1, 1);
				try {
					this.check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲('1', number);
					Assert.fail("从1拆出1应失败");
				}catch(Exception e) {
				}
			}
			if ("2拆分".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfOrderTest().get客户订单(2, 2);
				this.check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲('1', number);
			}
		}
		if ("在调整安排拆分订单".length()>0) {
			if ("1拆分出1，不合法".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_常规(1, 1);
				try {
					this.check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲('2', number);
					Assert.fail("从1拆出1应失败");
				}catch(Exception e) {
				}
			}
			if ("2常规，拆分".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_常规(2, 2);
				this.check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲('2', number);
			}
			if ("2采购，拆分".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(3, 3);
				this.check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲('2', number);
			}
		}
		if ("在订单红冲拆分订单".length()>0) {
			if ("1拆分出1，不合法".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(1, 1);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				try {
					this.check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲('3', number);
					Assert.fail("从1拆出1应失败");
				}catch(Exception e) {
				}
			}
			if ("2采购，拆分".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_直发(2, 2);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲('3', number);
			}
		}
		if ("在排单红冲编辑拆分订单".length()>0) {
			if ("1拆分出1，不合法".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(1, 1);
				try {
					this.check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲('4', number);
					Assert.fail("从1拆出1应失败");
				}catch(Exception e) {
				}
			}
			if ("2采购，拆分".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(2, 2);
				this.check拆分__1订单安排_2调整安排_3红冲处理_4排单红冲('4', number);
			}
		}
	}

	public void test调整安排() {
if (1==1) {
		if ("1排单常规库存，转普通，转回常规".length()>0) {
			this.setTestStart();
			String number=this.get客户订单_常规(1, 1);
			if ("常规不能再常规".length()>0) {
				try {
					this.check调整安排1__1常规_2普通_3直发_4当地购('1', number, "未采购");
					Assert.fail("已经是常规应不能再常规");
				}catch(Exception e) {
				}
			}
			this.check调整安排1__1常规_2普通_3直发_4当地购('2', number, "未采购");
			if ("普通不能再普通".length()>0) {
				try {
					this.check调整安排1__1常规_2普通_3直发_4当地购('2', number, "未采购");
					Assert.fail("已经是普通请购，还能再普通请购吗？");
				}catch(Exception e) {
				}
			}
			this.check调整安排1__1常规_2普通_3直发_4当地购('1', number, "未采购");
		}
		if ("2排单请购未采购".length()>0) {
			if ("1普通，转直发，转当地购，转回普通".length()>0) {
				this.setTestStart();
				String number=this.get客户订单_普通(1, 1);
				this.check调整安排1__1常规_2普通_3直发_4当地购('3', number, "未采购");
				this.check调整安排1__1常规_2普通_3直发_4当地购('4', number, "未采购");
				this.check调整安排1__1常规_2普通_3直发_4当地购('2', number, "未采购");
			}
			if ("2直发，转当地购，转普通，转回直发".length()>0) {
				this.setTestStart();
				String number=this.get客户订单_直发(2, 2);
				this.check调整安排1__1常规_2普通_3直发_4当地购('4', number, "未采购");
				this.check调整安排1__1常规_2普通_3直发_4当地购('2', number, "未采购");
				this.check调整安排1__1常规_2普通_3直发_4当地购('3', number, "未采购");
			}
			if ("3当地购，转普通，转直发，转回当地购".length()>0) {
				this.setTestStart();
				String number=this.get客户订单_当地购(3, 3);
				this.check调整安排1__1常规_2普通_3直发_4当地购('2', number, "未采购");
				this.check调整安排1__1常规_2普通_3直发_4当地购('3', number, "未采购");
				this.check调整安排1__1常规_2普通_3直发_4当地购('4', number, "未采购");
			}
		}
		if ("2采购在途未收货".length()>0) {
			if ("1普通，转直发，转当地购，转回普通".length()>0 && "取消".length()>0) {
				this.setTestStart();
				String number=this.getModeList().getSelfPurchaseTest().get客户订单_普通(1, 1);
				this.check调整安排1__1常规_2普通_3直发_4当地购('3', number, "取消");
				this.setTestStart();
				this.getModeList().getSelfPurchaseTest().check直发采购__1采购_2改单重新排单('1', number);
				this.check调整安排1__1常规_2普通_3直发_4当地购('4', number, "取消");
				this.setTestStart();
				if (true) {
					OrderDetail order = new OrderTicketTest().getOrderList("number", number).get(0);
					long sid = order.getClient().getFromSellerId();
					PurchaseTicketTest ptest = this.getModeList().getSelfPurchaseTest();
					if (sid == 0) {
						ptest.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('1', number);
					} else if (sid > 0) {
						Seller dwSeller=new Seller4lLogic().getSellerById(sid), upSeller=new Seller4lLogic().getSellerById(order.getSellerId());
						this.setTransSeller(dwSeller);
						ptest.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('1', number);
						this.setTransSeller(upSeller);
						ptest.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('2', number);
					}
				}
				this.check调整安排1__1常规_2普通_3直发_4当地购('2', number, "取消");
				this.setTestStart();
				this.getModeList().getSelfPurchaseTest().check普通采购__1采购_2改单重新排单('1', number);
			}
		}
		if ("绑定备货库存".length()>0) {
			if ("用常规库存订单4*2，挪用备货库存订单3*2，剩余常规订单2".length()>0) {
				this.setTestStart();
				String Anumber = this.get客户订单_常规(4, 4);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(3, 3);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "未采购");
			}
			if ("采购在途订单10，挪用备货库存订单9，剩余未安排1".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(10);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(9);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "转备料");
			}
			if ("采购在途订单7*3，挪用备货库存订单23，剩余备货库存2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(7, 7, 7);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(23);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "取消");
			}
			if ("采购在途订单20，挪用备货库存订单6*3，剩余未安排2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(20);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(6, 6, 6);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "转备料");
			}
			if ("采购在途订单6*2，挪用备货库存订单5*3，剩余备货库存3".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(6, 6);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(5, 5, 5);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "取消");
			}
		}
		if ("绑定订单库存".length()>0) {
			if ("用常规库存订单4*2，挪用客户库存订单3*2，剩余常规订单2".length()>0) {
				this.setTestStart();
				String Anumber = this.get客户订单_常规(4, 4);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(3, 3);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "未采购");
			}
			if ("采购在途订单10，挪用客户库存订单9，剩余未安排1".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(10);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(9);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "转备料");
			}
			if ("采购在途订单7*3，挪用客户库存订单23，剩余备货库存2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(7, 7, 7);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(23);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "取消");
			}
			if ("采购在途订单20，挪用客户库存订单6*3，剩余未安排2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(20);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(6, 6, 6);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "交换采购");
			}
			if ("采购在途订单6*2，挪用客户库存订单5*3，剩余备货库存3".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(6, 6);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(5, 5, 5);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "交换采购");
			}
		}
		if ("绑定备货在途".length()>0) {
			if ("用常规库存订单4*2，挪用备货在途订单3*2，剩余常规订单2".length()>0) {
				this.setTestStart();
				String Anumber = this.get客户订单_常规(4, 4);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(3, 3);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "未采购");
			}
			if ("采购在途订单10，挪用备货在途订单9，剩余未安排1".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(10);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(9);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "转备料");
			}
			if ("采购在途订单7*3，挪用备货在途订单23，剩余备货库存2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(7, 7, 7);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(23);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "取消");
			}
			if ("采购在途订单20，挪用备货在途订单6*3，剩余未安排2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(20);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(6, 6, 6);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "转备料");
			}
			if ("采购在途订单6*2，挪用备货在途订单5*3，剩余备货库存3".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(6, 6);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(5, 5, 5);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "取消");
			}
		}
		if ("绑定订单在途".length()>0) {
			if ("用常规库存订单4*2，挪用客户在途订单3*2，剩余常规订单2".length()>0) {
				this.setTestStart();
				String Anumber = this.get客户订单_常规(4, 4);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(3, 3);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "未采购");
			}
			if ("采购在途订单10，挪用客户在途订单9，剩余未安排1".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(10);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(9);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "转备料");
			}
			if ("采购在途订单7*3，挪用客户在途订单23，剩余备货库存2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(7, 7, 7);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(23);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "取消");
			}
			if ("采购在途订单20，挪用客户在途订单6*3，剩余未安排2".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(20);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(6, 6, 6);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "交换采购");
			}
			if ("采购在途订单6*2，挪用客户在途订单5*3，剩余备货库存3".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(6, 6);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(5, 5, 5);
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber}, new Object[]{"number", Bnumber}, "交换采购");
			}
		}
}
		if ("收货未发的调整安排".length()>0) {
			if ("转常规".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(11, 11);
				try {
					this.check调整安排1__1常规_2普通_3直发_4当地购('1', number, "未采购");
					Assert.fail("收货在库应不能做为未采购");
				}catch(Exception e) {
				}
				this.check调整安排1__1常规_2普通_3直发_4当地购('1', number, "转备料");
			}
			if ("终止订单".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(11, 11);
				String arrangeType = new DeliverTypeLogic().getCommonType();
				try {
					this.check调整安排2__1终止订单_2转为备料('1', new Object[]{"number", number, "arrangeType", arrangeType}, "未采购");
					Assert.fail("收货在库应不能做为未采购");
				}catch(Exception e) {
				}
				this.check调整安排2__1终止订单_2转为备料('1', new Object[]{"number", number, "arrangeType", arrangeType}, "转备料");
			}
			if ("收货未发的调整安排".length()>0) {
				this.setTestStart();
				if ("转备料".length()>0) {
					String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(11, 11);
					this.check调整安排2__1终止订单_2转为备料('2', new Object[]{"number", number}, null);
				}
			}
			if ("直发收货完，不能调整安排".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfReceiptTest().get客户订单_直发(11, 11);
				this.loadView("TransList");
				this.setFilters("number", number);
				Assert.assertTrue("直发收货合并了发货，已发货不能调整安排", this.getListViewValue().size()==0);
			}
			if ("普通转直发".length()>0) {
				this.setTestStart();
				String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(11, 11);
				try {
					this.check调整安排1__1常规_2普通_3直发_4当地购('3', number, "未采购");
					Assert.fail("收货在库应不能做为未采购");
				}catch(Exception e) {
				}
				this.check调整安排1__1常规_2普通_3直发_4当地购('3', number, "转备料");
			}
		}
		if (true) {
			if ("绑定备货库存".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(5, 5);
				String Bnumber = this.getModeList().getSelfReceiptTest().get备货订单_普通(3, 3, 3);
				String arrangeType = new DeliverTypeLogic().getCommonType();
				try {
					this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber, "arrangeType", arrangeType}, new Object[]{"number", Bnumber}, "未采购");
					Assert.fail("收货在库应不能做为未采购");
				}catch(Exception e) {
				}
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number", Anumber, "arrangeType", arrangeType}, new Object[]{"number", Bnumber}, "转备料");
			}
			if ("挪用订单库存，两订单交换采购单".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(3, 3, 3);
				String Bnumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(5, 5);
				String arrangeType = new DeliverTypeLogic().getCommonType();
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('2', new Object[]{"number", Anumber, "arrangeType", arrangeType}, new Object[]{"number", Bnumber}, "交换采购");
			}
			if ("绑定备货在途".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(5, 5);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get备货订单_普通(3, 3, 3);
				String arrangeType = new DeliverTypeLogic().getCommonType();
				try {
					this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber, "arrangeType", arrangeType}, new Object[]{"number", Bnumber}, "未采购");
					Assert.fail("收货在库应不能做为未采购");
				}catch(Exception e) {
				}
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('3', new Object[]{"number", Anumber, "arrangeType", arrangeType}, new Object[]{"number", Bnumber}, "转备料");
			}
			if ("挪用订单在途，两订单交换采购单".length()>0) {
				this.setTestStart();
				String Anumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(3, 3, 3);
				String Bnumber = this.getModeList().getSelfPurchaseTest().get客户订单_普通(5, 5);
				String arrangeType = new DeliverTypeLogic().getCommonType();
				this.check调整安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('4', new Object[]{"number", Anumber, "arrangeType", arrangeType}, new Object[]{"number", Bnumber}, "交换采购");
			}
		}
	}
	
	public void test订单红冲() {
		ArrangeTicketForm form = this.getForm();
		if ("1安排用常规".length()>0) {
			if (true) {
				if ("1常规，订单红冲，通过".length()>0) {
					this.setTestStart();
					String number = this.get客户订单_常规(1, 1);
					this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
					this.check红冲申请处理__1通过_2不通过_3重排单('1', number, null);
				}
				if ("2常规，订单红冲，重新排单".length()>0) {
					this.setTestStart();
					String number = this.get客户订单_常规(2, 2);
					this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
					this.check红冲申请处理__1通过_2不通过_3重排单('3', number, null);
				}
				if ("3常规，订单红冲，不通过".length()>0) {
					this.setTestStart();
					String number = this.get客户订单_常规(3, 3);
					this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
					this.check红冲申请处理__1通过_2不通过_3重排单('2', number, null);
				}
				if ("4红冲取消数量，通过".length()>0) {
					this.setTestStart();
					String number = this.get客户订单_常规(4, 4);
					this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("2", number);
					this.check红冲申请处理__1通过_2不通过_3重排单('1', number, null);
				}
			}
			if ("5常规，在订单红冲复制新增明细，可常规".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_常规(5, 5);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("3", number);
				this.loadView("ArrangeList", "number", number);
				Assert.assertTrue("复制新增(已安排常规的)新订单明细应可以待排单", this.getListViewValue().size()==3);
			}
			if ("6常规，在订单红冲追加订单，可请购".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_常规(6, 6);
				this.getModeList().getSelfOrderTest().check已排单红冲__1追加订单('1', number);
				this.loadView("ArrangeList", "number", number);
				Assert.assertTrue("在订单红冲追加订单(已安排常规的）新订单明细应可以待排单", this.getListViewValue().size()==2);
			}
		}
		if ("多个中选择通过1个明细".length()>0) {
			if ("1通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_常规(1, 1);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.loadView("AdjustList", "number", number);
				this.setSqlAllSelect(2);
				this.onMenu("红冲处理");
				this.setEditAllSelect(1);
				this.onMenu("红冲通过");
				this.loadView("AdjustList", "number", number);
				Assert.assertTrue("只通过1个应还剩下1个", this.getListViewValue().size()==1);
			}
			if ("2不通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(2, 2);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.loadView("AdjustList", "number", number);
				this.setSqlAllSelect(2);
				this.onMenu("红冲处理");
				this.setEditAllSelect(1);
				this.getForm().getDomain().setChangeRemark("不通过");
				this.onMenu("不通过驳回红冲");
				this.loadView("AdjustList", "number", number);
				Assert.assertTrue("只通过1个应还剩下1个", this.getListViewValue().size()==1);
			}
			if ("3重新排单".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(3, 3);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.loadView("AdjustList", "number", number);
				this.setSqlAllSelect(2);
				this.onMenu("红冲处理");
				this.setEditAllSelect(1);
				this.onMenu("重新排单");
				this.loadView("AdjustList", "number", number);
				Assert.assertTrue("只通过1个应还剩下1个", this.getListViewValue().size()==1);
			}
		}
		if ("3排单为普通请购，订单红冲，".length()>0) {
			if ("1请购，订单红冲，通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(1, 1);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.check红冲申请处理__1通过_2不通过_3重排单('1', number, null);
			}
			if ("2请购，订单红冲，重新排单".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(2, 2);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.check红冲申请处理__1通过_2不通过_3重排单('3', number, null);
			}
			if ("3请购，订单红冲，不通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(3, 3);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.check红冲申请处理__1通过_2不通过_3重排单('2', number, null);
			}
			if ("4红冲取消数量，通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(4, 4);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("2", number);
				this.check红冲申请处理__1通过_2不通过_3重排单('1', number, null);
			}
			if ("5请购，在订单红冲复制新增明细，可常规".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(5, 5);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("3", number);
				this.loadView("ArrangeList", "number", number);
				Assert.assertTrue("复制新增(已安排请购的)新订单明细应可以待排单", this.getListViewValue().size()==3);
			}
			if ("6请购，在订单红冲追加订单，可请购".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(6, 6);
				this.getModeList().getSelfOrderTest().check已排单红冲__1追加订单('1', number);
				this.loadView("ArrangeList", "number", number);
				Assert.assertTrue("在订单红冲追加订单(已安排请购)的新订单明细应可以待排单", this.getListViewValue().size()==2);
			}
		}
		if ("在已收货未发时开红冲".length()>0) {
			if ("1采购收货，订单红冲，通过".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(1, 1);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.check红冲申请处理__1通过_2不通过_3重排单('1', number, "无影响");
			}
			if ("2采购收货，订单红冲，重新排单".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(2, 2);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.check红冲申请处理__1通过_2不通过_3重排单('3', number, "转备料");
			}
			if ("3采购收货，订单红冲，不通过".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(3, 3);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.check红冲申请处理__1通过_2不通过_3重排单('2', number, null);
			}
			if ("4已收货红冲取消数量，不能取消原采购通过，不能取消原采购重排单，转转备料走重新排单".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(4, 4);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("2", number);
				try {
					this.check红冲申请处理__1通过_2不通过_3重排单('1', number, "取消");
					Assert.fail("订单数量减少，不能走通过");
				}catch(Exception e) {
				}
				try {
					this.check红冲申请处理__1通过_2不通过_3重排单('3', number, "取消");
					Assert.fail("原采购已收货，不能取消采购");
				}catch(Exception e) {
				}
				this.check红冲申请处理__1通过_2不通过_3重排单('3', number, "转备料");
			}
			if ("5采购收货，在订单红冲复制新增明细，可常规".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(5, 5);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("3", number);
				this.loadView("ArrangeList", "number", number);
				Assert.assertTrue("复制新增(已采购收货)的新订单明细应可以待排单", this.getListViewValue().size()==3);
			}
			if ("6采购收货，在订单红冲追加订单，可请购".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(6, 6);
				this.getModeList().getSelfOrderTest().check已排单红冲__1追加订单('1', number);
				this.loadView("ArrangeList", "number", number);
				Assert.assertTrue("在订单红冲追加订单(已采购收货)的新订单明细应可以待排单", this.getListViewValue().size()==2);
			}
		}
	}
	
	public void test排单红冲编辑() {
		if ("发起红冲普通转直发通过，转当地购通过，转普通通过".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(1, 1);
			this.check排单红冲__1开红冲_2重开红冲_3删除红冲('1', number, "直发");
			this.getModeList().getSelfPurchaseTest().check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('1', number);
			this.check排单红冲__1开红冲_2重开红冲_3删除红冲('1', number, "当地购");
			this.getModeList().getSelfPurchaseTest().check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('1', number);
			this.check排单红冲__1开红冲_2重开红冲_3删除红冲('1', number, "普通");
			this.getModeList().getSelfPurchaseTest().check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('1', number);
		}
		if ("发起红冲普通转直发，不通过，重发红冲，不通过，删除红冲".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(2, 2);
			this.check排单红冲__1开红冲_2重开红冲_3删除红冲('1', number, "直发");
			this.getModeList().getSelfPurchaseTest().check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('2', number);
			this.check排单红冲__1开红冲_2重开红冲_3删除红冲('2', number, "直发");
			this.getModeList().getSelfPurchaseTest().check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('2', number);
			this.check排单红冲__1开红冲_2重开红冲_3删除红冲('3', number, "直发");
		}
	}
	
	public void test待处理() {
		if ("订单安排，改单申请不通过确认".length()>0) {
			if ("11发起改单，不通过".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfOrderTest().get客户订单(11);
				this.check订单安排改单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
				this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('1', number);
			}
		}
		if ("返排单处理".length()>0) {
			if ("采购开单申请重新排单".length()>0) {
				this.setTestStart();
				String number = new ArrangeTicketTest().get客户订单_普通(13);
				this.getModeList().getSelfPurchaseTest().check普通采购__1采购_2改单重新排单('2', number);
			}
		}
		if ("红冲申请处理".length()>0) {
			if ("3订单采购在途， 排单开红冲申请转直发，采购红冲处理取消给排单处理".length()>0) {
				this.setTestStart();
				String number=this.getModeList().getSelfPurchaseTest().get客户订单_普通(3);
				new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', number);
				this.getModeList().getSelfPurchaseTest().check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('3', number);
			}
		}
		if ("排单红冲，红冲不通过确认".length()>0) {
			if ("发起红冲普通转直发，不通过".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(2);
				this.check排单红冲__1开红冲_2重开红冲_3删除红冲('1', number, "直发");
				this.getModeList().getSelfPurchaseTest().check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('2', number);
			}
		}
	}
	
	private void temp() {
		if ("1拆分出1，不合法".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(1, 1);
			this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
		}
	}
	
	public String get客户订单_常规(int... amountList) {
		String number = this.getModeList().getSelfOrderTest().get客户订单(amountList);
		this.check订单安排__1用常规库存_2请购_3直发_4当地购('1', number);
		return number;
	}
	
	public String get客户订单_普通(int... amountList) {
		String number = this.getModeList().getSelfOrderTest().get客户订单(amountList);
		this.check订单安排__1用常规库存_2请购_3直发_4当地购('2', number);
		return number;
	}
	
	public String get客户订单_直发(int... amountList) {
		String number = this.getModeList().getSelfOrderTest().get客户订单(amountList);
		this.check订单安排__1用常规库存_2请购_3直发_4当地购('3', number);
		return number;
	}
	
	public String get客户订单_当地购(int... amountList) {
		String number = this.getModeList().getSelfOrderTest().get客户订单(amountList);
		this.check订单安排__1用常规库存_2请购_3直发_4当地购('4', number);
		return number;
	}
	
	protected void setQ清空() {
	}
}
