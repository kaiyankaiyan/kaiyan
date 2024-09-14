package com.haoyong.salel.common.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.mily.common.JdbcQueryExecutor;
import net.sf.mily.common.SessionProvider;

import org.apache.commons.lang.time.DateUtils;

import com.haoyong.salel.common.listener.RunnableListener;

/**
 * 单号前缀的枚举
 * 
 */
public class SerialNumberFactory extends RunnableListener {
	
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
	/**
	 * 当前序号
	 */
	private String curSerial;
	
	private static LinkedHashMap<String, SerialNumberFactory> statMap = new LinkedHashMap<String, SerialNumberFactory>();
	public static SimpleDateFormat Date=new SimpleDateFormat("yyMMdd"), Year=new SimpleDateFormat("yy"), Month=new SimpleDateFormat("MM"), Still=new SimpleDateFormat("");
	private static Date serialDate = new Date();
	
	/**
	 * 初始化
	 */
	protected void runTask() {
		Date date = new Date();
		if (!DateUtils.isSameDay(serialDate, date)) {
			for (SerialNumberFactory s: new ArrayList<SerialNumberFactory>(statMap.values())) {
				if (!s.dateForm.format(serialDate).equals(s.dateForm.format(date))) {
					statMap.remove(s.prefix);
				}
			}
			serialDate = date;
		}
	}
	
	protected void runAfter() {
		new SessionProvider().clear();
	}
	
	// 默认流水号长度为 3
	public static SerialNumberFactory serialLen3(String sprefix, SimpleDateFormat dateForm, int catchSize) {
		sprefix = sprefix.concat(dateForm.format(new Date()));
		SerialNumberFactory serial = statMap.get(sprefix);
		if (serial == null) {
			serial = new SerialNumberFactory();
			serial.dateForm = dateForm;
			serial.prefix = sprefix;
			serial.length = 3;
			serial.catchSize = catchSize;
			serial.catchRemain = 0;
		}
		return serial;
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

	public String getNextSerial() {
		if (this.catchRemain==0) {
			this.gotCatch();
		}
		String serial = this.nextSerial36(this.curSerial);
		this.curSerial = serial;
		this.catchRemain--;
		return prefix.concat(serial);
	}
	
	/**
	 * @param prefix 前缀
	 * @param length 顺序号位数
	 */
	public String getNextSerial10(String prefix, int length) {
		this.prefix = prefix;
		this.length = length;
		if (this.catchRemain==0) {
			this.gotCatch();
		}
		String serial = String.format(new StringBuffer("%0").append(length).append("d").toString(), this.catchStart+1);
		this.curSerial = serial;
		this.catchRemain--;
		return prefix.concat(serial);
	}
	
	private void gotCatch() {
		JdbcQueryExecutor query = new JdbcQueryExecutor();
		try {
			query.setConnection(SessionProvider.getSession2().connection());
			List<List<Object>> result = query.getQueryResult("select sn from bs_SerialNumberInfo where prefix=?", this.prefix);
			long from=0, to=0;
			if (result.isEmpty()) {
				from = 0;
				to = from + this.catchSize;
				query.executeUpdate("insert into bs_SerialNumberInfo(prefix, sn, createDate) values(?,?,?);commit;", this.prefix, to, new Date());
			} else {
				from = Long.parseLong(result.get(0).get(0).toString());
				to = from + this.catchSize;
				query.executeUpdate("update bs_SerialNumberInfo set sn=? where prefix=?;commit;", to, this.prefix);
			}
			this.catchStart = from;
			this.curSerial = toSerial36(from, this.length);
			this.catchRemain = this.catchSize;
		} finally {
			query.close();
		}
	}
	
	public String toSerial36(long sn, int length) {
		int radix = 36;
		int[] intList = new int[10];
		int idx=0;
		for (long cur=sn,next=0; ; cur=next,next=0) {
			int item = Long.valueOf(cur%radix).intValue();
			intList[idx++] = item;
			next = (cur-item)/radix;
			if (next == 0)		break;
		}
		StringBuffer serial = new StringBuffer();
		for (int d=0,i=length; i-->0;) {
			d = intList[i];
			char c = Character.toUpperCase(Character.forDigit(d, radix));
			serial.append(c);
		}
		return serial.toString();
	}
	
	private String nextSerial36(String curSerial) {
		int radix = 36;
		for (int i=curSerial.length(); i-- > 0;) {
			char clast = curSerial.charAt(i);
			int dlast = Character.digit(clast, radix);
			int d = (dlast+1) % radix;
			char c = Character.toUpperCase(Character.forDigit(d, radix));
			curSerial = curSerial.substring(0, i).concat(c+"").concat(curSerial.substring(i+1));
			if (dlast < radix-1) {
				break;
			}
		}
		return curSerial;
	}
	
	protected boolean isRunnable() {
		return true;
	}
}
