package com.haoyong.sales.test.sale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.common.NoteAccessorFormer;
import net.sf.mily.support.tools.TicketPropertyUtil;
import net.sf.mily.types.DateTimeType;
import net.sf.mily.types.DoubleType;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.CommodityT;
import com.haoyong.sales.base.domain.SubCompany;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.logic.BillTypeLogic;
import com.haoyong.sales.base.logic.DeliverTypeLogic;
import com.haoyong.sales.base.logic.SupplierLogic;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.PurchaseT;
import com.haoyong.sales.sale.form.BillTicketForm;
import com.haoyong.sales.sale.form.ReceiptTicketForm;
import com.haoyong.sales.sale.form.SaleQueryForm;
import com.haoyong.sales.sale.form.SendTicketForm;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.sale.form.WBillTicketForm;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.OrderTypeLogic;
import com.haoyong.sales.test.base.AbstractTest;

public class ReceiptTicketTest extends AbstractTest<ReceiptTicketForm> implements TRemind {
	
	public ReceiptTicketTest() {
		this.setForm(new ReceiptTicketForm());
	}
	
	public void check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2(String types, String number, Object[]... changes_filters) {
		ReceiptTicketForm form = this.getForm();
		if ("1改单申请，改供应人、改商品型号".length()>0) {
			Object[] filters0 = new Object[0];
			if (changes_filters.length > 1)
				filters0 = changes_filters[1];
			this.loadView("WaitList", this.genFiltersStart(filters0, "number", number));
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("收货开单");
			this.setEditAllSelect(detailCount);
			boolean change = false;
			StringBuffer changeBuffer = new StringBuffer();
			if (changes_filters.length > 0) {
				Object[] changes = changes_filters[0];
				for (int ci=0; ci<changes.length; ci+=2) {
					String k=(String)changes[ci], v=(String)changes[ci+1];
					for (OrderDetail detail: form.getDetailList()) {
						form.getNoteFormer4Purchase().getVoNoteMap(detail).put(k, v);
					}
				}
			}
			if (types.contains("1") && "改供应人".length()>0) {
				StringBuffer fname = new StringBuffer("supplier.linkerCall");
				form.getNoteFormer4Purchase().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Purchase().getVoNoteMap(form.getDomain()).put(fname.toString(), "0512-6444444-5");
				changeBuffer.append("改供应商联系人，");
				change = true;
			}
			if (types.contains("2") && "改采购价格".length()>0) {
				StringBuffer fname = new StringBuffer("price");
				form.getNoteFormer4Purchase().getNoteString(form.getDetailList().get(0), fname);
				for (OrderDetail detail: form.getDetailList()) {
					form.getNoteFormer4Purchase().getVoNoteMap(detail).put(fname.toString(), detail.getPrice()+10+"");
				}
				changeBuffer.append("改采购价格，");
				change = true;
			}
			if (types.contains("3") && "改商品型号".length()>0) {
				StringBuffer fname = new StringBuffer("commodity.model");
				form.getNoteFormer4Purchase().getNoteString(form.getDetailList().get(0), fname);
				for (OrderDetail detail: form.getDetailList()) {
					form.getNoteFormer4Purchase().getVoNoteMap(detail).put(fname.toString(), "2.0*8");
				}
				changeBuffer.append("改商品型号，");
				change = true;
			}
			if (types.contains("4") && "多收货10个".length()>0) {
				for (OrderDetail detail: form.getDetailList()) {
					detail.getReceiptTicket().setReceiptAmount(detail.getAmount()+10);
				}
				changeBuffer.append("多收货10个，");
			} else {
				for (OrderDetail detail: form.getDetailList()) {
					detail.getReceiptTicket().setReceiptAmount(detail.getAmount());
				}
			}
			if (types.contains("5") && "有次品2个".length()>0) {
				for (OrderDetail detail: form.getDetailList()) {
					detail.getReceiptTicket().setReceiptAmount(detail.getReceiptTicket().getReceiptAmount()+2);
					detail.getReceiptTicket().setBadAmount(2);
				}
				changeBuffer.append("收货数其中有次品数2个，");
			}
			form.getDomain().setChangeRemark(changeBuffer.toString());
			try {
				this.onMenu("提交全数收货");
				Assert.fail("有改单不能全数收货");
			}catch(Exception e) {
			}
			String timeDo = this.getTimeDo();
			this.onMenu("提交改单申请");
			if (true) {
				for (OrderDetail detail: form.getDetailList()) {
					if (change)
						Assert.assertTrue("每个改单申请明细都要改单内容", detail.getNotes()!=null);
					Assert.assertTrue("每个改单申请明细都要有申请原因", detail.getChangeRemark()!=null);
				}
				this.loadView("AuditList", "number", number, "modifytime", timeDo);
				Assert.assertTrue("有改单应不能立即收货确认", this.getListViewValue().size()==0);
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "RechangeList", "number", number);
				Assert.assertTrue("有改单应到采购返改单处理", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check收货开单__1全数收货_2部分收货n拆单(char type, String number, Object... receiptN_filters0) {
		ReceiptTicketForm form = this.getForm();
		if ("1全数收货".length()>0 && type=='1') {
			this.loadView("WaitList", number==null? receiptN_filters0: this.genFiltersStart(receiptN_filters0, "number", number));
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("收货开单");
			this.setEditAllSelect(detailCount);
			for (OrderDetail detail: form.getDetailList()) {
				this.setRowFieldText(detail, "receiptAmount", detail.getAmount());
			}
			new StoreTicketTest().setQ清空();
			this.setBomConfirm();
			this.onMenu("提交全数收货");
			Assert.assertTrue("提交全数收货失败", this.hasMenu("提交全数收货")==false);
			OrderDetail pur=form.getDetailList().get(form.getDetailList().size()-1), spur=pur.getSnapShot();
			List<BomDetail> boms=pur.getVoparam("BomDetailReceipt");
			if (true) {
//				Assert.assertTrue("一个原物料加工收货", pur.getMonthnum().contains(".")? boms.size()==1: boms.size()==0);
				if ("原物料加工收货".length()>0 && boms.size()==1) {
					BomDetail bom=boms.get(0), sbom=bom.getSnapShot();
					Assert.assertTrue("物料采购收货，Bom有占用", bom.getBomTicket().getGiveAmount()==pur.getAmount()+sbom.getBomTicket().getGiveAmount());
					if (new SupplyTypeLogic().isProductType(bom.getCommodity().getSupplyType()))
						Assert.assertTrue("生产数量累加", bom.getBomTicket().getCommitAmount()==pur.getAmount()+sbom.getBomTicket().getCommitAmount());
					else
						Assert.assertTrue("无生产数量", bom.getBomTicket().getCommitAmount()==0);
				}
				if (new OrderTypeLogic().isBackType(spur.getOrderTicket().getOrderType()))
					Assert.assertTrue("备货订单失效，不用发货", pur.getStOrder()==0 && pur.getSendId()==0);
				else if (new DeliverTypeLogic().isCommonType(spur.getArrangeTicket().getArrangeType()))
					if (spur.getStOrder()>0)
						Assert.assertTrue("普通客户订单有效，可发货", pur.getStOrder()>=30 && pur.getSendId()==20);
				else if (spur.getStOrder()>0)
					Assert.assertTrue("直发当地购客户订单有效，发货完成", pur.getStOrder()>=30 && pur.getSendId()==30);
				this.loadView("AuditList", "monthnum", pur.getMonthnum());
				Assert.assertTrue("全数收货应不用收货确认", this.getListViewValue().size()==0);
				this.loadView("ShowQuery", "monthnum", pur.getMonthnum());
				Assert.assertTrue("全数收货应可收货查询", this.getListViewValue().size()>0);
				this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
				if (new DeliverTypeLogic().isCommonType(spur.getArrangeTicket().getArrangeType()) && spur.getStPurchase()==30) {
					Assert.assertTrue("普通收货应添加库存明细", this.getListViewValue().size()>0);
					this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
					Assert.assertTrue("普通收货应添加库存够用明细库存数、减在途数", this.getListViewValue().size()>0);
				} else if (new DeliverTypeLogic().isCommonType(spur.getArrangeTicket().getArrangeType())==false){
					Assert.assertTrue("非普通收货应不添加库存明细", this.getListViewValue().size()==0);
					this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
					Assert.assertTrue("非普通收货不应添加库存够用明细库存数、减在途数", this.getListViewValue().size()==0);
				}
				this.loadFormView(new SaleQueryForm(), "BillQuery", "typeName", new BillTypeLogic().getPurchaseType(), "monthnum", spur.getMonthnum(), "stBill", "!=0");
				Assert.assertTrue("收货应添加应付款明细", this.getListViewValue().size()==1);
			}
			if (spur.getStOrder()>0) {
				this.loadFormView(new SendTicketForm(), "SendList", "monthnum", spur.getMonthnum());
				if ("待迁移到账收货".length()>0 && spur.getStPurchase()==32)
					"".toCharArray();
				else if (new DeliverTypeLogic().isCommonType(spur.getArrangeTicket().getArrangeType()) && spur.getOrderTicket().getOrderType().equals("备货订单")==false)
					Assert.assertTrue("收货后订单应可发货", this.getListViewValue().size()==1);
				else
					Assert.assertTrue("非普通订单收货后不可发货", this.getListViewValue().size()==0);
				if (spur.getArrangeTicket().getArrangeType().equals("普通")==false) {
					this.loadFormView(new SaleQueryForm(), "BillQuery", "typeName", new BillTypeLogic().getSaleType(), "monthnum", spur.getMonthnum());
					Assert.assertTrue("收货合并发货添加应收款明细", this.getListViewValue().size()==1);
				}
				if (spur.getOrderTicket().getOrderType().equals("备货订单")==true)
					Assert.assertTrue("备货订单收货完成时结束", pur.getStOrder()==0);
				if (new DeliverTypeLogic().isCommonType(spur.getArrangeTicket().getArrangeType()))
					Assert.assertTrue("普通收货采购继续走", pur.getStPurchase()==(spur.getStPurchase()==32? 32: 30));
				else
					Assert.assertTrue("直发当地购收货采购走完", pur.getStPurchase()==0);
			}
			new StoreTicketTest().checkInStoreList(form.getBomInstoreForm());
			new StoreTicketTest().checkOutStoreList(form.getBomOutstoreForm());
		}
		if ("2部分收货".length()>0 && type=='2') {
			int receiptAmount = (Integer)receiptN_filters0[0];
			receiptN_filters0 = ArrayUtils.subarray(receiptN_filters0,1,receiptN_filters0.length);
			this.loadView("WaitList", number==null? receiptN_filters0: this.genFiltersStart(receiptN_filters0, "number", number));
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("收货开单");
			this.setEditAllSelect(detailCount);
			for (OrderDetail detail: form.getDetailList()) {
				this.setRowFieldText(detail, "receiptAmount", receiptAmount);
			}
			new StoreTicketTest().setQ清空();
			this.onMenu("部分收货拆单");
			Assert.assertTrue("部分收货拆单失败，应还可此拆出部分全数收货", this.hasMenu("部分收货拆单"));
			OrderDetail curSubtract=form.getDetailList().get(form.getDetailList().size()-1), nwRemain=curSubtract.getVoparam("NewRemainPurchase"), source=curSubtract.getSnapShot().getSnapShot();
			Assert.assertTrue("订单数减小", source.getAmount()==curSubtract.getAmount()+nwRemain.getAmount());
		}
	}
	
	// 返回采购名称
	public String check收货改单确认__1减少收货_2收货确认(String types, String number) {
		ReceiptTicketForm form = this.getForm();
		this.loadView("AuditList", "number", number);
		int detailCount = this.getListViewValue().size();
		Assert.assertTrue("未到收货改单确认", detailCount>0);
		if ("没有同月流水号在待收".length()>0) {
			this.loadView("WaitList", "monthnum", this.getListViewColumn("monthnum").toArray(new String[0]));
			Assert.assertTrue("收货确认采购单不在待收", this.getListViewValue().size()==0);
			this.loadView("AuditList", "number", number);
		}
		this.setSqlAllSelect(detailCount);
		this.onMenu("改单处理确认");
		if (types.contains(String.valueOf('1')) && "1减少收货数量,退货给供应商".length()>0) {
			this.setEditAllSelect(detailCount);
			if ("不能收货确认".length()>0) {
				try {
					this.onMenu("收货确认");
					Assert.fail("有减少收货数，应不能直接收货确认");
				}catch(Exception e) {
				}
			}
			this.onMenu("减少收货数");
			OrderDetail psubtract=form.getDetailList().get(form.getDetailList().size()-1), psource=psubtract.getSnapShot();
			OrderDetail premain=psubtract.getVoparam("NewRemainPurchase"), preject=null;
			if (psubtract.getReceiptTicket().getReceiptAmount()==0) {
				preject = psubtract;
				Assert.assertTrue("0收货应返回外界面", this.hasMenu("减少收货数")==false);
				this.setFilters("monthnum", psubtract.getMonthnum());
				Assert.assertTrue("无收货无收货确认", this.getListViewValue().size()==0);
				this.loadView("WaitList", "monthnum", psubtract.getMonthnum());
				Assert.assertTrue("无收货可重新收货", this.getListViewValue().size()==1);
				Assert.assertTrue("收货数量0", psubtract.getReceiptTicket().getReceiptAmount()==0);
			} else if (psubtract.getReceiptTicket().getReceiptAmount() < psource.getAmount()){
				preject = premain;
				Assert.assertTrue("减少收货应保持在收货确认界面", this.hasMenu("减少收货数"));
				Assert.assertTrue("收货数量减少", psubtract.getReceiptTicket().getReceiptAmount()<psource.getReceiptTicket().getReceiptAmount());
				Assert.assertTrue("每个改单申请明细都要改单内容改变", StringUtils.equals(psubtract.getNotes(), psource.getNotes())==false);
				this.loadView("WaitList", "monthnum", premain.getMonthnum());
				Assert.assertTrue("剩余数量可继续收货", this.getListViewValue().size()==1);
			} else if (psubtract.getReceiptTicket().getReceiptAmount() > psource.getAmount()) {
				Assert.assertTrue("多收货无剩余待收", premain==null);
			}
			if ("链接供应商家订单收到拒收退货入库申请".length()>0 && psubtract.getSupplier().getToSellerId()>0 && psubtract.getSupplier().getToSellerId()!=psubtract.getSellerId()) {
				OrderDetail receiptReject = psubtract.getVoparam("RejectPurchase");
				List<String> monthnums = receiptReject.getVoparam("WaitInstoreMonthnums");
				List<OrderDetail> upOrders = new OrderTicketTest().getOrderList(preject.getSupplier().getToSellerId(), "monthnum", monthnums.toArray(new String[0]));
				List<OrderDetail> upPurchases = this.getModeList().getSelfPurchaseTest().getPurchaseList(preject.getSupplier().getToSellerId(), "monthnum", monthnums.toArray(new String[0]));
				Assert.assertTrue("订单待拒收入库", upOrders.size()==monthnums.size() && upPurchases.size()==monthnums.size());
				OrderDetail upPur = upPurchases.get(upPurchases.size()-1);
				Assert.assertTrue("客户订单退货待收", upPur.getStPurchase()==78 && upPur.getReceiptId()==30);
			}
		}
		if (types.contains(String.valueOf('2')) && "2收货确认".length()>0) {
			this.loadView("AuditList", "number", number);
			List<String> monthnumList = (List)this.getListViewColumn("monthnum");
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单处理确认");
			this.setEditAllSelect(detailCount);
			if ("不能减少收货数".length()>0) {
				try {
					this.onMenu("减少收货数");
					Assert.fail("无减少收货数，应不能减少确认");
				}catch(Exception e) {
				}
			}
			new StoreTicketTest().setQ清空();
			this.setBomConfirm();
			String timeDo = this.getTimeDo();
			this.onMenu("收货确认");
			Iterator<OrderDetail> iter=form.getSelectFormer4Purchase().getSelectedList().iterator();
			for (boolean bill=false; iter.hasNext();) {
				OrderDetail pur=iter.next(), spur=pur.getSnapShot();
				OrderDetail purOrder = pur;
				try {
					spur.getVoParamMap().remove("notemap");
					new NoteAccessorFormer(OrderDetail.class).setEntityChanges(spur, "ReceiptTicket.Handle");
				} catch(Exception e) {
				}
				BomDetail bom = pur.getVoparam(BomDetail.class);
				if (bom!=null)
					Assert.assertTrue("物料采购收货，Bom有占用", bom.getBomTicket().getGiveAmount()>0);
				Assert.assertTrue("每个改单申请明细都要改单内容为空", StringUtils.equals(pur.getNotes(), null));
				Assert.assertTrue("每个改单申请明细都要改单备注为空", StringUtils.equals(pur.getChangeRemark(), null));
				if (pur.getAmount()==0) {
					Assert.assertTrue("原采购数量为0状态应为0", pur.getStPurchase()==0);
					if (purOrder!=null)
						Assert.assertTrue("原采购数量为0状态应为0", purOrder.getStOrder()==0);
				} else {
					bill = true;
					if (new OrderTypeLogic().isBackType(spur.getOrderTicket().getOrderType()))
						Assert.assertTrue("备货订单失效，不用发货", pur.getStOrder()==0 && pur.getSendId()==0);
					else if (new DeliverTypeLogic().isCommonType(spur.getArrangeTicket().getArrangeType()))
						Assert.assertTrue("普通客户订单有效，可发货", pur.getStOrder()>=30 && pur.getSendId()==20);
					else
						Assert.assertTrue("直发当地购客户订单有效，发货完成", pur.getStOrder()>=30 && pur.getSendId()==30);
				}
				if (spur.getReceiptTicket().getBadAmount()>0) {
					OrderDetail nd = (OrderDetail)pur.getVoParamMap().get("NewBad");
					Assert.assertTrue("次品数，备货订单，分支月流水号", pur.getReceiptTicket().getBadAmount()==0 && nd.getId()>0
							&& nd.getStOrder()==0 && nd.getSendId()==0
							&& nd.getMonthnum().startsWith(pur.getMonthnum().concat("-")));
					bill = true;
				}
				if (spur.getPurchaseTicket().getBackupAmount()-spur.getReceiptTicket().getBadAmount()>0) {
					OrderDetail nd = (OrderDetail)pur.getVoParamMap().get("NewBackup");
					Assert.assertTrue("备货数，备货订单，分支月流水号", pur.getPurchaseTicket().getBackupAmount()==0 && nd.getId()>0
							&& nd.getMonthnum().startsWith(pur.getMonthnum().concat("-")));
					bill = true;
				}
				if (spur.getPurchaseTicket().getCancelAmount()>0) {
					OrderDetail nd = (OrderDetail)pur.getVoParamMap().get("NewCancel");
					Assert.assertTrue("取消采购数，备货订单，退货申请，分支月流水号", pur.getPurchaseTicket().getCancelAmount()==0 && nd.getId()>0
							&& nd.getStOrder()==0 && nd.getSendId()==0
							&& nd.getTReturn().getReturnName()!=null
							&& nd.getMonthnum().startsWith(pur.getMonthnum().concat("-")));
					bill = true;
				}
				if (spur.getPurchaseTicket().getRearrangeAmount()>0) {
					OrderDetail nd = (OrderDetail)pur.getVoParamMap().get("NewRearrange");
					Assert.assertTrue("重排单数，客户订单，新月流水号", pur.getPurchaseTicket().getRearrangeAmount()==0 && nd.getId()>0
							&& nd.getStOrder()>0 && nd.getStPurchase()==0 && nd.getReceiptId()==0 && nd.getSendId()==0
							&& StringUtils.equals(nd.getMonthnum(), pur.getMonthnum())==false);
				}
				if (spur.getPurchaseTicket().getOverAmount()>0) {
					OrderDetail nd = (OrderDetail)pur.getVoParamMap().get("NewOver");
					Assert.assertTrue("多收货数，备货订单，分支月流水号", pur.getPurchaseTicket().getOverAmount()==0 && nd.getId()>0
							&& nd.getStOrder()==0 && nd.getSendId()==0
							&& nd.getMonthnum().startsWith(pur.getMonthnum().concat("-")));
					bill = true;
				}
				if (iter.hasNext()==false) {
					this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
					if (purOrder==null) {
					} else if (spur.getAmount()-spur.getPurchaseTicket().getCancelAmount()==0) {
						this.loadSqlView(new StoreTicketForm(), "RestoreList", "selectFormer4Purchase.selectedList", "number", spur.getOrderTicket().getNumber());
						Assert.assertTrue("无新库存", this.getListViewValue().size()==0);
						this.loadSqlView(new StoreTicketForm(), "EnoughList", "selectCross4Store.selectedList", "number", spur.getOrderTicket().getNumber());
						Assert.assertTrue("无新库存", this.getListViewValue().size()==0);
					} else if (new DeliverTypeLogic().isCommonType(purOrder.getArrangeTicket().getArrangeType())) {
						Assert.assertTrue("普通收货应添加库存明细", this.getListViewValue().size()>0);
						this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
						Assert.assertTrue("普通收货应添加库存够用明细库存数、减在途数", this.getListViewValue().size()>0);
					} else {
						Assert.assertTrue("非普通收货不应添加库存明细", this.getListViewValue().size()==0);
						this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
						Assert.assertTrue("非普通收货不应添加库存够用明细库存数、减在途数", this.getListViewValue().size()==0);
					}
					if (bill==true) {
						this.loadFormView(new BillTicketForm(), "ShowQuery", "number", number, "typeName", new BillTypeLogic().getPurchaseType(), "modifytime", timeDo);
						Assert.assertTrue("收货应添加应付款明细", this.getListViewValue().size()>=detailCount);
						if (purOrder!=null && purOrder.getArrangeTicket().getArrangeType().equals("普通")==false) {
							this.loadFormView(new BillTicketForm(), "ShowQuery", "number", number, "typeName", new BillTypeLogic().getSaleType(), "modifytime", timeDo);
							Assert.assertTrue("收货合并发货添加应收款明细", this.getListViewValue().size()==detailCount);
						}
					}
				}
			}
			double amount1 = form.getDetailList().get(0).getAmount();
			if (amount1>0) {
				OrderDetail firstPur = form.getDetailList().get(0);
				OrderDetail firstPurOrder=form.getDetailList().get(0), sfirstPurOrder=firstPurOrder.getSnapShot();
				if (sfirstPurOrder.getStOrder()>0) {
					this.loadFormView(new SendTicketForm(), "SendList", "number", number, "modifytime", timeDo);
					if (new DeliverTypeLogic().isCommonType(firstPurOrder.getArrangeTicket().getArrangeType()) && new OrderTypeLogic().isClientType(firstPurOrder.getOrderTicket().getOrderType()))
						Assert.assertTrue("收货后订单应可发货", this.getListViewValue().size()>0);
					else
						Assert.assertTrue("非普通订单收货后不可发货", this.getListViewValue().size()==0);
					if (new DeliverTypeLogic().isCommonType(firstPurOrder.getArrangeTicket().getArrangeType()))
						Assert.assertTrue("普通收货采购继续走", firstPur.getStPurchase()==30);
					else
						Assert.assertTrue("直发当地购收货采购走完", firstPur.getStPurchase()==0);
				}
			} else {
				OrderDetail firstPur=form.getDetailList().get(0), sfirstPur=firstPur.getSnapShot();
				if (sfirstPur.getStOrder()>0) {
					this.loadFormView(new SendTicketForm(), "SendList", "number", number, "modifytime", timeDo);
					Assert.assertTrue("0收货后订单应无可发货", this.getListViewValue().size()==0);
				}
			}
			new StoreTicketTest().checkInStoreList(form.getBomInstoreForm());
			new StoreTicketTest().checkOutStoreList(form.getBomOutstoreForm());
		}
		return form.getSelectFormer4Purchase().getFirst().getVoparam(PurchaseT.class).getPurName();
	}
	
	public void check收货确认再改单__1供应人_2采购价格_3商品_4多收货10_5有次品2(String number, String types) {
		ReceiptTicketForm form = this.getForm();
		if ("1改单申请，改供应人、改商品型号".length()>0) {
			this.loadView("AuditList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("改单处理确认");
			this.setEditAllSelect(detailCount);
			boolean change = false;
			StringBuffer changeBuffer = new StringBuffer();
			if (types.contains("1") && "改供应人".length()>0) {
				StringBuffer fname = new StringBuffer("supplier.linkerCall");
				form.getNoteFormer4Purchase().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Purchase().getVoNoteMap(form.getDomain()).put(fname.toString(), "0512-6444444-5");
				changeBuffer.append("改供应商联系人，");
				change = true;
			}
			if (types.contains("2") && "改采购价格".length()>0) {
				StringBuffer fname = new StringBuffer("price");
				form.getNoteFormer4Purchase().getNoteString(form.getDetailList().get(0), fname);
				for (OrderDetail detail: form.getDetailList()) {
					form.getNoteFormer4Purchase().getVoNoteMap(detail).put(fname.toString(), detail.getPrice()+10+"");
				}
				changeBuffer.append("改采购价格，");
				change = true;
			}
			if (types.contains("3") && "改商品型号".length()>0) {
				StringBuffer fname = new StringBuffer("commodity.model");
				form.getNoteFormer4Purchase().getNoteString(form.getDetailList().get(0), fname);
				for (OrderDetail detail: form.getDetailList()) {
					form.getNoteFormer4Purchase().getVoNoteMap(detail).put(fname.toString(), "2.0*8");
				}
				changeBuffer.append("改商品型号，");
				change = true;
			}
			if (types.contains("4") && "多收货10个".length()>0) {
				for (OrderDetail detail: form.getDetailList()) {
					detail.getReceiptTicket().setReceiptAmount(detail.getAmount()+10);
				}
				changeBuffer.append("多收货10个，");
			} else {
				for (OrderDetail detail: form.getDetailList()) {
					detail.getReceiptTicket().setReceiptAmount(detail.getAmount());
				}
			}
			if (types.contains("5") && "有次品2个".length()>0) {
				for (OrderDetail detail: form.getDetailList()) {
					detail.getReceiptTicket().setBadAmount(2);
				}
				changeBuffer.append("收货数其中有次品数2个，");
			}
			form.getDomain().setChangeRemark(changeBuffer.toString());
			try {
				this.onMenu("提交全数收货");
				Assert.fail("有改单不能全数收货");
			}catch(Exception e) {
			}
			this.onMenu("提交改单申请");
			if (true) {
				for (OrderDetail detail: form.getDetailList()) {
					Assert.assertTrue("每个改单申请明细都要改单内容", detail.getNotes()!=null);
					Assert.assertTrue("每个改单申请明细都要有申请原因", detail.getChangeRemark()!=null);
				}
				this.loadView("AuditList", "number", number);
				Assert.assertTrue("再次改单应不能在收货确认", this.getListViewValue().size()==0);
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "RechangeList",
						"number", number);
				Assert.assertTrue("有改单应到采购返改单处理", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check红冲处理__1通过_2不通过(char type, String number) {
		ReceiptTicketForm form = this.getForm();
		this.loadView("AdjustList", "number", number);
		int detailCount = this.getListViewValue().size();
		this.setSqlAllSelect(detailCount);
		this.onMenu("红冲处理");
		this.setEditAllSelect(detailCount);
		if ("通过采购开的红冲".length()>0 && type=='1') {
			form.getDomain().setChangeRemark("收货处理为红冲通过");
			new StoreTicketTest().setQ清空();
			this.onMenu("红冲通过");
			Assert.assertTrue("红冲通过失败", this.hasMenu("红冲通过")==false);
			for (OrderDetail detail: form.getDetailList()) {
				OrderDetail sd = detail.getSnapShot();
				Assert.assertTrue("通过后红冲备注为空", detail.getNotes()==null && detail.getChangeRemark()==null && detail.getStPurchase()==30);
				BillDetail bill=detail.getVoparam(BillDetail.class), sbill=bill.getSnapShot();
				DateTimeType dtype = new DateTimeType();
				if (detail.getPrice() != sd.getPrice())
					Assert.assertTrue("有应付差额", bill.getId()>0 && StringUtils.equals(dtype.format(bill.getBillTicket().getBillDate()), dtype.format(sbill.getBillTicket().getBillDate())));
			}
			if (true) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "DoadjustList",
						"number", number);
				Assert.assertTrue("可再次开红冲", this.getListViewValue().size()==detailCount);
				this.loadFormView(new SaleQueryForm(), "StoreItemQuery");
				Assert.assertTrue("重计算库存单价", this.getListViewValue().size()>0);
				this.loadFormView(new SaleQueryForm(), "StoreEnoughQuery");
				Assert.assertTrue("重计算库存单价", this.getListViewValue().size()>0);
			}
		}
		if ("不通过采购开的红冲".length()>0 && type=='2') {
			form.getDomain().setChangeRemark("收货人不通过红冲");
			this.onMenu("不通过驳回红冲");
			Assert.assertTrue("红冲通过失败", this.hasMenu("红冲通过")==false);
			for (OrderDetail detail: form.getDetailList()) {
				OrderDetail sd = detail.getSnapShot();
				Assert.assertTrue("红冲备注改变", detail.getStPurchase()==52 && StringUtils.equals(detail.getChangeRemark(), sd.getChangeRemark())==false);
			}
			if (true) {
				this.loadFormView(this.getModeList().getSelfPurchaseForm(), "DoadjustList",
						"number", number);
				Assert.assertTrue("需不通过确认", this.getListViewValue().size()==detailCount);
			}
		}
	}
	
	public void check撤销收货(String number) {
		ReceiptTicketForm form = this.getForm();
		if ("明细全部数量撤销".length()>0) {
			this.loadView("RollbackList", "number", number);
			int detailCount = this.getListViewValue().size();
			List<String> monthnumList = (List)this.getListViewColumn("monthnum");
			this.setSqlAllSelect(detailCount);
			this.onMenu("撤销");
			new StoreTicketTest().setQ清空();
			String timeDo = this.getTimeDo();
			this.onMenu("撤销收货");
			for (OrderDetail pur: form.getDetailList()) {
				OrderDetail order=pur, sorder=order.getSnapShot();
				if (sorder.getStOrder()>0) {
					Assert.assertTrue("撤销收货订单转为在途", order.getSendId()==10);
					if (StringUtils.equals("备货订单", sorder.getOrderTicket().getOrderType()))
						Assert.assertTrue("撤销收货备货订单还原为生效", order.getStOrder()==30);
				}
				Assert.assertTrue("采购明细为未收货状态", pur.getReceiptId()==0);
				this.loadFormView(new StoreTicketForm(), "StoreEnoughQuery",
						"commName", pur.getVoparam(CommodityT.class).getCommName());
				if (order!=null && new DeliverTypeLogic().isCommonType(order.getArrangeTicket().getArrangeType()))
					Assert.assertTrue("撤销收货订单原物料有够用数", this.getListViewValue().size()>0);
				else
					Assert.assertTrue("撤销收货订单原物料有够用数", this.getListViewValue().size()==0);
			}
			for (Iterator<BillDetail> iter=form.getSelectFormer4Bill().getSelectedList().iterator(); iter.hasNext();) {
				BillDetail bill=iter.next(), sbill=bill.getSnapShot();
				DateTimeType dtype = new DateTimeType();
				Assert.assertTrue("撤销收货应付款明细失效，直发当地购的应收款明细失效", bill.getStBill()==0 && StringUtils.equals(dtype.format(bill.getBillTicket().getBillDate()), dtype.format(sbill.getBillTicket().getBillDate())));
				if (iter.hasNext()==false) {
					this.loadFormView(new WBillTicketForm(), "SupplierQuery",
							"monthnum", monthnumList.toArray(new String[0]));
					Assert.assertTrue("撤销收货应付款明细失效", this.getListViewValue().size()==0);
					this.loadFormView(new WBillTicketForm(), "ClientQuery",
							"monthnum", monthnumList.toArray(new String[0]));
					Assert.assertTrue("撤销收货直发当地购的应收款明细失效", this.getListViewValue().size()==0);
				}
			}
			for (BomDetail bom: form.getSelectFormer4Bom().getSelectedList()) {
				BomDetail sbom = bom.getSnapShot();
				OrderDetail order = bom.getVoparam("InstorePurchase");//用来入库员物料
				Assert.assertTrue("撤销收货订单原物料还原为占用库存", bom.getStBom()==10);
				if (sbom.getBomTicket().getOccupyAmount()>0) {
					this.loadFormView(new StoreTicketForm(), "RestoreList", "modifytime", timeDo, "commName", bom.getVoparam(CommodityT.class).getCommName());
					Assert.assertTrue("撤销收货订单原物料有库存", this.getListViewValue().size()>0);
					this.loadFormView(new StoreTicketForm(), "EnoughList", "selectCross4Store.selectedList", "modifytime", timeDo, "commName", bom.getVoparam(CommodityT.class).getCommName());
					Assert.assertTrue("撤销收货订单原物料有够用数", this.getListViewValue().size()>0);
				}
			}
		}
	}
	
	public void linkClient待收货__1生成_2失效验证_3失效(char type, List<OrderDetail> sendList) {
		if ("供应商家配货发货，客户商家采购生成".length()>0 && type=='1') {
			Supplier supplier = new SupplierLogic().getSupplierByLink(sendList.get(0).getSubCompany().getSubmitNumber());
			List<OrderDetail> linkList = new ArrayList<OrderDetail>();
			for (OrderDetail send: sendList) {
				OrderDetail pur = TicketPropertyUtil.copyFieldsSkip(send, new OrderDetail());
				send.getClient().setFromSellerId(send.getSubCompany().getFromSellerId());
				send.getVoParamMap().put("DownPurchase", pur);
				pur.setSupplier(supplier);
				linkList.add(pur);
				if ("订单".length()>0 && send.getClient().getName()!=null) {
					pur.setSubCompany(new SubCompany());
				}
			}
			this.loadView("WaitList", "monthnum","0");
			this.getForm().getSelectFormer4Purchase().setSelectedList(linkList);
			this.onMenu("采购订单生成");
		}
		if ("供应商家撤销发货，客户商家采购失效，待收够数量验证".length()>0 && type=='2') {
			List<String> listMonthnums=new ArrayList<String>(), itemMonthnums=null;
			double rollbackSum = 0;
			for (Iterator<OrderDetail> rbIter=sendList.iterator(); rbIter.hasNext(); listMonthnums.addAll(itemMonthnums)) {
				OrderDetail rollback = rbIter.next();
				rollbackSum += rollback.getAmount();
				itemMonthnums = new ArrayList<String>();
				StringBuffer monthnum = new StringBuffer("like ").append(new OrderTicketLogic().getPrtMonthnum(rollback.getMonthnum())).append("%");
				for (String notnum: listMonthnums) {
					monthnum.append(" and !=").append(notnum);
				}
				this.loadView("WaitList", "uneditable","null", "monthnum",monthnum.toString());
				double sumAmount=(Double)this.getListFootColumn("amount").get(0);
				Assert.assertTrue("待收货采购单没有足够数量", this.getListViewValue().size()>0 && sumAmount>=rollback.getAmount());
				this.setSqlAllSelect(this.getListViewValue().size());
				this.onMenu("收货开单");
				double rollbackAmount = rollback.getAmount();
				for (Iterator<OrderDetail> pIter=this.getForm().getSelectFormer4Purchase().getSelectedList().iterator(); pIter.hasNext() && rollbackAmount>0;) {
					OrderDetail spur=pIter.next().getSnapShot();
					if (spur.getAmount() > rollbackAmount) {
						this.loadView("WaitList", "uneditable","null", "monthnum", spur.getMonthnum());
						this.setSqlAllSelect(1);
						this.onMenu("收货开单");
						this.setEditAllSelect(1);
						this.getForm().getSelectFormer4Purchase().getFirst().getReceiptTicket().setReceiptAmount(rollbackAmount);
						this.onMenu("部分收货拆单");
						OrderDetail purSplit = this.getForm().getSelectFormer4Purchase().getFirst();
						Assert.assertTrue("减小的采购单，数量应为撤销数", purSplit.getId()>0 && StringUtils.equals(new DoubleType().format(purSplit.getAmount()), new DoubleType().format(rollbackAmount)));
						itemMonthnums.add(purSplit.getMonthnum());
						rollbackAmount = 0;
					} else if (spur.getAmount() < rollbackAmount) {
						itemMonthnums.add(spur.getMonthnum());
						rollbackAmount -= spur.getAmount();
					} else {
						itemMonthnums.add(spur.getMonthnum());
						rollbackAmount = 0;
					}
				}
				Assert.assertTrue("订单数够开入库申请", rollbackAmount==0);
				rollback.getVoParamMap().put("WaitInstoreMonthnums", itemMonthnums);
			}
			this.loadView("WaitList", "uneditable","null", "monthnum",listMonthnums.toArray(new String[0]));
			Assert.assertTrue("待收货数为撤销数", rollbackSum==(Double)this.getListFootColumn("amount").get(0) && this.getListViewValue().size()==listMonthnums.size());
			sendList.get(0).getVoParamMap().put("RollbackClientMonthnums", listMonthnums);
		}
		if ("供应商家撤销发货，客户商家采购失效".length()>0 && type=='3') {
			List<String> listMonthnums = sendList.get(0).getVoparam("RollbackClientMonthnums");
			Assert.assertTrue("有待失效月流水号", listMonthnums!=null && listMonthnums.size()>0);
			this.loadView("WaitList", "uneditable","null", "monthnum",listMonthnums.toArray(new String[0]));
			Assert.assertTrue("有可失效待收", this.getListViewValue().size()>0);
			this.setSqlAllSelect(this.getListViewValue().size());
			this.onMenu("采购订单失效");
			sendList.get(0).getVoParamMap().put("RollbackClientPurchases", this.getForm().getSelectFormer4Purchase().getSelectedList());
		}
	}
	
	public void test收货开单() {
		if ("全数收货界面验证".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(11, 11);
			ReceiptTicketForm form = this.getForm();
			this.loadView("WaitList", "number", number);
			int detailCount = this.getListViewValue().size();
			this.setSqlAllSelect(detailCount);
			this.onMenu("收货开单");
			this.setBomConfirm();
			if ("有改单不能全数收货".length()>0) {
				StringBuffer fname = new StringBuffer("supplier.linkerCall");
				form.getNoteFormer4Purchase().getNoteString(form.getDomain(), fname);
				form.getNoteFormer4Purchase().getVoNoteMap(form.getDomain()).put(fname.toString(), "0512-6444444-6");
				fname = new StringBuffer("commodity.model");
				form.getNoteFormer4Purchase().getNoteString(form.getDetailList().get(0), fname);
				for (OrderDetail pur: form.getDetailList())
					form.getNoteFormer4Purchase().getVoNoteMap(pur).put(fname.toString(), "2.0*8");
				try {
					this.setEditAllSelect(detailCount);
					this.onMenu("提交全数收货");
					Assert.fail("有改单不能全数收货");
				}catch(Exception e) {
					form.getNoteFormer4Purchase().getVoNoteMap(form.getDomain()).clear();
					for (OrderDetail detail: form.getDetailList()) {
						form.getNoteFormer4Purchase().getVoNoteMap(detail).clear();
					}
				}
			}
			if ("为部分收货不能全数收货".length()>0) {
				form.getDetailList().get(0).getReceiptTicket().setReceiptAmount(form.getDetailList().get(0).getAmount()-1);
				try {
					this.setEditAllSelect(detailCount);
					this.onMenu("提交全数收货");
					Assert.fail("有部分收货不能全数收货");
				}catch(Exception e) {
					for (OrderDetail detail: form.getDetailList()) {
						detail.getReceiptTicket().setReceiptAmount(detail.getAmount());
					}
				}
			}
			if ("为全数收货不能部分收货".length()>0) {
				form.getDetailList().get(0).getReceiptTicket().setReceiptAmount(form.getDetailList().get(0).getAmount());
				try {
					this.setEditAllSelect(detailCount);
					this.onMenu("部分收货拆单");
					Assert.fail("有全数收货不能部分收货");
				}catch(Exception e) {
					for (OrderDetail detail: form.getDetailList()) {
						detail.getReceiptTicket().setReceiptAmount(detail.getAmount());
					}
				}
			}
			if ("部分收货拆单1留收，可继续全数收货1".length()>0) {
				for (OrderDetail detail: form.getDetailList()) {
					detail.getReceiptTicket().setReceiptAmount(1);
				}
				this.setEditAllSelect(detailCount);
				this.onMenu("部分收货拆单");
				for (OrderDetail detail: form.getDetailList()) {
					detail.getReceiptTicket().setReceiptAmount(1);
				}
				this.setEditAllSelect(detailCount);
				this.onMenu("提交全数收货");
			}
		}
		if ("采购生产11，部分收货1剩余10，10全数收货".length()>0) {
			if ("普通可发货".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(11, 11);
				this.check收货开单__1全数收货_2部分收货n拆单('2', number, 1);
				this.check收货开单__1全数收货_2部分收货n拆单('1', number, "amount", 10);
			}
			if ("直发合并发货，不可再发货".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_直发(11, 11);
				this.check收货开单__1全数收货_2部分收货n拆单('2', number, 1);
				this.check收货开单__1全数收货_2部分收货n拆单('1', number, "amount", 10);
			}
			if ("当地购合并发货，不可再发货".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_当地购(11, 11);
				this.check收货开单__1全数收货_2部分收货n拆单('2', number, 1);
				this.check收货开单__1全数收货_2部分收货n拆单('1', number, "amount", 10);
			}
		}
		if ("改供应商，采购同意，收货确认更改".length()>0) {
			if ("普通可发货".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(11, 11);
				this.check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("1", number);
				this.getModeList().getSelfPurchaseTest().check收货返单处理__1提交改单(number, new String[]{"供应商"}, null, new HashMap<String, Double>());
				this.check收货改单确认__1减少收货_2收货确认("2", number);
			}
			if ("直发合并发货，不能再发货".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_直发(11, 11);
				this.check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("1", number);
				this.getModeList().getSelfPurchaseTest().check收货返单处理__1提交改单(number, new String[]{"供应商"}, null, new HashMap<String, Double>());
				this.check收货改单确认__1减少收货_2收货确认("2", number);
			}
			if ("当地购合并发货，不能再发货".length()>0) {
				this.setTestStart();
				String number = this.getModeList().getSelfPurchaseTest().get客户订单_当地购(11, 11);
				this.check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("1", number);
				this.getModeList().getSelfPurchaseTest().check收货返单处理__1提交改单(number, new String[]{"供应商"}, null, new HashMap<String, Double>());
				this.check收货改单确认__1减少收货_2收货确认("2", number);
			}
		}
	}
	
	private void test采购红冲处理() {
		if ("客户订单采购收货，采购开红冲改供应人、价格，收货红冲处理为同意".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfPurchaseTest().check红冲申请1开红冲__A改供应人_B价格加2__1开红冲_2不通过确认_3删除红冲(number, "AB", '1');
			this.check红冲处理__1通过_2不通过('1', number);
		}
		if ("客户订单采购收货，采购开红冲改供应人、价格，收货红冲处理为不同意，采购不通过确认为只改价格，收货同意".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfPurchaseTest().check红冲申请1开红冲__A改供应人_B价格加2__1开红冲_2不通过确认_3删除红冲(number, "AB", '1');
			this.check红冲处理__1通过_2不通过('2', number);
			this.getModeList().getSelfPurchaseTest().check红冲申请1开红冲__A改供应人_B价格加2__1开红冲_2不通过确认_3删除红冲(number, "B", '2');
			this.check红冲处理__1通过_2不通过('1', number);
		}
		if ("客户订单采购收货，采购开红冲改供应人，收货红冲处理为不同意，采购删除红冲".length()>0) {
			this.setTestStart();
			String number = this.get客户订单_普通(11, 11);
			this.getModeList().getSelfPurchaseTest().check红冲申请1开红冲__A改供应人_B价格加2__1开红冲_2不通过确认_3删除红冲(number, "A", '1');
			this.check红冲处理__1通过_2不通过('2', number);
			this.getModeList().getSelfPurchaseTest().check红冲申请1开红冲__A改供应人_B价格加2__1开红冲_2不通过确认_3删除红冲(number, "", '3');
		}
	}
	
	public void test撤销收货() {
if (1==1) {
		if ("不能撤销收货时情况".length()>0) {
			if ("常规已发货不在撤销".length()>0) {
				this.setTestStart();
				String number = new SendTicketTest().get可发_常规(2, 2);
				new SendTicketTest().check发货开单__1全发_2部分发('1', number, 2);
				this.loadView("RollbackList", "number", number);
				Assert.assertTrue("常规已发货不在撤销", this.getListViewValue().size()==0);
			}
			if ("普通已发货不在撤销".length()>0) {
				this.setTestStart();
				String number = new SendTicketTest().get发货_普通(2, 2);
				this.loadView("RollbackList", "number", number);
				Assert.assertTrue("普通已发货不在撤销", this.getListViewValue().size()==0);
			}
			if ("排单为常规未发货，不在撤销".length()>0) {
				this.setTestStart();
				String number = new ArrangeTicketTest().get客户订单_常规(2, 2);
				this.loadView("RollbackList", "number", number);
				Assert.assertTrue("排单为常规未发货，不在撤销", this.getListViewValue().size()==0);
			}
		}
}
		if ("普通客户订单收货未发撤销".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(1, 1);
			this.check撤销收货(number);
		}
		if ("备货订单收货在撤销".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get备货订单_普通(3, 3);
			this.check撤销收货(number);
		}
		if ("部分发货，订单收货10，先出货7默认出库价，未发货3撤销收货，3继续收货，发货3".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(10, 10);
			new SendTicketTest().check发货开单__1全发_2部分发('2', number, 7);
			this.check撤销收货(number);
			this.check收货开单__1全数收货_2部分收货n拆单('1', number, "amount", 3);
			new SendTicketTest().check发货开单__1全发_2部分发('1', number, 3);
		}
		if ("直发已收货合并发货，再撤销".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_直发(3, 3);
			this.check撤销收货(number);
		}
		if ("当地购已收货合并发货，再撤销".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_当地购(3, 3);
			this.check撤销收货(number);
		}
	}
	
	private void temp() {
		if ("普通客户订单收货未发撤销".length()>0) {
			this.setTestStart();
			String number = this.getModeList().getSelfReceiptTest().get客户订单_普通(1, 1);
			this.check撤销收货(number);
		}
	}

	public void test待处理() {
		if ("收货改单确认，订单采购，收货发起改单申请多收货，采购拒绝多收".length()>0) {
			String number=this.getModeList().getSelfPurchaseTest().get客户订单_普通(11);
			this.check收货改单申请__1供应人_2采购价格_3商品_4多收货10_5有次品2("4", number);
			this.getModeList().getSelfPurchaseTest().check收货返单处理__2拒绝收货(number, "receiptAmount", 11d);
		}
		if ("订单采购收货在库，采购发起红冲".length()>0) {
			String number=this.getModeList().getSelfReceiptTest().get客户订单_普通(12);
			this.getModeList().getSelfPurchaseTest().check红冲申请1开红冲__A改供应人_B价格加2__1开红冲_2不通过确认_3删除红冲(number, "A", '1');
		}
	}
	
	public String getPur备货订单_普通(int... amountList) {
		String number = this.get备货订单_普通(amountList);
		this.loadFormView(this.getModeList().getSelfPurchaseForm(), "ShowQuery", "number", number);
		return (String)this.getListViewColumn("purName").get(0);
	}
	
	public String get备货订单_普通(int... amountList) {
		TestMode[] linkMode = this.getModeList().removeMode(TestMode.ClientOrder, TestMode.SubcompanyOrder);
		String number = this.getModeList().getSelfPurchaseTest().get备货订单_普通(amountList);
		this.getModeList().addMode(linkMode);
		this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
		return number;
	}
	
	public String get客户订单_普通(int... amountList) {
		String number = this.getModeList().getSelfPurchaseTest().get客户订单_普通(amountList);
		this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
		return number;
	}

	public String get客户订单_直发(int... amountList) {
		String number = this.getModeList().getSelfPurchaseTest().get客户订单_直发(amountList);
		this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
		return number;
	}
	
	public String get客户订单_当地购(int... amountList) {
		String number = this.getModeList().getSelfPurchaseTest().get客户订单_当地购(amountList);
		this.getModeList().getSelfReceiptTest().check收货开单__1全数收货_2部分收货n拆单('1', number);
		return number;
	}
	
	protected void setBomConfirm() {
		this.onMenu("确认入库数");
		this.onMenu("确认出库数");
		this.onMenu("确认留用.添加数");
		this.onMenu("确认留用.领用数");
	}

	protected void setQ清空() {
		
	}
}
