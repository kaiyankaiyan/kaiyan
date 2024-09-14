package com.haoyong.sales.test.sale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.TextField;
import net.sf.mily.ui.enumeration.EventListenerType;
import net.sf.mily.webObject.Field;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.SellerViewSetting;
import com.haoyong.sales.base.form.BOMForm;
import com.haoyong.sales.base.logic.CommodityLogic;
import com.haoyong.sales.base.logic.SellerViewSettingLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.PurchaseT;
import com.haoyong.sales.sale.form.PPurchaseTicketForm;
import com.haoyong.sales.sale.form.ReceiptTicketForm;
import com.haoyong.sales.sale.form.SaleQueryForm;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.sale.logic.ArrangeTypeLogic;
import com.haoyong.sales.test.base.CommodityTest;
import com.haoyong.sales.util.SSaleUtil;

public class PPurchaseTicketTest extends PurchaseTicketTest {

	public PPurchaseTicketTest() {
		this.setForm(new PPurchaseTicketForm());
	}
	
	public void check普通采购__1采购_2改单重新排单(char type, String number) {
		PPurchaseTicketForm form = (PPurchaseTicketForm)this.getForm();
		if ("1生产开单".length()>0 && type=='1') {
			this.loadSql("CommonList", "selectedList", "number", number);
			int detailCount = this.getListViewValue().size();
			Assert.assertTrue("订单明细未到生产开单", detailCount>0);
			this.setSqlAllSelect(detailCount);
			if (true) {
				this.check生产开单_1设置Bom_2物料计算_3拆分('1', number);
				this.check生产开单_1设置Bom_2物料计算_3拆分('2', number);
				this.loadSql("CommonList", "selectedList", "number", number);
				this.setBomDetailsShow();
				this.setSqlAllSelect(detailCount);
			}
			this.onMenu("开单");
			this.onButton("生成单号");
			form.getDomain().getPurchaseTicket().setPurDate(new Date());
			this.setFieldText("supplier.name", this.getSupplier().getName());
			for (OrderDetail pur: form.getDetailList())
				pur.setPrice(pur.getAmount()*10);
			new StoreTicketTest().setQ清空();
			this.onMenu("提交");
			Assert.assertTrue("生产开单失败", this.hasMenu("提交")==false);
			this.setFilters("number", number);
			Assert.assertTrue("生产开单失败", this.getListViewValue().size()==0);
			this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
			OrderDetail pur = form.getDetailList().get(form.getDetailList().size()-1);
			if (true) {
				Assert.assertTrue("明细与单头一致", StringUtils.equals(pur.getSupplier().getName(), form.getDomain().getSupplier().getName())
						&& StringUtils.equals(pur.getPurchaseTicket().getNumber(), form.getDomain().getPurchaseTicket().getNumber())
						);
				this.setFilters("commName", pur.getVoparam(CommodityT.class).getCommName());
				Assert.assertTrue("普通生产开单应计算够用数", this.getListViewValue().size()>0);
			}
			this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
			List<BomDetail> purBoms = form.getBomDetails(pur.getMonthnum());
			Assert.assertTrue("有BOM明细", purBoms.size()>0);
			BomDetail b = purBoms.get(purBoms.size()-1);
			if (true) {
				Assert.assertTrue("进入生产阶段，应占用库存物料", b.getStBom()==20 && StringUtils.equals(b.getMonthnum(),pur.getMonthnum()));
				if (b.getBomTicket().getOccupyAmount()+b.getBomTicket().getNotAmount()>0) {
					this.setFilters("commName", b.getVoparam(CommodityT.class).getCommName());
					Assert.assertTrue("普通生产开单应计算够用数", this.getListViewValue().size()>0);
					Assert.assertTrue("生产开单Bom物料减够用数", (Double)this.getListViewColumn("amount").get(0)<0);
					Assert.assertTrue("生产开单Bom物料加订单需求数", (Double)this.getListViewColumn("orderAmount").get(0)>0);
					Assert.assertTrue("生产开单Bom物料加库存锁定数", (Double)this.getListViewColumn("lockAmount").get(0)>0);
				}
			}
			if (true) {
				OrderDetail order = form.getDetailList().get(form.getDetailList().size()-1);
				this.loadView("RecordList");
				form.getMonthnumLinkList().get(0).getEventListenerList().fireListener();
				this.setFilters("number", number);
				Assert.assertTrue("生产开单有BOM要到生产录入", this.getListViewValue().size()>0);
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("13", number);
				this.loadFormView(new ReceiptTicketForm(), "WaitList", "number", number, "purName", order.getVoparam(PurchaseT.class).getPurName());
				Assert.assertTrue("生产开单应到待收货列表", this.getListViewValue().size()==detailCount);
				this.loadView("ShowQuery", "monthnum", order.getMonthnum());
				Assert.assertTrue("生产开单应到生产查询", this.getListViewValue().size()==1);
			}
		}
		if ("2生产改单，重返排单".length()>0 && type=='2') {
			this.loadView("CommonList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单申请");
			this.setCheckGroup("排单");
			this.setRadioGroup("重新排单");
			form.getOrderFirst().setChangeRemark("生产要重返排单");
			this.setEditAllSelect(detailCount);
			try {
				this.onMenu("删除申请改单");
				Assert.fail("生产开改单，应不能删除改单申请");
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
	
	public void check生产开单_1设置Bom_2物料计算_3拆分(char type, String number, String... Request去请购s) {
		PPurchaseTicketForm form = (PPurchaseTicketForm)this.getForm();
		List<String> RequestComms = Arrays.asList(Request去请购s);
		if (RequestComms.size()>0)
			"".toCharArray();
		if ("1设置订单物料BOM，已有Bom则退出".length()>0 && type=='1') {
			this.loadSql("CommonList", "selectedList", "number", number);
			Assert.assertTrue("到生产开单列表", this.getListViewValue().size()>0);
			for (String monthnum: (List<String>)(List)this.getListViewColumn("monthnum")) {
				this.setFilters("monthnum", monthnum);
				this.setSqlListSelect(1);
				this.onMenu("配置BOM");
				BOMForm bomForm = form.getBomForm();
				if ("已有Bom则退出".length()>0 && bomForm.getDetailList().size()>0 && bomForm.getDetailList().get(0).getId()>0)
					return;
				bomForm.getDetailList().clear();
				bomForm.getShowList().clear();
				this.getEditListView().update();
				bomForm.getDomain().setAmount(1);
				bomForm.getDomain().setLevel(10);
				this.onButton("调整显示级数");
				if (true) {
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(0);
					bomDetail.setCommodity(new CommodityTest().getS黑棒());
					bomDetail.getBomTicket().setAunit(1);
					bomDetail.setArrange(RequestComms.contains(bomDetail.getCommodity().getName())? "去请购": new ArrangeTypeLogic().getNormal());
				}
				if (true) {
					this.setEditListSelect(1);
					this.onMenu("新增子级");
					BomDetail bomDetail = bomForm.getDetailList().get(1);
					bomDetail.setCommodity(new CommodityTest().getC浆料());
					bomDetail.getBomTicket().setAunit(0.84);
					bomDetail.setArrange(RequestComms.contains(bomDetail.getCommodity().getName())? "去请购": new ArrangeTypeLogic().getNormal());
				}
				if (true) {
					this.setEditListSelect(2);
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(2);
					bomDetail.setCommodity(new CommodityTest().getC白棒());
					bomDetail.getBomTicket().setAunit(1);
					bomDetail.setArrange(RequestComms.contains(bomDetail.getCommodity().getName())? "去请购": new ArrangeTypeLogic().getNormal());
				}
				if (true) {
					this.setEditListSelect(1);
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(3);
					bomDetail.setCommodity(new CommodityTest().getC铁帽());
					bomDetail.getBomTicket().setAunit(2);
					bomDetail.setArrange("去请购");
				}
				new StoreTicketTest().setQ清空();
				this.onMenu("提交");
				this.setFilters("monthnum", monthnum);
				this.setBomDetailsShow();
			}
			List<BomDetail> orderBoms = form.getBomDetailsMap().values().iterator().next();
			Assert.assertTrue("订单Bom有4个物料", orderBoms.size()==4);
			this.loadFormView(new StoreTicketForm(), "StoreEnoughQuery");
			for (BomDetail bom: orderBoms) {
				this.setFilters("commName", bom.getVoparam(CommodityT.class).getCommName());
				Assert.assertTrue("订单设置Bom减够用数", ((Double)this.getListViewColumn("amount").get(0))<0);
				Assert.assertTrue("订单设置Bom加订单需求数", ((Double)this.getListViewColumn("orderAmount").get(0))>0);
				Assert.assertTrue("订单设置Bom库存锁定数", ((Double)this.getListViewColumn("lockAmount").get(0))==0);
			}
		}
		if ("2订单物料计算".length()>0 && type=='2') {
			this.loadView("BomDetailList", "selectFormer4Bom.selectedList", "number", number);
			this.setFilters("selectFormer4Order.selectedList", "number", number);
			this.onMenu("计算物料配给");
		}
		if ("3拆分订单及物料".length()>0 && type=='3') {
			this.loadSql("CommonList", "selectedList", "number", number);
			Assert.assertTrue("到生产开单列表", this.getListViewValue().size()>0);
			this.setBomDetailsShow();
			int fromBomDetailSize = form.getBomDetailsMap().keySet().size();
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			form.getOrderDetail().setAmount(1);
			List<BomDetail> fromList = new ArrayList<BomDetail>();
			for (Iterator<List<BomDetail>> iter=form.getBomDetailsMap().values().iterator(); iter.hasNext(); fromList.addAll(iter.next()));
			this.onButton("拆分订单");
			for (OrderDetail order: form.getSelectFormer4Order().getSelectedList()) {
				OrderDetail sorder = order.getSnapShot();
				if (sorder.getStPurchase()==0) {
					this.setFilters("monthnum", order.getMonthnum());
					Assert.assertTrue("拆分出订单数量1失败", order.getAmount()<sorder.getAmount());
					Assert.assertTrue("没有生产单要用新月流水号", this.getListViewValue().size()==1);
				} else {
					this.setFilters("monthnum", order.getMonthnum());
					Assert.assertTrue("有生产单拆分出的订单要用新拆分月流水号", this.getListViewValue().size()==2);
					this.loadView("ShowQuery", "monthnum", order.getMonthnum());
					Assert.assertTrue("有生产单拆分要有2个生产单，新生产用拆分月流水号", this.getListViewValue().size()==2);
				}
			}
			if (true) {
				this.loadSql("CommonList", "selectedList","number", number);
				this.setBomDetailsShow();
				Assert.assertTrue("拆分出的订单也应有BOM物料", form.getBomDetailsMap().keySet().size()==2*fromBomDetailSize);
				List<BomDetail> toList = new ArrayList<BomDetail>();
				for (Iterator<List<BomDetail>> iter=form.getBomDetailsMap().values().iterator(); iter.hasNext(); toList.addAll(iter.next()));
				double famount=0, fgot=0, foccupy=0, fnot=0, tamount=0, tgot=0, toccupy=0, tnot=0;
				for (BomDetail b0: fromList) {
					BomDetail b = (BomDetail)b0.getSnapShot();
					famount += b.getAmount();
					fgot += b.getBomTicket().getGotAmount();
					foccupy += b.getBomTicket().getOccupyAmount();
					fnot += b.getBomTicket().getNotAmount();
				}
				for (BomDetail b: toList) {
					tamount += b.getAmount();
					tgot += b.getBomTicket().getGotAmount();
					toccupy += b.getBomTicket().getOccupyAmount();
					tnot += b.getBomTicket().getNotAmount();
				}
				Assert.assertTrue("总需求数应不变", famount==tamount);
				Assert.assertTrue("总配给数应不变", fgot==tgot);
				Assert.assertTrue("总指派数应不变", foccupy==toccupy);
				Assert.assertTrue("总差额数应不变", foccupy==toccupy);
			}
		}
	}
	
	public void check生产录入__1清空_2指定录入_3标准录入(String types, String number, Object[]... Filters0_1Commodity_CommitOccupy1Back1Occupy2Back2) {
		PPurchaseTicketForm form = (PPurchaseTicketForm)this.getForm();
		Object[] filters0=new Object[0], Commodity_CommitOccupy1Back1Occupy2Back2=new Object[0];
		if (Filters0_1Commodity_CommitOccupy1Back1Occupy2Back2.length==2)
			Commodity_CommitOccupy1Back1Occupy2Back2=Filters0_1Commodity_CommitOccupy1Back1Occupy2Back2[1];
		if (Filters0_1Commodity_CommitOccupy1Back1Occupy2Back2.length>=1)
			filters0 = Filters0_1Commodity_CommitOccupy1Back1Occupy2Back2[0];
		this.loadView("RecordList", this.genFiltersStart(filters0, "number", number));
		this.setBomDetailsShow();
		boolean HasRecord = false;
		if ("清空原来的生产录入".length()>0 && types.contains("1")) {
			for (Hyperlink link: form.getMonthnumLinkList()) {
				if (link.getComponent()==null)
					continue;
				OrderDetail order = (OrderDetail)link.getComponent().getFormer();
				List<BomDetail> bomList = order.getVoparam("BomDetailList");
				for (BomDetail bom: bomList) {
					bom.getBomTicket().setCommitAmount(0);
					bom.getBomTicket().setOccupy1(0);
					bom.getBomTicket().setBack1(0);
					bom.getBomTicket().setOccupy2(0);
					bom.getBomTicket().setKeepAmount(0);
				}
			}
		}
		if ("指定值生产录入".length()>0 && types.contains("2") && Commodity_CommitOccupy1Back1Occupy2Back2.length>0) {
			for (Hyperlink link: form.getMonthnumLinkList()) {
				if (link.getComponent()==null)
					continue;
				LinkedHashSet<BomDetail> bomList = new LinkedHashSet<BomDetail>();
				for (TextField tf: link.getInnerComponentList(TextField.class)) {
					if (tf.getFormer()==null)
						continue;
					BomDetail bom = (BomDetail)tf.searchFormerByClass(Field.class).getFieldBuilder().getEntityBean().getBean();
					bomList.add(bom);
				}
				for (BomDetail bom: bomList) {
					for (int i=0,isize=Commodity_CommitOccupy1Back1Occupy2Back2.length; i<isize;) {
						Commodity commodity = (Commodity)Commodity_CommitOccupy1Back1Occupy2Back2[i++];
						if (StringUtils.equals(commodity.getName(), bom.getCommodity().getName())) {
							double commit = (Double)Commodity_CommitOccupy1Back1Occupy2Back2[i++];
							double occupy1 = (Double)Commodity_CommitOccupy1Back1Occupy2Back2[i++];
							double back1 = (Double)Commodity_CommitOccupy1Back1Occupy2Back2[i++];
							double occupy2 = (Double)Commodity_CommitOccupy1Back1Occupy2Back2[i++];
							double back2 = (Double)Commodity_CommitOccupy1Back1Occupy2Back2[i++];
							if (commit>0)
								this.setEntityFieldText(bom, "commitAmount", commit);
							this.setEntityFieldText(bom, "occupy1", occupy1);
							this.setEntityFieldText(bom, "back1", back1);
							this.setEntityFieldText(bom, "occupy2", occupy2);
							this.setEntityFieldText(bom, "keepAmount", back2);
							HasRecord = true;
							break;
						} else {
							i+=5;
						}
					}
				}
			}
		}
		if ("标准的生产录入".length()>0 && types.contains("3")) {
			for (Hyperlink link: form.getMonthnumLinkList()) {
				if (link.getComponent()==null)
					continue;
				LinkedHashSet<BomDetail> bomList = new LinkedHashSet<BomDetail>();
				for (TextField tf: link.getInnerComponentList(TextField.class)) {
					if (tf.getFormer()==null)
						continue;
					BomDetail bom = (BomDetail)tf.searchFormerByClass(Field.class).getFieldBuilder().getEntityBean().getBean();
					bomList.add(bom);
				}
				for (BomDetail bom: bomList) {
					if (new SupplyTypeLogic().isProductType(bom.getCommodity().getSupplyType()) && new ArrangeTypeLogic().isNormal(bom.getArrange()))
						this.setEntityFieldText(bom, "commitAmount", bom.getAmount());
					this.setEntityFieldText(bom, "occupy1", bom.getAmount());
					HasRecord = true;
				}
				"1".toCharArray();
			}
		}
		Date timeDo = new Date();
		this.onMenu("保存录入");
		Iterator<List<BomDetail>> iter=form.getBomDetailsMap().values().iterator();
		Assert.assertTrue("有原物料明细", iter.hasNext());
		for (BomDetail b: iter.next()) {
			if (b.getModifytime().after(timeDo)) {
				BomDetail sb = (BomDetail)b.getSnapShot();
				Assert.assertTrue("物料应有领用日志", b.getStateBuffer().length()>sb.getStateBuffer().length());
			}
		}
	}
	
	public void check直发采购__1采购_2改单重新排单(char type, String number) {
		super.check直发采购__1采购_2改单重新排单(type, number);
		if ("直发生产".length()>0 && type=='1') {
			this.loadView("RecordList", "number", number, "stOrder", ">0");
			Assert.assertTrue("直发采购不能生产录入", this.getListViewValue().size()==0);
		}
	}
	
	public void check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交(char type, String number) {
		super.check当地采购__1开单_2审核通过_3审核不通过_4不通过重提交(type, number);
		if ("当地生产".length()>0 && type=='1') {
			this.loadView("RecordList", "number", number, "stOrder", ">0");
			Assert.assertTrue("当地生产不能生产录入", this.getListViewValue().size()==0);
		}
	}
	
	public void setBomDetailsShow() {
		Hyperlink link = this.getSqlListView().getComponent().getInnerComponentList(Hyperlink.class).get(0);
		Assert.assertTrue("第1待生产明细要激活显示子物料明细列表", link.getEventListenerList().getListenerCount(EventListenerType.Action)>0);
		link.getEventListenerList().fireListener();
	}
	
	private void test设置Bom() {
		String number = new ArrangeTicketTest().get客户订单_普通(11);
		this.loadSql("CommonList", "selectedList", "number", number);
		Assert.assertTrue("到生产开单列表", this.getListViewValue().size()>0);
		this.setSqlListSelect(1);
		this.onMenu("配置BOM");
		PPurchaseTicketForm form = (PPurchaseTicketForm)this.getForm();
		BOMForm bomForm = form.getBomForm();
		bomForm.getDomain().setLevel(10);
		this.onButton("调整显示级数");
		this.onMenu("新增同级");
	}
	
	private void test物料计算() {
		if ("设置Bom，物料计算，拆分，生产开单".length()>0) {
			this.setTestStart();
			String number = new ArrangeTicketTest().get客户订单_普通(15, 15);
			this.check生产开单_1设置Bom_2物料计算_3拆分('1', number);
			this.check生产开单_1设置Bom_2物料计算_3拆分('2', number);
			this.check生产开单_1设置Bom_2物料计算_3拆分('3', number);
		}
		if ("生产录入".length()>0) {
			this.setTestStart();
			String number = new PPurchaseTicketTest().get客户订单_普通(2);
			if ("领料数+5+6，还料数-1,-2，剩余领料数8".length()>0) {
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("12", number, new Object[0], new Object[]{
						new CommodityTest().getS黑棒(),2d,2d,0d,0d,0d,
						new CommodityTest().getC浆料(),0d,1.68d,0d,0d,0d,
						new CommodityTest().getC白棒(),0d,5d,0d,0d,0d,
						new CommodityTest().getC铁帽(),0d,4d,0d,0d,0d });
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,6d,0d,0d,0d});
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,1d,0d,0d});
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,2d,0d,0d});
				BomDetail bom = this.getBomDetails("number", number, "sn", 3).get(0);
				Assert.assertTrue("白棒领料数8", bom.getBomTicket().getOccupy1()==8 && bom.getBomTicket().getOccupy2()==0 && bom.getBomTicket().getOccupyAmount()==8);
			}
			if ("车间留用库存，领料数+1+2，留用领料数3".length()>0) {
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,0d,1d,0d});
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,0d,2d,0d});
				BomDetail bom = this.getBomDetails("number", number, "sn", 3).get(0);
				Assert.assertTrue("白棒留用领料数3", bom.getBomTicket().getOccupy2()==3 && bom.getBomTicket().getOccupy1()==8 && bom.getBomTicket().getOccupyAmount()==11);
			}
			if ("车间留用库存，留用数+2+3，减领用3，剩余留用2".length()>0) {
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,0d,0d,2d});
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,0d,0d,3d});
				BomDetail bom = this.getBomDetails("number", number, "sn", 3).get(0);
				Assert.assertTrue("白棒留用领料数3", bom.getBomTicket().getKeepAmount()==2 && bom.getBomTicket().getOccupy2()==0 && bom.getBomTicket().getOccupy1()==8 && bom.getBomTicket().getOccupyAmount()==8);
				this.loadFormView(new StoreTicketForm(), "EnoughList", "车间原物料台账库存", "number", number);
				Assert.assertTrue("未生产收货的留用不能算入台账", this.getListViewValue().size()==0);
			}
		}
	}
	
	private void temp() {
		if ("生产录入".length()>0) {
			this.setTestStart();
			String number = new PPurchaseTicketTest().get客户订单_普通(2);
			if ("领料数+5+6，还料数-1,-2，剩余领料数8".length()>0) {
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("12", number, new Object[0], new Object[]{
						new CommodityTest().getS黑棒(),2d,2d,0d,0d,0d,
						new CommodityTest().getC浆料(),0d,1.68d,0d,0d,0d,
						new CommodityTest().getC白棒(),0d,5d,0d,0d,0d,
						new CommodityTest().getC铁帽(),0d,4d,0d,0d,0d });
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,6d,0d,0d,0d});
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,1d,0d,0d});
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,2d,0d,0d});
				BomDetail bom = this.getBomDetails("number", number, "sn", 3).get(0);
				Assert.assertTrue("白棒领料数8", bom.getBomTicket().getOccupy1()==8 && bom.getBomTicket().getOccupy2()==0 && bom.getBomTicket().getOccupyAmount()==8);
			}
			if ("车间留用库存，领料数+1+2，留用领料数3".length()>0) {
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,0d,1d,0d});
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,0d,2d,0d});
				BomDetail bom = this.getBomDetails("number", number, "sn", 3).get(0);
				Assert.assertTrue("白棒留用领料数3", bom.getBomTicket().getOccupy2()==3 && bom.getBomTicket().getOccupy1()==8 && bom.getBomTicket().getOccupyAmount()==11);
			}
			if ("车间留用库存，留用数+2+3，减领用3，剩余留用2".length()>0) {
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,0d,0d,2d});
				new PPurchaseTicketTest().check生产录入__1清空_2指定录入_3标准录入("2", number, new Object[0], new Object[]{
						new CommodityTest().getC白棒(),0d,0d,0d,0d,3d});
				BomDetail bom = this.getBomDetails("number", number, "sn", 3).get(0);
				Assert.assertTrue("白棒留用领料数3", bom.getBomTicket().getKeepAmount()==2 && bom.getBomTicket().getOccupy2()==0 && bom.getBomTicket().getOccupy1()==8 && bom.getBomTicket().getOccupyAmount()==8);
				this.loadFormView(new StoreTicketForm(), "EnoughList", "车间原物料台账库存", "number", number);
				Assert.assertTrue("未生产收货的留用不能算入台账", this.getListViewValue().size()==0);
			}
			if ("生产收货，有车间留用台账2".length()>0) {
				new PReceiptTicketTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
				this.loadFormView(new StoreTicketForm(), "EnoughList", "车间原物料台账库存", "number", number, "instore", "notnull");
				Assert.assertTrue("生产收货的留用算入台账", this.getListViewValue().size()==1);
			}
		}
	}
	
	public List<BomDetail> getBomDetails(Object... filters0) {
		this.loadView("BomDetailList", this.genFiltersStart(filters0, "selectFormer4Bom.selectedList"));
		this.onMenu("全选");
		return this.getForm().getSelectFormer4Bom().getSelectedList();
	}
	
	public void setQ清空() {
		String sql = "delete from sa_BomDetail where sellerId=?";
		SSaleUtil.executeSqlUpdate(sql);
	}
}
