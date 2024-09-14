package com.haoyong.sales.sale.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class TicketUser {

	/**
	 * 下单人
	 */
	private String user;
	
	/**
	 * 下单人备注
	 */
	private String remark;
	
	/**
	 * 下单日期
	 */
	private Date date = new Date();
	
	private static SimpleDateFormat format = new SimpleDateFormat("MM/ddHH:mm");

	public String toString() {
		return this.getUserDate();
	}
	
	public String getUserDate() {
		StringBuffer sb = new StringBuffer();
		sb.append(user);
		sb.append(format.format(date));
		return sb.toString();
	}
	
	public String addUserDate(String sourceUserDate) {
		if (sourceUserDate==null)			sourceUserDate="";
		Matcher m = Pattern.compile("\\d{2}\\/\\d{2}").matcher(sourceUserDate);
		int di=5;
		String prevDate=null, cur=format.format(date), curDate=cur.substring(0,di), curTime=cur.substring(di+1);
		while (m.find()) {
			prevDate = sourceUserDate.substring(m.start(), m.end());
		}
		StringBuffer sb = new StringBuffer(sourceUserDate).append("¸");
		sb.append(user).append(curDate.equals(prevDate)? curTime: cur);
		sb.append(StringUtils.isBlank(remark)? "": this.remark);
		return sb.toString();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
