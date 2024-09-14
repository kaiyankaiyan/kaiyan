package com.haoyong.salel.common.listener;

import java.util.HashMap;
import java.util.Map;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;

import com.haoyong.salel.common.dao.TransactionManager;

/**
 * 定期在事务环境执行Listener
 *
 */
public abstract class TransRunnableListener implements Runnable {
	
	private boolean manual=false, isCommit=false;
	
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
		this.runBefore();
		TransactionManager.begin();
		try {
			runTask();
			TransactionManager.commit();
			this.isCommit = true;
		} catch (Exception e) {
			TransactionManager.rollback();
			String info = "T计划事务执行错误"+this.getClass().getSimpleName();
			if (isManual()) {
				throw new LogicException(2, e, info);
			} else {
				LogUtil.error(info, e);
			}
		}
		this.runAfter();
	}
	
	protected void runBefore() {
		//
	}
	
	protected abstract void runAfter();

	public boolean isManual() {
		return this.manual;
	}
	
	protected boolean isCommit() {
		return this.isCommit;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}
}
