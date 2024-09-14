package com.haoyong.sales.common.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.server.EditViewer;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.ViewBuilder;

import com.haoyong.sales.base.domain.Client;
import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.base.domain.Storehouse;
import com.haoyong.sales.base.domain.Supplier;
import com.haoyong.sales.base.logic.Seller4lLogic;
import com.haoyong.sales.base.service.SellerService;
import com.haoyong.sales.common.form.TestCaseForm;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.BillDetail;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.OrderTicket;
import com.haoyong.sales.sale.domain.Question;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.domain.StoreItem;
import com.haoyong.sales.sale.domain.StoreMonth;
import com.haoyong.sales.test.base.AbstractTest.TestMode;
import com.haoyong.sales.test.base.AbstractTest.TestModeList;
import com.haoyong.sales.test.base.ClientTest;
import com.haoyong.sales.test.base.UserTest;
import com.haoyong.sales.test.sale.ArrangeTicketTest;
import com.haoyong.sales.test.sale.BillTicketTest;
import com.haoyong.sales.test.sale.InStoreTicketTest;
import com.haoyong.sales.test.sale.OrderTicketTest;
import com.haoyong.sales.test.sale.OutStoreTicketTest;
import com.haoyong.sales.test.sale.PurchaseTicketTest;
import com.haoyong.sales.test.sale.ReceiptTicketTest;
import com.haoyong.sales.test.sale.SendTicketTest;
import com.haoyong.sales.test.sale.StoreTicketTest;
import com.haoyong.sales.test.sale.TRemind;
import com.haoyong.sales.test.sale.WBillTicketTest;

public class TestCaseListener extends AttrRunnableListener {

	public void runTask() throws Exception {
		new OrderTicketTest().set行业();
		new ClientTest().getModeList().setMode();
		for (Iterator<ViewBuilder> iter=EntityClass.forName(TestCaseForm.class).getViewIterator(); iter.hasNext();) {
			ViewBuilder builder0 = iter.next();
			if (builder0.getName().charAt(0)!='g')
				continue;
			ViewBuilder builder = (ViewBuilder)builder0.createClone();
			new EditViewer(builder, new TestCaseForm(), 1280).createView();
		}
		// 生成待处理提醒记录
		this.genWaitingRemind();
	}
	
	public void clearSellerData() {
		ViewData viewData = new ViewData();
		if (true) {
			List<Class> ticketList = new ArrayList<Class>();
			ticketList.add(Client.class);
			ticketList.add(Supplier.class);
			ticketList.add(Commodity.class);
			ticketList.add(Storehouse.class);
			ticketList.add(Question.class);
			ticketList.add(OrderTicket.class);
			ticketList.add(OrderDetail.class);
			ticketList.add(BomDetail.class);
			ticketList.add(BillDetail.class);
			ticketList.add(StoreItem.class);
			ticketList.add(StoreMonth.class);
			ticketList.add(StoreEnough.class);
			viewData.setTicketDetails(ticketList);
		}
		new UserTest().setQ清空();
		
		new ClientTest().setTransSeller(new Seller4lLogic().get吉高电子());
		new SellerService().deleteTickets(viewData);
		new ClientTest().setTransSeller(new Seller4lLogic().get南宁古城());
		new SellerService().deleteTickets(viewData);
	}
	
	public void genWaitingRemind() {
		Class[] testList = new Class[]{
				ArrangeTicketTest.class,
				BillTicketTest.class,
				InStoreTicketTest.class,
				OrderTicketTest.class,
				OutStoreTicketTest.class,
				PurchaseTicketTest.class,
				ReceiptTicketTest.class,
				SendTicketTest.class,
				StoreTicketTest.class,
				WBillTicketTest.class};
		TestModeList modeList = new ClientTest().getModeList();
		modeList.setMode(TestMode.Purchase);
		modeList.setOrderTime(new Date());
		for (Class test: testList) {
			if (TRemind.class.isAssignableFrom(test)) {
				TRemind remind = (TRemind)ReflectHelper.invokeConstructor(test, new Object[0]);
				remind.test待处理();
			}
		}
		modeList = new ClientTest().getModeList();
		modeList.setMode(TestMode.Product);
		modeList.setOrderTime(new Date());
		for (Class test: testList) {
			if (TRemind.class.isAssignableFrom(test)) {
				TRemind remind = (TRemind)ReflectHelper.invokeConstructor(test, new Object[0]);
				remind.test待处理();
			}
		}
	}
	
	protected void runBefore() {
		PerformTrace.start();
		this.getWindowMonitor();
		this.clearSellerData();
	}
	
	protected void runAfter() {
		PerformTrace.end("----------------------------service.process(conn)------------------------------");
		new ClientTest().getModeList().clearTest();
		new SessionProvider().clear();
		if (this.getSessionName()!=null)
			WindowMonitor.getMonitor().close();
	}
	
	public boolean isRunnable() {
		return true;
	}
}
