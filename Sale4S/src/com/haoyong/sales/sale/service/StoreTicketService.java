package com.haoyong.sales.sale.service;

import java.util.ArrayList;
import java.util.List;

import net.sf.mily.bus.annotation.ActionService;
import net.sf.mily.bus.annotation.ExecutionPriority;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.server.EditViewer;
import net.sf.mily.ui.Component;
import net.sf.mily.ui.Menu;
import net.sf.mily.ui.Window;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.EditViewBuilder;
import net.sf.mily.webObject.SqlListBuilder;

import com.haoyong.sales.base.domain.Commodity;
import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.domain.AbstractCommodityItem;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.MatchActions;
import com.haoyong.sales.common.form.ViewData;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.domain.StoreEnough;
import com.haoyong.sales.sale.domain.StoreItem;
import com.haoyong.sales.sale.domain.StoreMonth;
import com.haoyong.sales.sale.form.StoreTicketForm;
import com.haoyong.sales.test.base.ClientTest;

/**
 * 业务服务类——仓库计算
 *
 */
@ActionService
public class StoreTicketService {

	@MatchActions({ActionEnum.StoreTicket_Restore})
	public void restore(ViewData<StoreItem> viewData) {
		List<StoreItem> storeList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (StoreItem store: storeList) {
			dao.saveOrUpdate(store);
		}
		for (StoreItem store: (List<StoreItem>)viewData.getParam("DeleteList")) {
			dao.remove(store);
		}
	}

	@MatchActions(priority=ExecutionPriority.AfterTrans, value={
			ActionEnum.ReceiptTicket_Effect, 
			ActionEnum.SaleTicket_Effect})
	public void restore_Firer(ViewData<AbstractCommodityItem> viewData) {
		List<Commodity> commodityList = new ArrayList<Commodity>();
		for (AbstractCommodityItem detail: viewData.getTicketDetails()) {
			AbstractCommodityItem sdetail = detail.getSnapShot();
			commodityList.add(detail.getCommodity());
			commodityList.add(sdetail.getCommodity());
		}
		new ClientTest().getModeList().addTest("storeForm.getStoreCommodityList", commodityList);
		StoreTicketForm storeForm = new StoreTicketForm();
		EditViewBuilder builder = (EditViewBuilder)EntityClass.loadViewBuilder(storeForm.getClass(), "RestoreList");
		for (SqlListBuilder sqlBuilder: builder.getFieldBuildersDeep(SqlListBuilder.class)) {
			storeForm.getSearchSetting(sqlBuilder);
		}
		storeForm.getStoreCommodityList().addAll(commodityList);
		EditViewer viewer = new EditViewer(builder, storeForm, 1024);
		EditView editView = viewer.createView();
		Window window = new Window();
		window.add(editView.getComponent());
		Menu menu = editView.getComponent().getInnerComponentList(Menu.class).get(0);
		menu.getEventListenerList().fireListener();
	}
	
	@MatchActions({ActionEnum.StoreTicket_Extra})
	public void extraAdd(ViewData<OrderDetail> viewData) {
		BaseDAO dao = new BaseDAO();
		if (viewData.getParam("SaveList")!=null)
			for (OrderDetail store: (List<OrderDetail>)viewData.getParam("SaveList")) {
				dao.saveOrUpdate(store);
			}
		if (viewData.getParam("DeleteList")!=null)
			for (OrderDetail store: (List<OrderDetail>)viewData.getParam("DeleteList")) {
				dao.saveOrUpdate(store);
			}
	}

	/**
	 * 每尾（下月1号过0点）统计完整一月的出入库数量
	 */
	@MatchActions({ActionEnum.StoreTicket_Month})
	public void month(ViewData<StoreMonth> viewData) {
		List<StoreMonth> storeList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (StoreMonth store: storeList) {
			dao.saveOrUpdate(store);
		}
		for (StoreMonth store: (List<StoreMonth>)viewData.getParam("DeleteList")) {
			dao.remove(store);
		}
	}
	
	@MatchActions({ActionEnum.StoreTicket_Enough})
	public void enough(ViewData<StoreEnough> viewData) {
		List<StoreEnough> storeList = viewData.getTicketDetails();
		BaseDAO dao = new BaseDAO();
		for (StoreEnough store: storeList) {
			dao.saveOrUpdate(store);
		}
		for (StoreEnough store: (List<StoreEnough>)viewData.getParam("DeleteList")) {
			dao.remove(store);
		}
	}

	@MatchActions(priority=ExecutionPriority.AfterTrans, value={
			ActionEnum.ReceiptTicket_Effect,
			ActionEnum.SaleTicket_Effect, 
			ActionEnum.OrderTicket_Effect, ActionEnum.PurchaseTicket_Effect
	})
	public void enough_Firer(ViewData<AbstractCommodityItem> viewData) {
		"".toCharArray();
		List<Commodity> commodityList = new ArrayList<Commodity>();
		for (AbstractCommodityItem detail: viewData.getTicketDetails()) {
			AbstractCommodityItem sdetail = detail.getSnapShot();
			commodityList.add(detail.getCommodity());
			commodityList.add(sdetail.getCommodity());
		}
		new ClientTest().getModeList().addTest("storeForm.getStoreCommodityList", commodityList);
		StoreTicketForm storeForm = new StoreTicketForm();
		EditViewBuilder builder = (EditViewBuilder)EntityClass.loadViewBuilder(storeForm.getClass(), "EnoughList");
		for (SqlListBuilder sqlBuilder: builder.getFieldBuildersDeep(SqlListBuilder.class)) {
			storeForm.getSearchSetting(sqlBuilder);
		}
		storeForm.getStoreCommodityList().addAll(commodityList);
		EditViewer viewer = new EditViewer(builder, storeForm, 1024);
		EditView editView = viewer.createView();
		Window window = new Window();
		window.add((Component)editView.getComponent());
		Menu menu = editView.getComponent().getInnerComponentList(Menu.class).get(0);
		menu.getEventListenerList().fireListener();
	}
}
