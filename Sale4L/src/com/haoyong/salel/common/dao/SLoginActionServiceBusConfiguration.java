package com.haoyong.salel.common.dao;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sf.mily.bus.Bus;
import net.sf.mily.bus.annotation.ActionService;
import net.sf.mily.bus.service.Action;
import net.sf.mily.bus.service.ActionComparator;
import net.sf.mily.bus.service.ActionServiceBusConfiguration;
import net.sf.mily.bus.service.Service;
import net.sf.mily.bus.service.ViewDataProxyMethodService;
import net.sf.mily.util.LogUtil;

import org.apache.commons.lang.ClassUtils;
import org.junit.Test;

import com.haoyong.salel.common.form.ActionEnum;
import com.haoyong.salel.common.form.MatchActions;
import com.haoyong.salel.util.SLoginUtil;

public class SLoginActionServiceBusConfiguration extends ActionServiceBusConfiguration {

	/**
	 * 返回这个配置的详细信息。 <B>debug级别的日志能看到最详尽的信息。</B>
	 */
	@Test
	public void report() {
		Bus bus = new Bus();
		this.setBus(bus);
		StringBuilder sb = new StringBuilder("详情如下：\n");
		sb.append("系统的Action配置中， 共配置了 ").append(ActionEnum.values().length).append(" 个Action。");
		sb.append("服务方法上一共有 ").append(bus.getCommandList().size()).append(" 个 Action的显式标注。");

		sb.append("详情如下：\n");
		sb.append("Bus实例为 ").append(bus.toString()).append("\n");
		Action[] actions = bus.getCommandList().toArray(new ActionEnum[]{});
		Arrays.sort(actions);
		for (Action action : actions) {
			int serviceListSize = bus.get(action).size();
//			if (serviceListSize == 1)			continue;
			String ticketName = action.getTicketClass()==null? "": action.getTicketClass().getSimpleName();
			sb.append(ticketName).append(" 的 ").append(action.getName()).append(" Action 会被以下Service处理了： \n");
			for (Iterator<Service> iter = bus.get(action).iterator(); iter.hasNext();) {
				ViewDataProxyMethodService proxyMethodService = (ViewDataProxyMethodService)iter.next();
				sb.append(proxyMethodService.getPriority().name()).append(" ");
				sb.append(proxyMethodService.getMethod().toString());
				sb.append(" 方法\n");
			}
			sb.append("\n");
		}
		LogUtil.debug(sb.toString());
	}

	@SuppressWarnings("unchecked")
	protected void analyzeService() throws InstantiationException, IllegalAccessException {
		Bus bus = getBus();
		// 根据 Serice上配置的Action列表， 整理出 Service 配置的Action 关系， actionMap并没有包含
		// Action之间的关系逻辑。
		// 这段分析过程通常只需执行一遍。
		String rootPackage = SLoginUtil.getProperties().getString("BusConfigurationRoot");
		List<Class<?>> classSet = this.loadClassList(rootPackage);
		for (Class<?> clazz : classSet) {
			ActionService actionService = clazz.getAnnotation(ActionService.class);
			if (actionService != null) {
				for (Method m : clazz.getMethods()) {
					MatchActions ma = null;
					ma = m.getAnnotation(MatchActions.class);
					if (ma != null) { // MatchActions标注在实现类的Method上
						for (ActionEnum actionEnum : ma.value()) {
							ViewDataProxyMethodService service = new ViewDataProxyMethodService(m, clazz, ma.priority());
							settingService(service, actionEnum);
							bus.addService(actionEnum, service);
						}
					} else { // 找接口
						List<Class<?>> faces = ClassUtils.getAllInterfaces(clazz);
						for (Class<?> f : faces) {
							try {
								Method fm = f.getDeclaredMethod(m.getName(), m.getParameterTypes());
								ma = fm.getAnnotation(MatchActions.class);
								if (ma != null) { // MatchActions 标注在接口的方法上
									for (ActionEnum actionEnum : ma.value()) {
										ViewDataProxyMethodService service = new ViewDataProxyMethodService(m, clazz, ma.priority());
										settingService(service, actionEnum);
										bus.addService(actionEnum, service);
									}
								}
							} catch (SecurityException e) {
								// 服务接口不应该有安全异常
							} catch (NoSuchMethodException e) {
								// 接口没有对应方法表明不是服务接口定义的方法，也不应该有Action匹配
							}
						}
					}

				}
			}
		}
		ActionComparator comparator = new ActionComparator();
		for (Action action : bus.getCommandList()) {
			bus.get(action).sort(comparator);
		}
	}
}
