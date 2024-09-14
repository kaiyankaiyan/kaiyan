package com.haoyong.sales.common.listener;


public class DayCalculateListener extends AttrRunnableListener {

	@Override
	public boolean isRunnable() {
		return true;
	}

	@Override
	public void runTask() throws Exception {
		if ("每天订单销售量统计".length()>0) {
		}
	}
	
	protected void runBefore() {
	}
	
	public void runAfter() {
	}
}
