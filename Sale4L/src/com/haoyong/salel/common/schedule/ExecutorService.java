package com.haoyong.salel.common.schedule;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.sf.mily.cfg.ScheduledExecutor;

import com.haoyong.salel.base.service.SerialNumberInfoLisenter;
import com.haoyong.salel.common.dao.SLoginSessionProvider;
import com.haoyong.salel.common.dao.SerialNumberFactory;
import com.haoyong.salel.common.listener.AsellerRunnableListener;
import com.haoyong.salel.common.listener.DatabaseRunnableListener;
import com.haoyong.salel.common.listener.DerbyServerListener;
import com.haoyong.salel.common.listener.SchemaUpdateListener;
import com.haoyong.salel.common.listener.SellerRunnableListener;
import com.haoyong.salel.common.listener.TestSellerRunnableListener;
import com.haoyong.salel.common.listener.UpdateRightListener;
import com.haoyong.salel.util.SLoginUtil;

/**
 * 网站定期执行计划 服务
 *
 */
public class ExecutorService extends HttpServlet {
	
	private static final long serialVersionUID = 2349080051830294598L;
	
	private static ScheduledExecutorService multiExecutor = Executors.newScheduledThreadPool(10);
	private static ScheduledExecutorService singleExecutor = Executors.newScheduledThreadPool(1);
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		execute(new ScheduledExecutor());
		SLoginUtil.getWebIncludeUrl("");
		singleExecutor.schedule(new DerbyServerListener(), 1, TimeUnit.SECONDS);
		if (1==1) {
			return ;
		}
		appendStartService();
		appendEveryDayService();
		appendEveryMinuteService();
		appendEveryMonthService();
		appendEveryHourService();
	}
	
	public static void execute(Runnable command) {
		multiExecutor.execute(command);
	}
	
	/**
	 * 添加网站启动立即执行的计划
	 */
	private void appendStartService() {
		long delay = 5;
		singleExecutor.schedule(new SLoginSessionProvider(), delay, TimeUnit.SECONDS);
		singleExecutor.schedule(new SchemaUpdateListener(), delay, TimeUnit.SECONDS);
		singleExecutor.schedule(new SerialNumberFactory(), delay, TimeUnit.SECONDS);
		// 更新功能点列表
		singleExecutor.schedule(new DatabaseRunnableListener("com.haoyong.sales.common.listener.SchemaUpdateListener"), delay, TimeUnit.SECONDS);
		singleExecutor.schedule(new AsellerRunnableListener("com.haoyong.sales.common.derby.DerbyUpdateListener"), delay, TimeUnit.SECONDS);
		singleExecutor.schedule(new UpdateRightListener(), delay, TimeUnit.SECONDS);
		singleExecutor.schedule(new SellerRunnableListener("com.haoyong.sales.common.listener.UpdateRightListener"), delay, TimeUnit.SECONDS);
	}
	
	/**
	 * 添加网站每月指定一天0：00执行的计划
	 */
	private void appendEveryMonthService() {
		Calendar cur = Calendar.getInstance();
		long period = TimeUnit.DAYS.toSeconds(1);
		long initialDelay = period + TimeUnit.HOURS.toSeconds(1) - TimeUnit.HOURS.toSeconds(cur.get(Calendar.HOUR_OF_DAY)) - TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE)) - cur.get(Calendar.SECOND);
		// 每月1号删除半年前的最大单号记录
		singleExecutor.scheduleAtFixedRate(new SerialNumberInfoLisenter(), initialDelay, period, TimeUnit.SECONDS);
		// 每月1号商家删除半年前的最大单号记录
		singleExecutor.scheduleAtFixedRate(new SellerRunnableListener("com.haoyong.sales.base.service.SerialNumberInfoLisenter"), initialDelay, period, TimeUnit.SECONDS);
		// 每月1号商家月出入库数量统计
		singleExecutor.scheduleAtFixedRate(new SellerRunnableListener("com.haoyong.sales.sale.service.StoreMonthLisenter"), initialDelay, period, TimeUnit.SECONDS);
		// 每月1号商家月订单销售量统计
		singleExecutor.scheduleAtFixedRate(new SellerRunnableListener("com.haoyong.sales.common.listener.MonthCalculateListener"), initialDelay, period, TimeUnit.SECONDS);
	}
	
	/**
	 * 添加网站每天定时间点执行的计划
	 */
	private void appendEveryTimeListener(Runnable listener, String...times) {
		Calendar cur=Calendar.getInstance(), time=Calendar.getInstance();
		long period = TimeUnit.DAYS.toSeconds(1);
		for (String t: times) {
			String[] slist = t.split(":");
			int hour=Integer.valueOf(slist[0]), minute=Integer.valueOf(slist[1]);
			time.set(Calendar.HOUR_OF_DAY, hour);
			time.set(Calendar.MINUTE, minute);
			long initialDelay = (period -(TimeUnit.HOURS.toSeconds(cur.get(Calendar.HOUR_OF_DAY))+TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE))+cur.get(Calendar.SECOND))
					+(TimeUnit.HOURS.toSeconds(time.get(Calendar.HOUR_OF_DAY))+TimeUnit.MINUTES.toSeconds(time.get(Calendar.MINUTE))+time.get(Calendar.SECOND)))
					%period;
			singleExecutor.scheduleAtFixedRate(listener, initialDelay, period, TimeUnit.SECONDS);
		}
	}
	
	/**
	 * 添加网站每天0：00执行的计划
	 */
	private void appendEveryDayService() {
		Calendar cur = Calendar.getInstance();
		long period = TimeUnit.DAYS.toSeconds(1);
		// 每天00:00点处理的------------------------------------------------------
		long initialDelay = (period - TimeUnit.HOURS.toSeconds(cur.get(Calendar.HOUR_OF_DAY)) - TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE)) - cur.get(Calendar.SECOND))%period;
		singleExecutor.scheduleAtFixedRate(new SerialNumberFactory(), initialDelay, period, TimeUnit.SECONDS);

		// 每天01:00点处理的------------------------------------------------------
		initialDelay = (period + TimeUnit.HOURS.toSeconds(1) - TimeUnit.HOURS.toSeconds(cur.get(Calendar.HOUR_OF_DAY)) - TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE)) - cur.get(Calendar.SECOND))%period;
		singleExecutor.scheduleAtFixedRate(new TestSellerRunnableListener("com.haoyong.sales.common.listener.TestCaseListener"), initialDelay, period, TimeUnit.SECONDS);
		// 商家日销售量统计
//		singleExecutor.scheduleAtFixedRate(new SellerRunnableListener("com.haoyong.sales.common.listener.DayCalculateListener"), initialDelay, period, TimeUnit.SECONDS);
	}
	
	/**
	 * 添加网站每分钟执行的计划
	 */
	private void appendEveryMinuteService() {
		Calendar cur = Calendar.getInstance();
		// 每隔5分钟整分钟时执行------------------------------------------------------
		long circle = 5;
		long period = TimeUnit.MINUTES.toSeconds(circle);
		long initialDelay = period - TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE)%circle) - cur.get(Calendar.SECOND);
		// 统计待处理记录数量
		singleExecutor.scheduleAtFixedRate(new SellerRunnableListener("com.haoyong.sales.common.listener.RemindCountListener"), initialDelay, period, TimeUnit.SECONDS);
		singleExecutor.scheduleAtFixedRate(new SellerRunnableListener("com.haoyong.sales.common.listener.RemindInstallorListener"), initialDelay, period, TimeUnit.SECONDS);
//		singleExecutor.scheduleAtFixedRate(new SellerRunnableListener("com.haoyong.sales.custom.MinuteShunfengListener"), initialDelay, period, TimeUnit.SECONDS);
	}
	
	/**
	 * 添加网站每小时执行的计划
	 */
	private void appendEveryHourService() {
//		appendEveryTimeListener(new SerialNumberInfoLisenter(),"01:00");
		Calendar cur = Calendar.getInstance();
		long circle = 3;
		long period = TimeUnit.HOURS.toSeconds(circle);
		long initialDelay = period - TimeUnit.HOURS.toSeconds(cur.get(Calendar.HOUR_OF_DAY)%circle) - TimeUnit.MINUTES.toSeconds(cur.get(Calendar.MINUTE)) - cur.get(Calendar.SECOND);
//		singleExecutor.scheduleAtFixedRate(new TestSellerRunnableListener("com.haoyong.sales.common.listener.TestCaseListener"), initialDelay, period, TimeUnit.SECONDS);
//		singleExecutor.scheduleAtFixedRate(new SellerRunnableListener("com.haoyong.sales.custom.HourShunfengListener"), initialDelay, period, TimeUnit.SECONDS);
	}
}
