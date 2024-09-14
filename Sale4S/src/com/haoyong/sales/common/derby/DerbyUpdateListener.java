package com.haoyong.sales.common.derby;

import java.net.URL;

import net.sf.mily.support.database.SchemaTool;
import net.sf.mily.ui.WindowMonitor;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import com.haoyong.sales.common.listener.AttrRunnableListener;

public class DerbyUpdateListener extends AttrRunnableListener {
	
	private static SessionFactory sessionFactory=HbmConfiguration();
	private static org.hibernate.cfg.Configuration hbmcfg;
	
	public void runTask() throws Exception {
		Configuration cfg = this.hbmcfg;
		SchemaTool schemaTool = new SchemaTool(cfg);
		schemaTool.update();
	}
	
	protected void runBefore() {
		this.getWindowMonitor();
	}
	
	public void runAfter() {
		WindowMonitor.getMonitor().close();
	}
	
	private static SessionFactory HbmConfiguration(){
		URL url = Thread.currentThread().getContextClassLoader().getResource("derby.cfg.xml");
		hbmcfg=new AnnotationConfiguration().configure(url);
		sessionFactory = hbmcfg.buildSessionFactory();
		return sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	public boolean isRunnable() {
		return true;
	}
}
