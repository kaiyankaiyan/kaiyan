package com.haoyong.sales.common.listener;

import java.util.Calendar;

import net.sf.mily.ui.WindowMonitor;

public class MonthCalculateListener extends AttrRunnableListener {

	@Override
	public boolean isRunnable() {
		Calendar calendar = Calendar.getInstance();
		int day=calendar.get(Calendar.DAY_OF_MONTH);
		return (day==1);
	}

	@Override
	public void runTask() throws Exception {
		if ("订单，每月师傅销售量统计，每月城市销售量统计".length()>0) {
		}
	}
	
	protected void runBefore() {
		this.getWindowMonitor();
	}
	
	public void runAfter() {
		WindowMonitor.getMonitor().close();
	}
}
