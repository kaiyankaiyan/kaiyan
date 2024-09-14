package com.haoyong.sales.test.sale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DoubleType;

import org.junit.Assert;

import com.haoyong.sales.common.domain.AbstractCommodityItem;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.CommodityTest;
import com.haoyong.sales.util.SSaleUtil;

public class StoreTicketTest extends AbstractTest<StoreTicketForm> {
	
	public StoreTicketTest() {
		this.setForm(new StoreTicketForm());
	}
	
	protected int[] check任意入库(List<OrderDetail> wantinList, String... monthnumList) {
		if (monthnumList.length==0)
			monthnumList = new String[]{"000"};
		StoreTicketForm form = this.getForm();
		form.getExtraInstoreList().clear();
		form.getExtraInstoreList().addAll(wantinList);
		double amount = 0;
		this.loadView("InExtraList", "selectedList", "monthnum", monthnumList);
		for (OrderDetail in: form.getDetailList()) {
			amount += in.getAmount();
		}
		Double fromAmount = (Double)this.getListFootColumn("amount").get(0);
		if (fromAmount==null)
			fromAmount = 0d;
		new StoreTicketTest().setQ清空();
		this.onMenu("添加备货库存action");
		if (true) {
			List<String> monthnumSaves = new ArrayList<String>();
			monthnumSaves.addAll(Arrays.asList(monthnumList));
			for (OrderDetail pur: (List<OrderDetail>)form.getFormProperty("attrMap.InstorePurchaseList")) {
				monthnumSaves.add(pur.getMonthnum());
			}
			this.setFilters("selectedList", "monthnum", monthnumSaves.toArray(new String[0]));
			Double toAmount=(Double)this.getListFootColumn("amount").get(0);
			if (toAmount==null)
				toAmount = 0d;
//			Assert.assertEquals("库存添加", new DoubleType().format(fromAmount + amount), new DoubleType().format(toAmount));
		}
		return this.checkInStoreList(this.getForm());
	}
	
	protected int[] checkInStoreList(StoreTicketForm form) {
		List<OrderDetail> saveList=(List<OrderDetail>)form.getFormProperty("attrMap.InstorePurchaseList");
		if (saveList==null)
			return new int[0];
		List<OrderDetail> updateList=(List<OrderDetail>)form.getFormProperty("attrMap.InstorePurchase4UpdateList"),
			createList=(List<OrderDetail>)form.getFormProperty("attrMap.InstorePurchase4CreateList"),
			deleteList=(List<OrderDetail>)form.getFormProperty("attrMap.InstorePurchase4DeleteList");
		double update=0d, money=0d, create=0, delete=0;
		for (Iterator<OrderDetail> iter=updateList.iterator(); iter.hasNext();) {
			OrderDetail pur = iter.next();
			pur.getReceiptTicket().setStoreMoney(pur.getAmount() * pur.getReceiptTicket().getStorePrice());
			update += pur.getAmount();
			money += pur.getReceiptTicket().getStoreMoney();
			if ((pur.getId()>0 && pur.getStPurchase()>=30 && pur.getAmount()>0
					&& pur.getPurchaseTicket().getNumber()!=null && pur.getReceiptTicket().getNumber()!=null)==false)
				"".toCharArray();
			Assert.assertTrue("入库更新库存明细，有采购收货", pur.getId()>0 && pur.getStPurchase()>=30 && pur.getAmount()>0
					&& pur.getPurchaseTicket().getNumber()!=null && pur.getReceiptTicket().getNumber()!=null);
		}
		for (Iterator<OrderDetail> iter=createList.iterator(); iter.hasNext();) {
			OrderDetail pur = iter.next();
			pur.getReceiptTicket().setStoreMoney(pur.getAmount() * pur.getReceiptTicket().getStorePrice());
			create += pur.getAmount();
			money += pur.getReceiptTicket().getStoreMoney();
			if ((pur.getId()>0 && pur.getStPurchase()>=30 && pur.getAmount()>0
					&& pur.getPurchaseTicket().getNumber()!=null && pur.getReceiptTicket().getNumber()!=null)==false)
				"".toCharArray();
			Assert.assertTrue("入库添加库存明细，有采购收货", pur.getId()>0 && pur.getStPurchase()>=30 && pur.getAmount()>0
					&& pur.getPurchaseTicket().getNumber()!=null && pur.getReceiptTicket().getNumber()!=null);
		}
		for (Iterator<OrderDetail> iter=deleteList.iterator(); iter.hasNext();) {
			OrderDetail pur=iter.next(), spur=pur.getSnapShot();
			pur.getReceiptTicket().setStoreMoney(pur.getAmount() * pur.getReceiptTicket().getStorePrice());
			delete += spur.getAmount();
			money += pur.getReceiptTicket().getStoreMoney();
			Assert.assertTrue("入库合并删除的库存明细，有采购收货", pur.getId()>0 && pur.getStPurchase()==0 && pur.getAmount()==0
					&& pur.getPurchaseTicket().getNumber()!=null && pur.getReceiptTicket().getNumber()!=null);
		}
		return new int[]{Double.valueOf(update).intValue(), Double.valueOf(create).intValue(), Double.valueOf(delete).intValue(), Double.valueOf(money).intValue()};
	}
	
	protected int[] check任意出库(List<OrderDetail> wantoutList, String... monthnumList) {
		StoreTicketForm form = this.getForm();
		form.getExtraOutstoreList().clear();
		form.getExtraOutstoreList().addAll(wantoutList);
		if (monthnumList.length==0)
			monthnumList = new String[]{"000"};
		double amount = 0;
		for (AbstractCommodityItem out: wantoutList) {
			amount += out.getAmount();
		}
		this.loadView("OutExtraList", "selectedList", "monthnum", monthnumList);
		Double fromAmount = (Double)this.getListFootColumn("amount").get(0);
		new StoreTicketTest().setQ清空();
		this.onMenu("减少备货库存action");
		if (true) {
			List<String> monthnumSaves = new ArrayList<String>(Arrays.asList(monthnumList));
			for (OrderDetail pur: (List<OrderDetail>)form.getFormProperty("attrMap.OutstorePurchaseList")) {
				monthnumSaves.add(pur.getMonthnum());
			}
			this.setFilters("selectedList", "monthnum", monthnumSaves.toArray(new String[0]));
			Double toAmount=(Double)this.getListFootColumn("amount").get(0);
			double navAmount = 0;
			for (OrderDetail nav: (List<OrderDetail>)form.getFormProperty("attrMap.OutstorePurchase4CreateList")) {
				navAmount += nav.getAmount();
			}
			if (toAmount==null)
				toAmount = 0D;
			Assert.assertEquals("库存减少", new DoubleType().format(amount+navAmount), new DoubleType().format(fromAmount - toAmount));
			if (amount>fromAmount)
				Assert.assertTrue("有负库存", navAmount<0);
		}
		return this.checkOutStoreList(this.getForm());
	}
	
	public int[] checkOutStoreList(StoreTicketForm form) {
		List<OrderDetail> saveList=(List<OrderDetail>)form.getFormProperty("attrMap.OutstorePurchaseList");
		if (saveList==null)
			return new int[0];
		List<OrderDetail> wantoutList=(List<OrderDetail>)form.getFormProperty("attrMap.ExtraOutstoreList");
		for (OrderDetail sorder: wantoutList) {
			OrderDetail ord = sorder.getVoparam("OutstoreOrder");
			LinkedHashSet<OrderDetail> orderSplits = sorder.getVoparam("OrderSplitList");
			if (orderSplits==null)
				continue;
			for (Iterator<OrderDetail> spIter=orderSplits.iterator(); spIter.hasNext();) {
				OrderDetail split=spIter.next(), fromStore=split.getVoparam("FromStore");
				Assert.assertTrue("有出库来源，来源成本价，有采购收货", split.getAmount()>0 && fromStore!=null && fromStore.getAmount()==0
						&& split.getPurchaseTicket().getNumber()!=null && split.getReceiptTicket().getNumber()!=null);
				if (spIter.hasNext()==false) {
					if ((sorder.getAmount()>0 && sorder.getReceiptTicket().getStorePrice()>0
							&& sorder.getPurchaseTicket().getNumber()!=null && sorder.getReceiptTicket().getNumber()!=null)==false)
					Assert.assertTrue("订单有成本价，有采购收货", sorder.getAmount()>0 && sorder.getReceiptTicket().getStorePrice()>0
							&& sorder.getPurchaseTicket().getNumber()!=null && sorder.getReceiptTicket().getNumber()!=null);
				}
			}
		}
		List<OrderDetail> updateList=(List<OrderDetail>)form.getFormProperty("attrMap.OutstorePurchase4UpdateList"),
			createList=(List<OrderDetail>)form.getFormProperty("attrMap.OutstorePurchase4CreateList"),
			deleteList=(List<OrderDetail>)form.getFormProperty("attrMap.OutstorePurchase4DeleteList");
		double remain=0, money=0, create=0, delete=0d;
		for (Iterator<OrderDetail> iter=updateList.iterator(); iter.hasNext();) {
			OrderDetail pur=iter.next(), spur=pur.getSnapShot();
			pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
			remain += pur.getAmount();
			money += pur.getReceiptTicket().getStoreMoney();
			if ((pur.getId()>0 && pur.getStPurchase()>=30 && pur.getReceiptId()>=30 && pur.getAmount()>0
					&& pur.getPurchaseTicket().getNumber()!=null && pur.getReceiptTicket().getNumber()!=null)==false)
				"".toCharArray();
			Assert.assertTrue("更新库存，有采购收货", pur.getId()>0 && pur.getStPurchase()>=30 && pur.getReceiptId()>=30 && pur.getAmount()>0
					&& pur.getPurchaseTicket().getNumber()!=null && pur.getReceiptTicket().getNumber()!=null);
			if (pur.getReceiptTicket().getStorePrice()>0)
				Assert.assertTrue("更新库存成本金额", pur.getReceiptTicket().getStoreMoney()>0);
		}
		for (Iterator<OrderDetail> iter=createList.iterator(); iter.hasNext();) {
			OrderDetail pur=iter.next();
			pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
			create += pur.getAmount();
			money += pur.getReceiptTicket().getStoreMoney();
			Assert.assertTrue("不够库存生成的负数库存，无采购收货，无成本", pur.getId()==0 && pur.getStPurchase()==0 && pur.getReceiptId()==0 && pur.getAmount()<0
					&& pur.getPurchaseTicket().getNumber()==null && pur.getReceiptTicket().getNumber()==null
					&& pur.getReceiptTicket().getStorePrice()==0 && pur.getReceiptTicket().getStoreMoney()==0);
		}
		for (Iterator<OrderDetail> iter=deleteList.iterator(); iter.hasNext();) {
			OrderDetail pur=iter.next(), spur=pur.getSnapShot();
			pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
			delete += spur.getAmount();
			money += pur.getReceiptTicket().getStoreMoney();
			Assert.assertTrue("出库完库存0，有采购收货", pur.getId()>0 && pur.getStPurchase()==0 && pur.getAmount()==0
					&& pur.getPurchaseTicket().getNumber()!=null && pur.getReceiptTicket().getNumber()!=null);
			Assert.assertTrue("0库存成本金额0", pur.getReceiptTicket().getStoreMoney()==0);
		}
		return new int[]{Double.valueOf(remain).intValue(), Double.valueOf(create).intValue(), Double.valueOf(delete).intValue(), Double.valueOf(money).intValue()};
	}
	
	protected int[] check台账入库(BomDetail agent, List<BomDetail> wantinList, Long... storeidList) {
		if (storeidList.length==0)
			storeidList = new Long[]{0L};
		StoreTicketForm form = this.getForm();
		form.getAgentInstoreList().clear();
		form.getAgentInstoreList().addAll(wantinList);
		form.setBomDetail(agent);
		double amount = 0;
		this.loadView("InAgentList");
		for (BomDetail in: form.getAgentList()) {
			amount += in.getBomTicket().getKeepAmount();
		}
		this.setFilters("selectFormer4Bom.selectedList", "id0", storeidList);
		Double fromAmount = (Double)this.getListFootColumn("keepAmount").get(0);
		if (fromAmount==null)
			fromAmount = 0d;
		this.onMenu("添加留用台账action");
		if (true) {
			List<Long> monthnumSaves = new ArrayList<Long>();
			monthnumSaves.addAll(Arrays.asList(storeidList));
			for (BomDetail pur: (List<BomDetail>)form.getFormProperty("attrMap.KeepBomList")) {
				monthnumSaves.add(pur.getId());
			}
			this.setFilters("selectFormer4Bom.selectedList", "id0", monthnumSaves.toArray(new Long[0]));
			Double toAmount=(Double)this.getListFootColumn("keepAmount").get(0);
			if (toAmount==null)
				toAmount = 0d;
			Assert.assertEquals("库存添加", new DoubleType().format(fromAmount + amount), new DoubleType().format(toAmount));
		}
		double remain=0, create=0, delete=0;
		for (Iterator<BomDetail> iter=((List<BomDetail>)form.getFormProperty("attrMap.KeepBom4UpdateList")).iterator(); iter.hasNext();) {
			BomDetail store=iter.next();
			remain += store.getBomTicket().getKeepAmount();
			Assert.assertTrue("更新台账", store.getId()>0 && store.getBomTicket().getKeepAmount()>0);
		}
		for (Iterator<BomDetail> iter=((List<BomDetail>)form.getFormProperty("attrMap.KeepBom4CreateList")).iterator(); iter.hasNext();) {
			BomDetail store=iter.next();
			create += store.getBomTicket().getKeepAmount();
			Assert.assertTrue("新增台账", store.getId()>0 && store.getBomTicket().getKeepAmount()>0);
		}
		for (Iterator<BomDetail> iter=((List<BomDetail>)form.getFormProperty("attrMap.KeepBom4DeleteList")).iterator(); iter.hasNext();) {
			BomDetail store=iter.next(), sStore=store.getSnapShot();
			delete += sStore.getBomTicket().getKeepAmount();
			Assert.assertTrue("被合并0台账删除", store.getBomTicket().getKeepAmount()==0);
		}
		return new int[]{Double.valueOf(remain).intValue(), Double.valueOf(create).intValue(), Double.valueOf(delete).intValue()};
	}
	
	protected int[] check台账出库(List<BomDetail> wantoutList, Long... storeidList) {
		StoreTicketForm form = this.getForm();
		form.getAgentOutstoreList().clear();
		form.getAgentOutstoreList().addAll(wantoutList);
		form.setBomDetail(wantoutList.get(0));
		if (storeidList.length==0)
			storeidList = new Long[]{0L};
		double amount = 0;
		for (BomDetail out: wantoutList) {
			amount += out.getBomTicket().getKeepAmount();
		}
		this.loadView("OutAgentList", "selectFormer4Bom.selectedList", "id0", storeidList);
		Double fromAmount = (Double)this.getListFootColumn("keepAmount").get(0);
		this.onMenu("减少留用台账action");
		if (true) {
			List<Long> monthnumSaves = new ArrayList<Long>(Arrays.asList(storeidList));
			for (BomDetail pur: (List<BomDetail>)form.getFormProperty("attrMap.KeepBomList")) {
				monthnumSaves.add(pur.getId());
			}
			this.setFilters("selectFormer4Bom.selectedList", "id0", monthnumSaves.toArray(new Long[0]));
			Double toAmount=(Double)this.getListFootColumn("keepAmount").get(0);
			double navAmount = 0;
			for (BomDetail nav: (List<BomDetail>)form.getFormProperty("attrMap.KeepBom4CreateList")) {
				navAmount += nav.getBomTicket().getKeepAmount();
			}
			if (toAmount==null)
				toAmount = 0D;
			Assert.assertEquals("库存减少", new DoubleType().format(amount+navAmount), new DoubleType().format(fromAmount - toAmount));
			if (amount>fromAmount)
				Assert.assertTrue("有负库存", navAmount<0);
		}
		double remain=0, create=0, delete=0;
		for (Iterator<BomDetail> iter=((List<BomDetail>)form.getFormProperty("attrMap.KeepBom4UpdateList")).iterator(); iter.hasNext();) {
			BomDetail store=iter.next();
			remain += store.getBomTicket().getKeepAmount();
			Assert.assertTrue("更新台账", store.getId()>0 && store.getBomTicket().getKeepAmount()>0);
		}
		for (Iterator<BomDetail> iter=((List<BomDetail>)form.getFormProperty("attrMap.KeepBom4CreateList")).iterator(); iter.hasNext();) {
			BomDetail store=iter.next();
			create += store.getBomTicket().getKeepAmount();
			Assert.assertTrue("不够出库数不保存", store.getId()==0 && store.getBomTicket().getKeepAmount()<0);
		}
		for (Iterator<BomDetail> iter=((List<BomDetail>)form.getFormProperty("attrMap.KeepBom4DeleteList")).iterator(); iter.hasNext();) {
			BomDetail store=iter.next(), sStore=store.getSnapShot();
			delete += sStore.getBomTicket().getKeepAmount();
			Assert.assertTrue("0台账删除", store.getBomTicket().getKeepAmount()==0);
		}
		return new int[]{Double.valueOf(remain).intValue(), Double.valueOf(create).intValue(), Double.valueOf(delete).intValue()};
	}
	
	public void test任意入库() {
		if ("客户订单采购收货在库，不可用".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(1, 1);
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", number);
			List<String> monthnumList = (List)this.getListViewColumn("monthnum");
			this.loadView("InExtraList");
			this.setFilters("selectedList", "monthnum", monthnumList.toArray(new String[0]));
			Assert.assertTrue("客户订单采购收货在库，不可用", this.getListViewValue().size()==0);
		}
		this.setTestStart();
		OrderDetail purchase = this.get备货库存1个();
		if ("待入库10，已有库存为空，新增库存明细10".length()>0) {
			this.setTestStart();
			List<OrderDetail> inList = new ArrayList<OrderDetail>();
			for (int amount: new int[]{10}) {
				OrderDetail in = new PurchaseTicketLogic().genClonePurchase(purchase);
				in.setAmount(amount);
				in.setPrice(20);
				in.getReceiptTicket().setStorePrice(in.getPrice());
				in.setMonthnum(new OrderTicketLogic().genMonthnum());
				inList.add(in);
			}
			int[] updateCreateDelete = this.check任意入库(inList, "000");
			Assert.assertArrayEquals("新增1库存明细", updateCreateDelete, new int[]{0,10,0, 200});
		}
		if ("待入库10*2，已有库存9，合并库存29".length()>0) {
			this.setTestStart();
			List<OrderDetail> inList = new ArrayList<OrderDetail>();
			for (int amount: new int[]{10, 10}) {
				OrderDetail in = new PurchaseTicketLogic().genClonePurchase(purchase);
				in.setCommodity(purchase.getCommodity());
				in.setAmount(amount);
				in.setPrice(10);
				in.getReceiptTicket().setStorePrice(in.getPrice());
				in.setMonthnum(new OrderTicketLogic().genMonthnum());
				inList.add(in);
			}
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{9}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.getReceiptTicket().setStorePrice(1);
				pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			int[] updateCreateDelete = this.check任意入库(inList, monthnumList.toArray(new String[0]));
			Assert.assertArrayEquals("更新1库存明细", updateCreateDelete, new int[]{29,0,0, 209});
		}
		if ("待入库10，已有库存9*2，合并1库存28，删除1多余库存记录".length()>0) {
			this.setTestStart();
			List<OrderDetail> inList = new ArrayList<OrderDetail>();
			for (int amount: new int[]{10}) {
				OrderDetail in = new PurchaseTicketLogic().genClonePurchase(purchase);
				in.setAmount(amount);
				in.setPrice(10);
				in.getReceiptTicket().setStorePrice(in.getPrice());
				in.setMonthnum(new OrderTicketLogic().genMonthnum());
				inList.add(in);
			}
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{9, 9}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.getReceiptTicket().setStorePrice(1);
				pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			int[] updateCreateDelete = this.check任意入库(inList, monthnumList.toArray(new String[0]));
			Assert.assertArrayEquals("更新1删除1库存明细", updateCreateDelete, new int[]{28,0,9, 118});
		}
		if ("无待入库，已有库存1|-1，删除2库存".length()>0) {
			this.setTestStart();
			List<OrderDetail> inList = new ArrayList<OrderDetail>();
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{1, -1}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			int[] updateCreateDelete = this.check任意入库(inList, monthnumList.toArray(new String[0]));
			Assert.assertArrayEquals("删除2库存明细", updateCreateDelete, new int[]{0,0,0, 0});
		}
		if ("待入库10，已有库存-2，负库存更新为8".length()>0) {
			this.setTestStart();
			List<OrderDetail> inList = new ArrayList<OrderDetail>();
			for (int amount: new int[]{10}) {
				OrderDetail in = new PurchaseTicketLogic().genClonePurchase(purchase);
				in.setAmount(amount);
				in.setPrice(10);
				in.getReceiptTicket().setStorePrice(in.getPrice());
				in.setMonthnum(new OrderTicketLogic().genMonthnum());
				inList.add(in);
			}
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{-2}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.getReceiptTicket().setStorePrice(1);
				pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			int[] updateCreateDelete = this.check任意入库(inList, monthnumList.toArray(new String[0]));
			Assert.assertArrayEquals("更新1库存明细", updateCreateDelete, new int[]{8,0,0, 98});
		}
	}
	
	public void test任意出库() {
		OrderDetail purchase = this.get备货库存1个();
if (1==1) {
}
		if ("客户订单采购收货在库，不可用".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(1, 1);
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery",
					"number", number);
			List<String> monthnumList = (List)this.getListViewColumn("monthnum");
			this.loadView("OutExtraList");
			this.setFilters("selectedList", "monthnum", monthnumList.toArray(new String[0]));
			Assert.assertTrue("客户订单采购收货在库，不可用", this.getListViewValue().size()==0);
		}
		if ("备货库存用完库存0，不可用".length()>0) {
			this.setTestStart();
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{0}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				pur.setStPurchase(0);
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			this.loadView("OutExtraList", "selectedList", "monthnum", monthnumList.toArray(new String[0]));
			Assert.assertTrue("备货库存用完库存0，不可用", this.getListViewValue().size()==0);
		}
		if ("待出库订单10，备货库存订单10，无剩余".length()>0) {
			this.setTestStart();
			List<OrderDetail> outList = new ArrayList<OrderDetail>();
			for (int amount: new int[]{10}) {
				OrderDetail out = new PurchaseTicketLogic().genClonePurchase(purchase);
				out.setAmount(amount);
				out.setMonthnum(new OrderTicketLogic().genMonthnum());
				outList.add(out);
			}
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{10}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.getReceiptTicket().setStorePrice(10);
				pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			int[] updateCreateDelete = this.check任意出库(outList, monthnumList.toArray(new String[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{0,0,10, 0});
		}
		if ("待出库订单10，备货库存订单9，不够库存1".length()>0) {
			this.setTestStart();
			List<OrderDetail> outList = new ArrayList<OrderDetail>();
			for (int amount: new int[]{10}) {
				OrderDetail out = new PurchaseTicketLogic().genClonePurchase(purchase);
				out.setCommodity(purchase.getCommodity());
				out.setAmount(amount);
				out.setMonthnum(new OrderTicketLogic().genMonthnum());
				outList.add(out);
			}
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{9}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.getReceiptTicket().setStorePrice(10);
				pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			int[] updateCreateDelete = this.check任意出库(outList, monthnumList.toArray(new String[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{0,-1,9, 0});
		}
		if ("待出库订单7*3，备货库存23，剩余备货库存2".length()>0) {
			this.setTestStart();
			List<OrderDetail> outList = new ArrayList<OrderDetail>();
			for (int amount: new int[]{7, 7, 7}) {
				OrderDetail out = new PurchaseTicketLogic().genClonePurchase(purchase);
				out.setAmount(amount);
				out.setMonthnum(new OrderTicketLogic().genMonthnum());
				outList.add(out);
			}
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{23}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.getReceiptTicket().setStorePrice(10);
				pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			int[] updateCreateDelete = this.check任意出库(outList, monthnumList.toArray(new String[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{2,0,0, 20});
		}
		if ("待出库订单20，备货库存订单6*3，不够库存2".length()>0) {
			this.setTestStart();
			List<OrderDetail> outList = new ArrayList<OrderDetail>();
			for (int amount: new int[]{20}) {
				OrderDetail out = new PurchaseTicketLogic().genClonePurchase(purchase);
				out.setAmount(amount);
				out.setMonthnum(new OrderTicketLogic().genMonthnum());
				outList.add(out);
			}
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{6, 6, 6}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.getReceiptTicket().setStorePrice(10);
				pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			int[] updateCreateDelete = this.check任意出库(outList, monthnumList.toArray(new String[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{0,-2,18, 0});
		}
		if ("待出库订单6*2，备货库存订单5*3，剩余备货库存3".length()>0) {
			this.setTestStart();
			List<OrderDetail> outList = new ArrayList<OrderDetail>();
			for (int amount: new int[]{6, 6}) {
				OrderDetail out = new PurchaseTicketLogic().genClonePurchase(purchase);
				out.setAmount(amount);
				out.setMonthnum(new OrderTicketLogic().genMonthnum());
				outList.add(out);
			}
			List<String> monthnumList = new ArrayList<String>();
			for (int amount: new int[]{5, 5, 5}) {
				OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
				pur.setAmount(amount);
				pur.getReceiptTicket().setStorePrice(10);
				pur.getReceiptTicket().setStoreMoney(pur.getReceiptTicket().getStorePrice() * pur.getAmount());
				pur.setMonthnum(new OrderTicketLogic().genMonthnum());
				SSaleUtil.saveOrUpdate(pur);
				monthnumList.add(pur.getMonthnum());
			}
			int[] updateCreateDelete = this.check任意出库(outList, monthnumList.toArray(new String[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{3,0,10, 30});
		}
	}
	
	public void test台账任意入库() {
		if (this.getModeList().contain(TestMode.Product)==false)
			return;
		this.setTestStart();
		BomDetail store = this.get车间台账1个();
		if ("待入库10，已有库存为空，新增库存明细10".length()>0) {
			this.setTestStart();
			List<BomDetail> inList = new ArrayList<BomDetail>();
			for (int amount: new int[]{10}) {
				BomDetail in = TicketPropertyUtil.copyProperties(store, new BomDetail());
				in.getBomTicket().setKeepAmount(amount);
				inList.add(in);
			}
			int[] updateCreateDelete = this.check台账入库(store, inList);
			Assert.assertArrayEquals("新增1库存明细", updateCreateDelete, new int[]{0,10,0});
		}
		if ("待入库10*2，已有库存9，合并库存29".length()>0) {
			this.setTestStart();
			List<BomDetail> inList = new ArrayList<BomDetail>();
			for (int amount: new int[]{10, 10}) {
				BomDetail in = TicketPropertyUtil.copyProperties(store, new BomDetail());
				in.getBomTicket().setKeepAmount(amount);
				inList.add(in);
			}
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{9}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			int[] updateCreateDelete = this.check台账入库(store, inList, storeidList.toArray(new Long[0]));
			Assert.assertArrayEquals("更新1库存明细", updateCreateDelete, new int[]{29,0,0});
		}
		if ("待入库10，已有库存9*2，合并1库存28，删除1多余库存记录".length()>0) {
			this.setTestStart();
			List<BomDetail> inList = new ArrayList<BomDetail>();
			for (int amount: new int[]{10}) {
				BomDetail in = TicketPropertyUtil.copyProperties(store, new BomDetail());
				in.getBomTicket().setKeepAmount(amount);
				inList.add(in);
			}
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{9, 9}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			int[] updateCreateDelete = this.check台账入库(store, inList, storeidList.toArray(new Long[0]));
			Assert.assertArrayEquals("更新1删除1库存明细", updateCreateDelete, new int[]{28,0,9});
		}
		if ("无待入库，已有库存1|2，删除先有库存1".length()>0) {
			this.setTestStart();
			List<BomDetail> inList = new ArrayList<BomDetail>();
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{1, 2}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			int[] updateCreateDelete = this.check台账入库(store, inList, storeidList.toArray(new Long[0]));
			Assert.assertArrayEquals("删除库存2明细", updateCreateDelete, new int[]{3,0,1});
		}
	}
	
	public void test台账任意出库() {
		if (this.getModeList().contain(TestMode.Product)==false)
			return;
		BomDetail store = this.get车间台账1个();
		this.setTestStart();
		if ("备货库存用完库存0，不可用".length()>0) {
			this.setTestStart();
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{0}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			this.loadView("OutAgentList");
			this.setFilters("selectFormer4Bom.selectedList", "id0", storeidList.toArray(new Long[0]));
			Assert.assertTrue("备货库存用完库存0，不可用", this.getListViewValue().size()==0);
		}
		if ("负数库存，不可用".length()>0) {
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{-1}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			this.loadView("OutAgentList");
			this.setFilters("selectFormer4Bom.selectedList", "id0", storeidList.toArray(new Long[0]));
			Assert.assertTrue("负数库存，不可用", this.getListViewValue().size()==0);
		}
		if ("待出库订单10，备货库存订单10，无剩余".length()>0) {
			this.setTestStart();
			List<BomDetail> outList = new ArrayList<BomDetail>();
			for (int amount: new int[]{10}) {
				BomDetail in = TicketPropertyUtil.copyProperties(store, new BomDetail());
				in.getBomTicket().setKeepAmount(amount);
				outList.add(in);
			}
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{10}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			int[] updateCreateDelete = this.check台账出库(outList, storeidList.toArray(new Long[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{0,0,10});
		}
		if ("待出库订单10，备货库存订单9，不够库存1".length()>0) {
			this.setTestStart();
			List<BomDetail> outList = new ArrayList<BomDetail>();
			for (int amount: new int[]{10}) {
				BomDetail in = TicketPropertyUtil.copyProperties(store, new BomDetail());
				in.getBomTicket().setKeepAmount(amount);
				outList.add(in);
			}
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{9}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			int[] updateCreateDelete = this.check台账出库(outList, storeidList.toArray(new Long[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{0,-1,9});
		}
		if ("待出库订单7*3，备货库存23，剩余备货库存2".length()>0) {
			this.setTestStart();
			List<BomDetail> outList = new ArrayList<BomDetail>();
			for (int amount: new int[]{7, 7, 7}) {
				BomDetail in = TicketPropertyUtil.copyProperties(store, new BomDetail());
				in.getBomTicket().setKeepAmount(amount);
				outList.add(in);
			}
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{23}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			int[] updateCreateDelete = this.check台账出库(outList, storeidList.toArray(new Long[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{2,0,0});
		}
		if ("待出库订单20，备货库存订单6*3，不够库存2".length()>0) {
			this.setTestStart();
			List<BomDetail> outList = new ArrayList<BomDetail>();
			for (int amount: new int[]{20}) {
				BomDetail in = TicketPropertyUtil.copyProperties(store, new BomDetail());
				in.getBomTicket().setKeepAmount(amount);
				outList.add(in);
			}
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{6, 6, 6}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			int[] updateCreateDelete = this.check台账出库(outList, storeidList.toArray(new Long[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{0,-2,18});
		}
		if ("待出库订单6*2，备货库存订单5*3，剩余备货库存3".length()>0) {
			this.setTestStart();
			List<BomDetail> outList = new ArrayList<BomDetail>();
			for (int amount: new int[]{6, 6}) {
				BomDetail in = TicketPropertyUtil.copyProperties(store, new BomDetail());
				in.getBomTicket().setKeepAmount(amount);
				outList.add(in);
			}
			List<Long> storeidList = new ArrayList<Long>();
			for (int amount: new int[]{5, 5, 5}) {
				BomDetail pur = TicketPropertyUtil.copyProperties(store, new BomDetail());
				pur.getBomTicket().setKeepAmount(amount);
				SSaleUtil.saveOrUpdate(pur);
				storeidList.add(pur.getId());
			}
			int[] updateCreateDelete = this.check台账出库(outList, storeidList.toArray(new Long[0]));
			Assert.assertArrayEquals(updateCreateDelete, new int[]{3,0,10});
		}
	}
	
	private void temp() {
	}
	
	protected List<StoreEnough> getEnoughs(Object... filters) {
		this.loadView("StoreEnoughQuery", filters);
		int detailCount = this.getListViewValue().size();
		if (detailCount > 0) {
			this.setSqlAllSelect(detailCount);
			this.onMenu("选择够用明细");
			return this.getForm().getSelectFormer4Enough().getSelectedList();
		}
		return new ArrayList<StoreEnough>(0);
	}
	
	protected OrderDetail get备货库存1个() {
		TestMode[] fromModes = this.getModeList().getModeList();
		this.getModeList().setMode(TestMode.Purchase);
		OrderDetail purchase = null;
		for (; purchase==null;) {
			StoreTicketForm storeForm = new StoreTicketForm();
			this.loadSqlView(storeForm, "InExtraList", "selectedList", "purName", "notnull", "receiptName", "notnull");
			if (this.getListViewValue().size()==0) {
				TestMode[] preModes = this.getModeList().removeMode(TestMode.Purchase, TestMode.Product);
				this.getModeList().addMode(TestMode.Purchase);
				String number = this.getModeList().getSelfReceiptTest().get备货订单_普通(1);
				this.getModeList().removeMode(TestMode.Purchase, TestMode.Product);
				this.getModeList().addMode(preModes);
				continue;
			}
			this.setSqlListSelect(1);
			this.onMenu("选择库存");
			purchase = storeForm.getSelectFormer4Purchase().getFirst();
		}
		purchase.getOrderTicket().setNumber(null);
		purchase.getOrderTicket().genSerialNumber();
		purchase.getPurchaseTicket().setNumber(null);
		purchase.getPurchaseTicket().genSerialNumber();
		purchase.getReceiptTicket().setNumber(null);
		purchase.getReceiptTicket().genSerialNumber();
		purchase.setMonthnum(new OrderTicketLogic().genMonthnum());
		this.getModeList().setMode(fromModes);
		return purchase;
	}
	
	protected BomDetail get车间台账1个() {
		TestMode[] fromModes = this.getModeList().getModeList();
		this.getModeList().setMode(TestMode.Product);
		BomDetail store = null;
		for (; store==null;) {
			StoreTicketForm storeForm = new StoreTicketForm();
			this.loadFormView(storeForm, "StoreAgentQuery");
			if (this.getListViewValue().size()==0) {
				String number = new PPurchaseTicketTest().get客户订单_普通(1);
				"".toCharArray();
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("12", number, new Object[0], new Object[]{
						new CommodityTest().getS黑棒(),0d,2d,0d,0d,0d,
						new CommodityTest().getC浆料(),0d,1.68d,0d,0d,0d,
						new CommodityTest().getC白棒(),0d,3d,0d,0d,1d,
						new CommodityTest().getC铁帽(),0d,4d,0d,0d,0d });
				new PReceiptTicketTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
				continue;
			}
			this.setSqlListSelect(1);
			this.onMenu("选择台账");
			store = storeForm.getSelectFormer4Bom().getFirst();
		}
		store.setMonthnum(new OrderTicketLogic().genMonthnum());
		this.getModeList().setMode(fromModes);
		return store;
	}
	
	public void setQ清空() {
		String sstore = "delete from sa_StoreItem where sellerId=?";
		String smonth = "delete from sa_StoreMonth where sellerId=?";
		String senough = "delete from sa_StoreEnough where sellerId=?";
		SSaleUtil.executeSqlUpdate(sstore, smonth, senough);
		this.getModeList().setOrderTime(new Date());
	}
}
