package com.haoyong.salel.base.service;

import java.util.Calendar;
import java.util.Date;

import net.sf.mily.common.SessionProvider;

import org.junit.Test;

import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.listener.TransRunnableListener;

/**
 * 定时删除6个月前的历史最大单号记录
 */
public class SerialNumberInfoLisenter extends TransRunnableListener {
	
	@Test
	public void runTask() throws Exception {
		BaseDAO dao=new BaseDAO();
		String sql="DELETE FROM bs_SerialNumberInfo WHERE createDate<?";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -6);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date date = calendar.getTime();
		dao.getSQLQuery(sql, date).executeUpdate();
	}
	
	protected void runAfter() {
		new SessionProvider().clear();
	}
	
	@Override
	public boolean isRunnable() {
		Calendar calendar = Calendar.getInstance();
		int day=calendar.get(Calendar.DAY_OF_MONTH);
		return (day==1);
	}
}
