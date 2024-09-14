package com.haoyong.sales.test.sale;

import org.junit.Assert;

import com.haoyong.sales.base.logic.BillTypeLogic;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.form.OutStoreTicketForm;
import com.haoyong.sales.sale.form.SaleQueryForm;
import com.haoyong.sales.sale.logic.OrderTypeLogic;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.ClientTest;
import com.haoyong.sales.test.base.UserTest;

public class OutStoreTicketTest extends AbstractTest<OutStoreTicketForm> {
	
	public OutStoreTicketTest() {
		this.setForm(new OutStoreTicketForm());
	}
	
	public String check销售出库_1全出_2部分出3(char type, Double storePrice, Object... storeFilters0) {
		Object[] storeFilters = this.genFiltersStart(storeFilters0, "selectedList");
		OutStoreTicketForm form = this.getForm();
		this.loadView("OutStore4Sale");
		this.onButton("生成单号");
		if ("选择出库明细".length()>0) {
			this.onRowButton("选择", 1);
			this.setFilters(storeFilters);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("确定");
		}
		for (OrderDetail order: form.getDetailList()) {
			OrderDetail pur = order.getVoparam(OrderDetail.class);
			order.setAmount(type=='1'? pur.getAmount(): 3);
			order.getOrderTicket().setCprice(pur.getPrice()+5);
			if (storePrice != null)
				order.getReceiptTicket().setStorePrice(storePrice);
		}
		if ("填写客户名称自动加载客户联系人、电话".length()>0) {
			this.setFieldText("client.name", new ClientTest().get幸亚().getName());
		}
		new StoreTicketTest().setQ清空();
		this.onMenu("提交销售");
		BaseDAO dao = new BaseDAO();
		for (OrderDetail order: form.getDetailList()) {
			OrderDetail spur=order.getVoparam(OrderDetail.class), pur=dao.load(spur);
			BillDetail bill = order.getVoparam(BillDetail.class);
			Assert.assertTrue("明细与单头一致", form.getDomain().getClient().getLinker().equals(order.getClient().getLinker())
					&& form.getDomain().getSendTicket().getNumber().equals(order.getSendTicket().getNumber())
					&& form.getDomain().getOrderTicket().getNumber().equals(order.getOrderTicket().getNumber()));
			Assert.assertTrue("订单有发货单号、发货日期", order.getSendTicket().getNumber()!=null && order.getSendTicket().getSendDate()!=null);
//			Assert.assertTrue("订单明细记出货库存分账", StringUtils.isBlank(order.getLocationTicket().getOut().getName())==false);
			Assert.assertTrue("库存数量减少", spur.getAmount()-pur.getAmount()==order.getAmount());
			Assert.assertTrue("出货订单有出库单号，订单有效，已安排，已发货", order.getSendTicket().getNumber()!=null && order.getStOrder()==30 && order.getArrangeId()==30 && order.getSendId()==30);
			Assert.assertTrue("订单发货有应收", bill!=null && bill.getId()>0 && bill.getClient().getName()!=null 
					&& bill.getPrice()==order.getOrderTicket().getCprice() && bill.getMoney()>0 && new BillTypeLogic().getSaleType().equals(bill.getBillTicket().getTypeName()));
			Assert.assertTrue("0金额应收立即生效、有收款日期", bill.getMoney()==0? (bill.getStBill()==30 && bill.getBillTicket().getBillDate()!=null): (bill.getStBill()==20 && bill.getBillTicket().getBillDate()==null));
			if ("全部出库".length()>0 && type=='1')
				Assert.assertTrue("采购单库存出货清空", pur.getStPurchase()==(pur.getReceiptTicket().getStoreMoney()==0? 0: ((OrderDetail)pur.getSnapShot()).getStPurchase()) && pur.getAmount()==0);
			if ("部分出库".length()>0 && type=='2')
				Assert.assertTrue("部分发货的订单用新分支流水号", order.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0].concat("-")));
		}
		if (type=='2') {
			this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
			Assert.assertTrue("部分出货后库存重计算", this.getListViewValue().size()>0);
			this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
			Assert.assertTrue("出库后够用数重计算", this.getListViewValue().size()>0);
		}
		new StoreTicketTest().checkOutStoreList(form.getOut4StoreTicketForm());
		return form.getDomain().getOrderTicket().getNumber();
	}
	
	public String check内部出库_1全出_2部分出(char type, String purName, int outAmount, Double storePrice) {
		OutStoreTicketForm form = this.getForm();
		this.loadView("OutStore4Self");
		this.onButton("生成单号");
		form.getDomain().getOutstoreTicket().setType("内部出库");
		if ("选择出库明细".length()>0) {
			this.onRowButton("选择", 1);
			this.setFilters("selectedList", "purName", purName);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("确定");
		}
		for (OrderDetail order: form.getDetailList()) {
			order.setAmount(outAmount);
			order.setPrice(order.getPrice()+5);
			if (storePrice != null)
				order.getReceiptTicket().setStorePrice(storePrice);
		}
		new StoreTicketTest().setQ清空();
		this.onMenu("提交出库");
		BaseDAO dao = new BaseDAO();
		for (OrderDetail order: form.getDetailList()) {
			OrderDetail spur=order.getVoparam(OrderDetail.class), pur=dao.load(spur);
			BillDetail bill = order.getVoparam(BillDetail.class);
			Assert.assertTrue("明细与单头一致", order.getClient().getLinker()==null
					&& form.getDomain().getSendTicket().getNumber().equals(order.getSendTicket().getNumber())
					&& form.getDomain().getOrderTicket().getNumber().equals(order.getOrderTicket().getNumber()));
//			Assert.assertTrue("订单明细记出货库存分账", StringUtils.isBlank(order.getLocationTicket().getOut().getName())==false);
			Assert.assertTrue("库存数量减少", spur.getAmount()-pur.getAmount()==outAmount);
			Assert.assertTrue("出货订单有出库单号，是备货订单,已安排,已发货", order.getSendTicket().getNumber()!=null
					&& new OrderTypeLogic().isBackType(order.getOrderTicket().getOrderType()) && order.getArrangeId()==30 && order.getSendId()==30);
			Assert.assertTrue("内部出库没应收", bill==null);
			if ("全部出库".length()>0 && type=='1'){
				Assert.assertTrue("采购单库存出货清空", pur.getStPurchase()==0 && pur.getAmount()==0);
			}
			if ("部分出库".length()>0 && type=='2') {
				Assert.assertTrue("部分发货的订单用新分支流水号", order.getMonthnum().startsWith(spur.getMonthnum().split("\\-")[0].concat("-")));
			}
		}
		if (type=='2') {
			this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
			Assert.assertTrue("出货后库存重计算", this.getListViewValue().size()>0);
			this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
			Assert.assertTrue("出库后够用数重计算", this.getListViewValue().size()>0);
		}
		new StoreTicketTest().checkOutStoreList(form.getOut4StoreTicketForm());
		return form.getDomain().getOrderTicket().getNumber();
	}
	
	public void test销售出货() {
if (1==1) {
		if ("备货库存全数出货".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get备货订单_普通(10, 10);
			this.check销售出库_1全出_2部分出3('1', null, new Object[]{"number",number});
		}
		if ("备货订单收货10，先出货3默认出库价，再出货剩余的全部7默认出库价".length()>0) {
			this.setTestStart();
			String purName = this.getModeList().getSelfReceiptTest().get备货订单_普通(10, 10);
			this.check销售出库_1全出_2部分出3('2', null, new Object[]{"number",purName});
			this.check销售出库_1全出_2部分出3('1', null, new Object[]{"number",purName});
		}
}
		if ("安装师傅分账出货，只能看到自己分账的库存".length()>0) {
			this.setTestStart();
			String purName1 = new LocationTicketTest().get安装领班分账_备货(new UserTest().getUser安装师傅01(), 11);
			String purName2 = this.getModeList().getSelfReceiptTest().getPur备货订单_普通(12, 12);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.loadView("OutStore4Sale");
			this.onRowButton("选择", 1);
			this.setFilters("selectedList", "purName", new String[]{purName1, purName2});
			Assert.assertTrue("安装师傅只能看到自己的分账", this.getListViewValue().size()==1);
		}
		if ("安装师傅分账出货，订单发货单头记出货的分账outstore".length()>0) {
			this.setTestStart();
			String purName = new LocationTicketTest().get安装领班分账_备货(new UserTest().getUser安装师傅01(), 11, 11);
			this.setTransUser(new UserTest().getUser安装师傅01());
			this.check销售出库_1全出_2部分出3('2', null, new Object[]{"purName",purName});
			this.check销售出库_1全出_2部分出3('1', null, new Object[]{"purName",purName});
		}
	}
	
	public void test内部出库() {
		if ("备货库存全数出货".length()>0) {
			this.setTestStart();
			String purName = this.getModeList().getSelfReceiptTest().getPur备货订单_普通(10, 10);
			this.check内部出库_1全出_2部分出('1', purName, 10, null);
		}
		if ("备货订单收货10，先出货7默认出库价，再出货3默认出库价".length()>0) {
			this.setTestStart();
			String purName = this.getModeList().getSelfReceiptTest().getPur备货订单_普通(10, 10);
			this.check内部出库_1全出_2部分出('2', purName, 7, null);
			this.check内部出库_1全出_2部分出('1', purName, 3, null);
		}
	}
	
	private void temp() {
		this.get内部出库(1);
	}
	
	public String get销售出库(int... amountList) {
		String numback = this.getModeList().getSelfReceiptTest().get备货订单_普通(amountList);
		String number = this.check销售出库_1全出_2部分出3('1', null, new Object[]{"number",numback});
		return number;
	}
	public String get内部出库(int... amountList) {
		String purName = this.getModeList().getSelfReceiptTest().getPur备货订单_普通(amountList);
		String number = this.check内部出库_1全出_2部分出('1', purName, 10, null);
		return number;
	}
	
	protected void setQ清空() {
		
	}

}
