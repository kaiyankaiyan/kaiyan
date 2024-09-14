package com.haoyong.sales.sale.service;

import java.util.Calendar;
import java.util.List;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.mappings.EntityClass;
import net.sf.mily.server.EditViewer;
import net.sf.mily.ui.Menu;
import net.sf.mily.ui.Window;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;
import net.sf.mily.webObject.EditView;
import net.sf.mily.webObject.EditViewBuilder;
import net.sf.mily.webObject.SqlListBuilder;

import org.junit.Test;

import com.haoyong.sales.common.listener.TransRunnableListener;
import com.haoyong.sales.sale.form.StoreTicketForm;

/**
 * 每月每个Seller月出入库数量计算
 */
public class StoreMonthLisenter extends TransRunnableListener {

	private long sellerId;

	@Test
	public void runTask() throws Exception {
		Window win = new Window();
		StoreTicketForm form = new StoreTicketForm();
		this.sellerId = form.getSellerId();
		EditViewBuilder builder = (EditViewBuilder)EntityClass.loadViewBuilder(form.getClass(), "MonthCountList");
		// clear filters
		for (SqlListBuilder sqlBuilder: builder.getFieldBuildersDeep(SqlListBuilder.class)) {
			form.getSearchSetting(sqlBuilder);
		}
		EditViewer viewer = new EditViewer(builder, form, 1024);
		EditView view = viewer.createView();
		win.add(view.getComponent());
		List<Menu> menuList = view.getComponent().getInnerComponentList(Menu.class);
		menuList.get(1).getEventListenerList().fireListener();
	}
	
	protected void runBefore() {
		this.getWindowMonitor();
	}
	
	public void runAfter() {
		new SessionProvider().clear();
		WindowMonitor.getMonitor().close();
		if (this.isCommit()==true)
			LogUtil.info(new StringBuffer().append(sellerId).append("月出入库数计算完成。。。。。。").toString());
	}
	
	public boolean isRunnable() {
		Calendar calendar = Calendar.getInstance();
		int day=calendar.get(Calendar.DAY_OF_MONTH);
		return day==1;
	}
}
