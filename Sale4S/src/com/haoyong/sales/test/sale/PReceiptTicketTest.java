package com.haoyong.sales.test.sale;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.form.BOMForm;
import com.haoyong.sales.common.listener.SelectDomainListener;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.form.ReceiptTicketForm;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.sale.logic.OrderTicketLogic;
import com.haoyong.sales.sale.logic.PurchaseTicketLogic;
import com.haoyong.sales.test.base.CommodityTest;
import com.haoyong.sales.util.SSaleUtil;


public class PReceiptTicketTest extends ReceiptTicketTest {
	
	public void check收货开单__1全数收货_2部分收货n拆单(char type, String number, Object... receiptN_filters0) {
		Object[] filters0 = receiptN_filters0;
		if (receiptN_filters0.length % 2==1)
			filters0 = ArrayUtils.subarray(receiptN_filters0, 1, receiptN_filters0.length);
		if (number==null) {
			this.loadView("WaitList", filters0);
			number = (String)this.getListViewColumn("number").get(0);
		}
		this.instoreBomDetails(number);
		super.check收货开单__1全数收货_2部分收货n拆单(type, number, receiptN_filters0);
		ReceiptTicketForm form = this.getForm();
		OrderDetail pur=form.getDetailList().get(form.getDetailList().size()-1), spur=pur.getSnapShot();
		List<BomDetail> bomList = pur.getVoparam("BomDetails");
		if ("生产成品全数收货".length()>0 && type=='1' && bomList.size()>0) {
			Long[] bomidList = new Long[bomList.size()];
			for (int bi=0; bi<bomList.size(); bomidList[bi]=bomList.get(bi).getId(),bi++);
			this.loadFormView(new BOMForm(), "ShowQuery", "number", number, "bomId", bomidList);
			List<Double> bomAmounts1 = (List)this.getListFootColumn("amount", "gotAmount", "notAmount", "occupyAmount", "keepAmount");
			if ("留用数加入台账".length()>0 && bomAmounts1.get(4)>0) {
				this.loadFormView(new StoreTicketForm(), "StoreAgentQuery");
				Assert.assertTrue("添加台账数", this.getListViewValue().size()>0);
			}
			int level=bomList.get(0).getLevel();
			for (BomDetail b: bomList) {
				Assert.assertTrue("生产Bom物料出库， >=同一级别物料", b.getStBom()==30 && b.getLevel()>=level);
				Assert.assertTrue("生产Bom物料出库", b.getStBom()==30);
			}
		}
	}
	
	// 返回purName
	public String check收货改单确认__1减少收货_2收货确认(String types0, String number) {
		if (types0.contains(String.valueOf('2'))==false)
			return super.check收货改单确认__1减少收货_2收货确认(types0, number);
		this.loadFormView(new BOMForm(), "ShowQuery", "number", number, "arrange", "notnull");
		this.instoreBomDetails(number);
		String purName = super.check收货改单确认__1减少收货_2收货确认(types0, number);
		ReceiptTicketForm form = this.getForm();
		OrderDetail pur=form.getDetailList().get(form.getDetailList().size()-1), spur=pur.getSnapShot();
		List<BomDetail> bomList = pur.getVoparam("BomDetails");
		if ("生产成品全数收货".length()>0 && types0.contains("2") && bomList.size()>0) {
			Long[] bomidList = new Long[bomList.size()];
			for (int bi=0; bi<bomList.size(); bomidList[bi]=bomList.get(bi).getId(),bi++);
			this.loadFormView(new BOMForm(), "ShowQuery", "number", number, "bomId", bomidList);
			List<Double> bomAmounts1 = (List)this.getListFootColumn("amount", "gotAmount", "notAmount", "occupyAmount", "keepAmount");
			if ("留用数加入台账".length()>0 && bomAmounts1.get(4)>0) {
				this.loadFormView(new StoreTicketForm(), "StoreAgentQuery");
				Assert.assertTrue("添加台账数", this.getListViewValue().size()>0);
			}
			int level=bomList.get(0).getLevel();
			for (BomDetail b: bomList) {
				Assert.assertTrue("生产Bom物料出库， >=同一级别物料", b.getStBom()==30 && b.getLevel()>=level);
				Assert.assertTrue("生产Bom物料出库", b.getStBom()==30);
			}
		}
		return purName;
	}
	
	protected void instoreBomDetails(String number) {
		this.loadFormView(new BOMForm(), "ShowQuery", "number", number, "arrange", "notnull");
		List<BomDetail> sourceList = new SelectDomainListener().toDomains((List<List<Object>>)(List)this.getListViewValue(), BomDetail.class);
		if (sourceList.size()==0)
			return;
		List<OrderDetail> bomList = new ArrayList<OrderDetail>();
		OrderDetail pur = new StoreTicketTest().get备货库存1个();
		for (BomDetail sbom: sourceList) {
			OrderDetail bom = new PurchaseTicketLogic().genClonePurchase(pur);
			bom.setCommodity(sbom.getCommodity());
			bom.setAmount(sbom.getBomTicket().getOccupyAmount()+sbom.getBomTicket().getNotAmount());
			bom.setPrice(1);
			bom.setMonthnum(new OrderTicketLogic().genMonthnum());
			bomList.add(bom);
		}
		new StoreTicketTest().check任意入库(bomList);
	}
}
