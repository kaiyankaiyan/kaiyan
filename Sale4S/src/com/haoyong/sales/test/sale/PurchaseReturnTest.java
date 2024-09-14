package com.haoyong.sales.test.sale;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.ReturnT;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.form.OrderTicketForm;
import com.haoyong.sales.sale.form.PurchaseReturnForm;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.test.base.AbstractTest;

public class PurchaseReturnTest extends AbstractTest<PurchaseReturnForm>{

	public PurchaseReturnTest() {
		this.setForm(new PurchaseReturnForm());
	}
	
	protected void check退货申请__1申请_2拆单3去申请_3不通过确认(char type, String purName) {
		PurchaseReturnForm form = this.getForm();
		this.loadView("ApplyList", "purName", purName);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if ("1退货申请".length()>0 && type=='1') {
			this.onButton("生成单号");
			form.getPurchaseDetail().getReturnTicket().setRemark(new StringBuffer().append("退货申请").append(new Date()).toString());
			this.onMenu("申请退货");
			Assert.assertTrue("复位为空白提货单", form.getPurchaseDetail().getReceiptTicket().getNumber()==null);
			this.loadView("ApplyList", "purName", purName);
			Assert.assertTrue("已经申请订单退货不能重复申请", this.getListViewValue().size()==0);
			OrderDetail pur=form.getSelectFormer4Purchase().getLast(), fpur=form.getPurchaseDetail().getSnapShot();
			Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==70 && pur.getVoparam(ReturnT.class).getReturnName().contains(fpur.getReturnTicket().getNumber()));
		}
		if ("2拆库存3去开退货申请".length()>0 && type=='2') {
			form.getOrderDetail().setAmount(3);
			this.onButton("拆分出数量");
			OrderDetail premain=form.getSelectFormer4Purchase().getLast(), psource=premain.getSnapShot(), pnew=(OrderDetail)premain.getVoParamMap().get("NewPurchase");
			Assert.assertTrue("采购明细拆分", premain.getAmount()==psource.getAmount()-3 && pnew.getAmount()==3 && pnew.getId()>0
					&& pnew.getStPurchase()==psource.getStPurchase() && pnew.getReceiptId()==psource.getReceiptId()
					&& new OrderTicketLogic().isSplitMonthnum(pnew.getMonthnum(), psource.getMonthnum()) && premain.getMonthnum().equals(psource.getMonthnum()));
			if (psource.getStOrder()>0) {
				OrderDetail oremain=premain, osource=oremain.getSnapShot(), onew=(OrderDetail)premain.getVoParamMap().get("NewOrder");
				Assert.assertTrue("订单明细拆分", oremain.getAmount()==psource.getAmount()-3 && onew.getAmount()==3 && onew.getId()>0
						&& onew.getStOrder()==osource.getStOrder() && onew.getArrangeId()==osource.getArrangeId() && onew.getSendId()==osource.getSendId()
						&& onew.getMonthnum().equals(pnew.getMonthnum()) && oremain.getMonthnum().equals(osource.getMonthnum()));
			}
			this.loadView("ApplyList", "purName", purName, "amount", 3);
			this.setSqlAllSelect(detailCount);
			if (true) {
				this.onButton("生成单号");
				form.getPurchaseDetail().getReturnTicket().setRemark(new StringBuffer().append("退货申请").append(new Date()).toString());
				this.onMenu("申请退货");
				Assert.assertTrue("复位为空白提货单", form.getPurchaseDetail().getReceiptTicket().getNumber()==null);
				OrderDetail pur=form.getSelectFormer4Purchase().getLast(), fpur=form.getPurchaseDetail().getSnapShot();
				Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==70 && pur.getVoparam(ReturnT.class).getReturnName().contains(fpur.getReturnTicket().getNumber()));
			}
		}
		if ("3审核不通过确认，继续为库存".length()>0 && type=='3') {
			this.onMenu("不通过确认");
			if (true) {
				OrderDetail pur=form.getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==30 && pur.getVoparam(ReturnT.class).getReturnName()!=null);
				this.loadView("ApplyList", "purName", purName, "uneditable", "isnull");
				Assert.assertTrue("可继续发起退货申请", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check退货出库__1出库_2不同意_3部分2不同意剩余同意(char type, String purName) {
		PurchaseReturnForm form = this.getForm();
		this.loadView("OutstoreList", "purName", purName);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if ("退货申请方出库".length()>0 && type=='1') {
			new StoreTicketTest().setQ清空();
			this.onMenu("退货出库");
			form.getPurchaseDetail().getReturnTicket().setDeliverNum(new StringBuffer("货运单号").append(new Date()).toString());
			Assert.assertTrue("复位退货单单头", this.getForm().getPurchaseDetail().getReturnTicket().getNumber()==null);
			if (true) {
				OrderDetail pur=this.getForm().getSelectFormer4Purchase().getLast(), spur=pur.getSnapShot(), fpur=this.getForm().getPurchaseDetail().getSnapShot();
				BillDetail bill=pur.getVoparam(BillDetail.class), diffbill=(BillDetail)pur.getVoParamMap().get("DiffBill"), sbill=bill.getSnapShot();
				Assert.assertTrue("采购库存保存退货出库信息，采购明细失效", pur.getReturnTicket().getNumber()!=null 
						&& pur.getVoparam(ReturnT.class).getReturnName()!=null
						&& pur.getReturnTicket().getReturnAmount()>0 && pur.getReturnTicket().getReturnDate()!=null
						&& StringUtils.equals(pur.getReturnTicket().getDeliverNum(), fpur.getReturnTicket().getDeliverNum()) 
						&& pur.getStPurchase()==0 && pur.getTReturn().getReturnName()!=null
						&& bill.getStBill()==0);
				if (sbill.getStBill()==30)
					Assert.assertTrue("已付款有负数应付", diffbill.getId()>0 && diffbill.getStBill()==20 && diffbill.getBillTicket().getBillDate()!=null && diffbill.getAmount()==pur.getAmount() && diffbill.getMoney()==0-pur.getPurchaseTicket().getPmoney());
				this.loadFormView(new StoreTicketForm(), "RestoreList", "selectFormer4Purchase.selectedList", "number", pur.getOrderTicket().getNumber());
				Assert.assertTrue("退货了没库存", this.getListViewValue().size()==0);
				List<StoreEnough> enoughList = new StoreTicketTest().getEnoughs("instore", ""+spur.getLocationTicket().getIn().getName(), "commName", pur.getVoparam(CommodityT.class).getCommName());
				Assert.assertTrue("退货了没够用数", enoughList.size()==0);
				OrderDetail ord=pur, sord=ord.getSnapShot();
				if (sord.getStOrder()>0)
					Assert.assertTrue("采购退货备货订单失效", ord.getStOrder()==0);
				if ("Client链接供应商家收到拒收退货入库申请".length()>0 && pur.getSupplier().getToSellerId()>0 && pur.getSupplier().getToSellerId()!=pur.getSellerId()) {
					List<String> monthnums = spur.getVoparam("WaitInstoreMonthnums");
					List<OrderDetail> upOrders = new OrderTicketTest().getOrderList(spur.getSupplier().getToSellerId(), "monthnum", monthnums.toArray(new String[0]));
					List<OrderDetail> upPurchases = this.getModeList().getSelfPurchaseTest().getPurchaseList(spur.getSupplier().getToSellerId(), "monthnum", monthnums.toArray(new String[0]));
					Assert.assertTrue("订单待拒收入库", upOrders.size()==monthnums.size() && upPurchases.size()==monthnums.size());
					OrderDetail upPur = upPurchases.get(upPurchases.size()-1);
					Assert.assertTrue("客户订单退货等待收货入库", upPur.getStPurchase()==78 && upPur.getReceiptId()==30);
				}
			}
		}
		if ("退货方出库已用出，不同意".length()>0 && type=='2') {
			this.onMenu("不同意");
			if (true) {
				OrderDetail pur = this.getForm().getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==30 && pur.getVoparam(ReturnT.class).getReturnName()!=null
						&& pur.getReturnTicket().getNumber()!=null && pur.getTReturn().getReturnName()!=null
						&& pur.getReturnTicket().getRemark()!=null);
			}
		}
		if ("部分2不同意，剩余同意".length()>0 && type=='3') {
			form.getOrderDetail().setAmount(2);
			this.onButton("拆分出数量");
			this.loadView("OutstoreList", "purName", purName);
			Assert.assertTrue("拆分出两份数量", this.getListViewValue().size()==detailCount*2);
			OrderDetail premain=form.getSelectFormer4Purchase().getLast(), pnew=(OrderDetail)premain.getVoParamMap().get("NewPurchase"), psource=premain.getSnapShot();
			BillDetail bremain=premain.getVoparam(BillDetail.class), bnew=(BillDetail)premain.getVoParamMap().get("NewBill"), bsource=bremain.getSnapShot();
			Assert.assertTrue("新拆分数原剩余数", premain.getAmount()+pnew.getAmount()==psource.getAmount() && pnew.getTReturn().getReturnName()!=null
					&& new OrderTicketLogic().isSplitMonthnum(pnew.getMonthnum(), psource.getMonthnum())
					&& bremain.getAmount()+bnew.getAmount()==psource.getAmount() && bnew.getStBill()==bsource.getStBill() && bnew.getMonthnum().equals(pnew.getMonthnum()));
			if ("3不同意".length()>0) {
				this.loadView("OutstoreList", "purName", purName, "amount", 2);
				this.setSqlAllSelect(detailCount);
				this.onMenu("不同意");
				OrderDetail pur = this.getForm().getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==30 && pur.getVoparam(ReturnT.class).getReturnName()!=null
						&& pur.getReturnTicket().getNumber()!=null
						&& pur.getReturnTicket().getRemark()!=null);
			}
			if ("剩余同意".length()>0) {
				this.loadView("OutstoreList", "purName", purName);
				this.setSqlAllSelect(detailCount);
				this.onMenu("退货出库");
				form.getPurchaseDetail().getReturnTicket().setDeliverNum(new StringBuffer("货运单号").append(new Date()).toString());
				Assert.assertTrue("复位退货单单头", this.getForm().getPurchaseDetail().getReturnTicket().getNumber()==null);
				OrderDetail pur=this.getForm().getSelectFormer4Purchase().getLast(), spur=pur.getSnapShot(), fpur=this.getForm().getPurchaseDetail().getSnapShot();
				BillDetail bill=pur.getVoparam(BillDetail.class), diffbill=(BillDetail)pur.getVoParamMap().get("DiffBill"), sbill=bill.getSnapShot();
				Assert.assertTrue("采购库存保存退货出库信息，采购明细失效", pur.getReturnTicket().getNumber()!=null 
						&& pur.getVoparam(ReturnT.class).getReturnName()!=null
						&& pur.getReturnTicket().getReturnAmount()>0 && pur.getReturnTicket().getReturnDate()!=null
						&& StringUtils.equals(pur.getReturnTicket().getDeliverNum(), fpur.getReturnTicket().getDeliverNum()) 
						&& pur.getStPurchase()==0 && pur.getTReturn().getReturnName()!=null
						&& bill.getStBill()==0);
				if (sbill.getStBill()==30)
					Assert.assertTrue("已付款有负数应付", diffbill.getId()>0 && diffbill.getStBill()==20 && diffbill.getBillTicket().getBillDate()!=null && diffbill.getAmount()==pur.getAmount() && diffbill.getMoney()==0-pur.getPurchaseTicket().getPmoney());
			}
		}
	}
	
	private void test链接客户商家采购退货() {
if (1==1) {
		if ("客户商家采购退货出库7，供应商家收到客户订单退货入库申请7，入库确认".length()>0) {
			this.setTestStart();
			String monthnum=null, purName=null;
			if (true) {
				String number = new SendTicketTest().get发货_常规(10, 10);
				this.loadFormView(new OrderTicketForm(), "ShowQuery","DetailForm.selectedList", "number", number);
				monthnum = (String)this.getListViewColumn("monthnum").get(0);
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "monthnum", monthnum);
				purName=(String)this.getListViewColumn("purName").get(0);
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', null, "purName", purName);
				new ArrangeTicketTest().check调整安排2__1终止订单_2转为备料('2', new Object[]{"purName", purName}, null);
			}
			this.check退货申请__1申请_2拆单3去申请_3不通过确认('1', purName);
			this.getModeList().getSelfPurchaseTest().check退货审核__1同意_2不同意_3拆分出3不同意剩余同意('3', purName);
			this.check退货出库__1出库_2不同意_3部分2不同意剩余同意('1', purName);
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			new OrderReturnTest().check到货确认__1收货_2有次品2_3有短货3("1", "monthnum", new StringBuffer("like ").append(monthnum).append("%").toString());
		}
}
		if ("客户商家采购4+4+4退货出库5+7，供应商家收到客户订单退货入库申请5+7，入库确认".length()>0) {
			this.setTestStart();
			String purName=null, monthnum=null;
			if (true) {
				String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(12);
				this.loadFormView(new OrderTicketForm(), "ShowQuery","DetailForm.selectedList", "number", number);
				monthnum = (String)this.getListViewColumn("monthnum").get(0);
				new SendTicketTest().check发货开单__1全发_2部分发('2', number, 4);
				new SendTicketTest().check发货开单__1全发_2部分发('2', number, 4);
				new SendTicketTest().check发货开单__1全发_2部分发('1', number, 4);
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "monthnum", monthnum);
				purName = (String)this.getListViewColumn("purName").get(0);
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('2', null, 5, "purName", purName);
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', null, "purName", purName);
				new ArrangeTicketTest().check调整安排2__1终止订单_2转为备料('2', new Object[]{"purName", purName}, null);
			}
			this.check退货申请__1申请_2拆单3去申请_3不通过确认('1', purName);
			this.getModeList().getSelfPurchaseTest().check退货审核__1同意_2不同意_3拆分出3不同意剩余同意('1', purName);
			this.check退货出库__1出库_2不同意_3部分2不同意剩余同意('1', purName);
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			new OrderReturnTest().check到货确认__1收货_2有次品2_3有短货3("1", "monthnum", new StringBuffer("like ").append(monthnum).append("%").toString());
		}
	}
	
	public void test采购退货() {
if (1==1) {
		if ("备货采购退货申请，审核通过，出库".length()>0) {
			this.setTestStart();
			String purName=this.getModeList().getSelfReceiptTest().getPur备货订单_普通(10, 10);
			this.check退货申请__1申请_2拆单3去申请_3不通过确认('1', purName);
			this.getModeList().getSelfPurchaseTest().check退货审核__1同意_2不同意_3拆分出3不同意剩余同意('1', purName);
			this.check退货出库__1出库_2不同意_3部分2不同意剩余同意('1', purName);
		}
		if ("退货申请部分退货3，审核通过，出库部分2不通过剩余通过".length()>0) {
			this.setTestStart();
			String purName=this.getModeList().getSelfReceiptTest().getPur备货订单_普通(10, 10);
			this.check退货申请__1申请_2拆单3去申请_3不通过确认('2', purName);
			this.getModeList().getSelfPurchaseTest().check退货审核__1同意_2不同意_3拆分出3不同意剩余同意('1', purName);
			this.check退货出库__1出库_2不同意_3部分2不同意剩余同意('3', purName);
		}
		if ("订单请购已收货，排单调整安排退货+转常规，采购退货审核通过，出库".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", number);
			String purName = (String)this.getListViewColumn("purName").get(0);
			new ArrangeTicketTest().check调整安排1__1常规_2普通_3直发_4当地购('1', number, "取消");
			this.getModeList().getSelfPurchaseTest().check退货审核__1同意_2不同意_3拆分出3不同意剩余同意('1', purName);
			this.check退货出库__1出库_2不同意_3部分2不同意剩余同意('1', purName);
		}
		if ("订单请购收货发起改单申请，采购处理为取消采购+重新排单，排单同意，收货确认开退货申请，采购退货审核通过，出库部分2不通过剩余通过".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(10, 10);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", number);
			HashMap<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("CancelAmount", 10d);
			checkMap.put("RearrangeAmount", 10d);
			this.getModeList().getSelfPurchaseTest().check收货返单处理__1提交改单(number, null, null, checkMap);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", number, "stPurchase", 70);
			String purName = (String)this.getListViewColumn("purName").get(0);
			this.getModeList().getSelfPurchaseTest().check退货审核__1同意_2不同意_3拆分出3不同意剩余同意('1', purName);
			this.check退货出库__1出库_2不同意_3部分2不同意剩余同意('3', purName);
		}
}
		if ("备货订单退货申请10，审核部分3不通过剩余7通过，7出库，3不通过确认".length()>0) {
			this.setTestStart();
			String purName=this.getModeList().getSelfReceiptTest().getPur备货订单_普通(10, 10);
			this.check退货申请__1申请_2拆单3去申请_3不通过确认('1', purName);
			this.getModeList().getSelfPurchaseTest().check退货审核__1同意_2不同意_3拆分出3不同意剩余同意('3', purName);
			this.check退货出库__1出库_2不同意_3部分2不同意剩余同意('1', purName);
			this.check退货申请__1申请_2拆单3去申请_3不通过确认('3', purName);
		}
		if ("客户订单不可退货".length()>0) {
			this.setTestStart();
			String number = new SendTicketTest().get可发_普通(10, 10);
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", number);
			String purName = (String)this.getListViewColumn("purName").get(0);
			this.loadView("ApplyList", "purName", purName);
			Assert.assertTrue("客户订单要用的库存，不可退货", this.getListViewValue().size()==0);
		}
	}
	
	private void temp() {
	}

	protected void setQ清空() {
	}
}