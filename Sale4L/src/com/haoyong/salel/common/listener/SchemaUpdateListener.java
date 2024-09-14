package com.haoyong.salel.common.listener;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.support.database.SchemaTool;

public class SchemaUpdateListener extends RunnableListener {
	
	public void runTask() throws Exception {
		SchemaTool schemaTool = new SchemaTool(SessionProvider.getHbmConfiguration());
		schemaTool.update();
	}
	
	public void runAfter() {
		new SessionProvider().clear();
	}
	
	public boolean isRunnable() {
		return true;
	}
}
