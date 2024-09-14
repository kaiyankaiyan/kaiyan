package com.haoyong.sales.common.listener;

import net.sf.mily.util.LogUtil;

/**
 * 定期在无事务环境——执行Listener
 *
 */
public abstract class RunnableListener implements Runnable {
	
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
	
	protected void runBefore(){
		
	}
	
	public void run() {
		if (this.isRunnable()==false)
			return;
		try {
			this.runBefore();
			runTask();
			this.isCommit = true;
		} catch (Throwable e) {
			if (this.manual==true)
				LogUtil.error("计划执行错误"+this.getClass().getSimpleName(), e);
			else
				throw LogUtil.getRuntimeException(e);
		} finally {
			this.runAfter();
		}
	}
	
	protected abstract void runAfter();

	public void setManual(boolean manual) {
		this.manual = manual;
	}
	
	protected boolean isCommit() {
		return this.isCommit;
	}
}
