package com.haoyong.sales.common.listener;

import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;

import com.haoyong.sales.common.dao.BaseDAO;

/**
 * 定期在执行Listener
 *
 */
public abstract class AttrRunnableListener implements Runnable {
	
	private boolean manual=false, isCommit=false;
	private String sessionName;
	
	/**
	 * 执行任务
	 */
	public abstract void runTask() throws Exception;

	/**
	 * 是否可运行
	 * 开始、结束行要LogInfo
	 */
	public abstract boolean isRunnable();
	
	public void run() {
		if (isRunnable()==false)
			return;
		try {
			this.runBefore();
			runTask();
			this.isCommit = true;
		} catch (Throwable e) {
			String info = "T计划事务执行错误"+this.getClass().getSimpleName();
			if (this.manual == true) {
				throw new LogicException(2, e, info);
			} else {
				LogUtil.error(info, e);
			}
		} finally {
			this.runAfter();
		}
	}
	
	protected abstract void runBefore();
	
	protected abstract void runAfter();
	
	public WindowMonitor getWindowMonitor() {
		if (this.sessionName==null)
			return WindowMonitor.getMonitor();
		return WindowMonitor.getMonitor(this.sessionName);
	}
	
	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}
	
	public String getSessionName() {
		return this.sessionName;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}
	
	protected boolean isCommit() {
		return this.isCommit;
	}
}
