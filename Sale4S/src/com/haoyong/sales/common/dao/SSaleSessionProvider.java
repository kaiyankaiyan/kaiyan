package com.haoyong.sales.common.dao;

import net.sf.mily.cfg.SessionProvidly;
import net.sf.mily.support.database.DatabaseInfoReader;
import net.sf.mily.support.database.HbmConfiguration;
import net.sf.mily.ui.WindowMonitor;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.common.derby.DerbyDAO;
import com.haoyong.sales.common.listener.RunnableListener;
/**
 * hibernate工具,获取数据连接
 */
public class SSaleSessionProvider extends RunnableListener implements SessionProvidly {
	
	/**
	 * 初始化加载所有库Configuration,
	 * 把HbmConfiguration实体保存到Map中:Map<"数据库名",HbmConfiguration>
	 */
	public void runTask() {
		WindowMonitor monitor = WindowMonitor.getMonitor(new StringBuffer().append(this.getClass().getSimpleName()).append(this.hashCode()).toString());
		for (String name: DatabaseInfoReader.getReader().getDatabaseInfo().getSellerList()) {
			Seller seller = new Seller();
			seller.setSqlName(name);
			monitor.addAttribute("seller", seller);
			HbmConfiguration.getCached(name).getConfiguration();
		}
	}
	
	public void runAfter() {
		WindowMonitor.getMonitor().close();
	}
	
	public Configuration getHbmConfiguration() {
		return getLocalHbmConfiguration().getConfiguration();
	}
	
	public SessionFactory getSessionFactory() {
		return getLocalHbmConfiguration().getSessionFactory();
	}
	
	public void close(){
		BaseDAO.close();
		DerbyDAO.close();
	}
	
	/**
	 * 获取session,传递过来的数据库名
	 */
	public String getLocalDBName(){
		Seller seller = (Seller)WindowMonitor.getMonitor().getAttribute("seller");
		return seller.getSqlName();
	}
	
	public HbmConfiguration getLocalHbmConfiguration(){
		return HbmConfiguration.getCached(getLocalDBName());//切换数据库
	}
	
	public boolean isRunnable() {
		return true;
	}
}
