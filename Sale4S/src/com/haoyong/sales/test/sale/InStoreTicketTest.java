package com.haoyong.sales.test.sale;

import org.junit.Assert;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.form.InStoreTicketForm;
import com.haoyong.sales.sale.form.SaleQueryForm;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.CommodityTest;
import com.haoyong.sales.util.SSaleUtil;

public class InStoreTicketTest extends AbstractTest<InStoreTicketForm> {
	
	public InStoreTicketTest() {
		this.setForm(new InStoreTicketForm());
	}
	
	public void check内部入库(Commodity comm, double... amountList) {
		InStoreTicketForm form = this.getForm();
		OrderDetail purchase = new StoreTicketTest().get备货库存1个();
		if ("新增".length()>0) {
			this.loadView("InStore");
			this.onButton("生成单号");
			form.getDomain().getInstoreTicket().setType("内部入库");
			form.getDomain().getPurchaseTicket().setRemark(new StringBuffer().append(comm.getName()).append(amountList).toString());
			if (true) {
				OrderDetail detail = form.getDetailList().get(0);
				new PurchaseTicketLogic().setPurchaseTicket(purchase, detail);
				detail.setCommodity(comm);
				detail.setAmount(amountList[0]);
				detail.setPrice(10+amountList[0]);
			}
			for (int i=1; i<amountList.length; i++) {
				this.onMenu("添加明细");
				OrderDetail detail = form.getDetailList().get(i);
				new PurchaseTicketLogic().setPurchaseTicket(purchase, detail);
				detail.setCommodity(comm);
				detail.getCommodity().setName(detail.getCommodity().getName() + i);
				detail.setAmount(amountList[i]);
				detail.setPrice(10+amountList[i]);
			}
			new StoreTicketTest().setQ清空();
			this.onMenu("提交入库");
			for (OrderDetail detail: form.getDetailList()) {
				Assert.assertTrue("每个入库明细都有入库单号", detail.getInstoreTicket().getNumber()!=null);
			}
			if (true) {
				this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
				Assert.assertTrue("库存数要重计算", this.getListViewValue().size()>0);
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("库存够用数要重计算", this.getListViewValue().size()>0);
			}
			new StoreTicketTest().checkInStoreList((StoreTicketForm)form.getFormProperty("attrMap.StoreTicketForm"));
		}
	}
	
	public void test内部入库() {
		if ("同种商品连续入库".length()>0) {
			this.setTestStart();
			for (int i=2; i-->0; ) {
				this.check内部入库(new CommodityTest().getC浆料(), 11, 12);
				this.check内部入库(new CommodityTest().getC白棒(), 11, 12);
				this.check内部入库(new CommodityTest().getC铁帽(), 11, 12);
			}
		}
	}
	
	private void temp() {
		this.get内部入库(new CommodityTest().getC铁帽(), 20);
		this.get内部入库(new CommodityTest().getC浆料(), 20);
		new ArrangeTicketTest().get客户订单_普通(2);
	}
	
	public void get内部入库(int amount, Commodity... commList) {
		OrderDetail purchase = new StoreTicketTest().get备货库存1个();
		for (Commodity comm: commList) {
			OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
			pur.setCommodity(comm);
			pur.setAmount(amount);
			pur.setMonthnum(new OrderTicketLogic().genMonthnum());
			SSaleUtil.saveOrUpdate(pur);
		}
	}
	
	public void get内部入库(Commodity comm, int... amountList) {
		OrderDetail purchase = new StoreTicketTest().get备货库存1个();
		for (int amount: amountList) {
			OrderDetail pur = new PurchaseTicketLogic().genClonePurchase(purchase);
			pur.setCommodity(comm);
			pur.setAmount(amount);
			pur.setMonthnum(new OrderTicketLogic().genMonthnum());
			SSaleUtil.saveOrUpdate(pur);
		}
	}

	protected void setQ清空() {
		
	}
}
