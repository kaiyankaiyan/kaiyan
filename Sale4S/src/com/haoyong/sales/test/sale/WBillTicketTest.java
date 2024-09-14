package com.haoyong.sales.test.sale;

import java.util.Iterator;

import net.sf.mily.types.DateType;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.form.WBillTicketForm;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.ClientTest;
import com.haoyong.sales.test.base.SupplierTest;

public class WBillTicketTest extends AbstractTest<WBillTicketForm> {
	
	public WBillTicketTest() {
		this.setForm(new WBillTicketForm());
	}
	
	public void check应收对账(Client client, String receiptBillName, String waitNumber) {
		WBillTicketForm form = this.getForm();
		this.loadView("InBill");
		this.onButton("生成单号");
		for (int i=2; i-->0 && "重复选择不会多增加明细".length()>0;) {
			this.setFieldText("client.name", client.getName());
			this.setFilters("ClientQuery.selectedList", "number", waitNumber);
			int dsize = this.getListViewValue().size();
			this.setSqlAllSelect(dsize);
			this.onMenu("选择应收");
			Assert.assertTrue("应收明细数=选择行数", form.getDetailList().size()==dsize);
		}
		for (int i=2; i-->0 && "重复选择不会多增加明细".length()>0;) {
			this.onMenu("添加收款");
			this.setFilters("ReceiptQuery.selectedList", "billName", receiptBillName);
			int dsize = this.getListViewValue().size();
			this.setSqlAllSelect(dsize);
			this.onMenu("确定");
			Assert.assertTrue("收款明细数=选择行数", form.getReachList().size()==dsize);
		}
		this.getEditListView("reachList");
		this.setEditAllSelect(this.getListViewValue().size());
		this.getEditListView("detailList");
		this.setEditAllSelect(this.getListViewValue().size());
		this.onMenu("提交收款对账");
		if (true) {
			double mreach=0, mwait=0;
			for (Iterator<BillDetail> iter=form.getSelectEdit4Reach().getSelectedList().iterator(); iter.hasNext(); mreach+=((BillDetail)iter.next().getSnapShot()).getMoney());
			for (Iterator<BillDetail> iter=form.getSelectEdit4Bill().getSelectedList().iterator(); iter.hasNext(); mwait+=((BillDetail)iter.next().getSnapShot()).getMoney());
			form.getDomain().setClient(client);
			this.loadView("ReceiptQuery", "billName", receiptBillName);
			if (mreach > mwait)
				Assert.assertTrue("有剩余收款", (Double)this.getListFootColumn("money").get(0)==mreach-mwait);
			else
				Assert.assertTrue("无剩余收款", this.getListFootColumn("money").get(0)==null);
			this.loadView("ClientQuery", "number", waitNumber);
			if (mwait > mreach)
				Assert.assertTrue("有剩余应收", (Double)this.getListFootColumn("money").get(0)==mwait-mreach);
			else
				Assert.assertTrue("无剩余收款", this.getListFootColumn("money").get(0)==null);
			double mused = 0;
			for (Iterator<BillDetail> iter=form.getSelectEdit4Reach().getSelectedList().iterator(); iter.hasNext();) {
				BillDetail reach=iter.next(), sreach=reach.getSnapShot();
				mused += sreach.getMoney();
				if (mused>mwait || mused>mreach) {
				} else {
					Assert.assertTrue("收款对账无剩余结束", reach.getStBill()==0 && reach.getMoney()==0);
					Assert.assertTrue("有对账单头信息", reach.getWBillTicket().getNumber()!=null);
				}
			}
			mused = 0;
			for (Iterator<BillDetail> iter=form.getSelectEdit4Bill().getSelectedList().iterator(); iter.hasNext();) {
				BillDetail wait=iter.next(), swait=wait.getSnapShot(), receipt=(BillDetail)wait.getVoParamMap().get("Receipt");
				mused += wait.getMoney();
				if (mused>mwait || mused>mreach) {
				} else {
					DateType dtype = new DateType();
					Assert.assertTrue("应收对账生效", wait.getStBill()==30 && StringUtils.equals(dtype.format(wait.getBillTicket().getBillDate()), dtype.format(receipt.getBillTicket().getBillDate())) && swait.getBillTicket().getBillDate()==null);
					Assert.assertTrue("有对账单头信息", wait.getWBillTicket().getNumber()!=null);
				}
			}
		}
	}
	
	public void check应付对账(Supplier supplier, String paidBillName, String waitNumber) {
		WBillTicketForm form = this.getForm();
		this.loadView("OutBill");
		this.onButton("生成单号");
		for (int i=2; i-->0 && "重复选择不会多增加明细".length()>0;) {
			this.setFieldText("supplier.name", supplier.getName());
			this.setFilters("SupplierQuery.selectedList", "number", waitNumber);
			int dsize = this.getListViewValue().size();
			this.setSqlAllSelect(dsize);
			this.onMenu("选择应付");
			Assert.assertTrue("应付明细数=选择行数", form.getDetailList().size()==dsize);
		}
		for (int i=2; i-->0 && "重复选择不会多增加明细".length()>0;) {
			this.onMenu("添加付款");
			this.setFilters("PaidQuery.selectedList", "billName", paidBillName);
			int dsize = this.getListViewValue().size();
			this.setSqlAllSelect(dsize);
			this.onMenu("确定");
			Assert.assertTrue("付款明细数=选择行数", form.getReachList().size()==dsize);
		}
		this.getEditListView("reachList");
		this.setEditAllSelect(this.getListViewValue().size());
		this.getEditListView("detailList");
		this.setEditAllSelect(this.getListViewValue().size());
		this.onMenu("提交付款对账");
		if (true) {
			double mreach=0, mwait=0;
			for (Iterator<BillDetail> iter=form.getSelectEdit4Reach().getSelectedList().iterator(); iter.hasNext(); mreach+=((BillDetail)iter.next().getSnapShot()).getMoney());
			for (Iterator<BillDetail> iter=form.getSelectEdit4Bill().getSelectedList().iterator(); iter.hasNext(); mwait+=((BillDetail)iter.next().getSnapShot()).getMoney());
			form.getDomain().setSupplier(supplier);
			this.loadView("PaidQuery", "billName", paidBillName);
			if (Math.abs(mreach) > Math.abs(mwait))
				Assert.assertTrue("有剩余付款", (Double)this.getListFootColumn("money").get(0)==mreach-mwait);
			else
				Assert.assertTrue("无剩余付款", this.getListFootColumn("money").get(0)==null);
			this.loadView("SupplierQuery", "number", waitNumber);
			if (Math.abs(mwait) > Math.abs(mreach))
				Assert.assertTrue("有剩余应付", (Double)this.getListFootColumn("money").get(0)==mwait-mreach);
			else
				Assert.assertTrue("无剩余付款", this.getListFootColumn("money").get(0)==null);
			double mused = 0;
			for (Iterator<BillDetail> iter=form.getSelectEdit4Reach().getSelectedList().iterator(); iter.hasNext();) {
				BillDetail reach=iter.next(), sreach=reach.getSnapShot();
				mused += sreach.getMoney();
				if (Math.abs(mused)>Math.abs(mwait) || Math.abs(mused)>Math.abs(mreach)) {
				} else {
					Assert.assertTrue("付款对账无剩余结束", reach.getStBill()==0 && reach.getMoney()==0);
					Assert.assertTrue("有对账单头信息", reach.getWBillTicket().getNumber()!=null);
				}
			}
			mused = 0;
			for (Iterator<BillDetail> iter=form.getSelectEdit4Bill().getSelectedList().iterator(); iter.hasNext();) {
				BillDetail wait=iter.next(), swait=wait.getSnapShot();
				mused += wait.getMoney();
				if (Math.abs(mused)>Math.abs(mwait) || Math.abs(mused)>Math.abs(mreach)) {
				} else {
					Assert.assertTrue("应付对账生效", wait.getStBill()==30 && wait.getBillTicket().getBillDate()!=null && swait.getBillTicket().getBillDate()==null);
					Assert.assertTrue("有对账单头信息", wait.getWBillTicket().getNumber()!=null);
				}
			}
		}
	}
	
	public void test收款对账() {
		if ("应收10*20=200，收款190，剩余应收10".length()>0) {
			this.setTestStart();
			String number=new SendTicketTest().get发货_普通(10);
			String reachName=new BillTicketTest().get收款(new ClientTest().get幸亚(), 190);
			this.check应收对账(new ClientTest().get幸亚(), reachName, number);
		}
		if ("应收7*17*3=357，收款400，剩余收款43".length()>0) {
			this.setTestStart();
			String number=new SendTicketTest().get发货_普通(7, 7, 7);
			String reachName=new BillTicketTest().get收款(new ClientTest().get幸亚(), 400);
			this.check应收对账(new ClientTest().get幸亚(), reachName, number);
		}
		if ("应收20*30=600，收款190*3=570，剩余应收30".length()>0) {
			this.setTestStart();
			String number=new SendTicketTest().get发货_普通(20);
			String reachName=new BillTicketTest().get收款(new ClientTest().get幸亚(), 190, 190, 190);
			this.check应收对账(new ClientTest().get幸亚(), reachName, number);
		}
		if ("应收6*16*2=192，收款70*3=210，剩余收款18".length()>0) {
			this.setTestStart();
			String number=new SendTicketTest().get发货_普通(6, 6);
			String reachName=new BillTicketTest().get收款(new ClientTest().get幸亚(), 70, 70, 70);
			this.check应收对账(new ClientTest().get幸亚(), reachName, number);
		}
	}
	
	public void test付款对账() {
		if ("应付10*20=200，付款190，剩余应付10".length()>0) {
			this.setTestStart();
			String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(10);
			String reachName=new BillTicketTest().get付款(new SupplierTest().get永晋(), 190);
			this.check应付对账(new SupplierTest().get永晋(), reachName, number);
		}
		if ("应付7*17*3=357，付款400，剩余付款43".length()>0) {
			this.setTestStart();
			String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(7, 7, 7);
			String reachName=new BillTicketTest().get付款(new SupplierTest().get永晋(), 400);
			this.check应付对账(new SupplierTest().get永晋(), reachName, number);
		}
		if ("应付20*30=600，付款190*3=570，剩余应付30".length()>0) {
			this.setTestStart();
			String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(20);
			String reachName=new BillTicketTest().get付款(new SupplierTest().get永晋(), 190, 190, 190);
			this.check应付对账(new SupplierTest().get永晋(), reachName, number);
		}
		if ("应付6*16*2=192，付款70*3=210，剩余付款18".length()>0) {
			this.setTestStart();
			String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(6, 6);
			String reachName=new BillTicketTest().get付款(new SupplierTest().get永晋(), 70, 70, 70);
			this.check应付对账(new SupplierTest().get永晋(), reachName, number);
		}
	}
	
	protected void setQ清空() {
		
	}
}
