package com.haoyong.sales.common.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.mily.common.JdbcQueryExecutor;
import net.sf.mily.common.SessionProvider;
import net.sf.mily.util.LogUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import com.haoyong.sales.base.logic.SellerLogic;
import com.haoyong.sales.common.listener.RunnableListener;

/**
 * 单号前缀的枚举
 * 
 */
public class SerialNumberFactory extends RunnableListener {
	
	private long sellerId;
	/**
	 * 单号前缀
	 */
	private String prefix;
	/**
	 * 日期格式
	 */
	private SimpleDateFormat dateForm;
	/**
	 * 流水位长度
	 */
	private int length;
	/**
	 * 单号缓存大小
	 */
	private int catchSize;
	/**
	 * 起始的数量，加1开始使用
	 */
	private long catchStart;
	/**
	 * 单号缓存剩余可用数量
	 */
	private int catchRemain;
	
	private static ConcurrentLinkedQueue<SerialNumberFactory> statQueue = new ConcurrentLinkedQueue<SerialNumberFactory>();
	public static SimpleDateFormat Date=new SimpleDateFormat("yyMMdd"), Month=new SimpleDateFormat("MM");
	private static Date serialDate = new Date();
	
	/**
	 * 初始化
	 */
	@Test
	public void runTask() {
		Date date = new Date();
		if (!DateUtils.isSameDay(serialDate, date)) {
			for (SerialNumberFactory s: new ArrayList<SerialNumberFactory>(statQueue)) {
				if (!s.dateForm.format(serialDate).equals(s.dateForm.format(date))) {
					statQueue.remove(s);
				}
			}
			serialDate = date;
		}
	}
	
	public void runAfter() {
		new SessionProvider().clear();
	}

	@Test
	private void test() {
		StringBuffer sb = new StringBuffer();
		for (int isize=5,i=5*3; i-->0;) {
			String serial = new SerialNumberFactory().serialLen2("TDate", SerialNumberFactory.Date, isize).getNextSerial();
			sb.append(serial).append(",");
		}
		LogUtil.info(sb.toString());
		sb = new StringBuffer();
		for (int isize=5,i=5*3; i-->0;) {
			String serial = new SerialNumberFactory().serialLen2("TMonth", SerialNumberFactory.Month, isize).getNextSerial();
			sb.append(serial).append(",");
		}
		LogUtil.info(sb.toString());
		sb = new StringBuffer();
		for (int isize=5,i=5*3; i-->0;) {
			String serial = new SerialNumberFactory().serialLen2("TNone", null, isize).getNextSerial();
			sb.append(serial).append(",");
		}
		LogUtil.info(sb.toString());
	}
	
	// 默认流水号长度为 2
	public SerialNumberFactory serialLen2(String sprefix, SimpleDateFormat dateForm, int catchSize) {
		if (dateForm!=null)		sprefix=sprefix.concat(new SerialNumberFactory().getFormatedDate(dateForm));
		SerialNumberFactory serial = getStat(sprefix);
		if (serial == null) {
			serial = new SerialNumberFactory();
			serial.dateForm = dateForm;
			serial.prefix = sprefix;
			serial.catchSize = catchSize;
			serial.catchRemain = 0;
			setStat(sprefix, serial);
		}
		serial.length = 2;
		return serial;
	}
	
	// 默认流水号长度为 3
	public SerialNumberFactory serialLen3(String sprefix, SimpleDateFormat dateForm, int catchSize) {
		if (dateForm!=null)		sprefix=sprefix.concat(new SerialNumberFactory().getFormatedDate(dateForm));
		SerialNumberFactory serial = getStat(sprefix);
		if (serial == null) {
			serial = new SerialNumberFactory();
			serial.dateForm = dateForm;
			serial.prefix = sprefix;
			serial.catchSize = catchSize;
			serial.catchRemain = 0;
			setStat(sprefix, serial);
		}
		serial.length = 3;
		return serial;
	}
	
	private SerialNumberFactory getStat(String prefix) {
		for (SerialNumberFactory serial: new ArrayList<SerialNumberFactory>(statQueue)) {
			if (serial.getSellerId()==this.getSellerId() && StringUtils.equals(serial.prefix, prefix)) {
				return serial;
			}
		}
		return null;
	}
	
	private void setStat(String prefix, SerialNumberFactory serial) {
		serial.sellerId = this.getSellerId();
		statQueue.add(serial);
	}
	
	private long getSellerId() {
		if (this.sellerId == 0)
			this.sellerId = SellerLogic.getSellerId();
		return this.sellerId;
	}

	public int getLength() {
		return length;
	}

	public void setCatchSize(int catchSize) {
		this.catchSize = catchSize;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	private String getFormatedDate(SimpleDateFormat dateForm) {
		String s = dateForm==null? "": dateForm.format(new Date());
		if (dateForm==Month) {
			s = this.toSerial36(Long.valueOf(s));
		}
		return s;
	}

	public String getNextSerial36() {
		if (this.catchRemain==0) {
			this.gotCatch();
		}
		String serial = this.toSerial36(this.catchStart+1);
		this.catchStart++;
		this.catchRemain--;
		return prefix.concat(serial);
	}
	
	/**
	 * @param prefix 前缀
	 * @param length 顺序号位数
	 */
	public String getNextSerial() {
		if (this.catchRemain==0) {
			this.gotCatch();
		}
		String serial = this.toSerial10(this.catchStart+1);
		this.catchStart++;
		this.catchRemain--;
		return prefix.concat(serial);
	}
	
	public String getNextIndex36() {
		if (this.catchRemain==0) {
			this.gotCatch();
		}
		String serial = this.toSerial36(this.catchStart+1);
		this.catchStart++;
		this.catchRemain--;
		return serial;
	}
	
	public long getNextIndex10() {
		if (this.catchRemain==0) {
			this.gotCatch();
		}
		this.catchStart++;
		this.catchRemain--;
		return this.catchStart;
	}
	
	private void gotCatch() {
		JdbcQueryExecutor query = new JdbcQueryExecutor();
		try {
			query.setConnection(SessionProvider.getSession2().connection());
			List<List<Object>> result = query.getQueryResult("select sn from bs_SerialNumberInfo where prefix=? and sellerid=?", this.prefix, this.getSellerId());
			long from=0, to=0;
			if (result.isEmpty()) {
				from = 0;
				to = from + this.catchSize;
				query.executeUpdate("insert into bs_SerialNumberInfo(prefix, sn, sellerId, createDate) values(?,?,?,?);commit;", this.prefix, to, this.getSellerId(), new Date());
			} else {
				from = Long.parseLong(result.get(0).get(0).toString());
				to = from + this.catchSize;
				query.executeUpdate("update bs_SerialNumberInfo set sn=? where prefix=? and sellerId=?;commit;", to, this.prefix, this.getSellerId());
			}
			this.catchStart = from;
			this.catchRemain = this.catchSize;
		} finally {
			query.close();
		}
	}
	
	private String toSerial10(long sn) {
		int idx = String.valueOf(sn).length();
		if (idx>length)			this.length = idx;
		StringBuffer serial = new StringBuffer();
		for (int i=this.length-idx; i-->0;) {
			serial.append("0");
		}
		serial.append(sn);
		return serial.toString();
	}
	
	private String toSerial36(long sn) {
		int radix = 36;
		int[] intList = new int[10];
		int idx=0;
		for (long cur=sn,next=0; ; cur=next,next=0) {
			int item = Long.valueOf(cur%radix).intValue();
			intList[idx++] = item;
			next = (cur-item)/radix;
			if (next == 0)		break;
		}
		if (idx>length)			this.length = idx;
		StringBuffer serial = new StringBuffer();
		for (int d=0,i=length; i-->0;) {
			d = intList[i];
			char c = Character.toUpperCase(Character.forDigit(d, radix));
			serial.append(c);
		}
		return serial.toString();
	}
	
	@Override
	public boolean isRunnable() {
		return true;
	}
}
