package com.haoyong.sales.test.sale;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.mily.types.DoubleType;
import net.sf.mily.webObject.FieldBuilder;
import net.sf.mily.webObject.TextFieldBuilder;
import net.sf.mily.webObject.ViewBuilder;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.base.domain.SellerViewInputs;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.form.BaseImportForm;
import com.haoyong.sales.base.logic.BomTicketLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.logic.SellerLogic;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.logic.PropertyChoosableLogic;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.PurchaseT;
import com.haoyong.sales.sale.domain.PurchaseTicket;
import com.haoyong.sales.sale.form.OrderTicketForm;
import com.haoyong.sales.sale.form.PurchaseReturnForm;
import com.haoyong.sales.sale.form.PurchaseTicketForm;
import com.haoyong.sales.sale.form.ReceiptTicketForm;
import com.haoyong.sales.sale.form.SaleQueryForm;
import com.haoyong.sales.sale.logic.OrderTypeLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.CommodityTest;
import com.haoyong.sales.test.base.SupplierTest;
import com.haoyong.sales.util.SSaleUtil;

public class PurchaseTicketTest extends AbstractTest<PurchaseTicketForm> implements TRemind {
	
	public PurchaseTicketTest() {
		this.setForm(new PurchaseTicketForm());
	}
	
	private void check导入库存(String supplyType, String sDomains, String sIndexs) {
		this.getForm().prepareImport();
		this.loadView("Import");
		this.setFieldText("supplyType", supplyType);
		if (sIndexs != null) {
			ViewBuilder viewBuilder = this.getEditListView().getViewBuilder();
			LinkedHashMap<String, String> mapFieldIndex = new LinkedHashMap<String, String>(); 
			for (Iterator fiter=this.getEditListView().getViewBuilder().getFieldBuilderLeafs().iterator(), citer=Arrays.asList(sIndexs.split("\\t")).iterator(); fiter.hasNext() && citer.hasNext();) {
				FieldBuilder builder = (FieldBuilder)fiter.next();
				if (builder.getClass() != TextFieldBuilder.class)
					continue;
				String index = (String)citer.next();
				StringBuffer name = new StringBuffer();
				for (FieldBuilder cur=builder; StringUtils.equals(cur.getName(),viewBuilder.getName())==false; cur=cur.getViewBuilder()) {
					name.insert(0, cur.getName().concat("."));
				}
				if (StringUtils.isBlank(index)==false)
					mapFieldIndex.put(name.deleteCharAt(name.length()-1).toString(), index);
			}
			BaseImportForm imform = this.getForm().getFormProperty("attrMap.BaseImportForm");
			SellerViewInputs inputs = imform.getFormProperty("attrMap.SellerViewInputs");
			inputs.getInputs().clear();
			inputs.getInputs().putAll(mapFieldIndex);
			imform.getSellerIndexes(this.getEditListView().getComponent());
			this.getForm().setFormProperty("attrMap.FieldIndex", mapFieldIndex);
		}
		this.getForm().getDomain().getVoParamMap().put("Remark", sDomains);
		this.onMenu("导入格式化");
		this.onMenu("导入提交");
		if (sIndexs!=null) {
			this.loadView("Import");
			SellerViewInputs inputs = this.getForm().getFormProperty("attrMap.BaseImportForm.attrMap.SellerViewInputs");
			LinkedHashMap<String, String> mapFieldIndex = (LinkedHashMap<String, String>)this.getForm().getFormProperty("attrMap.FieldIndex");
			Assert.assertTrue("库存类型有保存", StringUtils.equals(this.getForm().getDomain().getCommodity().getSupplyType(), supplyType));
			Assert.assertTrue("有保存列序号，能加载出来", inputs.getId()>0 && inputs.getInputs().keySet().containsAll(mapFieldIndex.keySet()) && inputs.getInputs().values().containsAll(mapFieldIndex.values()));
		}
		if (true) {
			List<OrderDetail> importList = this.getForm().getImportList();
			List<OrderDetail> saveList = this.getForm().getFormProperty("attrMap.StoreTicketForm.attrMap.InstorePurchaseList");
			double famount=0, tamount=0;
			for (Iterator<OrderDetail> iter=importList.iterator(); iter.hasNext(); famount+=iter.next().getAmount());
			for (Iterator<OrderDetail> iter=saveList.iterator(); iter.hasNext(); tamount+=iter.next().getAmount());
			DoubleType type = new DoubleType();
			Assert.assertTrue("导入库存有保存", StringUtils.equals(type.format(famount), type.format(tamount)));
		}
	}
	
	public void check直发采购__1采购_2改单重新排单(char type, String number) {
		PurchaseTicketForm form = this.getForm();
		if ("1采购开单".length()>0 && type=='1') {
			this.loadView("DirectList", "number", number);
			int detailCount = this.getListViewValue().size();
			Assert.assertTrue("订单明细未到采购开单", detailCount>0);
			this.setSqlAllSelect(detailCount);
			this.onMenu("开单");
			this.onButton("生成单号");
			form.getDomain().getPurchaseTicket().setPurDate(new Date());
			this.setFieldText("supplier.name", this.getSupplier().getName());
			for (OrderDetail pur: form.getDetailList()) {
				pur.setPrice(pur.getAmount()*10);
			}
			new StoreTicketTest().setQ清空();
			this.onMenu("提交");
			Assert.assertTrue("采购开单失败", this.hasMenu("提交")==false);
			this.setFilters("number", number);
			Assert.assertTrue("采购开单失败", this.getListViewValue().size()==0);
			OrderDetail detail = form.getDetailList().get(form.getDetailList().size()-1);
			Assert.assertTrue("明细与单头一致", StringUtils.equals(detail.getSupplier().getName(), form.getDomain().getSupplier().getName())
					&& StringUtils.equals(detail.getPurchaseTicket().getNumber(), form.getDomain().getPurchaseTicket().getNumber()));
			if (true) {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("直发采购开单不应计算够用数", this.getListViewValue().size()==0);
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "monthnum", detail.getMonthnum());
				Assert.assertTrue("直发采购开单应到待收货列表", this.getListViewValue().size()==1);
				this.loadView("ShowQuery", "monthnum", detail.getMonthnum());
				Assert.assertTrue("采购开单应到采购查询", this.getListViewValue().size()==1);
			}
		}
		if ("2采购改单，重返排单".length()>0 && type=='2') {
			this.loadView("DirectList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单申请");
			this.setCheckGroup("排单");
			this.setRadioGroup("重新排单");
			form.getOrderFirst().setChangeRemark("采购要重返排单");
			this.setEditAllSelect(detailCount);
			try {
				this.onMenu("删除申请改单");
				Assert.fail("采购开改单，应不能删除改单申请");
			}catch(Exception e) {
			}
			this.onMenu("提交申请改单");
			if (true) {
				for (int i=0; i<detailCount; i++, 
					Assert.assertTrue("排单改单申请明细要有s改单内容", form.getOrderList().get(i-1).getNotes()!=null),
					Assert.assertTrue("排单改单申请ArrangeId40", form.getOrderList().get(i-1).getArrangeId()==40)
					);
				this.setFilters("number", number);
				this.setSqlAllSelect(detailCount);
				try {
					this.onMenu("改单申请");
					Assert.fail("有改单应不能重发起改单申请");
				}catch(Exception e) {
				}
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "RechangeList",
						"number", number);
				Assert.assertTrue("重新排单改单申请应到排单改单申请处理", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交(char type, String number) {
		PurchaseTicketForm form = this.getForm();
		if ("1采购开单".length()>0 && type=='1') {
			this.loadView("LocalList", "orderList", "number", number);
			int detailCount = this.getListViewValue().size();
			Assert.assertTrue("订单明细未到采购开单", detailCount>0);
			this.setSqlAllSelect(detailCount);
			this.onMenu("开单");
			this.onButton("生成单号");
			form.getDomain().getPurchaseTicket().setPurDate(new Date());
			this.setFieldText("supplier.name", this.getSupplier().getName());
			for (OrderDetail pur: form.getDetailList()) {
				pur.setPrice(pur.getAmount()*10);
			}
			Assert.assertTrue("开单没有不通过返回Note", this.hasNoteRead("supplier.name", "price")==false && this.hasNoteRead("commodity.name")==false);
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("提交");
			Assert.assertTrue("采购开单失败", this.hasMenu("提交")==false);
			this.loadView("LocalList", "orderList", "number", number);
			Assert.assertTrue("采购开单失败", this.getListViewValue().size()==0);
			OrderDetail pur=form.getDetailList().get(form.getDetailList().size()-1), spur=pur.getSnapShot();
			if ("下级开当地购单，要审核".length()>0 && spur.getClient().getFromSellerId()>0) {
				Assert.assertTrue("采购明细要有供应商、待审核", pur.getSupplier().getName()!=null && pur.getStPurchase()==20);
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("当地采购开单不应计算够用数", this.getListViewValue().size()==0);
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "monthnum", pur.getMonthnum(), "modifytime", timeDo);
				Assert.assertTrue("采购开单未审核不应到待收货列表", this.getListViewValue().size()==0);
				Assert.assertTrue("当地购采购开单待审核，未生效", pur.getStPurchase()==20 && pur.getSellerId()==spur.getSellerId() && pur.getSendId()==0);
			}
			if ("本级当地购单，立即生效".length()>0 && spur.getClient().getFromSellerId()==0) {
				Assert.assertTrue("采购明细要有供应商、有效，订单发货采购中", pur.getSupplier().getName()!=null && pur.getStPurchase()==30 && pur.getSendId()==10);
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("当地采购开单不应计算够用数", this.getListViewValue().size()==0);
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "monthnum", pur.getMonthnum(), "modifytime", timeDo);
				Assert.assertTrue("当地购审核通过应到待收货列表", this.getListViewValue().size()==1);
				this.loadView("ShowQuery", "monthnum", pur.getMonthnum(), "stPurchase", "=30", "modifytime", timeDo);
				Assert.assertTrue("采购开单应到采购查询", this.getListViewValue().size()==1);
			}
		}
		if ("2当地购审核通过".length()>0 && type=='2') {
			this.loadView("LocalList", "selectFormer4Purchase.selectedList", "number", number);
			int detailCount = this.getListViewValue().size();
			Assert.assertTrue("订单明细未到当地购审核", detailCount>0);
			this.setSqlAllSelect(detailCount);
			this.onMenu("审核");
			Assert.assertTrue("审核采购属性有Note可编辑", this.hasNoteWrite("supplier.name", "price") && this.hasNoteRead("commodity.name")==false);
			this.onMenu("清空审核备注");
			new StoreTicketTest().setQ清空();
			this.onMenu("通过");
			Assert.assertTrue("当地购审核通过失败", this.hasMenu("通过")==false);
			this.setFilters("number", number);
			this.loadView("LocalList", "selectFormer4Purchase.selectedList", "number", number);
			OrderDetail pur = form.getSelectFormer4Purchase().getLast();
			OrderDetail order = pur;
			Assert.assertTrue("采购明细要有供应商、有效、上级的采购单、无审核Note", pur.getSupplier().getName()!=null && pur.getStPurchase()==30 && pur.getSellerId()==order.getSellerId() && form.getNoteFormer4Purchase().isNoted(pur)==false);
			if (true) {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("当地采购开单不应计算够用数", this.getListViewValue().size()==0);
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "monthnum", pur.getMonthnum());
				Assert.assertTrue("当地购审核通过应到待收货列表", this.getListViewValue().size()==1);
				this.loadView("ShowQuery", "monthnum", pur.getMonthnum(), "stPurchase", "=30");
				Assert.assertTrue("采购开单应到采购查询", this.getListViewValue().size()==1);
			}
		}
		if ("3当地购审核，不通过".length()>0 && type=='3') {
			this.loadView("LocalList", "selectFormer4Purchase.selectedList", "number", number);
			int detailCount = this.getListViewValue().size();
			Assert.assertTrue("订单明细未到当地购审核", detailCount>0);
			this.setSqlAllSelect(detailCount);
			this.onMenu("审核");
			Assert.assertTrue("审核采购属性有Note可编辑", this.hasNoteWrite("supplier.name", "price") && this.hasNoteRead("commodity.name")==false);
			if ("改供应商名称、价格".length()>0) {
				this.setNoteText("supplier.name", "供应商名称不够详细");
				this.setNoteText("price", "23");
			}
			form.getDomain().setChangeRemark("改供应商名称、价格"+new Date());
			new StoreTicketTest().setQ清空();
			this.onMenu("不通过");
			Assert.assertTrue("当地购审核不通过失败", this.hasMenu("通过")==false);
			this.loadView("LocalList", "selectFormer4Purchase.selectedList", "number", number);
			Assert.assertTrue("当地购审核不通过失败", this.getListViewValue().size()==0);
			OrderDetail detail = form.getSelectFormer4Purchase().getLast();
			Assert.assertTrue("采购明细要有供应商、返回、有审核Note", detail.getSupplier().getName()!=null && detail.getStPurchase()==10 && form.getNoteFormer4Purchase().isNoted(detail));
			if (true) {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("当地采购审核不通过不应计算够用数", this.getListViewValue().size()==0);
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "monthnum", detail.getMonthnum());
				Assert.assertTrue("当地购审核不通过不应到待收货列表", this.getListViewValue().size()==0);
				this.loadView("ShowQuery", "monthnum", detail.getMonthnum(), "stPurchase", "<30");
				Assert.assertTrue("采购查询中未生效", this.getListViewValue().size()==1);
			}
		}
		if ("4当地购审核不通过返回，重新编辑提交".length()>0 && type=='4') {
			this.loadView("LocalList", "orderList", "number", number);
			int detailCount = this.getListViewValue().size();
			Assert.assertTrue("订单明细未到采购开单", detailCount>0);
			this.setSqlAllSelect(detailCount);
			this.onMenu("开单不通过确认");
			Assert.assertTrue("有不通过返回Note", this.hasNoteRead("supplier.name", "price") && this.hasNoteRead("commodity.name")==false);
			new StoreTicketTest().setQ清空();
			this.onMenu("提交");
			Assert.assertTrue("采购重开单失败", this.hasMenu("提交")==false);
			this.loadView("LocalList", "orderList", "number", number);
			Assert.assertTrue("采购重开单失败", this.getListViewValue().size()==0);
			OrderDetail pur = form.getSelectFormer4Purchase().getLast(), spur=pur.getSnapShot();
			Assert.assertTrue("采购明细要有供应商、待审核", pur.getSupplier().getName()!=null && pur.getStPurchase()==20);
			if (true) {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("当地采购开单不应计算够用数", this.getListViewValue().size()==0);
				Assert.assertTrue("采购开单待审核，未生效", pur.getStPurchase()==20 && pur.getSellerId()==spur.getSellerId() && pur.getSendId()==0 && pur.getReceiptId()==0);
			}
		}
	}
	
	public void check普通采购__1采购_2改单重新排单(char type, String number) {
		PurchaseTicketForm form = this.getForm();
		if ("1采购开单".length()>0 && type=='1') {
			this.loadView("CommonList", "number", number);
			int detailCount = this.getListViewValue().size();
			Assert.assertTrue("订单明细未到采购开单", detailCount>0);
			this.setSqlAllSelect(detailCount);
			this.onMenu("开单");
			this.onButton("生成单号");
			form.getDomain().getPurchaseTicket().setPurDate(new Date());
			this.setFieldText("supplier.name", this.getSupplier().getName());
			for (OrderDetail pur: form.getDetailList()) {
				pur.setPrice(pur.getAmount()+10);
			}
			new StoreTicketTest().setQ清空();
			this.onMenu("提交");
			Assert.assertTrue("采购开单失败", this.hasMenu("提交")==false);
			this.setFilters("number", number);
			Assert.assertTrue("采购开单失败", this.getListViewValue().size()==0);
			OrderDetail pur=form.getDetailList().get(form.getDetailList().size()-1);
			Assert.assertTrue("每一个采购明细都要有供应商", pur.getSupplier().getName()!=null);
			if (true) {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("普通采购开单应计算够用数", this.getListViewValue().size()>0);
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "monthnum", pur.getMonthnum());
				Assert.assertTrue("采购开单应到待收货列表", this.getListViewValue().size()==1);
				this.loadView("ShowQuery", "monthnum", form.getDetailList().get(0).getMonthnum());
				Assert.assertTrue("采购开单应到采购查询", this.getListViewValue().size()==1);
			}
			if ("生产原物料记使用加工商".length()>0 && this.getModeList().contain(TestMode.Product)) {
				List<BomDetail> bomSources = new PPurchaseTicketTest().getBomDetails("monthnum", pur.getMonthnum());
				List<BomDetail> bomRoots = new BomTicketLogic().getChildrenRoot(bomSources);
				for (BomDetail bom: bomRoots) {
					Assert.assertTrue("有加工商", bom.getSupplier().getName()!=null);
				}
			}
			if (this.getModeList().contain(TestMode.LinkAsClient) || this.getModeList().contain(TestMode.LinkAsSubcompany)) {
				OrderDetail up=pur.getVoparam("UpOrder");
				Assert.assertTrue("生成向上级采购的订单，作为客户或下级分公司，订单待审核，采购价转订单价", up!=null && up.getId()>0 && up.getSellerId()!=pur.getSellerId()
						&& up.getClient().getName()!=null && up.getClient().getFromSellerId()==pur.getSellerId()
						&& up.getStOrder()==20 && up.getUorder()!=null
						&& up.getOrderTicket().getCprice()==pur.getPrice() && up.getOrderTicket().getCmoney()==pur.getPurchaseTicket().getPmoney());
				Assert.assertTrue("原采购客户不变", pur.getClient().getFromSellerId()==0 && StringUtils.isBlank(pur.getClient().getUaccept()));
			}
		}
		if ("2采购改单，重返排单".length()>0 && type=='2') {
			this.loadView("CommonList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单申请");
			this.setCheckGroup("排单");
			this.setRadioGroup("重新排单");
			form.getOrderFirst().setChangeRemark("采购要重返排单");
			this.setEditAllSelect(detailCount);
			try {
				this.onMenu("删除申请改单");
				Assert.fail("采购开改单，应不能删除改单申请");
			}catch(Exception e) {
			}
			this.onMenu("提交申请改单");
			if (true) {
				for (int i=0; i<detailCount; i++, 
					Assert.assertTrue("排单改单申请明细要有改单内容", form.getOrderList().get(i-1).getNotes()!=null),
					Assert.assertTrue("排单改单申请ArrangeId40", form.getOrderList().get(i-1).getArrangeId()==40)
					);
				this.setFilters("number", number);
				this.setSqlAllSelect(detailCount);
				try {
					this.onMenu("改单申请");
					Assert.fail("有改单应不能重发起改单申请");
				}catch(Exception e) {
				}
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "RechangeList",
						"number", number);
				Assert.assertTrue("重新排单改单申请应到排单改单申请处理", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check普通采购改订单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0(char type, String number) {
		PurchaseTicketForm form = this.getForm();
		if ("1在采购开单发起改单申请".length()>0 && type=='1') {
			this.loadView("CommonList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单申请");
			this.setCheckGroup("订单");
			if ("改客户人，改商品".length()>0) {
				this.setEditAllSelect(detailCount);
				try {
					this.onMenu("删除申请改单");
					Assert.fail("开申请页面不能删除改单申请");
				} catch(Exception e) {
				}
				StringBuffer fname = new StringBuffer("client.linkerCall");
				form.getNoteFormer4Order().getNoteString(form.getOrderFirst(), fname);
				form.getNoteFormer4Order().getVoNoteMap(form.getOrderFirst()).put(fname.toString(), "0512-63333333-3");
				fname = new StringBuffer("commodity.model");
				form.getNoteFormer4Order().getNoteString(form.getOrderList().get(0), fname);
				for (int i=0; i<detailCount; i++,form.getNoteFormer4Order().getVoNoteMap(form.getOrderList().get(i-1)).put(fname.toString(), "2.0*8"));
				form.getOrderFirst().setChangeRemark("改客户人，改商品");
			}
			this.setEditAllSelect(detailCount);
			this.onMenu("提交申请改单");
			for (OrderDetail detail: form.getOrderList()) {
				OrderDetail sd = (OrderDetail)detail.getSnapShot();
				Assert.assertTrue("每个改单申请明细都要改单内容", StringUtils.equals(detail.getNotes(), sd.getNotes())==false);
				Assert.assertTrue("每个改单申请明细都要有申请原因", StringUtils.equals(detail.getChangeRemark(), sd.getChangeRemark())==false);
			}
		}
		if ("2重发起改单申请，调整改单".length()>0 && type=='2') {
			this.loadView("CommonList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单不通过确认");
			form.getOrderFirst().setChangeRemark("改客户人，改商品2");
			this.setEditAllSelect(detailCount);
			this.onMenu("提交申请改单");
		}
		if ("3订单改单不通过，删除改单申请".length()>0 && type=='3') {
			this.loadView("CommonList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单不通过确认");
			this.setEditAllSelect(detailCount);
			this.onMenu("删除申请改单");
		}
		if ("4改单0数量返订单".length()>0 && type=='4') {
			this.loadView("CommonList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单申请");
			this.setCheckGroup("订单");
			if (true) {
				StringBuffer fname = new StringBuffer("amount");
				form.getNoteFormer4Order().getNoteString(form.getOrderList().get(0), fname);
				for (int i=0; i<detailCount; i++, form.getNoteFormer4Order().getVoNoteMap(form.getOrderList().get(i-1)).put(fname.toString(), "0"));
				fname = new StringBuffer("commodity.model");
				form.getNoteFormer4Order().getNoteString(form.getOrderList().get(0), fname);
				for (int i=0; i<detailCount; i++, form.getNoteFormer4Order().getVoNoteMap(form.getOrderList().get(i-1)).put(fname.toString(), "12UZ"));
				form.getOrderFirst().setChangeRemark("改数量0，改商品3");
			}
			this.setEditAllSelect(detailCount);
			this.onMenu("提交申请改单");
		}
	}
	
	public void check原物料普通采购__1采购_2同商品采购(char type, String number, Object... filters0) {
		PurchaseTicketForm form = this.getForm();
		Object[] filters = this.genFiltersStart(filters0, "number", number);
		if ("1采购开单".length()>0 && type=='1') {
			this.loadSql("CommonList", "selectFormer4Bom.selectedList", filters);
			int detailCount = this.getListViewValue().size();
			Assert.assertTrue("订单的原物料请购明细未到原物料开单", detailCount>0);
			this.setSqlAllSelect(detailCount);
			this.onMenu("开单");
			this.onButton("生成单号");
			form.getDomain().getPurchaseTicket().setPurDate(new Date());
			form.getDomain().setSupplier(new SupplierTest().get永晋());
			for (OrderDetail pur: form.getDetailList()) {
				pur.setPrice(pur.getAmount()+10);
			}
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("提交");
			OrderDetail pur=form.getDetailList().get(detailCount-1), order=pur.getVoparam(OrderDetail.class);
			BomDetail bom=pur.getVoparam(BomDetail.class);
			if ("生产原物料记使用加工商".length()>0 && this.getModeList().contain(TestMode.Product)) {
				List<BomDetail> bomSource = new PPurchaseTicketTest().getBomDetails("monthnum", pur.getMonthnum());
				List<BomDetail> bomBrother = new BomTicketLogic().getChildrenBrother(bomSource, bom);
				for (BomDetail b: bomBrother) {
					Assert.assertTrue("有加工商", b.getSupplier().getName()!=null);
				}
			}
			Assert.assertTrue("原物料普通采购，用BOM月流水号，为原物料订单", pur.getStOrder()==0 && pur.getSendId()==0 && StringUtils.equals(pur.getArrangeTicket().getArrangeType(), new DeliverTypeLogic().getCommonType())
					&& StringUtils.equals(pur.getCommodity().getName(), bom.getCommodity().getName()) && pur.getAmount()==bom.getAmount()
					&& pur.getMonthnum().startsWith(bom.getMonthnum().concat("."))
					&& new OrderTypeLogic().isBomType(pur.getOrderTicket().getOrderType())
					);
			Assert.assertTrue("Bom已采购，有价格", bom.getVoparam(PurchaseT.class).getPurName()!=null && bom.getStPurchase()==30
					&& bom.getPrice()==pur.getPrice() && bom.getPurchaseTicket().getPmoney()==pur.getPurchaseTicket().getPmoney());
			Assert.assertTrue("采购开单失败", this.hasMenu("提交")==false);
			this.setFilters("selectFormer4Bom.selectedList", "number", number);
			Assert.assertTrue("采购开单失败", this.getListViewValue().size()==0);
			Assert.assertTrue("每一个采购明细都要有供应商，用BOM月流水号，有订单信息", pur.getSupplier().getName()!=null && StringUtils.equals(pur.getMonthnum(), bom.getMonthnum())==false
					&& StringUtils.equals(pur.getOrderTicket().getNumber(), order.getOrderTicket().getNumber()));
			if (true) {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery", "modifytime", timeDo, "onroadAmount",">0", "orderAmount",">0");
				Assert.assertTrue("普通采购开单应计算够用数", this.getListViewValue().size()>0);
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "monthnum", pur.getMonthnum());
				Assert.assertTrue("采购开单应到待收货列表", this.getListViewValue().size()==1);
				this.loadView("ShowQuery", "monthnum", pur.getMonthnum());
				Assert.assertTrue("采购开单应到采购查询", this.getListViewValue().size()==1);
			}
		}
		if ("2同商品采购开单".length()>0 && type=='2') {
			this.loadSql("CommonList", "selectFormer4Bom.selectedList", filters);
			Assert.assertTrue("订单的原物料请购明细未到原物料开单", this.getListViewValue().size()>0);
			this.setSqlAllSelect(1);
			this.onMenu("同商品开单");
			this.onButton("生成单号");
			form.getDomain().getPurchaseTicket().setPurDate(new Date());
			form.getDomain().setSupplier(new SupplierTest().get永晋());
			for (OrderDetail pur: form.getDetailList())
				pur.setPrice(pur.getAmount()+10);
			new StoreTicketTest().setQ清空();
			this.onMenu("提交");
			Assert.assertTrue("采购开单失败", this.hasMenu("提交")==false);
			OrderDetail pur=form.getDetailList().get(form.getDetailList().size()-1), order=pur.getVoparam(OrderDetail.class);
			BomDetail bom=pur.getVoparam(BomDetail.class);
			Assert.assertTrue("原物料普通采购，用Bom月流水号", pur.getStOrder()==0 && pur.getSendId()==0 && StringUtils.equals(pur.getArrangeTicket().getArrangeType(), new DeliverTypeLogic().getCommonType())
					&& StringUtils.equals(pur.getCommodity().getName(), bom.getCommodity().getName()) && pur.getAmount()==bom.getAmount()
					&& pur.getMonthnum().startsWith(bom.getMonthnum().concat("."))
					);
			Assert.assertTrue("Bom已采购，有价格", bom.getVoparam(PurchaseT.class).getPurName()!=null && bom.getStPurchase()==30
					&& bom.getPrice()==pur.getPrice() && bom.getPurchaseTicket().getPmoney()==pur.getPurchaseTicket().getPmoney());
			this.setFilters("selectFormer4Bom.selectedList", "bomId", form.getSelectFormer4Bom().getFirst().getId());
			Assert.assertTrue("采购开单失败", this.getListViewValue().size()==0);
			Assert.assertTrue("每一个采购明细都要有供应商，用BOM月流水号，有订单信息", pur.getSupplier().getName()!=null && StringUtils.equals(pur.getMonthnum(), bom.getMonthnum())==false
					&& StringUtils.equals(pur.getOrderTicket().getNumber(), order.getOrderTicket().getNumber()));
			if (true) {
				int dsize=form.getDetailList().size();
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("普通采购开单应计算够用数", this.getListViewValue().size()>0);
				String purName = pur.getVoparam(PurchaseT.class).getPurName();
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "purName", purName);
				Assert.assertTrue("采购开单应到待收货列表", this.getListViewValue().size()==dsize);
				this.loadView("ShowQuery", "purName", purName);
				Assert.assertTrue("采购开单应到采购查询", this.getListViewValue().size()==dsize);
			}
		}
	}

	protected void check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单(char type, String number) {
		PurchaseTicketForm form = this.getForm();
		if ("1红冲通过".length()>0 && type=='1') {
			this.loadView("AdjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("红冲处理");
			Assert.assertTrue("应可看到排单发货方式备注", this.hasField("deliverNote"));
			this.setEditAllSelect(detailCount);
			new StoreTicketTest().setQ清空();
			OrderDetail detail = form.getOrderList().get(0);
			this.setRadioGroup("无影响");
			this.onMenu("红冲通过");
			if (detail.getAmount()==0) {
				this.loadFormView(new OrderTicketForm(), "ShowQuery","DetailForm.selectedList",
						"number", number, "amount", "0");
				Assert.assertTrue("红冲通过0数量应为0", this.getListViewValue().size()==detailCount);
				this.loadFormView(this.getModeList().getSelfOrderForm(), "DoadjustList",
						"number", number, "amount", "0");
				Assert.assertTrue("红冲通过0数量应不可再红冲", this.getListViewValue().size()==0);
			} else if (new DeliverTypeLogic().isCommonType(detail.getArrangeTicket().getArrangeType())) {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("红冲通过改单为普通后，要重计算库存够用数", this.getListViewValue().size()>0);
			} else {
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("红冲通过改单为非普通后，重计算库存够用数为空", this.getListViewValue().size()==0);
			}
			if (true) {
				this.loadView("AdjustList", "number", number);
				if (this.getListViewValue().size()>0)
					"".toCharArray();
				Assert.assertTrue("红冲通过失败", this.getListViewValue().size()==0);
			}
		}
		if ("2不通过驳回红冲".length()>0 && type=='2') {
			this.loadView("AdjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("红冲处理");
			this.setEditAllSelect(detailCount);
			form.getOrderFirst().setChangeRemark("不通过");
			this.onMenu("不通过驳回红冲");
			this.setFilters("number", number);
			Assert.assertTrue("红冲不通过失败", this.getListViewValue().size()==0);
			if (true) {
				this.loadFormView(this.getModeList().getSelfOrderForm(), "DoadjustList",
						"number", number);
				Assert.assertTrue("红冲排单不通过应返回红冲不通过处理", this.getListViewValue().size()==detailCount);
			}
		}
		if ("3转交排单处理红冲，并申请原采购取消".length()>0 && type=='3') {
			this.loadView("AdjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("红冲处理");
			this.setEditAllSelect(detailCount);
			form.getOrderFirst().setChangeRemark("转交排单处理");
			this.setRadioGroup("取消");
			this.onMenu("转交排单处理");
			this.setFilters("number", number);
			Assert.assertTrue("红冲转交排单处理失败", this.getListViewValue().size()==0);
			if (true) {
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "AdjustList",
						"number", number);
				Assert.assertTrue("转交排单处理红冲，应到排单红冲申请处理", this.getListViewValue().size()==detailCount);
			}
		}
		if ("4转交排单处理红冲，并申请原采购转备料".length()>0 && type=='4') {
			this.loadView("AdjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("红冲处理");
			this.setEditAllSelect(detailCount);
			form.getOrderFirst().setChangeRemark("转交排单处理");
			this.setRadioGroup("转备料");
			this.onMenu("转交排单处理");
			this.setFilters("number", number);
			Assert.assertTrue("红冲转交排单处理失败", this.getListViewValue().size()==0);
			if (true) {
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "AdjustList",
						"number", number);
				Assert.assertTrue("转交排单处理红冲，应到排单红冲申请处理", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	protected void check取消审核__1同意_2不同意(char type, String purName) {
		PurchaseTicketForm form = this.getForm();
		if ("1同意采购取消".length()>0 && type=='1') {
			this.loadView("CancelList", "purName", purName);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			new StoreTicketTest().setQ清空();
			this.onMenu("同意");
			if (true) {
				this.setFilters("purName", purName);
				Assert.assertTrue("采购取消审核同意失败", this.getListViewValue().size()==0);
				OrderDetail pur = form.getSelectFormer4Purchase().getLast();
				Assert.assertTrue("取消采购审核通过", pur.getStPurchase()==0 && pur.getAmount()>0 && pur.getUcancel().contains(form.getUserName()));
				// 采购取消审核同意无库存够用数
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "purName", purName);
				Assert.assertTrue("采购取消审核同意应不能收货", this.getListViewValue().size()==0);
			}
		}
		if ("2不同意取消".length()>0 && type=='2') {
			this.loadView("CancelList", "purName", purName);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("不同意");
			if (true) {
				this.setFilters("purName", purName);
				Assert.assertTrue("取消采购审核不同意失败", this.getListViewValue().size()==0);
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "purName", purName);
				Assert.assertTrue("采购取消审核不同意应能继续收货", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check退货审核__1同意_2不同意_3拆分出3不同意剩余同意(char type, String purName) {
		PurchaseTicketForm form = this.getForm();
		this.loadView("ReturnList", "purName", purName);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if ("1同意采购退货".length()>0 && type=='1') {
			this.onMenu("同意");
			if (true) {
				this.setFilters("purName", purName);
				Assert.assertTrue("采购取消审核同意失败", this.getListViewValue().size()==0);
				OrderDetail pur = form.getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购退货审核同意，待退货出库", pur.getStPurchase()==75 && pur.getTReturn().getReturnName()!=null && pur.getUreturn().contains(form.getUserName()));
				this.loadFormView(new PurchaseReturnForm(), "OutstoreList", "purName", purName);
				Assert.assertTrue("待退货出库", this.getListViewValue().size()==detailCount);
			}
		}
		if ("2不同意采购退货".length()>0 && type=='2') {
			this.onMenu("不同意");
			if (true) {
				this.setFilters("purName", purName);
				Assert.assertTrue("取消采购审核不同意失败", this.getListViewValue().size()==0);
				OrderDetail pur = form.getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购退货审核不同意", pur.getStPurchase()==72 && pur.getTReturn().getReturnName()!=null);
				this.loadFormView(new PurchaseReturnForm(), "ApplyList", "purName", purName);
				Assert.assertTrue("待退货审核不同意确认", this.getListViewValue().size()==detailCount);
			}
		}
		if ("3拆分出采购明细数量3不同意，剩余数量同意".length()>0 && type=='3') {
			form.getOrderDetail().setAmount(3);
			this.onButton("拆分出数量");
			Assert.assertTrue("清空以备下一次拆分", form.getOrderDetail().getAmount()==0);
			OrderDetail premain=form.getSelectFormer4Purchase().getLast(), pnew=(OrderDetail)premain.getVoParamMap().get("NewPurchase"), psource=premain.getSnapShot();
			Assert.assertTrue("新拆分数量原剩余数量", premain.getAmount()+pnew.getAmount()==psource.getAmount()
					&& pnew.getAmount()==3 && pnew.getStPurchase()==70 && pnew.getId()>0 && pnew.getTReturn().getReturnName()!=null);
			this.loadView("ReturnList", "purName", purName);
			Assert.assertTrue("拆分成两份采购明细", this.getListViewValue().size()==detailCount*2);
			if ("新库存3不同意退货".length()>0) {
				this.loadView("ReturnList", "purName", purName, "amount", 3);
				this.setSqlAllSelect(detailCount);
				new StoreTicketTest().setQ清空();
				this.onMenu("不同意");
				this.setFilters("purName", purName, "amount", 3);
				Assert.assertTrue("取消采购审核不同意失败", this.getListViewValue().size()==0);
				OrderDetail pur = form.getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购退货审核不同意", pur.getStPurchase()==72 && pur.getTReturn().getReturnName()!=null);
				this.loadFormView(new PurchaseReturnForm(), "ApplyList", "purName", purName);
				Assert.assertTrue("待退货审核不同意确认", this.getListViewValue().size()==detailCount);
			}
			if ("剩余数量同意".length()>0) {
				this.loadView("ReturnList", "purName", purName);
				this.setSqlAllSelect(detailCount);
				this.onMenu("同意");
				this.setFilters("purName", purName);
				Assert.assertTrue("采购取消审核同意失败", this.getListViewValue().size()==0);
				OrderDetail pur = form.getSelectFormer4Purchase().getLast();
				Assert.assertTrue("采购退货审核同意，待退货出库", pur.getStPurchase()==75 && pur.getTReturn().getReturnName()!=null && pur.getUreturn().contains(form.getUserName()));
				this.loadFormView(new PurchaseReturnForm(), "OutstoreList", "purName", purName);
				Assert.assertTrue("待退货出库", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	protected void check红冲申请1开红冲__A改供应人_B价格加2__1开红冲_2不通过确认_3删除红冲(String number, String tChange, char tAction) {
		PurchaseTicketForm form = this.getForm();
		this.loadView("DoadjustList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		if ("1开红冲".length()>0 && tAction=='1') {
			if ("不可重编辑".length()>0) {
				try {
					this.onMenu("未收货编辑");
					Assert.fail("可红冲时应不可编辑");
				}catch(Exception e) {
				}
			}
			if ("不可红冲不通过确认".length()>0) {
				try {
					this.onMenu("红冲不通过确认");
					Assert.fail("可红冲时应不可不通过确认");
				}catch(Exception e) {
				}
			}
			this.onMenu("红冲申请");
			StringBuffer changeRemark = new StringBuffer();
			if ("A改供应人".length()>0 && tChange.contains("A")) {
				StringBuffer fname = new StringBuffer("supplier.linkerCall");
				form.getNoteFormer4Purchase().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Purchase().getVoNoteMap(form.getDomain()).put(fname.toString(), "0512-63333333-3");
				changeRemark.append("改供应人，");
			}
			if ("改价格".length()>0 && tChange.contains("B")) {
				StringBuffer fname = new StringBuffer("price");
				form.getNoteFormer4Purchase().getNoteString(form.getDetailList().get(0), fname);
				OrderDetail detail = null;
				for (int i=0; i<detailCount; i++,detail=form.getDetailList().get(i-1), form.getNoteFormer4Purchase().getVoNoteMap(detail).put(fname.toString(), detail.getPrice()+2+""));
				changeRemark.append("改价格，");
			}
			form.getDomain().setChangeRemark(changeRemark.toString());
			this.setEditAllSelect(detailCount);
			this.onMenu("提交红冲");
			if (true) {
				for (OrderDetail detail: form.getDetailList()) {
					Assert.assertTrue("每个改单申请明细都要改单内容", detail.getNotes()!=null);
					Assert.assertTrue("每个改单申请明细都要有申请原因", detail.getChangeRemark()!=null);
				}
				this.loadFormView(new ReceiptTicketForm(), "AdjustList",
						"number", number);
				Assert.assertTrue("红冲要到收货红冲处理", this.getListViewValue().size()==detailCount);
			}
		}
		if ("2不通过确认".length()>0 && tAction=='2') {
			if ("不可重编辑".length()>0) {
				try {
					this.onMenu("未收货编辑");
					Assert.fail("可红冲时应不可编辑");
				}catch(Exception e) {
				}
			}
			if ("不可红冲申请".length()>0) {
				try {
					this.onMenu("红冲申请");
					Assert.fail("确认红冲时应不可红冲申请");
				}catch(Exception e) {
				}
			}
			this.onMenu("红冲不通过确认");
			StringBuffer changeRemark = new StringBuffer();
			
			if ("改供应人".length()>0) {
				StringBuffer fname = new StringBuffer("supplier.linkerCall");
				form.getNoteFormer4Purchase().getNoteString(form.getDomain(), fname);
				if (tChange.contains("A")) {
					form.getNoteFormer4Purchase().getVoNoteMap(form.getDomain()).put(fname.toString(), "0512-63333333-3");
					changeRemark.append("改供应人，");
				} else {
					form.getNoteFormer4Purchase().getVoNoteMap(form.getDomain()).put(fname.toString(), null);
				}
			}
			if ("改价格".length()>0) {
				StringBuffer fname = new StringBuffer("price");
				form.getNoteFormer4Purchase().getNoteString(form.getDetailList().get(0), fname);
				if (tChange.contains("B")) {
					OrderDetail detail = null;
					for (int i=0; i<detailCount; i++,detail=form.getDetailList().get(i-1), form.getNoteFormer4Purchase().getVoNoteMap(detail).put(fname.toString(), detail.getPrice()+2+""));
					changeRemark.append("改价格，");
				} else {
					form.getNoteFormer4Purchase().getVoNoteMap(form.getDomain()).put(fname.toString(), null);
				}
			}
			form.getDomain().setChangeRemark(changeRemark.toString());
			this.setEditAllSelect(detailCount);
			this.onMenu("提交红冲");
			if (true) {
				for (OrderDetail detail: form.getDetailList()) {
					Assert.assertTrue("每个改单申请明细都要改单内容", detail.getNotes()!=null);
					Assert.assertTrue("每个改单申请明细都要有申请原因", detail.getChangeRemark()!=null);
				}
				this.loadFormView(new ReceiptTicketForm(), "AdjustList",
						"number", number);
				Assert.assertTrue("红冲要到收货红冲处理", this.getListViewValue().size()==detailCount);
			}
		}
		if ("3删除红冲".length()>0 && tAction=='3') {
			this.onMenu("红冲不通过确认");
			this.setEditAllSelect(detailCount);
			this.onMenu("删除红冲申请");
			Assert.assertTrue("删除红冲失败", this.hasMenu("删除红冲申请")==false);
			for (OrderDetail detail: form.getDetailList()) {
				Assert.assertTrue("每个改单申请明细都无改单内容", detail.getNotes()==null);
				Assert.assertTrue("每个改单申请明细都无申请原因", detail.getChangeRemark()==null);
				Assert.assertTrue("未红冲状态", detail.getStPurchase()==30);
			}
		}
	}
	
	public void check红冲申请2编辑采购_1供应商_2价格(String types, String number) {
		PurchaseTicketForm form = this.getForm();
		this.loadView("DoadjustList", "number", number);
		int detailCount = this.getListViewValue().size();
		Assert.assertTrue("采购单未收货编辑有记录", detailCount>0);
		this.setSqlAllSelect(detailCount);
		this.onMenu("未收货编辑");
		if (types.indexOf('1')>-1) {
			this.setFieldText("supplier.name", new SupplierTest().get浙江().getName());
			Assert.assertTrue("供应商编号更改", StringUtils.equals(form.getDomain().getSupplier().getNumber(), new SupplierTest().get浙江().getNumber()));
		}
		if (types.indexOf('2')>-1)
		for (OrderDetail detail: form.getDetailList()) {
			detail.setPrice(detail.getPrice()+1);
		}
		this.onMenu("提交");
		Assert.assertTrue("采购开单失败", this.hasMenu("提交")==false);
		OrderDetail pur=form.getDetailList().get(form.getDetailList().size()-1), spur=pur.getSnapShot();
		Assert.assertEquals("重编辑采购单号应保持不奕", spur.getPurchaseTicket().getNumber(), pur.getPurchaseTicket().getNumber());
		Assert.assertTrue("每一个采购明细都要有供应商", StringUtils.equals(spur.getSupplier().getName(), pur.getSupplier().getName())==false);
		if (true) {
			this.loadFormView(new ReceiptTicketForm(), "WaitList", "purName", pur.getVoparam(PurchaseT.class).getPurName());
			Assert.assertTrue("采购开单应到待收货列表", this.getListViewValue().size()==detailCount);
		}
	}
	
	public void check收货返单处理__1提交改单(String number, String[] purchaseAgrees, String[] orderAgrees, Map<String, Double> checkMap) {
		PurchaseTicketForm form = this.getForm();
		this.loadView("RechangeList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.onMenu("改单处理");
		boolean change=false;
		if (purchaseAgrees==null)		purchaseAgrees=new String[0];
		if (orderAgrees==null)			orderAgrees = new String[0];
		if (purchaseAgrees.length>0)
			this.setCheckGroup("voParamMap.PurchaseAgree", purchaseAgrees);
		if (orderAgrees.length>0)
			this.setCheckGroup("voParamMap.OrderAgree", orderAgrees);
		for (OrderDetail pur: form.getSelectFormer4Purchase().getSelectedList()) {
			Double checkValue = null;
			String kname="ReceiptAmount";
			StringBuffer fname = new StringBuffer("receiptAmount");
			form.getNoteFormer4Purchase().getNoteString(form.getDomain(), fname);
			if (checkMap.containsKey(kname)==false) {
			} else if ((checkValue=checkMap.get(kname))!=null && checkValue!=form.getDomain().getReceiptTicket().getReceiptAmount()) {
				form.getNoteFormer4Purchase().getVoNoteMap(pur).put(fname.toString(), checkValue+"");
				change = true;
			} else {
				form.getNoteFormer4Purchase().getVoNoteMap(pur).remove(fname.toString());
			}
			kname = "BadAmount";
			fname = new StringBuffer("badAmount");
			form.getNoteFormer4Purchase().getNoteString(form.getDomain(), fname);
			if (checkMap.containsKey(kname)==false) {
			} else if ((checkValue=checkMap.get(kname))!=null && checkValue!=form.getDomain().getReceiptTicket().getBadAmount()) {
				form.getNoteFormer4Purchase().getVoNoteMap(pur).put(fname.toString(), checkValue+"");
				change = true;
			} else {
				form.getNoteFormer4Purchase().getVoNoteMap(pur).remove(fname.toString());
			}
			kname = "Amount";
			fname = new StringBuffer("amount");
			form.getNoteFormer4Purchase().getNoteString(form.getDomain(), fname);
			if (checkMap.containsKey(kname)==false) {
			} else if ((checkValue=checkMap.get(kname))!=null && checkValue!=form.getDomain().getAmount()) {
				form.getNoteFormer4Purchase().getVoNoteMap(pur).put(fname.toString(), checkValue+"");
				change = true;
			} else {
				form.getNoteFormer4Purchase().getVoNoteMap(pur).remove(fname.toString());
			}
			if ((checkValue=checkMap.get("BackupAmount"))!=null)
				pur.getPurchaseTicket().setBackupAmount(checkValue);
			if ((checkValue=checkMap.get("CancelAmount"))!=null)
				pur.getPurchaseTicket().setCancelAmount(checkValue);
			if ((checkValue=checkMap.get("RearrangeAmount"))!=null)
				pur.getPurchaseTicket().setRearrangeAmount(checkValue);
			if ((checkValue=checkMap.get("OverAmount"))!=null)
				pur.getPurchaseTicket().setOverAmount(checkValue);
		}
		form.getDomain().setChangeRemark(new StringBuffer().append(Arrays.toString(purchaseAgrees)).append(Arrays.toString(orderAgrees)).toString());
		this.onMenu("提交改单处理");
		OrderDetail pur=form.getSelectFormer4Purchase().getLast(), spur=pur.getSnapShot();
		if (true) {
			Assert.assertTrue("提交改单处理失败", this.hasMenu("提交改单处理")==false);
			this.setFilters("number", number);
			Assert.assertTrue("采购处理收货改单申请失败", this.getListViewValue().size()==0);
			Assert.assertTrue("采购明细应是处理完收货改单的状态", pur.getStPurchase()==30);
			if (change)
				Assert.assertTrue("每个改单申请明细都要有改单内容", StringUtils.equals(pur.getNotes(), spur.getNotes())==false);
			Assert.assertTrue("每个改单申请明细都要有申请原因", StringUtils.equals(pur.getChangeRemark(), spur.getChangeRemark())==false);
			Assert.assertTrue("采购金额=采购价格*数量", pur.getPurchaseTicket().getPmoney() == pur.getPrice()*pur.getAmount());
		}
		if (form.isRechange2Order()) {
			this.loadFormView(this.getModeList().getSelfOrderForm(), "RechangeList", "number", number);
			Assert.assertTrue("应返订单改单处理", this.getListViewValue().size()==detailCount);
			Assert.assertTrue("剩余有订单改单内容", form.getNoteFormer4Purchase().getVoNoteMapEX(pur, "voParamMap").size()>0);
		} else if (form.isRechange2Arrange()) {
			this.loadFormView(this.getModeList().getSelfArrangeForm(), "RechangeList", "number", number);
			Assert.assertTrue("应返排单改单处理", this.getListViewValue().size()==detailCount);
		}
	}
	
	public void check收货返单处理__2拒绝收货(String number, Object... notes) {
		PurchaseTicketForm form = this.getForm();
		this.loadView("RechangeList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.onMenu("改单处理");
		for (int ni=0, nsize=notes.length; ni<nsize; ni+=2) {
			String fieldName=(String)notes[ni];
			Object value = notes[ni+1];
			this.setNoteText(fieldName, value);
		}
		form.getDomain().setChangeRemark("拒绝收货");
		this.onMenu("拒绝收货");
		if (true) {
			this.setFilters("number", number);
			Assert.assertTrue("采购处理收货改单为拒绝收货失败", this.getListViewValue().size()==0);
			this.loadFormView(new ReceiptTicketForm(), "AuditList", "number", number);
			Assert.assertTrue("拒绝收货应到收货确认", this.getListViewValue().size()==detailCount);
			for (OrderDetail detail: form.getDetailList()) {
				OrderDetail sd = (OrderDetail)detail.getSnapShot();
				Assert.assertTrue("采购明细应是处理完收货改单的状态", detail.getStPurchase()==30);
				Assert.assertTrue("每个改单申请明细都要改单内容", StringUtils.equals(detail.getNotes(), sd.getNotes())==false);
				Assert.assertTrue("每个改单申请明细都要有申请原因", StringUtils.equals(detail.getChangeRemark(), sd.getChangeRemark())==false);
			}
		}
	}
	
	public void check供应商返单处理__1订单改单_2排单改单重新排单_3继续商家采购(char type, String number) {
		PurchaseTicketForm form = this.getForm();
		this.loadView("RechangeList", "number", number);
		Assert.assertTrue("要采购处理不通过的商家采购单", this.getListViewValue().size()>0);
		int detailCount = this.getListViewValue().size();
		this.setSqlListSelect(1);
		try {
			this.onMenu("改单处理");
			Assert.fail("供应商返单的采购单，不可做为收货改单处理");
		} catch(Exception e) {}
		this.onMenu("供应商返单处理");
		Assert.assertTrue("同1采购单多明细一起处理", this.getListViewValue().size()==detailCount);
		this.setEditAllSelect(detailCount);
		if ("商品更改转交订单处理，采购停止".length()>0 && type=='1') {
			this.onMenu("订单改单申请");
			Assert.assertTrue("返回外列表", this.hasMenu("订单更改申请")==false);
			OrderDetail pur=form.getSelectFormer4Purchase().getLast(), spur=pur.getSnapShot();
			Assert.assertTrue("订单改单，采购停止", pur.getStOrder()==40
					&& pur.getStPurchase()==0 && pur.getSendId()==0);
			this.loadFormView(this.getModeList().getSelfOrderForm(), "RechangeList", "number", number);
			Assert.assertTrue("返订单改单处理", this.getListViewValue().size()==detailCount);
		}
		if ("商品更改转交排单处理，采购停止".length()>0 && type=='2') {
			this.setRadioGroup("重新排单");
			this.onMenu("排单改单申请");
			Assert.assertTrue("返回外列表", this.hasMenu("订单更改申请")==false);
			OrderDetail pur=form.getSelectFormer4Purchase().getLast(), spur=pur.getSnapShot();
			Assert.assertTrue("排单改单，采购停止", pur.getArrangeId()==40
					&& pur.getStPurchase()==0 && pur.getSendId()==0);
			this.loadFormView(this.getModeList().getSelfArrangeForm(), "RechangeList", "number", number);
			Assert.assertTrue("返排单改单处理", this.getListViewValue().size()==detailCount);
		}
		if ("继续供应商采购开单".length()>0 && type=='3') {
			this.onMenu("继续采购");
			Assert.assertTrue("返回外列表", this.hasMenu("订单更改申请")==false);
			OrderDetail pur=form.getSelectFormer4Purchase().getLast(), spur=pur.getSnapShot(), up=pur.getVoparam("UpOrder");
			Assert.assertTrue("改单完继续采购", pur.getStPurchase()==30 && pur.getSendId()==10);
			Assert.assertTrue("生成向上级采购的订单，作为客户或下级分公司，订单待审核，采购价转订单价", up!=null && up.getId()>0 && up.getSellerId()!=pur.getSellerId()
					&& up.getClient().getName()!=null && up.getClient().getFromSellerId()==pur.getSellerId()
					&& up.getStOrder()==20 && up.getUorder()!=null
					&& up.getOrderTicket().getCprice()==pur.getPrice() && up.getOrderTicket().getCmoney()==pur.getPurchaseTicket().getPmoney());
			Assert.assertTrue("原采购客户不变", pur.getClient().getFromSellerId()==0 && StringUtils.isBlank(pur.getClient().getUaccept()));
			this.loadView("ShowQuery", "number", number, "stPurchase", 30);
			Assert.assertTrue("采购单继续生效", this.getListViewValue().size()==detailCount);
		}
	}
	
	private void test普通采购() {
		if ("1不可采购开单，红冲中".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_普通(11);
			this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
			try {
				this.check普通采购__1采购_2改单重新排单('1', number);
				Assert.fail("订单开红冲应不可开采购单");
			}catch(Exception e) {
			}
		}
		if ("2采购开单".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_普通(12, 12);
			this.check普通采购__1采购_2改单重新排单('1', number);
		}
		if ("3采购改单申请，改排单重新排单，14不通过".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_普通(14, 14);
			this.check普通采购__1采购_2改单重新排单('2', number);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('2', number);
		}
		if ("4采购改单申请，改订单供应商and商品".length()>0 && "通过".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_普通(1, 1);
			this.check普通采购改订单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('2', number);
		}
		if ("4采购改单申请，改订单供应商and商品".length()>0 && "不通过".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_普通(2, 2);
			this.check普通采购改订单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('1', number);
		}
		if ("4采购改单申请，改订单供应商and商品".length()>0 && "重发起改单，通过".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_普通(2, 2);
			this.check普通采购改订单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('1', number);
			this.check普通采购改订单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('2', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('2', number);
		}
		if ("4采购改单申请，改订单供应商and商品".length()>0 && "3改数量为0，通过0".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_普通(3, 3);
			this.check普通采购改订单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('4', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('3', number);
		}
		if ("4采购改单申请，改订单供应商and商品".length()>0 && "4不通过的改单，删除".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_普通(4, 4);
			this.check普通采购改订单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('1', number);
			this.check普通采购改订单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('3', number);
		}
		if ("生产成品车间加工，指定原料领用数，铁帽外购，委外加工黑棒委外有留用数".length()>0 && this.getModeList().contain(TestMode.Purchase)) {
			this.setTestStart();
			this.getModeList().removeMode(TestMode.Purchase, TestMode.Product);
			this.getModeList().setMode(TestMode.Purchase);
			new InStoreTicketTest().get内部入库(new CommodityTest().getC浆料(), 1);
			new InStoreTicketTest().get内部入库(new CommodityTest().getC白棒(), 1);
			this.getModeList().removeMode(TestMode.Purchase, TestMode.Product);
			this.getModeList().setMode(TestMode.Product);
			String number = new ArrangeTicketTest().get客户订单_普通(2);
			new PPurchaseTicketTest().check生产开单_1设置Bom_2物料计算_3拆分('1', number, new CommodityTest().getS黑棒().getName());
			if ("铁帽采购收货".length()>0) {
				this.getModeList().removeMode(TestMode.Purchase, TestMode.Product);
				this.getModeList().addMode(TestMode.Purchase);
				new PurchaseTicketTest().check原物料普通采购__1采购_2同商品采购('1', number);
				new ReceiptTicketTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
			}
			if ("黑棒加工采购收货".length()>0) {
				this.getModeList().removeMode(TestMode.Purchase, TestMode.Product);
				this.getModeList().addMode(TestMode.Product);
				new PPurchaseTicketTest().check原物料普通采购__1采购_2同商品采购('1', number);
				new PPurchaseTicketTest().check生产开单_1设置Bom_2物料计算_3拆分('2', number);
				if ("成品开单，生产录入领料数，委外白棒有留用1".length()>0) {
					new PPurchaseTicketTest().check普通采购__1采购_2改单重新排单('1', number);
					new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("12", number, new Object[0], new Object[]{
							new CommodityTest().getS黑棒(),0d,2d,0d,0d,0d,
							new CommodityTest().getC浆料(),0d,1.68d,0d,0d,0d,
							new CommodityTest().getC白棒(),0d,3d,0d,0d,1d,
							new CommodityTest().getC铁帽(),0d,4d,0d,0d,0d });
				}
				new PReceiptTicketTest().check收货开单__1全数收货_2部分收货n拆单('1', number, "monthnum", "like %.%");
				new PPurchaseTicketTest().check生产开单_1设置Bom_2物料计算_3拆分('2', number);
			}
			if ("成品收货".length()>0) {
				new PReceiptTicketTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
				this.getModeList().removeMode(TestMode.Purchase, TestMode.Product);
				this.getModeList().addMode(TestMode.Purchase);
			}
		}
	}
	
	private void test原物料普通采购() {
		if ("BOM原物料安排为去请购，选择开单".length()>0) {
			this.setTestStart();
			String number = new POrderTicketTest().get备货订单生产(1,2);
			new PPurchaseTicketTest().check生产开单_1设置Bom_2物料计算_3拆分('1', number, new CommodityTest().getS黑棒().getName());
			this.check原物料普通采购__1采购_2同商品采购('1', number);
		}
		if ("BOM原物料安排为去请购，同商品开单".length()>0) {
			this.setTestStart();
			String number = new POrderTicketTest().get备货订单生产(1,2);
			new PPurchaseTicketTest().check生产开单_1设置Bom_2物料计算_3拆分('1', number, new CommodityTest().getS黑棒().getName());
			this.check原物料普通采购__1采购_2同商品采购('2', number);
		}
	}
	
	private void test订单红冲() {
		if ("已采购，开订单红冲".length()>0) {
			if ("1通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(1, 1);	
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('1', number);
			}
			if ("3不通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(3, 3);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
				this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('2', number);
			}
			if ("4红冲取消数量，通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(4, 4);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("2", number);
				this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('1', number);
			}
			if ("5在订单红冲复制新增明细，可常规".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(5, 5);
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("3", number);
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "ArrangeList", "number", number);
				Assert.assertTrue("复制新增(已采购在途的)新订单明细应可以待排单", this.getListViewValue().size()==3);
			}
			if ("6在订单红冲追加订单，可请购".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(6, 6);
				this.getModeList().getSelfOrderTest().check已排单红冲__1追加订单('1', number);
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "ArrangeList", "number", number);
				Assert.assertTrue("在订单红冲追加订单(已采购在途的）新订单明细应可以待排单", this.getListViewValue().size()==2);
			}
		}
		if ("红冲采购申请取消，转交排单处理，排单处理为重新排单，排单为常规，采购取消审核通过".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(1, 1);
			String purName = this.getSelfPurchaseChoosableLogic().toTrunk(this.getForm().getDetailList().get(0).getPurchaseTicket()).getPurName();
			this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
			this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('3', number);
			try {
				new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('1', number, "取消");
				Assert.fail("有取消申请，应不能通过");
			}catch(Exception e) {
			}
			new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('3', number, "取消");
			this.check取消审核__1同意_2不同意('1', purName);
			new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('1', number);
		}
		if ("红冲采购申请转备料，转交排单处理，排单处理为重新排单，排单为请购".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(2, 2);
			this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
			this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('4', number);
			try {
				new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('1', number, "转备料");
				Assert.fail("有转备料申请，应不能通过");
			}catch(Exception e) {
			}
			new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('3', number, "转备料");
			new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('2', number);
		}
		if ("红冲取消下单，红冲采购申请取消，转交排单处理，排单处理为重新排单无订单，采购取消审核不通过".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(3, 3);
			String purName = this.getSelfPurchaseChoosableLogic().toTrunk(this.getForm().getDetailList().get(0).getPurchaseTicket()).getPurName();
			this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("2", number);
			this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('3', number);
			try {
				new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('1', number, "取消");
				Assert.fail("有取消申请，应不能通过");
			}catch(Exception e) {
			}
			new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('3', number, "取消");
			this.check取消审核__1同意_2不同意('2', purName);
		}
	}
	
	private void test排单红冲() {
		if ("1常规应不可红冲，请购未采购不可红冲".length()>0) {
			if (true) {
				this.setTestStart();
				String number = new ArrangeTicketTest().get客户订单_常规(1);
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "DoadjustList",
						"number", number);
				Assert.assertTrue("排单用常规库存，应不能红冲请购发货方式", this.getListViewValue().size()==0);
			}
			if (true) {
				this.setTestStart();
				String number = new ArrangeTicketTest().get客户订单_普通(1);
				this.loadFormView(this.getModeList().getSelfArrangeForm(), "DoadjustList",
						"number", number);
				Assert.assertTrue("排单请购未采购开单，应不能红冲请购发货方式", this.getListViewValue().size()==0);
			}
		}
		if ("多个中选择通过1个明细".length()>0) {
			if ("1通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(1, 1);
				new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', number);
				this.loadView("AdjustList", "number", number);
				this.setSqlAllSelect(2);
				this.onMenu("红冲处理");
				this.setEditAllSelect(1);
				this.setRadioGroup("无影响");
				this.onMenu("红冲通过");
				this.setFilters("number", number);
				Assert.assertTrue("只通过1个应还剩下1个", this.getListViewValue().size()==1);
			}
			if ("2不通过".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(2, 2);
				new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', number);
				this.loadView("AdjustList", "number", number);
				this.setSqlAllSelect(2);
				this.onMenu("红冲处理");
				this.setEditAllSelect(1);
				this.getForm().getOrderFirst().setChangeRemark("不通过");
				this.onMenu("不通过驳回红冲");
				this.setFilters("number", number);
				Assert.assertTrue("只通过1个应还剩下1个", this.getListViewValue().size()==1);
			}
			if ("3返排单处理".length()>0) {
				this.setTestStart();
				String number = this.get客户订单_普通(3, 3);
				new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', number);
				this.loadView("AdjustList", "number", number);
				this.setSqlAllSelect(2);
				this.onMenu("红冲处理");
				this.setEditAllSelect(1);
				this.getForm().getOrderFirst().setChangeRemark("转交排单处理");
				this.setRadioGroup("取消");
				this.onMenu("转交排单处理");
				this.setFilters("number", number);
				Assert.assertTrue("只通过1个应还剩下1个", this.getListViewValue().size()==1);
			}
		}
		if ("2普通请购转直发".length()>0) {
			if ("1通过".length()>0) {
				this.setTestStart();
				String number=this.get客户订单_普通(1, 1);
				new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', number);
				this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('1', number);
			}
			if ("2不通过".length()>0) {
				this.setTestStart();
				String number=this.get客户订单_普通(2, 2);
				new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', number);
				this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('2', number);
			}
		}
		if ("3原采购取消申请，并转排单红冲处理，排单处理为重新安排，安排用常规库存，采购取消审核通过".length()>0) {
			this.setTestStart();
			String number=this.get客户订单_普通(3, 3);
			String purName = this.getSelfPurchaseChoosableLogic().toTrunk(this.getForm().getDetailList().get(0).getPurchaseTicket()).getPurName();
			new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', number);
			this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('3', number);
			new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('3', number, "取消");
			new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('1', number);
			this.check取消审核__1同意_2不同意('1', purName);
		}
		if ("4原采购转备料申请，并转交排单红冲处理，排单处理为重新排单，安排请购".length()>0) {
			this.setTestStart();
			String number=this.get客户订单_普通(4, 4);
			new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', number);
			this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('4', number);
			new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('3', number, "转备料");
			new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('2', number);
		}
	}
	
	private void test收货改单处理() {
if (1==1) {
		if ("收货有次品，采购处理为拒收次品，收货确认1拒收次品2收货合格品".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("5", number);
			this.check收货返单处理__2拒绝收货(number, "receiptAmount",9d, "badAmount",0d);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("12", number);
		}
		if ("改商品型号，采购处理为拒收，收货确认后可继续收货".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", number);
			this.check收货返单处理__2拒绝收货(number, "receiptAmount",0d);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("1", number);
		}
		if ("改供应商、商品型号，采购同意，收货确认更改".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("13", number);
			this.check收货返单处理__1提交改单(number, new String[]{"供应商","更改商品"}, null, new HashMap<String, Double>());
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
		if ("改商品型号，采购转排单备料重排单，排单同意，收货确认为备料".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("BackupAmount", 11d);
			checkMap.put("RearrangeAmount", 11d);
			this.check收货返单处理__1提交改单(number, null, null, checkMap);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
		if ("改商品型号，采购转排单取消重排单，排单同意，收货确认为无收货".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("CancelAmount", 11d);
			checkMap.put("RearrangeAmount", 11d);
			this.check收货返单处理__1提交改单(number, null, null, checkMap);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
		if ("改商品型号，采购备料重排单转排单，排单改为取消8转备料3重排单11，采购认同，收货确认只收无法退货的备料3".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("BackupAmount", 11d);
			checkMap.put("RearrangeAmount", 11d);
			this.check收货返单处理__1提交改单(number, null, null, checkMap);
			checkMap = new HashMap<String, Double>();
			checkMap.put("BackupAmount", 3d);
			checkMap.put("CancelAmount", 8d);
			checkMap.put("RearrangeAmount", 11d);
			new ArrangeTicketTest().check返改单申请采购__2不通过(number, checkMap);
			checkMap = new HashMap<String, Double>();
			this.check收货返单处理__1提交改单(number, null, null, checkMap);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
		if ("改供应商|价格|多收货，采购同意供应商价格|多备料转排单，排单同意，收货确认多备料".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("124", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("OverAmount", 10d);
			this.check收货返单处理__1提交改单(number, new String[]{"供应商", "价格"}, null, checkMap);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
}
		if ("改供应商|商品型号|有次品2，采购同意供应商更改|拒收次品|转订单改商品|转排单取消2重排单2，订单同意，排单同意，收货确认1拒收次品2收货".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("135", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("ReceiptAmount", 9d);
			checkMap.put("BadAmount", 0d);
			checkMap.put("Amount", 9d);
			checkMap.put("CancelAmount", 2d);
			checkMap.put("RearrangeAmount", 2d);
			this.check收货返单处理__1提交改单(number, new String[]{"供应商"}, new String[]{"更改商品"}, checkMap);
			this.getModeList().getSelfOrderTest().check采购收货改单处理__1同意n_不同意('1', number, new String[]{"更改商品"});
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("12", number);
		}
		if ("多收货，采购转排单多备料，排单同意，收货确认多备料".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("4", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("OverAmount", 10d);
			this.check收货返单处理__1提交改单(number, null, null, checkMap);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
		if ("多收货，采购转订单多下单数，订单同意，收货确认多收货给订单".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("4", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("Amount", 21d);
			this.check收货返单处理__1提交改单(number, null, new String[]{"数量"}, checkMap);
			this.getModeList().getSelfOrderTest().check采购收货改单处理__1同意n_不同意('1', number, new String[]{"数量"});
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
		if ("多收货10有次品2，采购拒收次品2转排单多备料8，排单同意，收货确认1拒收次品2确认多收备料8".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("45", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("ReceiptAmount", 17d);
			checkMap.put("BadAmount", 0d);
			checkMap.put("OverAmount", 6d);
			this.check收货返单处理__1提交改单(number, null, null, checkMap);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("12", number);
		}
		if ("多收货，采购转排单多备料，排单不同意改回按原采购数量，采购同意，收货确认1减少2收货".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("4", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("OverAmount", 10d);
			this.check收货返单处理__1提交改单(number, null, null, checkMap);
			checkMap = new HashMap<String, Double>();
			checkMap.put("ReceiptAmount", 11d);
			checkMap.put("OverAmount", 0d);
			new ArrangeTicketTest().check返改单申请采购__2不通过(number, checkMap);
			this.check收货返单处理__1提交改单(number, null, null, new HashMap<String, Double>());
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("12", number);
		}
		if ("多收货10有次品2，采购拒收次品2转排单多备料8，排单改收货15多备料4，采购认同，收货确认1拒收次品2+4确认多收备料4".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("45", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("ReceiptAmount", 19d);
			checkMap.put("BadAmount", 0d);
			checkMap.put("OverAmount", 8d);
			this.check收货返单处理__1提交改单(number, null, null, checkMap);
			checkMap = new HashMap<String, Double>();
			checkMap.put("ReceiptAmount", 15d);
			checkMap.put("OverAmount", 4d);
			new ArrangeTicketTest().check返改单申请采购__2不通过(number, checkMap);
			this.check收货返单处理__1提交改单(number, null, null, new HashMap<String, Double>());
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("12", number);
		}
		if ("改供应商、商品型号、多收货，采购同意改供应商，转订单处理改商品、多下单，订单同意改商品、多下单，收货确认多收货给订单".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("134", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("Amount", 21d);
			this.check收货返单处理__1提交改单(number, new String[]{"供应商"}, new String[]{"更改商品","数量"}, checkMap);
			this.getModeList().getSelfOrderTest().check采购收货改单处理__1同意n_不同意('1', number, new String[]{"更改商品","数量"});
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
		if ("改供应商、商品型号、多收货，采购同意改供应商，转订单处理改商品、多下单，订单同意改商品、不同意多下单，采购改为拒绝多收，收货确认1减少2确认给订单".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("134", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("Amount", 21d);
			this.check收货返单处理__1提交改单(number, new String[]{"供应商"}, new String[]{"更改商品","数量"}, checkMap);
			this.getModeList().getSelfOrderTest().check采购收货改单处理__1同意n_不同意('1', number, new String[]{"更改商品"});
			checkMap = new HashMap<String, Double>();
			checkMap.put("ReceiptAmount", 11d);
			checkMap.put("Amount", 11d);
			this.check收货返单处理__1提交改单(number, null, new String[]{"更改商品"}, checkMap);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("12", number);
		}
		if ("改供应商|商品型号|多收货|有次品，采购同意改供应商|拒收次品|转订单处理改商品多下单，订单同意改商品|不同意多下单，采购转排单多收备料，排单同意，收货确认1减少2确认给订单多备料".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("1345", number);
			Map<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("ReceiptAmount", 19d);
			checkMap.put("BadAmount", 0d);
			checkMap.put("Amount", 19d);
			this.check收货返单处理__1提交改单(number, new String[]{"供应商"}, new String[]{"更改商品","数量"}, checkMap);
			this.getModeList().getSelfOrderTest().check采购收货改单处理__1同意n_不同意('1', number, new String[]{"更改商品"});
			checkMap = new HashMap<String, Double>();
			checkMap.put("Amount", null);
			checkMap.put("OverAmount", 8d);
			this.check收货返单处理__1提交改单(number, null, new String[]{"更改商品"}, checkMap);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("12", number);
		}
		if ("改供应商|价格|商品|多收货，采购同意供应商价格|订单改商品数量，订单同意改商品|不同意该数量，采购转排单多备料，排单同意，收货确认多备料".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("1234", number);
			if (true) {
				Map<String, Double> checkMap = new HashMap<String, Double>();
				checkMap.put("Amount", 21d);
				this.check收货返单处理__1提交改单(number, new String[]{"供应商", "价格"}, new String[]{"更改商品", "数量"}, checkMap);
			}
			this.getModeList().getSelfOrderTest().check采购收货改单处理__1同意n_不同意('1', number, new String[]{"更改商品"});
			if (true) {
				Map<String, Double> checkMap = new HashMap<String, Double>();
				checkMap.put("Amount", 11d);
				checkMap.put("OverAmount", 10d);
				this.check收货返单处理__1提交改单(number, null, null, checkMap);
			}
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
	}
	
	private void test供应商返单处理() {
		if ("不通过的商家订单返商家改单采购处理，1订单更改通过，可采购开单继续为商家订单".length()>0) {
			this.setTestStart();
			String curOrdNumber=null, upOrdNumber=null;
			if (true) {
				String numbers[]=new LOrderTicketTest().get待商家审核订单__1客户_2备货('1', 2, 2);
				curOrdNumber = numbers[0];
				upOrdNumber = numbers[1];
				HashMap<String, Object> changeMap = new HashMap<String, Object>();
				changeMap.put("model", "mm");
				new LOrderTicketTest().check审核商家订单__1通过_2不通过('2', upOrdNumber, changeMap);
			}
			this.setTransSeller(new Seller4lLogic().get吉高电子());
			this.check供应商返单处理__1订单改单_2排单改单重新排单_3继续商家采购('1', curOrdNumber);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('2', curOrdNumber);
			this.check普通采购__1采购_2改单重新排单('1', curOrdNumber);
		}
		if ("不通过的商家订单返商家改单，2排单更改通过并安排请购，可采购开单继续为商家订单".length()>0) {
			this.setTestStart();
			String curOrdNumber=null, upOrdNumber=null;
			if (true) {
				String numbers[] = new LOrderTicketTest().get待商家审核订单__1客户_2备货('1', 3, 3);
				curOrdNumber = numbers[0];
				upOrdNumber = numbers[1];
				HashMap<String, Object> changeMap = new HashMap<String, Object>();
				changeMap.put("model", "mm");
				new LOrderTicketTest().check审核商家订单__1通过_2不通过('2', upOrdNumber, changeMap);
			}
			this.setTransSeller(new Seller4lLogic().get吉高电子());
			this.check供应商返单处理__1订单改单_2排单改单重新排单_3继续商家采购('2', curOrdNumber);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('1', curOrdNumber);
			new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('2', curOrdNumber);
			this.check普通采购__1采购_2改单重新排单('1', curOrdNumber);
			upOrdNumber = ((OrderDetail)this.getForm().getDomain().getVoParamMap().get("UpOrder")).getOrderTicket().getNumber();
			this.setTransSeller(new Seller4lLogic().get南宁古城());
			this.loadFormView(new OrderTicketForm(), "ShowQuery","DetailForm.selectedList", "number", upOrdNumber);
			Assert.assertTrue("重采购开单生成链接商家订单", this.getListViewValue().size()==2);
		}
		if ("不通过的商家订单返商家改单，3继续采购，重开商家订单审核通过".length()>0) {
			this.setTestStart();
			String curOrdNumber=null, upOrdNumber=null;
			if (true) {
				String numbers[] = new LOrderTicketTest().get待商家审核订单__1客户_2备货('1', 3, 3);
				curOrdNumber = numbers[0];
				upOrdNumber = numbers[1];
				HashMap<String, Object> changeMap = new HashMap<String, Object>();
				new LOrderTicketTest().check审核商家订单__1通过_2不通过('2', upOrdNumber, changeMap);
			}
			this.setTransSeller(new Seller4lLogic().get吉高电子());
			this.check供应商返单处理__1订单改单_2排单改单重新排单_3继续商家采购('3', curOrdNumber);
			upOrdNumber = ((OrderDetail)this.getForm().getDomain().getVoParamMap().get("UpOrder")).getOrderTicket().getNumber();
			if ("南宁审核通过".length()>0) {
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				new LOrderTicketTest().check审核商家订单__1通过_2不通过('1', upOrdNumber, null);
			}
		}
	}
	
	private void test红冲申请_编辑() {
		if ("界面验证".length()>0) {
			String number = this.get客户订单_普通(11, 11);
			this.loadView("DoadjustList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			if ("不可红冲开单".length()>0) {
				try {
					this.onMenu("红冲申请");
					Assert.fail("可编辑时应不可开红冲");
				}catch(Exception e) {
				}
			}
			if ("不可红冲不通过确认".length()>0) {
				try {
					this.onMenu("红冲不通过确认");
					Assert.fail("可红冲时应不可不通过确认");
				}catch(Exception e) {
				}
			}
		}
		if ("11普通采购开单，可重编辑".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.check红冲申请2编辑采购_1供应商_2价格("12", number);
		}
		if ("12直发采购开单，可重编辑".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_直发(12, 12);
			this.check红冲申请2编辑采购_1供应商_2价格("12", number);
		}
		if ("13当地购采购开单，可重编辑".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_当地购(13, 13);
			this.check红冲申请2编辑采购_1供应商_2价格("12", number);
		}
	}
	
	private void testLink当地购_收货改单_采购取消() {
	if ("上级公司让下级当地购".length()>0) {
			if ("总部给分公司下订单安排当地购，当地采购开单，上级审核通过".length()>0) {
				this.setTestStart();
				String number = new ArrangeTicketTest().get客户订单_当地购(1, 2);
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				this.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('1', number);
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('2', number);
			}
			if ("总部给分公司下订单安排当地购，当地采购开单，上级审核不通过，当地重编辑提交，上级审核通过".length()>0) {
				this.setTestStart();
				String number = new ArrangeTicketTest().get客户订单_当地购(1, 2);
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				this.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('1', number);
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('3', number);
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				this.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('4', number);
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				this.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('2', number);
			}
	}
	if ("客户商家采购改单，上级供应商家处理红冲草稿".length()>0) {
			if ("上级订单，下级收货改商品|价格，采购供应商同意+上级订单开草稿的红冲，上级红冲确认编辑".length()>0) {
				this.setTestStart();
				String upNumber=this.getModeList().getSelfOrderTest().get客户订单(11), monthnum=new OrderTicketTest().getOrderList("number", upNumber).get(0).getMonthnum();
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				String dwNumber=new OrderTicketTest().getOrderList("monthnum", monthnum).get(0).getOrderTicket().getNumber();
				this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("23", dwNumber);
				this.check收货返单处理__1提交改单(dwNumber, new String[]{"更改商品","价格"}, null, new HashMap());
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				try {
					this.getModeList().getSelfOrderTest().check未排单编辑__1型号_2数量0_3价格_4客户商家红冲("13", upNumber);
					Assert.fail("要先处理客户商家红冲草稿");
				} catch(Throwable e) {
					BaseDAO.getSessionInThread().close();
				}
				this.getModeList().getSelfOrderTest().check未排单编辑__1型号_2数量0_3价格_4客户商家红冲("134", upNumber);
			}
			if ("上级排单常规，下级收货改商品|价格，采购供应商同意+上级订单开草稿的红冲，上级红冲确认编辑".length()>0) {
				this.setTestStart();
				this.getModeList().getModeList();
				String upNumber=new ArrangeTicketTest().get客户订单_常规(11), monthnum=new OrderTicketTest().getOrderList("number", upNumber).get(0).getMonthnum();
				this.setTransSeller(new Seller4lLogic().get吉高电子());
				String dwNumber=new OrderTicketTest().getOrderList("monthnum", monthnum).get(0).getOrderTicket().getNumber();
				this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("23", dwNumber);
				this.check收货返单处理__1提交改单(dwNumber, new String[]{"更改商品","价格"}, null, new HashMap());
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				try {
					this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", upNumber);
					Assert.fail("要先处理客户商家红冲草稿");
				} catch(Throwable e) {
				}
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("14", upNumber);
			}
	}
	if ("客户商家采购取消审核通过，上级供应商家处理红冲取消数量0草稿".length()>0) {
			if ("客户商家对排单红冲，处理为取消+重排单，采购取消审核通过，上级订单未排单编辑".length()>0) {
				this.setTestStart();
				String upNumber=this.getModeList().getSelfOrderTest().get客户订单(11), monthnum=new OrderTicketTest().getOrderList("number", upNumber).get(0).getMonthnum();
				this.setTransSeller(new Seller4lLogic().get吉高电子());			
				OrderDetail pur=new PurchaseTicketTest().getPurchaseList("monthnum", monthnum).get(0);
				String dwOrdNumber=pur.getOrderTicket().getNumber();
				new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', dwOrdNumber);
				this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('3', dwOrdNumber);
				new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('3', dwOrdNumber, "取消");
				String purName = pur.getVoparam(PurchaseT.class).getPurName();
				this.check取消审核__1同意_2不同意('1', purName);
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				try {
					this.getModeList().getSelfOrderTest().check未排单编辑__1型号_2数量0_3价格_4客户商家红冲("2", upNumber);
					Assert.fail("要先处理客户商家红冲草稿");
				} catch(Throwable e) {
					BaseDAO.getSessionInThread().close();
				}
				this.getModeList().getSelfOrderTest().check未排单编辑__1型号_2数量0_3价格_4客户商家红冲("24", upNumber);
			}
			if ("客户商家对排单红冲，处理为取消+重排单，采购取消审核通过，上级订单已排单红冲".length()>0) {
				this.setTestStart();
				this.getModeList().getModeList();
				String upNumber=new ArrangeTicketTest().get客户订单_常规(11), monthnum=new OrderTicketTest().getOrderList("number", upNumber).get(0).getMonthnum();
				this.setTransSeller(new Seller4lLogic().get吉高电子());			
				OrderDetail pur=new PurchaseTicketTest().getPurchaseList("monthnum", monthnum).get(0);
				String dwOrdNumber=pur.getOrderTicket().getNumber();
				new ArrangeTicketTest().check开红冲__1转直发_2转当地购_3转普通('1', dwOrdNumber);
				this.check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('3', dwOrdNumber);
				new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('3', dwOrdNumber, "取消");
				String purName = pur.getVoparam(PurchaseT.class).getPurName();
				this.check取消审核__1同意_2不同意('1', purName);
				this.setTransSeller(new Seller4lLogic().get南宁古城());
				try {
					this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("2", upNumber);
					Assert.fail("要先处理客户商家红冲草稿");
				} catch(Throwable e) {
				}
				this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("24", upNumber);
			}
	}
		}

	private void temp() {
		if ("采购到货在库发起红冲，仓库不同意红冲".length()>0) {
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(14);
			this.getModeList().getSelfPurchaseTest().check红冲申请1开红冲__A改供应人_B价格加2__1开红冲_2不通过确认_3删除红冲(number, "A", '1');
			this.getModeList().getSelfReceiptTest().check红冲处理__1通过_2不通过('2', number);
		}
	}

	public void test导入库存() {
		this.setQ清空();
		if ("列序号记忆保存".length()>0) {
			StringBuffer sIndexs = new StringBuffer();
			for (int i=0; i<10; i++, sIndexs.append(i).append("\t"));
			this.check导入库存("采购", sIndexs.toString(), sIndexs.deleteCharAt(sIndexs.length()-1).toString());
		}
	}

	public void test待处理() {
		if ("普通采购开单发起改单申请，不通过确认".length()>0) {
			String number = new ArrangeTicketTest().get客户订单_普通(11);
			this.getModeList().getSelfPurchaseTest().check普通采购改订单申请__1发起改单_2重发起改单_3删除改单申请_4改单数量为0('1', number);
			this.getModeList().getSelfOrderTest().check改单申请__1不同意_2通过_3通过改0('1', number);
		}
		if ("BOM原物料安排为去请购".length()>0) {
			String number = new POrderTicketTest().get备货订单生产(1,2);
			new PPurchaseTicketTest().check生产开单_1设置Bom_2物料计算_3拆分('1', number);
		}
		if ("返采购改单处理，收货发起改单申请".length()>0) {
			String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(12);
			new ReceiptTicketTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("2", number);
		}
		if ("订单采购在途，订单发起红冲申请，给采购红冲处理".length()>0) {
			String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(13);
			this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
		}
		if ("采购到货在库发起红冲，仓库不同意红冲".length()>0) {
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(14);
			this.getModeList().getSelfPurchaseTest().check红冲申请1开红冲__A改供应人_B价格加2__1开红冲_2不通过确认_3删除红冲(number, "A", '1');
			this.getModeList().getSelfReceiptTest().check红冲处理__1通过_2不通过('2', number);
		}
		if ("待取消审核，订单采购在途，订单发起红冲，采购判定原采购取消|重排单，排单同意".length()>0) {
			String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(15);
			this.getModeList().getSelfOrderTest().check已排单红冲__1红冲_2取消下单_3复制新增添1_4客户红冲草稿("1", number);
			this.getModeList().getSelfPurchaseTest().check红冲处理__1通过_2不通过_3取消转交排单_4转备料转交排单('3', number);
			new ArrangeTicketTest().check红冲申请处理__1通过_2不通过_3重排单('3', number, "取消");
		}
		if ("待退货审核，订单采购收货开单发起改单申请，采购判定原采购退货|重排单，排单同意，收货改单确认".length()>0) {
			String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(16);
			this.getModeList().getSelfReceiptTest().check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("3", number);
			HashMap<String, Double> checkMap = new HashMap<String, Double>();
			checkMap.put("CancelAmount", 16d);
			checkMap.put("RearrangeAmount", 16d);
			this.getModeList().getSelfPurchaseTest().check收货返单处理__1提交改单(number, null, null, checkMap);
			new ArrangeTicketTest().check返改单申请__1排单通过_2排单不通过_3采购通过('3', number);
			this.getModeList().getSelfReceiptTest().check收货改单确认__1减少收货_2收货确认("2", number);
		}
	}
	
	public String get客户订单_普通(int... amountList) {
		String number = new ArrangeTicketTest().get客户订单_普通(amountList);
		this.check普通采购__1采购_2改单重新排单('1', number);
		return number;
	}
	
	public String get备货订单_普通(int... amountList) {
		String number = this.getModeList().getSelfOrderTest().get备货订单(amountList);
		this.check普通采购__1采购_2改单重新排单('1', number);
		return number;
	}
	
	public String get客户订单_直发(int... amountList) {
		String number = new ArrangeTicketTest().get客户订单_直发(amountList);
		this.check直发采购__1采购_2改单重新排单('1', number);
		return number;
	}
	
	public String get客户订单_当地购(int... amountList) {
		String number = new ArrangeTicketTest().get客户订单_当地购(amountList);
		OrderDetail order = new OrderTicketTest().getOrderList("number", number).get(0);
		long sid = order.getClient().getFromSellerId();
		if (sid == 0) {
			this.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('1', number);
		} else if (sid > 0) {
			Seller dwSeller=new Seller4lLogic().getSellerById(sid), upSeller=new Seller4lLogic().getSellerById(order.getSellerId());
			this.setTransSeller(dwSeller);
			this.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('1', number);
			this.setTransSeller(upSeller);
			this.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交('2', number);
		}
		return number;
	}
	
	public List<OrderDetail> getPurchaseList(Object... filters0) {
		Object[] filters = this.genFiltersStart(filters0, "selectedList");
		PurchaseTicketTest test = this.getModeList().getSelfPurchaseTest();
		test.loadView("ShowQuery", filters);
		test.setSqlAllSelect(test.getListViewValue().size());
		test.onMenu("选择采购");
		return test.getForm().getSelectFormer4Purchase().getSelectedList();
	}
	public List<OrderDetail> getPurchaseList(Long toSellerId, Object... filters0) {
		Object[] filters = this.genFiltersStart(filters0, "selectedList");
		PurchaseTicketTest test = this.getModeList().getSelfPurchaseTest();
		Seller fromSeller = SellerLogic.getSeller();
		test.setTransSeller(new Seller4lLogic().getSellerById(toSellerId));
		test.loadView("ShowQuery", filters);
		test.setSqlAllSelect(test.getListViewValue().size());
		test.onMenu("选择采购");
		List<OrderDetail> purList = test.getForm().getSelectFormer4Purchase().getSelectedList();
		test.setTransSeller(fromSeller);
		return purList;
	}
	
	private PropertyChoosableLogic.TicketDetail<PurchaseTicketForm, PurchaseTicket, PurchaseT> getSelfPurchaseChoosableLogic() {
		return this.getClass()==PurchaseTicketTest.class? new PurchaseTicketLogic().getTicketChoosableLogic(): new PurchaseTicketLogic().getPTicketChoosableLogic();
	}
	
	protected Supplier getSupplier() {
		if (this.getModeList().contain(TestMode.LinkAsClient))
			return new SupplierTest().get南宁古城伙伴();
		if (this.getModeList().contain(TestMode.LinkAsSubcompany))
			return new SupplierTest().get南宁古城总部();
		return new SupplierTest().get永晋();
	}

	protected void setQ清空() {
		SSaleUtil.executeSqlUpdate("delete from sa_OrderDetail where sellerId=?", "delete from sa_BomDetail where sellerId=?");
	}
}
