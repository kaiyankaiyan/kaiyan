package com.haoyong.sales.common.listener;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.support.database.DatabaseInfoReader;
import net.sf.mily.support.database.HbmConfiguration;
import net.sf.mily.support.database.SchemaTool;
import net.sf.mily.ui.WindowMonitor;

import org.junit.Test;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.common.dao.SSaleSessionProvider;
import com.haoyong.sales.test.base.UserTest;

public class SchemaUpdateListener extends AttrRunnableListener {
	
	public void runTask() throws Exception {
		for (String dbname: DatabaseInfoReader.getReader().getDatabaseInfo().getSellerList()) {
			Seller seller = new Seller();
			seller.setId(14);
			seller.setSqlName(dbname);
			
			WindowMonitor.getMonitor().addAttribute("seller", seller);
			SchemaTool schemaTool = new SchemaTool(HbmConfiguration.getCached(dbname).getConfiguration());
			schemaTool.update();
			new UserTest().getUser管理员();
		}
	}
	
	protected void runBefore() {
		this.getWindowMonitor();
	}
	
	public void runAfter() {
		new SessionProvider().clear();
		WindowMonitor.getMonitor().close();
	}
	
	@Test
	public void test() {
		new SSaleSessionProvider().run();
		this.run();
	}
	
	public boolean isRunnable() {
		return true;
	}
}
