package com.haoyong.sales.test.sale;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DateType;
import net.sf.mily.types.DoubleType;
import net.sf.mily.types.TimeType;
import net.sf.mily.ui.BlockGrid;
import net.sf.mily.ui.BlockGrid.BlockCell;
import net.sf.mily.ui.BlockGrid.BlockRow;
import net.sf.mily.ui.BlockgridList;
import net.sf.mily.ui.BlockgridList.BlockgridTr;
import net.sf.mily.ui.Text;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.enumeration.BlockGridMode;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SellerLogic;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.form.ArrangeTicketForm;
import com.haoyong.sales.sale.form.DOrderTicketForm;
import com.haoyong.sales.sale.form.OrderTicketForm;
import com.haoyong.sales.sale.form.ReceiptTicketForm;
import com.haoyong.sales.sale.form.SaleQueryForm;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.ClientTest;
import com.haoyong.sales.test.base.CommodityTest;
import com.haoyong.sales.test.base.SubCompanyTest;
import com.haoyong.sales.test.base.SupplierTest;
import com.haoyong.sales.test.base.UserTest;
import com.haoyong.sales.util.SSaleUtil;

public class OrderTicketTest extends AbstractTest<OrderTicketForm> implements TRemind {

	public OrderTicketTest() {
		this.setForm(this.getModeList().contain(TestMode.SubCompany)? new DOrderTicketForm(): new OrderTicketForm());
	}
	
	public void check改单申请__1不同意_2通过_3通过改0(char type, String number) {
		OrderTicketForm form = this.getForm();
		if ("1返订单改单处理，不同意".length()>0 && type=='1') {
			this.loadView("RechangeList", "number", number);
			Assert.assertTrue("改单申请没有返订单改单", this.getListViewValue().size()>0);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			try {
				this.onMenu("采购改单处理");
				Assert.fail("排单发起的改单申请，应不能采购改单处理");
			}catch(Exception e) {
			}
			this.onMenu("订单改单处理");
			if (form.getDomain().getClient().getName()!=null)
				Assert.assertTrue("客户订单应显示客户名称", this.hasField("client.name"));
			this.setEditAllSelect(detailCount);
			this.onMenu("移除明细");
			Assert.assertTrue("移除后没有明细应返回外层", this.hasMenu("移除明细")==false);
			this.setFilters("number", number);
			this.setSqlAllSelect(detailCount);
			this.onMenu("订单改单处理");
			form.getDomain().setChangeRemark("改客户人，改商品，不同意");
			this.onMenu("不同意");
			this.setFilters("number", number);
			Assert.assertTrue("改单申请不同意失败", this.getListViewValue().size()==0);
		}
		if ("2返订单通过".length()>0 && type=='2') {
			this.loadView("RechangeList", "number", number);
			Assert.assertTrue("改单申请没有返订单改单", this.getListViewValue().size()>0);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("订单改单处理");
			new StoreTicketTest().setQ清空();
			this.onMenu("改单同意");
			this.setFilters("number", number);
			Assert.assertTrue("改单同意失败", this.getListViewValue().size()==0);
			this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
			Assert.assertTrue("改单同意要更新库存够用数", this.getListViewValue().size()>0);
		}
		if ("3返订单通过0数量".length()>0 && type=='3') {
			this.loadView("RechangeList", "number", number);
			Assert.assertTrue("改单申请没有返订单改单", this.getListViewValue().size()>0);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("订单改单处理");
			this.onMenu("改单同意");
			this.setFilters("number", number);
			Assert.assertTrue("改单同意失败", this.getListViewValue().size()==0);
			this.loadSql("ShowQuery", "DetailForm.selectedList", "number", number, "amount", "0");
			Assert.assertTrue("改单同意要保存0数量订单", this.getListViewValue().size()==detailCount);
			this.loadView("DoadjustList", "number", number, "amount", "0");
			Assert.assertTrue("数量为0应不能订单红冲", this.getListViewValue().size()==0);
		}
	}
	
	public void check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿(String types, String number) {
		OrderTicketForm form = this.getForm();
		this.loadView("DoadjustList", "number", number);
		int detailCount = this.getListViewValue().size();
		Assert.assertTrue("订单应在已排单红冲", detailCount>0);
		this.setSqlAllSelect(detailCount);
		this.onMenu("已排单红冲");
		if ("是客户商家开的红冲草稿".length()>0 && types.indexOf('4')>-1) {
			Assert.assertTrue("待已客户商家红冲申请处理", this.hasField("domain.voParamMap.ClientDoadjust"));
			OrderDetail order = form.getSelectFormer4Order().getLast();
			for (String kprop: form.getNoteFormer4Order().getVoNoteMap(order).keySet()) {
				if (this.hasField(kprop))
					Assert.assertTrue("有红冲草稿内容", this.hasNoteWrite(kprop));
			}
			this.setEditAllSelect(detailCount);
			this.onButton("已客户商家红冲申请处理");
			Assert.assertTrue("已客户商家红冲申请处理", this.hasField("domain.voParamMap.ClientDoadjust")==false && order.getStOrder()==30);
		}
		if ("1已排单红冲".length()>0 && types.indexOf('1')>-1) {
			boolean hasNumber2=this.hasField("OrderTicket.number2"), hasClient=this.hasField("client.name");
			if (hasNumber2)
				this.setNoteText("OrderTicket.number2", "人工订单号"+new Date().getTime());
			if (hasClient)
				this.setNoteText("client.name", form.getDomain().getClient().getName().concat("1"));
			StringBuffer fname = new StringBuffer("commodity.model");
			form.getNoteFormer4Order().getNoteString(form.getDetailList().get(0), fname);
			for (int i=0; i<detailCount; i++, form.getNoteFormer4Order().getVoNoteMap(form.getDetailList().get(i-1)).put(fname.toString(), "2.0*8"));
			form.getDomain().setChangeRemark("改客户人，改商品");
			Date timeD = new Date();
			this.onMenu("提交更改");
			if (true) {
				OrderDetail order=form.getDetailList().get(detailCount-1), domain=form.getDomain(), sdomain=domain.getSnapShot();
				OrderTicket ticket=form.getDomain().getVoparam(OrderTicket.class), sticket=ticket.getSnapShot();
				Assert.assertTrue("明细要保存改单内容", order.getNotes()!=null);
				Assert.assertTrue("明细要保存改单备注", order.getChangeRemark()!=null);
				Assert.assertTrue("红冲开单，保存单头更改", order.getStOrder()==50 && ticket.getModifytime().after(timeD) 
						&& StringUtils.equals(sdomain.getClient().getName(), domain.getClient().getName()));
				if (hasNumber2)
					Assert.assertTrue("人工编号更改", StringUtils.equals(sticket.getNumber2(), ticket.getNumber2()));
				if (hasClient)
					Assert.assertTrue("客户名称更改", StringUtils.equals(sticket.getClientName(), ticket.getClientName())==false);
			}
		}
		if ("2取消下单".length()>0 && types.indexOf('2')>-1) {
			this.setEditAllSelect(detailCount);
			this.onMenu("取消下单");
			form.getDomain().setChangeRemark("改下单数量为0");
			this.onMenu("提交更改");
			OrderDetail order = form.getDetailList().get(detailCount-1);
			Assert.assertTrue("改数量为0红冲开单失败", order.getStOrder()==50);
		}
		if ("3红冲开单复制新增明细".length()>0 && types.indexOf('3')>-1) {
			if (true) {
				this.setEditAllSelect(detailCount);
				this.onMenu("复制新增明细");
				int i=0;
				for (OrderDetail ord: form.getSelectedList()) {
					OrderDetail detail = form.getCreateList().get(i++);
					detail.setAmount(ord.getAmount()+1);
					detail.setPrice(13.0);
				}
			}
			if (true) {
				this.onMenu("添加明细");
				OrderDetail detail = form.getCreateList().get(detailCount);
				detail.setCommodity(form.getSelectFormer4Order().getFirst().getCommodity());
				detail.setAmount(form.getSelectFormer4Order().getFirst().getAmount()+1);
				detail.setPrice(13.0);
			}
			try {
				this.onMenu("提交更改");
				Assert.fail("有新增明细应不能提交更改");
			}catch(Exception e) {
			}
			if ("单头有更改提交新增失败".length()>0) {
				StringBuffer fname = new StringBuffer("client.linkerCall");
				form.getNoteFormer4Order().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Order().getVoNoteMap(form.getDomain()).put(fname.toString(), "0512-61111111-1");
				try {
					this.onMenu("提交新增");
					Assert.fail("单头有更改提交新增失败");
				}catch(Exception e) {
					form.getNoteFormer4Order().getVoNoteMap(form.getDomain()).remove(fname.toString());
				}
			}
			List<OrderDetail> createList = new ArrayList<OrderDetail>(form.getCreateList());
			this.onMenu("提交新增");
			this.getEditListView("createList");
			Assert.assertTrue("提交新增失败", this.getListViewValue().size()==0);
			this.loadView("DoadjustList", "number", number);
			Assert.assertTrue("新增1*+1明细应可红冲", this.getListViewValue().size()==detailCount*2+1);
			StringBuffer sourceMonth = new StringBuffer();
			for (OrderDetail d: form.getDetailList()) {
				sourceMonth.append(d.getMonthnum()).append(",");
			}
			for (int i=createList.size(); i-->0; Assert.assertTrue("新增明细要有新月流水号", createList.get(i).getMonthnum()!=null && sourceMonth.indexOf(createList.get(i).getMonthnum())==-1));
		}
	}
	
	public void check已排单红冲__1追加订单(char type, String number) {
		OrderTicketForm form = this.getForm();
		if ("4追加订单商品".length()>0 && type=='1') {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("追加订单商品");
			int i=0;
			for (OrderDetail ord: form.getSelectFormer4Order().getSelectedList()) {
				OrderDetail detail = form.getCreateList().get(i++);
				detail.setAmount(ord.getAmount()+1);
				detail.setPrice(14.0);
			}
			new StoreTicketTest().setQ清空();
			List<OrderDetail> createList = new ArrayList<OrderDetail>(form.getCreateList());
			this.onMenu("提交新增");
			this.getEditListView("createList");
			Assert.assertTrue("提交新增失败", this.getListViewValue().size()==0);
			StringBuffer sourceMonth = new StringBuffer();
			for (OrderDetail d: form.getDetailList()) {
				sourceMonth.append(d.getMonthnum()).append(",");
			}
			for (OrderDetail citem: createList)
				Assert.assertTrue("新增明细要有新月流水号", citem.getMonthnum()!=null && sourceMonth.indexOf(citem.getMonthnum())==-1);
			this.loadView("DoadjustList", "number", number);
			Assert.assertTrue("新增1*明细应可红冲", this.getListViewValue().size()==detailCount*2);
			this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
			Assert.assertTrue("应重新计算库存够用数", this.getListViewValue().size()>0);
		}
	}
	
	public void check红冲驳回确认__1再次红冲_2删除红冲(String number, char type) {
		OrderTicketForm form = this.getForm();
		if ("1改红冲内容，再次提交发起红冲".length()>0 && type=='1') {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("红冲驳回确认");
			form.getDomain().setChangeRemark(form.getDomain().getChangeRemark() + "，再次提交红冲");
			this.setEditAllSelect(detailCount);
			this.onMenu("提交更改");
		}
		if ("2删除驳回的红冲".length()>0 && type=='2') {
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("红冲驳回确认");
			this.setEditAllSelect(detailCount);
			this.onMenu("删除红冲申请");
		}
	}
	
	public void check开订单__1备货低于安全库存(char type, String number) {
		OrderTicketForm form = this.getForm();
		if ("1新增备货采购".length()>0 && type=='1' && this.getModeList().contain(TestMode.SubCompany)==false) {
			this.loadView("Create4Back", "selectedList");
			form.getDomain().getOrderTicket().setNumber(number);
			Assert.assertTrue("备货订单不应显示出客户输入框", this.hasField("client.name")==false);
			this.getSqlListView("selectedList");
			this.setSqlAllSelect(this.getListViewValue().size());
			this.onMenu("添加入备货单明细");
			new StoreTicketTest().setQ清空();
			this.onMenu("提交");
			OrderDetail detail = form.getDetailList().get(form.getDetailList().size()-1);
			if (true) {
				Assert.assertTrue("新增备货订单，单头保存", detail.getId()>0 && form.getDomain().getVoparam(OrderTicket.class).getId()>0);
				Assert.assertEquals("备货订单明细没有number", number, detail.getOrderTicket().getNumber());
			}
			if ("低于安全库存备货".length()>0 && detail.getVoparam(Commodity.class)!=null) {
				Assert.assertEquals("用够用数的商品", detail.getVoparam(Commodity.class).getVoparam(StoreEnough.class).getTCommodity().getTrunk(), detail.getTCommodity().getTrunk());
			}
			if ("查统计".length()>0) {
				this.loadFormView(new StoreTicketForm(), "StoreEnoughQuery");
				Assert.assertTrue("订单生成没有生成够用数", this.getListViewValue().size()>0);
			}
			if ("查询查看".length()>0) {
				this.loadSql("ShowQuery", "DetailForm.selectedList", "number", number);
				this.setSqlListSelect(1);
				this.onMenu("查看");
				Assert.assertTrue("先1订单明细查看应出订单全部的2明细", form.getDetailList().size()>0);
				Assert.assertTrue("备货订单不应显示出客户输入框", this.hasField("client.name")==false);
			}
			if ("查不在排单".length()>0) {
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "ArrangeList",
						"number", number);
				Assert.assertTrue("备货订单不应到订单排单", this.getListViewValue().size()==0);
			}
			if ("查在采购开单".length()>0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CommonList",
						"number", number);
				Assert.assertTrue("备货订单没到采购开单", this.getListViewValue().size()>0);
			}
			if ("查红冲申请可按钮".length()>0) {
				this.loadView("DoadjustList", "number", number);
				this.setSqlListSelect(1);
				try {
					this.onMenu("已排单红冲");
				}catch(Exception e) {
					Assert.fail("备货订单为已排单请购，应可红冲");
				}
				this.loadView("DoadjustList", "number", number);
				this.setSqlListSelect(1);
				try {
					this.onMenu("追加订单商品");
				}catch(Exception e) {
					Assert.fail("备货订单为已排单应能追加商品");
				}
				this.loadView("DoadjustList", "number", number);
				this.setSqlListSelect(1);
				try {
					this.onMenu("未排单编辑");
					Assert.fail("备货订单为已排单请购，应不可编辑");
				}catch(Exception e) {
				}
			}
		}
	}
	
	public String check开订单__1新增客户_2新增备货_3新增客户导入(char type, int... amountList) {
		OrderTicketForm form = this.getForm();
		String number = null;
		if ("1新增客户".length()>0 && type=='1') {
			this.loadView("Create");
			number = new StringBuffer("客户").append(this.getModeList().getModeLabel()).append(Calendar.getInstance().getTimeInMillis()).toString();
			form.getDomain().getOrderTicket().setNumber(number);
			Assert.assertTrue("要显示出客户输入框", this.hasField("client.name"));
			this.setFieldText("client.name", new ClientTest().get幸亚().getName());
			form.getDomain().getOrderTicket().setNumber(number);
			this.genOrderTicket();
			this.genOrderDetail(amountList);
			if (true) {
				this.onMenu("添加明细");
				this.setEditListSelect(form.getDetailList().size());
				this.onMenu("移除明细");
				Assert.assertTrue("客户订单移除明细不成功", form.getDetailList().size()==amountList.length);
			}
			new StoreTicketTest().setQ清空();
			this.onMenu("提交");
			OrderDetail detail = form.getDetailList().get(form.getDetailList().size()-1);
			if (detail.getSubCompany().getFromSellerId()>0) {
				Assert.assertTrue("给分公司查看", detail.getClient().getFromSellerId()>0 && StringUtils.isBlank(detail.getClient().getUaccept())==false);
			}
			if (true) {
				Assert.assertTrue("新增客户订单，单头保存", detail.getId()>0 && form.getDomain().getVoparam(OrderTicket.class).getId()>0);
				Assert.assertTrue("客户订单明细没有number", StringUtils.isBlank(detail.getOrderTicket().getNumber())==false);
				Assert.assertEquals("客户订单明细没有Client", detail.getClient().getNumber(), new ClientTest().get幸亚().getNumber());
				if (this.getModeList().contain(TestMode.SubCompany))
					Assert.assertEquals("客户订单明细没有SubCompany", detail.getSubCompany().getNumber(), new SubCompanyTest().get湖南().getNumber());
			}
			if ("查统计".length()>0) {
				this.loadFormView(new StoreTicketForm(), "StoreEnoughQuery");
				Assert.assertTrue("订单生成没有生成够用数", this.getListViewValue().size()>0);
			}
			if ("查询查看".length()>0) {
				this.loadSql("ShowQuery", "TicketForm.selectedList", "number", number, "OrderT", "notnull", "Arrange", "notnull");
				Assert.assertTrue("订单合计无安排合计", this.getListViewValue().size()>0);
				this.loadSql("ShowQuery", "DetailForm.selectedList", "number", number);
				this.setSqlListSelect(1);
				this.onMenu("查看");
				Assert.assertTrue("没有显示出客户输入框", this.hasField("client.name"));
				Assert.assertTrue("先1订单明细查看应出订单明细", form.getDetailList().size()>0);
			}
			if ("查在排单".length()>0) {
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "ArrangeList",
						"number", number).toString();
				Assert.assertTrue("订单没有到排单", this.getListViewValue().size()>0);
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CommonList",
						"number", number);
				Assert.assertTrue("订单没有请购不应到采购开单", this.getListViewValue().size()==0);
			}
		}
		if ("2新增备货".length()>0 && type=='2') {
			this.loadView("Create4Back");
			number = new StringBuffer("备货").append(this.getModeList().getModeLabel()).append(Calendar.getInstance().getTimeInMillis()).toString();
			form.getDomain().getOrderTicket().setNumber(number);
			Assert.assertTrue("备货订单不应显示出客户输入框", this.hasField("client.name")==false);
			if (this.getModeList().contain(TestMode.SubCompany))
				this.setFieldText("subCompany.name", this.getSubCompany().getName());
			this.genOrderTicket();
			this.genOrderDetail(amountList);
			if (true) {
				this.onMenu("添加明细");
				this.setEditListSelect(form.getDetailList().size());
				this.onMenu("移除明细");
				Assert.assertTrue("备货订单移除明细不成功", form.getDetailList().size()>0);
			}
			new StoreTicketTest().setQ清空();
			this.onMenu("提交");
			OrderDetail order = form.getDetailList().get(form.getDetailList().size()-1);
			if (order.getSubCompany().getFromSellerId()>0) {
				Assert.assertTrue("给分公司查看", order.getClient().getFromSellerId()>0 && StringUtils.isBlank(order.getClient().getUaccept())==false);
			}
			if (true) {
				Assert.assertTrue("新增备货订单，有普通安排", order.getId()>0 && form.getDomain().getVoparam(OrderTicket.class).getId()>0 && order.getArrangeTicket().getNumber()!=null && new DeliverTypeLogic().isCommonType(order.getArrangeTicket().getArrangeType()));
				Assert.assertEquals("备货订单明细没有number", number, order.getOrderTicket().getNumber());
				if (this.getModeList().contain(TestMode.SubCompany))
					Assert.assertEquals("客户订单明细没有SubCompany", order.getSubCompany().getNumber(), new SubCompanyTest().get湖南().getNumber());
			}
			if ("查统计".length()>0) {
				this.loadFormView(new StoreTicketForm(), "StoreEnoughQuery");
				Assert.assertTrue("订单生成没有生成够用数", this.getListViewValue().size()>0);
			}
			if ("查询查看".length()>0) {
				this.loadSql("ShowQuery", "TicketForm.selectedList", "number", number, "OrderT", "notnull", "Arrange", "notnull");
				Assert.assertTrue("订单合计安排合计", this.getListViewValue().size()>0);
				this.loadSql("ShowQuery", "DetailForm.selectedList", "number", number);
				this.setSqlListSelect(1);
				this.onMenu("查看");
				Assert.assertTrue("先1订单明细查看应出订单全部的明细", form.getDetailList().size()>0);
				Assert.assertTrue("备货订单不应显示出客户输入框", this.hasField("client.name")==false);
			}
			if ("查不在排单".length()>0) {
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "ArrangeList",
						"number", number);
				Assert.assertTrue("备货订单不应到生产排单", this.getListViewValue().size()==0);
			}
			if ("查在生产设置BOM".length()>0) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "CommonList",
						"number", number);
				Assert.assertTrue("备货订单没到生产开单", this.getListViewValue().size()>0);
			}
		}
		if ("3新增客户订单，明细已导入".length()>0 && type=='3') {
			this.loadView("Create");
			number = new StringBuffer("客户").append(this.getModeList().getModeLabel()).append(Calendar.getInstance().getTimeInMillis()).toString();
			form.getDomain().getOrderTicket().setNumber(number);
			Assert.assertTrue("要显示出客户输入框", this.hasField("client.name"));
			this.setFieldText("client.name", new ClientTest().get幸亚().getName());
			if (this.getModeList().contain(TestMode.SubCompany))
				this.setFieldText("subCompany.name", this.getSubCompany().getName());
			this.genOrderTicket();
			form.getDomain().getOrderTicket().setNumber(number);
			form.getDetailList().addAll(form.getImportList());
			this.onMenu("提交");
			Assert.assertTrue("订单单头保存", form.getDomain().getVoparam(OrderTicket.class).getId()>0);
		}
		return number;
	}
	
	private List<OrderDetail> check导入订单(String sDomains) {
		this.getForm().prepareImport();
		this.loadView("Import");
		if ("列标题序号".length()>0) {
			LinkedHashMap<String, String> mapFieldIndex = new LinkedHashMap<String, String>();
			int coli=0;
			mapFieldIndex.put("commodity.supplyType", ++coli+"");
			mapFieldIndex.put("commodity.commNumber", ++coli+"");
			mapFieldIndex.put("commodity.name", ++coli+"");
			mapFieldIndex.put("commodity.factory", ++coli+"");
			mapFieldIndex.put("commodity.model", ++coli+"");
			mapFieldIndex.put("commodity.labelModel", ++coli+"");
			mapFieldIndex.put("commodity.antiNum", ++coli+"");
			mapFieldIndex.put("commodity.unit", ++coli+"");
			mapFieldIndex.put("amount", ++coli+"");
			mapFieldIndex.put("OrderTicket.cprice", ++coli+"");
			BaseImportForm imform = this.getForm().getFormProperty("attrMap.BaseImportForm");
			SellerViewInputs inputs = imform.getFormProperty("attrMap.SellerViewInputs");
			inputs.getInputs().clear();
			inputs.getInputs().putAll(mapFieldIndex);
			imform.getSellerIndexes(this.getEditListView().getComponent());
			this.getForm().setFormProperty("attrMap.FieldIndex", mapFieldIndex);
		}
		this.getForm().getDomain().getVoParamMap().put("Remark", sDomains);
		this.onMenu("导入格式化");
		if ("有保存列序号".length()>0) {
			this.loadView("Import");
			SellerViewInputs inputs = this.getForm().getFormProperty("attrMap.BaseImportForm.attrMap.SellerViewInputs");
			LinkedHashMap<String, String> mapFieldIndex = (LinkedHashMap<String, String>)this.getForm().getFormProperty("attrMap.FieldIndex");
			if ("去除未选用的".length()>0) {
				HashSet<String> hideList = new HashSet<String>(mapFieldIndex.keySet());
				hideList.removeAll(inputs.getInputs().keySet());
				for (String key: hideList) {
					mapFieldIndex.remove(key);
				}
			}
			Assert.assertTrue("有保存列序号，能加载出来", inputs.getId()>0 && inputs.getInputs().keySet().size()>0 && inputs.getInputs().values().containsAll(mapFieldIndex.values()));
		}
		Assert.assertTrue("有导入明细", this.getForm().getImportList().size()>0);
		OrderDetail detail = this.getForm().getImportList().get(this.getForm().getImportList().size()-1);
		if (this.hasField("commodity.supplyType") && detail.getCommodity().getSupplyType()==null)
			Assert.assertTrue("有导入行错误", StringUtils.isBlank((String)detail.getVoparam("error"))==false);
		return this.getForm().getImportList();
	}
	
	public void check订单跟踪统计(String... numbers) {
		OrderTicketForm form = this.getForm();
		Assert.assertTrue("有订单号", numbers.length>0);
		this.loadView("TicketCount", "selectedList", "number", numbers);
		int detailSize = this.getListViewValue().size();
		this.setFilters("TicketForm.selectedList", "number", numbers);
		int ticketSize = this.getListViewValue().size();
		if ((ticketSize>0 && detailSize>0)==false)
			return;
		Assert.assertTrue("有订单单头、明细", ticketSize>0 && detailSize>0);
		this.onMenu("统计");
		OrderTicket ticket = form.getSelectFormer4OrderTicket().getLast();
//		Assert.assertTrue("订单合计", ticket.getOrderT().length()>0);
	}
	
	public void check未排单编辑__1型号_2数量0_3价格_4客户商家红冲(String types, String number, Object... filters0) {
		OrderTicketForm form = this.getForm();
		Object[] filters = this.genFiltersStart(filters0, "number", number);
		this.loadView("DoadjustList", filters);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.onMenu("未排单编辑");
		Assert.assertTrue("只选择的1*带入编辑", form.getDetailList().size()==detailCount);
		if (form.getDomain().getClient().getName()!=null)
			Assert.assertTrue("没有显示出客户输入框", this.hasField("client.name"));
		if ("1未排单编辑改型号".length()>0 && types.indexOf('1')>-1) {
			for (int i=0; i<detailCount; i++) {
				OrderDetail detail = form.getDetailList().get(i);
				Commodity comm = detail.getCommodity();
				comm.setModel("2.0*8");
				comm.setZuzhi("RP-12UZ");
			}
		}
		if ("2改为0删除".length()>0 && types.indexOf('2')>-1) {
			for (int i=0; i<detailCount; i++, form.getDetailList().get(i-1).setAmount(0.0));
		}
		if ("3改价格".length()>0 && types.indexOf('3')>-1) {
			for (OrderDetail detail: form.getSelectFormer4Order().getSelectedList()) {
				detail.getOrderTicket().setCprice(detail.getOrderTicket().getCprice()+0.1);
			}
		}
		OrderDetail order = form.getSelectFormer4Order().getLast();
		if ("4改客户商家".length()>0 && types.indexOf('4')>-1) {
			Assert.assertTrue("有客户红冲", this.hasField("domain.voParamMap.ClientDoadjust"));
			this.setEditAllSelect(detailCount);
			this.onButton("已客户商家红冲申请处理");
			Assert.assertTrue("无客户红冲", this.hasField("domain.voParamMap.ClientDoadjust")==false);
			Assert.assertTrue("无改单备注", form.getNoteFormer4Order().getVoNoteMap(order).size()==0);
		} else {
			Assert.assertTrue("无客户红冲", this.hasField("domain.voParamMap.ClientDoadjust")==false);
		}
		new StoreTicketTest().setQ清空();
		Date timeD = new Date();
		this.onMenu("提交");
		OrderTicket ticket = form.getDomain().getVoparam(OrderTicket.class);
		Assert.assertTrue("提交成功界面跳转", this.hasMenu("提交")==false);
		Assert.assertTrue("订单未排单编辑生效", order.getStOrder()==30 && form.getDomain().getVoparam(OrderTicket.class).getModifytime().after(timeD));
		Assert.assertTrue("订单合计保留", StringUtils.isNotEmpty(ticket.getOrderT()));
		if ("1未排单编辑改型号".length()>0 && types.indexOf('1')>-1) {
			Assert.assertTrue("订单未排单编辑失败", order.getCommodity().getModel().equals("2.0*8"));
			this.loadFormView(new StoreTicketForm(), "StoreEnoughQuery");
			Assert.assertTrue("订单未排单编辑没有更改够用数", this.getListViewValue().size()>0);
		}
		if ("2改为0删除".length()>0 && types.indexOf('2')>-1) {
			this.loadFormView(new StoreTicketForm(), "StoreEnoughQuery");
			Assert.assertTrue("订单未排单编辑为0没有删除够用数", this.getListViewValue().size()==0);
		}
	}
	
	public void check采购收货改单处理__1同意n_不同意(char type, String number, String[] orderAgrees) {
		OrderTicketForm form = this.getForm();
		this.loadView("RechangeList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if ("不可以订单改单处理".length()>0) {
			try {
				this.onMenu("订单改单处理");
				Assert.fail("收货给采购的改单申请，不可以订单改单处理");
			}catch(Exception e) {
			}
			this.onMenu("采购改单处理");
		}
		if (type=='1' && "1同意改单的一些内容项".length()>0) {
			if (true) {
				this.setCheckGroup("voParamMap.OrderAgree", orderAgrees);
				if (form.isOrderAgreeAll()==false)
					form.getPurchaseFirst().setChangeRemark(new StringBuffer().append("部分同意").append(Arrays.toString(orderAgrees)).toString());
			}
			this.onMenu("提交同意项");
			Assert.assertTrue("提交同意项失败", this.hasMenu("提交同意项")==false);
			if (form.isOrderAgreeAll()==false) {
				for (OrderDetail detail: form.getPurchaseList()) {
					OrderDetail sd = (OrderDetail)detail.getSnapShot();
					Assert.assertTrue("每个改单申请明细都要改单内容", StringUtils.equals(detail.getNotes(), sd.getNotes())==false);
					Assert.assertTrue("每个改单申请明细都要有申请原因", StringUtils.equals(detail.getChangeRemark(), sd.getChangeRemark())==false);
				}
			}
			if (true) {
				if (form.isOrderAgreeAll() && form.getPurchaseFirst().getArrangeId()==30) {
					this.loadFormView(new ReceiptTicketForm(), "AuditList", "number", number);
					Assert.assertTrue("订单处理完排单不用处理，应到收货确认", this.getListViewValue().size()==detailCount);
				} else if (form.getPurchaseFirst().getArrangeId()==43) {
					this.loadFormView(this.getModeList().getSelfArrangeForm(), "RechangeList", "number", number);
					Assert.assertTrue("订单处理完排单还要处理，应到排单的采购返单处理", this.getListViewValue().size()==detailCount);
				}
			}
		}
		if (type=='2' && "2不同意".length()>0) {
			form.getPurchaseFirst().setChangeRemark("订单不同意收货改单申请的处理");
			for (OrderDetail detail: form.getPurchaseList()) {
				OrderDetail sd = (OrderDetail)detail.getSnapShot();
				Assert.assertTrue("每个改单申请明细都要改单内容", StringUtils.equals(detail.getNotes(), sd.getNotes())==false);
				Assert.assertTrue("每个改单申请明细都要有申请原因", StringUtils.equals(detail.getChangeRemark(), sd.getChangeRemark())==false);
			}
			this.loadFormView(this.getModeList().getSelfPurchaseForm(), "RechangeList", "number", number);
			Assert.assertTrue("订单处理完排单还要处理，应到采购返单处理", this.getListViewValue().size()==detailCount);
		}
	}
	
	public void linkSupplier开红冲__1拆单准备_2红冲草稿(char type, List<OrderDetail> clientPurs) {
		if ("拆分出数量monthnums，准备用来开红冲".length()>0 && type=='1') {
			List<OrderDetail> orderList = new ArrayList<OrderDetail>();
			List<String> listMonthnums=new ArrayList<String>(), itemMonthnums=null;
			double adjustSum = 0;
			for (Iterator<OrderDetail> pIter=clientPurs.iterator(); pIter.hasNext(); listMonthnums.addAll(itemMonthnums)) {
				OrderDetail adjust = pIter.next();
				adjustSum += adjust.getAmount();
				itemMonthnums = new ArrayList<String>();
				StringBuffer monthnum = new StringBuffer("like ").append(new OrderTicketLogic().getPrtMonthnum(adjust.getMonthnum())).append("%");
				for (String notnum: listMonthnums) {
					monthnum.append(" and !=").append(notnum);
				}
				this.loadView("DoadjustList", "monthnum", monthnum.toString());
				Assert.assertTrue("供应商家此订单不可红冲", this.getListViewValue().size()>0);
				double sumAmount=(Double)this.getListFootColumn("amount").get(0);
				Assert.assertTrue("供应商家此订单没有足够未发货数开红冲", this.getListViewValue().size()>0 && sumAmount>=adjust.getAmount());
				this.setSqlAllSelect(this.getListViewValue().size());
				this.onMenu("选择订单");
				double adjustAmount = adjust.getAmount();
				for (Iterator<OrderDetail> oIter=this.getForm().getSelectFormer4Order().getSelectedList().iterator(); oIter.hasNext() && adjustAmount>0;) {
					OrderDetail order=oIter.next(), aorder=null;
					if (order.getAmount() > adjustAmount) {
						this.loadView("DoadjustList", "monthnum", order.getMonthnum());
						this.setSqlAllSelect(1);
						this.setFieldText("orderDetail.amount", adjustAmount);
						this.onButton("拆分出数量");
						OrderDetail orderSplit = this.getForm().getSelectFormer4Order().getFirst().getVoparam("NewOrder");
						Assert.assertTrue("拆分出的新订单，数量为红冲数", orderSplit.getId()>0 && StringUtils.equals(new DoubleType().format(orderSplit.getAmount()), new DoubleType().format(adjustAmount)));
						itemMonthnums.add(orderSplit.getMonthnum());
						adjustAmount = 0;
						aorder = orderSplit;
					} else if (order.getAmount() < adjustAmount) {
						itemMonthnums.add(order.getMonthnum());
						adjustAmount -= order.getAmount();
						aorder = order;
					} else {
						itemMonthnums.add(order.getMonthnum());
						adjustAmount = 0;
						aorder = order;
					}
					orderList.add(aorder);
					this.getForm().getNoteFormer4Order().getVoNoteMap(aorder).putAll((LinkedHashMap<String,String>)adjust.getVoparam("DownCommodityRechanges"));
					Assert.assertTrue("有红冲更改", this.getForm().getNoteFormer4Order().isNoted(aorder));
				}
				Assert.assertTrue("订单数够开入库申请", adjustAmount==0);
				adjust.getVoParamMap().put("WaitDoadjustMonthnums", itemMonthnums);
			}
			this.loadView("DoadjustList", "monthnum", listMonthnums.toArray(new String[0]));
			Assert.assertTrue("申请退货数够拒收数", adjustSum==(Double)this.getListFootColumn("amount").get(0) && listMonthnums.size()>0);
			clientPurs.get(0).getVoParamMap().put("DoadjustSupplierMonthnums", listMonthnums);
			clientPurs.get(0).getVoParamMap().put("DoadjustSupplierOrders", orderList);
		}
		if ("红冲草稿开单".length()>0 && type=='2') {
			List<String> listMonthnums = clientPurs.get(0).getVoparam("DoadjustSupplierMonthnums");
			List<OrderDetail> orderList = clientPurs.get(0).getVoparam("DoadjustSupplierOrders");
			Assert.assertTrue("有指定退货订单月流水号", listMonthnums!=null && listMonthnums.size()>0 && orderList!=null && orderList.size()>0);
			this.loadView("DoadjustList", "monthnum", listMonthnums.toArray(new String[0]));
			Assert.assertTrue("有待红冲记录", this.getListViewValue().size()==listMonthnums.size());
			this.getForm().getSelectFormer4Order().setSelectedList(orderList);
			this.onMenu("开红冲草稿Untrans");
			Assert.assertTrue("订单开红冲草稿状态", this.getForm().getSelectFormer4Order().getLast().getStOrder()==51);
		}
	}
	
	public void test开客户订单() {
		if ("客户订单红冲申请，不可红冲|追加商品，能编辑".length()>0) {
			this.setTestStart();
			String number = this.check开订单__1新增客户_2新增备货_3新增客户导入('1', 1,2);
			this.loadView("DoadjustList", "number", number);
			this.setSqlListSelect(1);
			try {
				this.onMenu("已排单红冲");
				Assert.fail("未排单应不能红冲");
			}catch(Exception e) {
				//
			}
			this.loadView("DoadjustList", "number", number);
			this.setSqlListSelect(1);
			try {
				this.onMenu("追加订单商品");
				Assert.fail("未排单应不能追加商品");
			}catch(Exception e) {
				//
			}
			this.loadView("DoadjustList", "number", number);
			this.setSqlListSelect(1);
			try {
				this.onMenu("未排单编辑");
			}catch(Exception e) {
				Assert.fail("客户订单为未安排，应可编辑");
			}
		}
		this.loadView("Create");
		if ("新增客户订单，导入明细界面切换".length()>0 && this.hasField("supplyType")) {
			this.setTestStart();
			OrderTicketForm form = this.getForm();
			this.onMenu("导入明细");
			Assert.assertTrue("Forward到导入界面", this.hasMenu("提交")==false);
			this.check导入订单("生产	A001	16盘位数字存储（16路）主机	国产	ST4000VX000			台	9	11	主机");
			this.onMenu("导入为明细");
			OrderDetail detail = form.getDetailList().get(form.getDetailList().size()-1);
			Assert.assertTrue("有效明细", detail.getCommodity().getSupplyType()!=null && detail.getOrderTicket().getCprice()>0 && detail.getOrderTicket().getCmoney()>0);
			Assert.assertTrue("Forward到单据界面", this.hasMenu("提交")==true);
			if ("导入行显示错误".length()>0) {
				this.check导入订单("	A001	16盘位数字存储（16路）主机	国产	ST4000VX000			台	9	11	主机");
			}
		}
		if ("导入明细商品类型".length()>0) {
			this.setTestStart();
			List<OrderDetail> importList = new ArrayList<OrderDetail>();
			if ("商品".length()>0) {
				List<String> trunkList = Arrays.asList(new String[]{"supplyType","commNumber","name"});
				List<String> chooseList = Arrays.asList(new String[0]);
				List<String> titleList = Arrays.asList(new String[]{"commNumber"});
				new CommodityTest().setSellerViewTrunk(trunkList, chooseList, titleList, null, null);
			}
			OrderDetail detail = this.check导入订单("生产	A001	16盘位数字存储（16路）主机	国产	ST4000VX000			台	9	11	主机").toArray(new OrderDetail[0])[0];
			Assert.assertTrue("生产商品", StringUtils.equals("生产", detail.getCommodity().getSupplyType()));
			importList.add(detail);
			detail = this.check导入订单("采购	A001	16盘位数字存储（16路）主机	国产	ST4000VX000			台	9	11	主机").toArray(new OrderDetail[0])[0];
			Assert.assertTrue("采购商品", StringUtils.equals("采购", detail.getCommodity().getSupplyType()));
			importList.add(detail);
			detail = this.check导入订单("甲供	A001	16盘位数字存储（16路）主机	国产	ST4000VX000			台	9	11	主机").toArray(new OrderDetail[0])[0];
			Assert.assertTrue("甲供商品", StringUtils.equals("甲供", detail.getCommodity().getSupplyType()));
			importList.add(detail);
			this.getForm().getImportList().addAll(importList);
			this.check开订单__1新增客户_2新增备货_3新增客户导入('3', 0);
			Assert.assertTrue("甲供商品明细已发货", detail.getSendId()==30);
		}
		if ("商品Trunk只有name时，订单商品明细默认为采购，去采购排单".length()>0) {
			this.setTestStart();
			try {
				if ("商品".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"name"});
					List<String> chooseList = Arrays.asList(new String[0]);
					List<String> titleList = Arrays.asList(new String[]{"name"});
					new CommodityTest().setSellerViewTrunk(trunkList, chooseList, titleList, null, null);
				}
				if ("订单单头".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"number","orderType"});
					this.setSellerViewTicket(trunkList, null, null, null, null);
				}
				new ClientTest().setSellerViewTrunk("name");
				new SubCompanyTest().setSellerViewTrunk("name");
				this.loadView("Create");
				OrderTicketForm form = this.getForm();
				OrderDetail d = form.getDomain();
				d.getOrderTicket().setNumber("订单"+new Date().getTime());
				d.getClient().setName("客户01");
				d.getSubCompany().setName("分公司01");
				form.getDetailList().add(d);
				d.getCommodity().setName("商品01");
				d.setAmount(2);
				d.getOrderTicket().setCprice(4);
				this.onMenu("提交");
				Assert.assertTrue("supplyType默认为采购", new SupplyTypeLogic().isPurchaseType(d.getCommodity().getSupplyType()));
				this.loadSqlView(new ArrangeTicketForm(), "ArrangeList", "tabArrange.selectedList", "number", d.getOrderTicket().getNumber());
				Assert.assertTrue("到采购订单安排", this.getListViewValue().size()==1);
			} finally {
				this.set行业();
			}
		}
		if ("按样例开单".length()>0) {
			this.setTestStart();
			this.loadView("Create");
			if (this.hasField("locationTicket.to.name") && this.hasField("commodity.unit")) {
				OrderDetail sample = new OrderDetail();
				sample.getOrderTicket().getLocationTicket().setTo(new UserTest().getUser安装师傅01());
				sample.getCommodity().setUnit("米米");
				String number = new OrderTicketTest().setSample(sample).get客户订单(1,2);
				OrderDetail d = this.getOrderList("number", number).get(0);
				Assert.assertTrue("带入样例值", "安装师傅01".equals(d.getOrderTicket().getLocationTicket().getTo().getName()) && "米米".equals(d.getCommodity().getUnit()));
			}
		}
		if ("新增的订单项目，可选择项目查询显示选择的生效列，带出项目信息到新订单".length()>0) {
			this.setTestStart();
			this.loadView("Create");
			if (this.hasField("proNumber") && this.getModeList().getMode(TestMode.ClientOrder, TestMode.SubcompanyOrder).length==0) {
				String number = this.get客户订单(1);
				OrderDetail detail = this.getOrderList("number", number).get(0);
				this.loadView("Create");
				this.onButton("选择x");
				this.setFilters("proNumber", detail.getOrderTicket().getProNumber());
				Assert.assertTrue("有新增的项目记录", this.getSqlListView().getListValue().size()==1);
				this.setSqlAllSelect(1);
				this.onMenu("确定");
				Assert.assertTrue("带出选择的项目值", StringUtils.equals(detail.getOrderTicket().getProNumber(), this.getForm().getDomain().getOrderTicket().getProNumber()));
			}
		}
		if ("客户订单采购收货，其中1转备货，客户订单单头查看只剩下一个客户订单明细".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(1,2);
			new ArrangeTicketTest().check调整安排2__1终止订单_2转为备料('2', new Object[]{"number", number, "amount", 1}, null);
			this.loadSql("ShowQuery", "TicketForm.selectedList", "number", number);
			this.setSqlAllSelect(1);
			this.onMenu("查看");
			Assert.assertTrue("只有一个客户订单明细", this.getForm().getSelectFormer4Order().getSelectedList().size()==1);
		}
	}
	
	public void test开备货订单() {
		if ("备货订单红冲申请，可红冲|追加商品，不能编辑".length()>0) {
			this.setTestStart();
			String number = this.check开订单__1新增客户_2新增备货_3新增客户导入('2', 1,2);
			this.loadView("DoadjustList", "number", number);
			this.setSqlListSelect(1);
			try {
				this.onMenu("已排单红冲");
			}catch(Exception e) {
				Assert.fail("备货订单为已排单请购，应可红冲");
			}
			this.loadView("DoadjustList", "number", number);
			this.setSqlListSelect(1);
			try {
				this.onMenu("追加订单商品");
			}catch(Exception e) {
				Assert.fail("备货订单为已排单应能追加商品");
			}
			this.loadView("DoadjustList", "number", number);
			this.setSqlListSelect(1);
			try {
				this.onMenu("未排单编辑");
				Assert.fail("备货订单为已排单请购，应不可编辑");
			}catch(Exception e) {
			}
		}
		if ("低于安全库存，新增备货".length()>0) {
			this.setTestStart();
			this.getModeList().getSelfReceiptTest().get备货订单_普通(11, 12);
			String number = new StringBuffer("低库存备货").append(Calendar.getInstance().getTimeInMillis()).toString();
			this.check开订单__1备货低于安全库存('1', number);
		}
		if ("备货订单采购收货，其中1被客户订单占用，备货订单单头查看只剩下一个明细".length()>0) {
			this.setTestStart();
			String bnum = this.getModeList().getSelfReceiptTest().get备货订单_普通(1,2);
			String knum = this.get客户订单(1);
			new ArrangeTicketTest().check订单安排A挪用B__1在库备用_2在库订单_3在途备货_4在途订单('1', new Object[]{"number",knum}, new Object[]{"number",bnum,"amount",1});
			this.loadSql("ShowQuery", "TicketForm.selectedList", "number", bnum);
			this.setSqlAllSelect(1);
			this.onMenu("查看");
			Assert.assertTrue("只有一个客户订单明细", this.getForm().getSelectFormer4Order().getSelectedList().size()==1);
		}
	}
	
	public void test红冲开单() {
if (1==1) {
		if ("界面验证".length()>0) {
			if ("查不可未排单编辑".length()>0) {
				this.setTestStart();
				String number = new ArrangeTicketTest().get客户订单_常规(1,2);
				this.loadView("DoadjustList", "number", number);
				int detailCount = this.getListViewValue().size();
				this.setSqlAllSelect(detailCount);
				try {
					this.onMenu("未排单编辑");
					Assert.fail("已排订单不能未排单编辑");
				}catch(Exception e) {
				}
				try {
					this.onMenu("红冲驳回确认");
					Assert.fail("已排订单未红冲，不能红冲驳回确认");
				}catch(Exception e) {
				}
			}
			if ("查已排单红冲跳出".length()>0) {
				this.setTestStart();
				String number = new ArrangeTicketTest().get客户订单_常规(1);
				this.loadView("DoadjustList", "number", number);
				int detailCount = this.getListViewValue().size();
				this.setSqlAllSelect(detailCount);
				this.onMenu("已排单红冲");
				try {
					this.onMenu("删除红冲申请");
					Assert.fail("未红冲订单，不能删除红冲申请");
				}catch(Exception e) {
				}
				this.setEditAllSelect(detailCount);
				this.onMenu("移除明细");
				Assert.assertTrue("移除唯一明细，应跳出红冲界面", this.hasMenu("提交更改")==false);
			}
		}
}
		if ("1客户订单未排单编辑，11改型号，12改数量0".length()>0) {
			this.setTestStart();
			String number = this.get客户订单(11, 11, 12, 12);
			this.check未排单编辑__1型号_2数量0_3价格_4客户商家红冲("1", number);
			this.check未排单编辑__1型号_2数量0_3价格_4客户商家红冲("2", number);
		}
		if ("2备货订单已排单编辑，21改型号".length()>0) {
			this.setTestStart();
			String number = this.get备货订单(21, 21);
			try {
				this.check未排单编辑__1型号_2数量0_3价格_4客户商家红冲("1", number);
				Assert.fail("备货时已安排请购为已排单，应不能未排单编辑");
			}catch(Exception e) {
			}
			this.check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
		}
		if ("2备货订单已排单编辑，22改数量0".length()>0) {
			this.setTestStart();
			String number = this.get备货订单(22, 22);
			this.check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("2", number);
		}
	}
	
	public void test待处理() {
		if ("返订单改单申请处理".length()>0) {
			if ("11发起改单，不通过".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfOrderTest().get客户订单(11);
				new ArrangeTicketTest().check订单安排改单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
			}
		}
		if ("订单红冲申请，不通过确认".length()>0) {
			if ("订单安排用常规库存，订单发起红冲取消下单，排单红冲处理不通过".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfOrderTest().get客户订单(11);
				new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('1', number);
				this.check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('2', number, null);
			}
		}
		if ("低于商品最小安全库存，待备货开单".length()>0) {
			if ("有库存，但不足".length()>0) {
				this.setTestStart();
				String timeDo = this.getTimeDo();
				this.getModeList().getSelfReceiptTest().get备货订单_普通(11);
				this.loadView("Create4Back", "selectedList", "modifytime", timeDo);
				Assert.assertTrue("安全库存不足", this.getListViewValue().size()>0);
			}
		}
	}
	
	private void temp() throws Exception {
		if ("2备货订单已排单编辑，21改型号".length()>0) {
			this.setTestStart();
			String number = this.get备货订单(21, 21);
			try {
				this.check未排单编辑__1型号_2数量0_3价格_4客户商家红冲("1", number);
				Assert.fail("备货时已安排请购为已排单，应不能未排单编辑");
			}catch(Exception e) {
			}
			this.check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
		}
	}
	
	private void temp1() throws Exception {
		StringBuffer nameBuffer = new StringBuffer()
//				.append("沈林 陈楠 ")
//				.append("刘俊炜 胡泊 李晓亮 杨蕾 裴贵亭 赵梦涵 王乔 刘畅 ")
				.append("王之纲 于婉琴 周诗萌 魏熙 陈平 朱天俊 曹上 陈兵 李尔云 刘彦 陈明昊 张钧诚 王宇翔 贾淕 张志明 齐溪 刘静 张武 徐巧龙 江飞 张世杰 胡艳君 孙忠怀 康开丽 王好 吴潇潇 刘君一 韩志杰 刘璇 张妍 朱一维 薛继军 舒济 聂竞竹 史航 博讓 陈亮 庞博 曹华益 赵红薇 李昂 马健熊 吕雪 汪悍贤 邵彦棚 韩静 严卫进 谷峰 任娜 孙小帅 张悦 马越 刘文武 李一歌 孙立 冯海宁 赵楠 韩青 刘岩 孟京辉 韩硕 史尔诺 文荣华 蒋希伟 尤行 齐九九 马骏 庄殿君 王嘉琪 李靖雯 钱露 王宇迪 吴继科 裴力威 丁一滕 代丽圆 英若诚 夏晓辉 张凯 黄星 李华 张洪宇 蒋昊 刘鸿飞 金山 宋晓飞 胡斯乐 马小楠 景麒駪 徐帅卿 李楠 杨春梓 徐佳 胡嫣雨 华山 张洁 李婵 尔少平 石璐 野路宁 王倦 王琦 于磊 赵小波 陈琳 杨江 李小燕 赵宇晗 乔爱宇 徐文宣")
				;
		String names[] = nameBuffer.toString().split("\\s+");
		names = new HashSet<String>(Arrays.asList(names)).toArray(new String[0]);
		Calendar c1=Calendar.getInstance(), c2=Calendar.getInstance();
		if ("日期段".length()>0) {
			Date cur=new DateType().parse( new SimpleDateFormat("yyyy-MM-01").format(new Date()) );
			c1.setTime(cur);
			c1.add(Calendar.MONTH, -1);
			c2.setTime(cur);
		}
		StringBuffer sb = new StringBuffer();
		BlockGrid grid = new BlockGrid().createGrid(7, BlockGridMode.Independent);
		for (Calendar cur=c1; cur.before(c2); cur.add(Calendar.DAY_OF_MONTH, 1)) {
			int persons=names.length;
			if ("人员，日期".length()>0) {
				BlockGrid g = new BlockGrid().createGrid(2);
				grid.append(g);
				for (int i=0; i<persons; i++) {
					g.append(new Text(names[i]));
					g.append(new Text(new DateType().format(cur.getTime())));
				}
			}
			if ("上午9点".length()>0) {
				Date time = new TimeType().parse("8:40");
				SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
				Calendar c = Calendar.getInstance();
				BlockGrid g = new BlockGrid().createGrid(1);
				grid.append(g);
				for (int i=persons; i-->0;) {
					c.setTime(time);
					int minute=22*60, second=ThreadLocalRandom.current().nextInt(minute);
					c.add(Calendar.SECOND, second);
					g.append(new Text(fmt.format(c.getTime())));
				}
			}
			if ("上午12点".length()>0) {
				Date time = new TimeType().parse("11:59");
				SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
				Calendar c = Calendar.getInstance();
				BlockGrid g = new BlockGrid().createGrid(1);
				grid.append(g);
				for (int i=persons; i-->0;) {
					c.setTime(time);
					int minute=62*60, second=ThreadLocalRandom.current().nextInt(minute);;
					c.add(Calendar.SECOND, second);
					g.append(new Text(fmt.format(c.getTime())));
				}
			}
			if ("下午13点".length()>0) {
				Date time = new TimeType().parse("12:39");
				SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
				Calendar c = Calendar.getInstance();
				BlockGrid g = new BlockGrid().createGrid(1);
				grid.append(g);
				for (int i=persons; i-->0;) {
					c.setTime(time);
					int minute=22*60, second=ThreadLocalRandom.current().nextInt(minute);;
					c.add(Calendar.SECOND, second);
					g.append(new Text(fmt.format(c.getTime())));
				}
			}
			if ("下午18点".length()>0) {
				Date time = new TimeType().parse("17:59");
				SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
				Calendar c = Calendar.getInstance();
				BlockGrid g = new BlockGrid().createGrid(1);
				grid.append(g);
				for (int i=persons; i-->0;) {
					c.setTime(time);
					int minute=32*60, second=ThreadLocalRandom.current().nextInt(minute);;
					c.add(Calendar.SECOND, second);
					g.append(new Text(fmt.format(c.getTime())));
				}
			}
			int ren[] = new int[persons];
			if ("随机会加班的人".length()>0) {
				for (int i=persons; i-->0;) {
					int num = ThreadLocalRandom.current().nextInt(100+persons+i) % (60);
					ren[i] = (num==9? 1: 0);
				}
			}
			if ("晚上20点".length()>0) {
				Date time = new TimeType().parse("19:39");
				SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
				Calendar c = Calendar.getInstance();
				BlockGrid g = new BlockGrid().createGrid(1);
				grid.append(g);
				for (int i=persons; i-->0;) {
					if (ren[i]==1) {
						c.setTime(time);
						int minute=22*60, second=ThreadLocalRandom.current().nextInt(minute);;
						c.add(Calendar.SECOND, second);
						g.append(new Text(fmt.format(c.getTime())));
					} else {
						g.append(new Text(""));
					}
				}
			}
			if ("晚上22点".length()>0) {
				Date time = new TimeType().parse("21:59");
				SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
				Calendar c = Calendar.getInstance();
				BlockGrid g = new BlockGrid().createGrid(1);
				grid.append(g);
				for (int i=persons; i-->0;) {
					if (ren[i]==1) {
						c.setTime(time);
						int minute=32*60, second=ThreadLocalRandom.current().nextInt(minute);;
						c.add(Calendar.SECOND, second);
						g.append(new Text(fmt.format(c.getTime())));
					} else {
						g.append(new Text(""));
					}
				}
			}
		}
		if ("人、日期时间记录，打竖".length()>0) {
			for (int irow=0,rsize=grid.getRowSize(),csize=grid.getColSize(); irow<rsize; irow++) {
				BlockRow row = grid.getRow(irow);
				BlockGrid g0 = (BlockGrid)row.getCells().get(0).getComponent();
				for (int icol=1; icol<csize; icol++) {
					BlockGrid g1 = (BlockGrid)row.getCells().get(icol).getComponent();
					for (int ri=0,rcount=g0.getRowSize(); ri<rcount; ri++) {
						List<BlockCell> cells = new ArrayList<BlockCell>(g0.getRow(ri).getCells());
						cells.addAll(g1.getRow(ri).getCells());
						Text tname=(Text)cells.get(0).getComponent(), tdate=(Text)cells.get(1).getComponent(), text=(Text)cells.get(2).getComponent();
						if (text.getText().length()==0)
							continue;
						sb.append("\n").append(tname.getText()).append("\t").append(tdate.getText()).append(" ").append(text.getText());
					}
				}
			}
		}
		if ("人、日期、时间记录，打横".length()<0) {
			BlockgridList blockgridList = grid.renderTrs();
			List<BlockgridTr> rows = blockgridList.getCurTrs();
			for (Iterator rowsIter = rows.iterator(); rowsIter.hasNext();) {
				BlockgridTr row = (BlockgridTr)rowsIter.next();
				sb.append("\n");
				for (Iterator<BlockCell> cellsIter = row.getCellIterator(); cellsIter.hasNext();) {
					BlockCell cell = cellsIter.next();
					Text text = (Text)cell.getComponent();
					sb.append(text.getText());
					if (cellsIter.hasNext())
						sb.append("\t");
				}
			}
		}
		LogUtil.error(sb.append("\n").toString());
	}
	
	public void set行业() {
		for (Seller aseller: new Seller[]{new Seller4lLogic().get吉高电子(), new Seller4lLogic().get南宁古城()}) {
			this.setTransSeller(aseller);
			if ("安防工程".length()>0 && this.getModeList().getTradeMode()==TradeMode.AnFangGongChon) {
				if ("订单单头".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"number","number2","orderType","orderDate","hopeDate","createUser","proNumber","proName","proType","province","city","area","saleMan","designMan","proOptions","siteAmount","regionName","regionMan","billUnit","locationTicket.out.name","locationTicket.to.name"});
					this.setSellerViewTicket(trunkList, null, null, null, null);
				}
				if ("订单明细".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"rowi","doption","spnote"});
					this.setSellerViewDetail(trunkList, null, null);
				}
				if ("商品".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"commNumber","name","model","antinum","barNum","labelModel","factory","unit","supplyType"});
					List<String> chooseList = Arrays.asList(new String[0]);
					List<String> titleList = Arrays.asList(new String[]{"commNumber"});
					new CommodityTest().setSellerViewTrunk(trunkList, chooseList, titleList, null, null);
				}
				if ("物料".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"commNumber","name",});
					List<String> titleList = Arrays.asList(new String[]{"commNumber"});
					new CommodityTest().setSellerMaterialTrunk(trunkList, null, titleList, null, null);
				}
				new SupplierTest().setSellerViewTrunk("number","name");
				new SubCompanyTest().setSellerViewTrunk("number","name");
			} else if ("常规进销存".length()>0 && this.getModeList().getTradeMode()==TradeMode.Common) {
				if ("订单单头".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"number","number2","orderType","orderDate","saleMan","supportMan"});
					this.setSellerViewTicket(trunkList, null, null, null, null);
				}
				if ("订单明细".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"otherMoney","paidedMoney"});
					this.setSellerViewDetail(trunkList, null, null);
				}
				if ("商品".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"commNumber","name","model","spec","supplyType"});
					List<String> chooseList = Arrays.asList(new String[]{"price"});
					List<String> titleList = Arrays.asList(new String[]{"commNumber"});
					new CommodityTest().setSellerViewTrunk(trunkList, chooseList, titleList, null, null);
				}
				if ("物料".length()>0) {
					List<String> trunkList = Arrays.asList(new String[]{"commNumber","name",});
					List<String> titleList = Arrays.asList(new String[]{"commNumber"});
					new CommodityTest().setSellerMaterialTrunk(trunkList, null, titleList, null, null);
				}
				new SupplierTest().setSellerViewTrunk("number","name","linker","linkerCall");
				new SubCompanyTest().setSellerViewTrunk("number","name","linker","linkerCall");
			}
		}
	}
	
	public void setSellerViewTicket(List<String> trunkList, List<String> requireList, LinkedHashMap<String, String> renameList, LinkedHashMap<String, String> inputList, LinkedHashMap<String, List<String>> optionsList) {
		SellerViewSetting vs = new OrderTicketLogic().getTicketChoosableLogic().getChooseSetting(new OrderTicketLogic().getTicketChoosableLogic().getTicketBuilder());
		vs.setTrunkList(trunkList);
		vs.setRequireList(requireList);
		vs.setRenameMap(renameList);
		vs.setInputMap(inputList);
		vs.setSelectMap(optionsList);
		new SellerViewSettingLogic().saveViewSetting(vs);
	}
	public void setSellerViewDetail(List<String> trunkList, LinkedHashMap<String, String> renameList, LinkedHashMap<String, String> inputList) {
		SellerViewSetting vs = new OrderTicketLogic().getTicketChoosableLogic().getChooseSetting(new OrderTicketLogic().getTicketChoosableLogic().getDetailBuilder());
		vs.setTrunkList(trunkList);
		vs.setRenameMap(renameList);
		vs.setInputMap(inputList);
		new SellerViewSettingLogic().saveViewSetting(vs);
	}
	
	public String get客户订单(int... amountList) {
		String number = this.check开订单__1新增客户_2新增备货_3新增客户导入('1', amountList);
		return number;
	}
	
	public String get备货订单(int... amountList) {
		TestMode[] linkMode = this.getModeList().removeMode(TestMode.ClientOrder, TestMode.SubcompanyOrder);
		String number = this.check开订单__1新增客户_2新增备货_3新增客户导入('2', amountList);
		this.getModeList().addMode(linkMode);
		return number;
	}
	
	protected SubCompany getSubCompany() {
		if (this.getModeList().contain(TestMode.LinkAsSubcompany))
			return new SubCompanyTest().get吉高分公司();
		return new SubCompanyTest().get湖南();
	}
	
	protected OrderDetail getSample() {
		String k = "attrMap.Sample";
		OrderDetail sample = this.getForm().getFormProperty(k);
		if (sample==null) {
			sample = new OrderDetail();
			this.getForm().setFormProperty(k, sample);
		}
		return sample;
	}
	public OrderTicketTest setSample(OrderDetail sample) {
		TicketPropertyUtil.copyFieldsSkip(sample, this.getSample());
		this.getSample().setId(1);
		return this;
	}
	private void genOrderTicket() {
		OrderDetail sample = this.getSample();
		OrderTicketForm form = this.getForm();
		if (this.hasField("subCompany.name"))
			this.setFieldText("subCompany.name", this.getSubCompany().getName());
		if ("单头".length()>0) {
			PropertyChoosableLogic.TicketDetail logic = new OrderTicketLogic().getTicketChoosableLogic();
			ViewBuilder dbuilder = logic.getTicketBuilder().createClone();
			logic.trunkViewBuilder(dbuilder);
			for (Iterator<FieldBuilder> fiter=dbuilder.getFieldBuilderLeafs().iterator(); fiter.hasNext();) {
				FieldBuilder fb = (FieldBuilder)fiter.next();
				fb = TicketPropertyUtil.copyFieldsSkip(fb, new TextFieldBuilder());
				Object v0 = fb.getProperty().getDynaProperty().get(sample.getOrderTicket());
				String s0 = ((TextField)fb.build(v0).getComponent()).getText();
				if (StringUtils.isEmpty(s0)==false)
				fb.getProperty().getDynaProperty().set(form.getDomain().getOrderTicket(), v0);
			}
		}
	}
	private void genOrderDetail(int... amountList) {
		OrderDetail sample = this.getSample();
		OrderTicketForm form = this.getForm();
		Assert.assertTrue("要指定生成订单模式", this.getModeList().getModeList().length>0);
		if (this.getModeList().contain(TestMode.Purchase)) {
			if (true) {
				this.onMenu("添加明细");
				OrderDetail detail = form.getDetailList().get(0);
				detail.setCommodity(new CommodityTest().getC白棒());
				detail.setAmount(amountList[0]);
				detail.getOrderTicket().setCprice(10+amountList[0]);
			}
			for (int i=1; i<amountList.length; i++) {
				this.onMenu("添加明细");
				OrderDetail detail = form.getDetailList().get(i);
				detail.setCommodity(new CommodityTest().getC浆料());
				detail.getCommodity().setName(detail.getCommodity().getName() + (i==1? "": i));
				detail.setAmount(amountList[i]);
				detail.getOrderTicket().setCprice(10+amountList[i]);
			}
		} else if (this.getModeList().contain(TestMode.Product)) {
			if (true) {
				this.onMenu("添加明细");
				OrderDetail detail = form.getDetailList().get(0);
				detail.setCommodity(new CommodityTest().getS电磁棒1加工1生产3常规());
				detail.getCommodity().setName(detail.getCommodity().getName() + 1);
				detail.setAmount(amountList[0]);
				detail.getOrderTicket().setCprice(10 + amountList[0]);
			}
			for (int i=1; i<amountList.length; i++) {
				this.onMenu("添加明细");
				OrderDetail detail = form.getDetailList().get(i);
				detail.setCommodity(new CommodityTest().getS黑棒());
				detail.getCommodity().setName(detail.getCommodity().getName() + i);
				detail.setAmount(amountList[i]);
				detail.getOrderTicket().setCprice(10 + amountList[i]);
			}
		}
		for (int rowi=0,size=form.getDetailList().size(); rowi<size; rowi++) {
			OrderDetail detail = form.getDetailList().get(rowi);
			detail.getOrderTicket().setRowi(rowi);
			detail.getOrderTicket().setSpnote("<textarea rows=3></textarea>\n<textarea rows=3></textarea>");
			detail.getOrderTicket().setDoption(null);
		}
		if ("明细".length()>0) {
			PropertyChoosableLogic.TicketDetail logic = new OrderTicketLogic().getTicketChoosableLogic();
			ViewBuilder dbuilder = logic.getDetailBuilder().createClone();
			logic.trunkViewBuilder(dbuilder);
			for (Iterator<FieldBuilder> fiter=dbuilder.getFieldBuilderLeafs().iterator(); fiter.hasNext();) {
				FieldBuilder fb = (FieldBuilder)fiter.next();
				fb = TicketPropertyUtil.copyFieldsSkip(fb, new TextFieldBuilder());
				Object v0 = fb.getProperty().getDynaProperty().get(sample.getOrderTicket());
				String v1 = ((TextField)fb.build(v0).getComponent()).getText();
				if (StringUtils.isEmpty(v1)==false)
				for (int rowi=0,size=form.getDetailList().size(); rowi<size; rowi++) {
					OrderDetail detail = form.getDetailList().get(rowi);
					fb.getProperty().getDynaProperty().set(detail.getOrderTicket(), v0);
				}
			}
		}
		if ("商品".length()>0) {
			PropertyChoosableLogic.Choose12 logic = new CommodityLogic().getPropertyChoosableLogic();
			ViewBuilder dbuilder = logic.getChooseBuilder().createClone();
			logic.trunkViewBuilder(dbuilder);
			for (Iterator<FieldBuilder> fiter=dbuilder.getFieldBuilderLeafs().iterator(); fiter.hasNext();) {
				FieldBuilder fb = (FieldBuilder)fiter.next();
				fb = TicketPropertyUtil.copyFieldsSkip(fb, new TextFieldBuilder());
				Object v0 = fb.getProperty().getDynaProperty().get(sample.getCommodity());
				String v1 = ((TextField)fb.build(v0).getComponent()).getText();
				if (StringUtils.isEmpty(v1)==false)
				for (int rowi=0,size=form.getDetailList().size(); rowi<size; rowi++) {
					OrderDetail detail = form.getDetailList().get(rowi);
					fb.getProperty().getDynaProperty().set(detail.getCommodity(), v0);
				}
			}
		}
		Assert.assertTrue("添加明细数量不足", form.getDetailList().size()==amountList.length);
	}
	
	public List<OrderTicket> getTicketList(Object... filterList) {
		OrderTicketTest test = this.getModeList().getSelfOrderTest();
		test.loadSql("ShowQuery", "TicketForm.selectedList", filterList);
		int detailCount = test.getListViewValue().size();
		Assert.assertTrue("订单查询里应有此记录", detailCount>0);
		test.setSqlAllSelect(test.getListViewValue().size());
		test.onMenu("选择OrderTicket");
		return test.getForm().getSelectFormer4OrderTicket().getSelectedList();
	}
	
	public List<OrderDetail> getOrderList(Object... filterList) {
		OrderTicketTest test = this.getModeList().getSelfOrderTest();
		test.loadSql("ShowQuery", "DetailForm.selectedList", filterList);
		int detailCount = test.getListViewValue().size();
		Assert.assertTrue("订单查询里应有此记录", detailCount>0);
		test.setSqlAllSelect(test.getListViewValue().size());
		test.onMenu("选择OrderDetail");
		return test.getForm().getSelectFormer4Order().getSelectedList();
	}
	
	public List<OrderDetail> getOrderList(Long toSellerId, Object... filterList) {
		OrderTicketTest test = this.getModeList().getSelfOrderTest();
		Seller fromSeller = SellerLogic.getSeller();
		test.setTransSeller(new Seller4lLogic().getSellerById(toSellerId));
		test.loadSql("ShowQuery", "DetailForm.selectedList", filterList);
		test.setSqlAllSelect(test.getListViewValue().size());
		test.onMenu("选择OrderDetail");
		List<OrderDetail> orderList = test.getForm().getSelectFormer4Order().getSelectedList();
		test.setTransSeller(fromSeller);
		return orderList;
	}

	protected void setQ清空() {
		String sticket = "delete from sa_OrderTicket where sellerId=?";
		String sdetail = "delete from sa_OrderDetail where sellerId=?";
		String scount = "delete from sa_OrderCount where sellerId=?";
		SSaleUtil.executeSqlUpdate(sticket, sdetail, scount);
	}
}
