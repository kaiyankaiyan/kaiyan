package com.haoyong.sales.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import net.sf.mily.bus.Bus;
import net.sf.mily.bus.service.ActionServiceBusConfiguration;
import net.sf.mily.bus.service.ServiceChannel;
import net.sf.mily.ui.enumeration.FileName;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.PropertyMap;

import com.haoyong.sales.common.dao.BaseDAO;
import com.haoyong.sales.common.dao.DomainChangeTracing;
import com.haoyong.sales.common.dao.SSaleActionServiceBusConfiguration;
import com.haoyong.sales.common.dao.TransactionManager;
import com.haoyong.sales.common.derby.DerbyDAO;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.haoyong.sales.common.form.ActionEnum;
import com.haoyong.sales.common.form.ViewData;

/**
 * SSell项目工具类
 *
 */
public class SSaleUtil {

	private static PropertyMap properties;
	private static PropertyMap messages = PropertyMap.create("Message");
	private static ActionServiceBusConfiguration configuration;
	private static Bus bus = getBus();
	
	/**
	 * 项目配置属性
	 * @return
	 */
	public static PropertyMap getProperties() {
		if (properties==null) {
			PropertyMap props = PropertyMap.create("SSale");
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
	 * @return
	 */
	public static PropertyMap getMessages() {
		return messages;
	}
	
	public static String getWebIncludeUri() {
		String uri = getProperties().getString("web.include");
		FileName.Calendar_CSS.setPackageUrl(uri);
		return uri;
	}
	
	public static ActionServiceBusConfiguration getConfiguration() {
		if (configuration == null) {
			synchronized (bus) {
				configuration = new SSaleActionServiceBusConfiguration().setBus(bus);
				LogUtil.setPackRoot(getProperties().getString("BusConfigurationRoot"));
			}
		}
		return configuration;
	}

	public static Bus getBus() {
		if (bus == null) {
			bus = new Bus();
			getConfiguration();
		}
		return bus;
	}
	
	public static void executeDerbyUpdate(String... sqlList) {
		DerbyDAO dao = new DerbyDAO();
		try {
			TransactionManager.begin();
			for (String sql: sqlList)
				dao.getSQLQuery(sql).executeUpdate();
			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();
			throw LogUtil.getRuntimeException(e);
		}
	}
	
	public static void executeSqlUpdate(String... sqlList) {
		BaseDAO dao = new BaseDAO();
		try {
			TransactionManager.begin();
			for (String sql: sqlList)
				dao.getSQLQuery(sql).executeUpdate();
			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();
			throw LogUtil.getRuntimeException(e);
		}
	}
	
	public static <T extends AbstractDomain> T saveOrUpdate(T... domains) {
		BaseDAO dao = new BaseDAO();
		T tsaved = null;
		try {
			TransactionManager.begin();
			StringBuffer sb = new StringBuffer("\nDirectSaveToDB");
			for (T domain: domains) {
				sb.append(domain.getId()==0? "新增，": "编辑，");
				tsaved = dao.saveOrUpdate(domain);
				DomainChangeTracing.getLog().info(DomainChangeTracing.printDomains(sb.toString(), domain));
			}
			TransactionManager.commit();
		} catch (Exception e) {
			TransactionManager.rollback();
			throw LogUtil.getRuntimeException(e);
		}
		return tsaved;
	}
	
	/**
	 * 取业务Action执行通道，并跑服务
	 * @param action
	 * @return
	 */
	public static Object runServiceChannel(ActionEnum action, ViewData viewData) {
		BaseDAO dao = new BaseDAO();
		dao.getDomainChangeTracing().getActonList().add(action);
		Object rtn=null, objects[]=new Object[]{viewData};
		boolean ok = false;
		ServiceChannel channel=null;
		TransactionManager.begin();
		try {
			channel = getBus().open(action);
			rtn = channel.run(objects);
			TransactionManager.commit();
			ok = true;
		} catch (Exception e) {
			TransactionManager.rollback();
			LogUtil.error("runServiceChannel error "+action.name());
			throw LogUtil.getRuntimeException(e);
		}finally{
			channel.close();
		}
		if (ok)				channel.runAftertrans(objects);
		return rtn;
	}
}
