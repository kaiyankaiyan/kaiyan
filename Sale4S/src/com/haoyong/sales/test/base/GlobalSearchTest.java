package com.haoyong.sales.test.base;

import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.util.ReflectHelper;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.common.derby.DerbyDAO;
import com.haoyong.sales.common.form.MainForm;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.form.GlobalSearchForm;
import com.haoyong.sales.test.sale.ArrangeTicketTest;
import com.haoyong.sales.test.sale.OrderTicketTest;
import com.haoyong.sales.test.sale.SendTicketTest;

public class GlobalSearchTest extends AbstractTest<GlobalSearchForm> {
	
	public GlobalSearchTest() {
		this.setForm(new GlobalSearchForm());
		MainForm mform = new MainForm();
		mform.getComponent();
		this.getForm().setFormProperty("attrMap.MainForm", mform);
	}
	
	protected String check搜索__1月流水号_2订单号(String input, int... caseList) {
		this.loadView("Search");
		TicketPropertyUtil.copyFieldsSkip(new OrderDetail(), this.getForm().getOrderDetail());
		String foundSearch = null;
		if ("按明细月流水号搜索".length()>0 && caseList[0]>0) {
			this.getForm().getOrderDetail().setMonthnum(input);
			this.onMenu("搜索");
			foundSearch = this.getForm().getFormProperty("SearchCountListener.inputs.voParamMap.FoundString");
			MainForm mform = this.getForm().getFormProperty("attrMap.MainForm");
			ReflectHelper.invokeMethod(mform, "RefreshListener.setSearchList");
			String foundMain = (String)mform.getFormProperty("RefreshListener.user.voParamMap.SearchFoundString");
			if (foundSearch.length()>5) {
				String s1 = foundSearch.substring(foundSearch.lastIndexOf(",", foundSearch.length()-5));
				String s2 = foundMain.substring(foundMain.lastIndexOf(",", foundSearch.length()-5));
				Assert.assertTrue("主界面的待处理Right数一致", StringUtils.equals(s1, s2));
			}
			if (true) {
				this.loadView("Search");
				Assert.assertTrue("加载保存的上次搜索的月流水号", this.getForm().getDomain().getId()>0 && StringUtils.equals(this.getForm().getOrderDetail().getMonthnum(), input));
			}
		}
		if ("按订单号搜索".length()>0 && caseList[1]>0) {
			this.getForm().getOrderDetail().getOrderTicket().setNumber(input);
			this.onMenu("搜索");
			foundSearch = this.getForm().getFormProperty("SearchCountListener.inputs.voParamMap.FoundString");
			MainForm mform = this.getForm().getFormProperty("attrMap.MainForm");
			ReflectHelper.invokeMethod(mform, "RefreshListener.setSearchList");
			String foundMain = (String)mform.getFormProperty("RefreshListener.user.voParamMap.SearchFoundString");
			if (foundSearch.length()>5) {
				String s1 = foundSearch.substring(foundSearch.lastIndexOf(",", foundSearch.length()-5));
				String s2 = foundMain.substring(foundMain.lastIndexOf(",", foundSearch.length()-5));
				Assert.assertTrue("主界面的待处理Right数一致", StringUtils.equals(s1, s2));
			}
			if (true) {
				this.loadView("Search");
				Assert.assertTrue("加载保存的上次搜索的订单号", this.getForm().getDomain().getId()>0 && StringUtils.equals(this.getForm().getOrderDetail().getOrderTicket().getNumber(), input));
			}
		}
		return foundSearch;
	}
	
	private void temp() {
		if ("首次搜索，无默认搜索条件".length()>0) {
			this.setTestStart();
			this.setQ清空();
			this.loadView("Search");
			Assert.assertTrue("无默认搜索条件", this.getForm().getDomain().getId()==0 && this.getForm().getOrderDetail().getMonthnum()==null && this.getForm().getOrderDetail().getOrderTicket().getNumber()==null);
		}
		if ("搜索月流水号".length()>0) {
			this.setTestStart();
			this.setQ清空();
			String number = this.getModeList().getSelfOrderTest().get客户订单(11, 12);
			String monthnum = new OrderTicketTest().getOrderList("number", number, "amount", 11).get(0).getMonthnum();
			String foundString = this.check搜索__1月流水号_2订单号(monthnum, 1,0);
			Assert.assertTrue("找到订单明细记录", foundString.contains("订单安排"));
		}
		if ("搜索订单号".length()>0) {
			this.setTestStart();
			this.setQ清空();
			String number = this.getModeList().getSelfOrderTest().get客户订单(11, 12);
			String foundString = this.check搜索__1月流水号_2订单号(number, 0,2);
			Assert.assertTrue("找到订单记录", foundString.contains("订单安排"));
		}
	}
	
	public void test搜索() {
		if ("首次搜索，无默认搜索条件".length()>0) {
			this.setTestStart();
			this.setQ清空();
			this.loadView("Search");
			Assert.assertTrue("无默认搜索条件", this.getForm().getDomain().getId()==0 && this.getForm().getOrderDetail().getMonthnum()==null && this.getForm().getOrderDetail().getOrderTicket().getNumber()==null);
		}
		if ("搜索月流水号".length()>0) {
			this.setTestStart();
			this.setQ清空();
			String number = this.getModeList().getSelfOrderTest().get客户订单(11, 12);
			String monthnum = new OrderTicketTest().getOrderList("number", number, "amount", 11).get(0).getMonthnum();
			String foundString = this.check搜索__1月流水号_2订单号(monthnum, 1,0);
			Assert.assertTrue("找到订单明细记录", foundString.contains("订单安排"));
		}
		if ("搜索订单号".length()>0) {
			this.setTestStart();
			this.setQ清空();
			String number = this.getModeList().getSelfOrderTest().get客户订单(11, 12);
			String foundString = this.check搜索__1月流水号_2订单号(number, 0,2);
			Assert.assertTrue("找到订单记录", foundString.contains("订单安排"));
		}
		if ("搜索订单号，去订单安排do请购，去采购do，去收货do，去订单发货do，搜不到".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfOrderTest().get客户订单(11, 12);
			String foundString = null;
			foundString = this.check搜索__1月流水号_2订单号(number, 0,2);
			Assert.assertTrue("在订单安排", foundString.contains("订单安排"));
			new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('2', number);
			foundString = this.check搜索__1月流水号_2订单号(number, 0,2);
			Assert.assertTrue("在采购", foundString.contains("采购单-普通开单"));
			this.getModeList().getSelfPurchaseTest().check普通采购__1采购_2改单重新排单('1', number);
			foundString = this.check搜索__1月流水号_2订单号(number, 0,2);
			Assert.assertTrue("在收货", foundString.contains("收货开单"));
			this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
			foundString = this.check搜索__1月流水号_2订单号(number, 0,2);
			Assert.assertTrue("在发货", foundString.contains("发货开单"));
			new SendTicketTest().check发货开单__1全发_2部分发('1', number, 0);
			foundString = this.check搜索__1月流水号_2订单号(number, 0,2);
			Assert.assertTrue("已完成找不到", foundString.contains("撤销发货"));
		}
	}

	@Override
	protected void setQ清空() {
		DerbyDAO dao = new DerbyDAO();
		dao.getSQLQuery("delete from bs_SellerViewInputs where id=? and sellerId=?", this.getForm().getDomain().getId()).executeUpdate();
		dao.getSession().flush();
		this.getModeList().setOrderTime(null);
	}
}
