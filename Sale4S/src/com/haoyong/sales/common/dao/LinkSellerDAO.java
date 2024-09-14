package com.haoyong.sales.common.dao;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.mily.common.SessionProvider;
import net.sf.mily.support.throwable.LogicException;
import net.sf.mily.types.TypeFactory;
import net.sf.mily.ui.WindowMonitor;
import net.sf.mily.util.LogUtil;
import net.sf.mily.util.PerformTrace;
import net.sf.mily.util.PerformTrace.TraceCombine;
import net.sf.mily.util.PerformTrace.TraceItem;
import net.sf.mily.util.ReflectHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.haoyong.sales.base.domain.Seller;
import com.haoyong.sales.common.domain.AbstractDomain;
import com.mysql.jdbc.Field;

/**
 * 所有DAO的父类 管理与数据库的所有操作
 */
public class LinkSellerDAO {
	
	/**
	 * 获得查询结果集
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> List<T> nativeQuery(String sql, Class<T> clazz, Object... params) {
		TraceItem traceItem = getTrace().onceStart("nativeQuery");
		List<HashMap<String, Object>> slist = queryGson(sql, params);
		List<T> list = new ArrayList<T>(slist.size());
		Gson gson = new Gson();
		Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
		for (HashMap<String, Object> row: slist) {
			String sgson = gson.toJson(row);
			T t = gson.fromJson(sgson, clazz);
			t.getVoParamMap().put("NativeQueryRow", row);
			list.add(t);
		}
		traceItem.onceEnd();
		return list;
	}

	/**
	 * 获得查询的单一结果
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> T nativeQuerySingleResult(String sql, Class<T> clazz, Object... params) {
		List<T> list = nativeQuery(sql, clazz, params);
		if (list.size()==1) {
			return list.get(0);
		} else if (list.size()==0) {
			return null;
		} else {
			throw new LogicException(2, "获得查询的单一记录失败");
		}
	}

	/**
	 * 获得查询的第一个结果
	 */
	@SuppressWarnings("unchecked")
	public <T extends AbstractDomain> T nativeQueryFirstResult(String sql, Class<T> clazz, Object... params) {
		List<T> list = nativeQuery(sql, clazz, params);
		if (list.size()>0) {
			return list.get(0);
		} else if (list.size()==0) {
			return null;
		} else {
			throw new LogicException(2, "获得查询的第一个记录失败");
		}
	}
	
	private List<HashMap<String, Object>> queryGson(String sql, Object... params){
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		try {
			sql = this.getParametedSql(sql, params);
			LogUtil.info(sql);
			ps = this.getConnection().prepareStatement(sql);
			resultSet = ps.executeQuery();
			Field[] columns = (Field[])ReflectHelper.getPropertyValue(resultSet, "fields");
			while(resultSet.next()){
				int icol=0;
				HashMap<String, Object> row = new HashMap<String, Object>();
				for (Field column: columns) {
					icol++;
					Object d = resultSet.getObject(icol), fd=d;
					if (d != null) {
						if (d instanceof Date)		fd = null;
						row.put(column.getName(), fd);
					}
				}
				list.add(row);
			}
		} catch (Exception e) {
			throw LogUtil.getRuntimeException("查询失败",e);
		} finally {
			try {
				if (resultSet!=null)	resultSet.close();
			} catch (Exception e) {
				//
			}
			try {
				if (ps!=null) 			ps.close();
			} catch (Exception e) {
				//
			}
		}
		return list;
	}
	
	private TraceCombine getTrace() {
		return PerformTrace.getTraceCombine(this);
	}

	private Connection getConnection() {
		String name = new StringBuffer("proxool.").append(this.getLinkSeller().getSqlName()).toString();
		Connection conn = new SessionProvider().getConnection(name, new String[]{name});
		return conn;
	}

	private Seller getLinkSeller() {
		Seller seller = (Seller)WindowMonitor.getMonitor().getAttribute("LinkSeller");
		return seller;
	}

	public void setLinkSeller(Seller seller) {
		WindowMonitor.getMonitor().addAttribute("LinkSeller", seller);
	}

	public String getParametedSql(String sql, Object... params0) {
		Matcher m = Pattern.compile("(\\s*)[(\\=)|(\\>)|(\\<)|(\\>=)|(\\<=)]?(\\s*)(\\?)").matcher(sql);
		StringBuffer sb = new StringBuffer();
		Object[] params = new Object[params0.length+1];
		for (int i=params0.length; i-->0;) {
			params[i] = params0[i];
		}
		params[params0.length] = this.getLinkSeller().getId();
		for (int msize=0, si=0, pi=0, slen=sql.length(); si<slen; si++) {
			if (m.find()) {
				msize++;
				String sitem = sql.substring(m.start(), m.end());
				Object param = params[pi++];
				sb.append(sql.substring(si, m.start()));
				if (param != null) {
					sb.append(sitem.replaceFirst("\\?", TypeFactory.createType(param.getClass()).sqlFormat(param)));
				} else {
					sb.append(" is null");
				}
				si = m.end()-1;
			} else {
				sb.append(sql.substring(si, sql.length()));
				si = sql.length()-1;
			}
			if (si==sql.length()-1 && msize<params.length)
				throw new LogicException(2, "查询Sql?个数不够放置参数个数");
		}
		LogUtil.info(sb.toString());
		return sb.toString();
	}
}
