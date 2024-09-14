package com.haoyong.salel.common.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mily.cfg.SessionProvidly;
import net.sf.mily.common.SessionProvider;
import net.sf.mily.support.database.DatabaseInfoReader;
import net.sf.mily.support.database.HbmConfiguration;
import net.sf.mily.util.ReflectHelper;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;

import com.haoyong.salel.common.listener.RunnableListener;

/**
 * hibernate工具,获取数据连接
 */
public class SLoginSessionProvider extends RunnableListener implements SessionProvidly{
	
	protected void runTask() {
		DatabaseInfoReader.getReader().getDatabaseInfo();
	}
	
	protected void runAfter() {
	}
	
	/**
	 * 获取指定类的持久化子类
	 */
	public static List<String> getHbmClass(Class faceClass) {
		Set<String> hbmList = new HashSet<String>();
		Map<String, PersistentClass> map = (Map<String,PersistentClass>)ReflectHelper.getPropertyValue(SessionProvider.getHbmConfiguration(), "classes");
		for (Map.Entry<String, PersistentClass> item: map.entrySet()) {
			String clsName = item.getKey();
			PersistentClass clsPresent = item.getValue();
			Class cls = ReflectHelper.classForName(clsName);
			if (faceClass.isAssignableFrom(cls) && !hbmList.contains(cls)) {
				hbmList.add(clsPresent.getTable().getName());
			}
		}
		return new ArrayList<String>(hbmList);
	}
	
	public Configuration getHbmConfiguration() {
		return getLocalHbmConfiguration().getConfiguration();
	}
	
	public SessionFactory getSessionFactory() {
		return getLocalHbmConfiguration().getSessionFactory();
	}
	
	public void close(){
		new BaseDAO().close();
	}
	
	/**
	 * 获取session,传递过来的数据库名
	 * @return String
	 */
	public String getLocalDBName(){
		String dbName = DatabaseInfoReader.getReader().getDatabaseInfo().getLogin();
		return dbName;
	}
	
	public HbmConfiguration getLocalHbmConfiguration(){
		return HbmConfiguration.getCached(getLocalDBName());//切换数据库
	}
	
	protected boolean isRunnable() {
		return true;
	}
}
