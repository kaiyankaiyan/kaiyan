package com.haoyong.salel.common.listener;

import java.io.PrintWriter;

import net.sf.mily.util.LogUtil;

import org.apache.derby.drda.NetworkServerControl;
import org.junit.Test;

public class DerbyServerListener extends RunnableListener {
	
	private NetworkServerControl derbyServer;

	@Test
	public void runTask() {
		try {
			this.derbyServer = new NetworkServerControl();
			PrintWriter pw = new PrintWriter(System.out);
			derbyServer.start(pw);//启动Derby服务器
		} catch (Exception ex) {
			LogUtil.error("启动derby失败！", ex);
		}
	}
	
	public void runAfter() {
		
	}
	
	public void close() throws Exception {
		this.derbyServer.shutdown();
	}

	protected boolean isRunnable() {
		return true;
	}
}
