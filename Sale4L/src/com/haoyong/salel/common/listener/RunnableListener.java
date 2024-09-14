package com.haoyong.salel.common.listener;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.util.LogUtil;

/**
 * 定期在无事务环境——执行Listener
 *
 */
public abstract class RunnableListener implements Runnable {
	
	private boolean isCommit=false;
	
	/**
	 * 执行任务
	 * @throws Exception
	 */
	protected abstract void runTask() throws Exception;

	/**
	 * 是否可运行
	 * 开始、结束行要LogInfo
	 */
	protected abstract boolean isRunnable();
	
	public void run() {
		if (isRunnable()==false)
			return ;
		try {
			runTask();
			this.isCommit = true;
		} catch (Exception e) {
			LogUtil.error("计划执行错误"+this.getClass().getSimpleName(), e);
		} finally {
			
		}
	}
	
	protected abstract void runAfter();
	
	protected boolean isCommit() {
		return this.isCommit;
	}
}
