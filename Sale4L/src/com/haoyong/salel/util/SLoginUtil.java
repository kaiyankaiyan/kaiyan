package com.haoyong.salel.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

import net.sf.mily.bus.Bus;
import net.sf.mily.bus.service.Action;
import net.sf.mily.bus.service.ActionServiceBusConfiguration;
import net.sf.mily.bus.service.ServiceChannel;
import net.sf.mily.ui.enumeration.FileName;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.PropertyMap;
import net.sf.mily.util.ReflectHelper;

import com.haoyong.salel.common.dao.BaseDAO;
import com.haoyong.salel.common.dao.SLoginActionServiceBusConfiguration;
import com.haoyong.salel.common.dao.TransactionManager;
import com.haoyong.salel.common.domain.AbstractDomain;
import com.haoyong.salel.common.domain.Serialy;
import com.haoyong.salel.common.form.ActionEnum;
import com.haoyong.salel.common.form.ViewData;

/**
 * SLogin项目工具类
 *
 */
public class SLoginUtil {

	private static PropertyMap properties;
	private static PropertyMap messages = PropertyMap.create("Message");
	private static Bus bus = getBus();
	private static ActionServiceBusConfiguration configuration;
	private static String webIncludeUri = getWebIncludeUri();
	
	/**
	 * 项目配置属性
	 */
	public static PropertyMap getProperties() {
		if (properties==null) {
			PropertyMap props = PropertyMap.create("SLogin");
			String host = null;
			if ("多网卡".length()>0) {
			    try {
			        //获得该机器上所有的网卡
			        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			        //Enumeration是一个继承迭代器的类，有hasMoreElements()以及nextElement()
			        while (networkInterfaces.hasMoreElements() && host==null){
			            NetworkInterface networkInterface = networkInterfaces.nextElement();
			            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
			            while (inetAddresses.hasMoreElements() && host==null){
			                InetAddress address = inetAddresses.nextElement();
//			                System.out.println("网卡名称："+networkInterface.getName()+"  "+"IP地址："+address.getHostAddress());
			                if (networkInterface.getName().startsWith("wlan") && address.getHostAddress().replaceAll("192.\\d+.\\d+.\\d+", "").length()==0)
			                	host = address.getHostAddress();
			            }
			        }

			    } catch (SocketException e) {
			    }
			}
			for (Object k0: props.keySet()) {
				String key=(String)k0, val=props.getString(key);
				if (host==null)
					continue;
				val = val.replaceAll("192.\\d+.\\d+.\\d+", host);
				props.put(key, val);
			}
			properties = props;
		}
		return properties;
	}
	
	/**
	 * 按编码，出报错内容
	 */
	public static PropertyMap getMessages() {
		return messages;
	}
	
	/**
	 * 取引用文件全路径名
	 */
	public static String getWebIncludeUrl(String path) {
		return webIncludeUri.concat(path);
	}
	
	private static String getWebIncludeUri() {
		String uri = getProperties().getString("web.include");
		FileName.Calendar_CSS.setPackageUrl(uri);
		return uri;
	}
	
	public static ActionServiceBusConfiguration getConfiguration() {
		if (configuration == null) {
			synchronized (bus) {
				configuration = new SLoginActionServiceBusConfiguration().setBus(bus);
				LogUtil.setPackRoot(getProperties().getString("BusConfigurationRoot"));
			}
		}
		return configuration;
	}

	public static Bus getBus() {
		if (bus==null) {
			bus = new Bus();
			getConfiguration();
		}
		return bus;
	}
	
	public static void executeSqlUpdate(String sql) {
		BaseDAO dao = new BaseDAO();
		try {
			TransactionManager.begin();
			dao.getSQLQuery(sql).executeUpdate();
			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();
			LogUtil.error("executeSqlUpdate error "+sql, e);
		}
	}
	
	public static <T extends AbstractDomain> void remove(T domain) {
		BaseDAO dao = new BaseDAO();
		try {
			TransactionManager.begin();
			dao.remove(domain);
			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();
			LogUtil.error("saveOrUpdate error "+domain, e);
		}
	}
	
	public static <T extends AbstractDomain> T saveOrUpdate(T domain) {
		BaseDAO dao = new BaseDAO();
		T tsaved = null;
		try {
			if (domain instanceof Serialy)		((Serialy) domain).genSerialNumber();
			TransactionManager.begin();
			tsaved = dao.saveOrUpdate(domain);
			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();
			LogUtil.error("saveOrUpdate error "+domain, e);
		}
		return tsaved;
	}
	
	@SuppressWarnings("rawtypes")
	public static Object runServiceMethod(Object bean, String method, Class[] paramClasses, Object[] paramValues) {
		Object rtn = null;
		TransactionManager.begin();
		try {
			ReflectHelper.invokeMethod(bean, method, paramClasses, paramValues);
			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();
			RuntimeException re = LogUtil.getRuntimeException(e);
			LogUtil.error("runServiceMethod error "+bean+"."+method, e);
			throw re;
		}
		return rtn;
	}
	
	/**
	 * 取业务Action执行通道，并跑服务
	 * @param action
	 * @return
	 */
	public static Object runServiceChannel(ActionEnum action, Object... objects) {
		BaseDAO dao = new BaseDAO();
		dao.getDomainChangeTracing().getActonList().add(action);
		if (objects[0] instanceof ViewData) {
			ViewData<?> viewData = (ViewData<?>)objects[0];
			dao.getDomainChangeTracing().setUserName(viewData.getCurrentUser());
		}
		Object rtn = null;
		ServiceChannel channel=null;
		TransactionManager.begin();
		try {
			channel = getBus().open(action);
			rtn = channel.run(objects);
			TransactionManager.commit();
			if (TransactionManager.isRealCommit())			channel.runAftertrans(objects);
		} catch (Exception e) {
			TransactionManager.rollback();
			LogUtil.error("runServiceChannel error "+action.name());
			throw LogUtil.getRuntimeException(e);
		}finally{
			channel.close();
		}
		return rtn;
	}
	
	/**
	 * 取业务ActionList执行通道，并跑服务
	 */
	public static Object runServiceChannel(ActionEnum[] actionList, Object... objects) {
		if(actionList.length==1){
			return runServiceChannel(actionList[0],objects);
		}
		BaseDAO dao = new BaseDAO();
		dao.getDomainChangeTracing().getActonList().addAll(Arrays.asList(actionList));
		if (objects[0] instanceof ViewData) {
			ViewData<?> viewData = (ViewData<?>)objects[0];
			dao.getDomainChangeTracing().setUserName(viewData.getCurrentUser());
		}
		Object rtn = null;
		TransactionManager.begin();
		try {
			for (Action action: actionList) {
				ServiceChannel channel = getBus().open(action);
				rtn = channel.run(objects);
				channel.close();
			}
			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();
			LogUtil.error("runServiceChannel error "+Arrays.toString(actionList));
			throw LogUtil.getRuntimeException(e);
		}
		return rtn;
	}
}
