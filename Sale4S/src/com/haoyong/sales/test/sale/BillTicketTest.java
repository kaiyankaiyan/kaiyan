package com.haoyong.sales.test.sale;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.logic.BillTypeLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.form.BillTicketForm;
import com.haoyong.sales.sale.logic.BillTicketLogic;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.ClientTest;
import com.haoyong.sales.test.base.SupplierTest;
import com.haoyong.sales.util.SSaleUtil;

public class BillTicketTest extends AbstractTest<BillTicketForm> {
	
	public BillTicketTest() {
		this.setForm(new BillTicketForm());
	}
	
	public void check收款(Client client, int... moneyList) {
		BillTicketForm form = this.getForm();
		this.loadView("InBill");
		this.onButton("生成单号");
		this.setFieldText("client.name", client.getName());
		form.getDomain().getBillTicket().setTypeName(new BillTypeLogic().getSaleType());
		for (int i=moneyList.length; i-->1; form.addDetail());
		int mi=0, msum=0;
		for (BillDetail detail: form.getDetailList()) {
			msum += moneyList[mi];
			detail.getCommodity().setName(new StringBuffer("客户订购预付款").append(Calendar.getInstance().getTimeInMillis()).toString());
			detail.setMoney(moneyList[mi++]);
		}
		this.onMenu("提交收款");
		if (true) {
			for (BillDetail detail: form.getDetailList()) {
				Assert.assertTrue("明细跟单头一致", StringUtils.equals(detail.getClient().getName(), form.getDomain().getClient().getName())
						&& detail.getBillTicket().getBillDate()!=null
						&& StringUtils.equals(detail.getBillTicket().getTypeName(), form.getDomain().getBillTicket().getTypeName())
						&& form.getDomain().getBillTicket().getNumber()!=null && StringUtils.equals(detail.getBillTicket().getNumber(), form.getDomain().getBillTicket().getNumber())
						);
				Assert.assertTrue("待对账状态", detail.getStBill()==30);
			}
			this.loadView("ShowQuery",
					"billName", new BillTicketLogic().getPropertyChoosableLogic().toTrunk(form.getDomain().getBillTicket()).getBillName());
			Assert.assertTrue("在待收查询", (Double)this.getListFootColumn("money").get(0)==msum);
		}
	}
	
	public void check付款(Supplier supplier, int... moneyList) {
		BillTicketForm form = this.getForm();
		this.loadView("OutBill");
		this.onButton("生成单号");
		this.setFieldText("supplier.name", supplier.getName());
		form.getDomain().getBillTicket().setTypeName(new BillTypeLogic().getPurchaseType());
		for (int i=moneyList.length; i-->1; form.addDetail());
		int mi=0, msum=0;
		for (BillDetail detail: form.getDetailList()) {
			msum += moneyList[mi];
			detail.getCommodity().setName(new StringBuffer("采购预付款").append(Calendar.getInstance().getTimeInMillis()).toString());
			detail.setMoney(moneyList[mi++]);
		}
		this.onMenu("提交付款");
		if (true) {
			for (BillDetail detail: form.getDetailList()) {
				Assert.assertTrue("明细跟单头一致", StringUtils.equals(detail.getSupplier().getName(), form.getDomain().getSupplier().getName())
						&& detail.getBillTicket().getBillDate()!=null
						&& StringUtils.equals(detail.getBillTicket().getTypeName(), form.getDomain().getBillTicket().getTypeName())
						&& form.getDomain().getBillTicket().getNumber()!=null && StringUtils.equals(detail.getBillTicket().getNumber(), form.getDomain().getBillTicket().getNumber())
						);
				Assert.assertTrue("待对账状态", detail.getStBill()==30 && detail.getMoney()<0);
			}
			this.loadView("ShowQuery",
					"billName", new BillTicketLogic().getPropertyChoosableLogic().toTrunk(form.getDomain().getBillTicket()).getBillName());
			Assert.assertTrue("在待收查询", (Double)this.getListFootColumn("money").get(0)==msum*-1);
		}
	}
	
	private void test收款() {
		if ("多个收款明细".length()>0) {
			this.setTestStart();
			this.check收款(new ClientTest().get幸亚(), 10, 20, 30, 40);
			this.check收款(new ClientTest().get幸亚(), 10, 20, 30, 40);
		}
	}
	
	private void test付款() {
		if ("多个付款明细".length()>0) {
			this.setTestStart();
			this.check付款(new SupplierTest().get永晋(), 10, 20, 30, 40);
			this.check付款(new SupplierTest().get永晋(), 10, 20, 30, 40);
		}
	}
	
	public String get收款(Client client, int... moneyList) {
		this.check收款(client, moneyList);
		return new BillTicketLogic().getPropertyChoosableLogic().toTrunk(this.getForm().getDomain().getBillTicket()).getBillName();
	}
	
	public String get付款(Supplier supplier, int... moneyList) {
		this.check付款(supplier, moneyList);
		return new BillTicketLogic().getPropertyChoosableLogic().toTrunk(this.getForm().getDomain().getBillTicket()).getBillName();
	}
	
	public List<BillDetail> getBillDetail(Object... filters) {
		this.loadView("ShowQuery", filters);
		this.setSqlAllSelect(this.getListViewValue().size());
		this.onMenu("选择应收");
		return this.getForm().getSelectFormer4Bill().getSelectedList();
	}
	
	protected void setQ清空() {
		String sql = "delete from sa_BillDetail where sellerId=?";
		SSaleUtil.executeSqlUpdate(sql);
	}
}
