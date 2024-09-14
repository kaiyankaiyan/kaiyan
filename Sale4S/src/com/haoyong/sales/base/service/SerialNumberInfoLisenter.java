package com.haoyong.sales.base.service;

import java.util.Calendar;
import java.util.Date;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.listener.TransRunnableListener;

/**
 * 定时删除历史单号记录
 */
public class SerialNumberInfoLisenter extends TransRunnableListener {
	
	private long sellerId;
	
	public void runTask() throws Exception {
		BaseDAO dao=new BaseDAO();
		String sql="DELETE FROM bs_SerialNumberInfo WHERE createDate<? and sellerId=?";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -6);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date date = calendar.getTime();
		dao.getSQLQuery(sql, date).executeUpdate();
	}
	
	protected void runBefore() {
		this.getWindowMonitor();
	}
	
	public void runAfter() {
		new SessionProvider().clear();
		WindowMonitor.getMonitor().close();
		if (this.isCommit()==true)
			LogUtil.info(new StringBuffer().append(sellerId).append("删除过去月份最大单号完成。。。。。。").toString());
	}
	
	public boolean isRunnable() {
		Calendar calendar = Calendar.getInstance();
		int day=calendar.get(Calendar.DAY_OF_MONTH);
		return day==1;
	}
}
