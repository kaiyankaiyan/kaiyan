package com.haoyong.sales.test.sale;

import java.util.Date;
import java.util.List;

import net.sf.mily.types.DateTimeType;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.logic.BillTypeLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.BillT;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.form.LocationTicketForm;
import com.haoyong.sales.sale.form.OrderReturnForm;
import com.haoyong.sales.sale.form.SendTicketForm;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.sale.logic.ArrangeTypeLogic;
import com.haoyong.sales.sale.logic.OrderDoptionLogic;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.UserTest;

public class SendTicketTest extends AbstractTest<SendTicketForm> {

	public SendTicketTest() {
		this.setForm(new SendTicketForm());
	}

	public void check发货开单__1全发_2部分发(char type, String number, int outAmount, Object... filters0) {
		SendTicketForm form = this.getForm();
		Object[] filters = number==null? filters0: this.genFiltersStart(filters0, "number", number);
		this.loadView("SendList", filters);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		double sumStore = (Double) this.getListFootColumn("amount").get(0);
		this.onMenu("发货开单");
		this.setEditAllSelect(detailCount);
		if ("全部发货".length()>0 && type=='1') {
			for (OrderDetail detail : form.getDetailList()) {
				this.setRowFieldText(detail, "sendAmount", detail.getAmount());
			}
			this.onButton("生成单号");
			this.onMenu("提交全数出货");
			OrderDetail order=form.getDetailList().get(form.getDetailList().size()-1), sorder=order.getSnapShot();
			Assert.assertTrue("月流水号不变，有发货单日期", order.getMonthnum().equals(sorder.getMonthnum())
					&& order.getSendTicket().getNumber()!=null && order.getSendTicket().getSendDate()!=null);
			if ("链接分公司商家采购".length()>0 && order.getSubCompany().getFromSellerId()>0 && sorder.getClient().getFromSellerId()==0) {
				List<OrderDetail> dwPurchases = this.getModeList().getSelfPurchaseTest().getPurchaseList(order.getSubCompany().getFromSellerId(), "monthnum", order.getMonthnum());
				Assert.assertTrue("有下级分公司采购单", dwPurchases.size()==1);
				OrderDetail dwPur = dwPurchases.get(0);
				Assert.assertTrue("采购待收，月流水号沿用", dwPur.getStPurchase()==30 && dwPur.getReceiptId()==0 && dwPur.getMonthnum().equals(sorder.getMonthnum()));
				Assert.assertTrue("变为链接订单", order.getClient().getFromSellerId()>0 && sorder.getClient().getFromSellerId()==0);
			}
			if (true) {
				boolean has师傅 = (form.getDomain().getLocationTicket().getIn().getName()!=null && StringUtils.equals(form.getDomain().getLocationTicket().getIn().getName(), form.getUserName())==false);
				if ("不需要安装".length()>0 && has师傅==false) {
					this.loadFormView(new OrderReturnForm(), "OutstoreList", "selectFormer4Order.selectedList", "monthnum", order.getMonthnum());
					Assert.assertTrue("已发货订单可开拒收退货入库申请", this.getListViewValue().size()==1);
					Assert.assertTrue("无师傅发货完成", order.getSendId()==30);
				}
				if (has师傅) {
					if ("本级师傅迁移待收货".length()>0 && order.getClient().getFromSellerId()==0) {
						Assert.assertTrue("订单库存迁移到安装师傅库存分账上，发货未完成", order.getStPurchase()==32 && order.getSendId()==20);
						this.loadFormView(new LocationTicketForm(), "InstoreList", "monthnum", order.getMonthnum());
						Assert.assertTrue("待师傅分账迁移到货确认", this.getListViewValue().size()==1);
					} else if ("下级分公司的师傅迁移待收".length()>0) {
						Assert.assertTrue("本级订单已发货无库存", order.getSendId()==30 && order.getStPurchase()==0);
						OrderDetail downPur = order.getVoparam("DownPurchase");
						Assert.assertTrue("订单库存迁移到安装师傅库存分账上，先采购收货，才能确认", downPur.getSellerId()!=order.getSellerId() 
								&& downPur.getStPurchase()==32 && downPur.getReceiptId()==0);
					}
				} else {
					if (sorder.getStPurchase()>0) {
						Assert.assertTrue("订单库存数量减少，采购单库存出货清空失效，订单记出货来源", order.getStPurchase()==0);
					} else if (sorder.getStPurchase()==0) {
						Assert.assertTrue("订单记常规库存出货来源，采购单库存出货清空失效", order.getPurchaseTicket().getNumber()!=null && order.getReceiptTicket().getNumber()!=null
								&& order.getStPurchase()==0);
					}
				}
				BillDetail bill = order.getVoparam(BillDetail.class);
				Assert.assertTrue("出货订单有出库单号", order.getSendTicket().getNumber() != null);
				if ("非链接分公司应收".length()>0 && (sorder.getSubCompany().getFromSellerId()>0 && sorder.getClient().getFromSellerId()>0)==false && has师傅==false) {
					Assert.assertTrue("订单发货有应收", bill != null && bill.getId() > 0
							&& StringUtils.equals(bill.getClient().getName(), order.getClient().getName())
							&& StringUtils.equals(bill.getSubCompany().getName(), order.getSubCompany().getName())
							&& bill.getPrice() == order.getOrderTicket().getCprice()
							&& new BillTypeLogic().getSaleType().equals(bill.getBillTicket().getTypeName()));
					if (new OrderDoptionLogic().isPresent(order.getOrderTicket().getDoption()))
						Assert.assertTrue("赠送为已支付", bill.getMoney()==0 && bill.getBillTicket().getBillDate()!=null && bill.getBillTicket().getDiffMoney()==order.getOrderTicket().getCmoney() && bill.getStBill()==30);
					else if (new OrderDoptionLogic().isPaid(order.getOrderTicket().getDoption()))
						Assert.assertTrue("已支付", bill.getMoney()>0 && bill.getBillTicket().getDiffMoney()==0 && bill.getStBill()==30 && bill.getBillTicket().getBillDate()!=null);
					else
						Assert.assertTrue("其它未支付", bill.getMoney()==order.getOrderTicket().getCmoney() && bill.getBillTicket().getDiffMoney()==0 && bill.getBillTicket().getBillDate()==null);
				} else if ("链接分公司商家".length()>0){
					Assert.assertTrue("链接分公司订单没应收", bill==null);
				}
			}
			this.loadView("SendList", filters);
			Assert.assertTrue("订单库货数减少", this.getListFootColumn("amount").get(0)==null);
			if (StringUtils.equals(new DeliverTypeLogic().getCommonType(), form.getDomain().getArrangeTicket().getArrangeType())==true) {
				this.loadSqlView(new StoreTicketForm(), "RestoreList", "实时采购在库明细", "monthnum", order.getMonthnum());
				Assert.assertTrue("出货后不在现库存Restore", this.getListViewValue().size()==0);
				this.loadSqlView(new StoreTicketForm(), "EnoughList", "现有库存", "monthnum", order.getMonthnum());
				Assert.assertTrue("出库后不在现库存Enough", this.getListViewValue().size()==0);
			}
			new StoreTicketTest().checkOutStoreList((StoreTicketForm)form.getFormProperty("attrMap.StoreTicketForm"));
		}
		if ("部分发货".length()>0 && type=='2') {
			for (OrderDetail detail : form.getDetailList()) {
				this.setRowFieldText(detail, "sendAmount", outAmount);
			}
			this.onMenu("部分发货拆单");
			if (true) {
				OrderDetail order = form.getDetailList().get(form.getDetailList().size()-1);
				OrderDetail sorder=(OrderDetail)order.getSnapShot().getSnapShot(), nwOrderRemain=(OrderDetail)order.getVoParamMap().get("NewRemainOrder");
				Assert.assertTrue("当前订单部分发货用新分支流水号", order.getMonthnum().startsWith(sorder.getMonthnum().split("\\-")[0].concat("-")));
				Assert.assertTrue("出货的分支订单与原采购单月流水号不同，订单库存数量减少", StringUtils.equals(order.getMonthnum(), sorder.getMonthnum())==false
						&& sorder.getAmount()-nwOrderRemain.getAmount()==outAmount);
				Assert.assertTrue("剩余数订单没有出货单号，用原月流水号，没有发货单", nwOrderRemain.getId()>0 && nwOrderRemain.getSendTicket().getNumber()==null
						&& StringUtils.equals(nwOrderRemain.getMonthnum(), sorder.getMonthnum())
						&& StringUtils.equals(nwOrderRemain.getSendTicket().getNumber(), sorder.getSendTicket().getNumber()));
				Assert.assertTrue("订单数量减少", sorder.getAmount()-nwOrderRemain.getAmount()==outAmount);
			}
			this.setEditAllSelect(detailCount);
			this.onButton("生成单号");
			String timeDo = this.getTimeDo();
			this.onMenu("提交全数出货");
			if (true) {
				OrderDetail order=form.getDetailList().get(form.getDetailList().size()-1), sorder=order.getSnapShot();
				BillDetail bill = order.getVoparam(BillDetail.class);
				Assert.assertTrue("出货订单有出库单号、发货日期", order.getSendTicket().getNumber()!=null && order.getSendTicket().getSendDate()!=null);
				if (order.getSubCompany().getFromSellerId()==0)
					Assert.assertTrue("订单发货有应收", bill != null
							&& bill.getId() > 0
							&& bill.getClient().getName() != null
							&& bill.getPrice() == order.getOrderTicket().getCprice()
							&& bill.getMoney() > 0
							&& new BillTypeLogic().getSaleType().equals(bill.getBillTicket().getTypeName()));
				if (sorder.getStPurchase()>0) {
					Assert.assertTrue("订单库存数量减少，采购单库存出货清空失效，订单记出货来源", order.getStPurchase()==0);
				} else {
					Assert.assertTrue("订单记常规库存出货来源", order.getPurchaseTicket().getNumber()!=null && order.getReceiptTicket().getNumber()!=null);
				}
				this.loadFormView(new OrderReturnForm(), "OutstoreList", "selectFormer4Order.selectedList", "monthnum", order.getMonthnum());
				Assert.assertTrue("已发货订单可收到拒收退货", this.getListViewValue().size()==1);
			}
			if (sumStore - outAmount * detailCount > 0) {
				this.loadView("SendList", filters);
				Assert.assertTrue("订单库货数减少",sumStore - outAmount * detailCount == (Double) this.getListFootColumn("amount").get(0));
			}
			if (form.getDomain().getSubCompany().getFromSellerId()==0) {
				this.loadFormView(new StoreTicketForm(), "RestoreList", "实时采购在库明细", "number", number);
				Assert.assertTrue("出货后库存重计算", this.getListViewValue().size()==0);
				if (new ArrangeTypeLogic().isNormal(form.getDomain().getArrangeTicket().getArrangeType())) {
					List<StoreEnough> enoughList = new StoreTicketTest().getEnoughs("modifytime", timeDo);
					Assert.assertTrue("出库后够用数重计算", enoughList.size()>0);
				}
			}
			new StoreTicketTest().checkOutStoreList((StoreTicketForm)form.getFormProperty("attrMap.StoreTicketForm"));
		}
	}
	
	public void check发货回款__1全额支付_2部分支付80(char type, String number, Object... filters0) {
		SendTicketForm form = this.getForm();
		Object[] filters = number==null? filters0: this.genFiltersStart(filters0, "number", number);
		this.loadView("PaidList", filters);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.onMenu("回款确认");
		this.setEditAllSelect(detailCount);
		this.onButton("生成单号");
		form.getDomain().getVoparam(BillDetail.class).getBillTicket().setBillDate(new Date());
		if ("全额支付".length()>0 && type=='1') {
			this.onButton("明细已支付");
			this.onMenu("已支付提交");
			if (true) {
				this.loadView("PaidList", filters);
				Assert.assertTrue("已提交不可再次提交", this.getListViewValue().size()==0);
				BillDetail bill=form.getSelectFormer4Order().getLast().getVoparam(BillDetail.class);
				Assert.assertTrue("应收已收款生效", bill.getBillTicket().getTypeName().equals("销售出货")
						&& bill.getStBill()==30 && bill.getBillTicket().getBillDate()!=null
						&& bill.getVoparam(BillT.class).getBillName()!=null);
			}
		}
		if ("部分支付".length()>0 && type=='2') {
			for (OrderDetail detail: form.getSelectFormer4Order().getSelectedList()) {
				BillDetail bill = detail.getVoparam(BillDetail.class);
				bill.setMoney(80);
				bill.getBillTicket().setDiffMoney(detail.getOrderTicket().getCmoney() - bill.getMoney());
			}
			this.onMenu("已支付提交");
			if (true) {
				this.loadView("PaidList", filters);
				Assert.assertTrue("已提交不可再次提交", this.getListViewValue().size()==0);
				BillDetail bill=form.getSelectFormer4Order().getLast().getVoparam(BillDetail.class);
				Assert.assertTrue("应收已收款生效", bill.getBillTicket().getTypeName().equals("销售出货")
						&& bill.getStBill()==30 && bill.getBillTicket().getBillDate()!=null
						&& bill.getMoney()==80
						&& bill.getVoparam(BillT.class).getBillName()!=null);
			}
		}
	}

	public void check撤消发货(String number) {
		SendTicketForm form = this.getForm();
		this.loadView("RollbackList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		new StoreTicketTest().setQ清空();
		String timeDo = this.getTimeDo();
		this.onMenu("撤销发货");
		OrderDetail order=form.getSelectFormer4Order().getFirst(), sorder=order.getSnapShot();
		if (true) {
			this.loadView("RollbackList", "number", number);
			Assert.assertTrue("撤销发货的明细不可再撤销", this.hasMenu("撤销发货") && this.getListViewValue().size()==0);
			this.loadView("SendList", "number", number, "modifytime", timeDo);
			Assert.assertTrue("撤销发货的订单可继续发货", this.getListViewValue().size()==detailCount);
			this.loadFormView(new StoreTicketForm(), "StoreEnoughQuery");
			Assert.assertTrue("订单撤销发货加回库存有够用数", this.getListViewValue().size()>0);
			Assert.assertTrue("撤货不能为已付款", new OrderDoptionLogic().isPaid(order.getOrderTicket().getDoption())==false);
			if (new OrderDoptionLogic().isPresent(sorder.getOrderTicket().getDoption()))
				Assert.assertTrue("撤货保持赠送", new OrderDoptionLogic().isPresent(order.getOrderTicket().getDoption()));
		}
		Assert.assertTrue("撤销发货无发货日期", order.getSendId()<30 && order.getSendTicket().getSendDate()==null);
		if ("订单安排为常规".length()>0 && new ArrangeTypeLogic().isNormal(sorder.getArrangeTicket().getArrangeType())) {
			this.loadFormView(new StoreTicketForm(), "StoreItemQuery");
			Assert.assertTrue("安排为常规的订单撤销发货，加回库存", this.getListViewValue().size()>=detailCount);
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", number, "modifytime", timeDo);
			Assert.assertTrue("撤销发货的常规库存绑定给此订单，订单转普通请购安排，可发", this.getListViewValue().size()==detailCount
					&& new DeliverTypeLogic().isCommonType(order.getArrangeTicket().getArrangeType())
					&& order.getSendTicket().getSendDate()==null
					&& order.getStPurchase()==30 && order.getSendId()==20);
		}
		if ("订单安排为请购".length()>0 && sorder.getStPurchase()>0) {
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", number, "modifytime", timeDo);
			Assert.assertTrue("撤销发货的订单采购库存还是订单的", this.getListViewValue().size()==detailCount);
			Assert.assertTrue("撤销发货有库存，可发", order.getStPurchase()==30 && order.getSendId()==20);
		}
		if ("下级分公司的订单".length()>0 && sorder.getSubCompany().getFromSellerId()>0) {
			Assert.assertTrue("仍为链接订单", order.getSubCompany().getFromSellerId()>0);
		}
		if ("非链接分公司应收".length()>0 && (sorder.getSubCompany().getFromSellerId()>0 && sorder.getClient().getFromSellerId()>0)==false) {
			BillDetail bill=order.getVoparam(BillDetail.class), sbill=bill.getSnapShot(), diffbill=(BillDetail)order.getVoParamMap().get("DiffBill");
			DateTimeType dtype = new DateTimeType();
			if (sbill.getStBill()==30 && sbill.getMoney()>0) {
				Assert.assertTrue("新差额应收为负数待处理", diffbill.getId()>0 && diffbill.getAmount()<0 && diffbill.getMonthnum().equals(order.getMonthnum()) && diffbill.getStBill()==20 && diffbill.getBillTicket().getBillDate()==null && diffbill.getMoney()==0-sbill.getMoney());
				Assert.assertTrue("已收款用新分支月流水号", new OrderTicketLogic().isSplitMonthnum(bill.getMonthnum(), sbill.getMonthnum()));
			} else {
				Assert.assertTrue("原应收款明细失效",  bill.getStBill()==0
						&& bill.getBillTicket().getTypeName().equals("销售出货")
						&& StringUtils.equals(dtype.format(bill.getBillTicket().getBillDate()), dtype.format(sbill.getBillTicket().getBillDate())));
			}
			Assert.assertTrue("撤销发货记是否已收款", order.getUsend().contains(((BillDetail)form.getDomain().getVoparam(BillDetail.class).getSnapShot()).getStBill()==30? "已收款": "未收款"));
		} else {
			Assert.assertTrue("链接分公司没应收", order.getVoparam(BillDetail.class)==null);
		}
	}

	public void test订单发货() {
if (1==1) {
		if ("客户订单排单为常规发货".length()>0) {
			this.setTestStart();
			String number = new SendTicketTest().get可发_常规(11, 11);
			this.check发货开单__1全发_2部分发('1', number, 11);
		}
		if ("客户订单排单为常规10, 先出货7默认出库价，再出货3默认出库价".length()>0) {
			this.setTestStart();
			String number = new SendTicketTest().get可发_常规(10, 10);
			this.check发货开单__1全发_2部分发('2', number, 7);
			this.check发货开单__1全发_2部分发('1', number, 3);
		}
		if ("普通客户订单全数出货".length() > 0) {
			this.getModeList().getSelfReceiptTest().get客户订单_普通(3);// 使发货后还有库存
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check发货开单__1全发_2部分发('1', number, 10);
		}
		if ("订单收货10，先出货7默认出库价，再出货3默认出库价".length() > 0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check发货开单__1全发_2部分发('2', number, 7);
			this.check发货开单__1全发_2部分发('1', number, 3);
		}
		if ("订单11，部分收货7剩余毪4，部分发货5剩余可发2，全发货2，收货4，全发货4".length() > 0) {
			this.setTestStart();
			String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('2', number, 7);
			this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number, "amount", 7);
			this.check发货开单__1全发_2部分发('2', number, 5);
			this.check发货开单__1全发_2部分发('1', number, 2);
			this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number, "amount", 4);
			this.check发货开单__1全发_2部分发('1', number, 4);
		}
		if ("直发收货合并发货，不能再发货".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_直发(10, 10);
			this.loadView("SendList", "number", number);
			Assert.assertTrue("直发收货的订单不能再发货", this.getListViewValue().size()==0);
		}
		if ("当地购收货合并发货，不能再发货".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_当地购(10, 10);
			this.loadView("SendList", "number", number);
			Assert.assertTrue("当地购收货的订单不能再发货", this.getListViewValue().size()==0);
		}
}
		if ("已付款订单发货，不用支付".length()>0) {
			this.loadFormView(this.getModeList().getSelfOrderForm(), "Create");
			if (this.hasField("doption")==true) {
				this.setTestStart();
				OrderDetail sample = new OrderDetail();
				sample.getOrderTicket().setDoption("已付款");
				String number = this.getModeList().getSelfOrderTest().setSample(sample).get客户订单(11, 11);
				new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('2', number);
				this.getModeList().getSelfPurchaseTest().check普通采购__1采购_2改单重新排单('1', number);
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
				this.check发货开单__1全发_2部分发('1', number, 0);
				this.loadView("PaidList", "number", number);
				Assert.assertTrue("已支付不在待回款列表", this.getListViewValue().size()==0);
			}
		}
		if ("赠送订单发货，不用支付".length()>0) {
			this.setTestStart();
			this.loadFormView(this.getModeList().getSelfOrderForm(), "Create");
			if (this.hasField("doption")==true) {
				OrderDetail sample = new OrderDetail();
				sample.getOrderTicket().setDoption("赠送");
				String number = this.getModeList().getSelfOrderTest().setSample(sample).get客户订单(11, 11);
				new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('2', number);
				this.getModeList().getSelfPurchaseTest().check普通采购__1采购_2改单重新排单('1', number);
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
				this.check发货开单__1全发_2部分发('1', number, 0);
				this.loadView("PaidList", "number", number);
				Assert.assertTrue("已支付不在待回款列表", this.getListViewValue().size()==0);
			}
		}
		if ("师傅只看到自己的可发货".length()>0) {
			this.setTestStart();
			String number1 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅01(), 11);
			String number2 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅02(), 12,12);
			String number = this.get可发_常规(13,13,13);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.loadView("SendList", "number", new String[]{number1, number2, number});
			Assert.assertTrue("安装领班师傅01只看到自己的可发货", this.getListViewValue().size()==1);
		}
		if ("给师傅发货，迁移分账收货".length()>0 && this.getModeList().getMode(TestMode.ClientOrder).length==0) {
			this.setTestStart();
			OrderDetail sample = new OrderDetail();
			sample.getLocationTicket().setTo(new UserTest().getUser安装师傅01());
			String number = this.getModeList().getSelfOrderTest().setSample(sample).get客户订单(1,2);
			OrderDetail order = new OrderTicketTest().getOrderList("number",number).get(0);
			Assert.assertTrue("有安装师傅", order.getLocationTicket().getTo().getName()!=null);
			this.set可发_普通(number);
			this.check发货开单__1全发_2部分发('1', number, 0);
			if ("下级分公司先收货，再师傅迁移确认，师傅安装发货".length()>0 && this.getModeList().contain(TestMode.SubcompanyOrder)) {
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', null, "monthnum", order.getMonthnum());
				this.setTransUser(new UserTest().getUser安装师傅01());
				new LocationTicketTest().check迁入确认__1签收_2拒收('1', "monthnum", order.getMonthnum());
				this.check发货开单__1全发_2部分发('1', null, 0, "monthnum", order.getMonthnum());
				this.check发货回款__1全额支付_2部分支付80('1', null, "monthnum", order.getMonthnum());
				this.setTransSeller(new Seller4lLogic().get南宁古城());
			} else if ("本级师傅直接迁移确认，师傅安装发货".length()>0) {
				this.setTransUser(new UserTest().getUser安装师傅01());
				new LocationTicketTest().check迁入确认__1签收_2拒收('1', "monthnum", order.getMonthnum());
				this.check发货开单__1全发_2部分发('1', number, 0);
				this.check发货回款__1全额支付_2部分支付80('1', number);
			}
		}
	}
	
	public void test撤销发货() {
		if ("用常规库存发货10的撤销发货1，可重发1、回款1，撤销2，再重发2、回款2，撤销3，再重发3、回款3，2次撤销已回款负数应收回款".length()>0) {
			this.setTestStart();
			String number = this.get发货_常规(10, 10);
			this.check撤消发货(number);
			this.check发货开单__1全发_2部分发('1', number, 0);
			if (this.getModeList().contain(TestMode.SubCompany)==false)
				this.check发货回款__1全额支付_2部分支付80('1', number);
			String timeDo2=this.getTimeDo();
			this.check撤消发货(number);
			this.check发货开单__1全发_2部分发('1', number, 0);
			if (this.getModeList().contain(TestMode.SubCompany)==false)
				this.check发货回款__1全额支付_2部分支付80('1', number, "amount", ">0");
			String timeDo3=this.getTimeDo();
			this.check撤消发货(number);
			this.check发货开单__1全发_2部分发('1', number, 0);
			if (this.getModeList().contain(TestMode.SubCompany)==false)
				this.check发货回款__1全额支付_2部分支付80('1', number, "amount", ">0");
			if (this.getModeList().contain(TestMode.SubCompany)==false) {
				this.check发货回款__1全额支付_2部分支付80('1', number, "amount", "<0", "modifytime", timeDo3);
				this.check发货回款__1全额支付_2部分支付80('1', number, "amount", "<0", "modifytime", timeDo2);
			}
		}
		if ("用常规库存10部分发货7的撤销发货7，可重发7普通+3常规".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_常规(10, 10);
			this.getModeList().getSelfReceiptTest().getPur备货订单_普通(10, 10);
			this.check发货开单__1全发_2部分发('2', number, 7);
			this.check撤消发货(number);
			this.check发货开单__1全发_2部分发('1', number, 0, "arrangeType", "普通");
			this.check发货开单__1全发_2部分发('1', number, 0, "arrangeType", "常规库存");
		}
		if ("用请购库存发货10的撤销，可重发".length()>0) {
			this.setTestStart();
			String number = this.get发货_普通(10, 10);
			this.check撤消发货(number);
			this.check发货开单__1全发_2部分发('1', number, 0);
		}
		if ("用请购库存10部分发货7的撤销，可重发7+3".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			this.check发货开单__1全发_2部分发('2', number, 7);
			this.check撤消发货(number);
			this.check发货开单__1全发_2部分发('1', number, 0);
		}
		if ("直发收货合并发货，不可撤销".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_直发(10, 10);
			this.loadView("RollbackList", "number", number);
			Assert.assertTrue("直发收货合并发货，不可撤销", this.getListViewValue().size()==0);
		}
		if ("当地购收货合并发货，不可撤销".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_当地购(10, 10);
			this.loadView("RollbackList", "number", number);
			Assert.assertTrue("当地购收货合并发货，不可撤销", this.getListViewValue().size()==0);
		}
	}
	
	public void test发货回款() {
		if (this.getModeList().contain(TestMode.SubcompanyOrder)==true)
			return;
if (1==1) {
		if ("用常规库存发货，全额回款，部分回款".length()>0) {
			this.setTestStart();
			String number1=this.get发货_常规(10, 10);
			this.check发货回款__1全额支付_2部分支付80('1', number1);
			String number2=this.get发货_常规(10, 10);
			this.check发货回款__1全额支付_2部分支付80('2', number2);
		}
		if ("用请购库存发货，全额回款，部分回款".length()>0) {
			this.setTestStart();
			String number1=this.get发货_普通(10, 10);
			this.check发货回款__1全额支付_2部分支付80('1', number1);
			String number2=this.get发货_普通(10, 10);
			this.check发货回款__1全额支付_2部分支付80('2', number2);
		}
		if ("直发可以回款，部分回款".length()>0) {
			this.setTestStart();
			String number1=this.getModeList().getSelfReceiptTest().get客户订单_直发(10, 10);
			this.check发货回款__1全额支付_2部分支付80('1', number1);
			String number2=this.getModeList().getSelfReceiptTest().get客户订单_直发(10, 10);
			this.check发货回款__1全额支付_2部分支付80('2', number2);
		}
		if ("当地购可以回款，部分回款".length()>0) {
			this.setTestStart();
			String number1=this.getModeList().getSelfReceiptTest().get客户订单_当地购(10, 10);
			this.check发货回款__1全额支付_2部分支付80('1', number1);
			String number2=this.getModeList().getSelfReceiptTest().get客户订单_当地购(10, 10);
			this.check发货回款__1全额支付_2部分支付80('2', number2);
		}
		if ("安装师傅分账的订单，发货安装，回款".length()>0) {
			this.setTestStart();
			String number2 = new SendTicketTest().get发货_常规(10,10);
			String number = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅01(), 10,10);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check发货开单__1全发_2部分发('1', number, 0);
			this.loadView("PaidList", "number", number2);
			Assert.assertTrue("师傅只能看到自己的已安装发货", this.getListViewValue().size()==0);
			this.check发货回款__1全额支付_2部分支付80('1', number);
			this.setTransUser(new UserTest().getUser管理员());
		}
}
		if ("安装师傅只看到自己的可回款".length()>0) {
			this.setTestStart();
			String number1 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅01(), 11);
			String number2 = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅02(), 12,12);
			String number = this.get发货_常规(13,13,13);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check发货开单__1全发_2部分发('1', number1, 0);
			this.setTransUser(new UserTest().getUser安装师傅02());
			this.check发货开单__1全发_2部分发('1', number2, 0);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.loadView("PaidList", "number", new String[]{number1, number2, number});
			Assert.assertTrue("安装领班师傅01只看到自己的可回款", this.getListViewValue().size()==1);
		}
	}
	
	private void temp() {
		if ("安装师傅分账的订单，发货安装，回款".length()>0) {
			this.setTestStart();
			String number2 = new SendTicketTest().get发货_常规(10,10);
			String number = new LocationTicketTest().get安装领班分账_客户(new UserTest().getUser安装师傅01(), 10,10);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check发货开单__1全发_2部分发('1', number, 0);
			this.loadView("PaidList", "number", number2);
			Assert.assertTrue("师傅只能看到自己的已安装发货", this.getListViewValue().size()==0);
			this.check发货回款__1全额支付_2部分支付80('1', number);
			this.setTransUser(new UserTest().getUser管理员());
		}
	}

	public String get可发_常规(int... amountList) {
		String number = new ArrangeTicketTest().get客户订单_常规(amountList);
		this.getModeList().getSelfReceiptTest().getPur备货订单_普通(amountList);
		return number;
	}
	
	public String get可发_普通(int... amountList) {
		String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(amountList);
		return number;
	}
	public void set可发_普通(String number) {
		new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('2', number);
		this.getModeList().getSelfPurchaseTest().check普通采购__1采购_2改单重新排单('1', number);
		this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
	}

	public String get发货_普通(int... amountList) {
		String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(amountList);
		this.check发货开单__1全发_2部分发('1', number, 0);
		return number;
	}
	
	public String get发货_常规(int... amountList) {
		String number = new ArrangeTicketTest().get客户订单_常规(amountList);
		this.getModeList().getSelfReceiptTest().getPur备货订单_普通(amountList);
		this.check发货开单__1全发_2部分发('1', number, 0);
		return number;
	}

	public String get发货_回款常规(int... amountList) {
		String number = new ArrangeTicketTest().get客户订单_常规(amountList);
		this.getModeList().getSelfReceiptTest().getPur备货订单_普通(amountList);
		this.check发货开单__1全发_2部分发('1', number, 0);
		this.check发货回款__1全额支付_2部分支付80('1', number);
		return number;
	}
	
	protected void setQ清空() {
	}
}
