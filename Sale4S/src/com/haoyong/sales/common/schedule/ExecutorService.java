package com.haoyong.sales.common.schedule;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.sf.mily.cfg.ScheduledExecutor;

import com.haoyong.sales.common.dao.SSaleSessionProvider;
import com.haoyong.sales.common.dao.SerialNumberFactory;
import com.haoyong.sales.common.listener.TestCaseListener;
import com.haoyong.sales.util.SSaleUtil;

/**
 * 网站定期执行计划 服务
 *
 */
public class ExecutorService extends HttpServlet implements ServletContextListener {
	
	private static final long serialVersionUID = 2349080051830294598L;
	
	private static ScheduledExecutorService multipleExecutor = Executors.newScheduledThreadPool(10);
	private static ScheduledExecutorService singleExecutor = Executors.newScheduledThreadPool(1);
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		single(new ScheduledExecutor());
		SSaleUtil.getWebIncludeUri();
		appendStartService();
		appendEveryDayService();
		appendEveryMinuteService();
		appendEveryMonthService();
		appendEveryHourService();
	}
	
	public static void multiple(Runnable command) {
		multipleExecutor.execute(command);
	}
	
	public static void single(Runnable command) {
		if (command instanceof TestCaseListener)
			multipleExecutor.execute(command);
		else
			singleExecutor.execute(command);
	}

	private void appendStartService() {
		long delay = 5;
		singleExecutor.schedule(new SSaleSessionProvider(), delay, TimeUnit.SECONDS);
		singleExecutor.schedule(new SerialNumberFactory(), delay, TimeUnit.SECONDS);
	}

	private void appendEveryDayService() {
		Calendar cur = Calendar.getInstance();
		long period = TimeUnit.DAYS.toSeconds(1);
		// 每天00:00点处理的
		long initialDelay = (period - TimeUnit.HOURS.toSeconds(cur.get(Calendar.HOUR_OF_DAY)) - TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE)) - cur.get(Calendar.SECOND))%period;
	}
	
	private void appendEveryMonthService() {
		Calendar cur = Calendar.getInstance();
		long period = TimeUnit.DAYS.toSeconds(1);
		long initialDelay = period + TimeUnit.HOURS.toSeconds(1) - TimeUnit.HOURS.toSeconds(cur.get(Calendar.HOUR_OF_DAY)) - TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE)) - cur.get(Calendar.SECOND);
	}
	
	private void appendEveryMinuteService() {
		Calendar cur = Calendar.getInstance();
		// 每隔5分钟整分钟时执行------------------------------------------------------
		long circle = 5;
		long period = TimeUnit.MINUTES.toSeconds(circle);
		long initialDelay = period - TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE)%circle) - cur.get(Calendar.SECOND);
	}
	
	private void appendEveryHourService() {
	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		ScheduledExecutor.getExecutor().shutdownNow();
		singleExecutor.shutdownNow();
		multipleExecutor.shutdownNow();
        System.exit(0);
	}

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
	}
}
