package com.haoyong.sales.test.sale;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.haoyong.sales.sale.form.OrderTicketForm;
import com.haoyong.sales.sale.form.PurchaseTicketForm;
import com.haoyong.sales.sale.logic.OrderTypeLogic;

public class LOrderTicketTest extends OrderTicketTest {
	
	public void check审核商家订单__1通过_2不通过(char type, String number, HashMap<String, Object> changeMap) {
		this.setForm(this.getModeList().getSelfOrderForm());
		OrderTicketForm form = this.getForm();
		this.loadView("AuditList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlListSelect(1);
		this.onMenu("审核");
		Assert.assertTrue("选择1个明细审核多个明细", this.getListViewValue().size()==detailCount);
		if ("1通过".length()>0 && type=='1') {
			this.onMenu("审核通过");
			OrderDetail detail=form.getSelectFormer4Order().getLast(), sdetail=detail.getSnapShot();
			Assert.assertTrue("订单生效，单头保存", detail.getStOrder()==30 && form.getDomain().getVoparam(OrderTicket.class).getId()>0);
			this.loadFormView(this.getModeList().getSelfArrangeForm(), "ArrangeList", "number", number);
			Assert.assertTrue("审核通过的客户订单待排单", this.getListViewValue().size()==detailCount);
			Assert.assertTrue("为客户订单,保持来源商家", new OrderTypeLogic().isClientType(detail.getOrderTicket().getOrderType()) && detail.getClient().getFromSellerId()==sdetail.getClient().getFromSellerId() && StringUtils.isBlank(detail.getClient().getUaccept())==false);
		}
		if ("2不通过".length()>0 && type=='2') {
			int rowi=1;
			for (Iterator<OrderDetail> iter=form.getDetailList().iterator(); iter.hasNext(); rowi++) {
				OrderDetail detail = iter.next();
				for (String key: changeMap.keySet()) {
					StringBuffer fname = new StringBuffer(key);
					form.getNoteFormer4Order().getNoteString(detail, fname);
					form.getNoteFormer4Order().getVoNoteMap(detail).put(fname.toString(), new StringBuffer().append(changeMap.get(key)).append(rowi).toString());
				}
			}
			form.getDomain().setChangeRemark(new StringBuffer().append(changeMap.keySet()).append(new Date()).toString());
			this.onMenu("不通过");
			this.loadSql("ShowQuery", "DetailForm.selectedList", "number", number);
			Assert.assertTrue("不通过的订单删除", this.getListViewValue().size()==0);
			this.setTransSeller(new Seller4lLogic().get吉高电子());
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "RechangeList", 
					"purName", "like %"+number.substring(0, number.indexOf("."))+"%", "stPurchase", 20);
			Assert.assertTrue("到采购单返单处理", this.getListViewValue().size()==detailCount);
			this.setSqlAllSelect(detailCount);
			try {this.onMenu("供应商返单处理");}catch(Exception e) {Assert.fail("不通过的商家客户订单，到采购返单处理");}
			this.setTransSeller(new Seller4lLogic().get南宁古城());
		}
	}
	
	private void test() {
		if ("备货订单".length()>0) {
			if ("审核商家订单，通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.setTestStart();
				String number = this.get待商家审核订单__1客户_2备货('2', 2,2)[1];
				this.check审核商家订单__1通过_2不通过('1', number, null);
			}
			if ("审核商家订单，改商品，不通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.setTestStart();
				String number = this.get待商家审核订单__1客户_2备货('2', 2, 2)[1];
				HashMap<String, Object> changeMap = new HashMap<String, Object>();
				changeMap.put("model", "mm");
				this.check审核商家订单__1通过_2不通过('2', number, changeMap);
			}
			if ("审核商家订单，无更改，不通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.setTestStart();
				String number = this.get待商家审核订单__1客户_2备货('2', 2, 2)[1];
				HashMap<String, Object> changeMap = new HashMap<String, Object>();
				this.check审核商家订单__1通过_2不通过('2', number, changeMap);
			}
		}
	}
	
	public void test审核商家订单() {
		if ("客户订单".length()>0) {
			if ("审核商家订单，通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.setTestStart();
				String number = this.get待商家审核订单__1客户_2备货('1', 2,2)[1];
				this.check审核商家订单__1通过_2不通过('1', number, null);
			}
			if ("审核商家订单，改商品，不通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.setTestStart();
				String number = this.get待商家审核订单__1客户_2备货('1', 2, 2)[1];
				HashMap<String, Object> changeMap = new HashMap<String, Object>();
				changeMap.put("model", "mm");
				this.check审核商家订单__1通过_2不通过('2', number, changeMap);
			}
			if ("审核商家订单，无更改，不通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.setTestStart();
				String number = this.get待商家审核订单__1客户_2备货('1', 2, 2)[1];
				HashMap<String, Object> changeMap = new HashMap<String, Object>();
				this.check审核商家订单__1通过_2不通过('2', number, changeMap);
			}
		}
		if ("备货订单".length()>0) {
			if ("审核商家订单，通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.setTestStart();
				String number = this.get待商家审核订单__1客户_2备货('2', 2,2)[1];
				this.check审核商家订单__1通过_2不通过('1', number, null);
			}
			if ("审核商家订单，改商品，不通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.setTestStart();
				String number = this.get待商家审核订单__1客户_2备货('2', 2, 2)[1];
				HashMap<String, Object> changeMap = new HashMap<String, Object>();
				changeMap.put("model", "mm");
				this.check审核商家订单__1通过_2不通过('2', number, changeMap);
			}
			if ("审核商家订单，无更改，不通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.setTestStart();
				String number = this.get待商家审核订单__1客户_2备货('2', 2, 2)[1];
				HashMap<String, Object> changeMap = new HashMap<String, Object>();
				this.check审核商家订单__1通过_2不通过('2', number, changeMap);
			}
		}
	}

	public String get客户订单(int... amountList) {
		String number = this.get待商家审核订单__1客户_2备货('1', amountList)[1];
		this.check审核商家订单__1通过_2不通过('1', number, null);
		return number;
	}
	
	public String[] get待商家审核订单__1客户_2备货(char type, int... amountList) {
		this.setTransSeller(new Seller4lLogic().get吉高电子());
		OrderDetail upOrder=null;
		String dwNumber = null;
		try {
			PurchaseTicketTest ptest = this.getModeList().getSelfPurchaseTest();
			PurchaseTicketForm form = (PurchaseTicketForm)ReflectHelper.getPropertyValue(ptest, "form");
			if (type=='1') {
				dwNumber = new OrderTicketTest().setSample(this.getSample()).get客户订单(amountList);
				new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('2', dwNumber);
				ptest.check普通采购__1采购_2改单重新排单('1', dwNumber);
			} else {
				dwNumber = new OrderTicketTest().setSample(this.getSample()).get备货订单(amountList);
				ptest.check普通采购__1采购_2改单重新排单('1', dwNumber);
			}
			upOrder = (OrderDetail)form.getDomain().getVoParamMap().get("UpOrder");
		} catch(Throwable e) {
			LogUtil.error("商家订单生成失败", e);
		} finally {
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			Assert.assertTrue("商家订单未生成", upOrder!=null && upOrder.getOrderTicket().getNumber()!=null);
		}
		return new String[]{dwNumber, upOrder.getOrderTicket().getNumber()};
	}
	
	private OrderTicketTest getSelfOrderTest() {
		if (this.getModeList().contain(TestMode.Purchase))
			return new OrderTicketTest();
		return new POrderTicketTest();
	}
}
