package com.haoyong.sales.test.sale;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.types.DateTimeType;
import net.sf.mily.types.DoubleType;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.domain.User;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.ReturnT;
import com.haoyong.sales.sale.domain.ReturnTicket;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.form.OrderReturnForm;
import com.haoyong.sales.sale.logic.OrderDoptionLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.UserTest;

public class OrderReturnTest extends AbstractTest<OrderReturnForm> {
	
	public OrderReturnTest() {
		this.setForm(new OrderReturnForm());
	}
	
	protected void check退货申请__1申请_2拆单3_3不通过确认(char type, Storehouse in, Object... filters) {
		OrderReturnForm form = this.getForm();
		if ("退货申请".length()>0 && type=='1') {
			this.loadView("ApplyList", filters);
			this.onButton("生成单号");
			if (in != null) {
				this.onButton("选择仓库");
				this.setFilters("ShowQuery.selectedList", "name", in.getName());
				this.setSqlListSelect(1);
				this.onMenu("确定");
				Assert.assertTrue("退货指定回货仓", StringUtils.equals(in.getName(), form.getPurchaseDetail().getReturnTicket().getInName())
						&& StringUtils.equals(in.getName(), form.getPurchaseDetail().getLocationTicket().getIn().getName()));
			}
			form.getPurchaseDetail().getReturnTicket().setRemark(new StringBuffer().append("退货申请").append(new Date()).toString());
			this.setFilters(filters);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("申请退货");
			Assert.assertTrue("复位为空白提货单", form.getPurchaseDetail().getReceiptTicket().getNumber()==null);
			this.loadView("ApplyList", filters);
			Assert.assertTrue("已经申请订单退货不能重复申请", this.getListViewValue().size()==0);
			OrderDetail pur=form.getSelectFormer4Purchase().getLast(), fpur=form.getPurchaseDetail().getSnapShot();
			Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==70 && pur.getVoparam(ReturnT.class).getReturnName().contains(fpur.getReturnTicket().getNumber())
					&& pur.getReturnTicket().getReturnDate()==null);
			if (in != null)
				Assert.assertTrue("退货指定回货仓", StringUtils.equals(in.getName(), pur.getReturnTicket().getInName())
						&& StringUtils.equals(in.getName(), pur.getLocationTicket().getIn().getName()));
		}
		if ("拆库存3去开退货申请".length()>0 && type=='2') {
			this.loadView("ApplyList", filters);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			form.getOrderDetail().setAmount(3);
			this.onButton("拆分出数量");
			OrderDetail premain=form.getSelectFormer4Purchase().getLast(), spremain=premain.getSnapShot(), psource=premain.getSnapShot(), pnew=(OrderDetail)premain.getVoParamMap().get("NewPurchase");
			Assert.assertTrue("采购明细拆分", premain.getAmount()==psource.getAmount()-3 && pnew.getAmount()==3 && pnew.getId()>0
					&& pnew.getStPurchase()==psource.getStPurchase() && pnew.getReceiptId()==psource.getReceiptId()
					&& new OrderTicketLogic().isSplitMonthnum(pnew.getMonthnum(), psource.getMonthnum()) && premain.getMonthnum().equals(psource.getMonthnum()));
			if (spremain.getStOrder()>0) {
				Assert.assertTrue("订单明细拆分", premain.getAmount()==psource.getAmount()-3 && pnew.getAmount()==3 && pnew.getId()>0
						&& pnew.getStOrder()==psource.getStOrder() && pnew.getArrangeId()==psource.getArrangeId() && pnew.getSendId()==psource.getSendId()
						&& pnew.getMonthnum().equals(pnew.getMonthnum()) && premain.getMonthnum().equals(psource.getMonthnum()));
			}
		}
		if ("审核不通过确认，继续为库存".length()>0 && type=='3') {
			this.loadView("ApplyList", filters);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("不通过确认");
			if (true) {
				OrderDetail pur=form.getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==30 && pur.getVoparam(ReturnT.class).getReturnName()!=null);
			}
		}
	}
	
	protected void check拒收退货申请__1申请_2拆单3(char type, Storehouse in, Object... filters) {
		OrderReturnForm form = this.getForm();
		this.loadView("ApplyList", filters);
		this.onMenu("显示待收货确认的采购单");
		this.setFilters(filters);
		if ("退货申请".length()>0 && type=='1') {
			this.onButton("生成单号");
			if (in != null) {
				this.onButton("选择仓库");
				this.setFilters("ShowQuery.selectedList", "name", in.getName());
				this.setSqlListSelect(1);
				this.onMenu("确定");
				Assert.assertTrue("退货指定回货仓", StringUtils.equals(in.getName(), form.getPurchaseDetail().getReturnTicket().getInName())
						&& StringUtils.equals(in.getName(), form.getPurchaseDetail().getLocationTicket().getIn().getName()));
			}
			form.getPurchaseDetail().getReturnTicket().setRemark(new StringBuffer().append("退货申请").append(new Date()).toString());
			this.setFilters(filters);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("申请退货");
			Assert.assertTrue("复位为空白提货单", form.getPurchaseDetail().getReceiptTicket().getNumber()==null);
			this.loadView("ApplyList", filters);
			Assert.assertTrue("已经申请订单退货不能重复申请", this.getListViewValue().size()==0);
			OrderDetail pur=form.getSelectFormer4Purchase().getLast(), fpur=form.getPurchaseDetail().getSnapShot();
			Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==70 && pur.getVoparam(ReturnT.class).getReturnName().contains(fpur.getReturnTicket().getNumber())
					&& pur.getReturnTicket().getReturnDate()==null);
			if (in != null)
				Assert.assertTrue("退货指定回货仓", StringUtils.equals(in.getName(), pur.getReturnTicket().getInName())
						&& StringUtils.equals(in.getName(), pur.getLocationTicket().getIn().getName()));
		}
		if ("拆库存3去开退货申请".length()>0 && type=='2') {
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			form.getOrderDetail().setAmount(3);
			this.onButton("拆分出数量");
			OrderDetail premain=form.getSelectFormer4Purchase().getLast(), spremain=premain.getSnapShot(), psource=premain.getSnapShot(), pnew=(OrderDetail)premain.getVoParamMap().get("NewPurchase");
			Assert.assertTrue("采购明细拆分", premain.getAmount()==psource.getAmount()-3 && pnew.getAmount()==3 && pnew.getId()>0
					&& pnew.getStPurchase()==psource.getStPurchase() && pnew.getReceiptId()==psource.getReceiptId()
					&& new OrderTicketLogic().isSplitMonthnum(pnew.getMonthnum(), psource.getMonthnum()) && premain.getMonthnum().equals(psource.getMonthnum()));
			if (spremain.getStOrder()>0) {
				OrderDetail oremain=premain, osource=oremain.getSnapShot(), onew=(OrderDetail)premain.getVoParamMap().get("NewOrder");
				Assert.assertTrue("订单明细拆分", oremain.getAmount()==psource.getAmount()-3 && onew.getAmount()==3 && onew.getId()>0
						&& onew.getStOrder()==osource.getStOrder() && onew.getArrangeId()==osource.getArrangeId() && onew.getSendId()==osource.getSendId()
						&& onew.getMonthnum().equals(pnew.getMonthnum()) && oremain.getMonthnum().equals(osource.getMonthnum()));
			}
		}
	}
	
	protected void check退货审核__1通过_2不通过(char type, Object... filters) {
		this.loadView("AuditList", filters);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if ("退货公司审核通过".length()>0 && type=='1') {
			this.onMenu("审核通过");
			if (true) {
				OrderDetail pur = this.getForm().getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购库存保存退货单信息", pur.getVoparam(ReturnT.class).getReturnName()!=null && pur.getStPurchase()==75
						&& pur.getReturnTicket().getReturnDate()==null);
			}
		}
		if ("退货公司审核，不通过".length()>0 && type=='2') {
			this.getForm().getPurchaseDetail().getReturnTicket().setRemark(new StringBuffer("审核不通过").append(new Date()).toString());
			this.onMenu("不通过");
			Assert.assertTrue("复位不通过原因", this.getForm().getPurchaseDetail().getRemark()==null);
			if (true) {
				OrderDetail pur=this.getForm().getSelectFormer4Purchase().getLast(), fpur=this.getForm().getPurchaseDetail().getSnapShot();
				Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==72 && pur.getVoparam(ReturnT.class).getReturnName()!=null
						&& pur.getReturnTicket().getRemark().contains(fpur.getReturnTicket().getRemark())
						&& pur.getReturnTicket().getReturnDate()==null);
			}
		}
	}
	
	protected void check退货出库__1出库_2不同意(char type, Object... filters) {
		this.loadView("OutstoreList", this.genFiltersStart(filters, "tabOutstore.selfForm.selectedList"));
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if ("退货申请方出库".length()>0 && type=='1') {
			this.onMenu("出库");
			this.getForm().getPurchaseDetail().getReturnTicket().setDeliverNum(new DateTimeType().format(new Date()));
			this.getForm().getPurchaseDetail().getReturnTicket().setRemark(new StringBuffer("退货出库").append(new Date()).toString());
			this.onMenu("提交出库");
			Assert.assertTrue("复位退货单单头", this.getForm().getPurchaseDetail().getReturnTicket().getNumber()==null);
			if (true) {
				OrderDetail pur=this.getForm().getSelectFormer4Purchase().getLast(), fpur=this.getForm().getPurchaseDetail().getSnapShot();
				Assert.assertTrue("采购库存保存退货出库信息", pur.getReturnTicket().getNumber()!=null 
						&& pur.getVoparam(ReturnT.class).getReturnName()!=null
						&& StringUtils.equals(pur.getReturnTicket().getRemark(), fpur.getReturnTicket().getRemark()) 
						&& pur.getStPurchase()==78 && pur.getReturnTicket().getReturnDate()==null);
			}
		}
		if ("退货方出库已用出，不同意".length()>0 && type=='2') {
			this.onMenu("不同意");
			if (true) {
				OrderDetail pur = this.getForm().getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购库存保存退货单信息", pur.getStPurchase()==30 && pur.getVoparam(ReturnT.class).getReturnName()!=null
						&& pur.getReturnTicket().getNumber()!=null
						&& pur.getReturnTicket().getRemark()!=null && pur.getReturnTicket().getReturnDate()==null);
			}
		}
	}
	
	public void check已发拒收退货__1申请退货_2拆出3备退_3收到退货(String type, Storehouse in, String number) {
		OrderReturnForm form = this.getForm();
		if ("已发货的订单，拆出3个预备退".length()>0 && type.contains("2")) {
			this.loadView("OutstoreList", "selectFormer4Order.selectedList", "number", number, "amount", ">=3");
			this.setSqlAllSelect(this.getListViewValue().size());
			form.getOrderDetail().setAmount(3);
			this.onButton("拆分出数量");
			Assert.assertTrue("拆分数清理", form.getOrderDetail().getAmount()==0);
			OrderDetail order=form.getSelectFormer4Order().getLast(), sorder=order.getSnapShot(), rejectOrder=(OrderDetail)order.getVoParamMap().get("NewOrder");
			Assert.assertTrue("原订单数有收下数，订单数减少，没退货信息", order.getStOrder()>=30 && order.getAmount()+3==sorder.getAmount() && order.getVoparam(ReturnT.class).getReturnName()==null 
					&& rejectOrder.getAmount()==3 && rejectOrder.getVoparam(ReturnT.class).getReturnName()==null 
					&& StringUtils.equals(order.getMonthnum(), sorder.getMonthnum()) && StringUtils.equals(rejectOrder.getMonthnum(), sorder.getMonthnum())==false);
			Assert.assertTrue("拒收订单用新分支月流水号", new OrderTicketLogic().isSplitMonthnum(rejectOrder.getMonthnum(), sorder.getMonthnum()));
			OrderDetail pur=order, spur=pur.getSnapShot();
			if (spur.getStPurchase()>0)
				Assert.assertTrue("采购退货中", pur.getStPurchase()==78);
			BillDetail bill=order.getVoparam(BillDetail.class), sbill=bill.getSnapShot(), rejectBill=(BillDetail)order.getVoParamMap().get("NewBill");
			Assert.assertTrue("总数量不变，流水号不同", bill.getStBill()==rejectBill.getStBill() && bill.getAmount()+rejectBill.getAmount()==sbill.getAmount() && StringUtils.equals(bill.getMonthnum(), rejectBill.getMonthnum())==false);
			if (order.getOrderTicket().getDoption()==null)
				Assert.assertTrue("应付金额减少", bill.getStBill()==sbill.getStBill() && bill.getBillTicket().getDiffMoney()==0 && bill.getMoney()+rejectBill.getMoney()==sbill.getMoney());
			else if (new OrderDoptionLogic().isPresent(order.getOrderTicket().getDoption()))
				Assert.assertTrue("应付赠送额减少", bill.getStBill()==sbill.getStBill() && bill.getMoney()==0 && bill.getBillTicket().getDiffMoney()+rejectBill.getBillTicket().getDiffMoney()==sbill.getBillTicket().getDiffMoney());
		}
		if ("对要验货的仓申请退货".length()>0 && type.contains("1")) {
			this.loadView("OutstoreList", "selectFormer4Order.selectedList");
			this.onButton("生成单号");
			boolean Is物流拒收=false;
			if (in != null) {
				this.setFieldText("inName", in.getName());
				Assert.assertTrue("退货指定回货仓", StringUtils.equals(in.getName(), form.getPurchaseDetail().getReturnTicket().getInName())
						&& StringUtils.equals(in.getName(), form.getPurchaseDetail().getLocationTicket().getIn().getName()));
				if ("物流包裹拒收调用时，写标记".length()>0 && StringUtils.equals("物流包裹拒收", in.getLinker())) {
					form.getPurchaseDetail().getLocationTicket().getIn().setRemark(in.getLinker());
					form.getPurchaseDetail().getReturnTicket().setRemark(in.getLinker());
					Is物流拒收 = true;
				}
			}
			form.getPurchaseDetail().getReturnTicket().setDeliverNum(new DateTimeType().format(new Date()));
			if (Is物流拒收==false)
				form.getPurchaseDetail().getReturnTicket().setRemark(new StringBuffer().append("退货申请").append(new Date()).toString());
			if (type.contains("2"))
				this.setFilters("selectFormer4Order.selectedList", "number", number, "amount", 3);
			else
				this.setFilters("selectFormer4Order.selectedList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("申请退货");
			Assert.assertTrue("退货单清理", this.getForm().getPurchaseDetail().getReturnTicket().getNumber()==null);
			OrderDetail order=form.getSelectFormer4Order().getLast();
			OrderDetail pur=order;
			Assert.assertTrue("订单数待仓验货", order.getInsfId()==(Is物流拒收? 0:20));
			Assert.assertTrue("采购记退货，待收货", pur.getStPurchase()==78 && pur.getReturnTicket().getNumber()!=null && pur.getVoparam(ReturnT.class).getReturnName()!=null && pur.getAmount()>0 && pur.getReturnTicket().getReturnAmount()>0);
			BillDetail bill=order.getVoparam(BillDetail.class);
			if (bill!=null) {
				BillDetail sbill=bill.getSnapShot();
				Assert.assertTrue("应付状态不变", bill.getStBill()==sbill.getStBill() && bill.getMoney()==sbill.getMoney());
			}
		}
		if ("已发货的全部数量退回".length()>0 && type.contains("3")) {
			this.loadView("OutstoreList", "selectFormer4Order.selectedList");
			this.onButton("生成单号");
			boolean Is物流拒收=false;
			if (in != null) {
				this.setFieldText("inName", in.getName());
				Assert.assertTrue("退货指定回货仓", StringUtils.equals(in.getName(), form.getPurchaseDetail().getReturnTicket().getInName())
						&& StringUtils.equals(in.getName(), form.getPurchaseDetail().getLocationTicket().getIn().getName()));
				if ("物流包裹拒收调用时，写标记".length()>0 && StringUtils.equals("物流包裹拒收", in.getLinker())) {
					form.getPurchaseDetail().getLocationTicket().getIn().setLinker(in.getLinker());
					form.getPurchaseDetail().getReturnTicket().setRemark(in.getLinker());
					Is物流拒收 = true;
				}
			}
			form.getPurchaseDetail().getReturnTicket().setDeliverNum(new DateTimeType().format(new Date()));
			if (Is物流拒收==false)
				form.getPurchaseDetail().getReturnTicket().setRemark(new StringBuffer().append("退货申请").append(new Date()).toString());
			if (type.contains("2"))
				this.setFilters("selectFormer4Order.selectedList", "number", number, "amount", 3);
			else
				this.setFilters("selectFormer4Order.selectedList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("收到退货");
			Assert.assertTrue("退货单清理", this.getForm().getPurchaseDetail().getReturnTicket().getNumber()==null);
			OrderDetail order=form.getSelectFormer4Order().getLast(), sorder=order.getSnapShot();
			Assert.assertTrue("订单失效，有退货信息", order.getStOrder()==0 && order.getAmount()==sorder.getAmount() 
					&& order.getReturnTicket().getReturnAmount()==sorder.getAmount() && order.getReturnTicket().getReturnDate()!=null && order.getVoparam(ReturnT.class).getReturnName()!=null);
			BillDetail bill=order.getVoparam(BillDetail.class), sbill=(bill==null? null: (BillDetail)bill.getSnapShot()), rejectBill=order.getVoparam("RejectBill");
			if (sbill==null) {
			} else if (sbill.getStBill()==20) {
				Assert.assertTrue("未付款应付失效", bill.getStBill()==0 && bill.getMoney()==sbill.getMoney());
			} else if (sbill.getStBill()==30) {
				Assert.assertTrue("已付款负数待处理应收", bill.getStBill()==30 && rejectBill.getId()>0 && rejectBill.getStBill()==20 && rejectBill.getMoney()==0-sbill.getMoney() && rejectBill.getBillTicket().getDiffMoney()==0-sbill.getBillTicket().getDiffMoney());
			}
			StoreEnough enough = new StoreTicketTest().getEnoughs("modifytime", timeDo).get(0);
			Assert.assertTrue("拒收数入库库存添加", enough.getStoreAmount()>0);
		}
	}
	
	public void check到货确认__1收货_2有次品2_3有短货3(String type, Object... filters) {
		this.loadView("ReceiptList", filters);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.onMenu("确认到货");
		for (OrderDetail pur: this.getForm().getSelectFormer4Purchase().getSelectedList()) {
			pur.getReceiptTicket().setReceiptAmount(pur.getAmount());
		}
		this.setEditAllSelect(detailCount);
		if ("退货数短货，小于原定数量，少了3个".length()>0 && type.contains("3")) {
			for (OrderDetail pur: this.getForm().getSelectFormer4Purchase().getSelectedList()) {
				pur.getReceiptTicket().setReceiptAmount(pur.getReceiptTicket().getReceiptAmount()-3);
			}
			this.onMenu("减少退货数");
			this.setEditAllSelect(detailCount);
			Assert.assertTrue("退货明细行数不变", this.getListViewValue().size()==detailCount);
			List<OrderDetail> purchaseList=((ArrayList<OrderDetail>)this.getForm().getFormProperty("attrMap.PreviousPurchaseList"));
			OrderDetail purchase=purchaseList.get(purchaseList.size()-1), purNew=purchase.getVoparam("NewPurchase"), spur=purchase.getSnapShot();
			Assert.assertTrue("退货数减少了", purNew.getAmount()==3 && purNew.getReceiptTicket().getReceiptAmount()==3 && StringUtils.equals(purchase.getMonthnum(), purNew.getMonthnum())==false
					&& purchase.getAmount()+purNew.getAmount()==spur.getAmount() && purchase.getReceiptTicket().getReceiptAmount()+purNew.getReceiptTicket().getReceiptAmount()==spur.getAmount());
			BillDetail bill=purchase.getVoparam(BillDetail.class);
			if (bill!=null) {
				BillDetail billNew=purchase.getVoparam("NewBill"), billAll=bill.getSnapShot();
				Assert.assertTrue("应付减少了", StringUtils.equals(billNew.getMonthnum(), purNew.getMonthnum())
						&& bill.getAmount()+billNew.getAmount()==billAll.getAmount()
						&& bill.getMoney()+billNew.getMoney()==billAll.getMoney()
						&& bill.getBillTicket().getDiffMoney()+billNew.getBillTicket().getDiffMoney()==billAll.getBillTicket().getDiffMoney());
			}
		}
		if ("退货到货数量中，其中有2个次品".length()>0 && type.contains("2")) {
			for (OrderDetail pur: this.getForm().getSelectFormer4Purchase().getSelectedList()) {
				pur.getReceiptTicket().setBadAmount(2);
			}
			this.onMenu("次品数拆单");
			Assert.assertTrue("拆成2倍明细", this.getListViewValue().size()==detailCount*2);
			this.setEditAllSelect(detailCount * 2);
			if (true) {
				List<OrderDetail> purchaseList=(ArrayList<OrderDetail>)this.getForm().getFormProperty("attrMap.PreviousPurchaseList");
				OrderDetail purGood=purchaseList.get(purchaseList.size()-1), purBad=purGood.getVoparam("NewPurchase"), spur=purGood.getSnapShot();
				BillDetail billGood=purGood.getVoparam(BillDetail.class), billBad=purGood.getVoparam("NewBill"), sbill=billGood==null? null: (BillDetail)billGood.getSnapShot();
				if ("正品".length()>0)
					Assert.assertTrue("是正品", purGood.getVoparam(CommodityT.class).getTrunk().contains("次品")==false);
				if ("次品".length()>0)
					Assert.assertTrue("是次品", purBad.getVoparam(CommodityT.class).getTrunk().contains("次品"));
				Assert.assertTrue("总数量不变", purBad.getAmount()+purGood.getAmount()==spur.getAmount() && purBad.getReceiptTicket().getReceiptAmount()+purGood.getReceiptTicket().getReceiptAmount()==spur.getAmount());
				if (sbill!=null)
					Assert.assertTrue("应付合计不变", billBad.getVoparam(CommodityT.class).getTrunk().contains("次品") && billBad.getAmount()+billGood.getAmount()==sbill.getAmount() && billBad.getMoney()+billGood.getMoney()==sbill.getMoney() && billBad.getBillTicket().getDiffMoney()+billGood.getBillTicket().getDiffMoney()==sbill.getBillTicket().getDiffMoney());
			}
		}
		if ("退货物品已到货".length()>0 && type.contains("1")) {
			this.onMenu("提交收货");
			if (true) {
				OrderDetail pur=this.getForm().getSelectFormer4Purchase().getLast(), spur=pur.getSnapShot();
				Assert.assertTrue("采购有库存", pur.getStPurchase()==30 && pur.getAmount()>0 && pur.getReceiptId()==30
						&& StringUtils.equals(pur.getLocationTicket().getIn().getName(), spur.getReturnTicket().getInName()));
				Assert.assertTrue("订单退货已收订单失效，记退货数", pur.getStOrder()==0
						&& pur.getReturnTicket().getReturnAmount()>0 && pur.getAmount()==spur.getAmount()
						&& pur.getReturnTicket().getReturnDate()!=null && pur.getVoparam(ReturnT.class).getReturnName()!=null);
				BillDetail bill=pur.getVoparam(BillDetail.class), sbill=(bill==null? null: (BillDetail)bill.getSnapShot()), rejectBill=pur.getVoparam("RejectBill");
				if (bill==null) {
				} else if (sbill.getStBill()==20) {
					Assert.assertTrue("应付失效", bill.getStBill()==0);
				} else if (sbill.getStBill()==30 && sbill.getMoney()>0) {
					Assert.assertTrue("已付款出负数应付待处理", rejectBill.getId()>0 && rejectBill.getStBill()==20 && rejectBill.getAmount()<0 && (rejectBill.getMoney()<0 || rejectBill.getBillTicket().getDiffMoney()<0));
				}
			}
		}
	}
	
	public void check订单退货全程(Object... filters) {
		this.check退货申请__1申请_2拆单3_3不通过确认('1', null, filters);
		this.check退货审核__1通过_2不通过('1', filters);
		this.check退货出库__1出库_2不同意('1', filters);
		this.check到货确认__1收货_2有次品2_3有短货3("1", filters);
	}
	
	public void linkSupplier拒收开退货入库申请_1拆分出退数_2退货申请无事务(char type, List<OrderDetail> rejectList) {
		if ("拆分出要退货的数量".length()>0 && type=='1') {
			List<String> listMonthnums=new ArrayList<String>(), itemMonthnums=null;
			double rejectSum = 0;
			for (Iterator<OrderDetail> pIter=rejectList.iterator(); pIter.hasNext(); listMonthnums.addAll(itemMonthnums)) {
				OrderDetail reject = pIter.next();
				rejectSum += reject.getAmount();
				itemMonthnums = new ArrayList<String>();
				StringBuffer monthnum = new StringBuffer("like ").append(new OrderTicketLogic().getPrtMonthnum(reject.getMonthnum())).append("%");
				for (String notnum: listMonthnums) {
					monthnum.append(" and !=").append(notnum);
				}
				this.loadView("OutstoreList", "selectFormer4Order.selectedList", "monthnum", monthnum.toString());
				double sumAmount=(Double)this.getListFootColumn("amount").get(0);
				Assert.assertTrue("有订单可退，足够数量", this.getListViewValue().size()>0 && sumAmount>=reject.getAmount());
				this.setSqlAllSelect(this.getListViewValue().size());
				this.onMenu("选择订单");
				double rejectAmount = reject.getAmount();
				for (Iterator<OrderDetail> oIter=this.getForm().getSelectFormer4Order().getSelectedList().iterator(); oIter.hasNext() && rejectAmount>0;) {
					OrderDetail order=oIter.next();
					if (order.getAmount() > rejectAmount) {
						this.loadView("OutstoreList", "selectFormer4Order.selectedList", "monthnum", order.getMonthnum());
						this.setSqlAllSelect(1);
						this.setFieldText("OrderDetail.amount", rejectAmount);
						this.onButton("拆分出数量");
						OrderDetail orderSplit = this.getForm().getSelectFormer4Order().getFirst().getVoparam("NewOrder");
						Assert.assertTrue("拆分出的新订单，数量为退货数", orderSplit.getId()>0 && StringUtils.equals(new DoubleType().format(orderSplit.getAmount()), new DoubleType().format(rejectAmount)));
						itemMonthnums.add(orderSplit.getMonthnum());
						rejectAmount = 0;
					} else if (order.getAmount() < rejectAmount) {
						itemMonthnums.add(order.getMonthnum());
						rejectAmount -= order.getAmount();
					} else {
						itemMonthnums.add(order.getMonthnum());
						rejectAmount = 0;
					}
				}
				Assert.assertTrue("订单数够开入库申请", rejectAmount==0);
				reject.getVoParamMap().put("WaitInstoreMonthnums", itemMonthnums);
			}
			this.loadView("OutstoreList", "selectFormer4Order.selectedList", "monthnum", listMonthnums.toArray(new String[0]));
			Assert.assertTrue("申请退货数够拒收数", rejectSum==(Double)this.getListFootColumn("amount").get(0) && listMonthnums.size()>0);
			rejectList.get(0).getVoParamMap().put("RejectSupplierMonthnums", listMonthnums);
		}
		if ("开退货入库申请单".length()>0 && type=='2') {
			List<String> listMonthnums = rejectList.get(0).getVoparam("RejectSupplierMonthnums");
			Assert.assertTrue("有指定退货订单月流水号", listMonthnums!=null && listMonthnums.size()>0);
			this.loadView("OutstoreList", "selectFormer4Order.selectedList", "monthnum", listMonthnums.toArray(new String[0]));
			this.getForm().getPurchaseDetail().setReturnTicket(rejectList.get(0).getReturnTicket());
			this.onButton("生成单号");
			this.getForm().getPurchaseDetail().getReturnTicket().setRemark("链接客户商家拒收退货申请开单");
			this.setSqlAllSelect(this.getListViewValue().size());
			this.onMenu("选择订单");
			this.onMenuUntrans("申请退货");
			if ("有事务".length()>0 && 1==0) {
				this.loadView("OutstoreList", "selectFormer4Order.selectedList", "monthnum", listMonthnums.toArray(new String[0]));
				Assert.assertTrue("拒收订单不再显示", this.getListViewValue().size()==0);
				this.loadView("ReceiptList", "monthnum", listMonthnums.toArray(new String[0]));
				Assert.assertTrue("退货待收数为拒收数", ((Double)this.getListFootColumn("amount").get(0))>0);
				Assert.assertTrue("拒收订单待收货确认", this.getListViewValue().size()==listMonthnums.size());
			}
		}
	}

	public void test订单退货() {
		if ("退货申请界面验证".length()>0) {
			this.loadView("ApplyList", "uneditable", "null");
			if (this.getListViewValue().size()==0) {
				this.getModeList().getSelfReceiptTest().get客户订单_普通(1);
				this.loadView("ApplyList", "uneditable", "null");
			}
			this.setSqlListSelect(1);
			ReturnTicket ticket = this.getForm().getPurchaseDetail().getReturnTicket();
			ticket.setNumber(new DateTimeType().format(new Date()));
			ticket.setRemark(ticket.getNumber());
			ticket.setDeliverNum(null);
			if ("不写备注".length()>0) {
				ticket.setRemark(null);
				try {
					this.onMenu("申请退货");
				}catch(Exception e) {
					Assert.assertTrue("有报错退货原因，".concat(e.getMessage()), e.getMessage().contains("退货原因"));
					ticket.setRemark(ticket.getNumber());
				}
			}
			if ("没订单号".length()>0) {
				ticket.setNumber(null);
				try {
					this.onMenu("申请退货");
				}catch(Exception e) {
					Assert.assertTrue("有报错退货单号，".concat(e.getMessage()), e.getMessage().contains("退货单号"));
					ticket.setNumber(ticket.getNumber());
				}
			}
		}
		if ("安装领班备货库存备货，退货".length()>0) {
			this.setTestStart();
			String purName = new LocationTicketTest().get安装领班分账_备货(new UserTest().getUser安装师傅01(), 11);
			this.check订单退货全程("purName", purName);
		}
		if ("订单安排用常规库存发货，撤销发货，退货，通过，出库，到货".length()>0) {
			this.setTestStart();
			String number=new SendTicketTest().get发货_常规(10, 10);
			new SendTicketTest().check撤消发货(number);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number", number);
			this.check退货审核__1通过_2不通过('1', "number", number);
			this.check退货出库__1出库_2不同意('1', "number", number);
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number", number);
		}
		if ("订单安排用常规库存发货，撤销发货，退货，不通过，不通过确认，退货2，通过，出库，到货".length()>0) {
			this.setTestStart();
			String number=new SendTicketTest().get发货_常规(10, 10);
			new SendTicketTest().check撤消发货(number);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			this.check退货审核__1通过_2不通过('2', "number",number);
			this.check退货申请__1申请_2拆单3_3不通过确认('3', null, "number",number);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			this.check退货审核__1通过_2不通过('1', "number",number);
			this.check退货出库__1出库_2不同意('1', "number",number);
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number",number);
		}
		if ("订单安排用请购可发货，退货，通过，出库，到货".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			this.check退货审核__1通过_2不通过('1', "number",number);
			this.check退货出库__1出库_2不同意('1', "number",number);
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number",number);
		}
		if ("订单安排用请购可发货，退货，不通过，不通过确认，退货2，通过，出库，到货".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			this.check退货审核__1通过_2不通过('2', "number",number);
			this.check退货申请__1申请_2拆单3_3不通过确认('3', null, "number",number);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			this.check退货审核__1通过_2不通过('1', "number",number);
			this.check退货出库__1出库_2不同意('1', "number",number);
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number",number);
		}
		if ("订单请购发货已回款，撤销发货，退货，通过，出库，到货".length()>0) {
			this.setTestStart();
			String number = new SendTicketTest().get发货_普通(10, 10);
			new SendTicketTest().check发货回款__1全额支付_2部分支付80('1', number);
			new SendTicketTest().check撤消发货(number);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			this.check退货审核__1通过_2不通过('1', "number",number);
			this.check退货出库__1出库_2不同意('1', "number",number);
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number",number);
		}
		if ("订单请购发货已回款，撤销发货，退货，不通过，不通过确认，可再发货".length()>0) {
			this.setTestStart();
			String number = new SendTicketTest().get发货_普通(10, 10);
			new SendTicketTest().check发货回款__1全额支付_2部分支付80('1', number);
			new SendTicketTest().check撤消发货(number);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			this.check退货审核__1通过_2不通过('2', "number",number);
			this.check退货申请__1申请_2拆单3_3不通过确认('3', null, "number",number);
			new SendTicketTest().check发货开单__1全发_2部分发('1', number, 0);
		}
		if ("订单普通可发货，退货，不通过，不通过确认，发货".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			this.check退货审核__1通过_2不通过('2', "number",number);
			this.check退货申请__1申请_2拆单3_3不通过确认('3', null, "number",number);
			new SendTicketTest().check发货开单__1全发_2部分发('1', number, 0);
		}
		if ("订单普通可发货，退货，通过，出库不同意，发货".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			this.check退货审核__1通过_2不通过('1', "number",number);
			this.check退货出库__1出库_2不同意('2', "number",number);
			new SendTicketTest().check发货开单__1全发_2部分发('1', number, 0);
		}
		if ("退货开单，不能发货".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number);
			try {
				new SendTicketTest().check发货开单__1全发_2部分发('1', number, 0);
				Assert.fail("退货开单，不能发货");
			}catch(Exception e) {
			}
		}
		if ("安装工程领班退货开单，只看到自己库存".length()>0) {
			this.setTestStart();
			String number1 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅01(), 11, 11);
			String number2 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅02(), 12, 12);
			User fromUser = this.setTransUser(new UserTest().getUser安装师傅01());
			this.loadView("ApplyList", "number", number2);
			Assert.assertTrue("安装师傅只能退自己的库存", this.getListViewValue().size()==0);
			this.setTransUser(fromUser);
		}
		if ("安装工程领班退货，审核不通过确认，只看到自己不通过".length()>0) {
			this.setTestStart();
			String number1 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅01(), 11, 11);
			String number2 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅02(), 12, 12);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number1);
			this.check退货审核__1通过_2不通过('2', "number",number1);
			this.setTransUser(new UserTest().getUser安装师傅02());
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number2);
			this.check退货审核__1通过_2不通过('2', "number",number2);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.loadView("ApplyList", "number", number2);
			Assert.assertTrue("安装师傅只能看到自己的退货不通过", this.getListViewValue().size()==0);
			this.setTransUser(new UserTest().getUser管理员());
		}
		if ("安装工程领班退货，审核通过，到师傅退货出库，师傅只看到自己的待出库".length()>0) {
			this.setTestStart();
			String number1 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅01(), 11, 11);
			String number2 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅02(), 12, 12);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number1);
			this.check退货审核__1通过_2不通过('1', "number",number1);
			this.setTransUser(new UserTest().getUser安装师傅02());
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number2);
			this.check退货审核__1通过_2不通过('1', "number",number2);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.loadView("OutstoreList", "number", number2);
			Assert.assertTrue("安装师傅只能看到自己的退货待出库", this.getListViewValue().size()==0);
			this.setTransUser(new UserTest().getUser管理员());
		}
		if ("安装工程领班在退货申请，拆分数量3开退货单，走退货全流程".length()>0) {
			this.setTestStart();
			String number = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅01(), 11, 11);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check退货申请__1申请_2拆单3_3不通过确认('2', null, "number",number);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number, "amount", 3);
			this.check退货审核__1通过_2不通过('1', "number",number);
			this.check退货出库__1出库_2不同意('1', "number",number);
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number",number);
		}
	}
	
	private void test链接客户商家拒收退货() {
if (1==1) {
		if ("常规发货给客户商家11，客户商家部分收货发起商品改单5，采购判收货3+拒收2，客户商家收货确认减少收货2+上级商家开退货申请2，退货到货2".length()>0) {
			this.setTestStart();
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			String upNumber = new SendTicketTest().get发货_常规(11, 11);
			List<OrderDetail> upOrders = new OrderTicketTest().getOrderList("number", upNumber);
			this.setTransSeller(new Seller4lLogic().get吉高电子());
			String dwNumber = new OrderTicketTest().getOrderList("monthnum", upOrders.get(0).getMonthnum()).get(0).getOrderTicket().getNumber();
			this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('2', dwNumber, 5);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", dwNumber, new Object[0], new Object[]{"amount", 5});
			this.getModeList().getSelfPurchaseTest().check收货返单处理__2拒绝收货(dwNumber, "receiptAmount", 3d);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("1", dwNumber);
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number", upNumber, "amount", 2);
		}
		if ("常规库存发货12=4+4+4，收货5+6拒收，退货上级到货确认5+6".length()>0) {
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			String upNumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(12);
			List<OrderDetail> upOrders = new OrderTicketTest().getOrderList("number", upNumber);
			this.getModeList().getSelfReceiptTest().getPur备货订单_普通(12);
			this.setTransSeller(new Seller4lLogic().get吉高电子());
			String dwNumber = new OrderTicketTest().getOrderList("monthnum", upOrders.get(0).getMonthnum()).get(0).getOrderTicket().getNumber();
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			new SendTicketTest().check发货开单__1全发_2部分发('2', upNumber, 4);
			new SendTicketTest().check发货开单__1全发_2部分发('2', upNumber, 4);
			new SendTicketTest().check发货开单__1全发_2部分发('1', upNumber, 4);
			this.setTransSeller(new Seller4lLogic().get吉高电子());
			if ("从12收5拒收".length()>0) {
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('2', dwNumber, 5, "amount", 12);
				this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", dwNumber, new Object[0], new Object[]{"amount", 5});
				this.getModeList().getSelfPurchaseTest().check收货返单处理__2拒绝收货(dwNumber, "receiptAmount", 0d);
				this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("1", dwNumber);
			}
			if ("从7收6拒收".length()>0) {
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('2', dwNumber, 6, "amount", 7);
				this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", dwNumber, new Object[0], new Object[]{"amount", 6});
				this.getModeList().getSelfPurchaseTest().check收货返单处理__2拒绝收货(dwNumber, "receiptAmount", 0d);
				this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("1", dwNumber);
			}
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number", upNumber);
		}
}
		if ("普通采购发货12=4+4+4，收货5+6拒收，退货上级到货确认5+6".length()>0) {
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			String upNumber = this.getModeList().getSelfReceiptTest().get客户订单_普通(12);
			List<OrderDetail> upOrders = new OrderTicketTest().getOrderList("number", upNumber);
			this.setTransSeller(new Seller4lLogic().get吉高电子());
			String dwNumber = new OrderTicketTest().getOrderList("monthnum", upOrders.get(0).getMonthnum()).get(0).getOrderTicket().getNumber();
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			new SendTicketTest().check发货开单__1全发_2部分发('2', upNumber, 4);
			new SendTicketTest().check发货开单__1全发_2部分发('2', upNumber, 4);
			new SendTicketTest().check发货开单__1全发_2部分发('1', upNumber, 4);
			this.setTransSeller(new Seller4lLogic().get吉高电子());
			if ("从12收5拒收".length()>0) {
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('2', dwNumber, 5, "amount", 12);
				this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", dwNumber, new Object[0], new Object[]{"amount", 5});
				this.getModeList().getSelfPurchaseTest().check收货返单处理__2拒绝收货(dwNumber, "receiptAmount", 0d);
				this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("1", dwNumber);
			}
			if ("从7收6拒收".length()>0) {
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('2', dwNumber, 6, "amount", 7);
				this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", dwNumber, new Object[0], new Object[]{"amount", 6});
				this.getModeList().getSelfPurchaseTest().check收货返单处理__2拒绝收货(dwNumber, "receiptAmount", 0d);
				this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("1", dwNumber);
			}
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number", upNumber);
		}
	}
	private void temp() {
		if ("安装工程领班在退货申请，拆分数量3开退货单，走退货全流程".length()>0) {
			this.setTestStart();
			String number = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅01(), 11, 11);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check退货申请__1申请_2拆单3_3不通过确认('2', null, "number",number);
			this.check退货申请__1申请_2拆单3_3不通过确认('1', null, "number",number, "amount", 3);
			this.check退货审核__1通过_2不通过('1', "number",number);
			this.check退货出库__1出库_2不同意('1', "number",number);
			this.check到货确认__1收货_2有次品2_3有短货3("1", "number",number);
		}
	}
	
	protected void setQ清空() {
	}
}
